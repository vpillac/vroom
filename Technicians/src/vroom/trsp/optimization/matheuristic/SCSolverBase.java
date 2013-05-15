package vroom.trsp.optimization.matheuristic;

import gurobi.GRB;
import gurobi.GRB.DoubleAttr;

import java.util.Collection;
import java.util.List;

import vroom.common.utilities.IDisposable;
import vroom.common.utilities.Stopwatch;
import vroom.common.utilities.Stopwatch.ReadOnlyStopwatch;
import vroom.common.utilities.lp.SolverStatus;
import vroom.trsp.datamodel.ITRSPTour;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPRequest;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.datamodel.TRSPTour;
import vroom.trsp.util.TRSPGlobalParameters;
import vroom.trsp.util.TRSPLogging;

/**
 * <code>SCSolverBase</code> is the base class for solvers that solve set covering model for the TRSP.
 * <p>
 * Creation date: Aug 17, 2011 - 10:10:59 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public abstract class SCSolverBase implements IDisposable {

    /** Zero tolerance for the variable values reported by the solve */
    public static final double ZERO_TOLERANCE = 1e-10;

    public abstract SolverStatus solve();

    /**
     * Define an incumbent solution for the current model.
     * <p>
     * This method will attempt to find the variables corresponding to the tours of the given <code>incumbent</code> and
     * set their {@link DoubleAttr#Start} value to 1. All other variables are left to 0 if the solution is feasible, or
     * {@link GRB#UNDEFINED} if not
     * </p>
     * 
     * @param incumbent
     *            the incumbent solution
     * @return <code>true</code> if the incumbent was defined correctly, <code>false</code> if an exception was caught
     */
    public abstract boolean setIncumbent(TRSPSolution incumbent);

    /**
     * Add a set of columns (tours) to this model.
     * 
     * @param tours
     *            the tours to be added
     * @return <code>true</code> if columns were added correctly, <code>false</code> if an exception was caught
     */
    public abstract boolean addColumns(Collection<ITRSPTour> tours);

    /**
     * Add covering constraints for all the given requests.
     * <p>
     * Please note that this is done by default for all the requests present in the instance given to constructor
     * {@link #SCGurobiSolver(TRSPInstance)}
     * 
     * @param requests
     *            the requests to be covered
     * @param forceEqual
     *            <code>true</code> if constraints should be of form <code>=1</code> (set partitioning formulation)
     * @return <code>true</code> if constraints were added correctly, <code>false</code> if an exception was caught
     */
    public abstract boolean addCoveringConstraints(List<TRSPRequest> requests, boolean equal);

    /** A timer for the solver wall clock */
    final Stopwatch                mTimer;
    /** The Instance. */
    private final TRSPInstance mInstance;

    /**
     * Returns the timer for the solver wall clock
     * 
     * @return the timer for the solver wall clock
     */
    public ReadOnlyStopwatch getTimer() {
        return mTimer.getReadOnlyStopwatch();
    }

    /** The solution found during the last run */
    private TRSPSolution mSolution;

    /**
     * Returns the solution found during the last run.
     * 
     * @return the solution found during the last run
     */
    public TRSPSolution getSolution() {
        return mSolution;
    }

    /**
     * Sets the current solution found by the solver
     * 
     * @param solution
     *            the current solution found by the solver
     */
    void setSolution(TRSPSolution solution) {
        mSolution = solution;
    }

    /**
     * Gets the single instance of SCGurobiSolver.
     * 
     * @return single instance of SCGurobiSolver
     */
    public TRSPInstance getInstance() {
        return mInstance;
    }

    /** the global parameters used in this instance **/
    private final TRSPGlobalParameters mParameters;

    /**
     * Getter for the global parameters used in this instance
     * 
     * @return the value of name
     */
    public TRSPGlobalParameters getParameters() {
        return this.mParameters;
    }

    /** The status returned by the solver during the last call to {@link #solve()} */
    protected SolverStatus mStatus;

    public SCSolverBase(TRSPInstance instance, TRSPGlobalParameters parameters) {
        super();
        mTimer = new Stopwatch();
        mInstance = instance;
        mParameters = parameters;
    }

    /**
     * The status returned by the solver during the last call to {@link #solve()}
     * 
     * @return the status returned by the solver during the last call to {@link #solve()}
     */
    public SolverStatus getStatus() {
        return mStatus;
    }

    /**
     * Repair the current solution by ensuring that each client is visited exactly once
     * 
     * @return <code>true</code> if the solution was initially infeasible, <code>false</code> if it was feasible
     */
    public boolean repairSolution() {
        if (getSolution() == null)
            return false;
        boolean r = false;

        boolean[] visitedNodes = new boolean[getInstance().getMaxId()];
        TRSPTour[] visitingTour = new TRSPTour[getInstance().getMaxId()];

        for (int t = 0; t < getSolution().getTourCount(); t++) {
            TRSPTour tour = getSolution().getTour(t);
            tour.setAutoUpdated(true);
            for (int node : tour) {
                if (!getInstance().isRequest(node))
                    continue;

                if (visitedNodes[node]) {
                    r = true;
                    // Found a node that is already visited
                    // Evaluate detour cost in both tours
                    double prevCost = getSolution().getObjectiveValue();
                    int tour1pred = visitingTour[node].getPred(node);
                    int tour1succ = visitingTour[node].getSucc(node);
                    int tour2pred = tour.getPred(node);
                    int tour2succ = tour.getSucc(node);

                    double detour1 = getSolution().getCostDelegate().evaluateDetour(//
                            visitingTour[node], tour1pred, node, tour1succ, true);
                    double detour2 = getSolution().getCostDelegate().evaluateDetour(//
                            tour, tour2pred, node, tour2succ, true);
                    TRSPTour removedTour = null;
                    TRSPTour selectedTour = null;
                    double savings;
                    if (detour1 > detour2) {
                        removedTour = visitingTour[node];
                        selectedTour = tour;
                        savings = detour1;
                    } else {
                        removedTour = tour;
                        selectedTour = visitingTour[node];
                        savings = detour2;
                    }
                    removedTour.removeNode(node);
                    visitingTour[node] = selectedTour;
                    TRSPLogging
                            .getOptimizationLogger()
                            .info("SCSolverBase.repairSolution: removed duplicate visit of %s from tour %s (savings: %.3f)",
                                    node, removedTour.getTechnicianId(), savings);

                    if (getSolution().getObjectiveValue() > prevCost) {
                        TRSPLogging
                                .getOptimizationLogger()
                                .warn("SCSolverBase.repairSolution: total cost increased after removing visit of %s from tour %s (was:%s is:%s)",
                                        node, removedTour.getTechnicianId(), prevCost,
                                        getSolution().getObjectiveValue());
                    }
                } else {
                    visitedNodes[node] = true;
                    visitingTour[node] = tour;
                }

            }
        }

        return r;
    }

}