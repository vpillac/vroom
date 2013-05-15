package vroom.optimization.pl.gurobi;

import gurobi.GRB;
import gurobi.GRB.DoubleAttr;
import gurobi.GRB.IntAttr;
import gurobi.GRBCallback;
import gurobi.GRBException;
import gurobi.GRBLinExpr;

import java.util.LinkedList;
import java.util.Set;

import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.utilities.gurobi.GRBConstraint;
import vroom.common.utilities.gurobi.GRBConstraintManager;
import vroom.common.utilities.gurobi.GRBConstraintManager.OrderingCriterion;
import vroom.common.utilities.gurobi.GRBPartialSolution;
import vroom.common.utilities.gurobi.GRBUtilities;
import vroom.common.utilities.lp.SolverStatus;

/**
 * <code>CVRPCuttingPlaneSolver</code> is a solver for the CVRP based on a cutting plane algorithm.
 * <p>
 * IP relaxations of the initial formulation are iteratively solved, and subtours are detected and eliminated at the end
 * of each iteration
 * </p>
 * <p>
 * Creation date: 3/09/2010 - 14:51:17
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class CVRPCuttingPlaneSolver extends CVRPSolverBase {

    private GRBConstraintManager                 mCtrManager;

    private final int                            mInjectedSolutions = 1;
    private final int                            mSavedSolutions    = 1;

    private int                                  mIterations;

    private final LinkedList<GRBPartialSolution> mPartialSolution;

    private int                                  mCutCount;

    private final boolean                        mSolInjection;

    public CVRPCuttingPlaneSolver(boolean output, boolean solInjection) throws GRBException {
        super(true, output);
        mPartialSolution = new LinkedList<GRBPartialSolution>();
        mSolInjection = solInjection;
    }

    /**
     * Create a callback to be added to the model.
     * 
     * @return the {@link GRBCallback} to be added to the model.
     */
    @Override
    protected CVRPCuttingPlaneCallback newCallback() {
        // if(mSolInjection)
        // return new CVRPCuttingPlaneCallback();
        // else
        return null;
    }

    @Override
    protected CVRPCuttingPlaneCallback getCallback() {
        return (CVRPCuttingPlaneCallback) super.getCallback();
    }

    /**
     * Returns the number of cutting plane iterations
     * 
     * @return the number of cutting plane iterations
     */
    @Override
    public int getIterations() {
        return mIterations;
    }

    @Override
    public void reset() {
        super.reset();
        mCtrManager = null;
        mCutCount = 0;
        mIterations = 0;
        mPartialSolution.clear();
    }

    @Override
    public void readInstance(IVRPInstance instance) throws GRBException {
        if (instance != getInstance()) {
            LOGGER.debug("Instance changed, reseting the callback");
            if (getCallback() != null) {
                getCallback().reset();
            }
        }
        super.readInstance(instance);
        mCtrManager = new GRBConstraintManager(getModel(), OrderingCriterion.INACTIVE_COUNT, 100);
        mCtrManager.setMaxConstraints(10000);
    }

    @Override
    public SolverStatus solve() throws GRBException {
        if (!isInitialized()) {
            throw new IllegalStateException("Solver is not initialized");
        }

        int status = GRB.Status.OPTIMAL;
        boolean subtourFound = true;

        mIterations = 0;
        mPartialSolution.clear();
        mCutCount = 0;

        getTimer().start();
        // Minimize
        getModel().set(IntAttr.ModelSense, +1);

        while (status == GRB.Status.OPTIMAL && subtourFound && !getTimer().hasTimedOut()) {

            if (mSolInjection && !mPartialSolution.isEmpty()) {
                LOGGER.debug("Injecting last partial solution");
                GRBPartialSolution sol = mPartialSolution.getFirst();
                if (sol != null) {
                    getModel().set(DoubleAttr.Start, sol.getVariables(), sol.getValues());
                }
            }

            LOGGER.debug("Starting the optimizer");
            getModel().update();
            getModel().optimize();

            LOGGER.debug("Optimization finished: %s", GRBUtilities.solverStatusString(getModel()));
            status = getModel().get(GRB.IntAttr.Status);
            mIterations++;

            if (status == GRB.Status.OPTIMAL) {
                mCtrManager.updateStats();
                mCtrManager.trashConstraints(true);

                // find subtours and add appropriate cuts
                subtourFound = findViolatedConstraints();

                // Save partial integer solutions
                savePartialSolutions();
            }
        }
        if (status != GRB.Status.OPTIMAL) {
            LOGGER.error("Optimization terminated in an unsupported status");
        }
        getTimer().stop();

        if (getTimer().hasTimedOut()) {
            LOGGER.info("Maximum time reached: %s", getTimer());
            return SolverStatus.TIME_LIMIT;
        }

        LOGGER.info("Optimization terminated in %sms", getTimer().readTimeMS());

        return GRBUtilities.convertStatus(status);
    }

    /**
     * Save the last {@link #mSavedSolutions} integer solutions
     */
    private void savePartialSolutions() {
        if (!mSolInjection) {
            return;
        }

        int solCount = 0;
        try {
            solCount = Math.min(mSavedSolutions, getModel().get(IntAttr.SolCount));
        } catch (GRBException e) {
            solCount = mSavedSolutions;
        }

        for (int i = 0; i < solCount; i++) {
            GRBPartialSolution sol;
            try {
                // Store the i-th partial solution
                sol = buildPartialMIPSolution(i);
                mPartialSolution.addFirst(sol);
            } catch (GRBException e) {
                LOGGER.warn("Exception when repairing int solution", e);
            }
        }

        // Remove exceeding partial solutions
        while (mPartialSolution.size() > mInjectedSolutions) {
            mPartialSolution.removeLast();
        }
    }

    /**
     * Find violated capacity constraints and add the corresponding cuts to the model
     * 
     * @return <code>true</code> if any violated constraint has been found
     * @throws GRBException
     */
    private boolean findViolatedConstraints() throws GRBException {

        double[] vars = getModel().get(DoubleAttr.X, getArcVars());

        Set<Set<Integer>> conComp = findConnectedComponents(vars);
        boolean subtourFound;
        if (conComp.size() > 1) {
            subtourFound = false;
            // Solution contains subtours
            for (Set<Integer> subtour : conComp) {
                double[] coefs = new double[getArcVars().length];

                double load = 0;
                double outter = 0;

                for (int i : subtour) {
                    load += getDemands()[i];
                    if (i != 0) {
                        for (int j = 0; j < getSize(); j++) {
                            if (j == 0 || !subtour.contains(j)) {
                                coefs[getArcIdx(j, i)] = 1;
                                outter += vars[getArcIdx(j, i)];
                            }
                        }
                    }
                }

                if (!subtour.contains(0)) {
                    LOGGER.debug("Disconnected subtour found (load:%s/%s): %s", load,
                            getCapacity(), subtour);
                } else if (load > getCapacity()) {
                    LOGGER.debug("Capacity constraint violation found(load:%s/%s): %s", load,
                            getCapacity(), subtour);
                }

                if (load > getCapacity() || !subtour.contains(0)) {
                    subtourFound = true;
                    double rhs = 2 * Math.ceil(load / getCapacity());

                    GRBLinExpr cut = new GRBLinExpr();
                    cut.addTerms(coefs, getArcVars());

                    GRBConstraint cons = new GRBConstraint(cut, GRB.GREATER_EQUAL, rhs, "cut"
                            + mCutCount);

                    // add the subtour elimitation cut
                    mCtrManager.addConstraint(cons);

                    LOGGER.debug("Cut added: %s", cons);

                    mCutCount++;
                } else {
                    // LOGGER.debug("Route is feasible (load:%s/%s): %s",
                    // load,getCapacity(),
                    // subtour);
                }
            }
        } else {
            subtourFound = true;
        }
        return subtourFound;
    }

    public class CVRPCuttingPlaneCallback extends GRBCallback {

        private boolean mInitialized;

        public CVRPCuttingPlaneCallback() {
            mInitialized = false;
        }

        public void reset() {
            mInitialized = false;
        }

        @Override
        protected void callback() {
            if (where == GRB.Callback.MIPNODE) {
                if (!mInitialized) {
                    for (GRBPartialSolution sol : mPartialSolution) {
                        try {
                            setSolution(sol.getVariables(), sol.getValues());
                        } catch (GRBException e) {
                            LOGGER.warn("Exception caught when setting a partial solution", e);
                        }
                    }
                    mInitialized = true;
                }
            }
        }

    }
}
