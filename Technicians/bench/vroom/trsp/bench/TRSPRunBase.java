/**
 *
 */
package vroom.trsp.bench;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import vroom.common.heuristics.alns.DiversifiedPool;
import vroom.common.modeling.io.DynamicPersistenceHelper;
import vroom.common.utilities.BestKnownSolutions;
import vroom.common.utilities.StatCollector;
import vroom.common.utilities.StatCollector.Label;
import vroom.common.utilities.Stopwatch;
import vroom.common.utilities.Utilities;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.common.utilities.logging.Logging;
import vroom.common.utilities.optimization.IConstraint;
import vroom.common.utilities.params.ParameterExperimentDesign.ExperimentParameterSetting;
import vroom.common.utilities.params.ParameterKey;
import vroom.trsp.ALNSSCSolver;
import vroom.trsp.DynBiObjSolver;
import vroom.trsp.TRSPSolver;
import vroom.trsp.datamodel.HashTourPool;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPRequest;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.datamodel.TRSPSolutionChecker;
import vroom.trsp.datamodel.TRSPTour;
import vroom.trsp.datamodel.costDelegates.TRSPDistance;
import vroom.trsp.datamodel.costDelegates.TRSPLevenshteinDistance;
import vroom.trsp.datamodel.costDelegates.TRSPTourBalance;
import vroom.trsp.datamodel.costDelegates.TRSPWorkingTime;
import vroom.trsp.io.DynamicTRSPPersistenceHelper;
import vroom.trsp.io.ITRSPPersistenceHelper;
import vroom.trsp.optimization.InsertionMove;
import vroom.trsp.optimization.TRSPUtilities;
import vroom.trsp.optimization.alns.RepairRegret;
import vroom.trsp.optimization.alns.TRSPDestroyResult;
import vroom.trsp.optimization.constraints.ServicedRequestsConstraint;
import vroom.trsp.optimization.constructive.TRSPpInsertion;
import vroom.trsp.optimization.mpa.DTRSPSolution;
import vroom.trsp.optimization.mpa.DTRSPSolution.DTRSPTour;
import vroom.trsp.sim.TRSPSimulator;
import vroom.trsp.util.BrokenPairsDistance;
import vroom.trsp.util.TRSPGlobalParameters;
import vroom.trsp.util.TRSPLogging;

