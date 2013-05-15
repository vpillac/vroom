/**
 *
 */
package vroom.trsp.optimization.constructive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import vroom.common.heuristics.ProcedureStatus;
import vroom.common.heuristics.vns.VariableNeighborhoodSearch;
import vroom.common.heuristics.vns.VariableNeighborhoodSearch.VNSVariant;
import vroom.common.utilities.BatchThreadPoolExecutor;
import vroom.common.utilities.Stopwatch;
import vroom.common.utilities.Stopwatch.ReadOnlyStopwatch;
import vroom.common.utilities.algorithms.HungarianAlgorithm;
import vroom.common.utilities.dataModel.ObjectWithIdComparator;
import vroom.common.utilities.optimization.ILocalSearch;
import vroom.common.utilities.optimization.IParameters.LSStrategy;
import vroom.common.utilities.optimization.OptimizationSense;
import vroom.common.utilities.optimization.SimpleParameters;
import vroom.trsp.datamodel.ITRSPTour;
import vroom.trsp.datamodel.TRSPDetailedSolutionChecker;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPRequest;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.datamodel.TRSPTour;
import vroom.trsp.datamodel.Technician;
import vroom.trsp.datamodel.costDelegates.TRSPCostDelegate;
import vroom.trsp.optimization.InsertionMove;
import vroom.trsp.optimization.constraints.TourConstraintHandler;
import vroom.trsp.optimization.localSearch.TRSPShift;
import vroom.trsp.optimization.localSearch.TRSPTwoOpt;
import vroom.trsp.util.TRSPGlobalParameters;
import vroom.trsp.util.TRSPLogging;

