package tatc.tradespaceiterator;

import org.moeaframework.algorithm.EpsilonMOEA;
import org.moeaframework.core.*;
import org.moeaframework.core.operator.*;
import seakers.aos.aos.AOSMOEA;
import seakers.aos.creditassignment.setimprovement.SetImprovementDominance;
import seakers.aos.history.AOSHistoryIO;
import seakers.aos.operator.AOSVariation;
import seakers.aos.operator.AOSVariationSI;
import seakers.aos.operatorselectors.AdaptivePursuit;
import seakers.aos.operatorselectors.OperatorSelector;
import seakers.architecture.operators.IntegerUM;
import tatc.ResultIO;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;

public class StandardFormAOS extends StandardFormGA {
    public StandardFormAOS(StandardFormProblemProperties properties) {
        super(properties);
    }

    public void start(){
        long startTime = System.nanoTime();
//        Constellation c = JSONIO.readJSON(new File("/Users/Prachi/Downloads/CostRisk_CYGNSS.json"), Constellation.class);

        //set up variations
        //example of operators you might use
        ArrayList<Variation> operators = new ArrayList();
        operators.add(new CompoundVariation(new OnePointCrossover(1.0), new IntegerUM(0.5)));
        operators.add(new CompoundVariation(new UniformCrossover(1.0), new IntegerUM(0.1)));

        //create AOS
        //create operator selector
        OperatorSelector operatorSelector = new AdaptivePursuit(operators, 0.8, 0.8, 0.5);

        //create credit assignment
        SetImprovementDominance creditAssignment = new SetImprovementDominance(archive, 1, 0);

        //create AOS
        AOSVariation aosStrategy = new AOSVariationSI(operatorSelector, creditAssignment, populationSize);
        EpsilonMOEA emoea = new EpsilonMOEA(problem, population, archive,
                selection, aosStrategy, initialization, comparator);
        AOSMOEA aos = new AOSMOEA(emoea, aosStrategy, true);

        HashSet<Solution> allSolutions = new HashSet<>();

        System.out.println(String.format("Initializing population... Size = %d", populationSize));
        while (aos.getNumberOfEvaluations() < maxNFE) {
            aos.step();
            double currentTime = ((System.nanoTime() - startTime) / Math.pow(10, 9)) / 60.;
            System.out.println(
                    String.format("%d NFE out of %d NFE: Time elapsed = %10f min."
                                    + " Approximate time remaining %10f min.",
                            aos.getNumberOfEvaluations(), maxNFE, currentTime,
                            currentTime / emoea.getNumberOfEvaluations() * (maxNFE - aos.getNumberOfEvaluations())));
            for (Solution solution : aos.getPopulation()) {
                allSolutions.add(solution);
            }
        }
        ResultIO.savePopulation(new Population(allSolutions), Paths.get(System.getProperty("tatc.moea"), "population").toString());
        ResultIO.saveSearchResults(new Population(allSolutions), Paths.get(System.getProperty("tatc.moea"), "results").toString());
        AOSHistoryIO.saveCreditHistory(aos.getCreditHistory(), new File(System.getProperty("tatc.moea"), "res.credit"), ",");
        AOSHistoryIO.saveSelectionHistory(aos.getSelectionHistory(), new File(System.getProperty("tatc.moea"), "res.select"), ",");
    }
}
