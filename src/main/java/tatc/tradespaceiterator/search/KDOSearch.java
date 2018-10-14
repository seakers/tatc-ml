/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.tradespaceiterator.search;

import java.io.BufferedReader;
import seakers.aos.aos.AOS;
import seakers.aos.operatorselectors.replacement.OperatorReplacementStrategy;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import knowledge.operator.EOSSOperatorCreator;
import org.moeaframework.algorithm.AbstractEvolutionaryAlgorithm;
import org.moeaframework.core.Algorithm;
import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;
import org.moeaframework.core.operator.CompoundVariation;
import org.moeaframework.util.TypedProperties;
import tatc.ResultIO;

/**
 * This method applies data mining and innovization to increase the efficiency
 * of the search
 *
 * @author Prachi
 */
public class KDOSearch implements Callable<Algorithm> {

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
    private AbstractPopulationLabeler dataLabeler;

    /**
     * Responsible for exporting the labels
     */
    private final ResultIO lableIO;

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
        this.lableIO = new ResultIO();
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
                
                //labels all the solutions in the history with an integer
                dataLabeler.label(allSolnPop);
                String labledDataFile = savePath + File.separator + name + "_" + String.valueOf(opResetCount) + "_labels.csv";
                lableIO.saveLabels(allSolnPop, labledDataFile, ",");
                
                String featureDataFile = savePath + File.separator + name + "_" + String.valueOf(opResetCount) + "_features.csv";

                /**
                 * behavioral are the ones that lie on the pareto front
                 * and the ones we are interested in. They have the label 1.
                **/
                ArrayList<Boolean> behavioral = new ArrayList<>();
                ArrayList<double[]> attributes = new ArrayList<>();

                File file = new File(labledDataFile);
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String line = br.readLine(); //skip this line because it's the header
                    String nextLine = br.readLine();
                    while (nextLine != null) {
                        String[] str = nextLine.split(",");
                        if (Integer.parseInt(str[0]) == 1) {
                            behavioral.add(true);
                        } else {
                            behavioral.add(false);
                        }

                        /**
                         * since the last two columns are metrics and not attributes,
                         * we will skip adding the metrics in the attributes list
                         */
                        double[] attrb = new double[(str.length-2) - 1]; 
                        for (int i = 1; i < str.length-2; i++) {
                            attrb[i - 1] = Double.parseDouble(str[i]);
                        }
                        attributes.add(attrb);
                        nextLine = br.readLine();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(KDOSearch.class.getName()).log(Level.SEVERE, null, ex);
                }

                double[][] dataset = new double[attributes.size()][attributes.get(0).length];
                BitSet behavioralSet = new BitSet(behavioral.size()); 

                for (int i = 0; i < behavioral.size(); i++) {
                    dataset[i] = attributes.get(i);
                    behavioralSet.set(i, behavioral.get(i));
                }

                AssociationRuleMining arm = new AssociationRuleMining(dataset, true);
                arm.run(behavioralSet, 0.0, 0.0, 6);
                
                List<DrivingFeature> topFeatures = arm.getTopFeatures(6, FeatureMetric.FCONFIDENCE);
                
                List<DrivingFeature> bestFeatures = MRMR.minRedundancyMaxRelevance(attributes.size(), behavioralSet, topFeatures, 4);
                lableIO.saveFeatures(bestFeatures, featureDataFile, ",");
            }
        }
        return alg;
    }
}