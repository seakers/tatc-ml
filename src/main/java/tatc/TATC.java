/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc;

/**
 * A pre-Phase A constellation mission analysis tool
 * @author Prachi
 */

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorService;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.moeaframework.core.Algorithm;
import tatc.tradespaceiterator.*;
import tatc.util.JSONIO;


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
        System.setProperty("tatc.results", new File(mainPath.getParentFile(), "results").getAbsolutePath());
        System.setProperty("tatc.access_results", new File((System.getProperty("tatc.results")), "access_results").getAbsolutePath());
        System.setProperty("tatc.dsms", new File(System.getProperty("tatc.access_results"), "DSMs").getAbsolutePath());
        System.setProperty("tatc.monos", new File(System.getProperty("tatc.access_results"), "Mono").getAbsolutePath());
        System.setProperty("tatc.moea", new File(System.getProperty("tatc.results"), "ga_results").getAbsolutePath());
        System.setProperty("tatc.mining", new File(System.getProperty("tatc.results"), "mining_results").getAbsolutePath());
        System.setProperty("tatc.numThreads", "16");
        Properties properties = new Properties();

        TradespaceSearchRequest tsr = JSONIO.readJSON(
                new File(mainPath, "TradespaceSearchRequest.json"),
                TradespaceSearchRequest.class);

        /*
         * This if-else block calls appropriate evaluation functions.
         * Modes for enumerating trade space
         * 0 - FF (Full Factorial)
         * 1 - GA (MOEA without AOS)
         * 2 - AOS (MOEA with Adaptive Operator Select)
         * 3 - KDO (MOEA with online Adaptive Operator Select/Knowledge Dependent Operator)
         */
        long startTime = System.nanoTime();

        if (tsr.getMissionConcept().getSearchPreferences() == 0) {
            StandardFormProblem problem = new StandardFormProblem(tsr,properties, StandardFormProblem.ProblemType.FF);
            problem.run();
            problem.shutdown();
        } else if (tsr.getMissionConcept().getSearchPreferences() == 1){
            StandardFormProblem problem = new StandardFormProblem(tsr,properties, StandardFormProblem.ProblemType.EPS);
            problem.run();
            problem.shutdown();
        } else if (tsr.getMissionConcept().getSearchPreferences() == 2){
            StandardFormProblem problem = new StandardFormProblem(tsr,properties, StandardFormProblem.ProblemType.AOS);
            problem.run();
            problem.shutdown();
        }
        else if(tsr.getMissionConcept().getSearchPreferences() == 3){
            StandardFormProblem problem = new StandardFormProblem(tsr,properties, StandardFormProblem.ProblemType.KDO);
            problem.run();
            problem.shutdown();
        }

        long endTime = System.nanoTime();
        Logger.getGlobal().finest(String.format("Took %.4f sec", (endTime - startTime) / Math.pow(10, 9)));
    }
}
