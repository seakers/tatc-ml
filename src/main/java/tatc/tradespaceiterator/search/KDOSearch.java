/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.tradespaceiterator.search;

import seakers.aos.aos.AOS;
import seakers.aos.history.AOSHistoryIO;
import seakers.aos.operatorselectors.replacement.OperatorReplacementStrategy;
import seakers.architecture.io.ResultIO;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.Callable;
import knowledge.operator.EOSSOperatorCreator;
import mining.DrivingFeaturesGenerator;
import mining.label.AbstractPopulationLabeler;
import mining.label.LabelIO;
import org.moeaframework.algorithm.AbstractEvolutionaryAlgorithm;
import org.moeaframework.core.Algorithm;
import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;
import org.moeaframework.core.operator.CompoundVariation;
import org.moeaframework.core.operator.OnePointCrossover;
import org.moeaframework.core.operator.binary.BitFlip;
import org.moeaframework.util.TypedProperties;

/**
 * This method applies data mining and innovization to
 * increase the efficiency of the search
 * 
 * @author Prachi
 */
public class KDOSearch implements Callable<Algorithm>{
    
       /**
     * The path to save the results
     */
    private final String savePath;
    /**
     * the name of the result files
     */
    private final String name;

    /**
     * the adaptive operator selector algorithm to use
     */
    private final AOS alg;

    /**
     * the properties associated with the algorithm
     */
    private final TypedProperties properties;

    /**
     * Class that supports method to label the interesting data
     */
    private final AbstractPopulationLabeler dataLabeler;

    /**
     * Responsible for exporting the labels
     */
    private final LabelIO lableIO;

    /**
     * operator creator for EOSS assignment problems
     */
    private final EOSSOperatorCreator opCreator;

    /**
     * the strategy for how to and when to remove and add operators
     */
    private final OperatorReplacementStrategy ops;

    /**
     * Constructs new search and automatically initializes Jess
     *
     * @param alg
     * @param properties
     * @param dataLabeler
     * @param ops
     * @param savePath
     * @param name
     */
    public KDOSearch(AOS alg, TypedProperties properties, AbstractPopulationLabeler dataLabeler, OperatorReplacementStrategy ops, String savePath, String name) {
        this.alg = alg;
        this.properties = properties;
        this.savePath = savePath;
        this.name = name;
        this.dataLabeler = dataLabeler;
        this.lableIO = new LabelIO();
        this.ops = ops;
        if (!(ops.getOperatorCreator() instanceof EOSSOperatorCreator)) {
            throw new IllegalArgumentException(String.format("Expected EOSSOperatorCreator as operator creation strategy. Found %s", ops.getOperatorCreator().getClass().getSimpleName()));
        } else {
            this.opCreator = (EOSSOperatorCreator) ops.getOperatorCreator();
        }
    }

