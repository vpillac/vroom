package vroom.trsp;

import java.text.DecimalFormat;
import java.util.concurrent.Callable;

import vroom.common.heuristics.ProcedureStatus;
import vroom.common.utilities.BestKnownSolutions;
import vroom.common.utilities.IDisposable;
import vroom.common.utilities.StatCollector;
import vroom.common.utilities.StatCollector.Label;
import vroom.common.utilities.Stopwatch;
import vroom.common.utilities.Stopwatch.ReadOnlyStopwatch;
import vroom.common.utilities.gurobi.GRBModelWriter;
import vroom.trsp.datamodel.CVRPTWSolutionChecker;
import vroom.trsp.datamodel.GroerSolutionHasher;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.datamodel.TRSPSolutionChecker;
import vroom.trsp.datamodel.TRSPSolutionCheckerBase;
import vroom.trsp.datamodel.costDelegates.TRSPCostDelegate;
import vroom.trsp.optimization.constraints.SolutionConstraintHandler;
import vroom.trsp.optimization.constraints.TourConstraintHandler;
import vroom.trsp.optimization.matheuristic.TRSPGRBStatCollector;
import vroom.trsp.util.TRSPGlobalParameters;

public abstract class TRSPSolver implements Callable<TRSPSolution>, IDisposable {

    /** this heuristic status **/
    private ProcedureStatus mStatus;

    /**
     * Getter for this solver status
     * 
     * @return the value of status
     */
    public ProcedureStatus getStatus() {
        return this.mStatus;
    }

    /**
     * Setter for this solver status
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

    public static final DecimalFormat COST_FORMAT = new DecimalFormat("###0.0000");
    public static final DecimalFormat TIME_FORMAT = new DecimalFormat("###0.000");
    public static final DecimalFormat PERC_FORMAT = new DecimalFormat("###0.000000");

    private final TRSPInstance        mInstance;

    /**
     * Returns the instance being solved by this solver
     * 
     * @return the instance being solved by this solver
     */
    public TRSPInstance getInstance() {
        return mInstance;
    }

    private final SolutionConstraintHandler mSolCtrHandler;

    /**
     * Returns the solution constraint handler used in this run
     * 
     * @return the solution constraint handler used in this run
     */
    public SolutionConstraintHandler getSolCtrHandler() {
        return mSolCtrHandler;
    }

    /**
     * Returns the tour constraint handler used in this run
     * 
     * @return the tour constraint handler used in this run
     */
    public TourConstraintHandler getTourCtrHandler() {
        return mSolCtrHandler.getTourConstraintHandler();
    }

    /**
     * Returns the cost delegate used to evaluate the final solution
     * 
     * @return the cost delegate used to evaluate the final solution
     */
    public TRSPCostDelegate getSolCostDelegate() {
        return getParams().newSCCostDelegate();
    }

    private final TRSPGlobalParameters mParams;

    /**
     * Getter for <code>params</code>
     * 
     * @return the params
     */
    public TRSPGlobalParameters getParams() {
        return mParams;
    }

    //
    // @Override
    // public RandomStream getRandomStream() {
    // return getParams().getRandomStream();
    // }
    //
    // @Override
    // public void setRandomStream(RandomStream stream) {
    // getParams().setRandomStream(stream);
    // }

    /**
     * Set the initial solution for this solver (optional)
     * 
     * @param sol
     *            the initial solution to set
     */
    public void setInitSol(TRSPSolution sol) {
        throw new UnsupportedOperationException();
    }

    private TRSPSolution mFinalSol;

    /**
     * Returns the final solution found by this solver
     * 
     * @return the final solution found by this solver
     */
    public TRSPSolution getFinalSolution() {
        return mFinalSol;
    }

    /**
     * Sets the final solution found by this solver
     * 
     * @param postOpSol
     *            the final solution found by this solver
     */
    protected void setFinalSolution(TRSPSolution postOpSol) {
        mFinalSol = postOpSol;
    }

    protected final TRSPSolutionCheckerBase mChecker;

    /**
     * Returns the solution check used to check the solutions
     * 
     * @return the solution check used to check the solutions
     */
    public TRSPSolutionCheckerBase getChecker() {
        return mChecker;
    }

