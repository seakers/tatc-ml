package tatc.tradespaceiterator;

import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.BinaryVariable;
import org.moeaframework.problem.AbstractProblem;
import tatc.architecture.StandardFormArchitecture;
import tatc.architecture.variable.IntegerVariable;

public class StandardFormProblemGATrain extends AbstractProblem {
    StandardFormProblemProperties properties;
    ArchitectureEvaluator archEval;


    public StandardFormProblemGATrain(StandardFormProblemProperties properties){
        super(3, 2);
        this.properties=properties;
        this.archEval=new ArchitectureEvaluator(properties);

    }


    @Override
    public void evaluate(Solution solution) {
    }

    //structure of solution with walker params
    @Override
    public final Solution newSolution() {
        Solution sol = new StandardFormArchitecture(getNumberOfVariables(), getNumberOfObjectives(), properties.existingSatellites);
        sol.setVariable(0, new IntegerVariable(0, 0, properties.smas.size() - 1));
        sol.setVariable(1, new BinaryVariable((int) Math.pow(2,properties.LTANs.size())));
        return sol;
    }
}
