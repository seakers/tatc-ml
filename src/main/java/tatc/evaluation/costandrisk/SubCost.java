/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.evaluation.costandrisk;

/**
 * Mission architecture cost estimate, with statistics
 *
 * @author Prachichi
 */
public class SubCost {

    /**
     * expected cost of some aspect of mission architecture [FYk$]
     */
    private final double estimate;

    /**
     * standard error associated with given cost estimate [FYk$]
     */
    private final double standardError;

    /**
     * fiscal year associated with the produced estimate
     */
    private final int fiscalYear;

    public SubCost(double estimate, int fiscalYear, double standardError) {
        this.estimate = estimate;
        this.standardError = standardError;
        this.fiscalYear = fiscalYear;
    }

    public double getEstimate() {
        return estimate;
    }

    public double getStandardError() {
        return standardError;
    }

    public int getFiscalYear() {
        return fiscalYear;
    }
}
