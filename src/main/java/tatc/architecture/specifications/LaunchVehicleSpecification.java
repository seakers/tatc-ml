/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.architecture.specifications;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * launch vehicle information and launch parameters
 *
 * @author nhitomi
 */
public class LaunchVehicleSpecification {

    /**
     * name of the launch vehicle
     */
    private final String name;
    
    /**
     * The mass[kg] of the launch vehicle 
     */
    private final double mass;
    
    /**
     * The volume[m^3] of the launch vehicle 
     */
    private final double volume;

    /**
     * expected success rate for intended vehicle/site/etc [0 <= p <= 1]
     */
    private final double reliability;
    
    /**
     * The cost [$] of the launch vehicle
     */
    private final double cost;

    public LaunchVehicleSpecification(String name, double mass, double volume, double reliability, double cost) {
        this.name = name;
        this.mass = mass;
        this.volume = volume;
        this.reliability = reliability;
        if(reliability <0 ||reliability>1){
            throw new IllegalArgumentException("Reliability must be in range [0,1]");
        }
        this.cost = cost;
    }
    
    /**
     * A space delimited string containing arguments defining the launch vehicle
     * specifications
     *
     * @param str a space delimited string with elements in the following order:
     * name, mass [kg], volume [m^3], reliability, cost[$]
     * @return an instance of an launch vehicle specification
     */
    public static LaunchVehicleSpecification create(String str) {
        return LaunchVehicleSpecification.create(str.split("\\s+"));
    }

    /**
     * A 16 element string array containing arguments defining the instrument
     * specification
     *
     * @param args a 16 element string array with elements in the following order:
     * name, mass [kg], volume [m^3], reliability, cost[$]
     * @return an instance of an instrument specification
     */
    public static LaunchVehicleSpecification create(String[] args) {
        if(args.length > 9){
            throw new IllegalArgumentException(String.format("Expected 9 arguments. Found %d.", args.length));
        }
        Pattern lvNamePattern = Pattern.compile("\"(?<name>.*)\"");
        Matcher matcher = lvNamePattern.matcher(args[0]);
        matcher.find();
        String name = matcher.group("name");
        double mass = Double.parseDouble(args[1]);
        double volume = Double.parseDouble(args[2]);
        double reliability = Double.parseDouble(args[6]);
        double cost = Double.parseDouble(args[8]);
        
        return new LaunchVehicleSpecification(name, mass, volume, reliability, cost);
    }

    public String getName() {
        return name;
    }

    public double getReliability() {
        return reliability;
    }

    /**
     * Gets the mass[kg] of launch vehicle
     * @return 
     */
    public double getMass() {
        return mass;
    }

    /**
     * Gets the volume[m^3] of the launch vehicle 
     * @return 
     */
    public double getVolume() {
        return volume;
    }

    /**
     * Gets the cost[$] of the launch vehicle
     * @return 
     */
    public double getCost() {
        return cost;
    }
    

}
