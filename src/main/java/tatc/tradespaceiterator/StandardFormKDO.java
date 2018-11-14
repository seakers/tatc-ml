package tatc.tradespaceiterator;

import org.moeaframework.algorithm.EpsilonMOEA;
import org.moeaframework.core.Variation;
import org.moeaframework.core.operator.CompoundVariation;
import org.moeaframework.core.operator.OnePointCrossover;
import org.moeaframework.core.operator.binary.BitFlip;
import org.moeaframework.util.TypedProperties;
import seakers.aos.aos.AOSMOEA;
import seakers.aos.creditassignment.setimprovement.SetImprovementDominance;
import seakers.aos.operator.AOSVariation;
import seakers.aos.operator.AOSVariationSI;
import seakers.aos.operatorselectors.AdaptivePursuit;
import seakers.aos.operatorselectors.OperatorSelector;
import seakers.aos.operatorselectors.replacement.*;
import tatc.tradespaceiterator.search.AbstractPopulationLabeler;
import tatc.tradespaceiterator.search.KDOSearch;
import tatc.tradespaceiterator.search.PopulationLabeler;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StandardFormKDO extends StandardFormGA {
    public StandardFormKDO(StandardFormProblemProperties properties) {
        super(properties);
    }

    public void start(){
        //parameters and operators for search
        TypedProperties typProperties = new TypedProperties();

        typProperties.setInt("maxEvaluations", maxNFE);
        typProperties.setInt("populationSize", populationSize);
        double crossoverProbability = 1.0;
        typProperties.setDouble("crossoverProbability", crossoverProbability);
        double mutationProbability = 1. / 60.;
        typProperties.setDouble("mutationProbability", mutationProbability);

        //setup for innovization
        int epochLength = 1; //for learning rate
        int triggerOffset = 2;
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
        AOSVariation aosStrategy3 = new AOSVariationSI(operatorSelector3, creditAssignment3, populationSize);
        AOSMOEA aos3 = new AOSMOEA(emoea3, aosStrategy3, true);
        AbstractPopulationLabeler labeler = new PopulationLabeler();
        //ecs.submit(new KDOSearch(aos3, typProperties, labeler, ops, new File(mainPath.getParent(), "results").getAbsolutePath() + File.separator + "result", innovizeAssignment));
        KDOSearch kdo = new KDOSearch(aos3, typProperties, labeler, ops, Paths.get(System.getProperty("tatc.mining")).toString(), "mining");
        try {
            kdo.call();
        } catch (Exception ex) {
            Logger.getLogger(KDOSearch.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalStateException("Evaluation failed.", ex);
        }
    }
}
