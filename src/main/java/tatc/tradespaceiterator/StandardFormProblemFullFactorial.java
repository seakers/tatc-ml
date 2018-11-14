package tatc.tradespaceiterator;

import org.hipparchus.util.FastMath;
import org.orekit.errors.OrekitException;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.Constants;
import seakers.orekit.constellations.EnumerateConstellations;
import seakers.orekit.constellations.TrainParameters;
import seakers.orekit.constellations.WalkerParameters;
import seakers.orekit.util.Orbits;
import tatc.architecture.TATCTrain;
import tatc.architecture.TATCWalker;
import tatc.architecture.specifications.DSMSpecification;
import tatc.architecture.specifications.MissionConcept;
import tatc.evaluation.costandrisk.CostRisk;
import tatc.evaluation.costandrisk.ResultOutput;
import tatc.evaluation.reductionmetrics.ReductionMetrics;
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
        String problem = properties.tsr.getMissionConcept().getProblemType();
        switch (problem){
            case "Walker":
                startWalker();
                break;
            case "Train":
                startTrain();
                break;
            default:
                throw new IllegalArgumentException("No Problem Type found.");
        }
    }

    public void startWalker(){
        //output the variables and metrics in a csv file
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(System.getProperty("tatc.access_results"), "results.csv")))) {

            bufferedWriter.append("Altitude[m],Inclination[deg],Satellites,Planes,Phase,Avg Revisit Time[min],Avg Response Time[min],Cost[FY10$M]");
            bufferedWriter.newLine();

            //convert arraylists to array in order to pass into fullFactWalker
            Double[] smaArray = ((StandardFormProblemPropertiesWalker)properties).smas.toArray(new Double[((StandardFormProblemPropertiesWalker)properties).smas.size()]);
            Double[] incArray = ((StandardFormProblemPropertiesWalker)properties).inclination.toArray(new Double[((StandardFormProblemPropertiesWalker)properties).inclination.size()]);
            Integer[] numSatsArray = ((StandardFormProblemPropertiesWalker)properties).numberOfSats.toArray(new Integer[((StandardFormProblemPropertiesWalker)properties).numberOfSats.size()]);

            ArrayList<WalkerParameters> constellationParams = EnumerateConstellations.fullFactWalker(smaArray, incArray, numSatsArray);

            for (WalkerParameters params : constellationParams) {

                //create subspace directory
                properties.currentDSMSubspace = new File(System.getProperty("tatc.dsms"),
                        "Subspace" + Integer.toString(100000 + properties.evalCounter).substring(1));
                properties.currentDSMSubspace.mkdir();

                double incl;
                if (params.getI() == -1) {
                    incl = Orbits.incSSO(params.getA()-Constants.WGS84_EARTH_EQUATORIAL_RADIUS);
                } else {
                    incl = params.getI();
                }

                TATCWalker arch = new TATCWalker(params.getA(), incl, params.getT(), params.getP(), params.getF());

                //start date and end date/coverage
                MissionConcept newConcept = properties.tsr.getMissionConcept().copy();

                try {
                    archEval.reductionAndMetrics(arch, newConcept);
                    archEval.costAndRisk(arch, newConcept);
                    properties.evalCounter++;
                } catch (ReductionMetricsException rmEx) {
                    Logger.getLogger(ReductionMetrics.class.getName()).log(Level.SEVERE, null, rmEx);
                    throw new IllegalStateException("Evaluation of solution in R&M failed.", rmEx);
                } catch (CostRiskException crEx) {
                    Logger.getLogger(CostRisk.class.getName()).log(Level.SEVERE, null, crEx);
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


    public void startTrain(){
        //output the variables and metrics in a csv file
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(System.getProperty("tatc.access_results"), "results.csv")))) {
            //The LTANs should be defined somewhere in the tsr and ArrayList<Double> LTANs created in StandardFormProblemProperties

            bufferedWriter.append("Altitude[m],Inclination[deg],");
            for (int i=0; i<((StandardFormProblemPropertiesTrain)properties).LTANs.size(); i++){
                bufferedWriter.append(String.format("LTAN%d[hours],",i));
            }
            bufferedWriter.append("Avg Revisit Time[min],Avg Response Time[min],Cost[FY10$M]");
            bufferedWriter.newLine();

            //convert arraylists to array in order to pass into fullFactWalker
            Double[] smaArray = ((StandardFormProblemPropertiesTrain)properties).smas.toArray(new Double[((StandardFormProblemPropertiesTrain)properties).smas.size()]);

            DSMSpecification dsmSpec = JSONIO.readJSON(properties.rm.getInputFile(), DSMSpecification.class);
            AbsoluteDate startDate = dsmSpec.getMissionConcept().getPerformancePeriod()[0];

            ArrayList<TrainParameters> constellationParams=EnumerateConstellations.fullFactTrain(smaArray,((StandardFormProblemPropertiesTrain)properties).LTANs);

            for (TrainParameters train : constellationParams) {
                //create subspace directory
                properties.currentDSMSubspace = new File(System.getProperty("tatc.dsms"),
                        "Subspace" + Integer.toString(100000 + properties.evalCounter).substring(1));
                properties.currentDSMSubspace.mkdir();

                TATCTrain arch = new TATCTrain(train.getA(),train.getLTANs(),startDate);

                //start date and end date/coverage
                MissionConcept newConcept = properties.tsr.getMissionConcept().copy();

                try {
                    archEval.reductionAndMetrics(arch, newConcept);
                    archEval.costAndRisk(arch, newConcept);
                    properties.evalCounter++;
                } catch (ReductionMetricsException rmEx) {
                    Logger.getLogger(ReductionMetrics.class.getName()).log(Level.SEVERE, null, rmEx);
                    throw new IllegalStateException("Evaluation of solution in R&M failed.", rmEx);
                } catch (CostRiskException crEx) {
                    Logger.getLogger(CostRisk.class.getName()).log(Level.SEVERE, null, crEx);
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

                bufferedWriter.append(Double.toString(arch.getSma()) + ",");
                bufferedWriter.append(Double.toString(arch.getInclination()) + ",");
                for (int i=0; i<((StandardFormProblemPropertiesTrain)properties).LTANs.size(); i++){
                    if (arch.getLTANs().contains(((StandardFormProblemPropertiesTrain)properties).LTANs.get(i))){
                        bufferedWriter.append(Double.toString(((StandardFormProblemPropertiesTrain)properties).LTANs.get(i)) + ",");
                    }else {
                        bufferedWriter.append("NONE,");
                    }
                }
                bufferedWriter.append(Double.toString(properties.rm.getMetrics()[0] / 60.) + ",");
                bufferedWriter.append(Double.toString(properties.rm.getMetrics()[1] / 60.) + ",");
                bufferedWriter.append(Double.toString(crOutput.getLifecycleCost().getEstimate()));
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException | OrekitException e) {
            e.printStackTrace();
        }
    }
}
