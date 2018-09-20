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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.ArrayUtils;
import org.hipparchus.util.FastMath;
import org.orekit.errors.OrekitException;
import org.orekit.utils.Constants;
import seakers.conmop.util.Bounds;
import seakers.orekit.constellations.EnumerateWalkerConstellations;
import seakers.orekit.constellations.WalkerParameters;
import seakers.orekit.util.Units;
import tatc.architecture.MissionConcept;
import tatc.architecture.SpecialOrbit;
import tatc.architecture.TATCArchitecture;
import tatc.architecture.TATCWalker;
import tatc.architecture.specifications.DSMSpecification;
import tatc.architecture.specifications.GroundStationSpecification;
import tatc.architecture.specifications.InstrumentSpecification;
import tatc.architecture.specifications.LaunchVehicleSpecification;
import tatc.architecture.specifications.MonoSpecification;
import tatc.architecture.specifications.ObservatorySpecification;
import tatc.architecture.specifications.SatelliteOrbitSpecification;
import tatc.architecture.variable.MonolithVariable;
import tatc.evaluation.costandrisk.Constellation;
import tatc.evaluation.costandrisk.Context;
import tatc.evaluation.costandrisk.CostRiskSeak;
import tatc.evaluation.costandrisk.Launch;
import tatc.evaluation.costandrisk.MasterInput;
import tatc.evaluation.costandrisk.Payload;
import tatc.evaluation.costandrisk.ResultOutput;
import tatc.evaluation.costandrisk.Spacecraft;
import tatc.evaluation.reductionmetrics.AbsoluteDate;
import tatc.evaluation.reductionmetrics.ReductionMetrics;
import tatc.exceptions.CostRiskException;
import tatc.exceptions.ReductionMetricsException;
import tatc.util.JSONIO;

/**
 *
 * @author Prachi
 */
public class StandardFormProblemFullFact {

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
    public StandardFormProblemFullFact(TradespaceSearchRequest tsr, Properties properties) {

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

//        //Cost and Risk module for integration
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

//          //Launch Vehicle options to be considered here  
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

        } catch (OrekitException ex) {
            throw new IllegalArgumentException("Invalid tradespace search request", ex);
        }
    }

    public final void evaluate() {

        //output the variables and metrics in a csv file
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(System.getProperty("tatc.moea", "results"), "results.csv")))) {

            bufferedWriter.append("Altitude[m],Inclination[deg],Satellites,Planes,Phase,Avg Revisit Time[min],Cost[FY10$M]");
            bufferedWriter.newLine();

            //convert arraylists to array in order to pass into fullFactWalker
            Double[] altArray = new Double[altitudes.size()];
            altArray = altitudes.toArray(altArray);

            Double[] incArray = new Double[inclination.size()];
            incArray = inclination.toArray(incArray);

            Integer[] numSatsArray = new Integer[numberOfSats.size()];
            numSatsArray = numberOfSats.toArray(numSatsArray);

            ArrayList<WalkerParameters> constellationParams = new ArrayList<>();
            constellationParams = EnumerateWalkerConstellations.fullFactWalker(ArrayUtils.toPrimitive(altArray),
                    ArrayUtils.toPrimitive(incArray),
                    ArrayUtils.toPrimitive(numSatsArray));

            for (WalkerParameters params : constellationParams) {

                //create subspace directory
                currentDSMSubspace = new File(System.getProperty("tatc.dsms"),
                        "Subspace" + Integer.toString(100000 + evalCounter).substring(1));
                currentDSMSubspace.mkdir();

                WalkerArchitecture arch;

                if (params.getI() == -1) {
                    double ssoIncl = this.getSSOInclination(params.getA());
                    arch = new WalkerArchitecture(params.getA(), ssoIncl, params.getT(), params.getP(), params.getF(), existingSatellites);
                } else {
                    arch = new WalkerArchitecture(params.getA(), params.getI(), params.getT(), params.getP(), params.getF(), existingSatellites);

                }

                //start date and end date/coverage
                MissionConcept newConcept = tsr.getMissionConcept().copy();

                try {
                    reductionAndMetrics(arch, newConcept);
                    costAndRisk(arch, newConcept);
                    evalCounter++;
                } catch (ReductionMetricsException rmEx) {
                    Logger.getLogger(StandardFormProblemFullFact.class.getName()).log(Level.SEVERE, null, rmEx);
                    throw new IllegalStateException("Evaluation of solution in R&M failed.", rmEx);
                } catch (CostRiskException crEx) {
                    Logger.getLogger(StandardFormProblemFullFact.class.getName()).log(Level.SEVERE, null, crEx);
                    throw new IllegalStateException("Evaluation of solution in C&R failed.", crEx);
                }
                //set cost and risk metrics
                ResultOutput crOutput = JSONIO.readJSON(cr.getOutputFile(), ResultOutput.class);

                Logger.getGlobal().fine(String.format(
                        "avg revisit: %.2f[min], lifecycle cost: %.2f[$],",
                        rm.getMetrics()[0] / 60.,
                        crOutput.getLifecycleCost().getEstimate()));

                bufferedWriter.append(Double.toString(params.getA()) + ",");
                bufferedWriter.append(Double.toString(FastMath.toDegrees(params.getI())) + ",");
                bufferedWriter.append(Double.toString(params.getT()) + ",");
                bufferedWriter.append(Double.toString(params.getP()) + ",");
                bufferedWriter.append(Double.toString(params.getF()) + ",");
                bufferedWriter.append(Double.toString(rm.getMetrics()[0] / 60.) + ",");
                bufferedWriter.append(Double.toString(crOutput.getLifecycleCost().getEstimate()));
                bufferedWriter.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
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

        double l = bounds.getLowerBound();
        double u = bounds.getUpperBound();

        ArrayList<Double> alts = new ArrayList<>();

        for (double count = l; count <= u; count = count + 100000) {
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

        double l = FastMath.toDegrees(bounds.getLowerBound());
        double u = FastMath.toDegrees(bounds.getUpperBound());

        ArrayList<Double> incl = new ArrayList<>();

        for (double count = l; count <= u; count = count = count + 10) {
            incl.add(FastMath.toRadians(count));
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
