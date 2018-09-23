/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.architecture.specifications;

/**
 *
 * @author Prachi
 */
public class FullFactorialSpecification implements SearchSpecification{
    
    private final int altitudeSteps;
    private final int inclinationSteps;
    private final int satelliteSteps;
    
    
    public FullFactorialSpecification(int altitudeSteps, int inclinationSteps, int satelliteSteps) {
        this.altitudeSteps = altitudeSteps;
        this.inclinationSteps = inclinationSteps;
        this.satelliteSteps = satelliteSteps;
    }

    /**
     * A space delimited string containing arguments defining the 
     * full factorial enumeration specification
     *
     * @param str a 2+ element string with elements in the following order:
     * @return string of search arguments
     */
    public static SearchSpecification create(String str) {
        return FullFactorialSpecification.create(str.split("\\s+"));
    }

    /**
     * A 2+ element string array containing arguments defining the algorithm
     * specification
     *
     * @param args a 2+ element string with elements in the following order:
     * @return an instance of an observatory specification
     */
    public static FullFactorialSpecification create(String[] args) {
        if (args.length < 3) {
            throw new IllegalArgumentException(String.format("Expected 3+ arguments. Found %d.", args.length));
        }
        int altitudeSteps = Integer.parseInt(args[0]);
        int inclinationSteps = Integer.parseInt(args[1]);
        int satelliteSteps = Integer.parseInt(args[2]);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            sb.append(args[i]);
        }

        return new FullFactorialSpecification(altitudeSteps, inclinationSteps, satelliteSteps);
    }

    public int getAltitudeStepSize() {
        return altitudeSteps;
    }
    
    public int getInclinationStepSize() {
        return inclinationSteps;
    }
    
    public int getSatelliteStepSize() {
        return satelliteSteps;
    }
}
