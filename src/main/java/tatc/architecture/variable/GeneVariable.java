/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.architecture.variable;

import java.util.Arrays;
import org.moeaframework.core.Variable;

/**
 * A gene is group of variables that have a significant meaning to the
 * underlying problem.
 *
 * @author nozomihitomi
 */
public class GeneVariable implements Variable {

    private static final long serialVersionUID = 6417288439770115829L;

    /**
     * The variables that belong to this gene
     */
    private final Variable[] variables;

    /**
     * Create and define a new gene
     * @param variables the variables that belong to this gene
     */
    public GeneVariable(Variable[] variables) {
        this.variables = variables;
    }
    
    private GeneVariable(GeneVariable gene){
        int nVar =  gene.getVariables().length;
        Variable[] origVariables = gene.getVariables();
        Variable[] copiedVariables = new Variable[nVar];
        for(int i=0; i<nVar; i++){
            copiedVariables[i] = origVariables[i].copy();
        }
        this.variables = copiedVariables;
    }

    /**
     * Gets the variables that are stored in this gene
     * @return the variables that are stored in this gene
     */
    public Variable[] getVariables() {
        return variables;
    }

    @Override
    public void randomize(){
        for(Variable var : variables){
            var.randomize();
        }
    }

    @Override
    public Variable copy(){
        return new GeneVariable(this);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + Arrays.deepHashCode(this.variables);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GeneVariable other = (GeneVariable) obj;
        if (!Arrays.deepEquals(this.variables, other.variables)) {
            return false;
        }
        return true;
    }   
}
