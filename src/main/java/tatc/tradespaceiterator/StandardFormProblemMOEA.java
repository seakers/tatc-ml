package tatc.tradespaceiterator;

import org.moeaframework.algorithm.EpsilonMOEA;
import org.moeaframework.core.*;
import org.moeaframework.core.comparator.DominanceComparator;
import org.moeaframework.core.comparator.ParetoDominanceComparator;
import org.moeaframework.core.operator.CompoundVariation;
import org.moeaframework.core.operator.RandomInitialization;
import org.moeaframework.core.operator.TournamentSelection;
import org.moeaframework.core.operator.TwoPointCrossover;
import seakers.architecture.operators.IntegerUM;
import tatc.ResultIO;

import java.nio.file.Paths;
import java.util.HashSet;

public class StandardFormProblemMOEA extends StandardFormProblemGA {

    public StandardFormProblemMOEA(StandardFormProblemProperties properties) {
        super(properties);
    }

    public void start(){
        long startTime = System.nanoTime();

        Variation crossover = new TwoPointCrossover(1);
        Variation mutation = new IntegerUM(0.1);
        CompoundVariation operators = new CompoundVariation(crossover, mutation);

        //create MOEA
        EpsilonMOEA emoea = new EpsilonMOEA(this, population, archive,
                selection, operators, initialization, comparator);

        HashSet<Solution> allSolutions = new HashSet<>();

        System.out.println(String.format("Initializing population... Size = %d", populationSize));

        while (emoea.getNumberOfEvaluations() < maxNFE) {
            emoea.step();
            double currentTime = ((System.nanoTime() - startTime) / Math.pow(10, 9)) / 60.;
            System.out.println(
                    String.format("%d NFE out of %d NFE: Time elapsed = %10f min."
                                    + " Approximate time remaining %10f min.",
                            emoea.getNumberOfEvaluations(), maxNFE, currentTime,
                            currentTime / emoea.getNumberOfEvaluations() * (maxNFE - emoea.getNumberOfEvaluations())));
            for (Solution solution : emoea.getPopulation()) {
                allSolutions.add(solution);
            }
        }
        ResultIO.savePopulation(new Population(allSolutions), Paths.get(System.getProperty("tatc.moea"), "population").toString());
        ResultIO.saveSearchResults(new Population(allSolutions), Paths.get(System.getProperty("tatc.moea"), "results").toString());
    }
}
