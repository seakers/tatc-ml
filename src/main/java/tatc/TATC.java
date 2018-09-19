/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc;

/**
 * A pre-Phase A constellation mission analysis tool
 * @author Prachichi
 */

import seakers.aos.aos.AOSMOEA;
import seakers.aos.creditassignment.setimprovement.SetImprovementDominance;
import seakers.aos.history.AOSHistoryIO;
import seakers.aos.operator.AOSVariation;
import seakers.aos.operator.AOSVariationSI;
import seakers.aos.operatorselectors.AdaptivePursuit;
import seakers.aos.operatorselectors.OperatorSelector;
import seakers.aos.operatorselectors.replacement.EpochTrigger;
import seakers.aos.operatorselectors.replacement.InitialTrigger;
import seakers.aos.operatorselectors.replacement.OperatorReplacementStrategy;
import seakers.aos.operatorselectors.replacement.RemoveNLowest;
import seakers.aos.operatorselectors.replacement.ReplacementTrigger;
import seakers.aos.operatorselectors.replacement.CompoundTrigger;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorService;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import mining.label.AbstractPopulationLabeler;
import mining.label.NondominatedSortingLabeler;
import org.moeaframework.algorithm.EpsilonMOEA;
import org.moeaframework.core.Algorithm;
import org.moeaframework.core.EpsilonBoxDominanceArchive;
import org.moeaframework.core.Initialization;
import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;
import org.moeaframework.core.comparator.DominanceComparator;
import org.moeaframework.core.comparator.ParetoDominanceComparator;
import org.moeaframework.core.operator.CompoundVariation;
import org.moeaframework.core.operator.OnePointCrossover;
import org.moeaframework.core.operator.RandomInitialization;
import org.moeaframework.core.operator.TournamentSelection;
import org.moeaframework.core.operator.TwoPointCrossover;
import org.moeaframework.core.operator.UniformCrossover;
import org.moeaframework.core.operator.binary.BitFlip;
import org.moeaframework.util.TypedProperties;
import seakers.architecture.operators.IntegerUM;
import tatc.tradespaceiterator.TradespaceSearchRequest;
import tatc.util.JSONIO;
import tatc.tradespaceiterator.StandardFormProblemFullFact;
import tatc.tradespaceiterator.StandardFormProblemGA;
import tatc.tradespaceiterator.search.KDOSearch;


public class TATC {

    /**
     * pool of resources
     */
    private static ExecutorService pool;

    /**
     * Executor completion services helps remove completed tasks
     */
    private static CompletionService<Algorithm> ecs;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //setup logger
        Level level = Level.FINEST;
        Logger.getGlobal().setLevel(level);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(level);
        Logger.getGlobal().addHandler(handler);

       
        
        File mainPath = new File(System.getProperty("user.dir"), "problems");
        System.setProperty("tatc.root", mainPath.getAbsolutePath());
        System.setProperty("tatc.cr", new File(mainPath.getAbsolutePath(), "CaR").getAbsolutePath());
        System.setProperty("tatc.rm", new File(mainPath.getAbsolutePath(), "RM").getAbsolutePath());
        System.setProperty("tatc.groundstation", new File(mainPath.getAbsolutePath(), "GroundStations").getAbsolutePath());
        System.setProperty("tatc.results", new File(mainPath.getAbsolutePath(), "results").getAbsolutePath());
        System.setProperty("tatc.dsms", new File(System.getProperty("tatc.results"), "DSMs").getAbsolutePath());
        System.setProperty("tatc.monos", new File(System.getProperty("tatc.results"), "Mono").getAbsolutePath());
        System.setProperty("tatc.moea", new File(mainPath.getParent(), "results").getAbsolutePath());

        Properties properties = new Properties();
        System.setProperty("tatc.numThreads", "1");

        TradespaceSearchRequest tsr = JSONIO.readJSON(
                new File(mainPath, "TradespaceSearchRequest.json"),
                TradespaceSearchRequest.class);

