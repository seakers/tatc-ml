package tatc.tradespaceiterator;

import org.moeaframework.algorithm.EpsilonMOEA;
import org.moeaframework.core.*;
import org.moeaframework.core.operator.CompoundVariation;
import org.moeaframework.core.operator.OnePointCrossover;
import org.moeaframework.core.operator.TwoPointCrossover;
import seakers.architecture.operators.IntegerUM;
import tatc.ResultIO;
import tatc.tradespaceiterator.search.VariableMutation;

import java.nio.file.Paths;
import java.util.HashSet;

public class StandardFormMOEA extends StandardFormGA {

    public StandardFormMOEA(StandardFormProblemProperties properties) {
        super(properties);
    }

    public void start(){
        long startTime = System.nanoTime();

        Variation crossover = new OnePointCrossover(1);
        Variation mutation = new VariableMutation(1);
        CompoundVariation operators = new CompoundVariation(crossover, mutation);

        //create MOEA
        EpsilonMOEA emoea = new EpsilonMOEA(problem, population, archive,
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
