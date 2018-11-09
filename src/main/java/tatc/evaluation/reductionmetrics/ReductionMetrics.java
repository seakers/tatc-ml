/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.evaluation.reductionmetrics;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.bodies.BodyShape;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.errors.OrekitException;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.TopocentricFrame;
import org.orekit.orbits.Orbit;
import org.orekit.orbits.PositionAngle;
import org.orekit.propagation.SpacecraftState;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;
import seakers.orekit.analysis.AbstractSpacecraftAnalysis;
import seakers.orekit.analysis.Analysis;
import seakers.orekit.analysis.CompoundSpacecraftAnalysis;
import seakers.orekit.analysis.Record;
import seakers.orekit.analysis.ephemeris.OrbitalElementsAnalysis;
import seakers.orekit.analysis.vectors.VectorAnalysis;
import seakers.orekit.coverage.access.TimeIntervalArray;
import seakers.orekit.coverage.analysis.AnalysisMetric;
import seakers.orekit.coverage.analysis.GroundEventAnalyzer;
import seakers.orekit.coverage.analysis.LatencyGroundEventAnalyzer;
import seakers.orekit.event.*;
import seakers.orekit.object.CommunicationBand;
import seakers.orekit.object.Constellation;
import seakers.orekit.object.CoverageDefinition;
import seakers.orekit.object.GndStation;
import seakers.orekit.object.Instrument;
import seakers.orekit.object.Satellite;
import seakers.orekit.object.communications.ReceiverAntenna;
import seakers.orekit.object.communications.Transmitter;
import seakers.orekit.object.communications.TransmitterAntenna;
import seakers.orekit.object.fieldofview.NadirRectangularFOV;
import seakers.orekit.propagation.PropagatorFactory;
import seakers.orekit.scenario.Scenario;
import seakers.orekit.util.OrekitConfig;
import seakers.orekit.util.Units;
import tatc.AbstractModule;
import tatc.architecture.specifications.DSMSpecification;
import tatc.architecture.specifications.GroundStationSpecification;
import tatc.architecture.specifications.InstrumentSpecification;
import tatc.architecture.specifications.MonoSpecification;
import tatc.tradespaceiterator.TradespaceSearchRequest;
import tatc.util.JSONIO;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;

/**
 * The reduction and metrics module
 * @author Prachi
 */

public class ReductionMetrics extends AbstractModule {
    /**
     * Properties for the R&M parameters
     */
    private final Properties properties;

    private double[] metrics;

    private TradespaceSearchRequest tsr;

    /**
     * eval counter for the number of Orbits
     */
    private int evalCounter;

      /**
     * eval counter for the number of Ground Station parameters
     */
    private int gndStnCounter;

      /**
     * eval counter for the number of Payload parameters
     */
    private int payloadCounter;

    private JsonArray monosJson;

    public ReductionMetrics(File inputFile, File outputFile, TradespaceSearchRequest tsr, Properties properties) {
        super(inputFile, outputFile);
        this.properties = properties;
        this.tsr = tsr;
        OrekitConfig.init(Integer.valueOf(System.getProperty("tatc.numThreads", "1")));
    }
    
    private void updateJSON(){
        this.monosJson.add("Orb" + Integer.toString(10000 + this.getEvalCounter()).substring(1) + "_" +
                "Pay" + Integer.toString(10000 + this.getPayloadCounter()).substring(1) + "_" +
                        "GS" + Integer.toString(10000 + this.getGndStnCounter()).substring(1));
    }
    
    public JsonArray getJSON(){
        return this.monosJson;
    }

    @Override
    public ReductionMetrics call() throws Exception {
        //Initializing parameters for the propagation of the satellites
        TimeScale timeScale = TimeScalesFactory.getUTC();
        Frame inertialFrame = FramesFactory.getEME2000();
        Frame earthFrame = FramesFactory.getITRF(IERSConventions.IERS_2003, true);
        BodyShape earthShape = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
                Constants.WGS84_EARTH_FLATTENING, earthFrame);
        PropagatorFactory propatagorFactory = this.tsr.getSatelliteOrbits().getPropagatorFactory();
        double analysisTimeStep = Double.parseDouble(this.tsr.getFullOutputs().getTimeStep());

        DSMSpecification dsmSpec = JSONIO.readJSON(getInputFile(), DSMSpecification.class);
        AbsoluteDate startDate = dsmSpec.getMissionConcept().getPerformancePeriod()[0];
        AbsoluteDate endDate = dsmSpec.getMissionConcept().getPerformancePeriod()[1];

        this.monosJson = new JsonArray();
 
