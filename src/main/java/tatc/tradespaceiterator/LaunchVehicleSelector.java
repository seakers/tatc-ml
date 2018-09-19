/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.tradespaceiterator;

import tatc.architecture.variable.MonolithVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import seakers.orekit.util.Units;
import tatc.architecture.specifications.LaunchVehicleSpecification;

/**
 * A module to select the cheapest launch vehicle for a given DSM architecture
 *
 * @author nhitomi
 */
public class LaunchVehicleSelector {

    /**
     * The launch vehicles available for selection
     */
    private final Collection<LaunchVehicleSpecification> launchVehicles;

    private final SearchDatabase db;

    /**
     * Creates a new launch vehicle selector that can select any of the given
     * launch vehicles
     *
     * @param launchVehicles The launch vehicles available for selection
     */
    public LaunchVehicleSelector(Collection<LaunchVehicleSpecification> launchVehicles) {
        this.launchVehicles = launchVehicles;
        this.db = SearchDatabase.getInstance();
    }

    /**
     * Selects the vehicles to launch all monoliths. If monoliths are in the
     * same plane, then they may be packaged in the same launch vehicle, if both
     * fit within the launch vehicle's performance profile (e.g. deltaV, mass,
     * volume)
     *
     * @param monoliths the monoliths to launch
     * @return the map assigning monoliths to launch vehicles
     */
    public Map<Set<MonolithVariable>, LaunchVehicleSpecification> select(Set<MonolithVariable> monoliths) {
        Map<Set<MonolithVariable>, LaunchVehicleSpecification> lvMap = new HashMap<>();

        Collection<Set<MonolithVariable>> monolithGroup = groupMonoliths(monoliths);
        for (Set<MonolithVariable> group : monolithGroup) {
            //assign the best lv for the group
            lvMap.putAll(groupSelect(new ArrayList(group)));
        }

        return lvMap;
    }

    /**
     * Groups the monoliths with similar orbital parameters that might be
     * launched together in the same launch vehicle
     *
     * @param monoliths
     * @return a set of monoliths grouped together by orbital parameters
     */
    private Collection<Set<MonolithVariable>> groupMonoliths(Set<MonolithVariable> monoliths) {
        HashMap<Integer, Set<MonolithVariable>> planeGroup = new HashMap<>();
        int groupCount = 0;
        for (MonolithVariable mono : monoliths) {
            boolean added = false;
            for (Integer group : planeGroup.keySet()) {
                if (isSimilar(mono, planeGroup.get(group))) {
                    planeGroup.get(group).add(mono);
                    added = true;
                }
            }
            //if the monolith was not similar to any others so far, add it to a new group
            if (!added) {
                planeGroup.put(groupCount, new HashSet());
                planeGroup.get(groupCount).add(mono);
                groupCount++;
            }
        }
        return planeGroup.values();
    }

