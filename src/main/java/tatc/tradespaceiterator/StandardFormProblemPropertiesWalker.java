package tatc.tradespaceiterator;

import tatc.architecture.specifications.SpecialOrbit;

import java.util.ArrayList;
import java.util.Properties;

public class StandardFormProblemPropertiesWalker extends StandardFormProblemProperties{
    public final ArrayList<Double> smas;
    public final ArrayList<Double> inclination;
    public final ArrayList<Integer> numberOfSats;
    public final ArrayList<SpecialOrbit> specialOrbits;

    public StandardFormProblemPropertiesWalker(TradespaceSearchRequest tsr, Properties properties) {
        super(tsr, properties);
        this.smas = discretizeSemiMajorAxes(tsr.getSatelliteOrbits().getSemiMajorAxisRange());
        this.inclination = discretizeInclinations(tsr.getSatelliteOrbits().getInclinationRangesOfInterest());
        this.specialOrbits = tsr.getSatelliteOrbits().getSpecialOrbits();
        if (this.specialOrbits != null) {
            for (int i = 0; i < specialOrbits.size(); i++) {
                this.inclination.add(this.getSpecialOrbitInclinations(this.specialOrbits.get(i)));
            }
        }
        this.numberOfSats = discretizeSatellite(tsr.getSatelliteOrbits().getNumberOfNewSatellites());
    }
}
