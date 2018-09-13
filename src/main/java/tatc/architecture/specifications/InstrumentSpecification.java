/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.architecture.specifications;

import java.util.Arrays;
import java.util.Objects;

/**
 * Specification for an instrument
 *
 * @author nhitomi
 */
public class InstrumentSpecification {

    private final int conOpsTypeCode;

    private final int conOpsPartner;
    
    private final double mass; //Kg
    
    private final double power; //

    private final double[] spectrum;

    private final double spectralResolution;

    private final int minRadiometricRes;

    private final double alongFieldOfView;

    private final double crossFieldOfView;

    private final double min_iFieldOfView;

    private final boolean isRectangularFOV;

    private final double measurementTime;

    private final double maxDwnlnkLatency;

    private final double maxDataRate;

    private final String solarConditions;

    private final String sunglintPreference;

    private final double minOccultationAlt;

    private final double maxOccultationAlt;

    /**
     *
     * @param conOpsTypeCode
     * @param conOpsPartner
     * @param mass [Kg]
     * @param power [Watts]
     * @param spectrum
     * @param spectralResolution
     * @param minRadiometricRes
     * @param alongFieldOfView
     * @param crossFieldOfView
     * @param min_iFieldOfView
     * @param isRectangularFOV
     * @param measurementTime
     * @param maxDwnlnkLatency
     * @param maxDataRate
     * @param solarConditions
     * @param sunglintPreference
     * @param minOccultationAlt
     * @param maxOccultationAlt
     */
    public InstrumentSpecification(int conOpsTypeCode, int conOpsPartner,
            double mass, double power,
            double[] spectrum, double spectralResolution, int minRadiometricRes,
            double crossFieldOfView, double alongFieldOfView,
            double min_iFieldOfView, boolean isRectangularFOV,
            double measurementTime, double maxDwnlnkLatency,
            double maxDataRate, String solarConditions,
            String sunglintPreference, double minOccultationAlt,
            double maxOccultationAlt) {
        this.conOpsTypeCode = conOpsTypeCode;
        this.conOpsPartner = conOpsPartner;
        this.mass = mass;
        this.power = power;
        this.spectrum = spectrum;
        this.spectralResolution = spectralResolution;
        this.minRadiometricRes = minRadiometricRes;
        this.alongFieldOfView = alongFieldOfView;
        this.crossFieldOfView = crossFieldOfView;
        this.min_iFieldOfView = min_iFieldOfView;
        this.isRectangularFOV = isRectangularFOV;
        this.measurementTime = measurementTime;
        this.maxDwnlnkLatency = maxDwnlnkLatency;
        this.maxDataRate = maxDataRate;
        this.solarConditions = solarConditions;
        this.sunglintPreference = sunglintPreference;
        this.minOccultationAlt = minOccultationAlt;
        this.maxOccultationAlt = maxOccultationAlt;
    }

    /**
     * A space delimited string containing arguments defining the observatory
     * specification
     *
     * @param str a space delimited string with elements in the following order:
     * instrument conops, instrument conops partner, instrument mass [kg],
     * instrument volume [m^3], instrument power [W], spectral range [nm] in
     * min:max form, spectral resolution [nm], minimum radiometric resolution
     * [bits/pixel], field of view [deg] in min:max form, iFOV [deg] in min:max
     * form, measurement time [sec] in min:max form, maximum allowed downlink
     * latency [day], maximum overall data rate [GB/day], solar conditions,
     * sunglint preference, occultation altitudes [km] in min:max form
     * @return an instance of an instrument specification
     */
    public static InstrumentSpecification create(String str) {
        return InstrumentSpecification.create(str.split("\\s+"));
    }

