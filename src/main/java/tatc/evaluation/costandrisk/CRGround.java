/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.evaluation.costandrisk;

import tatc.architecture.specifications.GroundStationSpecification;

/**
 * Ground station for cost and risk master input
 * @author Prachichi
 */
public class CRGround {
    
    private final double lat;
    
    private final double lon;
    
    private final int isDesignated;
    
    private final String commBandTypes;

    public CRGround(double latitude, double longitude, int isDesignated, String commBandTypes) {
        this.lat = latitude;
        this.lon = longitude;
        this.isDesignated = isDesignated;
        this.commBandTypes = commBandTypes;
    }
    
    public CRGround(GroundStationSpecification groundStationSpec) {
        this.lat = groundStationSpec.getLatitude();
        this.lon = groundStationSpec.getLongitude();
        this.isDesignated = groundStationSpec.getDesignated();
        StringBuilder sb = new StringBuilder();
        this.commBandTypes = String.join(" ", groundStationSpec.getCommBandType());
    }
}
