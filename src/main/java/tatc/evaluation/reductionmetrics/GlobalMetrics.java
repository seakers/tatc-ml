/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.evaluation.reductionmetrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import seakers.orekit.coverage.analysis.AnalysisMetric;
import seakers.orekit.coverage.analysis.GroundEventAnalyzer;

/**
 * Global Metrics saved for imaging missions architectures
 *
 * @author nhitomi
 */
public class GlobalMetrics extends AbstractRMOutput {

    /**
     * Analyzer for the accesses the points of interest
     */
    private final GroundEventAnalyzer fovAnalyzer;

    /**
     * Analyzer for the ground stations
     */
    private final GroundEventAnalyzer gndAnalyzer;

    /**
     * Flag to see if writing is done
     */
    private boolean done;

    public GlobalMetrics(GroundEventAnalyzer fovAnalyzer, GroundEventAnalyzer gndAnalyzer) {
        super(groupsInit(), metaDataInit(), summaryInit(), metricsInit(), unitsInit());
        this.fovAnalyzer = fovAnalyzer;
        this.gndAnalyzer = gndAnalyzer;
        done = false;
    }

    private static String metaDataInit() {
        return "Description of subspace, including startTime. t0 is s after startTime, t1 is s after t0.";
    }

    private static String summaryInit() {
        return "During each time, DSM constellation global performance is summarized by these statistics.";
    }

    /**
     * Defines the groups of metrics
     *
     * @return An ordered list of the groups of the metrics
     */
    private static List<String> groupsInit() {
        List<String> groups = new ArrayList<>();
        groups.add("TimeToCoverage");
        groups.add("AccessTime");
        groups.add("RevisitTime");
        groups.add("Coverage");
        groups.add("NumOfPOIpasses");
        groups.add("DownLinkLatency");
        groups.add("NumGSpassesPD");
        groups.add("TotalDownlinkTimePD [s]");
        groups.add("DownlinkTimePerPass [s]");
        groups.add("CrossOverlap");
        groups.add("AlongOverlap");
        groups.add("LunarPhase");
        return groups;
    }

    /**
     * assign the units to the groups
     *
     * @return the mapping between the groups and units
     */
    private static Map<String, List<String>> unitsInit() {
        Map<String, List<String>> units = new HashMap<>();
        units.put("TimeToCoverage", Arrays.asList(new String[]{"[s]"}));
        units.put("AccessTime", Arrays.asList(new String[]{"[s]"}));
        units.put("RevisitTime", Arrays.asList(new String[]{"[s]"}));
        units.put("Coverage", Arrays.asList(new String[]{}));
        units.put("NumOfPOIpasses", Arrays.asList(new String[]{"[#]"}));
        units.put("DownLinkLatency", Arrays.asList(new String[]{"[s]"}));
        units.put("NumGSpassesPD", Arrays.asList(new String[]{}));
        units.put("TotalDownlinkTimePD [s]", Arrays.asList(new String[]{}));
        units.put("DownlinkTimePerPass [s]", Arrays.asList(new String[]{}));
        units.put("CrossOverlap", Arrays.asList(new String[]{"[%]"}));
        units.put("AlongOverlap", Arrays.asList(new String[]{"[%]"}));
        units.put("LunarPhase", Arrays.asList(new String[]{"[deg]"}));
        return units;
    }

