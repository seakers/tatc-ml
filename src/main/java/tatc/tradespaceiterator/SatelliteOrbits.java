/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.tradespaceiterator;

import tatc.architecture.variable.MonolithVariable;
import tatc.architecture.SpecialOrbit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.orekit.utils.Constants;
import seakers.conMOP.util.Bounds;
import seak.orekit.propagation.PropagatorFactory;
import seak.orekit.propagation.PropagatorType;
import seak.orekit.util.Units;
import tatc.architecture.specifications.InstrumentSpecification;
import tatc.architecture.specifications.ObservatorySpecification;
import tatc.architecture.specifications.SatelliteOrbitSpecification;
import tatc.evaluation.reductionmetrics.AbsoluteDate;


public class SatelliteOrbits {

    /**
     * String, Null, or filepath to ExistingSatelliteList
     */
    private final String ExistingSatelliteOptions;

    /**
     * String w/ positive integers min:max (0<min<=max). if max is greater than
     * the number of specified Obs-types, then it must be the case that
     * multiples of som Obs-type can be used
     */
    private final String NumberOfNewSatellites;

    /**
     * String, MND Tokens are LEO, GEO, MEO, min:max (positive float[km],
     * min<max
     */
    private final String AltitudeRangesOfInterest;

    /**
     * String, MND Tokens are min:max(positive float[deg], min<max)
     */
    private final String InclinationRangesOfInterest;

    /**
     * String, Null (empty list) or MND Tokens are SSO, Frozen,
     * CriticallyInclined, ISS
     */
    private final String SpecialOrbits;

    /**
     * Integer[0-9](low-high)
     */
    private final int PropagationFidelity;

    public SatelliteOrbits(String ExistingSatelliteOptions,
            String NumberOfNewSatellites, String AltitudeRangesOfInterest,
            String InclinationRangesOfInterest, String SpecialOrbits, int PropagationFidelity) {
        this.ExistingSatelliteOptions = ExistingSatelliteOptions;
        this.NumberOfNewSatellites = NumberOfNewSatellites;
        this.AltitudeRangesOfInterest = AltitudeRangesOfInterest;
        this.InclinationRangesOfInterest = InclinationRangesOfInterest;
        this.SpecialOrbits = SpecialOrbits;
        this.PropagationFidelity = PropagationFidelity;
    }

    public String getExistingSatelliteOptions() {
        return ExistingSatelliteOptions;
    }

    /**
     * Gets the range of the number of new satellites to add to the architecture
     *
     * @return the range of the number of new satellites to add to the
     * architecture
     */
    public Bounds<Integer> getNumberOfNewSatellites() {
        String[] arg = NumberOfNewSatellites.split(":");
        return new Bounds(Integer.parseInt(arg[0]), Integer.parseInt(arg[1]));
    }

    /**
     * Gets the range of the semimajor axis [m] to search
     *
     * @return the range of the semimajor axis [m] to search
     */
    public Bounds<Double> getSemiMajorAxisRange() {
        double earthRadius = Constants.WGS84_EARTH_EQUATORIAL_RADIUS;
        String[] arg = AltitudeRangesOfInterest.split(":");
        if (arg.length == 2) {
            return new Bounds(earthRadius + Units.km2m(Double.parseDouble(arg[0])),
                    earthRadius + Units.km2m(Double.parseDouble(arg[1])));
        } else {
            switch (arg[0]) {
                //200km to 2000km altitude
                case "LEO":
                    return new Bounds(
                            Constants.WGS84_EARTH_EQUATORIAL_RADIUS + 200000,
                            Constants.WGS84_EARTH_EQUATORIAL_RADIUS + 2000000);
                //2000km to 3500 altitude
                case "MEO":
                    return new Bounds(
                            Constants.WGS84_EARTH_EQUATORIAL_RADIUS + 2000000,
                            Constants.WGS84_EARTH_EQUATORIAL_RADIUS + 3500000);
                case "GEO":
                    return new Bounds(
                            Constants.WGS84_EARTH_EQUATORIAL_RADIUS + 42164000,
                            Constants.WGS84_EARTH_EQUATORIAL_RADIUS + 42164000);
                default:
                    throw new UnsupportedOperationException(
                            String.format("Expected range or "
                                    + "token {LEO, MEO, GEO}. Found %s", arg[0]));
            }
        }
    }

    /**
     * Gets the range of the inclination [rad] to search
     *
     * @return the range of the inclination [rad] to search
     */
    public Bounds<Double> getInclinationRangesOfInterest() {
        String[] arg = InclinationRangesOfInterest.split(":");
        return new Bounds(Units.deg2rad(Double.parseDouble(arg[0])),
                Units.deg2rad(Double.parseDouble(arg[1])));
    }

