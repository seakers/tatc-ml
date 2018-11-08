/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.tradespaceiterator.search;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import seakers.aos.aos.AOS;
import seakers.aos.operatorselectors.replacement.OperatorReplacementStrategy;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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
import org.moeaframework.core.operator.OnePointCrossover;
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
    private final AbstractPopulationLabeler dataLabeler;

    /**
     * Responsible for exporting the labels
     */
    private final ResultIO lableIO;

    /**
     * operator creator for EOSS assignment problems
     */
    private final TATCOperatorCreator operatorCreator;

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
        if (!(ops.getOperatorCreator() instanceof TATCOperatorCreator)) {
            throw new IllegalArgumentException(String.format("Expected TATCOperatorCreator as operator creation strategy. Found %s", ops.getOperatorCreator().getClass().getSimpleName()));
        } else {
            this.operatorCreator = (TATCOperatorCreator) ops.getOperatorCreator();
        }
    }

    @Override
    public Algorithm call() throws Exception {
        int populationSize = (int) properties.getDouble("populationSize", 2);
        int maxEvaluations = (int) properties.getDouble("maxEvaluations", 50);
        int nOpsToAdd = (int) properties.getInt("nOpsToAdd", 2);

        System.out.println("Starting " + alg.getClass().getSimpleName() + " on " + alg.getProblem().getName() + " with pop size: " + populationSize);
        alg.step();
        long startTime = System.currentTimeMillis();

        /*
         * Keep track of each solution that is ever created, but only keep the unique ones
         */
        HashSet<Solution> allSolutions = new HashSet();
        Population initPop = ((AbstractEvolutionaryAlgorithm) alg).getPopulation();
        for (int i = 0; i < initPop.size(); i++) {
            initPop.get(i).setAttribute("NFE", 0);
            allSolutions.add(initPop.get(i));
        }

        /*
         * Count the number of times the algorithm is reset
         */
        int operatorResetCount = 0;

        while (!alg.isTerminated() && (alg.getNumberOfEvaluations() < maxEvaluations)) {
            Population population = ((AbstractEvolutionaryAlgorithm) alg).getPopulation();

            int nFuncEvals = alg.getNumberOfEvaluations();

            /*
             * Check to see if the operators need to be replaced
             */
            if (ops.checkTrigger(alg)) {
                System.out.println(String.format("Operator replacement event triggered at %d func eval", nFuncEvals));

                /*
                 * Reset the algorithm properties
                 */
                alg.getOperatorSelector().reset();

                /*
                 * Remove inefficient operators from the pool
                 */
                Collection<Variation> removedOperators = ops.removeOperators(alg);
                for (Variation operators : removedOperators) {
                    if (operators instanceof CompoundVariation) {
                        System.out.println(String.format("Removed: %s", ((CompoundVariation) operators).getName()));
                    } else {
                        System.out.println(String.format("Removed: %s", operators.toString()));
                    }
                }

                Population allSolnPop = new Population(allSolutions);

                /*
                 * Labels all the solutions in the history with an integer 0
                 * and all the solutions in the current run with integer 1.
                 */
                dataLabeler.label(allSolnPop);
                String labledDataFile = savePath + File.separator + name + "_" + String.valueOf(operatorResetCount) + "_labels.csv";
                lableIO.saveLabels(allSolnPop, labledDataFile, ",");

                String featureDataFile = savePath + File.separator + name + "_" + String.valueOf(operatorResetCount) + "_features.csv";

                /*
                 * behavioral are the ones that lie on the pareto front and the
                 * ones we are interested in. They have the label 1.
                 */
                ArrayList<Boolean> behavioral = new ArrayList<>();
                ArrayList<double[]> datasetValues = new ArrayList<>();

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

                        /*
                         * since the last two columns are metrics and not
                         * attributes, we will skip adding the metrics in the
                         * attributes list
                         */
                        //TODO: also add planes and phasing
                        double[] attrb = new double[(str.length - 4) - 1];
                        for (int i = 1; i < str.length - 4; i++) {
                            attrb[i - 1] = Double.parseDouble(str[i]);
                        }
                        datasetValues.add(attrb);
                        nextLine = br.readLine();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(KDOSearch.class.getName()).log(Level.SEVERE, null, ex);
                }

                double[][] dataset = new double[datasetValues.size()][datasetValues.get(0).length];
                BitSet behavioralSet = new BitSet(behavioral.size());

                for (int i = 0; i < behavioral.size(); i++) {
                    dataset[i] = datasetValues.get(i);
                    behavioralSet.set(i, behavioral.get(i));
                }

                /*
                 * Start running the data mining and mRMR algorithms
                 */
                AssociationRuleMining arm = new AssociationRuleMining(dataset, false);
                arm.run(behavioralSet, 0.0, 0.0, 3);
                List<DrivingFeature> topFeatures = arm.getTopFeatures(20, FeatureMetric.FCONFIDENCE);
                List<DrivingFeature> bestFeatures = MRMR.minRedundancyMaxRelevance(datasetValues.size(), behavioralSet, topFeatures, 3);
                lableIO.saveFeatures(bestFeatures, featureDataFile, ",");

                /*
                 * Start the feature learning process
                 */
                operatorCreator.learnFeatures(new File(featureDataFile));

                /*
                 * Start creating new operators based on features learned
                 */
                Collection<Variation> newOperators = operatorCreator.createOperator(nOpsToAdd);

                for (Variation operator : newOperators) {
                    StringBuilder sb = new StringBuilder();
                    OnePointCrossover cross = new OnePointCrossover(properties.getDouble("crossoverProbability", 0.0));
                    sb.append(cross.getClass().getSimpleName()).append(" + ");
                    sb.append(((CompoundVariation) operator).getName());
                    CompoundVariation repair = new CompoundVariation(cross, operator);
                    repair.setName(sb.toString());
                    alg.getOperatorSelector().addOperator(repair);
                }

                alg.getOperatorSelector().reset();
                for (Variation op : alg.getOperatorSelector().getOperators()) {
                    if (op instanceof CompoundVariation) {
                        System.out.println(String.format("Using: %s", ((CompoundVariation) op).getName()));
                    } else {
                        System.out.println(String.format("Using: %s", ((CompoundVariation) op).getName()));
                    }
                }

                operatorResetCount++;
            }
        }

        alg.step();

        System.out.println("Finished running KDO.");

        sendOperatorsToKB("http://tatckb.org/", "agency46");

        return alg;
    }

    /**
     * This method sends http requests to the knowledge base to send
     * operators found/created during the search
     *
     * @param targetURL
     * @param urlParameters
     */
    public static String sendOperatorsToKB(String targetURL, String urlParameters) {
        HttpURLConnection connection = null;

        try {
            //Create connection
            URL url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            connection.setRequestProperty("Content-Length", Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches(false);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.close();

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}

