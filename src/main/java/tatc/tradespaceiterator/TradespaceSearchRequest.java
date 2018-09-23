/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.tradespaceiterator;

import tatc.architecture.specifications.MissionConcept;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import tatc.architecture.specifications.InstrumentSpecification;
import tatc.architecture.specifications.LaunchVehicleSpecification;
import tatc.architecture.specifications.ObservatorySpecification;

/**
 *
 * @author nhitomi
 */
public class TradespaceSearchRequest {

    private final MissionConcept MissionConcepts;
    private final SatelliteOrbits SatelliteOrbits;
    private final String ObservatorySpecifications;
    private final String InstrumentSpecifications;
    private final String LaunchVehicleSpecifications;
    private final OutputBounds OutputBounds;
    private final FullOutputs FullOutputs;

    /**
     * 
     * @param MissionConcepts
     * @param SatelliteOrbits
     * @param ObervatorySpecifications
     * @param InstrumentSpecifications
     * @param LaunchVehicleSpecification
     * @param OutputBounds
     * @param FullOutputs
     */
    public TradespaceSearchRequest(MissionConcept MissionConcepts,
            SatelliteOrbits SatelliteOrbits, String ObervatorySpecifications,
            String InstrumentSpecifications, String LaunchVehicleSpecification,
            OutputBounds OutputBounds, FullOutputs FullOutputs) {
        this.MissionConcepts = MissionConcepts;
        this.SatelliteOrbits = SatelliteOrbits;
        this.ObservatorySpecifications = ObervatorySpecifications;
        this.InstrumentSpecifications = InstrumentSpecifications;
        this.LaunchVehicleSpecifications = LaunchVehicleSpecification;
        this.OutputBounds = OutputBounds;
        this.FullOutputs = FullOutputs;
    }

    public MissionConcept getMissionConcept() {
        return MissionConcepts;
    }

    public SatelliteOrbits getSatelliteOrbits() {
        return SatelliteOrbits;
    }

    /**
     * Reads in the given observatory specifications and converts them into
     * observatory specification objects
     *
     * @return a set of instrument specifications
     */
    public Set<ObservatorySpecification> getObervatorySpecifications() {
        HashSet<ObservatorySpecification> out = new HashSet<>();
        try (BufferedReader br = new BufferedReader(
                new FileReader(new File(System.getProperty("tatc.root"), ObservatorySpecifications)))) {
            String line = br.readLine();
            while (line != null) {
                out.add(ObservatorySpecification.create(line));
                line = br.readLine();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TradespaceSearchRequest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TradespaceSearchRequest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return out;
    }

    /**
     * Reads in the given instrument specifications and converts them into
     * instrument specification objects
     *
     * @return a set of instrument specifications
     */
    public Set<InstrumentSpecification> getInstrumentSpecifications() {
        HashSet<InstrumentSpecification> out = new HashSet<>();
        try (BufferedReader br = new BufferedReader(
                new FileReader(new File(System.getProperty("tatc.root"), InstrumentSpecifications)))) {
            String line = br.readLine();
            while (line != null) {
                out.add(InstrumentSpecification.create(line));
                line = br.readLine();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TradespaceSearchRequest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TradespaceSearchRequest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return out;
    }
    
    /**
     * Reads in the given launch vehicle specifications and converts them into
     * instrument specification objects
     *
     * @return a set of instrument specifications
     */
    public Set<LaunchVehicleSpecification> getLaunchVehicleSpecifications() {
        HashSet<LaunchVehicleSpecification> out = new HashSet<>();
        try (BufferedReader br = new BufferedReader(
                new FileReader(new File(System.getProperty("tatc.root"), LaunchVehicleSpecifications)))) {
            String line = br.readLine();
            while (line != null) {
                out.add(LaunchVehicleSpecification.create(line));
                line = br.readLine();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TradespaceSearchRequest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TradespaceSearchRequest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return out;
    }


    public OutputBounds getOutputBounds() {
        return OutputBounds;
    }

    public FullOutputs getFullOutputs() {
        return FullOutputs;
    }
}
