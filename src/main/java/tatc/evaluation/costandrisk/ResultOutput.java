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
    private final SubCost lifecycleCost;

    /**
     * total cost to produce the physical satellites themselves
     */
    private final SubCost hardwareCost;

    /**
     * total cost of integrating, assembling and testing the system
     */
    private final SubCost iatCost;

    /**
     * total systems engineering program-level cost
     */
    private final SubCost programCost;

    /**
     * total cost of ground system and ground operations
     */
    private final SubCost groundCost;

    /**
     * total cost of launch vehicle and launch operations
     */
    private final SubCost launchCost;

    /**
     * total mission operations cost estimate
     */
    private final SubCost operationsCost;

    /**
     * total non-recurring mission costs estimate
     */
    private final SubCost nonRecurringCost;

    /**
     * total recurring mission costs estimate
     */
    private final SubCost recurringCost;

    /**
     * array of spacecraft rankings according to estimated cost
     */
    private final SpacecraftRank[] spacecraftRank;
    
    /**
     * array of system risks
     */
    private final SystemRisk[] systemRisk;
    
    /**
     * 
     * @param lifecycleCost
     * @param hardwareCost
     * @param iatCost
     * @param programCost
     * @param groundCost
     * @param launchCost
     * @param operationsCost
     * @param nonRecurringCost
     * @param recurringCost
     * @param spacecraftRank 
     * @param systemRisk
     */
    public ResultOutput(SubCost lifecycleCost, SubCost hardwareCost, SubCost iatCost, 
            SubCost programCost, SubCost groundCost, 
            SubCost launchCost, SubCost operationsCost, 
            SubCost nonRecurringCost, SubCost recurringCost, 
            SpacecraftRank[] spacecraftRank,
            SystemRisk[] systemRisk) {
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
        this.systemRisk = systemRisk;
    }

    public SubCost getLifecycleCost() {
        return lifecycleCost;
    }

    public SubCost getHardwareCost() {
        return hardwareCost;
    }

    public SubCost getIatCost() {
        return iatCost;
    }

    public SubCost getProgramCost() {
        return programCost;
    }

    public SubCost getGroundCost() {
        return groundCost;
    }

    public SubCost getLaunchCost() {
        return launchCost;
    }

    public SubCost getOperationsCost() {
        return operationsCost;
    }

    public SubCost getNonRecurringCost() {
        return nonRecurringCost;
    }

    public SubCost getRecurringCost() {
        return recurringCost;
    }

    public SpacecraftRank[] getSpacecraftRank() {
        return spacecraftRank;
    }
    
    public SystemRisk[] getSystemRisk() {
        return systemRisk;
    }
}
