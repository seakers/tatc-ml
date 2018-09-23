/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.evaluation.costandrisk;

/**
 * Wrapper for system risks
 * @author Prachi
 */
public class SystemRisk {
    
    public final String category;
    public final int consequence;
    public final int likelihood;
    public final String risk;

    public SystemRisk(String category, int consequence, int likelihood, String risk) {
        this.category = category;
        this.consequence = consequence;
        this.likelihood = likelihood;
        this.risk = risk;
    }

    public String getCategory() {
        return category;
    }

    public int getConsequence() {
        return consequence;
    }

    public int getLikelihood() {
        return likelihood;
    }
    
    public String getRisk() {
        return risk;
    }
}
