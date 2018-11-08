/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.evaluation.reductionmetrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.math3.util.FastMath;
import org.hipparchus.stat.descriptive.DescriptiveStatistics;
import seakers.orekit.coverage.analysis.AnalysisMetric;
import seakers.orekit.coverage.analysis.GroundEventAnalyzer;
import seakers.orekit.object.CoveragePoint;

/**
 * Metrics for each point
 * @author Prachi
 */
public class LocalMetricsImaging extends AbstractRMOutput {

    private final GroundEventAnalyzer fovAnalyzer;

    private final Iterator<CoveragePoint> iterator;

    private int count;

    public LocalMetricsImaging(GroundEventAnalyzer fovAnalyzer) {
        super(groupsInit(), metaDataInit(), summaryInit(), metricsInit(), unitsInit());
        this.fovAnalyzer = fovAnalyzer;
        this.iterator = fovAnalyzer.getCoveragePoints().iterator();
        this.count = 0;
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
        groups.add("POI");
        groups.add("AccessTime");
        groups.add("RevisitTime");
        groups.add("TimeToCoverage");
        groups.add("NumberOfPasses");
        return groups;
    }

    /**
     * assign the units to the groups
     *
     * @return the mapping between the groups and units
     */
    private static Map<String, List<String>> unitsInit() {
        Map<String, List<String>> units = new HashMap<>();
        units.put("POI", Arrays.asList(new String[]{"[deg]", "[deg]", "[km]"}));
        units.put("AccessTime", Arrays.asList(new String[]{"[s]"}));
        units.put("RevisitTime", Arrays.asList(new String[]{"[s]"}));
        units.put("TimeToCoverage", Arrays.asList(new String[]{"[s]"}));
        units.put("NumberOfPasses", Arrays.asList(new String[]{""}));
        return units;
    }

    /**
     * create the column headers
     *
     * @return the column headers
     */
    private static Map<String, List<String>> metricsInit() {
        Map<String, List<String>> metrics = new HashMap<>();
        metrics.put("POI",
                Arrays.asList(new String[]{"POI", "lat", "lon", "alt"}));
        metrics.put("AccessTime",
                Arrays.asList(new String[]{"ATavg", "ATmin", "ATmax"}));
        metrics.put("RevisitTime",
                Arrays.asList(new String[]{"RvTavg", "RvTmin", "RvTmax"}));
        metrics.put("TimeToCoverage",
                Arrays.asList(new String[]{"TCcov"}));
        metrics.put("NumberOfPasses",
                Arrays.asList(new String[]{"numPass"}));
        return metrics;
    }

    @Override
    public String getExtension() {
        return ".csv";
    }

    @Override
    protected String nextEntry() {
        if (iterator.hasNext()) {
            CoveragePoint point = iterator.next();
            Properties prop = new Properties();
            DescriptiveStatistics accesses = fovAnalyzer.getStatistics(AnalysisMetric.DURATION, true, point, prop);
            DescriptiveStatistics gaps = fovAnalyzer.getStatistics(AnalysisMetric.DURATION, false, point, prop);
            String[] entry = new String[14];
            entry[0] = "0";
            entry[1] = String.valueOf(fovAnalyzer.getEndDate().durationFrom(fovAnalyzer.getStartDate()));
            entry[2] = String.valueOf(count);
            entry[3] = String.valueOf(FastMath.toDegrees(point.getPoint().getLatitude()));
            entry[4] = String.valueOf(FastMath.toDegrees(point.getPoint().getLongitude()));
            entry[5] = String.valueOf(point.getPoint().getAltitude() / 1000.);
            entry[6] = String.valueOf(accesses.getMean());
            entry[7] = String.valueOf(accesses.getMin());
            entry[8] = String.valueOf(accesses.getMax());
            entry[9] = String.valueOf(gaps.getMean());
            entry[10] = String.valueOf(gaps.getMin());
            entry[11] = String.valueOf(gaps.getMax());
            entry[12] = "";
            entry[13] = String.valueOf(accesses.getN());

            count++;
            return String.join(",", entry);
        } else {
            return null;
        }
    }

}
