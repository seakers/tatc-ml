/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc;

import java.io.File;
import java.util.concurrent.Callable;

/**
 * An abstract class for a TATC module
 * @author nhitomi
 */
public abstract class AbstractModule implements Callable<AbstractModule> {
    
    private File inputFile;
    
    private File outputFile;

    public AbstractModule(File inputFile, File outputFile) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
    }

    public void setInputFile(File inputFile) {
        this.inputFile = inputFile;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    public File getInputFile() {
        return inputFile;
    }

    public File getOutputFile() {
        return outputFile;
    }
    
}
