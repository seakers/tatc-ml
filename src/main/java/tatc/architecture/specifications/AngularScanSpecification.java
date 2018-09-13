/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.architecture.specifications;

/**
 * Specification for scan parameters
 * @author nhitomi
 */
public class AngularScanSpecification {
    
    private final double coneAngle;
    
    private final double slewOfCenter;

    public AngularScanSpecification(double coneAngle, double slewOfCenter) {
        this.coneAngle = coneAngle;
        this.slewOfCenter = slewOfCenter;
    }

    public double getConeAngle() {
        return coneAngle;
    }

    public double getSlewOfCenter() {
        return slewOfCenter;
    }    
}
