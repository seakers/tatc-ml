/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.architecture;

import java.util.HashSet;
import org.hipparchus.util.FastMath;
import seakers.conmop.util.Bounds;
import tatc.architecture.variable.MonolithVariable;

/**
 *
 * @author Prachi
 */
public class TATCWalker implements Architecture{

    private final HashSet<MonolithVariable> satellites;

    /**
     * Creates a walker delta-pattern constellation in the specified Walker
     * configuration at the specified semi-major axis. The satellites contained
     * in this constellation are not assigned any instrumentation nor are any
     * steering/attitude laws. Can specify where the reference raan and true
     * anomaly to orient the walker configuration
     */
    public TATCWalker(double semimajoraxis, double inc, int t, int p, int f) {

        //checks for valid parameters
        if (t < 0 || p < 0) {
            throw new IllegalArgumentException(String.format("Expected t>0, p>0."
                    + " Found f=%d and p=%d", t, p));
        }
        if ((t % p) != 0) {
            throw new IllegalArgumentException(
                    String.format("Incompatible values for total number of "
                            + "satellites <t=%d> and number of planes <p=%d>. "
                            + "t must be divisible by p.", t, p));
        }
        if (f < 0 && f > p - 1) {
            throw new IllegalArgumentException(
                    String.format("Expected 0 <= f <= p-1. "
                            + "Found f = %d and p = %d.", f, p));
        }

        //Uses Walker delta pa
        final int s = t / p; //number of satellites per plane
        final double pu = 2 * FastMath.PI / t; //pattern unit
        final double delAnom = pu * p; //in plane spacing between satellites
        final double delRaan = pu * s; //node spacing
        final double phasing = pu * f;
        final double refAnom = 0;
        final double refRaan = 0;

        this.satellites = new HashSet<>(t);
        for (int planeNum = 0; planeNum < p; planeNum++) {
            for (int satNum = 0; satNum < s; satNum++) {
                MonolithVariable mono = new MonolithVariable(
                        new Bounds(semimajoraxis,semimajoraxis), 
                        new Bounds(0.0,0.0), new Bounds(inc,inc));
                
                mono.setSma(semimajoraxis);
                mono.setEcc(0.0);
                mono.setInc(inc);
                mono.setRaan(refRaan + planeNum * delRaan);
                mono.setArgPer(0.0);
                double anom = (refAnom + satNum * delAnom + phasing * planeNum) % (2. * FastMath.PI);
                //since eccentricity = 0, doesn't matter if using true or mean anomaly
                mono.setTrueAnomaly(anom);
                this.satellites.add(mono);
            }
        }
    }

    public HashSet<MonolithVariable> getSatellites() {
        return satellites;
    }
}