    /**
     * A 16 element string array containing arguments defining the instrument
     * specification
     *
     * @param args a 16 element string array with elements in the following
     * order: instrument conops, instrument conops partner, instrument mass
     * [kg], instrument volume [m^3], instrument power [W], spectral range [nm]
     * in min:max form, spectral resolution [nm], minimum radiometric resolution
     * [bits/pixel], field of view [deg] in min:max form, iFOV [deg] in min:max
     * form, measurement time [sec] in min:max form, maximum allowed downlink
     * latency [day], maximum overall data rate [GB/day], solar conditions,
     * sunglint preference, occultation altitudes [km] in min:max form
     * @return an instance of an instrument specification
     */
    public static InstrumentSpecification create(String[] args) {
        if (args.length < 16) {
            throw new IllegalArgumentException(String.format("Expected 16 arguments. Found %d.", args.length));
        }
        int conOps = Integer.parseInt(args[0]);
        int conOpsPartner = Integer.parseInt(args[1]);
        double mass = Double.parseDouble(args[2]);
        double volume = Double.parseDouble(args[3]);
        double power = Double.parseDouble(args[4]);
        String[] spectrumStr = args[5].split(",");
        double[] spectrum = new double[spectrumStr.length];
        for(int i=0; i<spectrumStr.length; i++){
            spectrum[i]= Double.parseDouble(spectrumStr[i]);
        }
        double spectralResolution = Double.parseDouble(args[6]);
        int minRadiometricResolution = Integer.parseInt(args[7]);
        String[] fov = args[8].split(":");
        double fovMin = Double.parseDouble(fov[0]);//cross track - clock
        double fovMax = Double.parseDouble(fov[1]);//along track - cone
        String[] ifov = args[9].split(":");
        double ifovMin = Double.parseDouble(ifov[0]);
        double ifovMax = Double.parseDouble(ifov[1]);
        String[] measurementTime = args[10].split(":");
        double measurementTimeMin = Double.parseDouble(measurementTime[0]);
        double measurementTimeMax = Double.parseDouble(measurementTime[1]);
        double maximumAllowedDownlinkLatency = Double.parseDouble(args[11]);
        double maximumOverallDataRate = Double.parseDouble(args[12]);
        String solarConditions = args[13];
        String sunglintPref = args[14];
        String[] occultationAltitude = args[15].split(":");
        double occultationAltitudeMin = Double.parseDouble(occultationAltitude[0]);
        double occultationAltitudeMax = Double.parseDouble(occultationAltitude[1]);

        //TODO: some paratmers are missing from instrument specification
        return new InstrumentSpecification(conOps, conOpsPartner, mass, power, spectrum,
                spectralResolution, minRadiometricResolution, fovMin, fovMax,
                ifovMax, true, measurementTimeMax, maximumAllowedDownlinkLatency,
                maximumOverallDataRate, solarConditions, sunglintPref,
                occultationAltitudeMin, occultationAltitudeMax);
    }

    public int getConOpsTypeCode() {
        return conOpsTypeCode;
    }

    public int getConOpsPartner() {
        return conOpsPartner;
    }
    
    public double getMass() {
        return mass;
    }
    
    public double getPower() {
        return power;
    }

    public double[] getSpectrum() {
        return spectrum;
    }

    public double getSpectralResolution() {
        return spectralResolution;
    }

    public int getMinRadiometricRes() {
        return minRadiometricRes;
    }

    public double getAlongFieldOfView() {
        return alongFieldOfView;
    }

    public double getCrossFieldOfView() {
        return crossFieldOfView;
    }

    public double getMin_iFieldOfView() {
        return min_iFieldOfView;
    }

    public boolean isRectangularFOV() {
        return isRectangularFOV;
    }

    public double getMeasurementTime() {
        return measurementTime;
    }

    public double getMaxDwnlnkLatency() {
        return maxDwnlnkLatency;
    }

    public double getMaxDataRate() {
        return maxDataRate;
    }

    public String getSolarConditions() {
        return solarConditions;
    }

    public String getSunglintPreference() {
        return sunglintPreference;
    }

    public double getMinOccultationAlt() {
        return minOccultationAlt;
    }

