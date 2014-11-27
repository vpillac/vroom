/**
 *
 */
package vroom..optimization.alns;

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import vroom.common.heuristics.alns.IDestroy.IDestroyResult;
import vroom.common.heuristics.alns.IRepair;
import vroom.common.utilities.BatchThreadPoolExecutor;
import vroom.common.utilities.optimization.IInstance;
import vroom.common.utilities.optimization.IParameters;
import vroom..datamodel.Solution;
import vroom..datamodel.costDelegates.CostDelegate;
import vroom..datamodel.costDelegates.TourBalanceDelegate;
import vroom..optimization.InsertionMove;
import vroom..optimization.constraints.ConstraintHandler;
import vroom..util.GlobalParameters;
import vroom..util.Logging;

/**
 * <code>RepairBestInsertion</code> is an implementation of the <em>best insertion</em> presented in:
 * <p>
 * Ropke, S. & Pisinger, D.<br/>
 * An adaptive large neighborhood search heuristic for the pickup and delivery problem with time windows<br/>
 * Transportation Science, 2006, 40, 455-472
 * </p>
 * <p>
 * Creation date: May 13, 2011 - 3:20:23 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class RepairBestInsertion implements IRepair<Solution> {

    private boolean           mBusy           = false;

    /** The currently optimized solution */
    private Solution      mSolution;

    /** A matrix containing the insertion cost of each request in each tour */
    private InsertionMove[][] mInsCostMatrix;
    /** The id of the best insertion tour for each request */
    private int[]             mBestInsTour;
    /** The best insertion move for each request */
    private InsertionMove[]   mBestInsMove;
    /** The tour in which a request was inserted last */
    private int               mLastInsTour;
    /** The best insertion overall</code> */
    private InsertionMove     mBestInsOverall = null;
    /** The best insertion tour overall</code> */
    private int               mBestInsTourOverall;

    /** A lock used to prevent concurrent updates */
    private final Lock        mUpdateLock     = new ReentrantLock();

    /** the maximum number of threads that will be used **/
    private int               mNumThreads     = Runtime.getRuntime().availableProcessors();

    /**
     * Getter for the maximum number of threads that will be used
     * 
     * @return the maximum number of threads that will be used
     */
    public int getNumThreads() {
        return this.mNumThreads;
    }

    /**
     * Setter for the maximum number of threads that will be used
     * 
     * @param numThreads
     *            the maximum number of threads that will be used
     */
    public void setNumThreads(int numThreads) {
        if (!mExecutor.isBatchComplete())
            throw new IllegalStateException("Cannot set the number of threads while there are active tasks");

        this.mNumThreads = numThreads;

        // Create a new executor
        mExecutor.shutdown();
        try {
            mExecutor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Logging.getBaseLogger().exception("RepairBestInsertion.setNumThreads", e);
        }
        int proc = Math.min(Runtime.getRuntime().availableProcessors(), getNumThreads());
        mExecutor = new BatchThreadPoolExecutor(proc, "repairBI");
    }

    /** An executor that will evaluate the request insertion costs */
    private BatchThreadPoolExecutor     mExecutor;

    /** A constraint handler for this repair component **/
    private final ConstraintHandler mConstraintHandler;

    /**
     * Getter for the constraint handler
     * 
     * @return A constraint handler for this repair component
     */
    public ConstraintHandler getConstraintHandler() {
        return this.mConstraintHandler;
    }

    /**
     * Creates a new <code>RepairBestInsertion</code>
     * 
     * @param constraintHandler
     */
    public RepairBestInsertion(ConstraintHandler constraintHandler) {
        super();
        mConstraintHandler = constraintHandler;

        int proc = Math.min(Runtime.getRuntime().availableProcessors(), getNumThreads());
        mExecutor = new BatchThreadPoolExecutor(proc, "repairBI");
    }

    @Override
    public boolean repair(Solution solution, IDestroyResult<Solution> destroyResult, IParameters params) {
        if (mBusy)
            throw new ConcurrentModificationException("This instance is already in use");
        mBusy = true;

        mSolution = solution;

        mLastInsTour = -1;

        mBestInsTour = new int[solution.getInstance().getMaxId()];
        Arrays.fill(mBestInsTour, -1);
        mBestInsMove = new InsertionMove[solution.getInstance().getMaxId()];
        mInsCostMatrix = new InsertionMove[solution.getInstance().getMaxId()][];
        for (int r : solution.getUnservedRequests())
            mInsCostMatrix[r] = new InsertionMove[solution.getTourCount()];

        while (!solution.getUnservedRequests().isEmpty()) {
            // Evaluate the insertion cost matrix
            evaluateInsCostMatrix(solution);

            // Execute the best insertion
            if (mBestInsOverall != null) {
                boolean inserted = InsertionMove.executeMove(mBestInsOverall);
                if (inserted) {
                    Logging.getOptimizationLogger().lowDebug(
                            "RepairBestInsertion.repair: insertion successfull - %s", mBestInsOverall);

                    // Remove the request from the set of pending requests
                    solution.markAsServed(mBestInsOverall.getNodeId());

                    // Store the id of the last modified tour
                    mLastInsTour = mBestInsTourOverall;

                    // Clear stored data
                    mBestInsMove[mBestInsOverall.getNodeId()] = null;
                    mInsCostMatrix[mBestInsOverall.getNodeId()] = null;
                } else {
                    Logging.getOptimizationLogger().lowDebug("RepairBestInsertion.repair: insertion failed - %s",
                            mBestInsOverall);
                    break;
                }
            } else {
                break;
            }

        }

        if (!solution.getUnservedRequests().isEmpty()) {
            Logging.getOptimizationLogger().lowDebug(
                    "RepairBestInsertion.repair: Unable to repair the solution, unfeasible requests: %s",
                    solution.getUnservedRequests());
        }

        mBusy = false;
        return solution.getUnservedRequests().isEmpty();
    }

    /**
     * Evaluates the insertion cost matrix stored in {@link #mInsCostMatrix} and updates the best insertion for each
     * request {@link #mBestInsTour} and overall {@link #mBestInsOverall}
     * 
     * @param solution
     *            the solution which unserved requests will be evaluated
     */
    private void evaluateInsCostMatrix(Solution solution) {
        if (getNumThreads())
            evaluateInsCostMatrixParallel(solution);
        else
            evaluateInsCostMatrixSequential(solution);
    }

    /**
     * Evaluates the insertion cost matrix stored in {@link #mInsCostMatrix} and updates the best insertion for each
     * request {@link #mBestInsTour} and overall {@link #mBestInsOverall}
     * 
     * @param solution
     *            the solution which unserved requests will be evaluated
     */
    private void evaluateInsCostMatrixSequential(Solution solution) {
        // Reset overall best insertion
        mBestInsOverall = null;
        mBestInsTourOverall = -1;

        // Select the tour cost delegate
        // We do this to ensure we have good insertions within a tour
        CostDelegate costDelegate = mSolution.getCostDelegate() instanceof TourBalanceDelegate ? ((TourBalanceDelegate) mSolution
                .getCostDelegate()).getTourCostDelegate() : mSolution.getCostDelegate();

        for (int req : solution.getUnservedRequests()) {
            if (mLastInsTour < 0 // First evaluation
                    || mBestInsTour[req] == mLastInsTour // or the best insertion tour has changed
            ) {
                // Evaluate insertion costs and update best request overall

                int bestTour = -1;
                InsertionMove bestIns = null;

                // Evaluate all tours in the first iteration, and only the last modified tour otherwise
                // int tmin = mLastInsTour < 0 ? 0 : mLastInsTour;
                // int tmax = mLastInsTour < 0 ? mSolution.getTourCount() : mLastInsTour + 1;
                // for (int t = tmin; t < tmax; t++) {
                for (int t = 0; t < mSolution.getTourCount(); t++) {

                    // Evaluate the insertion cost of the given request in tour t
                    InsertionMove ins = InsertionMove.bestInsertion(req, mSolution.getTour(t), costDelegate,
                            getConstraintHandler(), GlobalParameters.CTR_CHK_FWD_FEAS);

                    if (!ins.isFeasible())
                        // The move cannot be inserted in this tour, skip
                        continue;

                    // Reevaluate the insertion if needed to evaluate the insertion for the whole solution
                    if (mSolution.getCostDelegate() instanceof TourBalanceDelegate) {
                        mSolution.getCostDelegate().evaluateMove(ins);
                    }

                    // Store the value
                    mInsCostMatrix[req][t] = ins;
                    // Compare against best tour
                    if (bestIns == null || ins.getCost() < bestIns.getCost()) {
                        // Update best tour
                        bestTour = t;
                        bestIns = ins;
                    }
                }
                // Store best tour
                mBestInsTour[req] = bestTour;
                mBestInsMove[req] = bestIns;

                if (bestTour == -1)
                    // No feasible was found
                    return;

                // Update best request overall
                if (mBestInsOverall == null || mBestInsOverall.getCost() > mInsCostMatrix[req][bestTour].getCost()) {
                    mBestInsOverall = mBestInsMove[req];
                    mBestInsTourOverall = bestTour;
                }

            } else {
                if (mBestInsOverall == null
                        || (mBestInsTour[req] >= 0 && mBestInsOverall.getCost() > mInsCostMatrix[req][mBestInsTour[req]]
                                .getCost())) {
                    mBestInsOverall = mBestInsMove[req];
                    mBestInsTourOverall = mBestInsTour[req];
                }
            }
        }
    }

    /**
     * Evaluates the insertion cost matrix stored in {@link #mInsCostMatrix} and updates the best insertion for each
     * request {@link #mBestInsTour} and overall {@link #mBestInsOverall}
     * 
     * @param solution
     *            the solution which unserved requests will be evaluated
     */
    private void evaluateInsCostMatrixParallel(Solution solution) {
        // Reset overall best insertion
        mBestInsOverall = null;
        mBestInsTourOverall = -1;

        LinkedList<InsertionEvaluator> eval = new LinkedList<RepairBestInsertion.InsertionEvaluator>();

        for (int r : solution.getUnservedRequests()) {
            if (mLastInsTour < 0 // First evaluation
                    || mBestInsTour[r] == mLastInsTour // or the best insertion tour has changed
            ) {
                // Evaluate insertion costs and update best request overall
                eval.add(new InsertionEvaluator(r));
            } else {
                // Update best request overall
                mUpdateLock.lock();
                try {
                    if (mBestInsOverall == null
                            || mBestInsOverall.getCost() > mInsCostMatrix[r][mBestInsTour[r]].getCost()) {
                        mBestInsOverall = mBestInsMove[r];
                        mBestInsTourOverall = mBestInsTour[r];
                    }
                } finally {
                    mUpdateLock.unlock();
                }
            }
        }

        try {
            // Evaluate all requests in parallel
            Map<InsertionEvaluator, Future<?>> future = mExecutor.executeBatch(eval, true);
            // Log exceptions if any
            for (Future<?> f : future.values()) {
                try {
                    f.get();
                } catch (ExecutionException e) {
                    Logging.getBaseLogger().exception("pInsertion.executeAllAgents", e);
                }
            }
        } catch (InterruptedException e) {
            // Wait for evaluation to finish
            Logging.getBaseLogger().exception("pInsertion.executeAllAgents", e);
        } finally {
        }

    }

    /**
     * <code>InsertionEvaluator</code> is a {@link Runnable} that evaluates the insertion cost of a route in all
     * scenarios
     * <p>
     * Creation date: May 17, 2011 - 5:46:26 PM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    private class InsertionEvaluator implements Runnable {
        private final int mRequest;

        public InsertionEvaluator(int request) {
            super();
            mRequest = request;
        }

        @Override
        public void run() {
            int bestTour = -1;
            InsertionMove bestIns = null;

            // Select the tour cost delegate
            // We do this to ensure we have good insertions within a tour
            CostDelegate costDelegate = mSolution.getCostDelegate() instanceof TourBalanceDelegate ? ((TourBalanceDelegate) mSolution
                    .getCostDelegate()).getTourCostDelegate() : mSolution.getCostDelegate();

            // Evaluate all tours in the first iteration, and only the last modified tour otherwise
            // int tmin = mLastInsTour < 0 ? 0 : mLastInsTour;
            // int tmax = mLastInsTour < 0 ? mSolution.getTourCount() : mLastInsTour + 1;
            // for (int t = tmin; t < tmax; t++) {
            for (int t = 0; t < mSolution.getTourCount(); t++) {

                // Evaluate the insertion cost of the given request in tour t
                InsertionMove ins = InsertionMove.bestInsertion(mRequest, mSolution.getTour(t), costDelegate,
                        getConstraintHandler(), GlobalParameters.CTR_CHK_FWD_FEAS);

                if (!ins.isFeasible())
                    // The move cannot be inserted in this tour, skip
                    continue;

                // Reevaluate the insertion if needed to evaluate the insertion for the whole solution
                if (mSolution.getCostDelegate() instanceof TourBalanceDelegate) {
                    mSolution.getCostDelegate().evaluateMove(ins);
                }

                // Store the value
                mInsCostMatrix[mRequest][t] = ins;
                // Compare against best tour
                if (bestIns == null || ins.getCost() < bestIns.getCost()) {
                    // Update best tour
                    bestTour = t;
                    bestIns = ins;
                }
            }
            // Store best tour
            mBestInsTour[mRequest] = bestTour;
            mBestInsMove[mRequest] = bestIns;

            if (bestTour == -1)
                // No feasible was found
                return;

            // Update best request overall
            mUpdateLock.lock();
            try {
                if (mBestInsOverall == null || mBestInsOverall.getCost() > mInsCostMatrix[mRequest][bestTour].getCost()) {
                    mBestInsOverall = mBestInsMove[mRequest];
                    mBestInsTourOverall = bestTour;
                }
            } finally {
                mUpdateLock.unlock();
            }
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    protected void finalize() throws Throwable {
        mExecutor.shutdownNow();
        super.finalize();
    }

    @Override
    public void initialize(IInstance instance) {
        // Do nothing
    }

    @Override
    public String getName() {
        return "bestIns";
    }

    @Override
    public void dispose() {
        mInsCostMatrix = null;
        mBestInsTour = null;
        mBestInsMove = null;
        mExecutor.shutdownNow();
        mExecutor = null;
    }
}
