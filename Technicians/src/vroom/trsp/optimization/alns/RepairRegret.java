/**
 *
 */
package vroom.trsp.optimization.alns;

import java.util.ConcurrentModificationException;

import vroom.common.heuristics.alns.IDestroy.IDestroyResult;
import vroom.common.heuristics.alns.IRepair;
import vroom.common.utilities.Utilities;
import vroom.common.utilities.optimization.IInstance;
import vroom.common.utilities.optimization.IParameters;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.datamodel.costDelegates.NoisyCostDelegate;
import vroom.trsp.datamodel.costDelegates.TRSPCostDelegate;
import vroom.trsp.datamodel.costDelegates.TRSPTourBalance;
import vroom.trsp.optimization.InsertionMove;
import vroom.trsp.optimization.constraints.TourConstraintHandler;
import vroom.trsp.util.TRSPGlobalParameters;
import vroom.trsp.util.TRSPLogging;

/**
 * <code>RepairBestInsertion</code> is a sequential implementation of the <em>regret-q insertion</em> presented in:
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
 * @version 2.0
 */
public class RepairRegret implements IRepair<TRSPSolution> {

    private final TRSPGlobalParameters mParams;

    /** the regret level **/
    private final int                  mLevel;

    /**
     * Getter for the regret level, e.g 2 for the regret-2.
     * 
     * @return the regret level
     */
    public int getLevel() {
        return this.mLevel;
    }

    /** the noise flag **/
    private final boolean mNoise;
    private double        mMaxNoise;

    /**
     * Getter for the noise flag
     * 
     * @return {@code true} if noise is added to the objective function
     */
    public boolean isNoiseEnabled() {
        return this.mNoise;
    }

    private boolean                     mBusy = false;

    /** The currently optimized solution */
    TRSPSolution                        mSolution;

    /** A matrix containing the insertion cost of each request in each tour */
    InsertionMove[][]                   mInsMatrix;
    /** An array containing the regret value of each request */
    Double[]                            mRegretValues;
    /** An array containing the best insertion of each request */
    InsertionMove[]                     mBestIns;
    /** The tour in which a request was inserted last */
    int                                 mLastInsTour;

    /** A constraint handler for this repair component **/
    private final TourConstraintHandler mConstraintHandler;

    /**
     * Getter for the constraint handler
     * 
     * @return A constraint handler for this repair component
     */
    public TourConstraintHandler getConstraintHandler() {
        return this.mConstraintHandler;
    }

    /**
     * Creates a new <code>RepairBestInsertion</code>
     * 
     * @param params
     *            the global parameters
     * @param constraintHandler
     *            the constraint handler to use
     * @param level
     *            the level <em>q</em> of the <em>regret-q</em> heuristic
     * @param noise
     *            {@code true} if noise should be added to the objective function
     */
    public RepairRegret(TRSPGlobalParameters params, TourConstraintHandler constraintHandler,
            int level, boolean noise) {
        super();
        mConstraintHandler = constraintHandler;
        mLevel = level;

        mParams = params;

        mNoise = noise;
    }

    @Override
    public void initialize(IInstance instance) {
        if (isNoiseEnabled()) {
            mMaxNoise = mParams.get(TRSPGlobalParameters.ALNS_REP_ETA)
                    * ((TRSPInstance) instance).getCostDelegate().getMaxDistance()
                    / ((TRSPInstance) instance).getFleet().getVehicle().getSpeed();
        } else {
            mMaxNoise = 0;
        }
    }

    @Override
    public boolean repair(TRSPSolution solution, IDestroyResult<TRSPSolution> destroyResult,
            IParameters params) {
        if (mBusy)
            throw new ConcurrentModificationException("This instance is already in use");
        mBusy = true;

        mSolution = solution;

        mLastInsTour = -1;

        mBestIns = new InsertionMove[solution.getInstance().getMaxId()];
        mRegretValues = new Double[solution.getInstance().getMaxId()];
        mInsMatrix = new InsertionMove[solution.getInstance().getMaxId()][];
        for (int r : solution.getUnservedRequests())
            mInsMatrix[r] = new InsertionMove[solution.getTourCount()];

        while (!solution.getUnservedRequests().isEmpty()) {
            // Evaluate the insertion cost matrix
            evaluateInsCostMatrix(params);

            InsertionMove bestInsOverall = selectBestInsertion();

            // Execute the best insertion
            if (bestInsOverall != null) {
                boolean inserted = InsertionMove.executeMove(bestInsOverall);
                if (inserted) {
                    TRSPLogging.getOptimizationLogger().lowDebug(
                            "RepairBestInsertion.repair: insertion successfull - %s",
                            bestInsOverall);

                    // Remove the request from the set of pending requests
                    solution.markAsServed(bestInsOverall.getNodeId());

                    // Store the id of the last modified tour
                    mLastInsTour = bestInsOverall.getTour().getTechnicianId();

                    // Clear stored data
                    mInsMatrix[bestInsOverall.getNodeId()] = null;
                    mBestIns[bestInsOverall.getNodeId()] = null;
                    mRegretValues[bestInsOverall.getNodeId()] = null;
                } else {
                    TRSPLogging.getOptimizationLogger().lowDebug(
                            "RepairBestInsertion.repair: insertion failed - %s", bestInsOverall);
                    break;
                }
            } else {
                break;
            }

        }

        if (!solution.getUnservedRequests().isEmpty()) {
            TRSPLogging
                    .getOptimizationLogger()
                    .lowDebug(
                            "RepairBestInsertion.repair: Unable to repair the solution, unfeasible requests: %s",
                            solution.getUnservedRequests());
        }

        mBusy = false;
        return solution.getUnservedRequests().isEmpty();
    }

