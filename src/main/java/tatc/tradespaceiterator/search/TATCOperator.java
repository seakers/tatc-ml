/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.tradespaceiterator.search;

import org.moeaframework.core.variable.RealVariable;
import tatc.architecture.StandardFormArchitecture;

/**
 * @author Prachi
 */
public class TATCOperator extends AbstractTATCOperator {

    private final boolean negate;

    private final int decision;

    private final double level;

    public TATCOperator(boolean negate, int decision, double level) {
        this.negate = negate;
        this.decision = decision;
        this.level = level;
    }

    /**
     * This operator only takes in one solution and modifies it
     *
     * @return
     */
    @Override
    public int getArity() {
        return 1;
    }

    @Override
    protected StandardFormArchitecture evolve(StandardFormArchitecture child) {

        StandardFormArchitecture arch = (StandardFormArchitecture) child.copy();
        int variables = child.getNumberOfVariables();

        RealVariable realVariableLevel = new RealVariable(level, level, level);

        /*
         * check if the decision is valid, and if it is, apply the operator
         */
        if (decision <= variables) {
            if (!negate) {
                arch.setVariable(decision, realVariableLevel);
            }
            else {
                arch.setVariable(decision, realVariableLevel);
            }
        } else {
            throw new IllegalArgumentException("The number of variables and decisions don't match");
        }

        return arch;
    }

    @Override
    public String toString() {
        String operatorName;

        if (!this.negate) {
            operatorName = "TATCOperator{" + String.format("TATCOperator{ %d = %f }", this.decision, this.level);
        } else {
            operatorName = "TATCOperator{" + String.format("TATCOperator{ %d != %f }", this.decision, this.level);
        }
        return operatorName;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + (this.negate ? 1 : 0);
        hash = 83 * hash + this.decision;
        hash = 83 * hash + Double.valueOf(this.level).hashCode();
        return hash;
    }
}