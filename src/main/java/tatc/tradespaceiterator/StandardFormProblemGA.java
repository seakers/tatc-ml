/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.tradespaceiterator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.BufferedWriter;
import tatc.architecture.variable.MonolithVariable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hipparchus.util.FastMath;
import org.moeaframework.core.Solution;
import org.moeaframework.problem.AbstractProblem;
import org.orekit.errors.OrekitException;
import org.orekit.utils.Constants;
import seakers.conmop.util.Bounds;
import seakers.orekit.util.Units;
import org.moeaframework.core.variable.RealVariable;
import seakers.conmop.util.Factor;
import tatc.evaluation.costandrisk.Constellation;
import tatc.evaluation.costandrisk.Context;
import tatc.architecture.SpecialOrbit;
import tatc.evaluation.costandrisk.Launch;
import tatc.architecture.specifications.LaunchVehicleSpecification;
import tatc.evaluation.costandrisk.MasterInput;
import tatc.evaluation.costandrisk.Payload;
import tatc.evaluation.costandrisk.ResultOutput;
import tatc.evaluation.costandrisk.Spacecraft;
import tatc.exceptions.CostRiskException;
import tatc.exceptions.ReductionMetricsException;
import tatc.architecture.MissionConcept;
import tatc.architecture.StandardFormArch;
import tatc.architecture.TATCArchitecture;
import tatc.architecture.TATCWalker;
import tatc.architecture.specifications.DSMSpecification;
import tatc.architecture.specifications.GroundStationSpecification;
import tatc.architecture.specifications.InstrumentSpecification;
import tatc.architecture.specifications.MonoSpecification;
import tatc.architecture.specifications.ObservatorySpecification;
import tatc.architecture.specifications.SatelliteOrbitSpecification;
import tatc.architecture.variable.IntegerVariable;
import tatc.evaluation.costandrisk.CostRiskSeak;
import tatc.evaluation.reductionmetrics.AbsoluteDate;
import tatc.evaluation.reductionmetrics.ReductionMetrics;
import tatc.util.JSONIO;

/**
 *
 * @author nhitomi
 */
public class StandardFormProblemGA extends AbstractProblem {

    /**
     * The set of satellites that exist already and are not decision variables
     * in the search
     */
    private final Set<MonolithVariable> existingSatellites;

    /**
     * Counts the number of evaluations so we can use it to id architectures
     */
    private int evalCounter;

    /**
     * Path to the current subspace in the DSMs directory
     */
    private File currentDSMSubspace;

    /**
     * Path to the current subspace in the Monos directory
     */
    private File currentMonoSubspace;

    /**
     * The tradespace search request
     */
    private final TradespaceSearchRequest tsr;

    private final ReductionMetrics rm;

    private final CostRiskSeak cr;

    private final SearchDatabase db;

    private final ArrayList<Double> altitudes;

    private final ArrayList<Double> inclination;

    private final ArrayList<Integer> numberOfSats;

    private final ArrayList<SpecialOrbit> specialOrbits;

