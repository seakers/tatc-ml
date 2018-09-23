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
import seakers.orekit.event.EventAnalysis;
import seakers.orekit.event.EventAnalysisEnum;
import seakers.orekit.event.EventAnalysisFactory;
import seakers.orekit.event.FieldOfViewEventAnalysis;
import seakers.orekit.event.GndStationEventAnalysis;
import seakers.orekit.event.GroundEventAnalysis;
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
     * A factory to create instances of propagators
     */
    private final PropagatorFactory propatagorFactory;

    /**
     * The time scale is set to UTC by default
     */
    private final TimeScale timeScale;

    /**
     * The inertial frame of the simulation
     */
    private final Frame inertialFrame;

    /**
     * The earth shape
     */
    private final BodyShape earthShape;

    /**
     * Properties for the R&M parameters
     */
    private final Properties properties;

    private double[] metrics;

    /**
     * the time step for the analysis
     */
    private double analysisTimeStep;

    /**
     * eval counter for the number of Orbital parameters
     */
    int evalCounter;
    
      /**
     * eval counter for the number of Ground Station parameters
     */
    int gndStnCounter;
    
      /**
     * eval counter for the number of Payload parameters
     */
    int payloadCounter;
    
    private JsonArray monosJson;

    public ReductionMetrics(File inputFile, File outputFile, TradespaceSearchRequest tsr, Properties properties) {
        super(inputFile, outputFile);
        OrekitConfig.init(Integer.valueOf(System.getProperty("tatc.numThreads", "1")));
        this.properties = properties;
        try {
            this.timeScale = TimeScalesFactory.getUTC();
            this.inertialFrame = FramesFactory.getEME2000();
            Frame earthFrame = FramesFactory.getITRF(IERSConventions.IERS_2003, true);
            this.earthShape = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
                    Constants.WGS84_EARTH_FLATTENING, earthFrame);
            this.propatagorFactory = tsr.getSatelliteOrbits().getPropagatorFactory();
            this.analysisTimeStep = Double.parseDouble(tsr.getFullOutputs().getTimeStep());
        } catch (OrekitException ex) {
            Logger.getLogger(ReductionMetrics.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalStateException("Failed to initialize R&M", ex);
        }
    }
    
    private void updateJSON(){
        this.monosJson.add("Orb" + Integer.toString(10000 + this.evalCounter).substring(1) + "_" + 
                "Pay" + Integer.toString(10000 + this.payloadCounter).substring(1) + "_" + 
                        "GS" + Integer.toString(10000 + this.gndStnCounter).substring(1));
    }
    
    public JsonArray getJSON(){
        return this.monosJson;
    }

    @Override
    public AbstractModule call() throws Exception {
        DSMSpecification dsmSpec = JSONIO.readJSON(getInputFile(), DSMSpecification.class);
        AbsoluteDate startDate = dsmSpec.getMissionConcept().getPerformancePeriod()[0];
        AbsoluteDate endDate = dsmSpec.getMissionConcept().getPerformancePeriod()[1];
        
//        this.evalCounter = 0;
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
                    mono.getMissionConcept().getGroundStationSpecifications());

            Orbit orbit = new KeplerianElements(tatc.evaluation.reductionmetrics.AbsoluteDate.cast(startDate),
                    mono.getSatelliteOrbit());

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

        EventAnalysisFactory eaf = new EventAnalysisFactory(startDate, endDate,
                inertialFrame, propatagorFactory);
        ArrayList<EventAnalysis> eventAnalyses = new ArrayList<>();

        EventAnalysis fovAnalysis = eaf.createGroundPointAnalysis(EventAnalysisEnum.FOV, cdefs, properties);
        eventAnalyses.add(fovAnalysis);
        EventAnalysis gndStationAnalysis = eaf.createGroundStationAnalysis(EventAnalysisEnum.ACCESS, stationAssignment, properties);
        eventAnalyses.add(gndStationAnalysis);
        FieldOfViewEventAnalysis fovEventAnalysis = new FieldOfViewEventAnalysis(startDate, endDate, inertialFrame, cdefs, propatagorFactory, true, false);
        eventAnalyses.add(fovEventAnalysis);
        
        ArrayList<Analysis<?>> analyses = new ArrayList<>();
        HashMap<Analysis, Satellite> anaToSat = new HashMap<>();
        for (final Satellite sat : constel.getSatellites()){
            Collection<AbstractSpacecraftAnalysis<?>> abstractAnalysis = new ArrayList<>();
            abstractAnalysis.add(new OrbitalElementsAnalysis(startDate, endDate, analysisTimeStep, sat, PositionAngle.MEAN, this.propatagorFactory));
            abstractAnalysis.add(new VectorAnalysis(startDate, endDate, analysisTimeStep, sat, this.propatagorFactory, inertialFrame) {
                
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
            anaToSat.put(analyses.get(analyses.size() - 1), sat);
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
            satAccess.put(sat, new HashMap<>(fovEventAnalysis.getSatelliteAccesses(cdef, sat)));
        }
        
        //get TCavg, TCmin, TCmax
        //TCmin
        ArrayList<Double> firstRiseTimeValues = new ArrayList<>();
        //TCmax
        ArrayList<Double> lastRiseTimeValues = new ArrayList<>();
            

        // For every orbital and vector analysis in analysis array
        for (int j = 0; j < analyses.size(); j++) {
            this.gndStnCounter = 0;
            this.payloadCounter = 0;
            Analysis analysis = analyses.get(j);
            Satellite sat = anaToSat.get(analysis);
            Collection<Record> orbitAnalysis = new ArrayList<>();
            Collection<Record> vectorAnalysis = new ArrayList<>();

            for (Analysis anal : ((CompoundSpacecraftAnalysis) analysis).getAnalyses()) {
                if (anal instanceof OrbitalElementsAnalysis) {
                    orbitAnalysis.addAll(anal.getHistory());

                } else {
                    vectorAnalysis.addAll(anal.getHistory());
                }
            }

            Map<TopocentricFrame, TimeIntervalArray> access = satAccess.get(sat);

            CoarsePropObservatories coarse = new CoarsePropObservatories(orbitAnalysis, vectorAnalysis);
            File file = new File(System.getProperty("tatc.monos"), "Orb" + Integer.toString(100000 + evalCounter).substring(1));
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
            jsonObservatory.addProperty("startIncl", monoSpecs.get(sat).getSatelliteOrbit().getStartIncl());
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

            GroundEventAnalyzer gndStn = new GroundEventAnalyzer(((GndStationEventAnalysis) gndStationAnalysis).getEvents(sat));

            Set<GndStation> gndStation = stationAssignment.get(sat);

            //get all the points(keys) for the map
            Collection<TopocentricFrame> keys = new ArrayList<>();
            Map<TopocentricFrame, TimeIntervalArray> accesses = new HashMap<>();
            for (GndStation gnd : gndStation) {
                TopocentricFrame point = gnd.getBaseFrame();
                keys.add(point);
                accesses = gndStn.getEvents(keys);
            }
            
            this.updateJSON();
            this.gndStnCounter = 0;

            for (GndStation gnd : gndStation) {
                File gndStationFile = new File(file, "GS" + Integer.toString(10000 + this.gndStnCounter).substring(1));
                gndStationFile.mkdir();

                GndStationAccessMetrics gndStnMetric = new GndStationAccessMetrics(accesses.get(gnd.getBaseFrame()));

                double lat = Math.toDegrees(gnd.getBaseFrame().getPoint().getLatitude());
                double lon = Math.toDegrees(gnd.getBaseFrame().getPoint().getLongitude());

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

                gndStnMetric.save(gndStationFile, "Stn" + Integer.toString(10000 + gndStnCounter).substring(1) + "_"
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
                station.addProperty("lat", Math.toDegrees(gnd.getBaseFrame().getPoint().getLatitude()));
                station.addProperty("lon", Math.toDegrees(gnd.getBaseFrame().getPoint().getLongitude()));

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

                gndStnCounter++;
                this.updateJSON();
            }

            this.payloadCounter = 0;
            File accessesFile = new File(file, "Pay00" + Integer.toString(1000 + payloadCounter).substring(1));
            accessesFile.mkdir();

            //save point of interests
            int poiCount = 0;
            
            for (Map.Entry<TopocentricFrame, TimeIntervalArray> entry : access.entrySet()) {

                TopocentricFrame point = entry.getKey();
                TimeIntervalArray time = entry.getValue();
                
                firstRiseTimeValues.add(time.getRiseSetTimes().get(0).getTime());
                lastRiseTimeValues.add(time.getRiseSetTimes().get(time.getRiseSetTimes().size()-2).getTime());

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
                //finish writing istruments specs in json file

                poiCount++;
            }
            evalCounter++;
        }
   
        //compute metrics
        metrics = computeMetrics((FieldOfViewEventAnalysis) fovAnalysis, cdef,
                (GndStationEventAnalysis) gndStationAnalysis);

        //save all outputs
        GroundEventAnalyzer fovGea = new GroundEventAnalyzer(((GroundEventAnalysis) fovAnalysis).getEvents(cdef));
        GroundEventAnalyzer gndGea = new GroundEventAnalyzer(((GndStationEventAnalysis) gndStationAnalysis).getEvents());
        LocalMetricsImaging lmi = new LocalMetricsImaging(fovGea);
        
        //get TCmin and TCmax
        double TCmin = Collections.max(firstRiseTimeValues);
        double TCmax = Collections.max(lastRiseTimeValues);
        double TCavg = Collections.max(lastRiseTimeValues)/lastRiseTimeValues.size();

        lmi.save(getOutputFile(), "lcl");
        GlobalMetrics gm = new GlobalMetrics(fovGea, gndGea, TCmin, TCmax, TCavg);

        gm.save(getOutputFile(), "gbl");

        return this;
    }

    /**
     * Compute some custom metric
     *
     * @return
     */
    private double[] computeMetrics(FieldOfViewEventAnalysis fovAnalysis, CoverageDefinition cdef,
            GndStationEventAnalysis gstAnalysis) {
        double[] out = new double[2];
        GroundEventAnalyzer fovGea = new GroundEventAnalyzer(fovAnalysis.getEvents(cdef));
        Properties prop = new Properties();
        //average revisit time
        out[0] = fovGea.getStatistics(AnalysisMetric.DURATION, false, prop).getMean();
        out[1] = fovGea.getStatistics(AnalysisMetric.MEAN_TIME_TO_T, false, prop).getMean();
        
        //total ground station access time but add up the individual satellite links
        //GroundEventAnalyzer stGea = new GroundEventAnalyzer(gstAnalysis.getEvents());
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

    /**
     * Loads all the grounds stations from their specifications as defined in
     * the mission concept
     *
     * @param specs The specification of all the grounds stations to include in
     * this mission analysis
     * @return The grounds stations to include in this mission analysis
     */
    private Set<GndStation> loadGroundStations(Set<GroundStationSpecification> specs) {
        HashSet<GndStation> out = new HashSet<>(specs.size());
        int groundStationCount = 0;
        for (GroundStationSpecification spec : specs) {
            GeodeticPoint pt = new GeodeticCoordinates(
                    Math.toRadians(spec.getLatitude()), Math.toRadians(spec.getLongitude()), spec.getAltitude());
            HashSet<CommunicationBand> bands = new HashSet<>();
            for (String str : spec.getCommBandType()) {
                bands.add(CommunicationBand.get(str));
            }
            TopocentricFrame tpt = new TopocentricFrame(earthShape, pt, "ground station " + groundStationCount);
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
