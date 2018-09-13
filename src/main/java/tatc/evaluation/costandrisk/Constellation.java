/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tatc.evaluation.costandrisk;

/**
 *
 * @author Prachichi
 */
public class Constellation {

    /**
     * design life (on orbit) for constellation system [months]
     */
    private final double designLife;
    
    /**
     * array of constellation spacecraft, details as structure subfields
     */
    private final Spacecraft[] spacecraft;

    public Constellation(double designLife, Spacecraft[] spacecraft) {
        this.designLife = designLife;
        this.spacecraft = spacecraft;
    }

    public double getDesignLife() {
        return designLife;
    }

    public Spacecraft[] getSpacecraft() {
        return spacecraft;
    }
}