    //constructor for this class -> same name as the class
    public StandardFormProblemGA(TradespaceSearchRequest tsr, Properties properties) {
        //number of variables and objectives in the abstract class
        super(5, 2);

        //tsr contains the interval values
        this.tsr = tsr;
        File root = new File(System.getProperty("tatc.root"));
        File rmPath = new File(System.getProperty("tatc.rm"));
        File crPath = new File(System.getProperty("tatc.cr"));

        this.rm = new ReductionMetrics(
                new File(rmPath, "RM_input.json"),
                new File(rmPath, "RM_output.json"), tsr, properties);
        this.cr = new CostRiskSeak(new File(crPath, String.join(File.separator,
                new String[]{"bin", "CostRisk.json"})),
                new File(crPath, String.join(File.separator,
                        new String[]{"bin", "CostRisk_Default.json"})));
//        this.cr = new CostRisk(
//                new File(crPath, String.join(File.separator,
//                        new String[]{"bin", "CostRisk.json"})),
//                new File(crPath, String.join(File.separator,
//                        new String[]{"bin", "CostRisk_Default.json"})));
        try {
            //initialize the database
            this.db = SearchDatabase.getInstance();
            for (ObservatorySpecification spec : tsr.getObervatorySpecifications()) {
                db.addObservatorySpecification(spec);
            }
            for (InstrumentSpecification spec : tsr.getInstrumentSpecifications()) {
                db.addInstrumentSpecification(spec);
            }
            for (GroundStationSpecification spec : tsr.getMissionConcept().getGroundStationSpecifications()) {
                db.addGroundStationSpecification(spec);
            }
            ArrayList<LaunchVehicleSpecification> lvSpecs = new ArrayList();
            for (LaunchVehicleSpecification spec : tsr.getLaunchVehicleSpecifications()) {
                db.addLaunchVehicleSpecification(spec);
                lvSpecs.add(spec);
            }
//            this.lvs = new LaunchVehicleSelector(lvSpecs);

            //set discrete options for altitude, inclination, num sats, num planes
            altitudes = discretizeAltitudes(tsr.getSatelliteOrbits().getSemiMajorAxisRange());
            inclination = discretizeInclinations(tsr.getSatelliteOrbits().getInclinationRangesOfInterest());

            //get any special orbits
            specialOrbits = tsr.getSatelliteOrbits().getSpecialOrbits();

            //if there are special orbits, add SSO, ISS and criticcally inclined orbits
            if (this.specialOrbits != null) {
                for (int i = 0; i < specialOrbits.size(); i++) {
                    inclination.add(this.getSpecialOrbitInclinations(specialOrbits.get(i)));
                }
            }

            numberOfSats = discretizeSatellite(tsr.getSatelliteOrbits().getNumberOfNewSatellites());

            //existing satellites must be created after the db is initilizaed with the observatories and instruments
            //more observatories and instruments will be added to the database but exclusively for the existing satellites
            AbsoluteDate startDate = AbsoluteDate.cast(
                    tsr.getMissionConcept().getPerformancePeriod()[0]);
            this.existingSatellites = tsr.getSatelliteOrbits().
                    getExistingSatellites(startDate);
            //propagate and save the access times of the existing satellitess
            Logger.getGlobal().finer("Propagating and saving accesses for existing satellites...");
            properties.setProperty("fov.saveToDB", "true");

            //don't save the access of the new satellites entering the architecture
            properties.setProperty("fov.saveToDB", "false");

            evalCounter = 0;

            currentDSMSubspace = new File(System.getProperty("tatc.dsms"),
                    "Subspace" + Integer.toString(100000 + evalCounter).substring(1));

        } catch (OrekitException ex) {
            throw new IllegalArgumentException("Invalid tradespace search request", ex);
        }
    }

    /**
     * gets inclinations for special orbits
     */
    private double getSpecialOrbitInclinations(SpecialOrbit special) {

        switch (special.toString()) {
            //identifier of SSO = -1 so that we can calculate it using alt later on
            case "SSO":
                return -1;
            case "CriticallyInclined":
                return FastMath.toRadians(63.4);
            case "ISS":
                return FastMath.toRadians(51.6);
            default:
                throw new UnsupportedOperationException(
                        String.format("Expected token SSO, Frozen, "
                                + "CriticallyInclined, or ISS. Found %s.", special));
        }
    }

    /**
     * Calculates the SSO using Altitude
     *
     * @param alt
     * @return
     */
    private double getSSOInclination(double alt) {
        double multiplicationFactor = 10.10949;
        double cos_i = (Math.pow((alt / Constants.WGS84_EARTH_EQUATORIAL_RADIUS), 3.5))
                / (-multiplicationFactor);
        double incl = FastMath.toRadians(180 * Math.acos(cos_i) / 3.1415);
        return incl;
    }

    /**
     * Discretizes a range of altitudes
     *
     * @param lowerbound
     * @param upperboud
     * @return the discrete values for altitudes
     */
    private ArrayList<Double> discretizeAltitudes(Bounds<Double> bounds) {
        //these altitudes are to be changed according to Sreeja's code, instead of 10 alt ranges

        double l = bounds.getLowerBound();
        double u = bounds.getUpperBound();
        ArrayList<Double> alts = new ArrayList<>();

        double step = (u - l) / 4;
        for (double count = l; count <= u; count += step) {
            alts.add(count);
        }
        return alts;
    }

