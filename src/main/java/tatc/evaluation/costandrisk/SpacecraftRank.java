/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.evaluation.costandrisk;

/**
 * Global rank of spacecraft (versus overall constellation) as a cost driver
 * @author Prachichi
 */
public class SpacecraftRank {

    /**
     * spacecraft zero-based index within the constellation
     */
    private final int spacecraftIndex;

    /**
     * estimated total cost of the ranked spacecraft [FYk$]
     */
    private final double totalCost;

    /**
     * fiscal year associated with the produced estimate
     */
    private final int fiscalYear;

    /**
     * rank of the spacecraft (1 being the highest) in terms of cost relative to
     * the other spacecraft in the constellation
     */
    private final int rank;

    public SpacecraftRank(int spacecraftIndex, double totalCost, int fiscalYear, int rank) {
        this.spacecraftIndex = spacecraftIndex;
        this.totalCost = totalCost;
        this.fiscalYear = fiscalYear;
        this.rank = rank;
    }

    public int getSpacecraftIndex() {
        return spacecraftIndex;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public int getFiscalYear() {
        return fiscalYear;
    }

    public int getRank() {
        return rank;
    }
}
