/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.tradespaceiterator.search;

import seakers.aos.operatorselectors.replacement.OperatorCreator;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import knowledge.operator.EOSSOperator;
import org.moeaframework.core.Variation;
import org.moeaframework.core.operator.CompoundVariation;

/**
 *This class will read in a text file containing features that commonly occur
 * in good architectures and create appropriate operators that make the
 * architecture have those features
 * 
 * @author Prachi
 */
public class EOSSOperatorCreator implements OperatorCreator {

    private final String delimiter = ",";

    /**
     * Each feature should be in the form of (0 0 1 2 3)(1 3 -1 -1)(2 2 * -1)
     */
    private final Pattern compositeFeature = Pattern.compile(String.format("(\\([-\\d\\*A%s]*\\)).*", delimiter));
    
    /**
     * Each feature should be in the form of (0 0 1 2 3)(1 3 -1 -1)(2 2 * -1)
     */
    private final Pattern atomicFeature = Pattern.compile(String.format("(\\([-\\d\\*A%s]*\\))", delimiter));

    private final ArrayList<Variation> operatorSet;

    /**
     * The text file that will contain the rules to create new operators from.
     * The text must use some agreed upon interface/ontology. For this class,
     * the features that occur in good architectures shall be represented as
     * ([mode] [arg] [orbit] [instrument]...) For example (0 0 1 2 3) means run
     * mode 0 (pattern matching) such that orbit 1 has instrument 2 and
     * instrument 3
     *
     * (0 0 1 2 3)(1 3 -1 -1)(2 2 * -1) means in addition to the above rule,
     * also run mode 1 (orbit counting) to create architectures with 3 orbits,
     * and run mode 2 to create architectures with 2 instruments in orbit * (any
     * orbit)
     *
     */
    public EOSSOperatorCreator() {
        this.operatorSet = new ArrayList<>();
    }

    /**
     * Learns a new set of potential operators based on the feature file
     *
     * @param featureFile
     */
    public void learnFeatures(File featureFile) {
        operatorSet.clear();
        Collection<String> features = readFeatures(featureFile);
        for (String feature : features) {
            Variation operator = featureToOperator(feature);
            operatorSet.add(operator);
        }
    }

    private Collection<String> readFeatures(File featureFile) {
        ArrayList<String> features = new ArrayList();

        try (BufferedReader br = new BufferedReader(new FileReader(featureFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                //check to see that the line matches the regex pattern rule
                Matcher m = compositeFeature.matcher(line);
                if (m.matches()) {
                    features.add(line);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(EOSSOperatorCreator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EOSSOperatorCreator.class.getName()).log(Level.SEVERE, null, ex);
        }

        return features;
    }

    public Variation featureToOperator(String featureString) {
        CompoundVariation operator = new CompoundVariation();
        Matcher m = atomicFeature.matcher(featureString);
        String operatorName = "";
        while (m.find()) {
            //i=1 is the first matched group as explained in Matcher Javadoc
            for (int i = 1; i <= m.groupCount(); i++) {
                String feature = m.group(i);
                String[] params = feature.substring(1, feature.length() - 1).split(delimiter);
                //assumes that first 3 arguments are not the instruments and the rest are instruments
                String[] insts = new String[params.length - 3];
                System.arraycopy(params, 3, insts, 0, insts.length);
                EOSSOperator op = new EOSSOperator(params[0], params[1], params[2], insts);
                operator.appendOperator(op);
                operatorName += op.toString() + " + ";
            }
        }
        if (operatorName.equalsIgnoreCase("")) {
            throw new IllegalArgumentException(String.format("%s does not fit feature pattern.", featureString));
        }
        operator.setName(operatorName);
        return operator;
    }

    @Override
    public Variation createOperator() {
        Collections.shuffle(operatorSet);
        return operatorSet.get(0);
    }

    /**
     * Returns a new set of operators randomly selected from those available to
     * create
     *
     * @param nOperators
     * @return
     */
    @Override
    public Collection<Variation> createOperator(int nOperators) {
        if (nOperators > operatorSet.size()) {
            throw new IllegalArgumentException(String.format("Cannot create "
                    + "more operators than are available. Tried to create %d "
                    + "operators but only %d available", nOperators, operatorSet.size()));
        }
        Collections.shuffle(operatorSet);
        ArrayList<Variation> out = new ArrayList<>(nOperators);
        for (int i = 0; i < nOperators; i++) {
            out.add(operatorSet.get(i));
        }
        return out;
    }

    /**
     * returns all of the operators created from the feature file
     *
     * @return
     */
    public ArrayList<Variation> getOperatorSet() {
        return operatorSet;
    }

}
