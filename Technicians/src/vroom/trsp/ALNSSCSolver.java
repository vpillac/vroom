/**
 *
 */
package vroom.trsp;

import static vroom.trsp.util.TRSPGlobalParameters.ALNS_A_L;
import static vroom.trsp.util.TRSPGlobalParameters.ALNS_A_R;
import static vroom.trsp.util.TRSPGlobalParameters.ALNS_A_SIGMA1;
import static vroom.trsp.util.TRSPGlobalParameters.ALNS_A_SIGMA2;
import static vroom.trsp.util.TRSPGlobalParameters.ALNS_A_SIGMA3;
import static vroom.trsp.util.TRSPGlobalParameters.ALNS_MAX_IT;
import static vroom.trsp.util.TRSPGlobalParameters.ALNS_MAX_TIME;
import static vroom.trsp.util.TRSPGlobalParameters.ALNS_OBJ_GAMMA;
import static vroom.trsp.util.TRSPGlobalParameters.ALNS_SA_ALPHA;
import static vroom.trsp.util.TRSPGlobalParameters.ALNS_SA_P;
import static vroom.trsp.util.TRSPGlobalParameters.ALNS_SA_W;
import static vroom.trsp.util.TRSPGlobalParameters.RUN_INIT_HEUR;
import gurobi.GRB.DoubleAttr;
import gurobi.GRBException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import vroom.common.heuristics.ProcedureStatus;
import vroom.common.heuristics.alns.ALNSComponentHandler;
import vroom.common.heuristics.alns.ALNSEventType;
import vroom.common.heuristics.alns.ALNSGlobalParameters;
import vroom.common.heuristics.alns.ALNSLogger;
import vroom.common.heuristics.alns.ALNSSALogger;
import vroom.common.heuristics.alns.AdaptiveLargeNeighborhoodSearch;
import vroom.common.heuristics.alns.IDestroy;
import vroom.common.heuristics.alns.IRepair;
import vroom.common.heuristics.alns.ParallelALNS;
import vroom.common.utilities.BestKnownSolutions;
import vroom.common.utilities.StatCollector;
import vroom.common.utilities.StatCollector.Label;
import vroom.common.utilities.Stopwatch;
import vroom.common.utilities.Utilities;
import vroom.common.utilities.lp.SolverStatus;
import vroom.common.utilities.optimization.IComponentHandler;
import vroom.common.utilities.optimization.IParameters.LSStrategy;
import vroom.common.utilities.optimization.OptimizationSense;
import vroom.common.utilities.optimization.RndComponentHanlder;
import vroom.common.utilities.optimization.SAAcceptanceCriterion;
import vroom.common.utilities.optimization.SimpleParameters;
import vroom.common.utilities.optimization.SolutionComparator;
import vroom.trsp.datamodel.HashTourPool;
import vroom.trsp.datamodel.ITRSPTourPool;
import vroom.trsp.datamodel.NodeSetSolutionHasher;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.datamodel.costDelegates.TRSPCostDelegate;
import vroom.trsp.datamodel.costDelegates.TRSPDistance;
import vroom.trsp.datamodel.costDelegates.TRSPTourBalance;
import vroom.trsp.datamodel.costDelegates.TRSPWorkingTime;
import vroom.trsp.optimization.alns.DestroyCritical;
import vroom.trsp.optimization.alns.DestroyRandom;
import vroom.trsp.optimization.alns.DestroyStaticRelated;
import vroom.trsp.optimization.alns.DestroyTimeRelated;
import vroom.trsp.optimization.alns.RepairRegret;
import vroom.trsp.optimization.constraints.TourConstraintHandler;
import vroom.trsp.optimization.constructive.TRSPConstructiveHeuristic;
import vroom.trsp.optimization.matheuristic.SCGurobiSolver;
import vroom.trsp.optimization.matheuristic.TourPoolCallBack;
import vroom.trsp.util.TRSPGlobalParameters;
import vroom.trsp.util.TRSPLogging;

