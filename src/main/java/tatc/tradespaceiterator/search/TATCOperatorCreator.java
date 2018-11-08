/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.tradespaceiterator.search;

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
import org.moeaframework.core.Variation;
import org.moeaframework.core.operator.CompoundVariation;
import seakers.aos.operatorselectors.replacement.OperatorCreator;

/**
 *
 * @author Prachi
 */
public class TATCOperatorCreator implements OperatorCreator {

    private final ArrayList<Variation> operatorSet;

    public TATCOperatorCreator() {
        this.operatorSet = new ArrayList<>();
    }

    /**
     * Learns a new set of potential operators based on the feature file
     *
     * @param featureFile
     */
    public void learnFeatures(File featureFile) {
        operatorSet.clear(); // clear previously created operators
        Collection<String> features = readFeatures(featureFile);
        for (String feature : features) {
            Variation operator = featureToOperator(feature);
            operatorSet.add(operator);
        }
    }

    private Collection<String> readFeatures(File featureFile) {
        ArrayList<String> features = new ArrayList();

        try (BufferedReader br = new BufferedReader(new FileReader(featureFile))) {
            String firstLine = br.readLine(); //skip the header
            String line;

            while ((line = br.readLine()) != null) {
                String[] str = line.split(",");
                String compositeFeatureString = str[1];
                features.add(compositeFeatureString);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TATCOperatorCreator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(File.class.getName()).log(Level.SEVERE, null, ex);
        }

        return features;
    }

    public Variation featureToOperator(String featureString) {
        CompoundVariation operator = new CompoundVariation();
        String operatorName = featureString;

        String[] atomicFeature = featureString.split("&");

        for (int i = 0; i < atomicFeature.length; i++) {

            boolean negate = atomicFeature[i].contains("!="); //flag to see if this feature is a negation of a feature

            Matcher m = Pattern.compile("(?!=\\d\\.\\d\\.)([\\d.]+)").matcher(atomicFeature[i]);
            double[] matcherValues = new double[2]; //first value is decision, second value is option
            int countValues = 0;

            while (m.find()) {
                matcherValues[countValues] = Double.parseDouble(m.group(0));
                countValues++;
            }

            TATCOperator op = new TATCOperator(negate, (int) matcherValues[0], matcherValues[1]);
            operator.appendOperator(op);
            operatorName += op.toString();
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
     * @param numOfOperators
     * @return
     */
    @Override
    public Collection<Variation> createOperator(int numOfOperators) {
        if (numOfOperators > operatorSet.size()) {
            throw new IllegalArgumentException(String.format("Cannot create "
                    + "more operators than are available. Tried to create %d "
                    + "operators but only %d available", numOfOperators, operatorSet.size()));
        }
        Collections.shuffle(operatorSet);
        ArrayList<Variation> out = new ArrayList<>(numOfOperators);
        for (int i = 0; i < numOfOperators; i++) {
            out.add(operatorSet.get(i));
        }
        return out;
    }
}