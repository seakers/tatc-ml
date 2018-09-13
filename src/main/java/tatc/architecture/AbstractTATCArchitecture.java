/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.architecture;

import java.util.HashSet;
import java.util.Set;
import org.moeaframework.core.Solution;
import seak.conmop.variable.SatelliteVariable;
import tatc.architecture.variable.DSMVariable;
import tatc.architecture.variable.MonolithVariable;

/**
 * Abstract class for architectures to run on reduction and cost and risk
 * @author Prachi
 */
public abstract class AbstractTATCArchitecture extends Solution implements TATCArchitecture{
    
    /**
     * The existing satellites that are a part of the DSM but not decision
     * variables
     */
    private final Set<MonolithVariable> existingSatellites;

    public AbstractTATCArchitecture(int numberOfVariables, int numberOfObjectives, Set<MonolithVariable> existingSatellites) {
        this(numberOfVariables, numberOfObjectives, 0, existingSatellites);
    }

    public AbstractTATCArchitecture(int numberOfVariables, int numberOfObjectives, int numberOfConstraints, Set<MonolithVariable> existingSatellites) {
        super(numberOfVariables, numberOfObjectives, numberOfConstraints);
        this.existingSatellites = existingSatellites;
    }

    protected AbstractTATCArchitecture(AbstractTATCArchitecture solution) {
        super(solution);
        this.existingSatellites = solution.getExistingSatellites();
    }

    @Override
    public Set<MonolithVariable> getExistingSatellites() {
        return new HashSet(existingSatellites);
    }

    @Override
    public Set<MonolithVariable> getNewSatellites() {
        Set<MonolithVariable> monoliths = new HashSet<>();
        for (int i = 0; i < this.getNumberOfVariables(); i++) {
            if (this.getVariable(i) instanceof DSMVariable) {
                DSMVariable dsm = (DSMVariable) this.getVariable(i);
                for (SatelliteVariable sat : dsm.getSatelliteVariables()) {
                    monoliths.add((MonolithVariable) sat);
                }
            }
        }
        return monoliths;
    }


    @Override
    public int getNumberOfSatellites() {
        return existingSatellites.size()
                + getNewSatellites().size();
    }
}
