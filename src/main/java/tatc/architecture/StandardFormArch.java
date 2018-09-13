/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.architecture;

import java.util.Set;
import org.moeaframework.core.Solution;
import tatc.architecture.variable.MonolithVariable;


/**
 *
 * @author Prachi
 */
public class StandardFormArch extends Solution {

    private static final long serialVersionUID = -453938510114132596L;

    public StandardFormArch(int numberOfVariables, int numberOfObjectives, Set<MonolithVariable> existingSatellites) {
        super(numberOfVariables, numberOfObjectives);
    }

    /**
     * Private constructor used for copying solution
     * @param solution 
     */
    private StandardFormArch(Solution solution) {
        super(solution);
    }

    @Override
    public Solution copy() {
        return new StandardFormArch(this);
    }
    
}
