/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.evaluation.costandrisk;

/**
 * Model of a proposed spacecraft
 *
 * @author nhitomi
 */
public class Spacecraft {

    /**
     * stabilization method (e.g. '3axis', 'spinning', etc)
     */
    private final String stabilizationType;

    /**
     * total mass of the spacecraft without propellant [kg]
     */
    private final double totalDryMass;

    /**
     * type of spacecraft propellant (e.g. 'monoprop', etc)
     */
    private final String propellantType;

    /**
     * total spacecraft delta-V requirement [km/s]
     */
    private final double totalDeltaV;
    /**
     * initial mass of spacecraft propellant [kg]
     */
    private final double propellantMass;

    /**
     * expected total spacecraft maximum power [W]
     */
    private final double totalMaxPower;

    /**
     * peak/extreme total spacecraft maximum power [W]
     */
    private final double peakMaxPower;
    /**
     * required spacecraft beginning-of-life power production [W]
     */
    private final double beginLifePower;

    /**
     * expected spacecraft end-of-life power production [W]
     */
    private final double endLifePower;

    /**
     * required orbital altitude (assuming circular orbit) [km]
     */
    private final double alt;

    /**
     * required orbital plane inclination of the spacecraft [deg]
     */
    private final double incl;

    /**
     * required orbital RAAN element value for the spacecraft [deg]
     */
    private final double RAAN;

    /**
     * required frequency for s/c station keeping maneuvers in order to maintain
     * constellation configuration [maneuvers/month]
     */
    private final double stationKeepingMnvrFreq;

    /**
     * required frequency for s/c atmospheric drag maneuvers in order to
     * maintain relative separation [maneuvers/month]
     */
    private final double atmDragMnvrFreq;

    /**
     * can [1] or cannot [0] maneuver spacecraft via ground command These
     * maneuver commands may be used to avoid collision.
     */
    private final boolean canMnvrViaGndCmd;

    /**
     * required spacecraft attitude pointing accuracy [deg]
     */
    private final double pointingAccuracy;

    /**
     * required spacecraft radiation tolerance, if non-negative, else indicates
     * that orbital altitude drives requirement [mRad]
     */
    private final double radiationTolerance;

    /**
     * required communications band designators (e.g. 'k', 's', etc) string
     * thermalControlType required spacecraft thermal contol type ('active',
     * passive', etc)
     */
    private final String commBandTypes;

    /**
     * expected order in launch schedule (e.g. 1=first, 2=second, etc)
     */
    private final int launchNumber;

    /**
     * indicator, if any, of spacecraft launch priority aboard designated launch
     * vehicle (e.g. 0=none, 1=primary, 2=secondary, etc)
     */
    private final int launchPriority;

    /**
     * spacecraft is [1] or is not [0] spare (needed to address failure)
     */
    private final int isSpare;
    /**
     * overall spacecraft technical readiness level (usual 1-9 levels)
     */
    private final int techReadinessLevel;

    /**
     * Payload: instrument data – any applicable fields, not all are required
     * for each payload array of all payload instruments on board the spacecraft
     */
    private final Payload[] payload;

    /**
     * 
     * @param stabilizationType
     * @param totalDryMass
     * @param propellantType
     * @param totalDeltaV
     * @param propellantMass
     * @param totalMaxPower
     * @param peakMaxPower
     * @param beginLifePower
     * @param endLifePower
     * @param alt
     * @param incl
     * @param RAAN
     * @param stationKeepingMnvrFreq
     * @param atmDragMnvrFreq
     * @param canMnvrViaGndCmd
     * @param pointingAccuracy
     * @param radiationTolerance
     * @param commBandTypes
     * @param launchNumber
     * @param launchPriority
     * @param isSpare
     * @param techReadinessLevel
     * @param payload 
     */
    public Spacecraft(String stabilizationType, double totalDryMass, 
            String propellantType, double totalDeltaV, double propellantMass, 
            double totalMaxPower, double peakMaxPower, double beginLifePower, 
            double endLifePower, double alt, double incl, double RAAN, 
            double stationKeepingMnvrFreq, double atmDragMnvrFreq, 
            boolean canMnvrViaGndCmd, double pointingAccuracy, 
            double radiationTolerance, String commBandTypes, int launchNumber, 
            int launchPriority, int isSpare, int techReadinessLevel, 
            Payload[] payload) {
        this.stabilizationType = stabilizationType;
        this.totalDryMass = totalDryMass;
        this.propellantType = propellantType;
        this.totalDeltaV = totalDeltaV;
        this.propellantMass = propellantMass;
        this.totalMaxPower = totalMaxPower;
        this.peakMaxPower = peakMaxPower;
        this.beginLifePower = beginLifePower;
        this.endLifePower = endLifePower;
        this.alt = alt;
        this.incl = incl;
        this.RAAN = RAAN;
        this.stationKeepingMnvrFreq = stationKeepingMnvrFreq;
        this.atmDragMnvrFreq = atmDragMnvrFreq;
        this.canMnvrViaGndCmd = canMnvrViaGndCmd;
        this.pointingAccuracy = pointingAccuracy;
        this.radiationTolerance = radiationTolerance;
        this.commBandTypes = commBandTypes;
        this.launchNumber = launchNumber;
        this.launchPriority = launchPriority;
        this.isSpare = isSpare;
        this.techReadinessLevel = techReadinessLevel;
        this.payload = payload;
    }

    public String getStabilizationType() {
        return stabilizationType;
    }

    public double getTotalDryMass() {
        return totalDryMass;
    }

    public String getPropellantType() {
        return propellantType;
    }

    public double getTotalDeltaV() {
        return totalDeltaV;
    }

    public double getPropellantMass() {
        return propellantMass;
    }

    public double getTotalMaxPower() {
        return totalMaxPower;
    }

    public double getPeakMaxPower() {
        return peakMaxPower;
    }

    public double getBeginLifePower() {
        return beginLifePower;
    }

    public double getEndLifePower() {
        return endLifePower;
    }

    public double getAlt() {
        return alt;
    }

    public double getIncl() {
        return incl;
    }

    public double getRAAN() {
        return RAAN;
    }

    public double getStationKeepingMnvrFreq() {
        return stationKeepingMnvrFreq;
    }

    public double getAtmDragMnvrFreq() {
        return atmDragMnvrFreq;
    }

    public boolean isCanMnvrViaGndCmd() {
        return canMnvrViaGndCmd;
    }

    public double getPointingAccuracy() {
        return pointingAccuracy;
    }

    public double getRadiationTolerance() {
        return radiationTolerance;
    }

    public String getCommBandTypes() {
        return commBandTypes;
    }

    public int getLaunchNumber() {
        return launchNumber;
    }

    public int getLaunchPriority() {
        return launchPriority;
    }

    public boolean isIsSpare() {
        return isSpare == 1;
    }

    public int getTechReadinessLevel() {
        return techReadinessLevel;
    }

    public Payload[] getPayload() {
        return payload;
    }
}
