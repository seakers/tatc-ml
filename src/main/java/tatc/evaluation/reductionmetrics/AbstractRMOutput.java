/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.evaluation.reductionmetrics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The abstract class for a reduction and metrics output csv. This sets up the
 * skeleton of the csv file
 *
 * @author nhitomi
 */
public abstract class AbstractRMOutput {

    private final String delimiter = ",";

    /**
     * the meta data. (e.g. Description of subspace, including start time. T0 is
     * s after startTime, t1 is s after t0)
     */
    private final String metaData;

    /**
     * the summary. (e.g. During each time, DSM constellation global performance
     * is summarized by these statistics)
     */
    private final String summary;
    
    
    /**
     * An ordered list of the group of metric
     */
    private final List<String> groups;

    /**
     * A map of the metrics <group,List<metric>>.
     */
    private final Map<String, List<String>> metrics;

    /**
     * A map of the metrics units for each metric
     */
    private final Map<String, List<String>> units;

    /**
     * The number of values in the longest row (not counting the delimiter)
     */
    private final int rowLength;

    public AbstractRMOutput(List<String> group, String metaData, String summary, Map<String, List<String>> metrics, Map<String, List<String>> units) {
        this.metaData = metaData;
        this.summary = summary;
        this.groups = group;
        this.metrics = metrics;
        this.units = units;

        //check that the groups in both the units and metrics match
        if (!metrics.keySet().equals(new HashSet(groups))) {
            throw new IllegalArgumentException("Expected each group to have some metric");
        }
        if (!metrics.keySet().equals(units.keySet())) {
            throw new IllegalArgumentException("Expected group names in group headers and column headers to the identical");
        }

        //find the longest row
        this.rowLength = Math.max(getGroupHeaders().split(delimiter).length,
                getGroupHeaders().split(delimiter).length);
    }

    /**
     * Gets the meta data row that conforms to the following: Meta:,"<metadata>"
     * <optional sequence of commas>
     *
     * @return the meta data row
     */
    protected final String getMetaData() {
        StringBuilder sb = new StringBuilder();
        sb.append("Meta:").append(delimiter).append("\"").append(metaData).append("\"");
        for (int i = 2; i < rowLength; i++) {
            sb.append(delimiter);
        }
        return sb.toString();
    }

    /**
     * Gets the summary row that conforms to the following: Summ:,"<summary>"
     * <optional sequence of commas>
     *
     * @return the summary row
     */
    protected final String getSummaryRow() {
        StringBuilder sb = new StringBuilder();
        sb.append("Summ:").append(delimiter).append("\"").append(summary).append("\"");
        for (int i = 2; i < rowLength; i++) {
            sb.append(delimiter);
        }
        return sb.toString();
    }

    /**
     * Gets the group header that conforms to the following: Time, [s],
     * <grpHeader1>,...,<grpHeaderImax>.
     *
     * @return the group header
     */
    protected final String getGroupHeaders() {
        StringBuilder sb = new StringBuilder();
        sb.append("Time").append(delimiter).append("[s]");
        for (String group : groups) {
            sb.append(delimiter).append(group);
            for (String unit : units.get(group)) {
                sb.append(delimiter).append(unit);
            }
            //add extra delimiters until the row length of the group headers equals column headers
            for (int i = units.get(group).size() + 1; i < metrics.get(group).size(); i++) {
                sb.append(delimiter);
            }
        }
        return sb.toString();
    }

    /**
     * Gets the column headers where the
     *
     * @return
     */
    protected final String getColumnHeaders() {
        StringBuilder sb = new StringBuilder();
        sb.append("t0").append(delimiter).append("t1");
        for (String group : groups) {
            for (String col : metrics.get(group)) {
                sb.append(delimiter).append(col);
            }
        }
        return sb.toString();
    }

    public abstract String getExtension();

    /**
     * The method to get the next data entry.
     *
     * @return the next data entry. Return null if no more entries
     */
    protected abstract String nextEntry();

    /**
     * Provide the save path. The name given 
     * @param savePath
     * @param name Main name to save file. Extension is added automatically
     * @return 
     */
    public boolean save(File savePath, String name) {
        try (BufferedWriter bw = new BufferedWriter(
                new FileWriter(new File(savePath, name + getExtension())))) {
            bw.append(getMetaData());
            bw.newLine();
            bw.append(getSummaryRow());
            bw.newLine();
            bw.append(getGroupHeaders());
            bw.newLine();
            bw.append(getColumnHeaders());
            bw.newLine();
            
            String datarow = nextEntry();
            while(datarow != null){
                bw.append(datarow);
                bw.newLine();
                datarow = nextEntry();
            }
            bw.flush();
        } catch (IOException ex) {
            Logger.getLogger(AbstractRMOutput.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }
}
