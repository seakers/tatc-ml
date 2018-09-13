/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.tradespaceiterator;

import java.util.HashMap;
import tatc.architecture.specifications.GroundStationSpecification;
import tatc.architecture.specifications.InstrumentSpecification;
import tatc.architecture.specifications.LaunchVehicleSpecification;
import tatc.architecture.specifications.ObservatorySpecification;

/**
 * Database that stores the available observatories and instrument
 * specifications
 *
 * @author nhitomi
 */
public class SearchDatabase {

    /**
     * A singleton instance of the search database
     */
    private static SearchDatabase instance;

    /**
     * Map of the observatory specifications
     */
    private static HashMap<Integer, ObservatorySpecification> obsMap;

    /**
     * Map of the instrument specifications
     */
    private static HashMap<Integer, InstrumentSpecification> instMap;

    /**
     * Map of the ground station specifications
     */
    private static HashMap<Integer, GroundStationSpecification> gstMap;

    /**
     * Map of the launch vehicle specifications
     */
    private static HashMap<Integer, LaunchVehicleSpecification> lvMap;

    /**
     * Create private constructor
     */
    private SearchDatabase() {
        SearchDatabase.obsMap = new HashMap<>();
        SearchDatabase.instMap = new HashMap<>();
        SearchDatabase.gstMap = new HashMap<>();
        SearchDatabase.lvMap = new HashMap<>();
    }

    /**
     * Gets an instance of the database
     *
     * @return an instance of the database
     */
    public static SearchDatabase getInstance() {
        if (instance == null) {
            instance = new SearchDatabase();
        }
        return instance;
    }

    /**
     * Adds a new observatory specification to the database, only if it does not
     * already exist in the database
     *
     * @param spec a new observatory specification
     * @return true if the specification was added to the database. else false
     */
    public boolean addObservatorySpecification(ObservatorySpecification spec) {
        if (!SearchDatabase.obsMap.values().contains(spec)) {
            SearchDatabase.obsMap.put(SearchDatabase.obsMap.size(), spec);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Adds a new instrument specification to the database, only if it does not
     * already exist in the database
     *
     * @param spec a new instrument specification
     * @return true if the specification was added to the database. else false
     */
    public boolean addInstrumentSpecification(InstrumentSpecification spec) {
        if (!SearchDatabase.instMap.values().contains(spec)) {
            SearchDatabase.instMap.put(SearchDatabase.instMap.size(), spec);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Adds a new ground station specification to the database, only if it does
     * not already exist in the database
     *
     * @param spec a new ground station specification
     * @return true if the specification was added to the database. else false
     */
    public boolean addGroundStationSpecification(GroundStationSpecification spec) {
        if (!SearchDatabase.gstMap.values().contains(spec)) {
            SearchDatabase.gstMap.put(SearchDatabase.gstMap.size(), spec);
            return true;
        } else {
            return false;
        }
    }
    
     /**
     * Adds a new launch vehicle specification to the database, only if it does
     * not already exist in the database
     *
     * @param spec a new launch vehicle specification
     * @return true if the specification was added to the database. else false
     */
    public boolean addLaunchVehicleSpecification(LaunchVehicleSpecification spec) {
        if (!SearchDatabase.lvMap.values().contains(spec)) {
            SearchDatabase.lvMap.put(SearchDatabase.lvMap.size(), spec);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gets the observatory specification by its ID in the database.
     *
     * @param id the id of the specification
     * @return the observatory specification with the given ID
     */
    public ObservatorySpecification getObservatorySpecification(int id) {
        return SearchDatabase.obsMap.get(id);
    }

    /**
     * Gets the instrument specification by its ID in the database.
     *
     * @param id the id of the specification
     * @return the instrument specification with the given ID
     */
    public InstrumentSpecification getInstrumentSpecification(int id) {
        return SearchDatabase.instMap.get(id);
    }

    /**
     * Gets the ground station specification by its ID in the database.
     *
     * @param id the id of the specification
     * @return the ground station specification with the given ID
     */
    public GroundStationSpecification getGroundStationSpecification(int id) {
        return SearchDatabase.gstMap.get(id);
    }
    
    /**
     * Gets the launch vehicle specification by its ID in the database.
     *
     * @param id the id of the specification
     * @return the launch vehicle specification with the given ID
     */
    public LaunchVehicleSpecification getLaunchVehicleSpecification(int id) {
        return SearchDatabase.lvMap.get(id);
    }

    /**
     * Gets the number of observatory specifications stored in the database
     *
     * @return the number of observatory specifications stored in the database
     */
    public int getNumberOfObservatorySpecifications() {
        return SearchDatabase.obsMap.size();
    }

    /**
     * Gets the number of instrument specifications stored in the database
     *
     * @return the number of instrument specifications stored in the database
     */
    public int getNumberOfInstrumentSpecifications() {
        return SearchDatabase.instMap.size();
    }

    /**
     * Gets the number of ground station specifications stored in the database
     *
     * @return the number of ground station specifications stored in the
     * database
     */
    public int getNumberOfGroundStationSpecifications() {
        return SearchDatabase.gstMap.size();
    }
    
     /**
     * Gets the number of launch vehicle specifications stored in the database
     *
     * @return the number of launch vehicle  specifications stored in the
     * database
     */
    public int getNumberOfLaunchVehicleSpecifications() {
        return SearchDatabase.lvMap.size();
    }


    /**
     * Gets the ID for the given specification
     *
     * @param spec the specification to search for
     * @return the ID for the given specification. If the specification is not
     * found in the database, -1 is returned
     */
    public int getID(ObservatorySpecification spec) {
        for (Integer i : obsMap.keySet()) {
            if (obsMap.get(i).equals(spec)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Gets the ID for the given specification
     *
     * @param spec the specification to search for
     * @return the ID for the given specification. If the specification is not
     * found in the database, -1 is returned
     */
    public int getID(InstrumentSpecification spec) {
        for (Integer i : instMap.keySet()) {
            if (instMap.get(i).equals(spec)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Gets the ID for the given specification
     *
     * @param spec the specification to search for
     * @return the ID for the given specification. If the specification is not
     * found in the database, -1 is returned
     */
    public int getID(GroundStationSpecification spec) {
        for (Integer i : gstMap.keySet()) {
            if (gstMap.get(i).equals(spec)) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Gets the ID for the given specification
     *
     * @param spec the specification to search for
     * @return the ID for the given specification. If the specification is not
     * found in the database, -1 is returned
     */
    public int getID(LaunchVehicleSpecification spec) {
        for (Integer i : lvMap.keySet()) {
            if (lvMap.get(i).equals(spec)) {
                return i;
            }
        }
        return -1;
    }

}
