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
import seakers.orekit.coverage.access.RiseSetTime;
import seakers.orekit.coverage.access.TimeIntervalArray;

/**
 * Coverage information for point of interest
 * @author Prachi
 */
public class POIAccessMetrics extends AbstractRMOutput{

    private int count;
    
    private Iterator<RiseSetTime> iterator;
    
    private TimeIntervalArray accesTimes;
    
    public POIAccessMetrics(TimeIntervalArray access) {
        super(groupsInit(), metaDataInit(), summaryInit(), metricsInit(), unitsInit());
        this.accesTimes = access;
        this.iterator = access.iterator();
        this.count = 0;
    }
       
    private static String metaDataInit(){
        return "Description of lat long access for subspace/Obs, including startTime. t0 is s after startTime, t1 is s after t0.";
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
        return "_access.csv";
    }

    @Override
    protected String nextEntry() {
        if (iterator.hasNext()){
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
