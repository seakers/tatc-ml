/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.architecture;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.BinaryVariable;
import org.moeaframework.core.variable.RealVariable;
import tatc.architecture.variable.DSMVariable;
import tatc.architecture.variable.MonolithVariable;

/**
 *
 * @author nhitomi
 */
public class DSMArchitecture extends AbstractTATCArchitecture {

    private static final long serialVersionUID = -6521840278276269451L;


    /**
     *
     * @param numberOfObjectives
     * @param existingSatellites
     * @param searchSpace
     * @param numberOfGroundStations the number of ground stations available.
     * One is selected at random in constructor
     * @param numberOfLaunchVehicles the number of launch vehicle available. One
     * is selected at random in constructor
     */
    public DSMArchitecture(int numberOfObjectives,
            Set<MonolithVariable> existingSatellites,
            DSMVariable searchSpace, int numberOfGroundStations, int numberOfLaunchVehicles) {
        
        super(3, numberOfObjectives, existingSatellites);
        this.setVariable(0, searchSpace); //orbital elements

        BinaryVariable groundStationDecision = new BinaryVariable(numberOfGroundStations);
        
        //randomly assigns one of the ground stations
        groundStationDecision.set(PRNG.nextInt(numberOfGroundStations), true);
        this.setVariable(1, groundStationDecision);

        RealVariable lvDecisions = new RealVariable(0, numberOfLaunchVehicles - 1);//(TODO chane this to a interger value variable)
        
        //randomly assigns one of the launch vehicle
        lvDecisions.randomize();
        this.setVariable(2, lvDecisions);

    }

    /**
     * This is a protected constructor for the copy method
     *
     * @param arch
     */
    protected DSMArchitecture(DSMArchitecture arch) {
        super(arch);
    }

    /**
     * Gets the id assigned for the launch vehicle for this architecture
     *
     * @return the id assigned for the launch vehicle for this architecture
     */
    public int getLaunchVehicleID() {
        return (int) Math.round(((RealVariable) this.getVariable(2)).getValue());
    }

    /**
     * Gets the id's associated with the ground stations used in this
     * architecture
     *
     * @return the id's associated with the ground stations used in this
     * architecture
     */
    public Set<Integer> getGroundStationIDs() {
        HashSet<Integer> out = new HashSet<>();
        BitSet bitSet = ((BinaryVariable) this.getVariable(1)).getBitSet();
        for (int i = bitSet.nextSetBit(0); i != -1; i = bitSet.nextSetBit(i + 1)) {
            out.add(i);
        }
        return out;
    }

    @Override
    public Solution copy() {
        return new DSMArchitecture(this);
    }
}
