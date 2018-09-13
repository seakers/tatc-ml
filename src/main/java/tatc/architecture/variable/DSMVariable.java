/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.architecture.variable;

import java.util.ArrayList;
import org.moeaframework.core.Variable;
import seak.conmop.util.Bounds;
import seak.conmop.variable.ConstellationVariable;
import seak.conmop.variable.SatelliteVariable;

/**
 *
 * @author nhitomi
 */
public class DSMVariable extends ConstellationVariable {

    private static final long serialVersionUID = -8491639356497370172L;

    /**
     * The bounds on the observatory specification id
     */
    private final Bounds<Integer> obsIDBound;

    /**
     * The bounds on the instrument specification id
     */
    private final Bounds<Integer> instIDBound;

    /**
     * Constructs a new constellation variable
     *
     * @param satelliteBound The bounds on the number of satellites allowed in
     * this constellation
     * @param obsId The bounds on the observatory specification id
     * @param instId The bounds on the instrument specification id
     * @param smaBound The bounds on the semi major axis [m]
     * @param eccBound The bounds on the eccentricity
     * @param incBound The bounds on the inclination [rad]
     * @param argPerBound The bounds on the argument of perigee [rad]
     * @param raanBound The bounds on the right ascension of the ascending node
     * [rad]
     * @param anomBound The bounds on the true anomaly [rad]
     */
    public DSMVariable(
            Bounds<Integer> satelliteBound, Bounds<Integer> obsId, Bounds<Integer> instId,
            Bounds<Double> smaBound, Bounds<Double> eccBound, Bounds<Double> incBound,
            Bounds<Double> argPerBound, Bounds<Double> raanBound, Bounds<Double> anomBound) {

        super(satelliteBound, smaBound, eccBound, incBound, argPerBound, raanBound, anomBound);
        this.obsIDBound = obsId;
        this.instIDBound = instId;
    }

    /**
     * Copies the fields of the given constellation variable and creates a new
     * instance of a constellation.
     *
     * @param var the constellation variable to copy
     */
    protected DSMVariable(DSMVariable var) {
        super(var);
        this.obsIDBound = var.obsIDBound;
        this.instIDBound = var.instIDBound;
    }

    @Override
    public Variable copy() {
        return new DSMVariable(this);
    }

    @Override
    public void randomize() {
        super.randomize();
        ArrayList<SatelliteVariable> satellites = new ArrayList<>();
        for (SatelliteVariable tmp : getSatelliteVariables()) {
            MonolithVariable var = new MonolithVariable(
                    obsIDBound, instIDBound,
                    tmp.getSmaBound(), tmp.getEccBound(), tmp.getIncBound(),
                    tmp.getArgPerBound(), tmp.getRaanBound(), tmp.getAnomBound());
            var.randomize();
            satellites.add(var);
        }
        this.setSatelliteVariables(satellites);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (SatelliteVariable var : getSatelliteVariables()) {
            sb.append(var.getSma());
            sb.append(" ");
            sb.append(var.getEcc());
            sb.append(" ");
            sb.append(var.getInc());
            sb.append(" ");
            sb.append(var.getRaan());
            sb.append(" ");
            sb.append(var.getArgPer());
            sb.append(" ");
            sb.append(var.getTrueAnomaly());
            sb.append(" ");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

}
