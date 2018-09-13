/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.tradespaceiterator;

public class OutputBounds {

    private final String TimeToCoverage;
    private final String AccessTime;
    private final String NumberOfPassesPerMonth;
    private final String RevisitTime;
    private final String CrossOverlap;
    private final String AlongOverlap;
    private final String SignalNoiseRatio;
    private final String ObsZenith;
    private final String ObsAzimuth;
    private final String SunZenith;
    private final String DownlinkLatency;
    private final String DownlinkAccessPerDay;
    private final String SunAzimuth;
    private final String SpatialResolution;
    private final String CrossSwath;
    private final String AlongSwath;
    private final String ObsLatitude;
    private final String ObsLongitude;
    private final String ObsAltitude;
    private final String ObjZenith;
    private final String ObjAzimuth;
    private final String ObjRange;

    public OutputBounds(String TimeToCoverage, String AccessTime, 
            String NumberOfPassesPerMonth, String RevisitTime, 
            String CrossOverlap, String AlongOverlap, String SignalNoiseRatio, 
            String ObsZenith, String ObsAzimuth, String SunZenith, 
            String DownlinkLatency, String DownlinkAccessPerDay, 
            String SunAzimuth, String SpatialResolution, String CrossSwath,
            String AlongSwath, String ObsLatitude, String ObsLongitude, 
            String ObsAltitude, String ObjZenith, String ObjAzimuth, String ObjRange) {
        this.TimeToCoverage = TimeToCoverage;
        this.AccessTime = AccessTime;
        this.NumberOfPassesPerMonth = NumberOfPassesPerMonth;
        this.RevisitTime = RevisitTime;
        this.CrossOverlap = CrossOverlap;
        this.AlongOverlap = AlongOverlap;
        this.SignalNoiseRatio = SignalNoiseRatio;
        this.ObsZenith = ObsZenith;
        this.ObsAzimuth = ObsAzimuth;
        this.SunZenith = SunZenith;
        this.DownlinkLatency = DownlinkLatency;
        this.DownlinkAccessPerDay = DownlinkAccessPerDay;
        this.SunAzimuth = SunAzimuth;
        this.SpatialResolution = SpatialResolution;
        this.CrossSwath = CrossSwath;
        this.AlongSwath = AlongSwath;
        this.ObsLatitude = ObsLatitude;
        this.ObsLongitude = ObsLongitude;
        this.ObsAltitude = ObsAltitude;
        this.ObjZenith = ObjZenith;
        this.ObjAzimuth = ObjAzimuth;
        this.ObjRange = ObjRange;
    }

    public String getTimeToCoverage() {
        return TimeToCoverage;
    }

    public String getAccessTime() {
        return AccessTime;
    }

    public String getNumberOfPassesPerMonth() {
        return NumberOfPassesPerMonth;
    }

    public String getRevisitTime() {
        return RevisitTime;
    }

    public String getCrossOverlap() {
        return CrossOverlap;
    }

    public String getAlongOverlap() {
        return AlongOverlap;
    }

    public String getSignalNoiseRatio() {
        return SignalNoiseRatio;
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

    public String getDownlinkLatency() {
        return DownlinkLatency;
    }

    public String getDownlinkAccessPerDay() {
        return DownlinkAccessPerDay;
    }

    public String getSunAzimuth() {
        return SunAzimuth;
    }

    public String getSpatialResolution() {
        return SpatialResolution;
    }

    public String getCrossSwath() {
        return CrossSwath;
    }

    public String getAlongSwath() {
        return AlongSwath;
    }

    public String getObsLatitude() {
        return ObsLatitude;
    }

    public String getObsLongitude() {
        return ObsLongitude;
    }

    public String getObsAltitude() {
        return ObsAltitude;
    }

    public String getObjZenith() {
        return ObjZenith;
    }

    public String getObjAzimuth() {
        return ObjAzimuth;
    }

    public String getObjRange() {
        return ObjRange;
    }
    
    
    
    

}
