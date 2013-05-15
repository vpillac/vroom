package vroom.trsp.optimization;

import java.util.concurrent.Callable;

import vroom.common.heuristics.ProcedureStatus;
import vroom.common.utilities.IDisposable;
import vroom.common.utilities.Stopwatch;
import vroom.common.utilities.Stopwatch.ReadOnlyStopwatch;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.costDelegates.TRSPCostDelegate;
import vroom.trsp.optimization.constraints.TourConstraintHandler;
import vroom.trsp.util.TRSPGlobalParameters;
import vroom.trsp.util.TRSPLogging;

/**
 * <code>TRSPHeuristic</code> is the parent class for heuristics for the TRSP. It contains a reference to the parent
 * instance, a random stream, global parameters, among others.
 * <p>
 * Creation date: Sep 22, 2011 - 1:59:08 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public abstract class TRSPHeuristic implements Runnable, Callable<ProcedureStatus>, IDisposable {

    private final TRSPGlobalParameters mParameters;

    /**
     * Returns the parameters used for this heuristic
     * 
     * @return the parameters used for this heuristic
     */
    public TRSPGlobalParameters getParameters() {
        return mParameters;
    }

    /** the current instance **/
    private final TRSPInstance mInstance;

    /**
     * Getter for the current instance
     * 
     * @return the value of instance
     */
    public TRSPInstance getInstance() {
        return this.mInstance;
    }

    /** this heuristic status **/
    private ProcedureStatus mStatus;

    /**
     * Getter for this heuristic status
     * 
     * @return the value of status
     */
    public ProcedureStatus getStatus() {
        return this.mStatus;
    }

    /**
     * Setter for this heuristic status
     * 
     * @param status
     *            the value to be set for this heuristic status
     * @throws IllegalStateException
     *             if <code>status=</code>{@link ProcedureStatus.RUNNING RUNNING} and the heuristic is already running
     */
    protected void setStatus(ProcedureStatus status) {
        if (status == ProcedureStatus.RUNNING && this.mStatus == ProcedureStatus.RUNNING)
            throw new IllegalStateException("Heuristic is already running");
        this.mStatus = status;
    }

    /**
     * Returns <code>true</code> if this heuristic is currently running, <code>false</code> otherwise
     * 
     * @return <code>true</code> if this heuristic is currently running, <code>false</code> otherwise
     */
    public boolean isRunning() {
        return getStatus().isRunning();
    }

    /**
     * Check if the instance is properly defined and the heuristic is not already running
     */
    protected void checkState() {
        if (getInstance() == null)
            throw new IllegalStateException("Heuristic cannot be run if instance is not set");
        if (isRunning())
            throw new IllegalStateException("Heuristic is already running");
    }

    /** A timer used to measure time in this procedure */
    protected final Stopwatch mTimer;

    /**
     * Getter for the main timer
     * 
     * @return the main timer
     */
    public ReadOnlyStopwatch getTimer() {
        return mTimer.getReadOnlyStopwatch();
    }

    /** The current iteration count */
    protected int mIteration = 0;

    /**
     * Returns the current number of iterations
     * 
     * @return the current number of iterations
     */
    public int getIterationCount() {
        return mIteration;
    }

    /** the tour constraint handler for this heuristic **/
    protected final TourConstraintHandler mTourConstraintHandler;

    /**
     * Getter for the tour constraint handler for this heuristic
     * 
     * @return the tour constraint handler
     */
    public TourConstraintHandler getTourConstraintHandler() {
        return this.mTourConstraintHandler;
    }

    /** a cost delegate used to evaluate insertion costs **/
    protected final TRSPCostDelegate mCostDelegate;

    /**
     * Getter for a cost delegate used to evaluate insertion costs
     * 
     * @return the cost delegate
     */
    public TRSPCostDelegate getCostDelegate() {
        return this.mCostDelegate;
    }

    /** the maximum number of threads that will be used **/
    private int mNumThreads;

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
        this.mNumThreads = numThreads;
    }

    /**
     * Creates a new <code>TRSPHeuristic</code>
     * 
     * @param constraintHandler
     * @param costDelegate
     */
    public TRSPHeuristic(TRSPInstance instance, TRSPGlobalParameters parameters,
            TourConstraintHandler constraintHandler, TRSPCostDelegate costDelegate) {
        mInstance = instance;
        mParameters = parameters;
        mCostDelegate = costDelegate;
        mTourConstraintHandler = constraintHandler;
        this.mTimer = new Stopwatch();
        setNumThreads(mParameters.getThreadCount());
    }

    @Override
    public void run() {
        setStatus(ProcedureStatus.RUNNING);
        try {
            setStatus(call());
        } catch (Exception e) {
            TRSPLogging.getOptimizationLogger().exception("TRSPConstructiveHeuristic.run", e);
            setStatus(ProcedureStatus.EXCEPTION);
        }
    }

    @Override
    public void dispose() {
        mStatus = null;
    }

}