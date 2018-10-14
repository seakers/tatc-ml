/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc;

/**
 *
 * @author Prachi
 */
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.moeaframework.analysis.collector.Accumulator;
import org.moeaframework.analysis.collector.InstrumentedAlgorithm;
import org.moeaframework.core.Population;
import org.moeaframework.core.PopulationIO;
import org.moeaframework.core.Solution;
import tatc.tradespaceiterator.search.DrivingFeature;

public class ResultIO implements Serializable{

    public ResultIO() {
        super();
    }

    /**
     * Saves the measured metrics from the instrumenter (e.g. hypervolume,
     * elapsed time)
     *
     * @param instAlgorithm
     * @param filename filename including the path
     */
    public static void saveSearchMetrics(InstrumentedAlgorithm instAlgorithm, String filename) {
        Accumulator accum = instAlgorithm.getAccumulator();

        File results = new File(filename + ".txt");
        System.out.println("Saving metrics");

        try (FileWriter writer = new FileWriter(results)) {
            Set<String> keys = accum.keySet();
            Iterator<String> keyIter = keys.iterator();
            while (keyIter.hasNext()) {
                String key = keyIter.next();
                int dataSize = accum.size(key);
                writer.append(key).append(",");
                for (int i = 0; i < dataSize; i++) {
                    writer.append(accum.get(key, i).toString());
                    if (i + 1 < dataSize) {
                        writer.append(",");
                    }
                }
                writer.append("\n");
            }
            writer.flush();

        } catch (IOException ex) {
            Logger.getLogger(ResultIO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Saves only the objective values of the solutions in the population
     *
     * @param pop
     * @param filename
     */
    public static void saveObjectives(Population pop, String filename) {
        System.out.println("Saving objectives");

        try {
            PopulationIO.writeObjectives(new File(filename + ".txt"), pop);
        } catch (IOException ex) {
            Logger.getLogger(ResultIO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Saves the objective values, decision values, and attributes of the
     * solutions in the population
     *
     * @param pop
     * @param filename
     */
    public static void saveSearchResults(Population pop, String filename) {
        System.out.println("Saving search results");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(filename + ".res")))) {
            //write the headers
            for (int i = 0; i < pop.get(0).getNumberOfObjectives(); i++) {
                bw.append(String.format("obj", i));
                bw.append(" ");
            }
            for (int i = 0; i < pop.get(0).getNumberOfVariables(); i++) {
                bw.append(String.format("dec", i));
                bw.append(" ");
            }
            Set<String> attrSet = pop.get(0).getAttributes().keySet();
            for (String attr : attrSet) {
                bw.append(attr + " ");
            }
            bw.newLine();

            //record values for each solution
            for (Solution soln : pop) {
                for (int i = 0; i < soln.getNumberOfObjectives(); i++) {
                    bw.append(String.valueOf(soln.getObjective(i)));
                    bw.append(" ");
                }
                for (int i = 0; i < soln.getNumberOfVariables(); i++) {
                    bw.append(soln.getVariable(i).toString());
                    bw.append(" ");
                }
                for (String attr : attrSet) {
                    bw.append(String.valueOf((soln.getAttribute(attr))));
                    bw.append(" ");
                }
                bw.newLine();
            }
        } catch (IOException ex) {
            Logger.getLogger(ResultIO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Reads a set of objective vectors from the specified file. Files read
     * using this method should only have been created using the
     * {@code saveObjectives} method.
     *
     * @param file the file containing the objective vectors
     * @return a population containing all objective vectors in the specified
     * file
     * @throws IOException if an I/O exception occurred
     */
    public static Population readObjectives(File file) throws IOException {
        return PopulationIO.readObjectives(file);
    }

    /**
     * Writes a collection of solutions to the specified file. This saves all
     * the explanations as well as any computed objectives Files written using
     * this method should only be read using the method. This method relies on
     * serialization.
     *
     * @param pop the solutions to be written in the specified file
     * @param filename the filename including the path to which the solutions
     * are written
     */
    public static void savePopulation(Population pop, String filename) {
        System.out.println("Saving population");

        try {
            PopulationIO.write(new File(filename + ".pop"), pop);
        } catch (IOException ex) {
            Logger.getLogger(ResultIO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Reads a population from the specified file. Files read using this method
     * should only have been created using the method. This method relies on
     * serialization.
     *
     * @param filename the filename including the path to which the solutions
     * are written
     * @return a population containing all solutions in the specified file
     * @throws IOException if an I/O exception occurred
     */
    public static Population loadPopulation(String filename) throws IOException {
        return PopulationIO.read(new File(filename));
    }


    /**
     * This method will save the label of each individual stored in the
     * population to a dlm file with a user specified separator. Only
     * individuals with a label attribute will be saved. In addition to the
     * label, the decision values and objective values will be saved in the file
     * as well. If the population is empty, this method does not attempt to save
     * to any file and returns false
     *
     * @param population
     * @param filename
     * @param separator
     * @return True if a file is successfully saved. Else false.
     */
    public boolean saveLabels(Population population, String filename, String separator) {
        if (population.isEmpty()) {
            return false;
        }
        //Only try saving populations that are not empty
        try (FileWriter fw = new FileWriter(new File(filename))) {

            //write the header
            fw.append(String.format("label%s", separator));
            for (int i = 0; i < population.get(0).getNumberOfVariables(); i++) {
                fw.append(String.format("dec%d", i) + separator);
            }
            for (int i = 0; i < population.get(0).getNumberOfObjectives(); i++) {
                if (i == population.get(0).getNumberOfObjectives() - 1) {
                    fw.append(String.format("obj%d\n", i));
                } else {
                    fw.append(String.format("obj%d%s", i, separator));
                }
            }

            //Write information of each individual
            int numDec = population.get(0).getNumberOfVariables();
            int numObj = population.get(0).getNumberOfObjectives();
            for (Solution individual : population) {
                if (individual.hasAttribute(PopulationLabeler.CLASSLABEL)) {
                    fw.append(individual.getAttribute(PopulationLabeler.CLASSLABEL) + separator);
                    for (int i = 0; i < numDec; i++) {
                        fw.append(String.format("%s%s", individual.getVariable(i), separator));
                    }
                    for (int i = 0; i < numObj; i++) {
                        if (i == numObj - 1) {
                            fw.append(String.format("%f\n", individual.getObjective(i)));
                        } else {
                            fw.append(String.format("%f%s", individual.getObjective(i), separator));
                        }
                    }
                }
            }
            fw.flush();
        } catch (IOException ex) {
            Logger.getLogger(ResultIO.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return true;
    }

    /**
     * This method will save the top features extracted from Apriori algorithm
     * to a dlm file with a user specified separator. If the population is
     * empty, this method does not attempt to save to any file and returns false
     *
     * @param features
     * @param filename
     * @param separator
     * @return True if a file is successfully saved. Else false.
     */
    public boolean saveFeatures(List<DrivingFeature> features, String filename, String separator) {
        if (features.isEmpty()) {
            return false;
        }

        //Only try saving populations that are not empty
        try (FileWriter fw = new FileWriter(new File(filename))) {

            //write the header
            fw.append(String.format("dec#=value,Support,FConfidence,RConfidence,Lift"));
            fw.append("\n");
                     
            //write feature
            for (int i = 0; i < features.size(); i++) {
                DrivingFeature thisFeature = features.get(i);
                fw.append(String.format("%s%s", thisFeature.getName(), separator));
                fw.append(String.format("%f%s", thisFeature.getSupport(), separator));
                fw.append(String.format("%f%s", thisFeature.getFConfidence(), separator));
                fw.append(String.format("%f%s", thisFeature.getRConfidence(), separator));
                fw.append(String.format("%f%s", thisFeature.getLift(), separator));

                fw.append("\n");
            }
            fw.flush();
        } catch (IOException ex) {
            Logger.getLogger(ResultIO.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return true;
    }
    
    public static abstract class PopulationLabeler {
    
    /**
     * This is the attribute used to store the label values.
     */
     
    private static String CLASSLABEL = "label";
    private static String DECISION = "decision";
    private static String OBJECTIVE = "objective";

    /**
     * This method will label the solutions and store the label information in
     * the attributes of the solutions in the population. The attribute is
     * stored as "label"
     *
     * @param population
     * @return a population with the individuals labeled
     */
    public Population label(Population population) {
        process(population);
        for (Solution individual : population) {
            individual.setAttribute(CLASSLABEL, label(individual));
        }
        return population;
    }

    /**
     * This method should be overridden if the population must be processed
     * before its individuals are labeled. For example, if labeling the
     * non-dominated solutions, non-dominated filtering must be applied before
     * labels are given. The default method is to remove any old label
     * attributes from the solutions
     *
     * @param population the population to process
     */
    protected void process(Population population) {
        for (Solution individual : population) {
            individual.removeAttribute(CLASSLABEL);
        }
    }

    /**
     * This method will label a given individual with an integer value.
     *
     * @param individual
     * @return the label to
     */
    protected abstract int label(Solution individual);
}
}