    /**
     * Checks a given monolith against a group of other monoliths to see if any
     * of the other monoliths occupy a similar orbit as the incoming one
     *
     * @param incoming a monolith not in the set of other monoliths
     * @param others a set of monoliths to check the incoming monolith against
     * @return true if the incoming monolith has similar orbital parameters to
     * the others
     */
    private boolean isSimilar(MonolithVariable incoming, Set<MonolithVariable> others) {
        for (MonolithVariable otherMono : others) {
            //if inclination is within a degree
            if (Math.abs(incoming.getInc() - otherMono.getInc()) < Units.deg2rad(1.0)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method will automatically select the launch vehicles for the given
     * subgroup of monolithic missions. Null is assigned to spacecraft for which
     * no launch vehicle can inject it to its desired orbit
     *
     * @param monolithGroup
     * @return a map assigning each monolith subset to a launch vehicle capable
     * of injecting spacecrafts into its orbit. If a spacecraft cannot be
     * launched to its orbit, it will not be present in the map
     */
    private Map<Set<MonolithVariable>, LaunchVehicleSpecification> groupSelect(List<MonolithVariable> monolithGroup) {
        HashMap<Set<MonolithVariable>, LaunchVehicleSpecification> bestOption = new HashMap<>();
        double lowestCost = Double.POSITIVE_INFINITY;

        //find all combinations of spacecraft
        for (List<Integer> groupIndex : fullfactPartition(monolithGroup.size())) {
            HashMap<Integer, Set<MonolithVariable>> groups = new HashMap<>();
            int index = 0;
            for (Integer groupNumber : groupIndex) {
                if (!groups.containsKey(groupNumber)) {
                    groups.put(groupNumber, new HashSet<>());
                }
                groups.get(groupNumber).add(monolithGroup.get(index));
                index++;
            }

            double sumCost = 0;
            HashMap<Set<MonolithVariable>, LaunchVehicleSpecification> option = new HashMap();
            for (Integer groupNumber : groups.keySet()) {
                //find all the feasible launch vehicles for each group of spacecraft
                ArrayList<LaunchVehicleSpecification> feasibleLV = new ArrayList<>();
                for (LaunchVehicleSpecification lv : launchVehicles) {
                    if (canLaunch(lv, groups.get(groupNumber))) {
                        feasibleLV.add(lv);
                    }
                }
                double cheapestLVCost;
                if (feasibleLV.isEmpty()) {
                    cheapestLVCost = Double.POSITIVE_INFINITY;
                    option.put(groups.get(groupNumber), null);
                } else {
                    //find the cheapest launch vehicle
                    Collections.shuffle(feasibleLV);
                    LaunchVehicleSpecification cheapestLV = feasibleLV.get(0);
                    for (LaunchVehicleSpecification lv : feasibleLV) {
                        if (lv.getCost() < cheapestLV.getCost()) {
                            cheapestLV = lv;
                        }
                    }
                    option.put(groups.get(groupNumber), cheapestLV);
                    cheapestLVCost = cheapestLV.getCost();
                }
                sumCost += cheapestLVCost;
            }

            if (sumCost < lowestCost) {
                lowestCost = sumCost;
                bestOption = option;
            }
        }
        return bestOption;
    }

    /**
     * Produces a full factorial enumeration of partitions for length n
     * decisions. The list of integers indicate which partition each index
     * belongs to
     *
     * @return
     */
    private static List<List<Integer>> fullfactPartition(int n) {
        LinkedList<List<Integer>> out = new LinkedList<>();
        List<Integer> initialSet = Arrays.asList(new Integer[]{0});
        out.add(initialSet);
        while (out.getFirst().size() < n) {
            List<Integer> list = out.removeFirst();

            int maxSetValue = 0;
            for (int i = 0; i < list.size(); i++) {
                maxSetValue = Math.max(maxSetValue, list.get(i));
            }

            for (int i = 0; i <= maxSetValue + 1; i++) {
                ArrayList<Integer> partialSolution = new ArrayList<>(list);
                partialSolution.add(i);
                out.add(partialSolution);
            }
        }
        return out;
    }

    /**
     * Checks multiple parameters of the spacecraft to see if the given launch
     * vehicle is capable of launching the given spacecraft
     *
     * @param lv the launch vehicle
     * @param spacecrafts to launch
     * @return true if this launch vehicle can launch the given spacecraft. Else
     * false.
     */
    public boolean canLaunch(LaunchVehicleSpecification lv, Collection<MonolithVariable> spacecrafts) {
        return (checkMass(lv, spacecrafts) >= 0);
    }

    /**
     * Checks if the given launch vehicle can lift the mass of the given
     * spacecraft
     *
     * @param lv the launch vehicle
     * @param spacecrafts spacecrafts to launch
     * @return the margin left in the lift capability in kg. Negative values
     * mean that the spacecraft is too heavy to lift to the specified orbit.
     */
    private double checkMass(LaunchVehicleSpecification lv, Collection<MonolithVariable> spacecrafts) {
        double totalMass = 0.0;
        for (MonolithVariable mono : spacecrafts) {
            totalMass += db.getObservatorySpecification(mono.getObservatoryID()).getStartMass();
        }

        return lv.getMass() - totalMass;
    }

}
