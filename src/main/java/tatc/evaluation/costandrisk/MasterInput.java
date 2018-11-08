/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.evaluation.costandrisk;

/**
 * Master input for the cost and risk module
 * @author Prachichi
 */
public class MasterInput {
    
    /**
     * Structure of the constellation specifics to instrument level
     */
    private final Constellation constellation;
    
    /**
     * Structure representing mission context specifics
     */
    private final Context context;

    public MasterInput(Constellation constellation, Context context) {
        this.constellation = constellation;
        this.context = context;
    }

    public Constellation getConstellation() {
        return constellation;
    }

    public Context getContext() {
        return context;
    }  
}