    @Override
    public Algorithm call() throws Exception {
        int populationSize = (int) properties.getDouble("populationSize", 2);
        int maxEvaluations = (int) properties.getDouble("maxEvaluations", 50);
        int nOpsToAdd = (int) properties.getInt("nOpsToAdd", 2);

        // run the executor using the listener to collect results
        System.out.println("Starting " + alg.getClass().getSimpleName() + " on " + alg.getProblem().getName() + " with pop size: " + populationSize);
        alg.step();
        long startTime = System.currentTimeMillis();

        // keep track of each solution that is ever created, but only keep the unique ones
        HashSet<Solution> allSolutions = new HashSet();
        Population initPop = ((AbstractEvolutionaryAlgorithm) alg).getPopulation();
        for (int i = 0; i < initPop.size(); i++) {
            initPop.get(i).setAttribute("NFE", 0);
            allSolutions.add(initPop.get(i));
        }

        // count the number of times we reset operators
        int opResetCount = 0;

        while (!alg.isTerminated() && (alg.getNumberOfEvaluations() < maxEvaluations)) {
            Population pop = ((AbstractEvolutionaryAlgorithm) alg).getPopulation();

            int nFuncEvals = alg.getNumberOfEvaluations();
            //Check if the operators need to be replaced
            if (ops.checkTrigger(alg)) {
                System.out.println(String.format("Operator replacement event triggered at %d func eval", nFuncEvals));
                
                //for now reset the qualities
                alg.getOperatorSelector().reset();

                //remove inefficient operators
                Collection<Variation> removedOperators = ops.removeOperators(alg);
                for (Variation op : removedOperators) {
                    if (op instanceof CompoundVariation) {
                        System.out.println(String.format("Removed: %s", ((CompoundVariation) op).getName()));
                    } else {
                        System.out.println(String.format("Removed: %s", op.toString()));
                    }
                }

                //conduct learning
                Population allSolnPop = new Population(allSolutions);
                dataLabeler.label(allSolnPop);
                String labledDataFile = savePath + File.separator + name + "_" + String.valueOf(opResetCount) + "_labels.csv";
                lableIO.saveLabels(allSolnPop, labledDataFile, ",");

                String featureDataFile = savePath + File.separator + name + "_" + String.valueOf(opResetCount) + "_features.txt";

                //The association rule mining engine
                DrivingFeaturesGenerator dfg = new DrivingFeaturesGenerator(alg.getProblem().getNumberOfVariables());
                dfg.getDrivingFeatures(labledDataFile, featureDataFile, nOpsToAdd);

                opCreator.learnFeatures(new File(featureDataFile));

                //add new operators
                Collection<Variation> newOperators = opCreator.createOperator(nOpsToAdd);
                switch (properties.getString("kdomode", "operator")) {
                    case "operator":
                        //combines all extracted features into n operators 
                        for (Variation newOp : newOperators) {
                            StringBuilder sb = new StringBuilder();
                            OnePointCrossover cross = new OnePointCrossover(properties.getDouble("crossoverProbability", 0.0));
                            sb.append(cross.getClass().getSimpleName()).append(" + ");
                            sb.append(newOp.toString()).append(" + ");
                            CompoundVariation repair = new CompoundVariation(cross, newOp);
                            repair.setName(sb.toString());
                            alg.getOperatorSelector().addOperator(repair);
                        }
                        break;
                    case "repair":
                        //combines all extracted features into one operator
                        StringBuilder sb = new StringBuilder();
                        CompoundVariation repair = new CompoundVariation();
                        OnePointCrossover cross = new OnePointCrossover(properties.getDouble("crossoverProbability", 1.0));
                        sb.append(cross.getClass().getSimpleName()).append(" + ");
                        repair.appendOperator(cross);
                        for (Variation newOp : newOperators) {
                            repair.appendOperator(newOp);
                            sb.append(newOp.toString()).append(" + ");
                        }
                        BitFlip bitf = new BitFlip(properties.getDouble("mutationProbability", 1. / (double) alg.getProblem().getNumberOfVariables()));
                        sb.append(bitf.getClass().getSimpleName());
                        repair.appendOperator(bitf);
                        repair.setName(sb.toString());
                        alg.getOperatorSelector().addOperator(repair);
                        break;
                    default:
                        throw new UnsupportedOperationException("kdomod needs to be set to operator or repair");
                }
                alg.getOperatorSelector().reset();
                for (Variation op : alg.getOperatorSelector().getOperators()) {
                    if (op instanceof CompoundVariation) {
                        System.out.println(String.format("Using: %s", ((CompoundVariation) op).getName()));
                    } else {
                        System.out.println(String.format("Using: %s", op.toString()));
                    }
                }
                opResetCount++;
            }

            //print out the search stats every once in a while
            if (nFuncEvals % 500 == 0) {
                System.out.println("NFE: " + alg.getNumberOfEvaluations());
                System.out.print("Popsize: " + ((AbstractEvolutionaryAlgorithm) alg).getPopulation().size());
                System.out.println("  Archivesize: " + ((AbstractEvolutionaryAlgorithm) alg).getArchive().size());
            }
            alg.step();

            //since new solutions are put at end of population, only check the last few to see if any new solutions entered population
            for (int i = 1; i < 3; i++) {
                Solution s = pop.get(pop.size() - i);
                s.setAttribute("NFE", alg.getNumberOfEvaluations());
                allSolutions.add(s);
            }
        }

        Population allpop = new Population();
        Iterator<Solution> iter = allSolutions.iterator();
        while (iter.hasNext()) {
            allpop.add(iter.next());
        }

        alg.terminate();
        long finishTime = System.currentTimeMillis();
        System.out.println("Done with optimization. Execution time: " + ((finishTime - startTime) / 1000) + "s");

        String filename = savePath + File.separator + alg.getClass().getSimpleName() + "_" + name;
        ResultIO.savePopulation(((AbstractEvolutionaryAlgorithm) alg).getPopulation(), filename);
        ResultIO.savePopulation(allpop, filename + "_all");
        ResultIO.saveObjectives(alg.getResult(), filename);

        if (alg instanceof AOS) {
            AOS algAOS = (AOS) alg;
            if (properties.getBoolean("saveQuality", false)) {
                AOSHistoryIO.saveQualityHistory(algAOS.getQualityHistory(), new File(savePath + File.separator + name + ".qual"), ",");
            }
            if (properties.getBoolean("saveCredits", false)) {
                AOSHistoryIO.saveCreditHistory(algAOS.getCreditHistory(), new File(savePath + File.separator + name + ".credit"), ",");
            }
            if (properties.getBoolean("saveSelection", false)) {
                AOSHistoryIO.saveSelectionHistory(algAOS.getSelectionHistory(), new File(savePath + File.separator + name + ".hist"), ",");
            }
        }

        return alg;
    }

}