    /**
     * Discretizes a range of inclination
     *
     * @param lowerbound
     * @param upperboud
     * @return the discrete values for inclinations
     */
    private ArrayList<Double> discretizeInclinations(Bounds<Double> bounds) {

        double l = bounds.getLowerBound();
        double u = bounds.getUpperBound();
        ArrayList<Double> incl = new ArrayList<>();

        double step = (u - l) / 6;
        for (double count = l; count <= u; count += step) {
            incl.add(count);
        }

        return incl;
    }

    /**
     * Discretizes a range of number of satellites
     *
     * @param lowerbound
     * @param upperboud
     * @return the discrete values for number of satellites
     */
    private ArrayList<Integer> discretizeSatellite(Bounds<Integer> bounds) {
        int l = bounds.getLowerBound();
        int u = bounds.getUpperBound();
        ArrayList<Integer> sats = new ArrayList<>();

        for (int count = l; count <= u; count++) {
            sats.add(count);
        }
        return sats;
    }

    @Override
    public String getName() {
        return "DSM Tradespace Search";
    }

    @Override
    public final void evaluate(Solution solution) {
        //create subspace directory
        currentDSMSubspace = new File(System.getProperty("tatc.dsms"),
                "Subspace" + Integer.toString(100000 + evalCounter).substring(1));
        currentDSMSubspace.mkdir();

        StandardFormArch soln = null;
        if (solution instanceof StandardFormArch) {
            soln = (StandardFormArch) solution;
        } else {
            throw new IllegalArgumentException(
                    String.format("Expected a TATCArchitecture."
                            + " Found %s", solution.getClass()));
        }

        //read in values
        double alt = altitudes.get(((IntegerVariable) soln.getVariable(0)).getValue());
        double incl = inclination.get(((IntegerVariable) soln.getVariable(1)).getValue());

        //if there is an SSO, calculate it using the alt chosen
        if (incl == -1) {
            incl = this.getSSOInclination(alt);
        }

        int numSats = numberOfSats.get(((IntegerVariable) soln.getVariable(2)).getValue());

        //need to convert the real value that's between [0,1] to the number of planes. 
        //The available number of planes is listed in the line below
        List<Integer> possiblePlanes = Factor.divisors(numSats); //planes would be divisors

        HashMap<Double, Integer> mappedPlanes = new HashMap<>();

        int counterPlanes = 0; //counter for possible planes index

        for (double i = 0; i <= 1; i += 1. / (possiblePlanes.size() - 1)) {
            mappedPlanes.put(i, possiblePlanes.get(counterPlanes));
            counterPlanes = counterPlanes + 1;
        }

        //read in the real value of planes from the solution
        double planes = ((RealVariable) (soln.getVariable(3))).getValue();

        int p = -1;
        double minDistancePlanes = Double.POSITIVE_INFINITY;
        Iterator<Double> iter1 = mappedPlanes.keySet().iterator();
        while (iter1.hasNext()) {
            double val = iter1.next();
            if (Math.abs(planes - val) < minDistancePlanes) {
                minDistancePlanes = Math.abs(planes - val);
                p = mappedPlanes.get(val);
            }
        }
        
        if (p == -1) {
            throw new IllegalStateException("Error in number of planes p = -1");
        }

        //The available number of phases is listed below
        //copying available number of planes to phases array
        //then we subtract -1 from each value to get the number of phases
        List<Integer> possiblePhases = new ArrayList<>();

        for (int i = 0; i < possiblePlanes.size(); i++) {
            possiblePhases.add(possiblePlanes.get(i) - 1);
        }

        HashMap<Double, Integer> mappedPhases = new HashMap<>();

        int counterPhases = 0; //counter for possible planes index

        for (double i = 0; i <= 1; i += 1. / (possiblePhases.size() - 1)) {
            mappedPhases.put(i, possiblePhases.get(counterPhases));
            counterPhases = counterPhases + 1;
        }
        
        //read in the real value of phases from the solution
        double phases = ((RealVariable) (soln.getVariable(4))).getValue();

        int q = -1;
        double minDistancePhases = Double.POSITIVE_INFINITY;
        Iterator<Double> iter = mappedPhases.keySet().iterator();
        while (iter.hasNext()) {
            double val = iter.next();
            if (Math.abs(phases - val) < minDistancePhases) {
                minDistancePhases = Math.abs(phases - val);
                q = mappedPhases.get(val);
            }
        }
        
        if (q == -1) {
            throw new IllegalStateException("Error in number of phases q = -1");
        }

        WalkerArchitecture arch = new WalkerArchitecture(alt, incl, numSats, p, q, existingSatellites);

        //start date and end date/coverage
        MissionConcept newConcept = tsr.getMissionConcept().copy();

        try {
            reductionAndMetrics(arch, newConcept);
            costAndRisk(arch, newConcept);
        } catch (ReductionMetricsException rmEx) {
            Logger.getLogger(StandardFormProblemGA.class.getName()).log(Level.SEVERE, null, rmEx);
            throw new IllegalStateException("Evaluation of solution in R&M failed.", rmEx);
        } catch (CostRiskException crEx) {
            Logger.getLogger(StandardFormProblemGA.class.getName()).log(Level.SEVERE, null, crEx);
            throw new IllegalStateException("Evaluation of solution in C&R failed.", crEx);
        }

        //set rm metrics
        solution.setObjective(0, rm.getMetrics()[0]); //average revisit time

        //set cr metrics
        ResultOutput crOutput = JSONIO.readJSON(cr.getOutputFile(), ResultOutput.class);
        solution.setObjective(1, crOutput.getLifecycleCost().getEstimate());

        Logger.getGlobal().fine(String.format(
                "avg revisit: %.2f[min], lifecycle cost: %.2f[$],",
                rm.getMetrics()[0] / 60.,
                solution.getObjective(1)));

        evalCounter++;
    }

