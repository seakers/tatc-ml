/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.evaluation.costandrisk;

/**
 * Launch vehicle information and launch parameters
 *
 * @author nhitomi
 */
public class Launch {

    /**
     * name (standard designation) of intended launch site
     */
    private final String site;

    /**
     * planned year of first (or only) launch Must be greater than (OS value
     * for) the current year. Else, defaults to OS value for the current year.
     */
    private final int year;

    /**
     * name (standard designation) of intended launch vehicle
     */
    private final String vehicle;

    /**
     * number of launches expected to launch entire constellation
     */
    private final int totalNumber;

    /**
     * uniform rate expected for intended launch vehicle [launch/year]
     */
    private final double frequency;

    /**
     * expected success rate for intended vehicle/site/etc [0 <= p <= 1]
     */
    private final double reliability;

    public Launch(String site, int year, String vehicle, int totalNumber,
            double frequency, double reliability) {
        this.site = site;
        this.year = year;
        this.vehicle = vehicle;
        this.totalNumber = totalNumber;
        this.frequency = frequency;
        this.reliability = reliability;
    }

    public String getSite() {
        return site;
    }

    public int getYear() {
        return year;
    }

    public String getVehicle() {
        return vehicle;
    }

    public int getTotalNumber() {
        return totalNumber;
    }

    public double getFrequency() {
        return frequency;
    }

    public double getReliability() {
        return reliability;
    }
}