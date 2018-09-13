/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.evaluation.reductionmetrics;

import org.orekit.bodies.GeodeticPoint;

/**
 *
 * @author Prachi
 */
public class GeodeticCoordinates extends GeodeticPoint {
    
    private static final long serialVersionUID = 6950360800635510872L;
    
    public GeodeticCoordinates(double latitude, double longitude, double altitude) {
        super(latitude, longitude, altitude);
    }

    public double getAlt() {
        return super.getAltitude(); 
    }

    public double getLon() {
        return super.getLongitude();
    }

    public double getLat() {
        return super.getLatitude(); 
    }  
}