/**
 * <code>TRSPpInsertion</code>
 * <p>
 * Creation date: Mar 23, 2011 - 2:12:18 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TRSPpInsertion extends TRSPConstructiveHeuristic {

    public static enum AssignmentStrategy {
        AP, SEQUENTIAL, MIXED
    };

    private static int     sMaxBacktracking       = 100;

    /**
     * a flag to toggle the reproducibility of experiments: if set to <code>false</code> algorithm may be faster but
     * result may change from one run to another.
     * <p>
     * Default value is <code>true</code>
     * </p>
     **/
    private static boolean sEnsureReproducibility = true;

    /**
     * A flag to toggle the reproducibility of experiments: if set to <code>false</code> algorithm may be faster but
     * result may change from one run to another
     * 
     * @return the value of name
     */
    public static boolean isEnsureReproducibility() {
        return sEnsureReproducibility;
    }

    /**
     * Setter for the flag to toggle the reproducibility of experiments: if set to <code>false</code> algorithm may be
     * faster but result may change from one run to another
     * 
     * @param ensureRep
     *            the value to be set for a flag to toggle the reproducibility of experiments: if set to
     *            <code>false</code> algorithm may be faster but result may change from one run to another
     */
    public static void setEnsureReproducibility(boolean ensureRep) {
        sEnsureReproducibility = ensureRep;
    }

    /** A flag to toggle mSolution checking after each move */
    private static boolean sCheckSolutionAfterMove = false;

    /**
     * Setter for <code>checkSolutionAfterMove</code>
     * 
     * @param checkSolutionAfterMove
     *            the checkSolutionAfterMove to set
     */
    public static void setCheckSolutionAfterMove(boolean checkSolutionAfterMove) {
        sCheckSolutionAfterMove = checkSolutionAfterMove;
        if (isCheckSolutionAfterMove()) {
            TRSPLogging
                    .getOptimizationLogger()
                    .warn("TRSPpInsertion.CheckSolutionAfterMove is set to true, set to false to increase performance (set in %s)",
                            Thread.currentThread().getStackTrace()[2]);
        }
    }

    /**
     * Getter for <code>checkSolutionAfterMove</code>
     * 
     * @return the checkSolutionAfterMove
     */
    public static boolean isCheckSolutionAfterMove() {
        return sCheckSolutionAfterMove;
    }

    /** the strategy used for assignment rounds **/
    private AssignmentStrategy mAssignmentStrategy;

    /**
     * Getter for the strategy used for assignment rounds
     * 
     * @return the strategy used for assignment rounds
     */
    public AssignmentStrategy getAssignmentStrategy() {
        return this.mAssignmentStrategy;
    }

    /**
     * Setter for the strategy used for assignment rounds
     * 
     * @param strategy
     *            the strategy used for assignment rounds
     */
    public void setAssignmentStrategy(AssignmentStrategy strategy) {
        this.mAssignmentStrategy = strategy;
    }

    /** The set of available requests */
    private Set<TRSPRequest> mUnassignedRequests;

    /**
     * Returns the set of available requests
     * 
     * @return the set of available requests
     */
    public Set<TRSPRequest> getAvailableRequests() {
        return mUnassignedRequests;
    }

    /** the set of infeasible requests **/
    // private Set<TRSPRequest> mInfeasibleRequests;

    // /**
    // * Getter for the set of infeasible requests
    // *
    // * @return the set of infeasible requests
    // */
    // public Set<TRSPRequest> getUnfeasibleRequests() {
    // return this.mInfeasibleRequests;
    // }

    /** A timer used to measure time spent on backtracking */
    private final Stopwatch mBTTimer;

    /** A timer used to measure time spent on request evaluation */
    private final Stopwatch mEvalTimer;

    /** A timer used to measure time spent on AP assignment */
    private final Stopwatch mAPAssignmentTimer;

    /** A timer used to measure time spent on sequential assignment */
    private final Stopwatch mSeqAssignmentTimer;

    /** A timer used to measure time spent on post optimization */
    private final Stopwatch mPostOptTimer;

    /**
     * Getter for the backtracking timer
     * 
     * @return the backtracking timer
     */
    public ReadOnlyStopwatch getBTTimer() {
        return mBTTimer.getReadOnlyStopwatch();
    }

    /**
     * Getter for the evaluation timer
     * 
     * @return the evaluation timer
     */
    public ReadOnlyStopwatch getEvalTimer() {
        return mEvalTimer.getReadOnlyStopwatch();
    }

    /**
     * Getter for the AP assignment timer
     * 
     * @return the assignment timer
     */
    public ReadOnlyStopwatch getAPAssignmentTimer() {
        return mAPAssignmentTimer.getReadOnlyStopwatch();
    }

    /**
     * Getter for the sequential assignment timer
     * 
     * @return the sequential assignment timer
     */
    public ReadOnlyStopwatch getSeqAssignmentTimer() {
        return mSeqAssignmentTimer.getReadOnlyStopwatch();
    }

    /**
     * Getter for the post optimization timer
     * 
     * @return the post optimization timer
     */
    public ReadOnlyStopwatch getPostOpTimer() {
        return mPostOptTimer.getReadOnlyStopwatch();
    }

    /** An array containing the agents: one per tour */
    private InsertionAgent[]           mAgents;
    /** A sorted copy of mAgents */
    private InsertionAgent[]           mSortedAgents;
    /** An executor that will execute the agent tasks */
    private BatchThreadPoolExecutor    mExecutor;

    /**
     * A list containing the assigned requests in the same order they were assigned
     */
    private final LinkedList<Decision> mOrderedDecisions;

    /** An array containing the number of times a move was unassigned */
    private final int[]                mUnassignedCount;

    /**
     * A set containing the requests that were unassigned in the backtracking process
     */
    private Set<TRSPRequest>           mBacktrackUnassignedRequests;

    /** The number of times backtrack was used */
    private int                        mBacktrackCount = 0;

    /** The best solution found so far in terms of number of served requests */
    private TRSPSolution               mBestSolution;

    @Override
    protected void setSolution(TRSPSolution solution) {
        super.setSolution(solution);
        mAgents = new InsertionAgent[getInstance().getFleet().size()];
        mSortedAgents = new InsertionAgent[getInstance().getFleet().size()];
        for (Technician t : getInstance().getFleet()) {
            mAgents[t.getID()] = new InsertionAgent(this, t, getSolution().getTour(t.getID()));
            mSortedAgents[t.getID()] = mAgents[t.getID()];
        }
    }

    /** The cost of the solution before post optimization */
    private double mInitSolCost = 0;

    /**
     * Returns the cost of the solution before post optimization
     * 
     * @return The cost of the solution before post optimization
     */
    public double getInitSolCost() {
        return mInitSolCost;
    }

    /**
     * Creates a new <code>TRSPpInsertion</code>
     * 
     * @param costDelegate
     *            the cost delegate that will be used to evaluate moves
     * @param constraintHandler
     *            the constraint handler that will be used to evaluate the feasibility of moves
     */
    public TRSPpInsertion(TRSPInstance instance, TRSPGlobalParameters parameters,
            TourConstraintHandler constraintHandler, TRSPCostDelegate costDelegate) {
        super(instance, parameters, constraintHandler, costDelegate);

        setNumThreads(Runtime.getRuntime().availableProcessors());

        mAPAssignmentTimer = new Stopwatch();
        mSeqAssignmentTimer = new Stopwatch();
        mBTTimer = new Stopwatch();
        mEvalTimer = new Stopwatch();
        mPostOptTimer = new Stopwatch();

        setAssignmentStrategy(AssignmentStrategy.MIXED);

        if (isEnsureReproducibility()) {
            // Tree sets are used in order to ensure reproducibility
            // (reproducible foreach)
            mUnassignedRequests = new TreeSet<TRSPRequest>(new ObjectWithIdComparator());
            // mInfeasibleRequests = new TreeSet<TRSPRequest>(new
            // ObjectWithIdComparator());
            mBacktrackUnassignedRequests = new TreeSet<TRSPRequest>(new ObjectWithIdComparator());
        } else {
            // Hash sets are used for performance (O(1) add/remove/contains)
            mUnassignedRequests = new HashSet<TRSPRequest>();
            // mInfeasibleRequests = new HashSet<TRSPRequest>();
            mBacktrackUnassignedRequests = new HashSet<TRSPRequest>();
        }
        for (Integer r : getInstance().getReleasedRequests()) {
            mUnassignedRequests.add(instance.getRequest(r));
        }

        mOrderedDecisions = new LinkedList<Decision>();
        mUnassignedCount = new int[getInstance().getMaxId()];
    }

    @SuppressWarnings("unchecked")
    protected ILocalSearch<ITRSPTour> getTourLS() {
        return VariableNeighborhoodSearch.<ITRSPTour> newVNS(VNSVariant.VND, OptimizationSense.MINIMIZATION, null,
                getParameters().getALNSRndStream(), new TRSPShift(getTourConstraintHandler()), new TRSPTwoOpt(
                        getTourConstraintHandler()));
    }

    /**
     * Returns a string describing the context (current state) of the ALNS
     * 
     * @return a string describing the context (current state) of the ALNS
     */
    public String getContextString() {
        return String.format("pIns[t:%-5sms it:%-5s]: ", getTimer().readTimeMS(), mIteration);
    }

    @Override
    protected void initializeSolutionInternal(TRSPSolution sol) {
        reset();

        mBestSolution = getSolution().clone();

        mTimer.start();
        TRSPLogging.getOptimizationLogger().info(
                getContextString() + "Optimization started with %s threads (%-4s pending requests)",
                mExecutor.getMaximumPoolSize(), mUnassignedRequests.size());
        boolean reqAssigned = true;
        boolean backtrack = false;

        // Check for feasibility: all requests should be compatible with at
        // least one empty tour
        // ----------------------------------------------------------
        Set<TRSPRequest> unfeasibleRequests = new HashSet<TRSPRequest>(mUnassignedRequests);
        executeAllAgents();
        for (TRSPRequest r : mUnassignedRequests) {
            for (InsertionAgent a : mAgents) {
                if (a.getMove(r).isFeasible()) {
                    unfeasibleRequests.remove(r);
                    break;
                }
            }
        }
        if (!unfeasibleRequests.isEmpty()) {
            TRSPLogging.getOptimizationLogger().warn(
                    getContextString() + "Some requests cannot be inserted in any tour: %s", unfeasibleRequests);

            setStatus(ProcedureStatus.INFEASIBLE);
        }
        // ----------------------------------------------------------

        // Assign all requests that are feasible with only one technician
        // ----------------------------------------------------------

        // ----------------------------------------------------------

        // Build tours
        // ----------------------------------------------------------
        while (!mUnassignedRequests.isEmpty() && getStatus() == ProcedureStatus.RUNNING) {
            mIteration++;
            TRSPLogging.getOptimizationLogger().lowDebug(
                    getContextString() + "Start of iteration %-4s, pending requests: %-4s", mIteration,
                    mUnassignedRequests.size());

            // Try to assign at most 1 move per technician
            reqAssigned = assignmentRound(backtrack);

            // No assignment was made but there still are unassigned requests:
            // backtrack
            if (reqAssigned && backtrack) {
                TRSPLogging.getOptimizationLogger().debug(
                        getContextString() + "Backtraking was successfull - %s unassigned requests",
                        mUnassignedRequests.size());
                backtrackSucceeded();
                backtrack = false;
            } else if (!reqAssigned && !mUnassignedRequests.isEmpty()) {
                TRSPLogging.getOptimizationLogger().debug(
                        getContextString() + "No feasible assignment was found, backtraking - %s unassigned requests",
                        mUnassignedRequests.size());
                backtrack();
                backtrack = true;
            }

            if (getSolution().getUnservedCount() < mBestSolution.getUnservedRequests().size()
                    || (getSolution().getUnservedCount() == mBestSolution.getUnservedCount() && getSolution()
                            .getObjectiveValue() < mBestSolution.getObjectiveValue()))
                mBestSolution = getSolution().clone();

            TRSPLogging.getOptimizationLogger().lowDebug(
                    getContextString() + "End   of iteration %-4s, unassigned requests: %-4s, total cost: %-4.2f",
                    mIteration, mUnassignedRequests.size(), getSolution().getObjectiveValue());
        }
        // ----------------------------------------------------------

        setSolution(mBestSolution);

        // Local search
        // ----------------------------------------------------------
        postOptimization();
        // ----------------------------------------------------------

        mTimer.stop();

        if (mUnassignedRequests.isEmpty()) {
            if (getStatus() == ProcedureStatus.RUNNING) // Prevent status
                                                        // overwriting
                setStatus(ProcedureStatus.TERMINATED);

        } else {
            TRSPLogging.getOptimizationLogger().warn(getContextString() + "Some requests have been left over: %s",
                    mUnassignedRequests);
            if (getStatus() == ProcedureStatus.RUNNING) // Prevent status
                                                        // overwriting
                setStatus(ProcedureStatus.INFEASIBLE_SOLUTION);
        }

        TRSPLogging.getOptimizationLogger().info(
                getContextString()
                        + "Optimization finished after %sit and %sms, unserved requests: %-4s, total cost: %-4.2f",
                mIteration, mTimer.readTimeMS(), getSolution().getUnservedCount(), getSolution().getObjectiveValue());

        mExecutor.shutdown();

    }

    private void reset() {
        mTimer.reset();
        mAPAssignmentTimer.reset();
        mSeqAssignmentTimer.reset();
        mEvalTimer.reset();
        mBTTimer.reset();

        int proc = getParameters().getThreadCount();
        mExecutor = new BatchThreadPoolExecutor(proc, "pIns");
        mIteration = 0;
    }

    /**
     * Try to assign requests to technicians
     * 
     * @param backtrack
     *            <code>true</code> if a backtrack was performed
     * @return <code>true</code> if at least one assignment was made, <code>false</code> otherwise
     */
    private boolean assignmentRound(boolean backtrack) {
        // FIXME abort if at least one request is infeasible
        switch (getAssignmentStrategy()) {
        case AP:
            return assignmentRoundAP();
        case SEQUENTIAL:
            return assignmentRoundSeq();
        case MIXED:
            if (backtrack)
                return assignmentRoundSeq();
            else
                return assignmentRoundAP();
        default:
            throw new IllegalStateException("Unknown assignment strategy: " + getAssignmentStrategy());
        }
    }

    /**
     * Try to assign one request to the technician currently with the lowest load.
     * 
     * @return <code>true</code> if one assignment was made, <code>false</code> otherwise
     */
    private boolean assignmentRoundSeq() {
        mSeqAssignmentTimer.resume();
        boolean reqAssigned = false;

        TRSPLogging.getOptimizationLogger().lowDebug(
                getContextString() + "Trying to assign the following requests: %s", mUnassignedRequests);

        // Sort agents
        Arrays.sort(mSortedAgents);

        // The current agent
        int a = 0;
        while (!reqAssigned && a < mSortedAgents.length) {
            // Select the agent with the smallest cost
            InsertionAgent agent = mSortedAgents[a];
            // Evaluate insertion costs
            mEvalTimer.resume();
            agent.call();
            mEvalTimer.pause();

            // Select the best insertion
            InsertionMove mve = agent.getBestMove();

            if (mve != null)
                reqAssigned = executeMove(mve, agent);
            if (!reqAssigned)
                a++;
        }
        mSeqAssignmentTimer.pause();
        return reqAssigned;
    }

    /**
     * Check if all requests can be inserted in at least one tour
     * 
     * @return <code>true</code> if the current partial solution is feasible
     */
    private boolean isPartialSolFeasible() {
        for (TRSPRequest r : mUnassignedRequests) {
            boolean feasible = false;
            for (int i = 0; !feasible && i < mAgents.length; i++) {
                InsertionMove move = mAgents[i].getMove(r);
                if (move != null)
                    feasible = true;
            }
            if (!feasible)
                return false;
        }
        return true;
    }

    /**
     * Try to assign requests to technicians in a parallel manner by solving an assignment problem
     * 
     * @return <code>true</code> if at least one assignment was made, <code>false</code> otherwise
     */
    private boolean assignmentRoundAP() {
        mAPAssignmentTimer.resume();
        boolean reqAssigned = false;

        TRSPLogging.getOptimizationLogger().lowDebug(
                getContextString() + "Trying to assign the following requests: %s", mUnassignedRequests);

        executeAllAgents();
        if (!isPartialSolFeasible())
            return false;

        // Translate the agent move evaluations into a penalty matrix
        int size = Math.max(mAgents.length, mUnassignedRequests.size());
        double[][] penalties = new double[size][size];
        TRSPRequest[] requests = new TRSPRequest[size];
        double maxEval = Double.NEGATIVE_INFINITY;
        int k = 0;
        for (TRSPRequest r : mUnassignedRequests) {
            requests[k] = r;
            for (int i = 0; i < mAgents.length; i++) {
                InsertionMove move = mAgents[i].getMove(r);
                penalties[i][k] = move.getCost();
                if (penalties[i][k] != Double.POSITIVE_INFINITY && penalties[i][k] > maxEval)
                    maxEval = penalties[i][k];
            }
            k++;
        }
        if (maxEval == Double.NEGATIVE_INFINITY) {
            // mInfeasibleRequests.addAll(mUnassignedRequests);
            // No coherent evaluation was found, abort
            mAPAssignmentTimer.pause();
            return false;
        }

        for (int i = 0; i < mAgents.length; i++) {
            for (int j = 0; j < mUnassignedRequests.size(); j++) {
                if (penalties[i][j] == Double.POSITIVE_INFINITY)
                    penalties[i][j] = 100 * maxEval;
            }
        }

        // Solve the assignment problem
        int[] assignment = HungarianAlgorithm.solveAssignment(penalties);
        for (int i = 0; i < mAgents.length; i++) {
            InsertionAgent agent = mAgents[i];
            InsertionMove move = agent.getMove(requests[assignment[i]]);
            if (move != null) { // Check if an assignment was found
                reqAssigned |= executeMove(move, agent);
            }
        }

        mAPAssignmentTimer.pause();
        return reqAssigned;
    }

    /**
     * Performs a local search on each technician tour
     */
    private void postOptimization() {
        mInitSolCost = getSolution().getObjectiveValue();
        TRSPLogging.getOptimizationLogger().debug(getContextString() + "Start of post optimization, cost: %.3f",
                getSolution().getObjectiveValue());
        mPostOptTimer.resume();

        final SimpleParameters params = new SimpleParameters(LSStrategy.DET_BEST_IMPROVEMENT, Long.MAX_VALUE,
                Integer.MAX_VALUE, getParameters().getALNSRndStream());
        List<Callable<Object>> optJobs = new ArrayList<Callable<Object>>(mAgents.length);
        for (final InsertionAgent a : mAgents) {
            optJobs.add(new Callable<Object>() {
                @Override
                public Object call() {
                    TRSPLogging.getOptimizationLogger().lowDebug(getContextString() + "optimizing tour %s", a);
                    a.mTour = (TRSPTour) getTourLS().localSearch(getInstance(), a.mTour, params);
                    getSolution().importTour(a.mTour);
                    return new Object();
                }
            });
        }
        try {
            // Evaluate all requests in parallel
            Map<Callable<Object>, Future<Object>> future = mExecutor.submitBatch(optJobs, true);
            // Log exceptions if any
            for (Future<?> f : future.values()) {
                try {
                    f.get();
                } catch (ExecutionException e) {
                    TRSPLogging.getBaseLogger().exception("TRSPpInsertion.postOptimization", e);
                }
            }
        } catch (InterruptedException e) {
            // Wait for evaluation to finish
            TRSPLogging.getBaseLogger().exception("TRSPpInsertion.postOptimization", e);
        } finally {
            mEvalTimer.pause();
        }
        mPostOptTimer.pause();
        double imp = getSolution().getObjectiveValue() - mInitSolCost;
        TRSPLogging.getOptimizationLogger().debug(
                getContextString() + "Optimization finished after %sms, cost: %.3f (improvement: %.3f [%.1f])",
                mPostOptTimer.readTimeMS(), getSolution().getObjectiveValue(), imp, 100 * imp / mInitSolCost);

    }

    /**
     * Execute all the agents
     */
    private void executeAllAgents() {
        mEvalTimer.resume();
        try {
            // Evaluate all requests in parallel
            Map<InsertionAgent, Future<Boolean>> future = mExecutor.submitBatch(Arrays.asList(mAgents), true);
            // Log exceptions if any
            for (Future<?> f : future.values()) {
                try {
                    f.get();
                } catch (ExecutionException e) {
                    TRSPLogging.getBaseLogger().exception("TRSPpInsertion.executeAllAgents", e);
                }
            }
        } catch (InterruptedException e) {
            // Wait for evaluation to finish
            TRSPLogging.getBaseLogger().exception("TRSPpInsertion.executeAllAgents", e);
        } finally {
            mEvalTimer.pause();
        }
    }

    /**
     * Execute an {@link InsertionMove} with a specific {@link InsertionAgent}, log messages and store the decision
     * information for eventual backtracking
     * 
     * @param move
     *            the move to be executed
     * @param agent
     *            the agent that will execute the move
     * @return <code>true</code> if the insertion was successful, <code>false</code> otherwise
     */
    private boolean executeMove(InsertionMove move, InsertionAgent agent) {
        if (!move.isFeasible())
            return false;

        // if (!mUnassignedRequests.contains(move.getRequest()))
        // throw new IllegalArgumentException("Request " + move.getRequest()
        // + " is not in the list of unassigned requests");
        if (agent.executeMove(move)) {
            TRSPLogging.getOptimizationLogger().lowDebug(getContextString() + "Assigned %s to agent %s",
                    move.getNodeId(), agent);
            mUnassignedRequests.remove(getInstance().getRequest(move.getNodeId()));
            // mInfeasibleRequests.remove(move.getRequest());
            // TODO store more information on the assignment decision for better
            // backtracking
            mOrderedDecisions.addLast(new Decision(move, agent));
            return true;
        } else {
            // mInfeasibleRequests.add(move.getRequest());
            TRSPLogging.getOptimizationLogger().lowDebug(getContextString() + "Cannot assign %s to agent %s",
                    move.getNodeId(), agent);
            return false;
        }
    }

    /**
     * Performs a backtrack by removing a subset of requests from tours
     */
    private void backtrack() {
        if (mOrderedDecisions.isEmpty()) {
            setStatus(ProcedureStatus.EXCEPTION);
            TRSPLogging
                    .getOptimizationLogger()
                    .warn(getContextString()
                            + "Attempting to backtrack when no decision can be undone - aborting (%s unassigned requests: %s)",
                            mUnassignedRequests.size(), mUnassignedRequests);
            return;
        }

        mBTTimer.resume();
        int count = 0;
        mBacktrackCount++;
        LinkedList<Decision> bt = new LinkedList<TRSPpInsertion.Decision>();
        while (count < Math.max(mAgents.length / 2 * mBacktrackCount, mUnassignedRequests.size() * mBacktrackCount)
                && !mOrderedDecisions.isEmpty()) {
            // Gets the earliest assignment
            Decision decision = mOrderedDecisions.pop();
            if (mUnassignedCount[decision.move.getNodeId()] > sMaxBacktracking) {
                setStatus(ProcedureStatus.LIMIT_ITERATION);
                TRSPLogging.getOptimizationLogger().warn(
                        getContextString() + "Procedure aborted after request %s was removed %s times",
                        decision.move.getNodeId(), mUnassignedCount[decision.move.getNodeId()]);
                mBTTimer.pause();
                return;
            }

            decision.agent.undoMove(decision.move);
            mBacktrackUnassignedRequests.add(getInstance().getRequest(decision.move.getNodeId()));
            mUnassignedCount[decision.move.getNodeId()]++;
            bt.add(decision);
            // mUnassignedRequests.add(assignment.request); We don't want to
            // reassign the move right away
            count++;
            // Clear the stored information to allow re-evaluation
            for (InsertionAgent a : mAgents) {
                a.clear(getInstance().getRequest(decision.move.getNodeId()));
            }
        }
        TRSPLogging.getOptimizationLogger().lowDebug(getContextString() + "%s revoked assignments: %s", count, bt);
        mBTTimer.pause();
    }

    /**
     * Performs post-backtrack operations in the case backtrack was successful
     */
    private void backtrackSucceeded() {
        mBacktrackCount = 0;
        mUnassignedRequests.addAll(mBacktrackUnassignedRequests);
        mBacktrackUnassignedRequests.clear();
    }

    /**
     * <code>InsertionAgent</code> is the class responsible for the evaluation of requests in the parallel insertion
     * heuristic.
     * <p>
     * Creation date: Mar 24, 2011 - 1:49:51 PM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp" >SLP</a>
     * @version 1.0
     */
    private class InsertionAgent implements Callable<Boolean>, Comparable<InsertionAgent> {
        private final TRSPpInsertion  mController;

        private final InsertionMove[] mMoves;

        private InsertionMove         mBestMove;

        private final Technician      mTechnician;
        private TRSPTour              mTour;

        /**
         * A flag equal to <code>true</code> if the associated tour was modified since last iteration
         */
        private boolean               mChanged;

        /**
         * Creates a new <code>InsertionAgent</code>
         * 
         * @param controller
         */
        private InsertionAgent(TRSPpInsertion controller, Technician technician, TRSPTour tour) {
            super();
            mController = controller;
            mTechnician = technician;
            mTour = tour;

            mMoves = new InsertionMove[tour.getInstance().getMaxId()];
            mChanged = true;
        }

        /**
         * Clear all stored information
         */
        protected void clear() {
            for (int i = 0; i < mMoves.length; i++) {
                mMoves[i] = null;
            }
            mBestMove = null;
            mChanged = true;
        }

        /**
         * Clear the stored data associated with the given request
         * 
         * @param req
         */
        protected void clear(TRSPRequest req) {
            mMoves[req.getID()] = null;
        }

        @Override
        public Boolean call() {
            // Evaluate requests
            mBestMove = null;
            if (mChanged)
                clear();
            for (TRSPRequest req : mController.getAvailableRequests()) {
                InsertionMove mve = getMove(req);
                if (mChanged || mve == null) {
                    mve = evaluateRequest(req);
                    mMoves[req.getID()] = mve;
                }
                if (mBestMove == null || mBestMove.getImprovement() < mve.getImprovement())
                    mBestMove = mve;
            }
            mChanged = false;
            return mChanged;
        }

        /**
         * Evaluates the cost of visiting the given move in this tour.
         * <p>
         * The mEvaluation takes into account possible trips to a central depot
         * </p>
         * 
         * @param req
         *            the move to be evaluated
         * @return the cost of visiting the given move in this tour
         */
        private InsertionMove evaluateRequest(TRSPRequest req) {
            return InsertionMove.findInsertion(req.getID(), mTour, getCostDelegate(), getTourConstraintHandler(),
                    TRSPGlobalParameters.CTR_CHK_FWD_FEAS, true);
        }

        /**
         * Returns the {@link InsertionMove} corresponding to a move
         * 
         * @param req
         *            the considered move
         * @return the {@link InsertionMove} associated with the specified move
         */
        protected InsertionMove getMove(TRSPRequest req) {
            return req != null ? mMoves[req.getID()] : null;
        }

        /**
         * Getter for the best move found by this agent
         * 
         * @return the best move found by this agent
         */
        protected InsertionMove getBestMove() {
            return mBestMove;
        }

        /**
         * Execute an insertion move
         * 
         * @param move
         *            the insertion to be executed
         * @return <code>true</code> if the insertion was executed successfully, <code>false</code> otherwise
         */
        protected boolean executeMove(InsertionMove move) {
            if (move.getTour() != mTour)
                throw new IllegalArgumentException("The move tour should be the agent tour");

            double prev = mTour.getTotalCost();
            boolean executed = InsertionMove.executeMove(move);
            if (executed) {
                checkSolution(prev, move, false);
                mChanged = true;
            }
            return executed;

        }

        /**
         * Undo an insertion move
         * 
         * @param move
         *            the move that will be undone
         */
        protected void undoMove(InsertionMove move) {
            double prev = mTour.getTotalCost();

            // if (!mTour.isVisited(move.getRequestId()))
            // throw new IllegalArgumentException("The tour " + this +
            // " does not visit request " + move.getRequest());
            mTour.removeNode(move.getNodeId());
            mTour.getSolution().markAsUnserved(move.getNodeId());
            if (move.isDepotTrip() && mTour.isMainDepotVisited()) {
                // Check if a trip to the depot is still required
                if (!mTour.isVisitToMainDepotRequired())
                    mTour.removeNode(mTour.getMainDepotId());
            }

            mChanged = true;

            checkSolution(prev, move, true);
        }

        @Override
        public String toString() {
            return mTour.toString();
        }

        private void checkSolution(double prev, InsertionMove move, boolean undo) {
            if (isCheckSolutionAfterMove()) {
                String mod = undo ? "undoing " : "";
                String stack = undo ? "undoMove" : "executeMove";
                String err = TRSPDetailedSolutionChecker.INSTANCE.checkTour(mTour);
                if (!err.isEmpty()) {
                    TRSPLogging.getOptimizationLogger().warn(
                            "TRSPpInsertion.InsertionAgent.%s: Incoherencies in tour after %smove %s (%s)", stack, mod,
                            move, err);
                }

                err = TRSPpInsertion.this.getTourConstraintHandler().getInfeasibilityExplanation(mTour);
                if (err != null) {
                    TRSPLogging.getOptimizationLogger().warn(
                            "TRSPpInsertion.InsertionAgent.%s: Infeasible solution after %smove %s (%s)", stack, mod,
                            move, err);
                }

                double dif = prev + move.getCost() - mTour.getTotalCost();
                if (!undo && Math.abs(dif) > 1e-3) {
                    TRSPLogging
                            .getOptimizationLogger()
                            .warn("TRSPpInsertion.InsertionAgent.%s: Unexpected change in cost after %smove %s in tour %s (expected %.3f, was %.3f, delegate:%s)",
                                    stack, mod, move, mTechnician.getID(), move.getCost(), mTour.getTotalCost() - prev,
                                    mController.getCostDelegate().getClass().getSimpleName());
                }
            }
        }

        @Override
        public int compareTo(InsertionAgent o) {
            return Double.compare(this.mTour.getTotalCost(), o.mTour.getTotalCost());
        }
    }

    /**
     * <code>Decision</code> is a container class to store information on an assignment
     * <p>
     * Creation date: Mar 29, 2011 - 3:54:05 PM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp" >SLP</a>
     * @version 1.0
     */
    private static class Decision {
        private final InsertionMove  move;
        private final InsertionAgent agent;

        /**
         * Creates a new <code>Decision</code>
         * 
         * @param move
         * @param agent
         */
        public Decision(InsertionMove move, InsertionAgent agent) {
            super();
            this.move = move;
            this.agent = agent;
        }

        @Override
        public String toString() {
            return String.format("r:%s->t:%s", move.getNodeId(), agent.mTechnician.getID());
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (mExecutor != null)
            mExecutor.shutdownNow();
        mExecutor = null;
    }

    @Override
    protected void finalize() throws Throwable {
        mExecutor.shutdown();
        super.finalize();
    }
}