    /**
     * Gets any special orbits to use
     *
     * @return
     */
    public ArrayList<SpecialOrbit> getSpecialOrbits() {
        String[] args = SpecialOrbits.split("\\s");
        ArrayList<SpecialOrbit> special = new ArrayList<>();
        if (args[0].equals("")) {
            return null;
        } else {
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "SSO":
                        special.add(SpecialOrbit.SSO);
                        break;
                    case "Frozen":
                        special.add(SpecialOrbit.FROZEN);
                        break;
                    case "CriticallyInclined":
                        special.add(SpecialOrbit.CRITICALLY_INCLINED);
                        break;
                    case "ISS":
                        special.add(SpecialOrbit.ISS);
                        break;
                    default:
                        throw new UnsupportedOperationException(
                                String.format("Expected token SSO, Frozen, "
                                        + "CriticallyInclined, or ISS. Found %s.", SpecialOrbits));
                }
            }
        }
        return special;
    }

    /**
     * Gets the propagator factory based on the specified fidelity
     *
     * @return the propagator factory based on the specified fidelity
     */
    public PropagatorFactory getPropagatorFactory() {
        switch (PropagationFidelity) {
            case 0:
                return new PropagatorFactory(PropagatorType.KEPLERIAN);
            case 1:
                return new PropagatorFactory(PropagatorType.J2);
            case 2:
                return new PropagatorFactory(PropagatorType.NUMERICAL);
            default:
                throw new UnsupportedOperationException("Propagation fidelity supported for:\n"
                        + "[0]:Keplerian\n"
                        + "[1]:J2");
        }
    }

    /**
     * Gets the existing satellite with their initial orbital parameters set at
     * the startDate. If no observatory or instrument specifications are given
     * then it is assumed that id = 0 for both observatory and instrument
     * specification.
     *
     * @param startDate the startDate when the satellites are positioned by
     * their orbital parameters
     * @return the existing satellite with their initial orbital parameters set
     * at the startDate
     */
    public Set<MonolithVariable> getExistingSatellites(AbsoluteDate startDate) {
        Set<MonolithVariable> out = new HashSet<>();
        if (!ExistingSatelliteOptions.equalsIgnoreCase("Null")) {
            File file = new File(System.getProperty("tatc.root"), ExistingSatelliteOptions);
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line = br.readLine();
                while (line != null) {
                    String[] args = line.split("\\|");
                    SatelliteOrbitSpecification orbSpec;
                    ObservatorySpecification obsSpec;
                    InstrumentSpecification instSpec;
                    SearchDatabase db = SearchDatabase.getInstance();
                    switch (args.length) {
                        case 1:
                            //when only orbital elements
                            orbSpec = SatelliteOrbitSpecification.create(args[0]);
                            obsSpec = db.getObservatorySpecification(0);
                            instSpec = db.getInstrumentSpecification(0);
                            break;
                        case 2:
                            //when orbital elements + observatory specificaitons
                            orbSpec = SatelliteOrbitSpecification.create(args[0]);
                            obsSpec = ObservatorySpecification.create(args[1]);
                            db.addObservatorySpecification(obsSpec);
                            instSpec = db.getInstrumentSpecification(0);
                            break;
                        case 3:
//                            //when orbital elements + observatory specifications + instrument specifications
                            orbSpec = SatelliteOrbitSpecification.create(args[0]);
                            obsSpec = ObservatorySpecification.create(args[1]);
                            db.addObservatorySpecification(obsSpec);
                            instSpec = InstrumentSpecification.create(args[2]);
                            db.addInstrumentSpecification(instSpec);
                            break;
                        default:
                            throw new UnsupportedOperationException(
                                    String.format("Expected up to 3 arguments separated by |"
                                            + "for orbital elements, observatory "
                                            + "specifications, and/or instrument "
                                            + "specifications. Found %d", args.length));
                    }

                    int obsID = db.getID(obsSpec);
                    int instID = db.getID(instSpec);
                    Bounds<Integer> obsIDBound = new Bounds<>(obsID, obsID);
                    Bounds<Integer> instIDBound = new Bounds<>(instID, instID);
                    Bounds<Double> smaBound = new Bounds<>(orbSpec.getStartSMA(), orbSpec.getStartSMA());
                    Bounds<Double> eccBound = new Bounds<>(orbSpec.getStartEcc(), orbSpec.getStartEcc());
                    Bounds<Double> incBound = new Bounds<>(orbSpec.getStartIncl(), orbSpec.getStartIncl());
                    Bounds<Double> argPerBound = new Bounds<>(orbSpec.getStartPer(), orbSpec.getStartPer());
                    Bounds<Double> raanBound = new Bounds<>(orbSpec.getStartRAAN(), orbSpec.getStartRAAN());
                    Bounds<Double> anomBound = new Bounds<>(orbSpec.getStartTrueA(), orbSpec.getStartTrueA());
                    MonolithVariable var = new MonolithVariable(obsIDBound, instIDBound, smaBound, eccBound, incBound, argPerBound, raanBound, anomBound);
                    var.setSma(orbSpec.getStartSMA());
                    var.setEcc(orbSpec.getStartEcc());
                    var.setInc(orbSpec.getStartIncl());
                    var.setArgPer(orbSpec.getStartPer());
                    var.setRaan(orbSpec.getStartRAAN());
                    var.setTrueAnomaly(orbSpec.getStartTrueA());
                    out.add(var);
                    line = br.readLine();
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(SatelliteOrbits.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(SatelliteOrbits.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return out;
    }

}
