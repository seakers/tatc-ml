package tatc.architecture;

import org.hipparchus.util.FastMath;
import org.orekit.errors.OrekitException;
import org.orekit.orbits.Orbit;
import org.orekit.utils.Constants;
import seakers.conmop.util.Bounds;
import seakers.orekit.object.OrbitWizard;
import seakers.orekit.util.Orbits;
import tatc.architecture.variable.MonolithVariable;
import tatc.evaluation.reductionmetrics.AbsoluteDate;

import java.util.HashSet;
import java.util.ArrayList;

public class TATCTrain {
    private final HashSet<MonolithVariable> satellites;

    /**
     * Creates a train constellation given the semi-major axis, the date of the launch and an array of LTANs in decimal
     * hours. The satellites contained in this constellation are not assigned any instrumentation nor are any
     * steering/attitude laws. The date of the simulation is assumed to be the date of launch at the FIRST LTAN.
     */
    public TATCTrain(double semiMajorAxis, int dayLaunch, int monthLaunch, int yearLaunch, ArrayList<Double> LTANs, AbsoluteDate startDate) throws OrekitException{
        final double inc= Orbits.incSSO(semiMajorAxis- Constants.WGS84_EARTH_EQUATORIAL_RADIUS);
        this.satellites = new HashSet<>(LTANs.size());
        double raanRef0 = Orbits.LTAN2RAAN(semiMajorAxis- Constants.WGS84_EARTH_EQUATORIAL_RADIUS,LTANs.get(0),dayLaunch,monthLaunch,yearLaunch);
        int hourLTAN =  LTANs.get(0).intValue();
        int minLTAN = (int) (LTANs.get(0) * 60) % 60;
        int secLTAN = (int) (LTANs.get(0) * (60*60)) % 60;
        AbsoluteDate launchDate=new AbsoluteDate(yearLaunch,monthLaunch,dayLaunch,hourLTAN,minLTAN,secLTAN);
        double timeLaunchStartDate=startDate.durationFrom(launchDate)/3600;
        double raanRef=raanRef0  +  ( timeLaunchStartDate / 24 * 2 * Math.PI );
        for (int i=0 ; i<LTANs.size() ; i++) {
            MonolithVariable mono = new MonolithVariable(new Bounds(semiMajorAxis,semiMajorAxis),
                    new Bounds(0.0,0.0), new Bounds(inc,inc));
            mono.setSma(semiMajorAxis);
            mono.setEcc(0.0);
            mono.setInc(inc);
            mono.setRaan(  raanRef  +  ( (LTANs.get(i)-LTANs.get(0)) / 24 * 2 * Math.PI )  );
            mono.setArgPer(0.0);
            double anom = 0 - ( (LTANs.get(i)-LTANs.get(0)) / Orbits.circularOrbitPeriod(semiMajorAxis) * 2 * Math.PI );
            mono.setTrueAnomaly(anom);
            this.satellites.add(mono);
        }
    }

    public TATCTrain(double semiMajorAxis, ArrayList<Double> LTANs, AbsoluteDate startDate) throws OrekitException{
        final double inc= Orbits.incSSO(semiMajorAxis- Constants.WGS84_EARTH_EQUATORIAL_RADIUS);
        this.satellites = new HashSet<>(LTANs.size());
        double raanRef0 = Orbits.LTAN2RAAN(semiMajorAxis-Constants.WGS84_EARTH_EQUATORIAL_RADIUS,LTANs.get(0),startDate.getDay(),startDate.getMonth(),startDate.getYear());
        int hourLTAN =  LTANs.get(0).intValue();
        int minLTAN = (int) (LTANs.get(0) * 60) % 60;
        int secLTAN = (int) (LTANs.get(0) * (60*60)) % 60;
        AbsoluteDate launchDate=new AbsoluteDate(startDate.getYear(),startDate.getMonth(),startDate.getDay(),hourLTAN,minLTAN,secLTAN);
        double timeLaunchStartDate=startDate.durationFrom(launchDate)/3600;
        double raanRef=raanRef0  +  ( timeLaunchStartDate / 24 * 2 * Math.PI );
        for (int i=0 ; i<LTANs.size() ; i++) {
            MonolithVariable mono = new MonolithVariable(new Bounds(semiMajorAxis,semiMajorAxis),
                    new Bounds(0.0,0.0), new Bounds(inc,inc));
            mono.setSma(semiMajorAxis);
            mono.setEcc(0.0);
            mono.setInc(inc);
            mono.setRaan(  raanRef  +  ( (LTANs.get(i)-LTANs.get(0)) / 24 * 2 * Math.PI )  );
            mono.setArgPer(0.0);
            double anom = 0 - ( (LTANs.get(i)-LTANs.get(0)) / Orbits.circularOrbitPeriod(semiMajorAxis) * 2 * Math.PI );
            mono.setTrueAnomaly(anom);
            this.satellites.add(mono);
        }
    }

    public HashSet<MonolithVariable> getSatellites() {
        return satellites;
    }
}
