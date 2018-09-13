/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.architecture.specifications;

import java.util.Arrays;

/**
 * The specifications for a ground station
 *
 * @author nhitomi
 */
public class GroundStationSpecification {

    /**
     * The latitude [deg] of the ground station
     */
    private final double latitude;

    /**
     * The longitude [deg] of the ground station 
     */
    private final double longitude;
    
    /**
     * The altitude [m] of the ground station
     */
    private final double altitude;

    /**
     * Flag for if the ground station is designated or not
     */
    private final int designated;

    /**
     * Communication band type designation
     */
    private final String[] commBandType;

    /**
     * 
     * @param latitude The latitude [deg] of the ground station
     * @param longitude The longitude [deg] of the ground station 
     * @param altitude The altitude [m] of the ground station
     * @param designated Flag for if the ground station is designated or not
     * @param commBandType Communication band type designation
     */
    public GroundStationSpecification(double latitude, double longitude, double altitude, int designated, String[] commBandType) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.designated = designated;
        this.commBandType = commBandType;
    }
    
    /**
     * Creates a ground station from a space delimited string that contains the
     * ground station specifications in the following order:
     * latitude [deg], longitude [deg], designated {0,1}, commBandType
     *
     * @param str space delimited string that contains the
     * ground station specifications
     * @return
     */
    public static GroundStationSpecification create(String str) {
        return GroundStationSpecification.create(str.split("\\s"));
    }
    
     /**
     * Creates a ground station from an array of strings that contains the
     * ground station specifications in the following order:
     * latitude [deg], longitude [deg], designated {0,1}, commBandType
     *
     * @param args an array of strings that contains the
     * ground station specifications 
     * @return
     */
    public static GroundStationSpecification create(String[] args) {
        double latitude = Double.parseDouble(args[0]);
        double longitude = Double.parseDouble(args[1]);
        double altitude = Double.parseDouble(args[2]);
        int designated = Integer.parseInt(args[3]);
        
        String[] commBandType = new String[args.length-4];
        System.arraycopy(args, 4, commBandType, 0, args.length-4);
        return new GroundStationSpecification(latitude, longitude, altitude, designated, commBandType);
    }

    /**
     * Gets the latitude of the ground station [deg]
     * @return the latitude of the ground station [deg]
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Gets the latitude of the ground station [deg]
     * @return the latitude of the ground station [deg]
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Gets the altitude of the ground station [m]
     * @return the altitude of the ground station [m]
     */
    public double getAltitude() {
        return altitude;
    }

    /**
     * Gets the binary flag for if this ground station is designated
     * @return the binary flag for if this ground station is designated
     */
    public int getDesignated() {
        return designated;
    }

    /**
     * Gets the communication band types
     * @return the communication band types
     */
    public String[] getCommBandType() {
        return commBandType;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + (int) (Double.doubleToLongBits(this.latitude) ^ (Double.doubleToLongBits(this.latitude) >>> 32));
        hash = 23 * hash + (int) (Double.doubleToLongBits(this.longitude) ^ (Double.doubleToLongBits(this.longitude) >>> 32));
        hash = 23 * hash + (int) (Double.doubleToLongBits(this.altitude) ^ (Double.doubleToLongBits(this.altitude) >>> 32));
        hash = 23 * hash + this.designated;
        hash = 23 * hash + Arrays.deepHashCode(this.commBandType);
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
        final GroundStationSpecification other = (GroundStationSpecification) obj;
        if (Double.doubleToLongBits(this.latitude) != Double.doubleToLongBits(other.latitude)) {
            return false;
        }
        if (Double.doubleToLongBits(this.longitude) != Double.doubleToLongBits(other.longitude)) {
            return false;
        }
        if (Double.doubleToLongBits(this.altitude) != Double.doubleToLongBits(other.altitude)) {
            return false;
        }
        if (this.designated != other.designated) {
            return false;
        }
        if (!Arrays.deepEquals(this.commBandType, other.commBandType)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String delimiter = " ";
        sb.append(latitude).append(delimiter);
        sb.append(longitude).append(delimiter);
        sb.append(altitude).append(delimiter);
        sb.append(designated).append(delimiter);
        sb.append(String.join(delimiter, commBandType));
        return sb.toString();
    }

}
