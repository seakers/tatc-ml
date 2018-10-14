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
import knowledge.operator.EOSSOperator;
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
            String line;
            while ((line = br.readLine()) != null) {
                features.add(line);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TATCOperatorCreator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TATCOperatorCreator.class.getName()).log(Level.SEVERE, null, ex);
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

}
