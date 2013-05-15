package vroom.trsp.optimization.constructive;

import vroom.common.heuristics.ProcedureStatus;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.datamodel.TRSPTour;
import vroom.trsp.datamodel.costDelegates.TRSPCostDelegate;
import vroom.trsp.optimization.TRSPHeuristic;
import vroom.trsp.optimization.constraints.TourConstraintHandler;
import vroom.trsp.optimization.mpa.DTRSPSolution;
import vroom.trsp.sim.TRSPSimulator;
import vroom.trsp.util.TRSPGlobalParameters;

public abstract class TRSPConstructiveHeuristic extends TRSPHeuristic {
    /** the current solution **/
    private TRSPSolution mSolution;

    /**
     * Getter for the current solution
     * 
     * @return the current solution
     */
    public TRSPSolution getSolution() {
        return this.mSolution;
    }

    /**
     * Setter for the current solution
     * 
     * @param solution
     *            the value to be set for the current solution
     */
    protected void setSolution(TRSPSolution solution) {
        this.mSolution = solution;
    }

    /**
     * Creates a new <code>TRSPConstructiveHeuristic</code> with the given constraint handler
     * 
     * @param constraintHandler
     *            the constraint handler to use in this heuristic
     * @param costDelegate
     *            the cost delegate used in this heuristic
     */
    public TRSPConstructiveHeuristic(TRSPInstance instance, TRSPGlobalParameters parameters,
            TourConstraintHandler constraintHandler, TRSPCostDelegate costDelegate) {
        super(instance, parameters, constraintHandler, costDelegate);
    }

    /**
     * This method will add empty tours for each technician starting and ending at the technician's home
     * 
     * @param sol
     *            the solution for which empty tours will be created
     */
    private void addEmptyTours(TRSPSolution sol) {
        // Create empty tours
        for (TRSPTour tour : sol) {
            if (tour.length() == 0) {
                tour.appendNode(tour.getTechnician().getHome().getID());
                tour.appendNode(getInstance().getHomeDuplicate(
                        tour.getTechnician().getHome().getID()));
            }
        }
    }

    /**
     * Builds an initial solution
     * <p>
     * This method will check if a {@linkplain TRSPSimulator#getCurrentSolution() current solution} is defined and use
     * it as a starting point.
     * </p>
     * 
     * @param solution
     *            the solution to be initialized
     * @see TRSPSimulator#getCurrentSolution()
     */
    public final void initializeSolution(TRSPSolution solution) {
        // Initialize from current solution
        if (solution.getInstance().getSimulator() != null
                && !solution.getInstance().getSimulator().isStaticSetting()) {
            DTRSPSolution current = solution.getInstance().getSimulator().getCurrentSolution();
            if (current != null) {
                solution.importSolution(current);
                ((DTRSPSolution) solution).unfreeze();
            }
            for (TRSPTour t : solution) {
                t.setAutoUpdated(true);
                int home = t.getTechnician().getHome().getID();
                int homeD = solution.getInstance().getHomeDuplicate(home);
                if (t.length() == 0)
                    t.appendNode(home);
                if (t.getLastNode() != homeD)
                    t.appendNode(homeD);
            }
        } else {
            addEmptyTours(getSolution());
        }

        initializeSolutionInternal(solution);
    }

    /**
     * Finish the initialization of the solution, this method is called by {@link #initializeSolution(TRSPSolution)}
     * 
     * @param sol
     */
    protected abstract void initializeSolutionInternal(TRSPSolution sol);

    @Override
    public ProcedureStatus call() {
        setStatus(ProcedureStatus.RUNNING);

        // Instantiate a dynamic instance when needed
        if (getInstance().getSimulator() != null && !getInstance().getSimulator().isStaticSetting())
            setSolution(new DTRSPSolution(getInstance(), getCostDelegate()));
        else
            setSolution(new TRSPSolution(getInstance(), getCostDelegate()));

        initializeSolution(getSolution());

        if (getSolution().getUnservedCount() == 0)
            setStatus(ProcedureStatus.TERMINATED);
        else
            setStatus(ProcedureStatus.INFEASIBLE_SOLUTION);

        return getStatus();
    }
}