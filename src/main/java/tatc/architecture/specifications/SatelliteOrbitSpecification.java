/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.architecture.specifications;

import seakers.orekit.util.Units;


/**
 * The initial 6 orbital elements
 *
 * @author nhitomi
 */
public class SatelliteOrbitSpecification {

    /**
     * the initial eccentricity
     */
    private final double startEcc;

    /**
     * the initial inclination
     */
    private final double startIncl;

    /**
     * the initial semi-major axis
     */
    private final double startSMA;

    /**
     * the initial argument of perigee
     */
    private final double startPer;

    /**
     * the initial right ascension of the ascending node
     */
    private final double startRAAN;

    /**
     * the initial true anomaly
     */
    private final double startTrueA;

    /**
     *
     * @param startEcc the initial eccentricity
     * @param startIncl the initial inclination
     * @param startSMA the initial semi-major axis
     * @param startPer the initial argument of perigee
     * @param startRAAN the initial right ascension of the ascending node
     * @param startTrueA the initial true anomaly
     */
    public SatelliteOrbitSpecification(double startEcc, double startIncl, double startSMA, double startPer, double startRAAN, double startTrueA) {
        //check for valid parameters
        if (startEcc < 0. || startEcc > 1.) {
            throw new IllegalArgumentException(
                    String.format("Expected eccentricity to be in range [0,1]. "
                            + "Found %f", startEcc));
        }
        if (startIncl < 0. || startIncl > 360.) {
            throw new IllegalArgumentException(
                    String.format("Expected inclination [deg] to be in range [0,360]. "
                            + "Found %f", startIncl));
        }
        if (startPer < 0. || startPer > 360.) {
            throw new IllegalArgumentException(
                    String.format("Expected argument of perigee [deg] to be in range [0,360]. Found %f", startPer));
        }
        if (startRAAN < 0. || startRAAN > 360.) {
            throw new IllegalArgumentException(
                    String.format("Expected right ascension of the ascending node [deg] to be in range [0,360]. "
                            + "Found %f", startRAAN));
        }
        if (startTrueA < 0. || startTrueA > 360.) {
            throw new IllegalArgumentException(
                    String.format("Expected true anomaly [deg] to be in range [0,360]. "
                            + "Found %f", startTrueA));
        }

        this.startEcc = startEcc;
        this.startIncl = startIncl;
        this.startSMA = startSMA;
        this.startPer = startPer;
        this.startRAAN = startRAAN;
        this.startTrueA = startTrueA;
    }

    /**
     * Converts a space-delimited string that defines the orbital elements
     *
     * @param str a space-delimited string of orbital elements in the following
     * order: ecc incl[deg] sma[km] per[deg] raan[deg] trueA[deg]
     * @return an instance of an orbit specification
     */
    public static SatelliteOrbitSpecification create(String str) {
        return SatelliteOrbitSpecification.create(str.split("\\s+"));
    }

    /**
     * Converts a 6 element string array to the orbital elements
     *
     * @param args a 6-element string array of orbital elements in the following
     * order: ecc incl[deg] sma[km] per[deg] raan[deg] trueA[deg]
     * @return an instance of an orbit specification
     */
    public static SatelliteOrbitSpecification create(String[] args) {
        if (args.length != 6) {
            throw new IllegalArgumentException(String.format("Expected 6 arguments. Found %d.", args.length));
        }
        double ecc = Double.parseDouble(args[0]);
        double incl = Double.parseDouble(args[1]);
        double sma = Double.parseDouble(args[2]);
        double per = Double.parseDouble(args[3]);
        double raan = Double.parseDouble(args[4]);
        double trueA = Double.parseDouble(args[5]);

        return new SatelliteOrbitSpecification(ecc, incl, sma, per, raan, trueA);
    }

    /**
     * Gets the initial eccentricity.
     *
     * @return the initial eccentricity.
     */
    public double getStartEcc() {
        return startEcc;
    }

    /**
     * Gets the initial inclination [rad].
     *
     * @return the initial inclination [rad].
     */
    public double getStartIncl() {
        return Units.deg2rad(startIncl);
    }

    /**
     * Gets the initial semi-major axis [m].
     *
     * @return the initial semi-major axis [m].
     */
    public double getStartSMA() {
        return Units.km2m(startSMA);
    }

    /**
     * Gets the initial argument of perigee [rad].
     *
     * @return the initial argument of perigee [rad].
     */
    public double getStartPer() {
        return Units.deg2rad(startPer);
    }

    /**
     * gets the initial right ascension of the ascending node [rad].
     *
     * @return the initial right ascension of the ascending node [rad].
     */
    public double getStartRAAN() {
        return Units.deg2rad(startRAAN);
    }

    /**
     * gets the initial true anomaly [deg].
     *
     * @return the initial true anomaly [deg].
     */
    public double getStartTrueA() {
        return Units.deg2rad(startTrueA);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (int) (Double.doubleToLongBits(this.startEcc) ^ (Double.doubleToLongBits(this.startEcc) >>> 32));
        hash = 17 * hash + (int) (Double.doubleToLongBits(this.startIncl) ^ (Double.doubleToLongBits(this.startIncl) >>> 32));
        hash = 17 * hash + (int) (Double.doubleToLongBits(this.startSMA) ^ (Double.doubleToLongBits(this.startSMA) >>> 32));
        hash = 17 * hash + (int) (Double.doubleToLongBits(this.startPer) ^ (Double.doubleToLongBits(this.startPer) >>> 32));
        hash = 17 * hash + (int) (Double.doubleToLongBits(this.startRAAN) ^ (Double.doubleToLongBits(this.startRAAN) >>> 32));
        hash = 17 * hash + (int) (Double.doubleToLongBits(this.startTrueA) ^ (Double.doubleToLongBits(this.startTrueA) >>> 32));
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
        final SatelliteOrbitSpecification other = (SatelliteOrbitSpecification) obj;
        if (Double.doubleToLongBits(this.startEcc) != Double.doubleToLongBits(other.startEcc)) {
            return false;
        }
        if (Double.doubleToLongBits(this.startIncl) != Double.doubleToLongBits(other.startIncl)) {
            return false;
        }
        if (Double.doubleToLongBits(this.startSMA) != Double.doubleToLongBits(other.startSMA)) {
            return false;
        }
        if (Double.doubleToLongBits(this.startPer) != Double.doubleToLongBits(other.startPer)) {
            return false;
        }
        if (Double.doubleToLongBits(this.startRAAN) != Double.doubleToLongBits(other.startRAAN)) {
            return false;
        }
        if (Double.doubleToLongBits(this.startTrueA) != Double.doubleToLongBits(other.startTrueA)) {
            return false;
        }
        return true;
    }

}