/**
 * <code>ALNSSCSolver</code> is a class containing the logic to solve the TRSP
 * <p>
 * Creation date: Apr 8, 2011 - 4:38:48 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class ALNSSCSolver extends TRSPSolver {

    /**
     * The labels that will be used in {@link #collectStats(StatCollector, BestKnownSolutions)}
     */
    public static final Label<?>[] LABELS = new Label<?>[] {
            new Label<Integer>("run_id", Integer.class), new Label<String>("name", String.class), // name
            new Label<String>("group", String.class), // group
            new Label<Integer>("size", Integer.class), // size
            new Label<Integer>("crew", Integer.class), // crew
            new Label<Integer>("run", Integer.class), // run #
            new Label<String>("comment", String.class), // comment
            new Label<Double>("init_time", Double.class), // init_time
            new Label<Integer>("init_it", Integer.class), // init_it
            // new Label<AssignmentStrategy>("init_strat",
            // AssignmentStrategy.class),// init_strat
            new Label<ProcedureStatus>("init_status", ProcedureStatus.class), // init_status
            new Label<Integer>("init_unserved", Integer.class), // init_unserved
            new Label<Double>("init_wt", Double.class, COST_FORMAT), // init_wt
            // new Label<Double>("init_wt_dev", Double.class, COST_FORMAT), // init_wt_dev
            new Label<Double>("alns_time_s", Double.class, TIME_FORMAT), // alns_time
            new Label<Integer>("alns_it", Integer.class), // alns_it
            new Label<Integer>("alns_unserved", Integer.class), // alns_unserved
            new Label<Double>("alns_wt", Double.class, COST_FORMAT), // alns_wt
            // new Label<Double>("alns_wt_dev", Double.class, COST_FORMAT), // alns_wt_dev
            new Label<Integer>("alns_K", Integer.class), // alns_K
            new Label<Double>("alns_imp_wt", Double.class, PERC_FORMAT), // alns_wt
                                                                         // improvement
                                                                         // new Label<Double>("alns_imp_wt_dev",
                                                                         // Double.class, PERC_FORMAT), // alns_wt_dev
            // improvement
            new Label<String>("alns_checksol", String.class), // alns_feasible
            new Label<Double>("postop_time_s", Double.class, TIME_FORMAT), // postop_time
            new Label<Integer>("postop_unserved", Integer.class), // postop_unserved
            new Label<Double>("postop_wt", Double.class, COST_FORMAT), // postop_wt
            // new Label<Double>("postop_wt_dev", Double.class, COST_FORMAT), // postop_wt_dev
            new Label<Integer>("postop_K", Integer.class), // postop_K
            new Label<Double>("postop_imp_wt", Double.class, PERC_FORMAT), // postop_wt
                                                                           // improvement
                                                                           // new Label<Double>("postop_imp_wt_dev",
                                                                           // Double.class, PERC_FORMAT), //
                                                                           // postop_wt_dev
            // improvement
            new Label<String>("postop_checksol", String.class), // postop_checksol
            new Label<Double>("postop_bbgap", Double.class), // postop_gap
            new Label<SolverStatus>("postop_status", SolverStatus.class), // postop_status
            new Label<Integer>("postop_pool_size", Integer.class), // postop_pool_size//
            new Label<Integer>("postop_pool_collisions", Integer.class), // postop_pool_collisions//
            new Label<Double>("bks", Double.class, COST_FORMAT),//
            new Label<Double>("alns_gap", Double.class, PERC_FORMAT),//
            new Label<Double>("postop_gap", Double.class, PERC_FORMAT),//
            new Label<Integer>("bks_K", Integer.class), //
            new Label<Integer>("bks_opt", Integer.class), //
            new Label<String>("seeds", String.class), // seeds
            new Label<String>("final_sol", String.class), // the detailed
                                                          // solution
                                          };

    /**
     * Returns the default set of destroy procedures
     * 
     * @return the default set of destroy procedures
     */
    public static List<IDestroy<TRSPSolution>> newDestroySet(TRSPGlobalParameters params) {
        LinkedList<IDestroy<TRSPSolution>> destroys = new LinkedList<IDestroy<TRSPSolution>>();
        destroys.add(new DestroyRandom());
        destroys.add(new DestroyStaticRelated(params.get(TRSPGlobalParameters.ALNS_DES_P), 1, 1, 1));
        destroys.add(new DestroyTimeRelated(params.get(TRSPGlobalParameters.ALNS_DES_P)));
        destroys.add(new DestroyCritical(params.get(TRSPGlobalParameters.ALNS_DES_P)));
        return destroys;
    }

    /**
     * Returns the default set of repair procedures
     * 
     * @return the default set of repair procedures
     */
    public static List<IRepair<TRSPSolution>> newRepairSet(TourConstraintHandler constraintHandler,
            TRSPGlobalParameters params) {
        LinkedList<IRepair<TRSPSolution>> repairs = new LinkedList<IRepair<TRSPSolution>>();
        repairs.add(new RepairRegret(params, constraintHandler, 1, false));
        repairs.add(new RepairRegret(params, constraintHandler, 2, false));
        repairs.add(new RepairRegret(params, constraintHandler, 3, false));

        if (params.get(TRSPGlobalParameters.ALNS_REP_NOISE)) {
            repairs.add(new RepairRegret(params, constraintHandler, 1, true));
            repairs.add(new RepairRegret(params, constraintHandler, 2, true));
            repairs.add(new RepairRegret(params, constraintHandler, 3, true));
        }
        return repairs;
    }

    private TRSPConstructiveHeuristic                     mInit;
    private final Stopwatch                               mInitTimer    = new Stopwatch();

    private AdaptiveLargeNeighborhoodSearch<TRSPSolution> mALNS;

    /**
     * Returns the post-optimizer used in this run
     * 
     * @return the post-optimizer used in this run
     */
    private SCGurobiSolver                                mPostOp;

    private TRSPSolution                                  mALNSSol;

    private TRSPSolution                                  mInitSol;
    private List<TRSPSolution>                            mInitPool;

    private SimpleParameters                              mALNSParams;

    private ALNSLogger<TRSPSolution>                      mLogger;
    private TourPoolCallBack                              mTourPoolCB;
    private int                                           mTourPoolSize = -1;

    private final ALNSGlobalParameters                    mALNSGlobalParams;

    /**
     * Returns the global parameters used in the ALNS
     * 
     * @return the global parameters used in the ALNS
     */
    public ALNSGlobalParameters getALNSGlobalParams() {
        return mALNSGlobalParams;
    }

    /**
     * Returns the tour pool containing the tours collected during the ALNS
     * 
     * @return the tour pool containing the tours collected during the ALNS
     */
    public ITRSPTourPool getTourPool() {
        return mTourPoolCB.getTourPool();
    }

    /**
     * Returns the size of the tour pool
     * 
     * @return the size of the tour pool
     */
    public int getTourPoolSize() {
        if (mTourPoolSize == -1)
            return getTourPool().size();
        else
            return mTourPoolSize;
    }

    /**
     * Creates a new <code>ALNSSCSolver</code>
     * 
     * @param instance
     *            the instance to be solved
     * @param params
     *            the global parameters that will be used in this run
     * @param rndStream
     *            the random stream used in this solver
     */
    public ALNSSCSolver(TRSPInstance instance, TRSPGlobalParameters params) {
        super(instance, params);
        mALNSGlobalParams = new ALNSGlobalParameters();
    }

    @Override
    public TRSPSolution call() {
        TRSPLogging.getProcedureLogger().info(
                this.getClass().getSimpleName() + "[start ]: Solving instance %s (I=%s, SC=%s)",
                getInstance().getName(), getParams().get(TRSPGlobalParameters.ALNS_MAX_IT),
                getParams().get(TRSPGlobalParameters.SC_ENABLED));

        if (getInitSol() == null) {
            initialization();
            TRSPLogging.getProcedureLogger().info(
                    this.getClass().getSimpleName() + "[init  ]: Initialization finished in %.1fs",
                    mInitTimer.readTimeS());
            TRSPLogging.getProcedureLogger().info(
                    this.getClass().getSimpleName() + "[init  ]: Initial solution: %.3f (%s)",
                    getInitSol().getObjectiveValue(), getInitSol().getUnservedCount());
            String err = getChecker().checkSolution(getInitSol());
            if (!err.isEmpty()) {
                TRSPLogging.getProcedureLogger().warn(
                        this.getClass().getSimpleName() + "[init  ]:  Infeasibility: %s", err);
            }
        }

        setupALNS();
        if (ParallelALNS.class.isAssignableFrom(getALNS().getClass()))
            TRSPLogging.getProcedureLogger().info(
                    this.getClass().getSimpleName() + "[init  ]: Penalty: %.3f - Initial pool: %s",
                    getInitSol().getCostDelegate().getUnservedPenalty(),
                    ((ParallelALNS<?>) getALNS()).getSolPool());
        alns();
        TRSPLogging.getProcedureLogger().info(
                this.getClass().getSimpleName() + "[alns  ]: ALNS finished in %.1fs %s",
                getALNS().getTimer().readTimeS(), getALNS().getStoppingCriterion());
        if (ParallelALNS.class.isAssignableFrom(getALNS().getClass()))
            TRSPLogging.getProcedureLogger().info(
                    this.getClass().getSimpleName() + "[alns  ]: Final pool: %s",
                    ((ParallelALNS<?>) getALNS()).getSolPool());
        if (getALNSSol() != null)
            TRSPLogging.getProcedureLogger().info(
                    this.getClass().getSimpleName() + "[alns  ]: ALNS solution  : %.3f (%s)",
                    getALNSSol().getObjectiveValue(), getALNSSol().getUnservedCount());
        String err = getChecker().checkSolution(getALNSSol());
        if (!err.isEmpty()) {
            TRSPLogging.getProcedureLogger().warn(
                    this.getClass().getSimpleName() + "[alns  ]:  Infeasibility: %s", err);
        }

        if (getParams().get(TRSPGlobalParameters.SC_ENABLED) && getALNSSol() != null) {
            TRSPLogging.getProcedureLogger().info(
                    this.getClass().getSimpleName() + "[postop]: Post-optimization");
            setupPostOp();
            postOp();
            TRSPLogging.getProcedureLogger().info(
                    this.getClass().getSimpleName()
                            + "[postop]: Post-optimization finished in %.1fs",
                    getPostOp().getTimer().readTimeS());
            err = getChecker().checkSolution(getFinalSolution());
            if (!err.isEmpty()) {
                TRSPLogging.getProcedureLogger().warn(
                        this.getClass().getSimpleName() + "[end  ]:  Infeasibility: %s", err);
            }
        } else {
            setFinalSolution(getALNSSol());
        }

        TRSPLogging.getProcedureLogger().info(
                this.getClass().getSimpleName() + "[end   ]: Final solution : %.3f (%s)",
                getFinalSolution() != null ? getFinalSolution().getObjectiveValue() : Double.NaN,
                getFinalSolution() != null ? getFinalSolution().getUnservedCount() : "na");

        return getFinalSolution();
    }

    @Override
    public Label<?>[] getLabels() {
        return LABELS;
    }

    @Override
    public Object[] getStats(BestKnownSolutions bks, int runId, int runNum) {
        String group = getInstance().getName().substring(0,
                (getInstance().getName().contains("RC") ? 3 : 2));

        String instanceName = getInstance().getName().replace(".txt", "");

        TRSPCostDelegate wtDel;
        // sCVRPTW
        if (getParams().isCVRPTW())
            wtDel = new TRSPDistance();
        else
            wtDel = new TRSPWorkingTime();
        TRSPTourBalance tbDel = new TRSPTourBalance(wtDel, getParams().get(
                TRSPGlobalParameters.BALANCE_COST_DELEGATE_MEASURE));

        double init_wt = wtDel.evaluateSolution(getInitSol(), true, true);
        // double init_wt_dev = tbDel.evaluateSolution(getInitSol(), true, true);
        double alns_wt = wtDel.evaluateSolution(getALNSSol(), true, true);
        // double alns_wt_dev = tbDel.evaluateSolution(getALNSSol(), true, true);
        double postop_wt = wtDel.evaluateSolution(getFinalSolution(), true, true);
        // double postop_wt_dev = tbDel.evaluateSolution(getFinalSolution(), true, true);
        double postop_bbgap = Double.NaN;
        if (getPostOp() != null) {
            try {
                double mipObj = getPostOp().getModel().get(DoubleAttr.ObjVal);
                double mipLB = getPostOp().getModel().get(DoubleAttr.ObjBound);
                postop_bbgap = mipLB != 0 ? (mipObj - mipLB) / mipObj : mipObj / 100;
            } catch (GRBException e) {
                TRSPLogging.getProcedureLogger().exception(
                        this.getClass().getSimpleName() + ".collectStats", e);
            }
        }
        Object[] stats = new Object[] { runId, getInstance().getName(), // name
                group, // group
                getInstance().getRequestCount(), // size
                getInstance().getFleet().size(), // crew count
                runNum, getComment(), // comment
                mInitTimer.readTimeS(), // init_time
                getInit().getIterationCount(), // init_it
                // mInit.getAssignmentStrategy(),// init_strat
                getInit().getStatus(),// init_status
                getInitSol().getUnservedCount(), // init_unserved
                init_wt,// init_wt
                // init_wt_dev,// init_wt_dev
                getALNS().getTimer().readTimeS(),// alns_time
                getALNS().getStoppingCriterion().getIterationCount(),// alns_it
                getALNSSol().getUnservedCount(), // alns_unserved
                alns_wt,// alns_wt
                // alns_wt_dev,// alns_wt_dev
                getALNSSol().getActualTourCount(), // alns_K
                (init_wt - alns_wt) / init_wt, // wt imp
                // (init_wt_dev - alns_wt_dev) / init_wt_dev, // wt dev imp
                getChecker().checkSolution(getALNSSol()),// alns_feasible
                getPostOp() == null ? 0l : getPostOp().getTimer().readTimeS(), // postop_time
                getFinalSolution().getUnservedCount(), // postop_unserved
                postop_wt, // postop_wt
                // postop_wt_dev, // postop_wt_dev
                getFinalSolution().getActualTourCount(), // postop_K
                (alns_wt - postop_wt) / alns_wt, // postop_wt improvement
                // (alns_wt_dev - postop_wt_dev) / alns_wt_dev, // postop_wt_dev improvement
                getChecker().checkSolution(getFinalSolution()), // postop_checksol
                postop_bbgap, // postop_gap
                getPostOp() != null ? getPostOp().getStatus() : SolverStatus.UNKNOWN_STATUS, // postop_status
                getPostOp() != null ? getPostOp().getColumnCount() : 0, // postop_pool_size
                getHashPoolCollisionCount(), // postop_pool_collisions
                bks.getBKS(instanceName), // BKS
                bks.getGapToBKS(instanceName, alns_wt, OptimizationSense.MINIMIZATION), // ALNS GAP
                bks.getGapToBKS(instanceName, postop_wt, OptimizationSense.MINIMIZATION), // POSTOP GAP
                bks.getIntValue(instanceName, "K"), // BKS - K
                bks.isOptimal(instanceName) ? 1 : 0, // BKS is optimal?
                Utilities.toShortString(getParams().get(TRSPGlobalParameters.RUN_SEEDS)),// seeds
                getFinalSolution().toShortString() };
        return stats;
    }

    @Override
    protected void finalize() throws Throwable {
        if (getPostOp() != null)
            getPostOp().getModel().dispose();
        super.finalize();
    }

    /**
     * Returns the ALN used in this run
     * 
     * @return the ALN used in this run
     */
    public AdaptiveLargeNeighborhoodSearch<TRSPSolution> getALNS() {
        return mALNS;
    }

    /**
     * Returns the solution found by the ALNS
     * 
     * @return the solution found by the ALNS
     */
    public TRSPSolution getALNSSol() {
        return mALNSSol;
    }

    /**
     * Returns the constructive heuristic
     * 
     * @return the constructive heuristic
     */
    public TRSPConstructiveHeuristic getInit() {
        return mInit;
    }

    /**
     * Returns the initial solution
     * 
     * @return the initial solution
     */
    public TRSPSolution getInitSol() {
        return mInitSol;
    }

    public SCGurobiSolver getPostOp() {
        return mPostOp;
    }

    /**
     * Find an initial solution
     */
    public void initialization() {
        // ----------------------------------------
        // Setup the initialization
        setInit((TRSPConstructiveHeuristic) getParams().newInstance(RUN_INIT_HEUR, getInstance(),
                getParams(), getTourCtrHandler(), getParams().newInitCostDelegate()));
        // ----------------------------------------
        mInitTimer.reset();
        mInitTimer.start();
        if (getParams().get(TRSPGlobalParameters.ALNS_PARALLEL)) {
            // TODO parallelize this
            int size = Math.min(getParams().get(TRSPGlobalParameters.ALNS_PALNS_POOL_SIZE),
                    getParams().get(TRSPGlobalParameters.THREAD_COUNT));
            mInitPool = new ArrayList<TRSPSolution>(size);
            while (mInitPool.size() < size) {
                getInit().call();
                mInitPool.add(getInit().getSolution());
            }
            Collections.sort(mInitPool, new SolutionComparator<>(OptimizationSense.MINIMIZATION));
            mInitSol = mInitPool.get(mInitPool.size() - 1).clone();
        } else {
            getInit().call();
            setInitSol(getInit().getSolution());
        }
        mInitTimer.stop();

        getInitSol().getCostDelegate().unsetUnservedPenalty();
        getInitSol().getCostDelegate().evaluateSolution(getInitSol(), true, true);

    }

    public void setupALNS() {
        // ----------------------------------------
        // Setup the ALNS
        IComponentHandler<IDestroy<TRSPSolution>> destroyComponents = null;
        IComponentHandler<IRepair<TRSPSolution>> repairComponents = null;

        getInitSol().setCostDelegate(getALNSCostDelegate(getInitSol()));

        Class<?> handlerClass = getParams().get(TRSPGlobalParameters.ALNS_COMP_HANDLER);
        if (handlerClass == ALNSComponentHandler.class) {
            destroyComponents = new ALNSComponentHandler<IDestroy<TRSPSolution>>(getParams()
                    .getALNSRndStream(), newDestroySet(getParams()),
                    getParams().get(ALNS_A_SIGMA1), getParams().get(ALNS_A_SIGMA2), getParams()
                            .get(ALNS_A_SIGMA3), getParams().get(ALNS_A_R), getParams().get(
                            ALNS_A_L));
            repairComponents = new ALNSComponentHandler<IRepair<TRSPSolution>>(getParams()
                    .getALNSRndStream(), newRepairSet(getTourCtrHandler(), getParams()),
                    getParams().get(ALNS_A_SIGMA1), getParams().get(ALNS_A_SIGMA2), getParams()
                            .get(ALNS_A_SIGMA3), getParams().get(ALNS_A_R), getParams().get(
                            ALNS_A_L));
        } else if (handlerClass == RndComponentHanlder.class) {
            destroyComponents = new RndComponentHanlder<IDestroy<TRSPSolution>>(getParams()
                    .getALNSRndStream(), newDestroySet(getParams()), false);
            repairComponents = new RndComponentHanlder<IRepair<TRSPSolution>>(getParams()
                    .getALNSRndStream(), newRepairSet(getTourCtrHandler(), getParams()), false);
        } else {
            TRSPLogging.getSetupLogger().error(
                    this.getClass().getSimpleName()
                            + ".setupALNS: Unsupported ALNS component handler: %s", handlerClass);
        }

        getALNSGlobalParams().set(
                ALNSGlobalParameters.DESTROY_SIZE_RANGE,
                new double[] { getParams().get(TRSPGlobalParameters.ALNS_XI_MIN),
                        getParams().get(TRSPGlobalParameters.ALNS_XI_MAX) });

        // Parallel ALNS
        if (getParams().get(TRSPGlobalParameters.ALNS_PARALLEL)) {
            // && getParams().get(TRSPGlobalParameters.THREAD_COUNT) > 1) {
            getALNSGlobalParams().set(ALNSGlobalParameters.PALNS_IT_P,
                    getParams().get(TRSPGlobalParameters.ALNS_PALNS_IT_P));
            getALNSGlobalParams().set(ALNSGlobalParameters.PALNS_POOL_SIZE,
                    getParams().get(TRSPGlobalParameters.ALNS_PALNS_POOL_SIZE));
            getALNSGlobalParams().set(ALNSGlobalParameters.PALNS_POOL,
                    getParams().get(TRSPGlobalParameters.ALNS_PALNS_POOL));
            getALNSGlobalParams().set(ALNSGlobalParameters.DIVERSITY_METRIC,
                    getParams().get(TRSPGlobalParameters.ALNS_PALNS_DIV_METRIC));
            getALNSGlobalParams().set(ALNSGlobalParameters.PALNS_THREAD_COUNT,
                    getParams().getThreadCount());

            ParallelALNS<TRSPSolution> alns = new ParallelALNS<TRSPSolution>(
                    OptimizationSense.MINIMIZATION, getParams().getALNSRndStream(),
                    getALNSGlobalParams(), destroyComponents, repairComponents);
            setALNS(alns);

            // Setup the solution pool
            TRSPCostDelegate cd = getALNSCostDelegate(getInitSol());
            for (TRSPSolution s : mInitPool) {// FIXME update mInitPool in dynamic setting?
                s.setCostDelegate(cd);
                alns.getSolPool().add(s, true);
            }

        } else {
            // Sequential ALNS
            setALNS(new AdaptiveLargeNeighborhoodSearch<TRSPSolution>(
                    OptimizationSense.MINIMIZATION, getParams().getALNSRndStream(),
                    getALNSGlobalParams(), destroyComponents, repairComponents));
        }

        if (mLogger != null)
            mLogger.registerToALNS(getALNS());

        // ----------------------------------------
        mALNSParams = new SimpleParameters(LSStrategy.DET_BEST_IMPROVEMENT, getParams().get(
                ALNS_MAX_TIME), getParams().get(ALNS_MAX_IT), getParams().getALNSRndStream());

        if (getParams().get(TRSPGlobalParameters.SC_ENABLED)
                || getParams().get(TRSPGlobalParameters.TOUR_POOL_ENABLED)) {
            mTourPoolCB = new TourPoolCallBack(getInstance(), getParams().get(ALNS_MAX_IT),
                    new NodeSetSolutionHasher(getInstance(), getParams().getHashRndStream()));
            getALNS().registerCallback(mTourPoolCB, ALNSEventType.REPAIRED);
        }

        if (getParams().get(TRSPGlobalParameters.ALNS_ENABLE_LOGGING)) {
            ALNSLogger<TRSPSolution> logger = new ALNSSALogger<TRSPSolution>("results/alns");
            logger.registerToALNS(getALNS());
        }

        // Setup SA acceptance criterion
        mALNSParams.setAcceptanceCriterion(new SAAcceptanceCriterion(
                OptimizationSense.MINIMIZATION, getParams().getALNSRndStream(), getInitSol()
                        .getObjectiveValue(), getParams().get(ALNS_SA_W), getParams()
                        .get(ALNS_SA_P), mALNSParams.getMaxIterations(), getParams().get(
                        ALNS_SA_ALPHA), true));
    }

    /**
     * Returns the number of hash collisions detected, returns -1 if @link {@link HashTourPool#sCountCollisions} is set
     * to {@code false}
     * 
     * @return the number of hash collisions detected
     */
    public int getHashPoolCollisionCount() {
        if (mTourPoolCB != null && HashTourPool.sCountCollisions)
            return ((HashTourPool) mTourPoolCB.getTourPool()).getCollisionsCount();
        else
            return -1;
    }

    /**
     * Return a cost delegate for the ALNS
     * 
     * @param sol
     * @return
     */
    public TRSPCostDelegate getALNSCostDelegate(TRSPSolution sol) {
        // Load the cost delegate from the global parameters
        TRSPCostDelegate cd = getParams().newALNSCostDelegate(sol);
        // Set unserved customer penalty
        cd.setPenalize(true);
        // cd.setUnservedPenalty(sol, getParams().get(ALNS_OBJ_GAMMA));
        cd.setUnservedPenalty(sol, getParams().get(ALNS_OBJ_GAMMA) * 100
                / sol.getInstance().getReleasedRequests().size());
        return cd;
    }

    /**
     * Run the ALNS
     */
    public void alns() {
        setALNSSol(getALNS().localSearch(getInitSol().getInstance(), getInitSol(), mALNSParams));

        getALNSSol().getCostDelegate().unsetUnservedPenalty();
        getALNSSol().getCostDelegate().evaluateSolution(getALNSSol(), true, true);

        setFinalSolution(getALNSSol());
        getALNS().stop();
    }

    /**
     * Initialize and setup the post-optimizer
     */
    public void setupPostOp() {
        setPostOp(new SCGurobiSolver(getInstance(), getParams(), mTourPoolCB.getHasher(),
                !getALNSSol().getUnservedRequests().isEmpty()));

        getPostOp().addColumns(mTourPoolCB.getTourPool().getAllTours());

        mTourPoolSize = getTourPool().size();
        // Free some memory
        mTourPoolCB.getTourPool().clear();

        // Set initial solution
        getPostOp().setIncumbent(getALNSSol());

        if (getModelWriter() != null)
            try {
                getModelWriter().write(getPostOp().getModel(),
                        String.format("%s-%s", getInstance().getName(), getComment()));
            } catch (IOException e) {
                TRSPLogging.getOptimizationLogger().exception(
                        this.getClass().getSimpleName() + ".setupPostOp", e);
            }

        // Add stat writer
        if (getGRBStatCollector() != null)
            getGRBStatCollector().setModel(getPostOp().getModel());
    }

    /**
     * Performs the post optimization by solving a set covering problem with the tours generated during the ALNS.
     */
    public void postOp() {
        // Solve
        getPostOp().solve();
        // getPostOp().repairSolution();
        setFinalSolution(getPostOp().getSolution());
        if (getFinalSolution() != null) {
            // Evaluate the solution with the ALNS cost delegate
            getFinalSolution().setCostDelegate(getParams().newALNSCostDelegate(null));
        }
    }

    /**
     * Set the {@link AdaptiveLargeNeighborhoodSearch ALNS} algorithm
     * 
     * @param alns
     */
    private void setALNS(AdaptiveLargeNeighborhoodSearch<TRSPSolution> alns) {
        mALNS = alns;
    }

    /**
     * Set the solution reported as the one returned by the ALNS algorithm
     * 
     * @param alnsSol
     *            the solution
     */
    protected void setALNSSol(TRSPSolution alnsSol) {
        mALNSSol = alnsSol;
    }

    /**
     * Set the {@link TRSPConstructiveHeuristic} heuristic used to build an initial solution
     * 
     * @param init
     *            the constructive heuristic
     */
    private void setInit(TRSPConstructiveHeuristic init) {
        mInit = init;
    }

    /**
     * Set the initial solution that will be used as a starting point in the ALNS.
     * 
     * @param initSol
     *            the initial solution
     */
    @Override
    public void setInitSol(TRSPSolution initSol) {
        mInitSol = initSol;
        if (mInitPool != null) {
            mInitPool.clear();
            mInitPool.add(mInitSol);
        }
    }

    /**
     * Set the logger that will be attached to the ALN
     * 
     * @param logger
     *            a logger
     */
    public void setALNSLogger(ALNSLogger<TRSPSolution> logger) {
        mLogger = logger;
    }

    /**
     * Set the post optimization solver
     * 
     * @param postOp
     */
    private void setPostOp(SCGurobiSolver postOp) {
        mPostOp = postOp;
    }

    @Override
    public void dispose() {
        if (getInit() != null)
            getInit().dispose();
        if (getALNS() != null)
            getALNS().dispose();
        if (getPostOp() != null)
            getPostOp().dispose();
    }

}