    public double getMaxOccultationAlt() {
        return maxOccultationAlt;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.conOpsTypeCode;
        hash = 29 * hash + this.conOpsPartner;
        hash = 29 * hash + (int)this.mass;
        hash = 29 * hash + (int)this.power;
        hash = 29 * hash + Arrays.hashCode(this.spectrum);
        hash = 29 * hash + (int) (Double.doubleToLongBits(this.spectralResolution) ^ (Double.doubleToLongBits(this.spectralResolution) >>> 32));
        hash = 29 * hash + this.minRadiometricRes;
        hash = 29 * hash + (int) (Double.doubleToLongBits(this.alongFieldOfView) ^ (Double.doubleToLongBits(this.alongFieldOfView) >>> 32));
        hash = 29 * hash + (int) (Double.doubleToLongBits(this.crossFieldOfView) ^ (Double.doubleToLongBits(this.crossFieldOfView) >>> 32));
        hash = 29 * hash + (int) (Double.doubleToLongBits(this.min_iFieldOfView) ^ (Double.doubleToLongBits(this.min_iFieldOfView) >>> 32));
        hash = 29 * hash + (this.isRectangularFOV ? 1 : 0);
        hash = 29 * hash + (int) (Double.doubleToLongBits(this.measurementTime) ^ (Double.doubleToLongBits(this.measurementTime) >>> 32));
        hash = 29 * hash + (int) (Double.doubleToLongBits(this.maxDwnlnkLatency) ^ (Double.doubleToLongBits(this.maxDwnlnkLatency) >>> 32));
        hash = 29 * hash + (int) (Double.doubleToLongBits(this.maxDataRate) ^ (Double.doubleToLongBits(this.maxDataRate) >>> 32));
        hash = 29 * hash + Objects.hashCode(this.solarConditions);
        hash = 29 * hash + Objects.hashCode(this.sunglintPreference);
        hash = 29 * hash + (int) (Double.doubleToLongBits(this.minOccultationAlt) ^ (Double.doubleToLongBits(this.minOccultationAlt) >>> 32));
        hash = 29 * hash + (int) (Double.doubleToLongBits(this.maxOccultationAlt) ^ (Double.doubleToLongBits(this.maxOccultationAlt) >>> 32));
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
        final InstrumentSpecification other = (InstrumentSpecification) obj;
        if (this.conOpsTypeCode != other.conOpsTypeCode) {
            return false;
        }
        if (this.conOpsPartner != other.conOpsPartner) {
            return false;
        }
        if (this.mass != other.mass){
            return false;
        }
        if (this.power != other.power){
            return false;
        }
        if (Double.doubleToLongBits(this.spectralResolution) != Double.doubleToLongBits(other.spectralResolution)) {
            return false;
        }
        if (this.minRadiometricRes != other.minRadiometricRes) {
            return false;
        }
        if (Double.doubleToLongBits(this.alongFieldOfView) != Double.doubleToLongBits(other.alongFieldOfView)) {
            return false;
        }
        if (Double.doubleToLongBits(this.crossFieldOfView) != Double.doubleToLongBits(other.crossFieldOfView)) {
            return false;
        }
        if (Double.doubleToLongBits(this.min_iFieldOfView) != Double.doubleToLongBits(other.min_iFieldOfView)) {
            return false;
        }
        if (this.isRectangularFOV != other.isRectangularFOV) {
            return false;
        }
        if (Double.doubleToLongBits(this.measurementTime) != Double.doubleToLongBits(other.measurementTime)) {
            return false;
        }
        if (Double.doubleToLongBits(this.maxDwnlnkLatency) != Double.doubleToLongBits(other.maxDwnlnkLatency)) {
            return false;
        }
        if (Double.doubleToLongBits(this.maxDataRate) != Double.doubleToLongBits(other.maxDataRate)) {
            return false;
        }
        if (Double.doubleToLongBits(this.minOccultationAlt) != Double.doubleToLongBits(other.minOccultationAlt)) {
            return false;
        }
        if (Double.doubleToLongBits(this.maxOccultationAlt) != Double.doubleToLongBits(other.maxOccultationAlt)) {
            return false;
        }
        if (!Objects.equals(this.solarConditions, other.solarConditions)) {
            return false;
        }
        if (!Objects.equals(this.sunglintPreference, other.sunglintPreference)) {
            return false;
        }
        if (!Arrays.equals(this.spectrum, other.spectrum)) {
            return false;
        }
        return true;
    }
}