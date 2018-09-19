/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.evaluation.reductionmetrics;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.orekit.errors.OrekitException;
import org.orekit.frames.FramesFactory;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.PositionAngle;
import org.orekit.utils.Constants;
import seakers.orekit.util.Units;
import tatc.architecture.specifications.SatelliteOrbitSpecification;
import tatc.architecture.variable.MonolithVariable;

/**
 * The Keplerian elements of a simple orbital trajectory description. Assumes
 * ECI coordinates (J2000) and WGS84
 * 
 * @author Prachi
 */
public class KeplerianElements extends KeplerianOrbit {

    /**
     * The Keplerian elements of a simple orbital trajectory description.
     *
     * @param date startDate.
     * @param ecc eccentricity
     * @param incl inclination [rad]
     * @param sma semi-major axis [m]
     * @param per argument of perigee [rad]
     * @param raan right ascension of the ascending node [rad]
     * @param trueA true anomaly [rad]
     * @throws IllegalArgumentException
     */
    public KeplerianElements(AbsoluteDate date, double ecc, double incl, double sma, double per, double raan, double trueA) throws IllegalArgumentException {
        super(sma, ecc, incl, per, raan, trueA, PositionAngle.TRUE, FramesFactory.getEME2000(), date, Constants.WGS84_EARTH_MU);
        checkParameters();
    }

    /**
     * The Keplerian orbit is constructed from the orbit specification
     *
     * @param date startDate
     * @param orbitSpec the orbit specification
     */
    public KeplerianElements(AbsoluteDate date, SatelliteOrbitSpecification orbitSpec) {
        this(date,
                orbitSpec.getStartEcc(),
                orbitSpec.getStartIncl(),
                orbitSpec.getStartSMA(),
                orbitSpec.getStartPer(),
                orbitSpec.getStartRAAN(),
                orbitSpec.getStartTrueA());
    }

    /**
     * The Keplerian orbit is constructed from the orbit specification
     *
     * @param date startDate
     * @param var the satellite variable containing information about the
     * orbital elements
     */
    public KeplerianElements(AbsoluteDate date, MonolithVariable var) {
        this(date, var.getEcc(), var.getInc(), var.getSma(),
                var.getArgPer(), var.getRaan(), var.getTrueAnomaly());
    }

    /**
     * Checks the parameters of the given orbital elements.
     *
     * @return true if the orbital elements are all within reasonable bounds
     */
    private boolean checkParameters() {
        if (this.getE() < 0.0 || this.getE() > 1.0) {
            throw new IllegalArgumentException(
                    String.format("Expected ecccentricity to be between 0.0 and 1.0. Found %f.", this.getE()));
        }
        if (this.getA() < Constants.WGS84_EARTH_EQUATORIAL_RADIUS + 200000) {
            throw new IllegalArgumentException(
                    String.format("Expected semi-major axis to be greater than radius of earth + 200km [%f]. "
                            + "Found %f.", Constants.WGS84_EARTH_EQUATORIAL_RADIUS + 200000, this.getA()));
        }
        if (this.getI() < 0.0 || this.getI() > 2. * Math.PI) {
            throw new IllegalArgumentException(
                    String.format("Expected inclination to be between 0.0 deg and 180. deg. "
                            + "Found %f", Units.rad2deg(this.getI())));
        }
        if (this.getPerigeeArgument()< 0.0 || this.getPerigeeArgument()> 2. * Math.PI) {
            throw new IllegalArgumentException(
                    String.format("Expected true anomoaly to be between 0.0 deg and 180. deg. "
                            + "Found %f", Units.rad2deg(this.getI())));
        }
        if (this.getRightAscensionOfAscendingNode()< 0.0 || this.getRightAscensionOfAscendingNode()> 2. * Math.PI) {
            throw new IllegalArgumentException(
                    String.format("Expected true anomoaly to be between 0.0 deg and 180. deg. "
                            + "Found %f", Units.rad2deg(this.getI())));
        }
        if (this.getTrueAnomaly()< 0.0 || this.getTrueAnomaly()> 2. * Math.PI) {
            throw new IllegalArgumentException(
                    String.format("Expected true anomoaly to be between 0.0 deg and 180. deg. "
                            + "Found %f", Units.rad2deg(this.getI())));
        }
        return true;
    }

    @Override
    public AbsoluteDate getDate() {
        try {
            return AbsoluteDate.cast(super.getDate());
        } catch (OrekitException ex) {
            Logger.getLogger(KeplerianElements.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Gets the eccentricity which should be between 0.0 and 1.0
     *
     * @return the eccentricity
     */
    public double getEcc() {
        return super.getE();
    }

    /**
     * Gets the argument of perigee [rad]
     *
     * @return the argument of perigee [rad]
     */
    public double getPer() {
        return super.getPerigeeArgument();
    }

    /**
     * Gets the inclination [rad]
     *
     * @return the inclination [rad]
     */
    public double getIncl() {
        return super.getI();
    }

    /**
     * Gets the right ascension of the ascending node [rad]
     *
     * @return the right ascension of the ascending node [rad]
     */
    public double getRAAN() {
        return super.getRightAscensionOfAscendingNode();
    }

    /**
     * Gets the semi-major axis [m]
     *
     * @return the semi-major axis [m]
     */
    public double getSMA() {
        return super.getA();
    }

    /**
     * Gets the true anomaly [rad]
     *
     * @return the true anomaly [rad]
     */
    public double getTrueA() {
        return super.getTrueAnomaly();
    }
}