    /**
     * Create and process the reduction and metrics job request
     *
     * @param arch DSM architecture to evaluate
     * @param concept the mission concept for this architecture
     */
    private void reductionAndMetrics(TATCArchitecture arch, MissionConcept concept) throws ReductionMetricsException {
        //create a R&M job request in the form a JSON
        Set<MonolithVariable> monoliths = new HashSet<>();
        monoliths.addAll(arch.getExistingSatellites());
        monoliths.addAll(arch.getNewSatellites());
        MonoSpecification[] monoSpecs = new MonoSpecification[monoliths.size()];
        int i = 0;
        for (MonolithVariable var : monoliths) {
            SatelliteOrbitSpecification orbitSpec
                    = new SatelliteOrbitSpecification(var.getEcc(), Units.rad2deg(var.getInc()),
                            Units.m2km(var.getSma()),
                            Units.rad2deg(var.getArgPer()),
                            Units.rad2deg(var.getRaan()),
                            Units.rad2deg(var.getTrueAnomaly()));

            monoSpecs[i] = new MonoSpecification(concept,
                    orbitSpec,
                    db.getObservatorySpecification(var.getObservatoryID()),
                    db.getInstrumentSpecification(var.getInstrumentID()));
            i++;
        }
        DSMSpecification dsmSpec = new DSMSpecification(concept, monoSpecs);

        //input to the r&m module - gives all information about the subspace
        JSONIO.writeJSON(rm.getInputFile(), dsmSpec);
        rm.setOutputFile(currentDSMSubspace);

        try {
            rm.call();

            //output the Manifest json file
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject monoSpec = new JsonObject();

            monoSpec.addProperty("ConstellationType", this.evalCounter);
            monoSpec.add("Mono", rm.getJSON());

            String monoStr = gson.toJson(monoSpec);
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(currentDSMSubspace, "Manifest.json")))) {
                bw.append(monoStr);
                bw.flush();
            } catch (IOException ex) {
                Logger.getLogger(JSONIO.class.getName()).log(Level.SEVERE, null, ex);
            }
            //finish writing the observatory specs in json file

        } catch (ReductionMetricsException rmEx) {
            throw rmEx;
        } catch (Exception ex) {
            Logger.getLogger(StandardFormProblemGA.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalStateException("Evaluation of solution in R&M failed.", ex);
        }
    }

    /**
     * Create and process the cost and risk job request
     *
     * @param arch DSM architecture to evaluate
     * @param concept the mission concept for this architecture
     */
    private void costAndRisk(TATCArchitecture arch, MissionConcept concept) throws CostRiskException {
        Set<MonolithVariable> monoliths = new HashSet<>();
        monoliths.addAll(arch.getExistingSatellites());
        monoliths.addAll(arch.getNewSatellites());
        Spacecraft[] spacecraft = new Spacecraft[monoliths.size()];
        int count = 0;
        for (MonolithVariable var : monoliths) {
            ObservatorySpecification observSpec = db.getObservatorySpecification(var.getObservatoryID());
            InstrumentSpecification instSpec = db.getInstrumentSpecification(var.getInstrumentID());
            Payload[] payload = new Payload[1];
            payload[0] = new Payload("inst", 1.0, "passive", 420.0, 0.0, 590.0,
                    9, "body", 9.611, 1.0);

            spacecraft[count] = new Spacecraft(
                    "3axis", observSpec.getStartMass(), "monoprop", 0., 0.,
                    1550.0, 1550.0, 1550.0, 1550.0,
                    Units.m2km(var.getSma() - Constants.WGS84_EARTH_EQUATORIAL_RADIUS),
                    Units.rad2deg(var.getInc()), Units.rad2deg(var.getRaan()),
                    0., 0., true, 0.0012, 0.0, observSpec.getCommBandTypes(),
                    1, 0, 0, 9, payload);
            count++;
        }

        Constellation constellation = new Constellation(1.0, spacecraft);

        //TODO !!!!!!!!!!!!!!!!
        //Update this to reflect launch vehicle selection process
        LaunchVehicleSpecification lvSpec = db.getLaunchVehicleSpecification(0);

        Launch launch = new Launch("Cape Canaveral", 2017, lvSpec.getName(), monoliths.size(), 1, lvSpec.getReliability());

        Set<GroundStationSpecification> gndStations = concept.getGroundStationSpecifications();
        GroundStationSpecification[] groundStations = new GroundStationSpecification[gndStations.size()];
        gndStations.toArray(groundStations);

        Context crContext = new Context(concept.getMissionDirector(), 2016, launch, groundStations);

        MasterInput crInput = new MasterInput(constellation, crContext);

        cr.setInputFile(new File(currentDSMSubspace, "CostRisk.json"));
        cr.setOutputFile(new File(currentDSMSubspace, "CostRisk_Output.json"));

        JSONIO.writeJSON(cr.getInputFile(), crInput);
        try {
            cr.call();
        } catch (CostRiskException crEx) {
            throw crEx;
        } catch (Exception ex) {
            Logger.getLogger(StandardFormProblemGA.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalStateException("Evaluation of solution in CaR failed.", ex);
        }
    }

    //structure of solution with walker params
    @Override
    public final Solution newSolution() {
        Solution sol = new StandardFormArch(getNumberOfVariables(), getNumberOfObjectives(), this.existingSatellites);
        sol.setVariable(0, new IntegerVariable(0, 0, altitudes.size() - 1));
        sol.setVariable(1, new IntegerVariable(0, 0, inclination.size() - 1));
        sol.setVariable(2, new IntegerVariable(0, 0, numberOfSats.size() - 1));
        sol.setVariable(3, new RealVariable(0, 1)); //planes
        sol.setVariable(4, new RealVariable(0, 1)); //phasing
        return sol;
    }

    //class used just in StandardForm problem
    private class WalkerArchitecture implements TATCArchitecture {

        private final Set<MonolithVariable> monos;

        /**
         * The existing satellites that are a part of the DSM but not decision
         * variables
         */
        private final Set<MonolithVariable> existingSatellites;

        public WalkerArchitecture(double altitude, double inclination,
                int numberOfSatellite, int numberOfPlanes, int phases,
                Set<MonolithVariable> existingSatellites) {
            this.existingSatellites = existingSatellites;

            TATCWalker walker = new TATCWalker(altitude, inclination, numberOfSatellite,
                    numberOfPlanes, phases);
            this.monos = walker.getSatellites();
        }

        @Override
        public Set<MonolithVariable> getNewSatellites() {
            return monos;
        }

        @Override
        public Set<MonolithVariable> getExistingSatellites() {
            return existingSatellites;
        }

        @Override
        public int getNumberOfSatellites() {
            return monos.size() + existingSatellites.size();
        }
    }

    /**
     * Shuts down the search and releases any resources. Shuts down any threads
     * dedicated to search.
     */
    public void shutdown() {
        rm.shutdown();
    }

}
