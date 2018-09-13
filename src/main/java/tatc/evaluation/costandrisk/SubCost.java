package tatc.evaluation.costandrisk;

/**
 * Wrapper for sub-costs 
 * @author Prachi
 */

public class SubCost {
    public final double estimate;
    public final int fiscalYear;
    public final double standardError;

    public SubCost(double estimate, int fiscalYear, double standardError) {
        this.estimate = estimate;
        this.fiscalYear = fiscalYear;
        this.standardError = standardError;
    }

    public double getEstimate() {
        return estimate;
    }

    public int getFiscalYear() {
        return fiscalYear;
    }

    public double getStandardError() {
        return standardError;
    }
}
