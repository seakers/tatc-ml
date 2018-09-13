/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.evaluation.costandrisk;

/**
 * Instrument data – any applicable fields, not all are required for each
 * payload
 *
 * @author nhitomi
 */
public class Payload {

    /**
     * name of instrument payload (NICM types)
     */
    private final String name;

    /**
     * design life (on orbit) for instrument payload [months]
     */
    private final double designLife;

    /**
     * required instrument functionality (e.g. 'active', 'passive', etc)
     */
    private final String functionType;

    /**
     * expected total payload mass (all related components) [kg]
     */
    private final double totalMass;

    /**
     * expected total instrument mass (antenna, optics, etc) [kg]
     */
    private final double instrumentMass;

    /**
     * required total instrument payload peak power [W] integer
     * techReadinessLevel technological readiness of the subsystem (usual 1-9
     * levels)
     *
     */
    private final double peakPower;

    /**
     * overall spacecraft technical readiness level (usual 1-9 levels)
     */
    private final int techReadinessLevel;

    /**
     * mounting location of sensor (e.g. 'body', 'mast', 'probe'), where 'body'
     * is assumed in the absence of a recognized token
     */
    private final String mounting;

    /**
     * aperture diameter of instrument, as relevant [cm]
     */
    private final double apertureDiameter;

    /**
     * expected “raw” science data capture rate [bps]
     */
    private final double dataRate;

    /**
     *
     * @param name
     * @param designLife
     * @param functionType
     * @param totalMass
     * @param instrumentMass
     * @param peakPower
     * @param techReadinessLevel
     * @param mounting
     * @param apertureDiameter
     * @param dataRate
     */
    public Payload(String name, double designLife, String functionType,
            double totalMass, double instrumentMass, double peakPower,
            int techReadinessLevel, String mounting, double apertureDiameter, 
            double dataRate) {
        this.name = name;
        this.designLife = designLife;
        this.functionType = functionType;
        this.totalMass = totalMass;
        this.instrumentMass = instrumentMass;
        this.peakPower = peakPower;
        this.techReadinessLevel = techReadinessLevel;
        this.mounting = mounting;
        this.apertureDiameter = apertureDiameter;
        this.dataRate = dataRate;
    }

    public String getName() {
        return name;
    }

    public double getDesignLife() {
        return designLife;
    }

    public String getFunctionType() {
        return functionType;
    }

    public double getTotalMass() {
        return totalMass;
    }

    public double getInstrumentMass() {
        return instrumentMass;
    }

    public double getPeakPower() {
        return peakPower;
    }

    public String getMounting() {
        return mounting;
    }

    public double getApertureDiameter() {
        return apertureDiameter;
    }

    public double getDataRate() {
        return dataRate;
    }

    public int getTechReadinessLevel() {
        return techReadinessLevel;
    }
}
