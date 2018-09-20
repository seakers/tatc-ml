/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.architecture;

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
import org.orekit.bodies.BodyShape;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.errors.OrekitException;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;
import seakers.orekit.object.CoverageDefinition;
import seakers.orekit.object.CoveragePoint;
import tatc.architecture.specifications.GroundStationSpecification;

/**
 *
 * @author nhitomi
 */
public class MissionConcept {

    /**
     * POSIX Standard system time integer, second resolution
     */
    private final int StartEpoch;
    
    /**
     * Period to compute the performance metrics
     */
    private final String PerformancePeriod;

    /**
     * The duration of the mission
     */
    private final String MissionDuration;

    /**
     * String specifying one of: Filepath to EarthPointList or latMin:latMax
     * where the full range of longitudes is implied or latMin:latMax
     * lonMin:lonMax, where latitudes and longitudes are as expected for
     * EarthPointList files and any xMin<=xMax
     */
    private final String AreaOfInterest;

    /**
     * String, Null(empty list) or MND tokens are Sun, Moon, or filepath to
     * ExistingSatelliteList
     */
    private final String ObjectsOfInterest;

    /**
     * String MND Tokens are NEN{all,com,gov}, DSN, TDRSS or filepath to
     * EarthPointList
     */
    private String GroundStationOptions;

    /**
     * String, MND Tokens are Primary, Secondary, or filepath to
     * LaunchVehicleList
     */
    private final String LaunchPreferences;

    /**
     * String specifying one of Government, Military, Commercial, or Academic
     */
    private final String MissionDirector;

    private final int Propulsion;

    public MissionConcept(int StartEpoch, String PerformancePeriod,
            String MissionDuration, String AreaOfInterest,
            String ObjectsOfInterest, String GroundStationOptions,
            String LaunchPreferences, String MissionDirector, int Propulsion) {
        this.StartEpoch = StartEpoch;
        this.PerformancePeriod = PerformancePeriod;
        this.MissionDuration = MissionDuration;
        this.AreaOfInterest = AreaOfInterest;
        this.ObjectsOfInterest = ObjectsOfInterest;
        this.GroundStationOptions = GroundStationOptions;
        this.LaunchPreferences = LaunchPreferences;
        this.MissionDirector = MissionDirector;
        this.Propulsion = Propulsion;
    }

    public AbsoluteDate getStartEpoch() throws OrekitException {
        AbsoluteDate zero = new AbsoluteDate(0, 1, 1, 0, 0, 0.0, TimeScalesFactory.getUTC());
        return zero.shiftedBy(StartEpoch);
    }

    /**
     * Gets the period when the performance of the constellation should be
     * evaluated
     *
     * @return an 2-element array of AbsoluteDates. First element is the start
     * date and the second element is the end date
     */
    public AbsoluteDate[] getPerformancePeriod() throws OrekitException {
        String[] period = PerformancePeriod.split(":");
        AbsoluteDate epoch = getStartEpoch();
        AbsoluteDate start = epoch.shiftedBy(Integer.parseInt(period[0]));
        AbsoluteDate end = epoch.shiftedBy(Integer.parseInt(period[1]));
        return new AbsoluteDate[]{start, end};
    }

    /**
     * Gets the duration of the mission in positive integer seconds
     *
     * @return the duration of the mission in positive integer seconds
     */
    public int getMissionDuration() {
        return Integer.parseInt(MissionDuration);
    }