        //setting up the parameters for the search
        
        //adding options for enumerating trade space
        // 0 - full factorial
        // 1 - MOEA without AOS
        // 2 - MOEA with offline AOS
        // 3 - MOEA with online AOS
        int options = 0;

        if (options == 0) {

            //set up the system parameters
            long startTime = System.nanoTime();
            
            StandardFormProblemFullFact problem = new StandardFormProblemFullFact(tsr, properties);
            problem.evaluate();
            problem.shutdown();
            
            long endTime = System.nanoTime();
            
            Logger.getGlobal().finest(String.format("Took %.4f sec", (endTime - startTime) / Math.pow(10, 9)));

        } else if (options == 1){
            
            StandardFormProblemGA problem = new StandardFormProblemGA(tsr, properties);
            
            //set up the search parameters
            long startTime = System.nanoTime();
            int maxNFE = 100;
            int populationSize = 80;
            Initialization initialization = new RandomInitialization(problem,
                    populationSize);
            Population population = new Population();
            DominanceComparator comparator = new ParetoDominanceComparator();
            EpsilonBoxDominanceArchive archive = new EpsilonBoxDominanceArchive(new double[]{60, 10});
            final TournamentSelection selection = new TournamentSelection(2, comparator);

            //setup the operators
            Variation crossover = new TwoPointCrossover(1);
            Variation mutation = new IntegerUM(0.1);
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
            problem.shutdown();
            System.out.println(emoea.getArchive().size());

            long endTime = System.nanoTime();
            Logger.getGlobal().finest(String.format("Took %.4f sec", (endTime - startTime) / Math.pow(10, 9)));

            ResultIO.savePopulation(new Population(allSolutions), Paths.get(System.getProperty("tatc.moea"), "population").toString());
            ResultIO.saveSearchResults(new Population(allSolutions), Paths.get(System.getProperty("tatc.moea"), "results").toString());
            
        } else if (options == 2){

            StandardFormProblemGA problem = new StandardFormProblemGA(tsr, properties);
            
            //set up the search parameters
            long startTime = System.nanoTime();
            int maxNFE = 100;
            int populationSize = 80;
            Initialization initialization = new RandomInitialization(problem,
                    populationSize);
            Population population = new Population();
            DominanceComparator comparator = new ParetoDominanceComparator();
            EpsilonBoxDominanceArchive archive = new EpsilonBoxDominanceArchive(new double[]{60, 10});
            final TournamentSelection selection = new TournamentSelection(2, comparator);

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
            problem.shutdown();
            System.out.println(aos.getArchive().size());

            long endTime = System.nanoTime();
            Logger.getGlobal().finest(String.format("Took %.4f sec", (endTime - startTime) / Math.pow(10, 9)));

            ResultIO.savePopulation(new Population(allSolutions), Paths.get(System.getProperty("tatc.moea"), "population").toString());
            ResultIO.saveSearchResults(new Population(allSolutions), Paths.get(System.getProperty("tatc.moea"), "results").toString());
            AOSHistoryIO.saveCreditHistory(aos.getCreditHistory(), new File(System.getProperty("tatc.moea"), "res.credit"), ",");
            AOSHistoryIO.saveSelectionHistory(aos.getSelectionHistory(), new File(System.getProperty("tatc.moea"), "res.select"), ",");
        }
        