/**
 * <code>TRSPRunBase</code> is a benchmarking class used to test {@link TRSPpInsertion}
 * <p>
 * Creation date: Apr 8, 2011 - 4:38:48 PM.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TRSPRunBase implements Callable<TRSPSolution> {

    public static final String     CFG_TRSP          = "./config/bench/bench_trsp_palnssc_25crew.cfg";
    public static final String     CFG_DTRSP         = "./config/bench/bench_dtrsp_palns_25crew.cfg";
    public static final String     CFG_VRPTW         = "./config/bench/bench_cvrptw_palnssc.cfg";
    public static final String     CFG_DVRPTW        = "./config/bench/bench_dvrptw_lackner_pALNS.cfg";
    public static final String     CFG_BiDVRPTW      = "./config/bench/bench_dvrptw_lackner_pBiALNS.cfg";

    public static final File       PAUSE_LOCK_FILE   = new File("./run_pause.lock");

    /** The labels for stat collection in the dynamic case. */
    public final static Label<?>[] DYN_LABELS        = new Label<?>[] { //
                                                     new Label<Integer>("run_id", Integer.class),
            new Label<String>("name", String.class), // name
            new Label<String>("group", String.class), // group
            new Label<Integer>("size", Integer.class), // size
            new Label<Integer>("crew", Integer.class), // crew
            new Label<String>("dod", String.class), // run #
            new Label<Integer>("run", Integer.class), // run #
            new Label<String>("comment", String.class), // commentc
            new Label<Boolean>("terminated", Boolean.class), // terminated flag
            new Label<String>("times", String.class), // times
            new Label<Double>("final_cost", Double.class), // final solution cost
            new Label<Double>("final_dur", Double.class), // final solution duration
            new Label<Double>("final_dis", Double.class), // final solution distance
            new Label<Double>("final_bal", Double.class), // final solution balance
            new Label<Double>("bks", Double.class), // bks value
            new Label<Double>("static_cost", Double.class), // a-posteriori solution cost
            new Label<Double>("static_dur", Double.class), // a-posteriori solution duration
            new Label<Double>("static_dis", Double.class), // a-posteriori solution distance
            new Label<Double>("static_bal", Double.class), // a-posteriori solution balance
            new Label<Double>("vi_cost", Double.class), // vi for cost
            new Label<Integer>("static_num_req", Integer.class), // number of request in the a-posteriori solution
            new Label<Integer>("rejected_count", Integer.class), //
            new Label<String>("rejected", String.class), //
            new Label<String>("seeds", String.class), // seeds
            new Label<String>("final_sol", String.class), // the detailed
                                                          // solution
                                                     };

    /** The labels for stat collection in the biobjective dynamic case. */
    @SuppressWarnings("rawtypes")
    public final static Label<?>[] DYN_BI_OBJ_LABELS = new Label<?>[] { //
                                                     new Label<Integer>("run_id", Integer.class),
            new Label<String>("name", String.class), // name
            new Label<String>("group", String.class), // group
            new Label<Integer>("size", Integer.class), // size
            new Label<Integer>("crew", Integer.class), // crew
            new Label<String>("dod", String.class), // run #
            new Label<Integer>("run", Integer.class), // run #
            new Label<String>("comment", String.class), // comment
            new Label<Boolean>("terminated", Boolean.class), // terminated flag
            new Label<List>("times", List.class), // times
            new Label<Double>("init_cost", Double.class), // initial solution cost
            new Label<Integer>("init_num_req", Integer.class), // number of request in the initial solution
            new Label<Double>("final_cost", Double.class), // final solution cost
            new Label<Integer>("final_num_req", Integer.class), // number of request in the final solution
            new Label<Integer>("final_dist_init", Integer.class), // distance relative to the initial solution
            new Label<Double>("static_cost", Double.class), // a-posteriori solution cost
            new Label<Double>("vi_cost", Double.class), // vi for cost
            new Label<Integer>("static_num_req", Integer.class), // number of request in the a-posteriori solution
            new Label<Integer>("static_dist_init", Integer.class), // distance relative to the initial solution
            new Label<Double>("bks_cost", Double.class), // bks value
            new Label<Integer>("rejected_count", Integer.class), //
            new Label<String>("rejected", String.class), //
            new Label<String>("seeds", String.class), // seeds
            new Label<String>("final_sol", String.class), // the detailed
                                                          // solution
                                                     };

    // public static final String INSTANCES_PATH = "../Instances/trsp/toy";
    // public static final String INSTANCES_PATH = "../Instances/trsp/pillac";
    // public static final String INSTANCES_PATH = "../Instances/trsp/cvrptw";
    // public static final boolean sCVRPTW = INSTANCES_PATH.contains("cvrptw");

    /**
     * Returns the array of labels for which stats will be collected in {@link #collectStats(StatCollector, boolean)}.
     * 
     * @param params
     *            the params
     * @return the array of labels for which stats will be collected
     * @see #collectStats(StatCollector, boolean)
     */
    public static Label<?>[] getDefaultLabels(TRSPGlobalParameters params) {
        if (params.isBiObjective())
            return DYN_BI_OBJ_LABELS;
        else if (params.isDynamic())
            return DYN_LABELS;

        Class<?> solver = params.get(TRSPGlobalParameters.RUN_SOLVER);
        try {
            return (Label<?>[]) solver.getField("LABELS").get(null);
        } catch (IllegalArgumentException e) {
            TRSPLogging.getBaseLogger().exception("TRSPRunBase.getDefaultLabels", e);
        } catch (SecurityException e) {
            TRSPLogging.getBaseLogger().exception("TRSPRunBase.getDefaultLabels", e);
        } catch (IllegalAccessException e) {
            TRSPLogging.getBaseLogger().exception("TRSPRunBase.getDefaultLabels", e);
        } catch (NoSuchFieldException e) {
            TRSPLogging.getBaseLogger().exception("TRSPRunBase.getDefaultLabels", e);
        }

        return null;
    }

    /**
     * Returns the labels that should be used to collect stats for this run
     * 
     * @return the labels that should be used to collect stats for this run
     */
    public Label<?>[] getLabels() {
        if (getParameters().isBiObjective())
            return DYN_BI_OBJ_LABELS;
        else if (getParameters().isDynamic())
            return DYN_LABELS;
        else
            return getSolver().getLabels();
    }

    /** The Bks. */
    private final BestKnownSolutions           mBks;

    /** The Solver. */
    private TRSPSolver                         mSolver;

    /** The Parameters. */
    private final TRSPGlobalParameters         mParameters;

    /** The Run. */
    private final int                          mRun;

    /** The unique id of this run */
    private final int                          mRunId;

    /** The Instance. */
    private TRSPInstance                       mInstance;

    /** The initial solution */
    private TRSPSolution                       mInitSolution;

    /** The static a-posteriori solution */
    private TRSPSolution                       mStaticSolution;

    /** The final solution (at the end of the simulation) */
    private TRSPSolution                       mFinalSolution;

    /** The Times. */
    private List<Integer>                      mTimes;

    /** The Comment. */
    private final String                       mComment;

    /** a boolean flag indicating if the run terminated correctly */
    private boolean                            mTerminated;

    private RepairRegret                       mRepairOperator;

    /** saved values to switch between dynamic and static setup */
    private final Map<ParameterKey<?>, Object> mSavedStaticValues;

    /**
     * Gets the instance
     * 
     * @return the instance
     */
    public TRSPInstance getInstance() {
        return mInstance;
    }

    /**
     * Gets the comment.
     * 
     * @return the comment
     */
    public String getComment() {
        return mComment;
    }

    /**
     * Gets the run number
     * 
     * @return the run number
     */
    public int getRun() {
        return mRun;
    }

    /**
     * Gets the run unique id
     * 
     * @return the run unique id
     */
    public int getRunId() {
        return mRunId;
    }

    /**
     * Gets the simulator.
     * 
     * @return the simulator
     */
    public TRSPSimulator getSimulator() {
        return getInstance().getSimulator();
    }

    /**
     * Gets the solver.
     * 
     * @return the solver
     */
    public TRSPSolver getSolver() {
        return mSolver;
    }

    /**
     * Returns the parameters used in this run
     * 
     * @return the parameters used in this run
     */
    public TRSPGlobalParameters getParameters() {
        return mParameters;
    }

    /**
     * Returns {@code true} if the run terminated correctly
     * 
     * @return {@code true} if the run terminated correctly
     */
    public boolean isTerminated() {
        return mTerminated;
    }

    /**
     * Returns the BKS instance
     * 
     * @return the BKS instance
     */
    public BestKnownSolutions getBKS() {
        return mBks;
    }

    /**
     * Return the best known solution for a particular instance, or {@link Double#NaN} if the BKS is not known
     * 
     * @param instance
     * @return the BKS for the {@code  instance}
     */
    public Double getBKS(String instance) {
        if (getBKS() != null) {
            Double bks = getBKS().getBKS(getInstance().getName());
            return bks != null ? bks : Double.NaN;
        }
        return Double.NaN;
    }

    /**
     * Returns the static solution resulting from an a-posteriori run in a dynamic context
     * 
     * @return the static solution resulting from an a-posteriori run
     */
    public TRSPSolution getStaticSolution() {
        return mStaticSolution;
    }

    /**
     * Returns the final solution found by the solver
     * 
     * @return the final solution found by the solver
     */
    public TRSPSolution getFinalSolution() {
        return mFinalSolution;
    }

    protected void setFinalSolution(TRSPSolution finalSolution) {
        mFinalSolution = finalSolution;
    }

    protected void setStaticSolution(TRSPSolution mStaticSolution) {
        this.mStaticSolution = mStaticSolution;
    }

    /**
     * Returns the initial solution
     * 
     * @return the initial solution
     */
    public TRSPSolution getInitSolution() {
        return mInitSolution;
    }

    private void setInitSolution(TRSPSolution initSolution) {
        mInitSolution = initSolution;
    }

    /** the {@link ExperimentParameterSetting} used in this run (can be {@code null}) **/
    private ExperimentParameterSetting<TRSPGlobalParameters> mExpeSetting;

    /**
     * Getter for the {@link ExperimentParameterSetting} used in this run (can be {@code null})
     * 
     * @return the {@link ExperimentParameterSetting} used in this run
     */
    public ExperimentParameterSetting<TRSPGlobalParameters> getExpeSetting() {
        return this.mExpeSetting;
    }

    /**
     * Creates a new <code>TRSPRunBase</code>.
     * 
     * @param runId
     *            the run unique id
     * @param instance
     *            the instance that will be solved
     * @param params
     *            the global parameters
     * @param bks
     *            the best known solutions
     * @param run
     *            the run
     */
    public TRSPRunBase(Integer runId, TRSPInstance instance, TRSPGlobalParameters params,
            BestKnownSolutions bks, Integer run) {
        this(runId, instance, params, bks, run, "");
    }

    /**
     * Creates a new <code>TRSPRunBase</code>.
     * 
     * @param runId
     *            the run unique id
     * @param instance
     *            the instance that will be solved
     * @param params
     *            the global parameters
     * @param bks
     *            the best known solutions
     * @param run
     *            the run id
     * @param comment
     *            a comment for this run
     */
    public TRSPRunBase(Integer runId, TRSPInstance instance, TRSPGlobalParameters params,
            BestKnownSolutions bks, Integer run, String comment) {
        mRunId = runId;
        mInstance = instance;

        mInstanceFile = null;
        mRDFile = null;

        mParameters = params;
        mRun = run;

        mBks = bks;
        mComment = comment;

        mSolver = getParameters().newInstance(TRSPGlobalParameters.RUN_SOLVER, getInstance(),
                getParameters());
        if (params.isDynamic() && getInstance().getSimulator() == null)
            getInstance().setupSimulator(getSolver().getSolCostDelegate(), params);
        mSolver.setComment(mComment);
        mSavedStaticValues = new HashMap<ParameterKey<?>, Object>();
    }

    private final File mInstanceFile;
    private final File mRDFile;

    /**
     * Creates a new <code>TRSPRunBase</code> without reading the instance
     * 
     * @param runId
     * @param instanceFile
     * @param rdFile
     * @param params
     * @param bks
     * @param run
     * @param comment
     */
    public TRSPRunBase(Integer runId, File instanceFile, File rdFile, TRSPGlobalParameters params,
            BestKnownSolutions bks, Integer run, String comment) {
        mRunId = runId;
        mParameters = params;
        mRun = run;

        mInstanceFile = instanceFile;
        mRDFile = rdFile;

        mBks = bks;
        mComment = comment;

        mSavedStaticValues = new HashMap<ParameterKey<?>, Object>();
    }

    /**
     * Return {@code true} if this run has been initialized
     * 
     * @return
     */
    public boolean isInitialized() {
        return mInstance != null;
    }

    /**
     * Initialize this run
     * 
     * @throws Exception
     */
    public void initialize() throws Exception {
        if (mInstance != null)
            throw new IllegalStateException("This run is already initialized");

        ITRSPPersistenceHelper reader = TRSPUtilities.getPersistenceHelper(mInstanceFile.getPath());

        if (mRDFile.exists()) {
            mInstance = reader.readInstance(mInstanceFile, getParameters().isCVRPTW());
            DynamicTRSPPersistenceHelper.readRelDates(mInstance, mRDFile, getParameters()
                    .isCVRPTW());
        }

        // Set the force fxd slack time flag
        mInstance.setForceFwdSlackTime(getParameters().get(TRSPGlobalParameters.FORCE_FWD_SLK));

        mSolver = getParameters().newInstance(TRSPGlobalParameters.RUN_SOLVER, getInstance(),
                getParameters());
        mSolver.setComment(mComment);
        if (getParameters().isDynamic() && getInstance().getSimulator() == null)
            getInstance().setupSimulator(getSolver().getSolCostDelegate(), getParameters());
    }

    /*
     * (non-Javadoc)
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public TRSPSolution call() throws Exception {
        return call(false);
    }

    /**
     * Execute this run.
     * 
     * @param abortAfterInit
     *            the abort after init
     * @return the solution found {@code true} if the run should be aborted after the initialization and the initial
     *         solution should be returned (in the dynamic case only)
     */
    public TRSPSolution call(boolean abortAfterInit) throws Exception {
        if (!isInitialized())
            initialize();

        mTerminated = false;

        awaitUnpause();

        Stopwatch timer = new Stopwatch();
        timer.start();
        TRSPLogging.getRunLogger().info("TRSPRun.call %s: Instance %s (%s) run %s",
                TRSPBench.getInstance().getProgress(), getInstance().getName(), mComment, mRun);

        TRSPSolution sol;
        if (getInstance().isDynamic())
            sol = dynamicRun(abortAfterInit);
        else
            sol = staticRun();

        timer.stop();

        TRSPBench.getInstance().getProgress().iterationFinished();
        TRSPLogging.getRunLogger().info("TRSPRun.call %s: Instance %s (%s) run %s finished in %s",
                TRSPBench.getInstance().getProgress(), mSolver.getInstance().getName(), mComment,
                mRun, timer.readTimeString());

        mTerminated = true;
        return sol;
    }

    /**
     * Wait until the pause file is removed (if present)
     */
    protected void awaitUnpause() {
        boolean first = true;
        while (PAUSE_LOCK_FILE.exists()) {
            if (first) {
                TRSPLogging
                        .getRunLogger()
                        .info("TRSPRun.call %s: Instance %s (%s) run %s - Waiting for the lock file to be deleted (%s)",
                                TRSPBench.getInstance().getProgress(), getInstance().getName(),
                                mComment, mRun, PAUSE_LOCK_FILE);
                first = false;
                System.gc();
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Set dynamic param values
     */
    private void dynamicSetup() {
        for (Entry<ParameterKey<?>, Object> e : getParameters().getParametersMapping()) {
            if (e.getKey().getName().startsWith("DYN_")) {
                // Save the static value
                ParameterKey<?> sk = getParameters().getRegisteredKey(
                        e.getKey().getName().substring(4));
                mSavedStaticValues.put(sk, getParameters().get(sk));
                // Set the dynamic value
                getParameters().setNoCheck(sk, e.getValue());
            }
        }
    }

    /**
     * Restore the saved static param values
     */
    private void staticSetup() {
        for (Entry<ParameterKey<?>, Object> e : mSavedStaticValues.entrySet()) {
            // Restore the static value
            getParameters().setNoCheck(e.getKey(), e.getValue());
        }
    }

    /**
     * Dynamic run.
     * 
     * @param abortAfterInit
     *            the abort after init
     * @return the tRSP solution
     */
    protected DTRSPSolution dynamicRun(boolean abortAfterInit) {
        final boolean biobj = getParameters().isBiObjective();
        final boolean enablePR = getParameters().get(TRSPGlobalParameters.BIOBJ_ENABLE_PR);
        if (biobj)
            ((DynBiObjSolver) getSolver()).setBiObjective(false);

        mTimes = new ArrayList<Integer>(getSimulator().getUnreleasedCount());

        mRepairOperator = new RepairRegret(getParameters(), mSolver.getTourCtrHandler(), 1, false);

        Stopwatch runTimer = new Stopwatch();

        // Initial solution
        getParameters().set(TRSPGlobalParameters.BIOBJ_ENABLE_PR, false);
        runTimer.start();
        DTRSPSolution sol = (DTRSPSolution) staticRun();
        setInitSolution(sol);
        runTimer.stop();
        mTimes.add((int) runTimer.readTimeMS());
        getParameters().set(TRSPGlobalParameters.BIOBJ_ENABLE_PR, enablePR);

        // Abort the procedure and return the initial solution
        if (abortAfterInit)
            return sol;

        if (biobj) {
            ((DynBiObjSolver) getSolver()).setBiObjective(true);
            ((DynBiObjSolver) getSolver()).setReferenceSol(sol);
        }

        // Dynamic setup
        dynamicSetup();

        sol = sol.clone();
        while (getSimulator().hasUnreleasedRequests()) {
            // Get the released request(s)
            Collection<TRSPRequest> release = getSimulator().nextRelease();
            getSimulator().updateState(sol);

            ArrayList<Integer> relIds = new ArrayList<Integer>();
            for (TRSPRequest r : release) {
                relIds.add(r.getID());
                sol.markAsUnserved(r.getID());
            }

            TRSPLogging.getRunLogger().debug(
                    "TRSPRun.call %s:  [sim time:%-5s] Added %s new requet %s - Sim:%s - %s",
                    TRSPBench.getInstance().getProgress(),
                    getInstance().getSimulator().simulationTime(), release.size(),
                    Utilities.toShortString(release), getSimulator(), release);

            TRSPLogging.getRunLogger().debug(
                    "TRSPRun.call %s:  [sim time:%-5s] Current solution   : %s (pending: %s)",
                    TRSPBench.getInstance().getProgress(),
                    getInstance().getSimulator().simulationTime(),
                    getSimulator().getCurrentSolution().toShortString(),
                    getSimulator().getCurrentSolution().getUnservedRequests());

            // Set the initial solution
            runTimer.reset();
            runTimer.start();
            sol = solveTimeSlice(sol);
            runTimer.stop();
            mTimes.add((int) runTimer.readTimeMS());

            // Reject requests at this point
            List<Integer> rejected = new LinkedList<>();
            for (Integer r : relIds) {
                if (sol.getUnservedRequests().contains(r)) {
                    getSimulator().markAsRejected(r);
                    sol.markAsServed(r);
                    rejected.add(r);
                }
            }
            if (!rejected.isEmpty()) {
                TRSPLogging
                        .getRunLogger()
                        .debug("TRSPRun.call %s:  [sim time:%-5s] The following requests were rejected: %s",
                                TRSPBench.getInstance().getProgress(),
                                getInstance().getSimulator().simulationTime(), rejected);
                for (int node : rejected) {
                    InsertionMove bi = InsertionMove.findInsertion(node, sol,
                            sol.getCostDelegate(), getSolver().getTourCtrHandler(), false, true);
                    if (bi != null && bi.isFeasible())
                        TRSPLogging
                                .getRunLogger()
                                .debug("TRSPRun.call %s:  [sim time:%-5s]  Request %s could be inserted: %s",
                                        TRSPBench.getInstance().getProgress(),
                                        getInstance().getSimulator().simulationTime(), node, bi);
                }
            }

            TRSPLogging
                    .getRunLogger()
                    .debug("TRSPRun.call %s:  [sim time:%-5s] State: rejected requests:%s served Requests:%s",
                            TRSPBench.getInstance().getProgress(),
                            getInstance().getSimulator().simulationTime(),
                            getSimulator().getRejectedRequests(),
                            getSimulator().getServedRequests());
        }
        TRSPLogging.getRunLogger().debug("TRSPRun.call %s:  [sim time:%-5s] Final solution: %s",
                TRSPBench.getInstance().getProgress(),
                getInstance().getSimulator().simulationTime(), getFinalSolution());

        if (biobj)
            ((DynBiObjSolver) getSolver()).setBiObjective(false);

        getParameters().set(TRSPGlobalParameters.BIOBJ_ENABLE_PR, false);

        // Fix the earliest departure times of the last served nodes
        fixFinalEarliestDeparture(sol);

        aPosterioriRun();
        setFinalSolution(sol.clone());

        getParameters().set(TRSPGlobalParameters.RUN_BIOBJ, biobj);
        getParameters().set(TRSPGlobalParameters.BIOBJ_ENABLE_PR, enablePR);

        getSimulator().terminate(getFinalSolution());

        return sol;
    }

    /**
     * Fix the earliest departure of the last node of each tour so that it is not before the
     * {@link TRSPSimulator#getCutoffTime() cutoff} time
     * 
     * @param sol
     */
    public static void fixFinalEarliestDeparture(DTRSPSolution sol) {
        if (sol.getInstance().getSimulator().getCutoffTime() == Double.POSITIVE_INFINITY)
            return; // No cutoff time is defined
        for (TRSPTour tour : sol) {
            DTRSPTour t = (DTRSPTour) tour;
            int lastReq = t.getPred(t.getLastNode());
            double edLastReq = t.getEarliestDepartureTime(lastReq);
            if (!sol.getInstance().getSimulator().cutoff(edLastReq)) {
                edLastReq = sol.getInstance().getSimulator().getCutoffTime();
                sol.freeze(lastReq, t.getEarliestArrivalTime(lastReq), edLastReq);
                double arrivalHome = edLastReq + t.getTravelTime(lastReq, t.getLastNode());
                sol.freeze(t.getLastNode(), arrivalHome,
                        arrivalHome + t.getServiceTime(t.getLastNode()));
            }
        }
    }

    public TRSPSolution aPosterioriRun() {
        staticSetup();
        getParameters().set(TRSPGlobalParameters.RUN_BIOBJ, false);
        getParameters().set(TRSPGlobalParameters.BIOBJ_ALLOWED_DEG, 1d);
        ListIterator<IConstraint<TRSPSolution>> it = getSolver().getSolCtrHandler().iterator();
        while (it.hasNext()) {
            IConstraint<TRSPSolution> c = it.next();
            if (c.getClass() == ServicedRequestsConstraint.class)
                it.remove();
        }

        getInstance().getSimulator().staticSetup();

        TRSPLogging
                .getRunLogger()
                .info("TRSPRun.call %s:  Solving the static problem (%s accepted requests) - Rejected requests:%s Served Requests:%s",
                        TRSPBench.getInstance().getProgress(),
                        getInstance().getReleasedRequests().size(),
                        getSimulator().getRejectedRequests(), getSimulator().getServedRequests());

        getSolver().setInitSol(null);
        setStaticSolution(staticRun());

        return getStaticSolution();
    }

    /**
     * Solve the static problem defined at a specific time slice in a dynamic simulation
     * 
     * @param initSol
     *            the initial solution
     * @return the optimized solution
     */
    DTRSPSolution solveTimeSlice(DTRSPSolution initSol) {
        DTRSPSolution sol = initSol.clone();
        // We first try to repair the solution
        mRepairOperator.repair(sol, new TRSPDestroyResult(sol.getUnservedRequests()), null);
        if (!getParameters().get(TRSPGlobalParameters.RUN_REGRET_ONLY) //
                && (!getParameters().isBiObjective() || getParameters().get(
                        TRSPGlobalParameters.BIOBJ_ALLOWED_DEG) < Double.POSITIVE_INFINITY)) {
            getSolver().setInitSol(initSol);
            if (getParameters().isBiObjective()
                    && !getParameters().get(TRSPGlobalParameters.BIOBJ_INIT_AS_REF))
                // Set the previous solution as reference solution
                ((DynBiObjSolver) getSolver()).setReferenceSol(initSol);
            return (DTRSPSolution) mSolver.call();
        } else {
            return sol;
        }
    }

    /**
     * Static run.
     * 
     * @return the tRSP solution
     */
    TRSPSolution staticRun() {
        TRSPSolution sol = mSolver.call();
        setFinalSolution(sol);
        return sol;
    }

    /**
     * Collect statistics.
     * 
     * @param col
     *            the stat collector
     * @param exception
     *            {@code true} if an exception was caught externally during the run
     */
    public void collectStats(StatCollector col, boolean exception) {
        double fcost = getFinalSolution() == null ? Double.NaN : getFinalSolution()
                .getObjectiveValue();
        double scost = getStaticSolution() == null ? Double.NaN : getStaticSolution()
                .getObjectiveValue();
        double viCost = (fcost - scost) / scost;
        if (getParameters().isBiObjective()) {
            int idx = getComment().indexOf("rd_");
            String dod = getComment().substring(idx + 3, idx + 5);
            Object[] stats = new Object[] { getRunId(), getInstance().getName(), //
                    getInstance().getGroup(),//
                    getInstance().getRequestCount(), //
                    getInstance().getFleet().size(),//
                    dod,//
                    mRun,//
                    mComment,//
                    mTerminated && !exception,//
                    mTimes, //
                    getInitSolution() == null ? Double.NaN : getInitSolution().getObjective(),// initial solution cost
                    getInitSolution() == null ? -1 : getInitSolution().getServedCount(),// number of request in the
                                                                                        // initial
                    // solution
                    fcost, // final solution cost
                    getFinalSolution() == null ? -1 : getFinalSolution().getServedCount(),// number of request in the
                                                                                          // final
                    // solution
                    // distance relative to the initial solution
                    getInitSolution() == null || getFinalSolution() == null ? -1
                            : TRSPLevenshteinDistance.evaluateLevenshteinDistance(
                                    getInitSolution(), getFinalSolution()), scost,// a-posteriori solution cost
                    viCost,// vi for cost
                    getStaticSolution() == null ? -1 : getStaticSolution().getServedCount(), // number of request in
                                                                                             // the
                    // a-posteriori solution
                    // distance relative to the initial solution
                    getInitSolution() == null || getStaticSolution() == null ? -1
                            : TRSPLevenshteinDistance.evaluateLevenshteinDistance(
                                    getInitSolution(), getStaticSolution()), //
                    getBKS(getInstance().getName()), // bks value
                    getSimulator().getRejectedRequests().size(), //
                    getSimulator().getRejectedRequests().toString(), //
                    Utilities.toShortString(getParameters().get(TRSPGlobalParameters.RUN_SEEDS)),// seeds
                    getFinalSolution() == null ? "na" : getFinalSolution().toShortString() };
            col.collect(stats);
        } else if (getParameters().isDynamic()) {
            double[] evalFinal = evaluateSolution(getFinalSolution());
            double[] evalStatic = evaluateSolution(getStaticSolution());

            int idx = getComment().indexOf("rd_");
            String dod = getComment().substring(idx + 3, idx + 5);
            Object[] stats = new Object[] {//
                    getRunId(),
                    getInstance().getName(), //
                    getInstance().getGroup(),//
                    getInstance().getRequestCount(), //
                    getInstance().getFleet().size(),//
                    dod,//
                    mRun,//
                    mComment,//
                    mTerminated,//
                    Utilities.toShortString(mTimes, ',', false), //
                    getFinalSolution() == null ? Double.NaN : getFinalSolution().getCostDelegate()
                            .evaluateSolution(getFinalSolution(), true, false), //
                    evalFinal[0], //
                    evalFinal[1], //
                    evalFinal[2], //
                    getBKS(getInstance().getName()), //
                    getStaticSolution() == null ? Double.NaN : getStaticSolution()
                            .getObjectiveValue(), //
                    evalStatic[0], //
                    evalStatic[1], //
                    evalStatic[2], //
                    viCost,// VI cost
                    getStaticSolution() == null ? -1 : getStaticSolution().getServedCount(), //
                    // a-posteriori solution
                    getSimulator().getRejectedRequests().size(), //
                    getSimulator().getRejectedRequests().toString(), //
                    Utilities.toShortString(getParameters().get(TRSPGlobalParameters.RUN_SEEDS)),// seeds
                    getFinalSolution() == null ? "na" : getFinalSolution().toShortString()

            };
            col.collect(stats);
        } else {
            try {
                mSolver.collectStats(col, getBKS(), getRunId(), getRun());
                // TRSPLogging.getRunLogger().info("TRSPun.collectStats: Run stats: %s",
                // mSolver.getStatString(col, mBks, mRun));
            } catch (Exception e) {
                TRSPLogging.getRunLogger().exception("TRSPun.collectStats  %s:", e,
                        TRSPBench.getInstance().getProgress());
            }
        }
    }

    /**
     * Evaluates the total duration, distance, and balance of tours
     * 
     * @param sol
     *            the solution to be evaluated
     * @return an array [dur,dist,bal]
     */
    public double[] evaluateSolution(TRSPSolution sol) {
        double[] eval = new double[] { Double.NaN, Double.NaN, Double.NaN };

        if (sol == null)
            return eval;

        TRSPWorkingTime wt = new TRSPWorkingTime();
        TRSPDistance dist = new TRSPDistance();
        TRSPTourBalance bal = new TRSPTourBalance(wt, getParameters().get(
                TRSPGlobalParameters.BALANCE_COST_DELEGATE_MEASURE), Double.NaN);

        eval[0] = wt.evaluateSolution(sol, true, false);
        eval[1] = dist.evaluateSolution(sol, true, false);
        eval[2] = bal.evaluateSolution(sol, true, false);

        return eval;
    }

    /**
     * Dispose.
     */
    public void dispose() {
        if (mSolver != null)
            mSolver.dispose();
        mSolver = null;
        setInitSolution(null);
        setStaticSolution(null);
        setFinalSolution(null);
        mTimes = null;
        mRepairOperator = null;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("#%s %s (c:%s r:%s)", getRunId(), getInstance().getName(), mComment,
                mRun);
    }

    /**
     * Read an instance of {@link TRSPGlobalParameters} from a configuration file
     * 
     * @param configFile
     * @return the instance of {@link TRSPGlobalParameters} stored in {@code  configFile}
     */
    public static TRSPGlobalParameters readParameters(String configFile) {
        TRSPGlobalParameters params = new TRSPGlobalParameters();
        try {
            params.loadParameters(new File(configFile));
            return params;
        } catch (Exception e) {
            TRSPLogging.getBaseLogger().exception("TRSPRunBase.main", e);
            return null;
        }
    }

    /**
     * Creates a new {@link TRSPRunBase} from an instance file and a configuration file
     * 
     * @param instanceFile
     * @param configFile
     * @param dod
     *            the degree of dynamism for a dynamic run
     * @return a new {@link TRSPRunBase}
     */
    public static TRSPRunBase newTRSPRunTest(String instanceFile, String configFile, int dod) {
        return newTRSPRunTest(instanceFile, readParameters(configFile), dod);
    }

    /**
     * Creates a new {@link TRSPRunBase} from an instance file and a set of parameters
     * 
     * @param instanceFile
     * @param params
     * @param dod
     *            the degree of dynamism for a dynamic run
     * @return
     */
    public static TRSPRunBase newTRSPRunTest(String instanceFile, TRSPGlobalParameters params,
            int dod) {

        LoggerHelper.setupRootLogger(LoggerHelper.LEVEL_WARN, LoggerHelper.LEVEL_DEBUG, true);
        TRSPLogging.getRunLogger().setLevel(LoggerHelper.LEVEL_DEBUG);
        TRSPLogging.getProcedureLogger().setLevel(LoggerHelper.LEVEL_INFO);
        Logging.getSetupLogger().setLevel(LoggerHelper.LEVEL_INFO);

        HashTourPool.sCountCollisions = true;

        // Disable SC
        // params.set(TRSPGlobalParameters.SC_ENABLED, false);
        // params.set(TRSPGlobalParameters.SC_MAX_TIME, 60d);
        // Set ALNS iterations
        // params.set(TRSPGlobalParameters.ALNS_PARALLEL, true);
        // params.set(TRSPGlobalParameters.ALNS_MAX_IT, 25000);
        // params.set(TRSPGlobalParameters.ALNS_PALNS_IT_P, 25000);
        // Set pool
        params.set(TRSPGlobalParameters.ALNS_PALNS_POOL, DiversifiedPool.class);
        params.set(TRSPGlobalParameters.ALNS_PALNS_DIV_METRIC, BrokenPairsDistance.class);

        // Enable logging
        // params.set(TRSPGlobalParameters.ALNS_ENABLE_LOGGING, true);

        // Fix the number of threads
        params.set(TRSPGlobalParameters.THREAD_COUNT,
                Math.min(params.getThreadCount(), Runtime.getRuntime().availableProcessors()));

        // Setup the TRSPBench
        TRSPBench.setup(params, true, "TRSPRunBase_test");

        ITRSPPersistenceHelper reader = TRSPUtilities.getPersistenceHelper(params
                .get(TRSPGlobalParameters.RUN_INSTANCE_FOLDER));
        TRSPInstance instance = null;
        String com = "";
        try {
            instance = reader.readInstance(
                    new File(String.format("%s/%s",
                            params.get(TRSPGlobalParameters.RUN_INSTANCE_FOLDER), instanceFile)),
                    params.isCVRPTW());
            if (params.isDynamic()) {
                Map<String, List<File>> rdFileMapping = DynamicPersistenceHelper.getRelDateFiles(
                        params.get(TRSPGlobalParameters.RUN_REL_DATE_FOLDER), new int[] { dod });
                List<File> rdFiles = rdFileMapping.get(instance.getName());
                if (rdFiles != null && !rdFiles.isEmpty()) {
                    DynamicTRSPPersistenceHelper.readRelDates(instance, rdFiles.get(0),
                            params.isCVRPTW());
                    com = rdFiles.get(0).getName();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
        }

        TRSPRunBase run = new TRSPRunBase(0, instance, params, TRSPBench.getInstance().getBKS(), 0,
                com);

        return run;
    }

    public static void main(String[] args) {

        DiversifiedPool.sAutoAdjustWeights = false;

        // TRSPGlobalParameters params = readParameters(CFG_BiDVRPTW);
        // TRSPGlobalParameters params = readParameters(CFG_DVRPTW);
        TRSPGlobalParameters params = readParameters(CFG_DTRSP);
        // TRSPGlobalParameters params = readParameters(CFG_VRPTW);
        // params.set(TRSPGlobalParameters.BIOBJ_ALLOWED_DEG, Double.POSITIVE_INFINITY);
        // params.set(TRSPGlobalParameters.BIOBJ_ENABLE_PR, true);
        // params.set(TRSPGlobalParameters.ALNS_COST_DELEGATE, TRSPTourBalance.class);
        // params.set(TRSPGlobalParameters.BALANCE_COST_DELEGATE_MEASURE, DeviationMeasure.MaxMinGap);
        // params.set(TRSPGlobalParameters.BALANCE_COST_DELEGATE_PENALTY, 0.1);
        // params.set(TRSPGlobalParameters.ALNS_OBJ_GAMMA, 0.1);
        // params.set(TRSPGlobalParameters.ALNS_MAX_IT_STATIC, 1);

        TRSPRunBase run = newTRSPRunTest("C202.100_25-5-5-5.txt", params, 10);
        // TRSPRunBase run = newTRSPRunTest("U4.txt", params, 10);
        // TRSPRunBase run = newTRSPRunTest(true, "C105.100_25-5-5-5.txt");
        // TRSPRunBase run = newTRSPRunTest(false, "C101.txt", false, 50);
        // TRSPRunBase run = newTRSPRunTest("R111.txt", params, 90);
        // run.getParameters().set(TRSPGlobalParameters.THREAD_COUNT, 6);
        run.getParameters().set(TRSPGlobalParameters.ALNS_OBJ_GAMMA, 0.1);

        Stopwatch timer = new Stopwatch();
        try {
            timer.start();
            run.call();
            timer.stop();
        } catch (Exception e) {
            TRSPLogging.getBaseLogger().exception("TRSPRunBase.main", e);
            Logging.awaitLogging(5000);
            System.exit(1);
        }
        // run.testDynRun();

        Logging.awaitLogging(5000);

        System.out.println("wall time: " + timer.readTimeString());
        System.out.println("init     : " + run.getInitSolution());
        System.out.println("final    : " + run.getFinalSolution());
        System.out.println("checksol : "
                + TRSPSolutionChecker.INSTANCE.checkSolution(run.getFinalSolution()));
        System.out.println("static   : " + run.getStaticSolution());
        System.out.println("checksol : "
                + TRSPSolutionChecker.INSTANCE.checkSolution(run.getStaticSolution()));
        Double bks = run.getBKS().getBKS(run.getInstance().getName());
        if (bks != null) {
            double cost = run.getFinalSolution().getObjectiveValue();
            System.out.println("BKS      : " + bks);
            System.out.printf("GAP      : %.2f\n", (cost - bks) / bks * 100);
        }
        if (run.getParameters().get(TRSPGlobalParameters.SC_ENABLED)) {
            System.out.println("Pool size  :" + ((ALNSSCSolver) run.getSolver()).getTourPoolSize());
            System.out.println("Collisions :"
                    + ((ALNSSCSolver) run.getSolver()).getHashPoolCollisionCount());
        }

        if (run.getInstance().isDynamic()) {
            System.out.printf("Rejected reqs  : %s %s\n", run.getSimulator().getRejectedRequests()
                    .size(), run.getSimulator().getRejectedRequests());
            System.out
                    .printf("Final solution : %.3f\n", run.getFinalSolution().getObjectiveValue());
            System.out.printf("Static solution: %.3f\n", run.getStaticSolution()
                    .getObjectiveValue());
            System.out.printf("VI             : %.3f\n", (run.getFinalSolution()
                    .getObjectiveValue() - run.getStaticSolution().getObjectiveValue())
                    * 100d
                    / run.getStaticSolution().getObjectiveValue());
        }

        System.out.println("== FINAL SOLUTION  =================================");
        printDetailedSolution(run.getFinalSolution(), run);
        System.out.println("== STATIC SOLUTION =================================");
        printDetailedSolution(run.getStaticSolution(), run);
    }

    public static void printDetailedSolution(TRSPSolution solution, TRSPRunBase run) {
        System.out.println("Detailed solution " + solution.hashCode());
        DTRSPSolution dsol = solution instanceof DTRSPSolution ? (DTRSPSolution) solution : null;

        System.out.printf("cost  : %7.1f\n", solution.getObjectiveValue());

        if (solution.getInstance().getSimulator() != null) {
            System.out.printf("cutoff: %7.1f\n", solution.getInstance().getSimulator()
                    .getCutoffTime());
        }
        double cost = 0;
        for (TRSPTour tour : solution) {
            System.out.printf("--- Tour %2s ---\n", tour.getTechnicianId());
            System.out.printf(" cost:%7.2f\n", tour.getTotalCost());
            cost += tour.getTotalCost();
            DTRSPTour dtour = tour instanceof DTRSPTour ? (DTRSPTour) tour : null;
            for (int n : tour) {
                System.out.printf(
                        " %3s%1s ea:%7.1f ed:%7.1f fts:%7.1f tfs-ck:%7.1f\n",
                        n,
                        dsol != null && dsol.isFrozen(n) ? "f" : " ",//
                        tour.getEarliestArrivalTime(n), tour.getEarliestDepartureTime(n),
                        tour.getFwdSlackTime(n),
                        TRSPSolutionChecker.evaluateFwdSlackTime(tour, n, tour.getLastNode()));
            }
        }
        System.out.printf("sum cost: %7.1f\n", cost);
    }

    /**
     * Clone this run with a specific {@link ExperimentParameterSetting}
     * 
     * @param runId
     *            the id of the cloned run
     * @param setting
     * @return a clone of this run with the parameters defined in {@code  setting}
     */
    public TRSPRunBase clone(int runId, ExperimentParameterSetting<TRSPGlobalParameters> setting) {
        TRSPRunBase clone = new TRSPRunBase(runId, getInstance(), setting.getParameters(),
                getBKS(), mRun, setting.getChangedValuesString());
        clone.mExpeSetting = setting;
        return clone;
    }
}
