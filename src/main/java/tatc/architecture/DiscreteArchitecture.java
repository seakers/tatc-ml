/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.architecture;

/**
 *
 * @author Prachi
 */

public class DiscreteArchitecture implements AbstractDiscreteArchitecture{

    private final int id;
    private final double[] outputs;
    private final int[] inputs;
    
    public DiscreteArchitecture(int id, int[] inputs, double[] outputs){
        this.id = id;
        this.outputs = outputs;
        this.inputs = inputs;
    }
    
    @Override
    public int getID(){
        return id;
    }
    
    @Override
    public double[] getOutputs(){
        return outputs;
    }
    
    public int[] getInputs(){
        return inputs;
    }

}
