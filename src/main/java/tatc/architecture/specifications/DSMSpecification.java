/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.architecture.specifications;

/**
 * Specification for a distributed satellite mission
 * @author nhitomi
 */
public class DSMSpecification {
    
    private final MissionConcept missionConcept;
    
    private final MonoSpecification[] manifestOfMonoSpecifications;

    public DSMSpecification(MissionConcept missionConcept, MonoSpecification[] manifestOfMonoSpecifications) {
        this.missionConcept = missionConcept;
        this.manifestOfMonoSpecifications = manifestOfMonoSpecifications;
    }

    public MissionConcept getMissionConcept() {
        return missionConcept;
    }

    public MonoSpecification[] getManifestOfMonoSpecifications() {
        return manifestOfMonoSpecifications;
    }   
    
}
