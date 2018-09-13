package tatc.evaluation.costandrisk;

import eoss.problem.Instrument;
import eoss.problem.Orbit;
import eoss.problem.Orbits;
import eoss.problem.evaluation.CostModel;
import eoss.spacecraft.SpacecraftDesigner;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import org.orekit.utils.Constants;
import tatc.AbstractModule;
import tatc.util.JSONIO;

public class CostRiskSeak extends AbstractModule {

    /**
     * The root directory of cost and risk module
     */

    public CostRiskSeak(File inputFile, File outputFile) {
        super(inputFile, outputFile);
    }

    @Override
    public AbstractModule call() throws Exception {
        MasterInput wrapper = JSONIO.readJSON(getInputFile(), MasterInput.class);
        double totalLifecycleCost = 0;
        double launchCost = 100;

        //TODO: fix these numbers
        double fov = 50; //fov in degrees
        int name = 0;
        double groundCost = 0;
        double hardwareCost = 0;
        double iatCost = 0;
        double lifecycleCost = 0;
        double nonRecurringCost = 0;
        double operationsCost = 0;
        double programCost = 0;
        double recurringCost = 0;
        double standardError = 0;
        int fiscalYear = 2010;

        SpacecraftDesigner designer = new SpacecraftDesigner();
        for (Spacecraft s : wrapper.getConstellation().getSpacecraft()) {
            Payload[] payload = s.getPayload();
            ArrayList<Instrument> instrumentArrayList = new ArrayList<>();

            double totalDataRate = 0;
            for (Payload p : payload) {
                HashMap<String, String> prop = new HashMap();

                prop.put("developed-by", "DOM");
                prop.put("mass#", String.valueOf(p.getTotalMass()));
                prop.put("characteristic-power#", String.valueOf(p.getPeakPower()));
                prop.put("average-data-rate#", String.valueOf(p.getDataRate()));
                totalDataRate += p.getDataRate();
                prop.put("Technology-Readiness-Level", String.valueOf(p.getTechReadinessLevel()));

                Instrument inst = new Instrument(p.getName(), fov, prop);
                instrumentArrayList.add(inst);
            }
            eoss.spacecraft.Spacecraft sc = new eoss.spacecraft.Spacecraft(String.valueOf(name), instrumentArrayList);
            Orbit orb = new Orbit(String.valueOf(name), Orbit.OrbitType.LEO,
                    s.getAlt() * 1000 + Constants.WGS84_EARTH_EQUATORIAL_RADIUS, String.valueOf(s.getIncl()),
                    String.valueOf(s.getRAAN()), 0.0, 0.0, 0.0);

            double perOrbit = (totalDataRate * 1.2 * Orbits.period(orb)) / (1024 * 8); //(GByte/orbit) 20% overhead
            sc.setProperty("sat-data-rate-per-orbit#", String.valueOf(perOrbit));
            designer.designSpacecraft(sc, orb, wrapper.getConstellation().getDesignLife());
            name++;

            totalLifecycleCost += CostModel.lifeCycleCost(sc, wrapper.getConstellation().getDesignLife(), launchCost, 1);

            groundCost = Double.parseDouble(sc.getProperty("cost:programmatic"));
            hardwareCost = Double.parseDouble(sc.getProperty("cost:payload"));
            iatCost = Double.parseDouble(sc.getProperty("cost:IAT"));
            operationsCost = Double.parseDouble(sc.getProperty("cost:operations"));
            programCost = Double.parseDouble(sc.getProperty("cost:programmatic"));
            recurringCost = Double.parseDouble(sc.getProperty("cost:spacecraft-recurring"));
            nonRecurringCost = Double.parseDouble(sc.getProperty("cost:spacecraft-non-recurring"));
        }
        
        SubCost groundC = new SubCost(groundCost, fiscalYear, standardError);
        SubCost hardwareC = new SubCost(hardwareCost, fiscalYear, standardError);
        SubCost iatC = new SubCost(iatCost, fiscalYear, standardError);
        SubCost launchC = new SubCost(launchCost, fiscalYear, standardError);
        SubCost lifecycleC = new SubCost(totalLifecycleCost, fiscalYear, standardError);
        SubCost operationsC = new SubCost(operationsCost, fiscalYear, standardError);
        SubCost programC = new SubCost(programCost, fiscalYear, standardError);
        SubCost recurringC = new SubCost(recurringCost, fiscalYear, standardError);
        SubCost nonrecurringC = new SubCost(nonRecurringCost, fiscalYear, standardError);
        CostRiskJSON cr = new CostRiskJSON(groundC, hardwareC, iatC, launchC, lifecycleC, operationsC, programC, recurringC, nonrecurringC);

        JSONIO.writeJSON(getOutputFile(), cr);
        return this;
    }
    
}
