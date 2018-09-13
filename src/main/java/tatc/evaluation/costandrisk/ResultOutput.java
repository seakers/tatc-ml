/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.evaluation.costandrisk;

/**
 * Master output from Cost & Risk executable
 *
 * @author Prachichi
 */
public class ResultOutput {

    /**
     * total cost estimate for mission architecture under analysis
     */
    private final CostEstimate lifecycleCost;

    /**
     * total cost to produce the physical satellites themselves
     */
    private final CostEstimate hardwareCost;

    /**
     * total cost of integrating, assembling and testing the system
     */
    private final CostEstimate iatCost;

    /**
     * total systems engineering program-level cost
     */
    private final CostEstimate programCost;

    /**
     * total cost of ground system and ground operations
     */
    private final CostEstimate groundCost;

    /**
     * total cost of launch vehicle and launch operations
     */
    private final CostEstimate launchCost;

    /**
     * total mission operations cost estimate
     */
    private final CostEstimate operationsCost;

    /**
     * total non-recurring mission costs estimate
     */
    private final CostEstimate nonRecurringCost;

    /**
     * total recurring mission costs estimate
     */
    private final CostEstimate recurringCost;

    /**
     * array of spacecraft rankings according to estimated cost
     */
    private final SpacecraftRank[] spacecraftRank;

    public ResultOutput(CostEstimate lifecycleCost, CostEstimate hardwareCost, CostEstimate iatCost, CostEstimate programCost, CostEstimate groundCost, CostEstimate launchCost, CostEstimate operationsCost, CostEstimate nonRecurringCost, CostEstimate recurringCost, SpacecraftRank[] spacecraftRank) {
        this.lifecycleCost = lifecycleCost;
        this.hardwareCost = hardwareCost;
        this.iatCost = iatCost;
        this.programCost = programCost;
        this.groundCost = groundCost;
        this.launchCost = launchCost;
        this.operationsCost = operationsCost;
        this.nonRecurringCost = nonRecurringCost;
        this.recurringCost = recurringCost;
        this.spacecraftRank = spacecraftRank;
    }

    public CostEstimate getLifecycleCost() {
        return lifecycleCost;
    }

    public CostEstimate getHardwareCost() {
        return hardwareCost;
    }

    public CostEstimate getIatCost() {
        return iatCost;
    }

    public CostEstimate getProgramCost() {
        return programCost;
    }

    public CostEstimate getGroundCost() {
        return groundCost;
    }

    public CostEstimate getLaunchCost() {
        return launchCost;
    }

    public CostEstimate getOperationsCost() {
        return operationsCost;
    }

    public CostEstimate getNonRecurringCost() {
        return nonRecurringCost;
    }

    public CostEstimate getRecurringCost() {
        return recurringCost;
    }

    public SpacecraftRank[] getSpacecraftRank() {
        return spacecraftRank;
    }
}
