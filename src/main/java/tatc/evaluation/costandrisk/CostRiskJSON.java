package tatc.evaluation.costandrisk;

public class CostRiskJSON {

    public final SubCost groundCost;
    public final SubCost hardwareCost;
    public final SubCost iatCost;
    public final SubCost launchCost;
    public final SubCost lifecycleCost;
    public final SubCost nonRecurringCost;
    public final SubCost operationsCost;
    public final SubCost programCost;
    public final SubCost recurringCost;
    public final SpacecraftRank[] spacecraftRank;
    public final SystemRisk[] systemRisk;


    public CostRiskJSON(SubCost groundCost, SubCost hardwareCost, 
            SubCost iatCost, SubCost launchCost, SubCost lifecycleCost, 
            SubCost nonRecurringCost, SubCost operationsCost, 
            SubCost programCost, SubCost recurringCost,
            SpacecraftRank[] spacecraftRank, SystemRisk[] systemRisk) {
        this.groundCost = groundCost;
        this.hardwareCost = hardwareCost;
        this.iatCost = iatCost;
        this.launchCost = launchCost;
        this.lifecycleCost = lifecycleCost;
        this.nonRecurringCost = nonRecurringCost;
        this.operationsCost = operationsCost;
        this.programCost = programCost;
        this.recurringCost = recurringCost;
        this.spacecraftRank = spacecraftRank;
        this.systemRisk = systemRisk;
    }

    public SubCost getGroundCost() {
        return groundCost;
    }

    public SubCost getHardwareCost() {
        return hardwareCost;
    }

    public SubCost getIatCost() {
        return iatCost;
    }

    public SubCost getLaunchCost() {
        return launchCost;
    }

    public SubCost getLifecycleCost() {
        return lifecycleCost;
    }

    public SubCost getNonRecurringCost() {
        return nonRecurringCost;
    }

    public SubCost getOperationsCost() {
        return operationsCost;
    }

    public SubCost getProgramCost() {
        return programCost;
    }

    public SubCost getRecurringCost() {
        return recurringCost;
    }
    
    public SpacecraftRank[] getSpacecraftRanks() {
        return spacecraftRank;
    }
    
    public SystemRisk[] getSystemRisk() {
        return systemRisk;
    }
}
