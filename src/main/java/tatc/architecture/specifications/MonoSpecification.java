/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.architecture.specifications;

import java.util.Set;
import tatc.architecture.variable.MonolithVariable;

/**
 * The specification for a single spacecraft mission.
 * @author nhitomi
 */
public class MonoSpecification {
    
    private final MissionConcept missionConcept;
    
    private final SatelliteOrbitSpecification satelliteOrbit;
    
    private final ObservatorySpecification observatorySpecification;
    
    private final InstrumentSpecification instrumentSpecification;

    public MonoSpecification(MissionConcept missionConcept, 
            SatelliteOrbitSpecification satelliteOrbit, 
            ObservatorySpecification observatorySpecification,
            InstrumentSpecification instrumentSpecification) {
        this.missionConcept = missionConcept;
        this.satelliteOrbit = satelliteOrbit;
        this.observatorySpecification = observatorySpecification;
        this.instrumentSpecification = instrumentSpecification;
    }

    public MissionConcept getMissionConcept() {
        return missionConcept;
    }

    public SatelliteOrbitSpecification getSatelliteOrbit() {
        return satelliteOrbit;
    }

    public ObservatorySpecification getObservatorySpecification() {
        return observatorySpecification;
    }

    public InstrumentSpecification getInstrumentSpecification() {
        return instrumentSpecification;
    }  
}