    /**
     * create the column headers
     *
     * @return the column headers
     */
    private static Map<String, List<String>> metricsInit() {
        Map<String, List<String>> metrics = new HashMap<>();
        metrics.put("TimeToCoverage",
                Arrays.asList(new String[]{"TCavg", "TCmin", "TCmax"}));
        metrics.put("AccessTime",
                Arrays.asList(new String[]{"ATavg", "ATmin", "ATCmax"}));
        metrics.put("RevisitTime",
                Arrays.asList(new String[]{"RTavg", "RTmin", "RTmax"}));
        metrics.put("Coverage",
                Arrays.asList(new String[]{"% Grid Covered"}));
        metrics.put("NumOfPOIpasses",
                Arrays.asList(new String[]{"PASavg", "PASmin", "PASmax"}));
        metrics.put("DownLinkLatency",
                Arrays.asList(new String[]{"DLavg", "DLmin", "DLmax"}));
        metrics.put("NumGSpassesPD",
                Arrays.asList(new String[]{"PassesPerDay"}));
        metrics.put("TotalDownlinkTimePD [s]",
                Arrays.asList(new String[]{"DLTimePerDay"}));
        metrics.put("DownlinkTimePerPass [s]",
                Arrays.asList(new String[]{"DLTavg", "DLTmin", "DLTmax"}));
        metrics.put("CrossOverlap",
                Arrays.asList(new String[]{"COavg", "COmin", "COmax"}));
        metrics.put("AlongOverlap",
                Arrays.asList(new String[]{"AOavg", "AOmin", "AOmax"}));
        metrics.put("LunarPhase",
                Arrays.asList(new String[]{"LPavg", "LPmin", "LPmax"}));
        return metrics;
    }

    @Override
    public String getExtension() {
        return ".csv";
    }

    @Override
    protected String nextEntry() {
        if (!done) {
            String[] entry = new String[30];
            Properties prop = new Properties();
            //Time
            entry[0] = "0";
            entry[1] = String.valueOf(fovAnalyzer.getEndDate().durationFrom(fovAnalyzer.getStartDate()));
            //Time to Access
            entry[2] = "";
            entry[3] = "";
            entry[4] = "";
            //AccessTime
            entry[5] = String.valueOf(fovAnalyzer.getStatistics(AnalysisMetric.DURATION, true, prop).getMean());
            entry[6] = String.valueOf(fovAnalyzer.getStatistics(AnalysisMetric.DURATION, true, prop).getMin());
            entry[7] = String.valueOf(fovAnalyzer.getStatistics(AnalysisMetric.DURATION, true, prop).getMax());
            //Revisit Time
            entry[8] = String.valueOf(fovAnalyzer.getStatistics(AnalysisMetric.DURATION, false, prop).getMean());
            entry[9] = String.valueOf(fovAnalyzer.getStatistics(AnalysisMetric.DURATION, false, prop).getMin());
            entry[10] = String.valueOf(fovAnalyzer.getStatistics(AnalysisMetric.DURATION, false, prop).getMax());
            //Coverage
            entry[11] = "";
            //NumOfPOIPasses
            entry[12] = String.valueOf(fovAnalyzer.getStatistics(AnalysisMetric.OCCURRENCES, true, prop).getMean());
            entry[13] = String.valueOf(fovAnalyzer.getStatistics(AnalysisMetric.OCCURRENCES, true, prop).getMin());
            entry[14] = String.valueOf(fovAnalyzer.getStatistics(AnalysisMetric.OCCURRENCES, true, prop).getMax());
            //Data Latency
            entry[15] = "";
            entry[16] = "";
            entry[17] = "";
            //NumGSPasses
            entry[18] = String.valueOf(gndAnalyzer.getStatistics(AnalysisMetric.OCCURRENCES, true, prop).getSum()
                    / (gndAnalyzer.getEndDate().durationFrom(gndAnalyzer.getStartDate()) / 86400));
            //TotalDownlinkTimePD
            entry[19] = String.valueOf(gndAnalyzer.getStatistics(AnalysisMetric.DURATION, true, prop).getSum()
                    / (gndAnalyzer.getEndDate().durationFrom(gndAnalyzer.getStartDate()) / 86400));
            //DownlinkTimePerPass
            entry[20] = String.valueOf(gndAnalyzer.getStatistics(AnalysisMetric.DURATION, true, prop).getMean());
            entry[21] = String.valueOf(gndAnalyzer.getStatistics(AnalysisMetric.DURATION, true, prop).getMin());
            entry[22] = String.valueOf(gndAnalyzer.getStatistics(AnalysisMetric.DURATION, true, prop).getMax());
            //Cross swath
            entry[23] = "";
            entry[24] = "";
            entry[25] = "";
            //along swath
            entry[26] = "";
            entry[27] = "";
            entry[28] = "";
            //spatial resolution
            entry[29] = "";

            done = true;
            return String.join(",", entry);
        } else {
            return null;
        }
    }

}
