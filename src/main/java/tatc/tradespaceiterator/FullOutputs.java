/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.tradespaceiterator;

public class FullOutputs {

    private final String Ephemeris;
    private final String ObsZenith;
    private final String ObsAzimuth;
    private final String SunZenith;
    private final String SunAzimuth;
    private final String TimeStep;

    public FullOutputs(String Ephemeris, String ObsZenith, String ObsAzimuth, String SunZenith, String SunAzimuth, String TimeStep) {
        this.Ephemeris = Ephemeris;
        this.ObsZenith = ObsZenith;
        this.ObsAzimuth = ObsAzimuth;
        this.SunZenith = SunZenith;
        this.SunAzimuth = SunAzimuth;
        this.TimeStep = TimeStep;
    }

    public String getEphemeris() {
        return Ephemeris;
    }

    public String getObsZenith() {
        return ObsZenith;
    }

    public String getObsAzimuth() {
        return ObsAzimuth;
    }

    public String getSunZenith() {
        return SunZenith;
    }

    public String getSunAzimuth() {
        return SunAzimuth;
    }

    public String getTimeStep() {
        return TimeStep;
    }

}
