package tatc.tradespaceiterator;

import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.BinaryVariable;
import org.moeaframework.problem.AbstractProblem;
import org.orekit.errors.OrekitException;
import org.orekit.time.AbsoluteDate;
import tatc.architecture.StandardFormArchitecture;
import tatc.architecture.TATCTrain;
import tatc.architecture.TATCWalker;
import tatc.architecture.specifications.DSMSpecification;
import tatc.architecture.specifications.MissionConcept;
import tatc.architecture.variable.IntegerVariable;
import tatc.evaluation.costandrisk.ResultOutput;
import tatc.exceptions.CostRiskException;
import tatc.exceptions.ReductionMetricsException;
import tatc.util.JSONIO;

import java.io.File;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StandardFormProblemGATrain extends AbstractProblem {
    StandardFormProblemProperties properties;
    ArchitectureEvaluator archEval;


    public StandardFormProblemGATrain(StandardFormProblemProperties properties){
        super(2, 2);
        this.properties=properties;
        this.archEval=new ArchitectureEvaluator(properties);

    }


    @Override
    public void evaluate(Solution solution) {
        try {
            //create subspace directory
            properties.currentDSMSubspace = new File(System.getProperty("tatc.dsms"),
                    "Subspace" + Integer.toString(100000 + properties.evalCounter).substring(1));
            properties.currentDSMSubspace.mkdir();

            StandardFormArchitecture soln = null;
            if (solution instanceof StandardFormArchitecture) {
                soln = (StandardFormArchitecture) solution;
            } else {
                throw new IllegalArgumentException(
                        String.format("Expected a TATCArchitecture."
                                + " Found %s", solution.getClass()));
            }

            DSMSpecification dsmSpec = JSONIO.readJSON(properties.rm.getInputFile(), DSMSpecification.class);
            AbsoluteDate startDate = dsmSpec.getMissionConcept().getPerformancePeriod()[0];
            //read in values
            double sma = ((StandardFormProblemPropertiesTrain)properties).smas.get(((IntegerVariable) soln.getVariable(0)).getValue());
            BitSet bits=((BinaryVariable) soln.getVariable(1)).getBitSet();
            ArrayList<Double> LTANsFiltered = IntStream.range(0, ((StandardFormProblemPropertiesTrain)properties).LTANs.size())
                            .filter(i -> bits.get(i))
                            .mapToObj(((StandardFormProblemPropertiesTrain)properties).LTANs::get)
                            .collect(Collectors.toCollection(ArrayList::new));


            TATCTrain arch = new TATCTrain(sma, LTANsFiltered, startDate);

            //start date and end date/coverage
            MissionConcept newConcept = properties.tsr.getMissionConcept().copy();

            try {
                archEval.reductionAndMetrics(arch, newConcept);
                archEval.costAndRisk(arch, newConcept);
            } catch (ReductionMetricsException rmEx) {
                Logger.getLogger(StandardFormProblemGAWalker.class.getName()).log(Level.SEVERE, null, rmEx);
                throw new IllegalStateException("Evaluation of solution in R&M failed.", rmEx);
            } catch (CostRiskException crEx) {
                Logger.getLogger(StandardFormProblemGAWalker.class.getName()).log(Level.SEVERE, null, crEx);
                throw new IllegalStateException("Evaluation of solution in C&R failed.", crEx);
            }

            //set rm metrics
            solution.setObjective(0, properties.rm.getMetrics()[0]); //average revisit time

            //set cr metrics
            ResultOutput crOutput = JSONIO.readJSON(properties.cr.getOutputFile(), ResultOutput.class);
            solution.setObjective(1, crOutput.getLifecycleCost().getEstimate());

            Logger.getGlobal().fine(String.format(
                    "avg revisit: %.2f[min], lifecycle cost: %.2f[$],",
                    properties.rm.getMetrics()[0] / 60.,
                    solution.getObjective(1)));

            properties.evalCounter++;
        }catch (OrekitException e) {
            e.printStackTrace();
        }

    }

    //structure of solution with walker params
    @Override
    public final Solution newSolution() {
        Solution sol = new StandardFormArchitecture(getNumberOfVariables(), getNumberOfObjectives(), properties.existingSatellites);
        sol.setVariable(0, new IntegerVariable(0, 0, ((StandardFormProblemPropertiesTrain)properties).smas.size() - 1));
        sol.setVariable(1, new BinaryVariable(((StandardFormProblemPropertiesTrain)properties).LTANs.size()));
        return sol;
    }
}