        //coverage region
        HashSet<CoverageDefinition> cdefs = new HashSet();
        CoverageDefinition cdef = new CoverageDefinition("Coverage Definition",
                dsmSpec.getMissionConcept().getPOI(earthShape));

        //include all satellites in the simulation. 
        //Any pre-propagated satellites in the coverage database will be uploaded and not re-run
        HashSet<Satellite> satellites = new HashSet<>();

        HashMap<Satellite, Set<GndStation>> stationAssignment = new HashMap<>();

        HashMap<Satellite, MonoSpecification> monoSpecs = new HashMap<>();

        int i = 0;
        for (MonoSpecification mono : dsmSpec.getManifestOfMonoSpecifications()) {

            //load ground stations
            Set<GndStation> groundStations = loadGroundStations(
                    mono.getMissionConcept().getGroundStationSpecifications(),earthShape);

            Orbit orbit = new KeplerianElements(startDate, mono.getSatelliteOrbit());

            //load communication bands
            String[] bandStr = mono.getObservatorySpecification().getCommBandTypes().split("\\s");
            HashSet<CommunicationBand> bands = new HashSet<>();
            for (String str : bandStr) {
                bands.add(CommunicationBand.get(str));
            }
            Transmitter transmitter = new TransmitterAntenna(1.0, bands);
            ReceiverAntenna receiver = new ReceiverAntenna(1.0, bands);

            //load in instruments
            InstrumentSpecification instSpec = mono.getInstrumentSpecification();
            NadirRectangularFOV fov = new NadirRectangularFOV(
                    Units.deg2rad(instSpec.getCrossFieldOfView()),
                    Units.deg2rad(instSpec.getAlongFieldOfView()),
                    0.0, earthShape);
            ArrayList<Instrument> payload = new ArrayList<>();
            payload.add(new Instrument("inst_" + i, fov, instSpec.getMass(), instSpec.getPower()));

            //load in observatory mass
            double mass = mono.getObservatorySpecification().getStartMass();

            //create satellite
            //TO DO: this mass has to be passed to the propagator. the mass in the satellite object is not used.
            Satellite sat = new Satellite(String.valueOf(i), orbit, null, payload,
                    receiver, transmitter, mass, 0);

            //assume that all satellites are assigned the same ground stations
            stationAssignment.put(sat, groundStations);
            satellites.add(sat);

            monoSpecs.put(sat, mono);

            i++;
        }
        
        Constellation constel = new Constellation("Constellation 0", satellites);
        cdef.assignConstellation(constel);
        cdefs.add(cdef);

        ArrayList<EventAnalysis> eventAnalyses = new ArrayList<>();
        EventAnalysisFactory eaf = new EventAnalysisFactory(startDate, endDate,
                inertialFrame, propatagorFactory);
        Properties props = new Properties();
        props.setProperty("fov.saveAccess", "true");
        FieldOfViewEventAnalysis fovAnalysis = (FieldOfViewEventAnalysis) eaf.createGroundPointAnalysis(EventAnalysisEnum.FOV, cdefs, props);
        eventAnalyses.add(fovAnalysis);
        GndStationEventAnalysis gndStationAnalysis = (GndStationEventAnalysis) eaf.createGroundStationAnalysis(EventAnalysisEnum.ACCESS, stationAssignment, properties);
        eventAnalyses.add(gndStationAnalysis);
//        FieldOfViewAndGndStationEventAnalysis fovAndGndStationAnalysis=new FieldOfViewAndGndStationEventAnalysis(startDate, endDate,
//                inertialFrame, cdefs, stationAssignment,propatagorFactory, true, false);
//        eventAnalyses.add(fovAndGndStationAnalysis);
        
        ArrayList<Analysis<?>> analyses = new ArrayList<>();
        HashMap<Satellite,Analysis> anaToSat = new HashMap<>();
        for (final Satellite sat : constel.getSatellites()){
            Collection<AbstractSpacecraftAnalysis<?>> abstractAnalysis = new ArrayList<>();
            abstractAnalysis.add(new OrbitalElementsAnalysis(startDate, endDate, analysisTimeStep, sat, PositionAngle.MEAN, propatagorFactory));
            abstractAnalysis.add(new VectorAnalysis(startDate, endDate, analysisTimeStep, sat, propatagorFactory, inertialFrame) {
                @Override
                public Vector3D getVector(SpacecraftState currentState, Frame frame) throws OrekitException {
                    return currentState.getPVCoordinates(frame).getPosition();
                }
                @Override
                public String getName() {
                    return String.format("position_%s", sat.getName());
                }
            });
            analyses.add(new CompoundSpacecraftAnalysis(startDate, endDate, analysisTimeStep, sat, propatagorFactory, abstractAnalysis));
            anaToSat.put(sat, analyses.get(analyses.size() - 1));
        }
        
