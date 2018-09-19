/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.architecture.variable;

import org.moeaframework.core.PRNG;
import org.moeaframework.core.Variable;
import seakers.conMOP.util.Bounds;
import seakers.conMOP.variable.SatelliteVariable;

/**
 * Variable for a monolithic satellite
 *
 * @author nhitomi
 */
public class MonolithVariable extends SatelliteVariable {

    private static final long serialVersionUID = -4167522894255602666L;

    /**
     * The bounds on the observatory ID
     */
    private final Bounds<Integer> obsIDBound;

    /**
     * the assigned observatory id
     */
    private int obsID;

    /**
     * The bounds on the instrument ID
     */
    private final Bounds<Integer> instIDBound;

    /**
     * The assigned instrument id
     */
    private int instID;

    /**
     * Creates a new variable for a satellite. Assumes that only one observatory
     * and one instrument is available to select from. Assumes all valid values
     * are allowed for argument of perigee, right ascension of the ascending
     * node, and true anomaly.
     *
     * @param smaBound The bounds on the semi major axis [m]
     * @param eccBound The bounds on the eccentricity
     * @param incBound The bounds on the inclination [rad]
     */
    public MonolithVariable(Bounds<Double> smaBound, Bounds<Double> eccBound,
            Bounds<Double> incBound) {
        this(new Bounds<>(0, 0), new Bounds<>(0, 0), smaBound, eccBound, incBound);
    }

    /**
     * Creates a new variable for a satellite. Assumes there are more than one
     * instrument available to select from. Assumes all valid values are allowed
     * for argument of perigee, right ascension of the ascending node, and true
     * anomaly.
     *
     * @param obsIDBound The bounds on the observatory id
     * @param instIDBound The bounds on the instrument id
     * @param smaBound The bounds on the semi major axis [m]
     * @param eccBound The bounds on the eccentricity
     * @param incBound The bounds on the inclination [rad]
     */
    public MonolithVariable(Bounds<Integer> obsIDBound, Bounds<Integer> instIDBound,
            Bounds<Double> smaBound, Bounds<Double> eccBound,
            Bounds<Double> incBound) {
        this(obsIDBound, instIDBound, smaBound, eccBound, incBound,
                new Bounds<>(0., 2. * Math.PI), new Bounds<>(0., 2. * Math.PI),
                new Bounds<>(0., 2. * Math.PI));
    }

    /**
     * Creates a new variable for a satellite. Assumes that only one observatory
     * and one instrument is available to select from.
     *
     * @param smaBound The bounds on the semi major axis [m]
     * @param eccBound The bounds on the eccentricity
     * @param incBound The bounds on the inclination [rad]
     * @param argPerBound The bounds on the argument of perigee [rad]
     * @param raanBound The bounds on the right ascension of the ascending node
     * [rad]
     * @param anomBound The bounds on the true anomaly [rad]
     */
    public MonolithVariable(Bounds<Double> smaBound, Bounds<Double> eccBound,
            Bounds<Double> incBound, Bounds<Double> argPerBound,
            Bounds<Double> raanBound, Bounds<Double> anomBound) {
        this(new Bounds<>(0, 0), new Bounds<>(0, 0),
                smaBound, eccBound, incBound, argPerBound, raanBound, anomBound);
    }

    /**
     * Creates a new variable for a satellite. Assumes there are more than one
     * instrument available to select from.
     *
     * @param obsIDBound The bounds on the observatory id
     * @param instIDBound The bounds on the instrument id
     * @param smaBound The bounds on the semi major axis [m]
     * @param eccBound The bounds on the eccentricity
     * @param incBound The bounds on the inclination [rad]
     * @param argPerBound The bounds on the argument of perigee [rad]
     * @param raanBound The bounds on the right ascension of the ascending node
     * [rad]
     * @param anomBound The bounds on the true anomaly [rad]
     */
    public MonolithVariable(Bounds<Integer> obsIDBound, Bounds<Integer> instIDBound,
            Bounds<Double> smaBound, Bounds<Double> eccBound,
            Bounds<Double> incBound, Bounds<Double> argPerBound,
            Bounds<Double> raanBound, Bounds<Double> anomBound) {
        super(smaBound, eccBound, incBound, argPerBound, raanBound, anomBound);
        this.obsIDBound = obsIDBound;
        this.instIDBound = instIDBound;
    }

    /**
     * Copies the fields of the given satellite variable and creates a new
     * instance of a satellite.
     *
     * @param var the satellite variable to copy
     */
    protected MonolithVariable(MonolithVariable var) {
        super(var);
        this.obsIDBound = var.obsIDBound;
        this.instIDBound = var.instIDBound;
    }

    /**
     * Gets the id of the instrument assigned to the satellite
     *
     * @return the id of the instrument assigned to the satellite
     */
    public int getInstrumentID() {
        return instID;
    }

    /**
     * sets the id of the instrument to assign to the satellite
     *
     * @param instID the id of the instrument to assign to the satellite
     */
    public void setInstrumentID(int instID) {
        if (!instIDBound.inBounds(instID)) {
            throw new IllegalArgumentException(
                    String.format("Expected instrument id to be in bounds [%d,%d]."
                            + " Found %d.", instIDBound.getLowerBound(), instIDBound.getUpperBound(), instID));
        }
        this.instID = instID;
    }

    /**
     * Gets the id of the observatory assigned to the satellite
     *
     * @return the id of the observatory assigned to the satellite
     */
    public int getObservatoryID() {
        return obsID;
    }

    /**
     * sets the id of the observatory to assign to the satellite
     *
     * @param obsID the id of the observatory to assign to the satellite
     */
    public void setObservatoryID(int obsID) {
        if (!obsIDBound.inBounds(obsID)) {
            throw new IllegalArgumentException(
                    String.format("Expected instrument id to be in bounds [%d,%d]."
                            + " Found %d.", obsIDBound.getLowerBound(), obsIDBound.getUpperBound(), obsID));
        }
        this.obsID = obsID;
    }

    @Override
    public Variable copy() {
        MonolithVariable var = new MonolithVariable(this);
        var.setObservatoryID(this.getObservatoryID());
        var.setInstrumentID(this.getInstrumentID());
        return var;
    }

    @Override
    public void randomize() {
        super.randomize();
        this.setInstrumentID(PRNG.nextInt(instIDBound.getLowerBound(), instIDBound.getUpperBound()));
        this.setObservatoryID(PRNG.nextInt(obsIDBound.getLowerBound(), obsIDBound.getUpperBound()));
    }
}
