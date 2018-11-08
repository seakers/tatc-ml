package tatc.tradespaceiterator;

import org.apache.commons.lang3.ArrayUtils;
import org.hipparchus.util.FastMath;
import org.orekit.utils.Constants;
import seakers.orekit.constellations.EnumerateWalkerConstellations;
import seakers.orekit.constellations.WalkerParameters;
import seakers.orekit.util.Orbits;
import tatc.architecture.TATCWalker;
import tatc.architecture.specifications.MissionConcept;
import tatc.evaluation.costandrisk.ResultOutput;
import tatc.exceptions.CostRiskException;
import tatc.exceptions.ReductionMetricsException;
import tatc.util.JSONIO;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StandardFormProblemFullFactorial implements StandardFormProblemImplementation {
    StandardFormProblemProperties properties;
    ArchitectureEvaluator archEval;

    public StandardFormProblemFullFactorial(StandardFormProblemProperties properties){
        this.properties=properties;
        this.archEval=new ArchitectureEvaluator(properties);
    }

    public void start(){
        evaluate();
    }

    public void evaluate(){
        //output the variables and metrics in a csv file
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(System.getProperty("tatc.access_results"), "results.csv")))) {

            bufferedWriter.append("Altitude[m],Inclination[deg],Satellites,Planes,Phase,Avg Revisit Time[min],Avg Response Time[min],Cost[FY10$M]");
            bufferedWriter.newLine();

            //convert arraylists to array in order to pass into fullFactWalker
            Double[] altArray = new Double[properties.altitudes.size()];
            altArray = properties.altitudes.toArray(altArray);

            Double[] incArray = new Double[properties.inclination.size()];
            incArray = properties.inclination.toArray(incArray);

            Integer[] numSatsArray = new Integer[properties.numberOfSats.size()];
            numSatsArray = properties.numberOfSats.toArray(numSatsArray);

            ArrayList<WalkerParameters> constellationParams = new ArrayList<>();
            constellationParams = EnumerateWalkerConstellations.fullFactWalker(ArrayUtils.toPrimitive(altArray),
                    ArrayUtils.toPrimitive(incArray),
                    ArrayUtils.toPrimitive(numSatsArray));

            for (WalkerParameters params : constellationParams) {

                //create subspace directory
                properties.currentDSMSubspace = new File(System.getProperty("tatc.dsms"),
                        "Subspace" + Integer.toString(100000 + properties.evalCounter).substring(1));
                properties.currentDSMSubspace.mkdir();

                TATCWalker arch;
                double incl;

                if (params.getI() == -1) {
                    incl = Orbits.incSSO(params.getA()-Constants.WGS84_EARTH_EQUATORIAL_RADIUS);
                    //incl = this.getSSOInclination(params.getA());
                } else {
                    incl = params.getI();
                }

                arch = new TATCWalker(params.getA(), incl, params.getT(), params.getP(), params.getF());

                //start date and end date/coverage
                MissionConcept newConcept = properties.tsr.getMissionConcept().copy();

                try {
                    archEval.reductionAndMetrics(arch, newConcept);
                    archEval.costAndRisk(arch, newConcept);
                    properties.evalCounter++;
                } catch (ReductionMetricsException rmEx) {
                    Logger.getLogger(StandardFormProblemFullFact.class.getName()).log(Level.SEVERE, null, rmEx);
                    throw new IllegalStateException("Evaluation of solution in R&M failed.", rmEx);
                } catch (CostRiskException crEx) {
                    Logger.getLogger(StandardFormProblemFullFact.class.getName()).log(Level.SEVERE, null, crEx);
                    throw new IllegalStateException("Evaluation of solution in C&R failed.", crEx);
                }
                //set cost and risk metrics
                ResultOutput crOutput = JSONIO.readJSON(properties.cr.getOutputFile(), ResultOutput.class);

                Logger.getGlobal().fine(String.format(
                        "avg revisit: %.2f[min], lifecycle cost: %.2f[$],",
                        properties.rm.getMetrics()[0] / 60.,
                        crOutput.getLifecycleCost().getEstimate()));
                Logger.getGlobal().fine(String.format(
                        "avg response: %.2f[min], lifecycle cost: %.2f[$],",
                        properties.rm.getMetrics()[1] / 60.,
                        crOutput.getLifecycleCost().getEstimate()));

                bufferedWriter.append(Double.toString(params.getA()) + ",");
                bufferedWriter.append(Double.toString(FastMath.toDegrees(incl)) + ",");
                bufferedWriter.append(Double.toString(params.getT()) + ",");
                bufferedWriter.append(Double.toString(params.getP()) + ",");
                bufferedWriter.append(Double.toString(params.getF()) + ",");
                bufferedWriter.append(Double.toString(properties.rm.getMetrics()[0] / 60.) + ",");
                bufferedWriter.append(Double.toString(properties.rm.getMetrics()[1] / 60.) + ",");
                bufferedWriter.append(Double.toString(crOutput.getLifecycleCost().getEstimate()));
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
