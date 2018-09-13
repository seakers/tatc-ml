/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.architecture;

import java.util.Set;
import tatc.architecture.variable.MonolithVariable;

/**
 * Interface for architectures to run on reduction and cost and risk
 *
 * @author Prachi
 */
public interface TATCArchitecture {
    
    
    /**
     * Gets the satellites that are the new satellites that make up the DSM.
     * Ignores existing satellites
     *
     * @return t the satellites that are the new satellites
     */
    public Set<MonolithVariable> getNewSatellites();
    
    /**
     * Gets the satellites that are part of the DSM but were previously existing
     * and are not part of the search
     *
     * @return the satellites that are part of the DSM but were previously
     * existing and are not part of the search
     */
    public Set<MonolithVariable> getExistingSatellites();
    
    
    /**
     * Gets the number of satellites that make up this architecture including
     * existing and new satellites
     *
     * @return the number of satellites that make up this architecture including
     * existing and new satellites
     */
    public int getNumberOfSatellites();

}
