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
public class GASpecification implements SearchSpecification{
    
    private final int populationSize;
    private final int numberOfFunctionalEvaluations;
    private final double crossoverProbability;
    private final double mutationProbability;
    private final int numberOfOperatorsToRemove;
    private final int numberOfOperatorsToAdd;
    private final double alpha;
    private final double beta;
    private final double pmin;
    
    /**
     * 
     * @param populationSize
     * @param numberOfFunctionalEvaluations
     * @param crossoverProbability
     * @param mutationProbability
     * @param numberOfOperatorsToRemove
     * @param numberOfOperatorsToAdd
     * @param alpha
     * @param beta
     * @param pmin 
     */
    public GASpecification(int populationSize,
            int numberOfFunctionalEvaluations,
            double crossoverProbability,
            double mutationProbability,
            int numberOfOperatorsToRemove,
            int numberOfOperatorsToAdd,
            double alpha, double beta, double pmin) {
        this.populationSize = populationSize;
        this.numberOfFunctionalEvaluations = numberOfFunctionalEvaluations;
        this.crossoverProbability = crossoverProbability;
        this.mutationProbability = mutationProbability;
        this.numberOfOperatorsToRemove = numberOfOperatorsToRemove;
        this.numberOfOperatorsToAdd = numberOfOperatorsToAdd;
        this.alpha = alpha;
        this.beta = beta;
        this.pmin = pmin;
        
        if((crossoverProbability<0 ||crossoverProbability>1) || 
                (mutationProbability<0 || mutationProbability>1) || 
                (alpha<0 || alpha>1) || (beta<0 || beta>1) || (pmin<0 || pmin>1)){
            throw new IllegalArgumentException("Probability must be in range [0,1]");
        }
    }

      /**
     * A space delimited string containing arguments defining the 
     * full factorial enumeration specification
     *
     * @param str a 2+ element string with elements in the following order:
     * @return string of search arguments
     */
    public static SearchSpecification create(String str) {
        return GASpecification.create(str.split("\\s+"));
    }
    
    /**
     * A 10+ element string array containing arguments defining the algorithm
     * specification
     *
     * @param args a 9+ element string with elements in the following order:
     * @return an instance of an observatory specification
     */
    public static GASpecification create(String[] args) {
        if (args.length < 9) {
            throw new IllegalArgumentException(String.format("Expected 9+ arguments. Found %d.", args.length));
        }
        int popSize = Integer.parseInt(args[0]);
        int numOfFunctionalEvaluations = Integer.parseInt(args[1]);
        double crossoverProb = Double.parseDouble(args[2]);
        double mutationProb = Double.parseDouble(args[3]);
        int numOpsToRemove = Integer.parseInt(args[4]);
        int numOpsToAdd = Integer.parseInt(args[5]);
        double alphaVal = Double.parseDouble(args[6]);
        double betaVal = Double.parseDouble(args[7]);
        double pminVal = Double.parseDouble(args[8]);

        StringBuilder sb = new StringBuilder();
        for (int i = 9;
                i < args.length;
                i++) {
            sb.append(args[i]);
        }

        return new GASpecification(popSize, numOfFunctionalEvaluations,
        crossoverProb, mutationProb, numOpsToRemove,
        numOpsToAdd, alphaVal, betaVal, pminVal);

    }

    public int getPopulationSize() {
        return populationSize;
    }

    public int getNFE() {
        return numberOfFunctionalEvaluations;
    }

    public double getCrossoverProbability() {
        return crossoverProbability;
    }

    public double getMutationProbability() {
        return mutationProbability;
    }

    public int getNumOfOpsToRemove() {
        return numberOfOperatorsToRemove;
    }

    public int getNumOfOpsToAdd() {
        return numberOfOperatorsToAdd;
    }

    public double getAlpha() {
        return alpha;
    }
    
    public double getBeta() {
        return beta;
    }
    
    public double getPmin() {
        return pmin;
    }
}