    /**
     * Selects the best insertion among all candidates
     * 
     * @return the best insertion among all candidates
     */
    protected InsertionMove selectBestInsertion() {
        InsertionMove bestIns = null;
        double bestRegret = 0;

        for (int r : mSolution.getUnservedRequests()) {
            if (mRegretValues[r] == null) {
                // Get the q best insertions
                InsertionMove[] bests = Utilities.Math.max(mInsMatrix[r], getLevel());

                if (bests.length == 0) {
                    // No feasible insertion was found
                    mBestIns[r] = null;
                    mRegretValues[r] = null;
                    continue;
                }

                // Store the best insertion
                mBestIns[r] = bests[bests.length - 1];

                // Evaluate the regret
                if (getLevel() == 1)
                    mRegretValues[r] = -mBestIns[r].getCost();
                else {
                    mRegretValues[r] = 0d;
                    for (int i = 0; i < bests.length - 1; i++) {
                        mRegretValues[r] += bests[i].getCost() - mBestIns[r].getCost();
                    }
                }
            }

            // Select the best insertion overall
            if (bestIns == null || mRegretValues[r] > bestRegret || // Better insertion found
                    (mRegretValues[r] == bestRegret && mBestIns[r].getCost() < bestIns.getCost())) {// Resolve ties
                bestIns = mBestIns[r];
                bestRegret = mRegretValues[r];
            }
        }

        return bestIns;
    }

    /**
     * Evaluates the insertion cost matrix stored in {@link #mInsMatrix} and updates the best insertion for each request
     * {@link #mBestInsTour} and overall {@link #mBestInsOverall}
     */
    void evaluateInsCostMatrix(IParameters params) {
        for (int req : mSolution.getUnservedRequests()) {
            // Select the tour cost delegate
            // We do this to ensure we have good insertions within a tour
            TRSPCostDelegate costDelegate = TRSPTourBalance.class.isInstance(mSolution
                    .getCostDelegate()) ? ((TRSPTourBalance) mSolution.getCostDelegate())
                    .getTourCostDelegate() : mSolution.getCostDelegate();
            if (isNoiseEnabled())
                costDelegate = new NoisyCostDelegate(costDelegate, params.getRandomStream(),
                        mMaxNoise);

            // Evaluate all tours in the first iteration, and only the last modified tour otherwise
            int tmin = mLastInsTour < 0 ? 0 : mLastInsTour;
            int tmax = mLastInsTour < 0 ? mSolution.getTourCount() : mLastInsTour + 1;
            for (int t = tmin; t < tmax; t++) {
                // Evaluate the insertion cost of the considered request in tour t
                InsertionMove ins = InsertionMove.findInsertion(req, mSolution.getTour(t),
                        costDelegate, getConstraintHandler(),
                        TRSPGlobalParameters.CTR_CHK_FWD_FEAS, true);

                if (!ins.isFeasible()) {
                    if (mInsMatrix[req][t] != null) {
                        // The insertion used to be feasible, the best insertion and regret value may have changed
                        mBestIns[req] = null;
                        mRegretValues[req] = null;
                    }

                    // The move cannot be inserted in this tour
                    mInsMatrix[req][t] = null;
                } else {
                    // Reevaluate the insertion if needed to evaluate the insertion for the whole solution
                    if (mSolution.getCostDelegate() instanceof TRSPTourBalance) {
                        double scdImp = ins.getImprovement();
                        mSolution.getCostDelegate().evaluateMove(ins);
                        // The initial improvement is stored as secondary improvement
                        ins.setSecondaryImprovement(scdImp);
                    }

                    // Store the value
                    mInsMatrix[req][t] = ins;

                    // The best insertion and regret value are likely to have changed
                    mBestIns[req] = null;
                    mRegretValues[req] = null;
                }
            }
        }
    }

    @Override
    public String toString() {
        return String.format("%s[%s]", getName(), getLevel());
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    @Override
    public String getName() {
        return String.format("regret-%s%s", getLevel(), isNoiseEnabled() ? "-n" : "");
    }

    @Override
    public void dispose() {
        mInsMatrix = null;
        mRegretValues = null;
        mBestIns = null;
    }

    @Override
    public RepairRegret clone() {
        return new RepairRegret(mParams, mConstraintHandler, mLevel, false);
    }
}
