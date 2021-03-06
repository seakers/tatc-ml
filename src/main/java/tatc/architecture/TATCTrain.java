package tatc.architecture;

import org.hipparchus.util.FastMath;
import org.orekit.errors.OrekitException;
import org.orekit.orbits.Orbit;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;
import seakers.conmop.util.Bounds;
import seakers.orekit.object.OrbitWizard;
import seakers.orekit.util.Orbits;
import tatc.architecture.variable.MonolithVariable;
import org.orekit.time.AbsoluteDate;


import java.util.HashSet;
import java.util.ArrayList;

public class TATCTrain implements Architecture{
    private final HashSet<MonolithVariable> satellites;

    private final double sma;

    private final double inclination;

    private final ArrayList<Double> LTANs;

    /**
     * Creates a train constellation given the semi-major axis, the date of the launch and an array of LTANs in decimal
     * hours. The satellites contained in this constellation are not assigned any instrumentation nor are any
     * steering/attitude laws.
     */
    public TATCTrain(double semiMajorAxis, int dayLaunch, int monthLaunch, int yearLaunch, ArrayList<Double> LTANs, AbsoluteDate startDate) throws OrekitException{
        final double inc= Orbits.incSSO(semiMajorAxis- Constants.WGS84_EARTH_EQUATORIAL_RADIUS);
        this.satellites = new HashSet<>(LTANs.size());
        double raanRef0 = Orbits.LTAN2RAAN(semiMajorAxis- Constants.WGS84_EARTH_EQUATORIAL_RADIUS,LTANs.get(0),dayLaunch,monthLaunch,yearLaunch);
        int hourLTAN =  LTANs.get(0).intValue();
        int minLTAN = (int) (LTANs.get(0) * 60) % 60;
        int secLTAN = (int) (LTANs.get(0) * (60*60)) % 60;
        AbsoluteDate launchDate=new AbsoluteDate(yearLaunch,monthLaunch,dayLaunch,hourLTAN,minLTAN,secLTAN, TimeScalesFactory.getUTC());
        double timeLaunchStartDate=startDate.durationFrom(launchDate)/3600;
        double raanRef=( raanRef0  +  (timeLaunchStartDate / 24 * 2 * Math.PI) ) % ( 2 * Math.PI );
        for (int i=0 ; i<LTANs.size() ; i++) {
            MonolithVariable mono = new MonolithVariable(new Bounds(semiMajorAxis,semiMajorAxis),
                    new Bounds(0.0,0.0), new Bounds(inc,inc));
            mono.setSma(semiMajorAxis);
            mono.setEcc(0.0);
            mono.setInc(inc);
            mono.setRaan(  ( raanRef  +  ((LTANs.get(i)-LTANs.get(0)) / 24 * 2 * Math.PI) ) %  ( 2 * Math.PI ) );
            mono.setArgPer(0.0);
            double anom = ( 2 * Math.PI - ((LTANs.get(i)-LTANs.get(0)) / (Orbits.circularOrbitPeriod(semiMajorAxis)/3600) * 2 * Math.PI) ) %  ( 2 * Math.PI );
            mono.setTrueAnomaly(anom);
            this.satellites.add(mono);
        }
        this.inclination=inc;
        this.sma=semiMajorAxis;
        this.LTANs=LTANs;
    }

    public TATCTrain(double semiMajorAxis, ArrayList<Double> LTANs, AbsoluteDate startDate) throws OrekitException{
        final double inc= Orbits.incSSO(semiMajorAxis- Constants.WGS84_EARTH_EQUATORIAL_RADIUS);
        this.satellites = new HashSet<>(LTANs.size());
        double raanRef0 = Orbits.LTAN2RAAN(semiMajorAxis-Constants.WGS84_EARTH_EQUATORIAL_RADIUS,LTANs.get(0),
                startDate.getComponents(TimeScalesFactory.getUTC()).getDate().getDay(),
                startDate.getComponents(TimeScalesFactory.getUTC()).getDate().getMonth(),
                startDate.getComponents(TimeScalesFactory.getUTC()).getDate().getYear());
        int hourLTAN =  LTANs.get(0).intValue();
        int minLTAN = (int) (LTANs.get(0) * 60) % 60;
        int secLTAN = (int) (LTANs.get(0) * (60*60)) % 60;
        AbsoluteDate launchDate=new AbsoluteDate(startDate.getComponents(TimeScalesFactory.getUTC()).getDate().getYear(),
                startDate.getComponents(TimeScalesFactory.getUTC()).getDate().getMonth(),
                startDate.getComponents(TimeScalesFactory.getUTC()).getDate().getDay(),
                hourLTAN,minLTAN,secLTAN,TimeScalesFactory.getUTC());
        double timeLaunchStartDate=startDate.durationFrom(launchDate)/3600;
        double raanRef=( raanRef0  +  (timeLaunchStartDate / 24 * 2 * Math.PI) ) % ( 2 * Math.PI );
        for (int i=0 ; i<LTANs.size() ; i++) {
            MonolithVariable mono = new MonolithVariable(new Bounds(semiMajorAxis,semiMajorAxis),
                    new Bounds(0.0,0.0), new Bounds(inc,inc));
            mono.setSma(semiMajorAxis);
            mono.setEcc(0.0);
            mono.setInc(inc);
            mono.setRaan(  ( raanRef  +  ((LTANs.get(i)-LTANs.get(0)) / 24 * 2 * Math.PI) ) %  ( 2 * Math.PI ) );
            mono.setArgPer(0.0);
            double anom = ( 2 * Math.PI - ((LTANs.get(i)-LTANs.get(0)) / (Orbits.circularOrbitPeriod(semiMajorAxis)/3600) * 2 * Math.PI) ) %  ( 2 * Math.PI );
            mono.setTrueAnomaly(anom);
            this.satellites.add(mono);
        }
        this.inclination=inc;
        this.sma=semiMajorAxis;
        this.LTANs=LTANs;
    }

    public double getSma() {
        return sma;
    }

    public double getInclination() {
        return inclination;
    }

    public ArrayList<Double> getLTANs() {
        return LTANs;
    }

    public HashSet<MonolithVariable> getSatellites() {
        return satellites;
    }
}
