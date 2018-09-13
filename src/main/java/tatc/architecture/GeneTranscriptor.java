/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.architecture;

import tatc.architecture.variable.GeneVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variable;

/**
 * Reads in a chromosome containing genes and transcripts it to a Solution
 * containing a vector of the copied design/decision variables.
 *
 * @author nozomihitomi
 */
public class GeneTranscriptor {

    /**
     * Expands any gene in a solution to to create a solution containing a
     * vector of decision variables.
     *
     * @param soln the solution to be translated
     * @return 
     */
    public static Solution trasncript(Solution soln) {
        ArrayList<Variable> variables = new ArrayList<>();
        for (int i = 0; i < soln.getNumberOfVariables(); i++) {
            Variable var = soln.getVariable(i);
            if (var instanceof GeneVariable) {
                List<Variable> varList = Arrays.asList(((GeneVariable) var).getVariables());
                variables.addAll(varList);
            } else {
                variables.add(var);
            }
        }
        Solution translatedSoln = new Solution(variables.size(),
                soln.getNumberOfObjectives(), soln.getNumberOfConstraints());

        for (int i = 0; i < variables.size(); i++) {
            translatedSoln.setVariable(i, variables.get(i));
        }
        return translatedSoln;
    }

}