    /**
     * Converts the area of interest into a collection of points of interest
     *
     * @param earthShape the shape of the earth to project the geodetic (lat,
     * lon, alt) points onto
     * @return a collection of points of interest
     * @throws org.orekit.errors.OrekitException
     */
    public Set<CoveragePoint> getPOI(BodyShape earthShape) throws OrekitException {
        //check if AreaOfInterest is a file
        File file = new File(AreaOfInterest);
        if (file.isFile()) {
            ArrayList<GeodeticPoint> pts = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line = br.readLine();
                while (line != null) {
                    String[] args = line.split("\\s");
                    pts.add(new GeodeticPoint(
                            Integer.parseInt(args[0]),
                            Integer.parseInt(args[1]),
                            Integer.parseInt(args[2])));
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(MissionConcept.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(MissionConcept.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (pts.isEmpty()) {
                throw new IllegalStateException("No points in the EarthPointList file.");
            }
            HashSet ptSet = new HashSet(pts);
            if (pts.size() != ptSet.size()) {
                throw new IllegalArgumentException("There is a non-unique point in the EarthPointList file");
            }
            return new CoverageDefinition("", pts, earthShape).getPoints();
        } else {
            String args[] = AreaOfInterest.split("\\s");
            double[] lats = new double[2];
            String[] latstr = args[0].split(":");
            lats[0] = Double.parseDouble(latstr[0]);
            lats[1] = Double.parseDouble(latstr[1]);
            double[] lons = new double[2];
            switch (args.length) {
                case 1:
                    //when only latitue ranges are given, assumes that points should be given around in longitude
                    lons[0] = -180.0;
                    lons[1] = 180.0;
                    break;
                case 2:
                    String[] lonstr = args[1].split(":");
                    lons[0] = Double.parseDouble(lonstr[0]);
                    lons[1] = Double.parseDouble(lonstr[1]);
                    break;
                default:
                    throw new UnsupportedOperationException("Expected a filepath to the EarthPointList or a range of latitude and/or longitudes");
            }
            return new CoverageDefinition("", 30, lats[0], lats[1], lons[0], lons[1],
                    earthShape, CoverageDefinition.GridStyle.UNIFORM).getPoints();
        }
    }

    public String getObjectsOfInterest() {
        return ObjectsOfInterest;
    }

    /**
     * Sets the file location with information regarding the ground station
     * specifications
     *
     * @param file the file containing information regarding the ground station
     * specifications
     */
    public void setGroundStationFile(File file) {
        this.GroundStationOptions = file.getAbsolutePath();
    }

    /**
     * Gets the ground station specifications involved with this mission. Tokens
     * allowed for ground station files are NENall, NENcom, NENgov, DSN, or a
     * file path relative to the TAT-C root directory
     *
     * @return the ground stations involved with this mission
     */
    public Set<GroundStationSpecification> getGroundStationSpecifications() {
        String[] args = GroundStationOptions.split("\\s");
        HashSet<GroundStationSpecification> out = new HashSet<>();

        for (int i = 0; i < args.length; i++) {
            //check if groundstationoption is a file
            File file = new File(args[i]);
            if (file.isFile()) {
                out.addAll(loadGroundStations(file));
            } else {
                File presetFile;
                switch (args[i]) {
                    case "NENall":
                        presetFile = new File(
                                System.getProperty("tatc.groundstation"), "NENall.txt");
                        break;
                    case "NENcom":
                        presetFile = new File(
                                System.getProperty("tatc.groundstation"), "NENcom.txt");
                        break;
                    case "NENgov":
                        presetFile = new File(
                                System.getProperty("tatc.groundstation"), "NENgov.txt");
                        break;
                    case "DSN":
                        presetFile = new File(
                                System.getProperty("tatc.groundstation"), "DSN.txt");
                        break;
                    default:
                        presetFile = new File(System.getProperty("tatc.root"), args[i]);
                        if (!presetFile.exists()) {
                            throw new IllegalArgumentException(
                                    String.format("No file found at %s", presetFile.getAbsoluteFile()));
                        }
                }
                out.addAll(loadGroundStations(presetFile));
            }
        }
        return out;
    }

    /**
     * Loads the ground station specifications from the given EarthPointsList
     * file.
     *
     * @param file EarthPointsList file containing the following parameters for
     * each ground station latitude [deg], longitude [deg], altitude [m]
     * @return
     */
    private HashSet<GroundStationSpecification> loadGroundStations(File file) {
        HashSet<GroundStationSpecification> gndSt = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            while (line != null) {
                gndSt.add(GroundStationSpecification.create(line));
                line = br.readLine();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MissionConcept.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MissionConcept.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (gndSt.isEmpty()) {
            throw new IllegalStateException("No ground stations found in the EarthPointList file.");
        }
        return gndSt;
    }

    public String getLaunchPreferences() {
        return LaunchPreferences;
    }

    public String getMissionDirector() {
        return MissionDirector;
    }

    public int getPropulsion() {
        return Propulsion;
    }

    /**
     * Creates a new instance of this mission concept
     *
     * @return a new instance of this mission concept
     */
    public MissionConcept copy() {
        return new MissionConcept(StartEpoch, PerformancePeriod,
                MissionDuration, AreaOfInterest, ObjectsOfInterest,
                GroundStationOptions, LaunchPreferences, MissionDirector,
                Propulsion);
    }

}
