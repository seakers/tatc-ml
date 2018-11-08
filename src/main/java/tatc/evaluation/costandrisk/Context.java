/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.evaluation.costandrisk;

import tatc.architecture.specifications.GroundStationSpecification;

/**
 * mission ‘contextual’ description
 *
 * @author nhitomi
 */
public class Context {

    /**
     * nature of the mission directing organization Use: 'Government',
     * 'Military', 'Commercial', or 'Academic'. Default: 'Government' in the
     * absence of a recognized token.
     */
    private final String missionDirector;

    /**
     * desired fiscal year of produced cost estimates Must be greater than (OS
     * value for) the current year. Else, defaults to OS value for the current
     * year.
     *
     */
    private final int fiscalYear;

    /**
     * launch vehicle information and launch parameters
     */
    private final Launch launch;

    /**
     * array of ground station specifications, with each element giving the specifications of
     * an available ground station
     */
    private final CRGround[] groundStation;

    /**
     * 
     * @param missionDirector
     * @param fiscalYear
     * @param launch
     * @param groundStation 
     */
    public Context(String missionDirector, int fiscalYear, Launch launch, 
            GroundStationSpecification[] groundStation) {
        this.missionDirector = missionDirector;
        this.fiscalYear = fiscalYear;
        this.launch = launch;
        this.groundStation = new CRGround[groundStation.length];
        for(int i=0; i< groundStation.length; i++){
            this.groundStation[i]=new CRGround(groundStation[i]);
        }
    }

    public String getMissionDirector() {
        return missionDirector;
    }

    public int getFiscalYear() {
        return fiscalYear;
    }

    public Launch getLaunch() {
        return launch;
    }

    public CRGround[] getGroundStation() {
        return groundStation;
    }
}
