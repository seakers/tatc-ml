package tatc.tradespaceiterator;

import org.moeaframework.core.EpsilonBoxDominanceArchive;
import org.moeaframework.core.Initialization;
import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;
import org.moeaframework.core.comparator.DominanceComparator;
import org.moeaframework.core.comparator.ParetoDominanceComparator;
import org.moeaframework.core.operator.RandomInitialization;
import org.moeaframework.core.operator.TournamentSelection;
import org.moeaframework.core.variable.RealVariable;
import org.moeaframework.problem.AbstractProblem;
import org.orekit.utils.Constants;
import seakers.conmop.util.Factor;
import seakers.orekit.util.Orbits;
import tatc.architecture.StandardFormArchitecture;
import tatc.architecture.TATCWalker;
import tatc.architecture.specifications.MissionConcept;
import tatc.architecture.variable.IntegerVariable;
import tatc.evaluation.costandrisk.ResultOutput;
import tatc.exceptions.CostRiskException;
import tatc.exceptions.ReductionMetricsException;
import tatc.util.JSONIO;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class StandardFormProblemGA extends AbstractProblem implements StandardFormProblemImplementation {
    StandardFormProblemProperties properties;
    ArchitectureEvaluator archEval;
    int maxNFE;
    int populationSize;
    Initialization initialization;
    Population population;
    DominanceComparator comparator;
    EpsilonBoxDominanceArchive archive;
    TournamentSelection selection;

    public StandardFormProblemGA(StandardFormProblemProperties properties){
        super(5, 2);
        this.maxNFE=100;
        this.properties=properties;
        this.archEval=new ArchitectureEvaluator(properties);
        this.populationSize=80;
        this.initialization=new RandomInitialization(this, populationSize);
        this.population=new Population();
        this.comparator=new ParetoDominanceComparator();
        this.archive=new EpsilonBoxDominanceArchive(new double[]{60, 10});
        this.selection=new TournamentSelection(2, comparator);
    }


    public abstract void start();

    @Override
    public void evaluate(Solution solution) {
        //create subspace directory
        properties.currentDSMSubspace = new File(System.getProperty("tatc.dsms"),
                "Subspace" + Integer.toString(100000 + properties.evalCounter).substring(1));
        properties.currentDSMSubspace.mkdir();

        StandardFormArchitecture soln = null;
        if (solution instanceof StandardFormArchitecture) {
            soln = (StandardFormArchitecture) solution;
        } else {
            throw new IllegalArgumentException(
                    String.format("Expected a TATCArchitecture."
                            + " Found %s", solution.getClass()));
        }

        //read in values
        double sma = properties.smas.get(((IntegerVariable) soln.getVariable(0)).getValue());
        double incl = properties.inclination.get(((IntegerVariable) soln.getVariable(1)).getValue());

        //if there is an SSO, calculate it using the alt chosen
        //talk with Prachi here
        if (incl == -1) {
            incl = Orbits.incSSO(sma-Constants.WGS84_EARTH_EQUATORIAL_RADIUS);
            //incl = this.getSSOInclination(alt);
        }

        int numSats = properties.numberOfSats.get(((IntegerVariable) soln.getVariable(2)).getValue());

        //need to convert the real value that's between [0,1] to the number of planes.
        //The available number of planes is listed in the line below
        List<Integer> possiblePlanes = Factor.divisors(numSats); //planes would be divisors

        HashMap<Double, Integer> mappedPlanes = new HashMap<>();

        int counterPlanes = 0; //counter for possible planes index

        for (double i = 0; i <= 1; i += 1. / (possiblePlanes.size() - 1)) {
            mappedPlanes.put(i, possiblePlanes.get(counterPlanes));
            counterPlanes = counterPlanes + 1;
        }

        //read in the real value of planes from the solution
        double planes = ((RealVariable) (soln.getVariable(3))).getValue();

        int p = -1;
        double minDistancePlanes = Double.POSITIVE_INFINITY;
        Iterator<Double> iter1 = mappedPlanes.keySet().iterator();
        while (iter1.hasNext()) {
            double val = iter1.next();
            if (Math.abs(planes - val) < minDistancePlanes) {
                minDistancePlanes = Math.abs(planes - val);
                p = mappedPlanes.get(val);
            }
        }

        if (p == -1) {
            throw new IllegalStateException("Error in number of planes p = -1");
        }

        //The available number of phases is listed below
        //copying available number of planes to phases array
        //then we subtract -1 from each value to get the number of phases
        List<Integer> possiblePhases = new ArrayList<>();

        for (int i = 0; i < possiblePlanes.size(); i++) {
            possiblePhases.add(possiblePlanes.get(i) - 1);
        }

        HashMap<Double, Integer> mappedPhases = new HashMap<>();

        int counterPhases = 0; //counter for possible planes index

        for (double i = 0; i <= 1; i += 1. / (possiblePhases.size() - 1)) {
            mappedPhases.put(i, possiblePhases.get(counterPhases));
            counterPhases = counterPhases + 1;
        }

        //read in the real value of phases from the solution
        double phases = ((RealVariable) (soln.getVariable(4))).getValue();

        int q = -1;
        double minDistancePhases = Double.POSITIVE_INFINITY;
        Iterator<Double> iter = mappedPhases.keySet().iterator();
        while (iter.hasNext()) {
            double val = iter.next();
            if (Math.abs(phases - val) < minDistancePhases) {
                minDistancePhases = Math.abs(phases - val);
                q = mappedPhases.get(val);
            }
        }

        if (q == -1) {
            throw new IllegalStateException("Error in number of phases q = -1");
        }

        TATCWalker arch = new TATCWalker(sma, incl, numSats, p, q);

        //start date and end date/coverage
        MissionConcept newConcept = properties.tsr.getMissionConcept().copy();

        try {
            archEval.reductionAndMetrics(arch, newConcept);
            archEval.costAndRisk(arch, newConcept);
        } catch (ReductionMetricsException rmEx) {
            Logger.getLogger(StandardFormProblemGA.class.getName()).log(Level.SEVERE, null, rmEx);
            throw new IllegalStateException("Evaluation of solution in R&M failed.", rmEx);
        } catch (CostRiskException crEx) {
            Logger.getLogger(StandardFormProblemGA.class.getName()).log(Level.SEVERE, null, crEx);
            throw new IllegalStateException("Evaluation of solution in C&R failed.", crEx);
        }

        //set rm metrics
        solution.setObjective(0, properties.rm.getMetrics()[0]); //average revisit time

        //set cr metrics
        ResultOutput crOutput = JSONIO.readJSON(properties.cr.getOutputFile(), ResultOutput.class);
        solution.setObjective(1, crOutput.getLifecycleCost().getEstimate());

        Logger.getGlobal().fine(String.format(
                "avg revisit: %.2f[min], lifecycle cost: %.2f[$],",
                properties.rm.getMetrics()[0] / 60.,
                solution.getObjective(1)));

        properties.evalCounter++;
    }

    //structure of solution with walker params
    @Override
    public final Solution newSolution() {
        Solution sol = new StandardFormArchitecture(getNumberOfVariables(), getNumberOfObjectives(), properties.existingSatellites);
        sol.setVariable(0, new IntegerVariable(0, 0, properties.smas.size() - 1));
        sol.setVariable(1, new IntegerVariable(0, 0, properties.inclination.size() - 1));
        sol.setVariable(2, new IntegerVariable(0, 0, properties.numberOfSats.size() - 1));
        sol.setVariable(3, new RealVariable(0, 1)); //planes
        sol.setVariable(4, new RealVariable(0, 1)); //phasing
        return sol;
    }
}
