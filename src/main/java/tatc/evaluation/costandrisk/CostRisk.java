/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.evaluation.costandrisk;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import tatc.AbstractModule;

/**
 * The cost and risk module
 *
 * @author Prachichi
 */
public class CostRisk extends AbstractModule {

    /**
     * The root directory of cost and risk module
     */
    private final File carRoot;

    public CostRisk(File inputFile, File outputFile) {
        super(inputFile, outputFile);
        carRoot = inputFile.getParentFile().getParentFile();
    }

    @Override
    public AbstractModule call() throws Exception {        
        String cmd = String.format(".%s%s%sTATc_CostRisk", File.separator, "bin", File.separator);
        Process process = Runtime.getRuntime().exec(cmd, null, carRoot);
        BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        String s = null;
        if (!process.waitFor(30, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Timed on cost execution of 30 sec.");
        } else if ((s = stdError.readLine()) != null) {
            throw new IllegalStateException(String.format("Cost and risk module failed\n Error from Cost and Risk: %s", s));
        }
        return this;
    }
}
