package tatc.tradespaceiterator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.orekit.utils.Constants;
import seakers.orekit.util.Units;
import tatc.architecture.Architecture;
import tatc.architecture.specifications.*;
import tatc.architecture.variable.MonolithVariable;
import tatc.evaluation.costandrisk.*;
import tatc.exceptions.CostRiskException;
import tatc.exceptions.ReductionMetricsException;
import tatc.util.JSONIO;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ArchitectureEvaluator {

    StandardFormProblemProperties properties;

    public ArchitectureEvaluator(StandardFormProblemProperties properties){
        this.properties=properties;
    }
    /**
     * Create and process the reduction and metrics job request
     */
    public void reductionAndMetrics(Architecture arch, MissionConcept concept) throws ReductionMetricsException {
        //create a R&M job request in the form a JSON
        Set<MonolithVariable> monoliths = new HashSet<>();
        monoliths.addAll(properties.existingSatellites);
        monoliths.addAll(arch.getSatellites());
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
                    properties.db.getObservatorySpecification(var.getObservatoryID()),
                    properties.db.getInstrumentSpecification(var.getInstrumentID()));
            i++;
        }
        DSMSpecification dsmSpec = new DSMSpecification(concept, monoSpecs);

        //input to the r&m module - gives all information about the subspace
        JSONIO.writeJSON(properties.rm.getInputFile(), dsmSpec);
        properties.rm.setOutputFile(properties.currentDSMSubspace);

        try {
            properties.rm.call();

            //output the Manifest json file
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject monoSpec = new JsonObject();

            monoSpec.addProperty("ConstellationType", properties.evalCounter);
            monoSpec.add("Mono", properties.rm.getJSON());

            String monoStr = gson.toJson(monoSpec);
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(properties.currentDSMSubspace, "Manifest.json")))) {
                bw.append(monoStr);
                bw.flush();
            } catch (IOException ex) {
                Logger.getLogger(JSONIO.class.getName()).log(Level.SEVERE, null, ex);
            }
            //finish writing the observatory specs in json file

        } catch (ReductionMetricsException rmEx) {
            throw rmEx;
        } catch (Exception ex) {
            Logger.getLogger(StandardFormProblemGAWalker.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalStateException("Evaluation of solution in R&M failed.", ex);
        }
    }

    /**
     * Create and process the cost and risk job request
     */
    public void costAndRisk(Architecture arch, MissionConcept concept) throws CostRiskException {
        Set<MonolithVariable> monoliths = new HashSet<>();
        monoliths.addAll(properties.existingSatellites);
        monoliths.addAll(arch.getSatellites());
        Spacecraft[] spacecraft = new Spacecraft[monoliths.size()];
        int count = 0;
        for (MonolithVariable var : monoliths) {
            ObservatorySpecification observSpec = properties.db.getObservatorySpecification(var.getObservatoryID());
            InstrumentSpecification instSpec = properties.db.getInstrumentSpecification(var.getInstrumentID());
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
        LaunchVehicleSpecification lvSpec = properties.db.getLaunchVehicleSpecification(0);

        Launch launch = new Launch("Cape Canaveral", 2017, lvSpec.getName(), monoliths.size(), 1, lvSpec.getReliability());

        Set<GroundStationSpecification> gndStations = concept.getGroundStationSpecifications();
        GroundStationSpecification[] groundStations = new GroundStationSpecification[gndStations.size()];
        gndStations.toArray(groundStations);

        Context crContext = new Context(concept.getMissionDirector(), 2016, launch, groundStations);

        MasterInput crInput = new MasterInput(constellation, crContext);

        properties.cr.setInputFile(new File(properties.currentDSMSubspace, "CostRisk.json"));
        properties.cr.setOutputFile(new File(properties.currentDSMSubspace, "CostRisk_Output.json"));

        JSONIO.writeJSON(properties.cr.getInputFile(), crInput);
        try {
            properties.cr.call();
        } catch (CostRiskException crEx) {
            throw crEx;
        } catch (Exception ex) {
            Logger.getLogger(StandardFormProblemGAWalker.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalStateException("Evaluation of solution in CaR failed.", ex);
        }
    }
}