        Scenario scen = new Scenario("", startDate, endDate,
                timeScale, inertialFrame, propatagorFactory,
                cdefs, eventAnalyses, analyses, properties);

        try {
            scen.call();
        } catch (Exception ex) {
            throw new IllegalStateException("Evaluation failed.", ex);
        }
        
        Map<Satellite, Map<TopocentricFrame, TimeIntervalArray>> satAccess = new HashMap<>();
        
        for (Satellite sat : satellites){
            satAccess.put(sat, fovAnalysis.getSatelliteAccesses(cdef, sat));
        }


        //get TCavg, TCmin, TCmax
        //TCmin
        ArrayList<Double> firstRiseTimeValues = new ArrayList<>();
        //TCmax
        ArrayList<Double> lastRiseTimeValues = new ArrayList<>();
        // For every orbital and vector analysis in analysis array

        for (Satellite sat : satellites){
            setGndStnCounter(0);
            setPayloadCounter(0);
            Analysis analysis = anaToSat.get(sat);
            Collection<Record> orbitAnalysis = new ArrayList<>();
            Collection<Record> vectorAnalysis = new ArrayList<>();

            for (Analysis anal : ((CompoundSpacecraftAnalysis) analysis).getAnalyses()) {
                if (anal instanceof OrbitalElementsAnalysis) {
                    orbitAnalysis.addAll(anal.getHistory());

                } else {
                    vectorAnalysis.addAll(anal.getHistory());
                }
            }

            Map<TopocentricFrame, TimeIntervalArray> satAccesses = satAccess.get(sat);

            CoarsePropObservatories coarse = new CoarsePropObservatories(orbitAnalysis, vectorAnalysis);
            File file = new File(System.getProperty("tatc.monos"), "Orb" + Integer.toString(100000 + getEvalCounter()).substring(1));
            file.mkdir();
            coarse.save(file.getAbsoluteFile(), "obs");

            //output the observatory specs in json file
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject jsonObservatory = new JsonObject();

            jsonObservatory.addProperty("commBandTypes", monoSpecs.get(sat).getObservatorySpecification().getCommBandTypes());
            jsonObservatory.addProperty("dragCoeff", "");
            jsonObservatory.addProperty("startDate", monoSpecs.get(sat).getMissionConcept().getPerformancePeriod()[0].toString());
            jsonObservatory.addProperty("endDate", monoSpecs.get(sat).getMissionConcept().getPerformancePeriod()[1].toString());
            jsonObservatory.addProperty("startEcc", monoSpecs.get(sat).getSatelliteOrbit().getStartEcc());
            jsonObservatory.addProperty("startIncl", Math.toDegrees(monoSpecs.get(sat).getSatelliteOrbit().getStartIncl()));
            jsonObservatory.addProperty("startPer", monoSpecs.get(sat).getSatelliteOrbit().getStartPer());
            jsonObservatory.addProperty("startRAAN", monoSpecs.get(sat).getSatelliteOrbit().getStartRAAN());
            jsonObservatory.addProperty("startSMA", monoSpecs.get(sat).getSatelliteOrbit().getStartSMA());
            jsonObservatory.addProperty("startTrueA", monoSpecs.get(sat).getSatelliteOrbit().getStartTrueA());
            jsonObservatory.addProperty("volume", "");

            String obsSpecsStr = gson.toJson(jsonObservatory);

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(file, "Observatory.json")))) {
                bw.append(obsSpecsStr);
                bw.flush();
            } catch (IOException ex) {
                Logger.getLogger(JSONIO.class.getName()).log(Level.SEVERE, null, ex);
            }
            //finish writing the observatory specs in json file

            GroundEventAnalyzer gndStn = new GroundEventAnalyzer(gndStationAnalysis.getEvents(sat));

            Set<GndStation> gndStation = stationAssignment.get(sat);

            //get all the points(keys) for the map
            Collection<TopocentricFrame> keys = new ArrayList<>();
            Map<TopocentricFrame, TimeIntervalArray> gndAccesses = new HashMap<>();
            for (GndStation gnd : gndStation) {
                TopocentricFrame point = gnd.getBaseFrame();
                keys.add(point);
                gndAccesses = gndStn.getEvents(keys);
            }
            
            this.updateJSON();
            for (GndStation gnd : gndStation) {
                File gndStationFile = new File(file, "GS" + Integer.toString(10000 + getGndStnCounter()).substring(1));
                gndStationFile.mkdir();
                TopocentricFrame gndPoint=gnd.getBaseFrame();

                GndStationAccessMetrics gndStnMetric = new GndStationAccessMetrics(gndAccesses.get(gndPoint));

                double lat = Math.toDegrees(gndPoint.getPoint().getLatitude());
                double lon = Math.toDegrees(gndPoint.getPoint().getLongitude());

                char northOrSouth;
                char eastOrWest;

                if (lat >= 0) {
                    northOrSouth = 'N';
                } else {
                    northOrSouth = 'S';
                }

                if (lon >= 0) {
                    eastOrWest = 'E';
                } else {
                    eastOrWest = 'W';
                }

                //get upto 3 significant figures after decimal point
                DecimalFormat df = new DecimalFormat("#.###");
                String x = df.format(lat);
                String y = df.format(lon);

                //get absolute values of lat and lon
                lat = Math.abs(Double.valueOf(x));
                lon = Math.abs(Double.valueOf(y));

                gndStnMetric.save(gndStationFile, "Stn" + Integer.toString(10000 + getGndStnCounter()).substring(1) + "_"
                        + "lat" + northOrSouth + lat + "_"
                        + "lon" + eastOrWest + lon);

                //write ground station specs to json file 
                JsonObject station = new JsonObject();
                
                String commBand = "";
                for (CommunicationBand band : gnd.getReceiver().getBands()){
                    commBand = commBand + band.name();
                }
                station.addProperty("commBandTypes", commBand);
                station.addProperty("designation", "0.0");
                station.addProperty("lat", Math.toDegrees(gndPoint.getPoint().getLatitude()));
                station.addProperty("lon", Math.toDegrees(gndPoint.getPoint().getLongitude()));

                JsonObject jsonGroundNetwork = new JsonObject();
                jsonGroundNetwork.addProperty("designation", "0.0");
                jsonGroundNetwork.add("station", station);

                String gndStnSpecsStr = gson.toJson(jsonGroundNetwork);

                try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(gndStationFile, "GroundNetwork.json")))) {
                    bw.append(gndStnSpecsStr);
                    bw.flush();
                } catch (IOException ex) {
                    Logger.getLogger(JSONIO.class.getName()).log(Level.SEVERE, null, ex);
                }
                //finish writing the ground station specs in json file

                setGndStnCounter(getGndStnCounter()+1);
                this.updateJSON();
            }

            setPayloadCounter(0);
            File accessesFile = new File(file, "Pay00" + Integer.toString(1000 + getPayloadCounter()).substring(1));
            accessesFile.mkdir();

            //save point of interests
            int poiCount = 0;
            
            for (TopocentricFrame point : satAccesses.keySet()) {

                TimeIntervalArray time = satAccesses.get(point);
                
                if (time.getRiseSetTimes().isEmpty()) {
                    firstRiseTimeValues.add(null);
                    lastRiseTimeValues.add(null);
                }
                else {
                    firstRiseTimeValues.add(time.getRiseSetTimes().get(0).getTime());
                    lastRiseTimeValues.add(time.getRiseSetTimes().get(time.getRiseSetTimes().size()-2).getTime());
                }

                POIAccessMetrics poi = new POIAccessMetrics(time);

                double lat = Math.toDegrees(point.getPoint().getLatitude());
                double lon = Math.toDegrees(point.getPoint().getLongitude());

                char northOrSouth;
                char eastOrWest;

                if (lat >= 0) {
                    northOrSouth = 'N';
                } else {
                    northOrSouth = 'S';
                }

                if (lon >= 0) {
                    eastOrWest = 'E';
                } else {
                    eastOrWest = 'W';
                }

                //get upto 3 significant figures after decimal point
                DecimalFormat df = new DecimalFormat("#.###");
                String x = df.format(lat);
                String y = df.format(lon);

                //get absolute values of lat and lon
                lat = Math.abs(Double.valueOf(x));
                lon = Math.abs(Double.valueOf(y));

                poi.save(accessesFile.getAbsoluteFile(), "POI" + Integer.toString(10000 + poiCount).substring(1) + "_"
                        + "lat" + northOrSouth + lat + "_"
                        + "lon" + eastOrWest + lon);

                InstrumentSpecification inst = monoSpecs.get(sat).getInstrumentSpecification();
                
                //write instrument specs to json file 
                JsonObject angles = new JsonObject();
                angles.addProperty("clock", inst.getCrossFieldOfView());
                angles.addProperty("cone", inst.getAlongFieldOfView());

                JsonObject instrument = new JsonObject();
                instrument.add("fieldOfView", angles);
                
                String instrumentSpecsStr = gson.toJson(instrument);

                try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(accessesFile, "Instrument.json")))) {
                    bw.append(instrumentSpecsStr);
                    bw.flush();
                } catch (IOException ex) {
                    Logger.getLogger(JSONIO.class.getName()).log(Level.SEVERE, null, ex);
                }
                //finish writing instruments specs in json file

                poiCount++;
            }
            setEvalCounter(getEvalCounter()+1);
        }

        //save all outputs
        GroundEventAnalyzer fovGea = new GroundEventAnalyzer(fovAnalysis.getEvents(cdef));
        GroundEventAnalyzer gndGea = new GroundEventAnalyzer(gndStationAnalysis.getEvents());
        LatencyGroundEventAnalyzer latGea = new LatencyGroundEventAnalyzer(fovAnalysis.getAllAccesses().get(cdef),
                gndStationAnalysis.getAllAccesses(),false);
        LocalMetricsImaging lmi = new LocalMetricsImaging(fovGea);

        //compute metrics
        metrics = computeMetrics(fovGea, gndGea);
        
        //get TCmin and TCmax
        double TCmin, TCmax, TCavg;
        if (lastRiseTimeValues.contains(null) || lastRiseTimeValues.isEmpty() || 
                firstRiseTimeValues.contains(null) ||firstRiseTimeValues.isEmpty()){
            TCmin = 0;
            TCmax = 0;
            TCavg = 0;
        }
        else {
            TCmin = Collections.max(firstRiseTimeValues);
            TCmax = Collections.max(lastRiseTimeValues);
            TCavg = Collections.max(lastRiseTimeValues)/lastRiseTimeValues.size();
        }

        lmi.save(getOutputFile(), "lcl");
        GlobalMetrics gm = new GlobalMetrics(fovGea, gndGea, latGea, TCmin, TCmax, TCavg);

        gm.save(getOutputFile(), "gbl");

        return this;
    }

    /**
     * Compute some custom metric
     *
     * @return
     */
    private double[] computeMetrics(GroundEventAnalyzer fov, GroundEventAnalyzer stGea) {
        double[] out = new double[2];
        Properties prop = new Properties();
        //average revisit time
        out[0] = fov.getStatistics(AnalysisMetric.DURATION, false, prop).getMean();
        //mean response time
        out[1] = fov.getStatistics(AnalysisMetric.MEAN_TIME_TO_T, false, prop).getMean();

        //total ground station access time but add up the individual satellite links
        //out[1] = stGea.getStatistics(AnalysisMetric.DURATION, false,prop).getMean();

        if (Double.isNaN(out[1])) {
            out[1] = Double.POSITIVE_INFINITY;
        }
        return out;
    }

    /**
     * Gets the metrics
     *
     * @return
     */
    public double[] getMetrics() {
        return metrics;
    }

    public int getEvalCounter(){
        return this.evalCounter;
    }

    public int getGndStnCounter(){
        return this.gndStnCounter;
    }

    public int getPayloadCounter(){
        return this.payloadCounter;
    }

    private void setEvalCounter(int n){
        this.evalCounter=n;
    }

    private void setGndStnCounter(int n){
        this.gndStnCounter=n;
    }

    private void setPayloadCounter(int n){
        this.payloadCounter=n;
    }

    /**
     * Loads all the grounds stations from their specifications as defined in
     * the mission concept
     *
     * @param specs The specification of all the grounds stations to include in
     * this mission analysis
     * @return The grounds stations to include in this mission analysis
     */
    private Set<GndStation> loadGroundStations(Set<GroundStationSpecification> specs, BodyShape body) {
        HashSet<GndStation> out = new HashSet<>(specs.size());
        int groundStationCount = 0;
        for (GroundStationSpecification spec : specs) {
            GeodeticPoint pt = new GeodeticPoint(Math.toRadians(spec.getLatitude()), Math.toRadians(spec.getLongitude()), spec.getAltitude());
            HashSet<CommunicationBand> bands = new HashSet<>();
            for (String str : spec.getCommBandType()) {
                bands.add(CommunicationBand.get(str));
            }
            TopocentricFrame tpt = new TopocentricFrame(body, pt, "ground station " + groundStationCount);
            ReceiverAntenna receiver = new ReceiverAntenna(1., bands);
            TransmitterAntenna transmitter = new TransmitterAntenna(1., bands);

            try {
                GndStation gndStn = new GndStation(tpt, receiver, transmitter, Units.deg2rad(20));
                out.add(gndStn);

            } catch (OrekitException ex) {
                Logger.getLogger(ReductionMetrics.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
        return out;
    }

    /**
     * Shuts down the threads used to simulate the orbits
     */
    public void shutdown() {
        OrekitConfig.end();
    }

}
