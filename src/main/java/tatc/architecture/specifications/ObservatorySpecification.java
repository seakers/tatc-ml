/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.architecture.specifications;

import java.util.Objects;
import seak.orekit.util.Units;


public class ObservatorySpecification {

    private final double startMass;

    private final AngularScanSpecification alongAngularScan;

    private final AngularScanSpecification crossAngularScan;

    private final double angularScanRate;

    private final double startAngScnPhase;

    private final String commBandTypes;

    private final double commDataRate;

    /**
     *
     * @param startMass
     * @param alongAngularScan
     * @param crossAngularScan
     * @param angularScanRate
     * @param startAngScnPhase
     * @param commBandTypes
     * @param commDataRate
     */
    public ObservatorySpecification(double startMass,
            AngularScanSpecification alongAngularScan,
            AngularScanSpecification crossAngularScan,
            double angularScanRate, double startAngScnPhase,
            String commBandTypes, double commDataRate) {
        this.startMass = startMass;
        this.alongAngularScan = alongAngularScan;
        this.crossAngularScan = crossAngularScan;
        this.angularScanRate = angularScanRate;
        this.startAngScnPhase = startAngScnPhase;
        this.commBandTypes = commBandTypes;
        this.commDataRate = commDataRate;
    }

    /**
     * A space delimited string containing arguments defining the observatory
     * specification
     *
     * @param str a 10+ element string with elements in the following order:
     * observatory mass [kg], observatory volume [m^3], observatory power [W],
     * along track cone angle [deg] in min:max form, along track slew of center
     * [deg], crosstrack scan cone angle [deg] in min:max form, cross track slew
     * of center [deg], angular scan rate [deg] in min:max form, angular scan
     * start-phase [deg] in min:max form or the token "uniform", communication
     * band with multiple tokens {S,X,AmatureRadio,Ka,Ku,Laser}
     * @return an instance of an observatory specification
     */
    public static ObservatorySpecification create(String str) {
        return ObservatorySpecification.create(str.split("\\s+"));
    }

    /**
     * A 10+ element string array containing arguments defining the observatory
     * specification
     *
     * @param args a 10+ element string with elements in the following order:
     * observatory mass [kg], observatory volume [m^3], observatory power [W],
     * along track cone angle [deg] in min:max form, along track slew of center
     * [deg], crosstrack scan cone angle [deg] in min:max form, cross track slew
     * of center [deg], angular scan rate [deg] in min:max form, angular scan
     * start-phase [deg] in min:max form or the token "uniform", communication
     * band with multiple tokens {S,X,AmatureRadio,Ka,Ku,Laser}
     * @return an instance of an observatory specification
     */
    public static ObservatorySpecification create(String[] args) {
        if (args.length < 10) {
            throw new IllegalArgumentException(String.format("Expected 10+ arguments. Found %d.", args.length));
        }
        double mass = Double.parseDouble(args[0]);
        double volume = Units.deg2rad(Double.parseDouble(args[1]));
        double power = Units.km2m(Double.parseDouble(args[2]));
        String[] alongConeAngle = args[3].split(":");
        double minAlongConeAngle = Units.deg2rad(Double.parseDouble(alongConeAngle[0]));
        double maxAlongConeAngle = Units.deg2rad(Double.parseDouble(alongConeAngle[1]));
        String[] alongSlew = args[4].split(":");
        double minAlongSlew = Units.deg2rad(Double.parseDouble(alongSlew[0]));
        double maxAlongSlew = Units.deg2rad(Double.parseDouble(alongSlew[1]));
        AngularScanSpecification alongScan = new AngularScanSpecification(maxAlongConeAngle, maxAlongSlew);
        String[] crossConeAngle = args[5].split(":");
        double minCrossConeAngle = Units.deg2rad(Double.parseDouble(crossConeAngle[0]));
        double maxCrossConeAngle = Units.deg2rad(Double.parseDouble(crossConeAngle[1]));
        String[] crossSlew = args[6].split(":");
        double minCrossSlew = Units.deg2rad(Double.parseDouble(crossSlew[0]));
        double maxCrossSlew = Units.deg2rad(Double.parseDouble(crossSlew[1]));
        AngularScanSpecification crossScan = new AngularScanSpecification(maxCrossConeAngle, maxCrossSlew);
        double scanRate = Units.deg2rad(Double.parseDouble(args[7]));
        double scanStartPhase;
        if (args[8].equalsIgnoreCase("uniform")) {
            scanStartPhase = -1.; //TODO need to put something here for uniform
        } else {
            scanStartPhase = Units.deg2rad(Double.parseDouble(args[8]));
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 9;
                i < args.length;
                i++) {
            sb.append(args[i]);
        }

        return new ObservatorySpecification(mass, alongScan, crossScan,
                scanRate, scanStartPhase, sb.toString(), scanRate);

    }

    public double getStartMass() {
        return startMass;
    }

    public AngularScanSpecification getAlongAngularScan() {
        return alongAngularScan;
    }

    public AngularScanSpecification getCrossAngularScan() {
        return crossAngularScan;
    }

    public double getAngularScanRate() {
        return angularScanRate;
    }

    public double getStartAngScnPhase() {
        return startAngScnPhase;
    }

    public String getCommBandTypes() {
        return commBandTypes;
    }

    public double getCommDataRate() {
        return commDataRate;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + (int) (Double.doubleToLongBits(this.startMass) ^ (Double.doubleToLongBits(this.startMass) >>> 32));
        hash = 71 * hash + Objects.hashCode(this.alongAngularScan);
        hash = 71 * hash + Objects.hashCode(this.crossAngularScan);
        hash = 71 * hash + (int) (Double.doubleToLongBits(this.angularScanRate) ^ (Double.doubleToLongBits(this.angularScanRate) >>> 32));
        hash = 71 * hash + (int) (Double.doubleToLongBits(this.startAngScnPhase) ^ (Double.doubleToLongBits(this.startAngScnPhase) >>> 32));
        hash = 71 * hash + Objects.hashCode(this.commBandTypes);
        hash = 71 * hash + (int) (Double.doubleToLongBits(this.commDataRate) ^ (Double.doubleToLongBits(this.commDataRate) >>> 32));
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
        final ObservatorySpecification other = (ObservatorySpecification) obj;
        if (Double.doubleToLongBits(this.startMass) != Double.doubleToLongBits(other.startMass)) {
            return false;
        }
        if (Double.doubleToLongBits(this.angularScanRate) != Double.doubleToLongBits(other.angularScanRate)) {
            return false;
        }
        if (Double.doubleToLongBits(this.startAngScnPhase) != Double.doubleToLongBits(other.startAngScnPhase)) {
            return false;
        }
        if (Double.doubleToLongBits(this.commDataRate) != Double.doubleToLongBits(other.commDataRate)) {
            return false;
        }
        if (!Objects.equals(this.commBandTypes, other.commBandTypes)) {
            return false;
        }
        if (!Objects.equals(this.alongAngularScan, other.alongAngularScan)) {
            return false;
        }
        if (!Objects.equals(this.crossAngularScan, other.crossAngularScan)) {
            return false;
        }
        return true;
    }

}