    /** an optional comment used in output **/
    private String mComment = "";

    /**
     * Getter for an optional comment used in outpu
     * 
     * @return an optional comment used in outpu
     */
    public String getComment() {
        return this.mComment;
    }

    /**
     * Setter for an optional comment used in outpu
     * 
     * @param comment
     *            the value to be set for an optional comment used in outpu
     */
    public void setComment(String comment) {
        this.mComment = comment;
    }

    /** the main timer used by this solver **/
    private final Stopwatch mTimer;

    /**
     * Getter for the main timer used by this solver
     * 
     * @return the value of name
     */
    public ReadOnlyStopwatch getTimer() {
        return this.mTimer.getReadOnlyStopwatch();
    }

    /**
     * Getter for the main timer used by this solver
     * 
     * @return the value of name
     */
    protected Stopwatch getTimerInternal() {
        return this.mTimer;
    }

    /** the writer used to write models to file **/
    private GRBModelWriter mModelWriter = null;

    /**
     * Setter for the writer used to write models to file
     * 
     * @param writer
     *            the value to be set for the writer used to write models to file
     */
    public void setModelWriter(GRBModelWriter writer) {
        this.mModelWriter = writer;
    }

    /**
     * Returns the writer used to write models to file
     * 
     * @return the writer used to write models to file
     */
    public GRBModelWriter getModelWriter() {
        return mModelWriter;
    }

    /** the stat collector for the gurobi post optimization */
    private TRSPGRBStatCollector mGRBStatCollector = null;

    /**
     * Returns the stat collector for the gurobi post optimization
     * 
     * @return the stat collector for the gurobi post optimization
     */
    public TRSPGRBStatCollector getGRBStatCollector() {
        return mGRBStatCollector;
    }

    /**
     * Sets the stat collector for the gurobi post optimization
     * 
     * @param statCollector
     *            the stat collector for the gurobi post optimization
     */
    public void setGRBStatCollector(TRSPGRBStatCollector statCollector) {
        mGRBStatCollector = statCollector;
    }

    /**
     * Creates a new <code>ALNSSCSolver</code>
     * 
     * @param instance
     *            the instance to be solved
     * @param params
     *            the global parameters that will be used in this run
     * @param rndStream
     *            the random stream that will be used by this solver
     * @param simulator
     *            the simulator being used (can be null)
     */
    public TRSPSolver(TRSPInstance instance, TRSPGlobalParameters params) {
        mParams = params;

        mInstance = instance;

        if (params.isCVRPTW()) {
            getInstance().setMainDepotTripAllowed(false);
        }

        // Set the solution hasher
        getInstance().setSolutionHasher(
                new GroerSolutionHasher(getInstance(), getParams().getHashRndStream()));

        mSolCtrHandler = SolutionConstraintHandler.newConstraintHandler(getInstance());

        mChecker = params.isCVRPTW() ? CVRPTWSolutionChecker.INSTANCE
                : TRSPSolutionChecker.INSTANCE;

        mTimer = new Stopwatch();
        setStatus(ProcedureStatus.INITIALIZED);
    }

    public abstract Label<?>[] getLabels();

    public abstract Object[] getStats(BestKnownSolutions bks, int runId, int runNum);

    /**
     * Collects the stats
     * 
     * @param col
     *            the stat collector in which stats will be colletced
     * @param bks
     *            the best known solution
     * @param runId
     *            the unique id of this run
     * @param runNum
     *            the run number
     */
    public void collectStats(StatCollector col, BestKnownSolutions bks, int runId, int runNum) {
        col.collect(getStats(bks, runId, runNum));
    }

    /**
     * Returns a string containing the stats for this execution
     * 
     * @param col
     *            the stat collector in which stats will be colletced
     * @param bks
     *            the best known solution
     * @param runId
     *            the id of this run
     * @return a string containing the stats for this execution
     */
    public String getStatString(StatCollector col, BestKnownSolutions bks, int runId) {
        return col.getSatString(getStats(bks, runId, runId));
    }

    @Override
    public abstract TRSPSolution call();
}