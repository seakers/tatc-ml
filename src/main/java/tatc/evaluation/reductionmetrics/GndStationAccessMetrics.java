/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.evaluation.reductionmetrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import seak.orekit.coverage.access.RiseSetTime;
import seak.orekit.coverage.access.TimeIntervalArray;

/**
 * Coverage information for each ground station
 * @author Prachi
 */
public class GndStationAccessMetrics extends AbstractRMOutput{
    
    private final TimeIntervalArray events;
    
    private final Iterator<RiseSetTime> iterator;

    /**
     * Flag to see if writing is done
     */
    private boolean done;

    public GndStationAccessMetrics(TimeIntervalArray listOfEvents) {
        super(groupsInit(), metaDataInit(), summaryInit(), metricsInit(), unitsInit());
        this.events = listOfEvents;
        this.iterator = listOfEvents.iterator();
        this.done = false; 
    } 
    
    private static String metaDataInit(){
        return "Description of ground station accesses for subspace/Obs, including startTime. t0 is s after startTime, t1 is s after t0.";
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
        return groups;
    }
    
    /**
     * assign the units to the groups
     * @return the mapping between the groups and units
     */
    private static Map<String, List<String>> unitsInit(){
        Map<String, List<String>> units = new HashMap<>();
        return units;
    }
    
    /**
     * create the column headers
     * @return the column headers
     */
    private static Map<String, List<String>> metricsInit(){
        Map<String, List<String>> metrics = new HashMap<>();
        return metrics;
    }

    @Override
    public String getExtension() {
        return "_designation_access.csv";
    }

    @Override
    protected String nextEntry() {
        if (this.iterator.hasNext()){
            String[] entry = new String[2];
            RiseSetTime time0 = iterator.next();
            entry[0] = String.valueOf(time0.getTime());
            RiseSetTime time1 = iterator.next();
            entry[1] = String.valueOf(time1.getTime());
            return String.join(",", entry);
        }
        else{
            return null;
        } 
    }
}
