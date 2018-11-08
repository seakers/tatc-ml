package tatc.tradespaceiterator;

import org.hipparchus.util.FastMath;
import org.orekit.errors.OrekitException;
import seakers.conmop.util.Bounds;
import tatc.architecture.specifications.*;
import tatc.architecture.variable.MonolithVariable;
import tatc.evaluation.costandrisk.CostRiskSeak;
import tatc.evaluation.reductionmetrics.AbsoluteDate;
import tatc.evaluation.reductionmetrics.ReductionMetrics;

import java.io.File;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

public class StandardFormProblemProperties {
    /**
     * The set of satellites that exist already and are not decision variables
     * in the search
     */
    public final Set<MonolithVariable> existingSatellites;

    /**
     * Counts the number of evaluations so we can use it to id architectures
     */
    public int evalCounter;

    /**
     * Path to the current subspace in the DSMs directory
     */
    public File currentDSMSubspace;

    /**
     * Path to the current subspace in the Monos directory
     */
    public File currentMonoSubspace;

    /**
     * The tradespace search request
     */
    public final TradespaceSearchRequest tsr;

    public final ReductionMetrics rm;

    public final CostRiskSeak cr;

    public final SearchDatabase db;

    public final ArrayList<Double> smas;

    public final ArrayList<Double> inclination;

    public final ArrayList<Integer> numberOfSats;

    public final ArrayList<SpecialOrbit> specialOrbits;

    public StandardFormProblemProperties(TradespaceSearchRequest tsr, Properties properties) {
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
            smas = discretizeSemiMajorAxes(tsr.getSatelliteOrbits().getSemiMajorAxisRange());
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
     * Discretizes a range of smas
     *
     * @return the discrete values for smas
     */
    private ArrayList<Double> discretizeSemiMajorAxes(Bounds<Double> bounds) {

        double l = bounds.getLowerBound();
        double u = bounds.getUpperBound();

        ArrayList<Double> smas = new ArrayList<>();

        for (double count = l; count <= u; count = count + 50000) {
            smas.add(count);
        }
        return smas;
    }

    /**
     * Discretizes a range of inclination
     *
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
}