        else if(options == 3){

            String innovizeAssignment = "_" + System.nanoTime();
            
            //initialize problem
            StandardFormProblemGA problem = new StandardFormProblemGA(tsr, properties);
            
            //parameters and operators for search
            TypedProperties typProperties = new TypedProperties();
            
            //search paramaters set here
            int popSize = 3;
            int maxEvals = 50;
            typProperties.setInt("maxEvaluations", maxEvals);
            typProperties.setInt("populationSize", popSize);
            double crossoverProbability = 1.0;
            typProperties.setDouble("crossoverProbability", crossoverProbability);
            double mutationProbability = 1. / 60.;
            typProperties.setDouble("mutationProbability", mutationProbability);

            //define problem parameters
            Initialization initialization = new RandomInitialization(problem,
                    popSize);
            Population population = new Population();
            DominanceComparator comparator = new ParetoDominanceComparator();
            EpsilonBoxDominanceArchive archive = new EpsilonBoxDominanceArchive(new double[]{60, 10});
            final TournamentSelection selection = new TournamentSelection(2, comparator);
            
            //setup for innovization
            int epochLength = 3; //for learning rate
            int triggerOffset = 3;
            typProperties.setInt("nOpsToAdd", 4);
            typProperties.setInt("nOpsToRemove", 4);

            //setup for saving results
            typProperties.setBoolean("saveQuality", true);
            typProperties.setBoolean("saveCredits", true);
            typProperties.setBoolean("saveSelection", true);

            typProperties.setInt("nOpsToAdd", 2);
            typProperties.setInt("nOpsToRemove", 1);

            //setup for saving results
            typProperties.setBoolean("saveQuality", true);
            typProperties.setBoolean("saveCredits", true);
            typProperties.setBoolean("saveSelection", true);
        
            ArrayList<Variation> operators = new ArrayList();

            //kdo mode set to operator or repair
            typProperties.setString("kdomode", "operator");
                    
            //add domain-independent heuristics
            Variation SinglePointCross = new CompoundVariation(new OnePointCrossover(1.0), new BitFlip(0.2));
            operators.add(SinglePointCross);
             
            //set up OperatorReplacementStrategy
            EpochTrigger epochTrigger = new EpochTrigger(epochLength, triggerOffset);
            InitialTrigger initTrigger = new InitialTrigger(triggerOffset);
            CompoundTrigger compTrigger = new CompoundTrigger(Arrays.asList(new ReplacementTrigger[]{epochTrigger, initTrigger}));
            knowledge.operator.EOSSOperatorCreator eossOpCreator = new knowledge.operator.EOSSOperatorCreator();
            ArrayList<Variation> permanentOps = new ArrayList();
            permanentOps.add(SinglePointCross);
            RemoveNLowest operatorRemover = new RemoveNLowest(permanentOps, typProperties.getInt("nOpsToRemove", 2));
            OperatorReplacementStrategy ops = new OperatorReplacementStrategy(compTrigger, operatorRemover, eossOpCreator);
            typProperties.setDouble("pmin", 0.03);
            
            //create operator selector
            OperatorSelector operatorSelector3 = new AdaptivePursuit(operators, 0.8, 0.8, 0.03);
                    
            //create credit assignment
            SetImprovementDominance creditAssignment3 = new SetImprovementDominance(archive, 1, 0);
            
            //create AOS
            EpsilonMOEA emoea3 = new EpsilonMOEA(problem, population, archive,
                            selection, null, initialization, comparator);
            AOSVariation aosStrategy3 = new AOSVariationSI(operatorSelector3, creditAssignment3, popSize);
            AOSMOEA aos3 = new AOSMOEA(emoea3, aosStrategy3, true);
            AbstractPopulationLabeler labeler = new NondominatedSortingLabeler(.25);
            //ecs.submit(new KDOSearch(aos3, typProperties, labeler, ops, new File(mainPath.getParent(), "results").getAbsolutePath() + File.separator + "result", innovizeAssignment));
            KDOSearch kdo = new KDOSearch(aos3, typProperties, labeler, ops, new File(mainPath.getParent(), "results").getAbsolutePath() + File.separator + "result", innovizeAssignment);
            try {
                kdo.call();
            } catch (Exception ex) {
                Logger.getLogger(KDOSearch.class.getName()).log(Level.SEVERE, null, ex);
                throw new IllegalStateException("Evaluation failed.", ex);
            }   
        }
    }
}
