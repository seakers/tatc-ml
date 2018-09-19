/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.evaluation.reductionmetrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import seakers.orekit.analysis.Record;

/**
 * Coarse orbit propagation metrics saved for observatories
 *
 * @author nhitomi
 */
public class CoarsePropObservatories extends AbstractRMOutput {
    
    private final Collection<Record> orbit;
    
    private final Collection<Record> vector;
    
    private final Iterator<Record> orbitIterator;
    
    private final Iterator<Record> vectorIterator;

    private int count;
    
    public CoarsePropObservatories(Collection<Record> orbit, Collection<Record> vector) {
        super(groupsInit(), metaDataInit(), summaryInit(), metricsInit(), unitsInit());
        this.orbit = orbit;
        this.vector = vector;
        this.orbitIterator = orbit.iterator();
        this.vectorIterator = vector.iterator();
        this.count = 0;
    }
       
    private static String metaDataInit(){
        return "Description of subspace/Obs, including startTime. t0 is s after startTime, t1 is s after t0.";
    }
    
    private static String summaryInit(){
        return "During each time, Obs has known orbit Keplerian model and statistics.";
    }
    
    /**
     * Defines the groups of metrics
     *
     * @return An ordered list of the groups of the metrics
     */
    private static List<String> groupsInit() {
        List<String> groups = new ArrayList<>();
        groups.add("ObsOrbit");
        groups.add("ObsCoordinates");
        return groups;
    }
    
    /**
     * assign the units to the groups
     * @return the mapping between the groups and units
     */
    private static Map<String, List<String>> unitsInit(){
        Map<String, List<String>> units = new HashMap<>();
        units.put("ObsOrbit", Arrays.asList(new String[]{"[m]","","[deg]","[deg]","[deg]","[deg]"}));
        units.put("ObsCoordinates", Arrays.asList(new String[]{"[deg]","[deg]","[deg]"}));
        return units;
    }
    
    /**
     * create the column headers
     * @return the column headers
     */
    private static Map<String, List<String>> metricsInit(){
        Map<String, List<String>> metrics = new HashMap<>();
        metrics.put("ObsOrbit", 
                Arrays.asList(new String[]{" ","SMA","ecc", "inc","RAAN", "AOP", "MA"}));
        metrics.put("ObsCoordinates", Arrays.asList(new String[]{" ","lat","long","alt"}));
        return metrics;
    }

    @Override
    public String getExtension() {
        return ".csv";
    }

        @Override
    protected String nextEntry() {
        if (orbitIterator.hasNext() && vectorIterator.hasNext()) {
            String[] entry = new String[6];
            Record orb = orbitIterator.next();
            Record vec = vectorIterator.next();
            entry[0] = String.valueOf(orb.getDate().durationFrom(orbit.iterator().next().getDate()));
            entry[1] = "";
            entry[2] = "";
            entry[3] = orb.getValue().toString();
            entry[4] = "";
            entry[5] = vec.getValue().toString();
            
            return String.join(",", entry);
        }
        else{
            return null;
        }    
    }
}