/**
 *
 */
package vroom.trsp.util;

import gurobi.GRB;
import umontreal.iro.lecuyer.rng.MRG32k3a;
import umontreal.iro.lecuyer.rng.RandomStream;
import vroom.common.heuristics.alns.ALNSComponentHandler;
import vroom.common.heuristics.alns.IPALNSSolutionPool;
import vroom.common.heuristics.alns.SimpleSolutionPool;
import vroom.common.utilities.IDistance;
import vroom.common.utilities.Utilities.Math.DeviationMeasure;
import vroom.common.utilities.optimization.IComponentHandler;
import vroom.common.utilities.params.ClassParameterKey;
import vroom.common.utilities.params.GlobalParameters;
import vroom.common.utilities.params.ParameterKey;
import vroom.common.utilities.params.ParameterKey.BooleanParameterKey;
import vroom.common.utilities.params.ParameterKey.DoubleParameterKey;
import vroom.common.utilities.params.ParameterKey.IntegerParameterKey;
import vroom.common.utilities.params.ParameterKey.StringParameterKey;
import vroom.common.utilities.params.RequiredParameter;
import vroom.common.utilities.ssj.IRandomSource;
import vroom.trsp.TRSPSolver;
import vroom.trsp.datamodel.ITRSPSolutionHasher;
import vroom.trsp.datamodel.NodeSetSolutionHasher;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.datamodel.costDelegates.TRSPCostDelegate;
import vroom.trsp.datamodel.costDelegates.TRSPLevenshteinDistance;
import vroom.trsp.datamodel.costDelegates.TRSPTourBalance;
import vroom.trsp.datamodel.costDelegates.TRSPWorkingTime;
import vroom.trsp.optimization.biobj.HierarchicalParetoSelector;
import vroom.trsp.optimization.constructive.TRSPConstructiveHeuristic;

/**
 * <code>TRSPGlobalParameters</code> is a containing class to store the values of the the TRSP algorithm
 * <p>
 * Creation date: May 19, 2011 - 10:51:00 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TRSPGlobalParameters extends GlobalParameters implements IRandomSource {

    // Adaptive destroy/repair selection
    /**
     * The length <em>l</em> of a segment (in number of iterations) in the adaptive process
     */
    public static final IntegerParameterKey                          ALNS_A_L                      = new IntegerParameterKey(
                                                                                                           "ALNS_A_L",
                                                                                                           100);

    /** The reaction factor <em>r</em> */
    public static final DoubleParameterKey                           ALNS_A_R                      = new DoubleParameterKey(
                                                                                                           "ALNS_A_R",
                                                                                                           0.1);

    /** Value for &sigma;<sub>1</sub>, the score increment for new best solution */
    public static final DoubleParameterKey                           ALNS_A_SIGMA1                 = new DoubleParameterKey(
                                                                                                           "ALNS_A_SIGMA1",
                                                                                                           33d);

    /**
     * Value for &sigma;<sub>2</sub>, the score increment for improving new current solution
     */
    public static final DoubleParameterKey                           ALNS_A_SIGMA2                 = new DoubleParameterKey(
                                                                                                           "ALNS_A_SIGMA2",
                                                                                                           9d);
    /**
     * Value for &sigma;<sub>3</sub>, the score increment for non-improving new current solution
     */
    public static final DoubleParameterKey                           ALNS_A_SIGMA3                 = new DoubleParameterKey(
                                                                                                           "ALNS_A_SIGMA3",
                                                                                                           13d);
    @SuppressWarnings("rawtypes")
    public static final ClassParameterKey<IComponentHandler>         ALNS_COMP_HANDLER             = new ClassParameterKey<IComponentHandler>(
                                                                                                           "ALNS_COMP_HANDLER",
                                                                                                           IComponentHandler.class,
                                                                                                           ALNSComponentHandler.class);
    // Cost delegates
    /** The cost delegate used in the ALNS procedure */
    public static final ClassParameterKey<TRSPCostDelegate>          ALNS_COST_DELEGATE            = new ClassParameterKey<TRSPCostDelegate>(
                                                                                                           "ALNS_COST_DELEGATE",
                                                                                                           TRSPCostDelegate.class,
                                                                                                           TRSPWorkingTime.class);
    // Destroy operators
    /**
     * The randomness parameter <em>p</em> of destroy procedures (p ≥ 1, the lower p, the more randomness is introduced)
     */
    public static final DoubleParameterKey                           ALNS_DES_P                    = new DoubleParameterKey(
                                                                                                           "ALNS_DES_P",
                                                                                                           8d);
    // Repair operators
    /**
     * The noise switch <em>&eta;</em> for repair operators
     */
    public static final BooleanParameterKey                          ALNS_REP_NOISE                = new BooleanParameterKey(
                                                                                                           "ALNS_REP_NOISE",
                                                                                                           Boolean.TRUE);

    /**
     * The noise parameter <em>&eta;</em> for repair operators
     */
    public static final DoubleParameterKey                           ALNS_REP_ETA                  = new DoubleParameterKey(
                                                                                                           "ALNS_REP_ETA",
                                                                                                           0.025d);
    @RequiredParameter
    /** The maximum number of iterations*/
    public static final IntegerParameterKey                          ALNS_MAX_IT                   = new IntegerParameterKey(
                                                                                                           "ALNS_MAX_IT"

                                                                                                   );

    @RequiredParameter
    /** The maximum time allowed for the ALNS to finish*/
    public static final IntegerParameterKey                          ALNS_MAX_TIME                 = new IntegerParameterKey(
                                                                                                           "ALNS_MAX_TIME");

    // Objective function
    /**
     * The penalty for unserved request in the objective function, expressed as the proportion of the initial solution
     * cost
     */
    public static final DoubleParameterKey                           ALNS_OBJ_GAMMA                = new DoubleParameterKey(
                                                                                                           "ALNS_OBJ_GAMMA",
                                                                                                           1.0d);

    /** The number of iterations to be performed in parallel */
    public static final IntegerParameterKey                          ALNS_PALNS_IT_P               = new IntegerParameterKey(
                                                                                                           "ALNS_PALNS_IT_P");

    // Main parameters

    /** The type of {@linkplain IPALNSSolutionPool solution pool} */
    @SuppressWarnings("rawtypes")
    public static final ClassParameterKey<IPALNSSolutionPool>        ALNS_PALNS_POOL               = new ClassParameterKey<IPALNSSolutionPool>(
                                                                                                           "ALNS_PALNS_POOL",
                                                                                                           IPALNSSolutionPool.class,
                                                                                                           SimpleSolutionPool.class);
    /** {@code true} iif the diversified solution pool should auto-adjust its weights */
    public static final BooleanParameterKey                          ALNS_PALNS_DIV_AUTOADJUST     = new BooleanParameterKey(
                                                                                                           "ALNS_PALNS_DIV_AUTOADJUST",
                                                                                                           Boolean.TRUE);

    /** The type of {@linkplain IDistance diversity metric} */
    @SuppressWarnings("rawtypes")
    public static final ClassParameterKey<IDistance>                 ALNS_PALNS_DIV_METRIC         = new ClassParameterKey<IDistance>(
                                                                                                           "ALNS_PALNS_DIV_METRIC",
                                                                                                           IDistance.class,
                                                                                                           TRSPLevenshteinDistance.class);
    /** The size of the PALNS pool */
    public static final IntegerParameterKey                          ALNS_PALNS_POOL_SIZE          = new IntegerParameterKey(
                                                                                                           "ALNS_PALNS_POOL_SIZE",
                                                                                                           10);
    /** {@code true} iif the parallel version of the ALNS should be used */
    public static final BooleanParameterKey                          ALNS_PARALLEL                 = new BooleanParameterKey(
                                                                                                           "ALNS_PARALLEL",
                                                                                                           Boolean.TRUE);

    /**
     * Fraction of the initial temperature to be reached at the termination of the method (default 0.002 for 0.2%)
     */
    public static final DoubleParameterKey                           ALNS_SA_ALPHA                 = new DoubleParameterKey(
                                                                                                           "ALNS_SA_ALPHA",
                                                                                                           0.002);
    /**
     * Initial probability of accepting a solution representing a degradation of {@linkplain #ALNS_SA_W <em>w</em>}
     * (default 0.5)
     */
    public static final DoubleParameterKey                           ALNS_SA_P                     = new DoubleParameterKey(
                                                                                                           "ALNS_SA_P",
                                                                                                           0.5);
    /**
     * Reference degradation <em>w</em> for the determination of the start temperature (default 0.05)
     */
    public static final DoubleParameterKey                           ALNS_SA_W                     = new DoubleParameterKey(
                                                                                                           "ALNS_SA_W",
                                                                                                           0.05);
    // Simulated annealing
    /** Minimum proportion &xi;<sub>min</sub> of requests to be removed at each destroy */
    public static final DoubleParameterKey                           ALNS_XI_MIN                   = new DoubleParameterKey(
                                                                                                           "ALNS_XI_MIN",
                                                                                                           0.1);
    /** Maximum proportion &xi;<sub>max</sub> of requests to be removed at each destroy */
    public static final DoubleParameterKey                           ALNS_XI_MAX                   = new DoubleParameterKey(
                                                                                                           "ALNS_XI_MAX",
                                                                                                           0.4);
    /** A flag use to enable the logging of each temporary solution */
    public static final BooleanParameterKey                          ALNS_ENABLE_LOGGING           = new BooleanParameterKey(
                                                                                                           "ALNS_ENABLE_LOGGING",
                                                                                                           Boolean.FALSE);
    /** The cost delegate used in the ALNS procedure */
    public static final ParameterKey<DeviationMeasure>               BALANCE_COST_DELEGATE_MEASURE = new ParameterKey<DeviationMeasure>(
                                                                                                           "BALANCE_COST_DELEGATE_MEASURE",
                                                                                                           DeviationMeasure.class,
                                                                                                           DeviationMeasure.MaxMinGap);
    /** The cost delegate used in the ALNS procedure */
    public static final DoubleParameterKey                           BALANCE_COST_DELEGATE_PENALTY = new DoubleParameterKey(
                                                                                                           "BALANCE_COST_DELEGATE_PENALTY",
                                                                                                           1d);

    /**
     * The {@linkplain HierarchicalParetoSelector#getAllowedDegradation() allowed degradation} for solution selection
     */
    public static final DoubleParameterKey                           BIOBJ_ALLOWED_DEG             = new DoubleParameterKey(
                                                                                                           "BIOBJ_ALLOWED_DEG",
                                                                                                           1.0d);

    /**
     * {@code true} if LS should be enabled for bi-objective variant
     */
    public static final BooleanParameterKey                          BIOBJ_ENABLE_LS               = new BooleanParameterKey(
                                                                                                           "BIOBJ_ENABLE_LS",
                                                                                                           Boolean.TRUE);
    /**
     * {@code true} the problem is a bi-objective variant
     */
    public static final BooleanParameterKey                          BIOBJ_ENABLE_PR               = new BooleanParameterKey(
                                                                                                           "BIOBJ_ENABLE_PR",
                                                                                                           Boolean.TRUE);
    /**
     * A flag set to {@code true} if the initial solution should be used as reference solution in biobjective setting,
     * {@code false} if the previous solution should be used
     */
    public static final BooleanParameterKey                          BIOBJ_INIT_AS_REF             = new BooleanParameterKey(
                                                                                                           "BIOBJ_INIT_AS_REF",
                                                                                                           Boolean.TRUE);

    public static final boolean                                      CTR_CHK_FWD_FEAS              = true;
    /** The cost delegate used in the initialization phase */
    public static final ClassParameterKey<TRSPCostDelegate>          INIT_COST_DELEGATE            = new ClassParameterKey<TRSPCostDelegate>(
                                                                                                           "INIT_COST_DELEGATE",
                                                                                                           TRSPCostDelegate.class,
                                                                                                           TRSPTourBalance.class);

    /** The cost delegate used in the randomized heuristic solver procedure */
    public static final ClassParameterKey<TRSPCostDelegate>          RCH_COST_DELEGATE             = new ClassParameterKey<TRSPCostDelegate>(
                                                                                                           "RCH_COST_DELEGATE",
                                                                                                           TRSPCostDelegate.class,
                                                                                                           TRSPWorkingTime.class);
    /**
     * The type of heuristic used for RCH: {@code true} to use giant tours and split, {@code false} to generate only
     * feasible tours
     */
    public static final BooleanParameterKey                          RCH_GIANT_SPLIT               = new BooleanParameterKey(
                                                                                                           "RCH_GIANT_SPLIT",
                                                                                                           Boolean.TRUE);
    /**
     * {@code true} if TW feasibility should be checked when building a giant tour
     */
    public static final BooleanParameterKey                          RCH_GIANT_TW_CHECK            = new BooleanParameterKey(
                                                                                                           "RCH_GIANT_TW_CHECK",
                                                                                                           Boolean.TRUE);
    /**
     * A list of the constructive heuristics to be used, with the randomization parameter in parenthesis (e.g.,
     * RNN(6),RNI(3))
     */
    @RequiredParameter
    public static final StringParameterKey                           RCH_HEURISTICS                = new StringParameterKey(
                                                                                                           "RCH_HEURISTICS");
    @RequiredParameter
    /**
     * The number of tour generation iterations of the randomized constructive heuristic solver.
     */
    public static final IntegerParameterKey                          RCH_MAX_IT                    = new IntegerParameterKey(
                                                                                                           "RCH_MAX_IT"

                                                                                                   );

    /** The solution hasher used in the randomized constructive heuristic solver */
    public static final ClassParameterKey<ITRSPSolutionHasher>       RCH_POOL_HASHER               = new ClassParameterKey<ITRSPSolutionHasher>(
                                                                                                           "RCH_POOL_HASHER",
                                                                                                           ITRSPSolutionHasher.class,
                                                                                                           NodeSetSolutionHasher.class);

    /**
     * The randomization factor of the randomized constructive heuristics, between 1 (high randomness) and +infty (no
     * randomness) (default: 5)
     */
    public static final DoubleParameterKey                           RCH_RND_FACTOR                = new DoubleParameterKey(
                                                                                                           "RCH_RND_FACTOR",
                                                                                                           5d);
    /**
     * {@code true} if PR should be enabled for bi-objective variant
     */
    public static final BooleanParameterKey                          RUN_BIOBJ                     = new BooleanParameterKey(
                                                                                                           "RUN_BIOBJ",
                                                                                                           Boolean.FALSE);
    /** The path to the file containing the best known solutions */
    @RequiredParameter
    public static final StringParameterKey                           RUN_BKS_FILE                  = new StringParameterKey(
                                                                                                           "RUN_BKS_FILE");

    @RequiredParameter
    /** The type of instances: <code>true</code> for instances of the sCVRPTW,
     *            <code>false</code> for instances of the TRSP (default: <code>false</code>)*/
    public static final BooleanParameterKey                          RUN_CVRPTW                    = new BooleanParameterKey(
                                                                                                           "RUN_CVRPTW",
                                                                                                           Boolean.FALSE);
    /** A flag to force the definition of the fwd slack time in TRSPSolution even if {@code  RUN_CVRPTW=true} */
    public static final BooleanParameterKey                          FORCE_FWD_SLK                 = new BooleanParameterKey(
                                                                                                           "FORCE_FWD_SLK",
                                                                                                           Boolean.FALSE);

    /**
     * <code>true</code> to ensure the reproducibility of runs, <code>false</code> to improve performance
     */
    @RequiredParameter
    public static final BooleanParameterKey                          RUN_ENSURE_REP                = new BooleanParameterKey(
                                                                                                           "RUN_ENSURE_REP",
                                                                                                           Boolean.TRUE);

    /** The file pattern of the files containing instances to be run */
    @RequiredParameter
    public static final StringParameterKey                           RUN_FILE_PATTERN              = new StringParameterKey(
                                                                                                           "RUN_FILE_PATTERN",
                                                                                                           "R?C?\\d+.100_\\d+\\-\\d+\\-\\d+\\-\\d+.txt");

    /** The folder containing instances to be run */
    @RequiredParameter
    public static final StringParameterKey                           RUN_INSTANCE_FOLDER           = new StringParameterKey(
                                                                                                           "RUN_INSTANCE_FOLDER");

    /** The number of replicas for each instance */
    @RequiredParameter
    public static final IntegerParameterKey                          RUN_NUM_REPLICAS              = new IntegerParameterKey(
                                                                                                           "RUN_NUM_REPLICAS",
                                                                                                           1);

    /** The folder containing the dynamic info of instances to be run */
    public static final StringParameterKey                           RUN_REL_DATE_FOLDER           = new StringParameterKey(
                                                                                                           "RUN_REL_DATE_FOLDER");

    /** The degrees of dynamism accepted */
    public static final ParameterKey<int[]>                          RUN_DODS                      = new ParameterKey<int[]>(
                                                                                                           "RUN_DODS",
                                                                                                           int[].class);

    /** The seeds for the run */
    @RequiredParameter
    public static final ParameterKey<long[]>                         RUN_SEEDS                     = new ParameterKey<long[]>(
                                                                                                           "RUN_SEEDS",
                                                                                                           long[].class);

    /** The run class that will be used */
    public static final ClassParameterKey<?>                         RUN_CLASS                     = new ClassParameterKey<>(
                                                                                                           "RUN_CLASS",
                                                                                                           Object.class);

    /** The solver that will be used in this run */
    public static final ClassParameterKey<TRSPSolver>                RUN_SOLVER                    = new ClassParameterKey<TRSPSolver>(
                                                                                                           "RUN_SOLVER",
                                                                                                           TRSPSolver.class);

    /** {@code true} if only the regret repair should be used to solve time slices */
    public static final BooleanParameterKey                          RUN_REGRET_ONLY               = new BooleanParameterKey(
                                                                                                           "RUN_REGRET_ONLY",
                                                                                                           false);

    /** The constructive heuristic that will be used in this run */
    public static final ClassParameterKey<TRSPConstructiveHeuristic> RUN_INIT_HEUR                 = new ClassParameterKey<TRSPConstructiveHeuristic>(
                                                                                                           "RUN_INIT_HEUR",
                                                                                                           TRSPConstructiveHeuristic.class);
    /** The number of parallels runs */
    @RequiredParameter
    public static final IntegerParameterKey                          RUN_THREADS                   = new IntegerParameterKey(
                                                                                                           "RUN_THREADS",
                                                                                                           1);
    /** The speed of the simulation ({@code  simTime = wallTime * speed} ) */
    @RequiredParameter
    public static final DoubleParameterKey                           RUN_SIM_SPEED                 = new DoubleParameterKey(
                                                                                                           "RUN_SIM_SPEED",
                                                                                                           1d);
    /**
     * The base duration of the simulation with 100% dynamic requests (overrides {@link #RUN_SIM_SPEED})
     */
    public static final DoubleParameterKey                           RUN_SIM_DURATION              = new DoubleParameterKey(
                                                                                                           "RUN_SIM_DURATION");
    /** A value between 0 and 1 indicating until which time of the horizon the algorithm should expect new requests */
    @RequiredParameter
    public static final DoubleParameterKey                           RUN_CUTOFF_TIME               = new DoubleParameterKey(
                                                                                                           "RUN_CUTOFF_TIME",
                                                                                                           1d);

    /**
     * <code>true</code> to enable the writing of the mathematical model to a file
     */
    @RequiredParameter
    public static final BooleanParameterKey                          RUN_WRITE_LP                  = new BooleanParameterKey(
                                                                                                           "RUN_WRITE_LP",
                                                                                                           Boolean.FALSE);
    /**
     * <code>true</code> to enable the debug mode
     */
    public static final BooleanParameterKey                          RUN_DEBUG                     = new BooleanParameterKey(
                                                                                                           "RUN_DEBUG",
                                                                                                           Boolean.FALSE);

    // Cost delegates
    /** The cost delegate used in the ALNS procedure */
    public static final ClassParameterKey<TRSPCostDelegate>          SC_COST_DELEGATE              = new ClassParameterKey<TRSPCostDelegate>(
                                                                                                           "SC_COST_DELEGATE",
                                                                                                           TRSPCostDelegate.class,
                                                                                                           TRSPWorkingTime.class);

    /** Enable/Disable the set covering post-optimization */
    public static final BooleanParameterKey                          SC_ENABLED                    = new BooleanParameterKey(
                                                                                                           "SC_ENABLED",
                                                                                                           Boolean.TRUE);

    /** Enable/Disable the set covering post-optimization */
    public static final BooleanParameterKey                          TOUR_POOL_ENABLED             = new BooleanParameterKey(
                                                                                                           "TOUR_POOL_ENABLED",
                                                                                                           Boolean.FALSE);

    @RequiredParameter
    /** The type of subproblem to solve: <code>true</code> for set partitioning, <code>false</code> for set covering (default)*/
    public static final BooleanParameterKey                          SC_FORCE_EQUAL                = new BooleanParameterKey(
                                                                                                           "SC_FORCE_EQUAL",
                                                                                                           Boolean.FALSE);

    @RequiredParameter
    /** The path of the file containing the values for the GRBEnv*/
    public static final StringParameterKey                           SC_GRBENV_FILE                = new StringParameterKey(
                                                                                                           "SC_GRBENV_FILE",
                                                                                                           "dafaults_grb.env");

    @RequiredParameter
    /** The maximum time allowed for the post optimization to finish (in seconds)*/
    public static final DoubleParameterKey                           SC_MAX_TIME                   = new DoubleParameterKey(
                                                                                                           "SC_MAX_TIME",
                                                                                                           GRB.INFINITY);

    /** The path of the file containing the MPA parameters */
    public static final StringParameterKey                           MPA_CONFIG_FILE               = new StringParameterKey(
                                                                                                           "MPA_CONFIG_FILE");                            ;

    @RequiredParameter
    /** The number of Threads to use for parallelization*/
    public static final IntegerParameterKey                          THREAD_COUNT                  = new IntegerParameterKey(
                                                                                                           "THREAD_COUNT",
                                                                                                           Runtime.getRuntime()
                                                                                                                   .availableProcessors());
    /** The path of a file that contains the experimental setting */
    public static final StringParameterKey                           EXPE_CONFIG_FILE              = new StringParameterKey(
                                                                                                           "EXPE_CONFIG_FILE");

    private RandomStream                                             mALNSRndStream;
    private RandomStream                                             mInitRndStream;

    private RandomStream                                             mHashRndStream;

    private RandomStream                                             mMainRndStream;

    private RandomStream                                             mRCHRndStream;

    @Override
    public TRSPGlobalParameters clone() {
        return (TRSPGlobalParameters) super.clone();
    }

    /**
     * Returns the random stream used in the initialization procedure
     * 
     * @return the random stream used in the initialization procedure
     */
    public RandomStream getInitRndStream() {
        initRndStreams();
        return mInitRndStream;
    }

    /**
     * Returns the random stream used in the ALNS procedure
     * 
     * @return the random stream used in the ALNS procedure
     */
    public RandomStream getALNSRndStream() {
        initRndStreams();
        return mALNSRndStream;
    }

    /**
     * Returns the random stream used in the hashing procedure
     * 
     * @return the random stream used in the hashing procedure
     */
    public RandomStream getHashRndStream() {
        initRndStreams();
        return mHashRndStream;
    }

    @Override
    public RandomStream getRandomStream() {
        initRndStreams();
        return mMainRndStream;
    }

    /**
     * Returns the random stream used in the RCH procedure
     * 
     * @return the random stream used in the RCH procedure
     */
    public RandomStream getRCHRndStream() {
        initRndStreams();
        return mRCHRndStream;
    }

    /**
     * Returns the number of threads to be used
     * <p>
     * This method returns the minimum between the number of available processors and the {@link #THREAD_COUNT} value
     * </p>
     * 
     * @return the number of threads to be used
     */
    public int getThreadCount() {
        return get(THREAD_COUNT);
    }

    /**
     * Returns the simulation speed depending on the degree of dynamism and the length of the planning horizon
     * 
     * @param instance
     * @return the simulation speed
     */
    public double getSimSpeed(TRSPInstance instance) {
        if (isSet(RUN_SIM_DURATION)) {
            double horiz = instance.getMainDepot().getTimeWindow().width();
            double dod = instance.getDod();
            double dur = get(RUN_SIM_DURATION);
            return (horiz / dur) / dod;
        } else {
            return get(RUN_SIM_SPEED);
        }
    }

    private void initRndStreams() {
        if (mMainRndStream == null) {
            mMainRndStream = new MRG32k3a("TRSP_Main");
            ((MRG32k3a) mMainRndStream).setSeed(get(RUN_SEEDS));

            mInitRndStream = new MRG32k3a("TRSP_ALNS");
            ((MRG32k3a) mInitRndStream).setSeed(get(RUN_SEEDS));

            mALNSRndStream = new MRG32k3a("TRSP_ALNS");
            ((MRG32k3a) mALNSRndStream).setSeed(get(RUN_SEEDS));

            mRCHRndStream = new MRG32k3a("TRSP_RCH");
            ((MRG32k3a) mRCHRndStream).setSeed(get(RUN_SEEDS));

            mHashRndStream = new MRG32k3a("TRSP_Hash");
            ((MRG32k3a) mHashRndStream).setSeed(get(RUN_SEEDS));
        }
    };

    /**
     * Returns {@code true} if the problem at hand is bi-objective, {@code false} otherwise
     * 
     * @return {@code true} if the problem at hand is bi-objective, {@code false} otherwise
     */
    public boolean isBiObjective() {
        return get(RUN_BIOBJ);
    }

    /**
     * Returns <code>true</code> if instances that will be solved in this VM are instances of the sCVRPTW,
     * <code>false</code> if they are instances of the TRSP
     * 
     * @return <code>true</code> if instances that will be solved in this VM are instances of the sCVRPTW,
     *         <code>false</code> if they are instances of the TRSP
     */
    public boolean isCVRPTW() {
        return get(RUN_CVRPTW);
    }

    /**
     * Returns {@code true} if the problem at hand is dynamic, {@code false} otherwise
     * 
     * @return {@code true} if the problem at hand is dynamic, {@code false} otherwise
     */
    public boolean isDynamic() {
        return isSet(RUN_REL_DATE_FOLDER);
    }

    /**
     * Instantiate a new cost delegate for the ALNS of the type defined by {@link #ALNS_COST_DELEGATE}
     * 
     * @param initialSolution
     *            the initial solution of the ALNS
     * @return a new ALNS cost delegate
     * @see #ALNS_COST_DELEGATE
     */
    public TRSPCostDelegate newALNSCostDelegate(TRSPSolution initialSolution) {
        Object[] args = new Object[0];
        Class<?>[] classes = new Class<?>[0];
        Class<?> clazz = get(ALNS_COST_DELEGATE);
        if (TRSPTourBalance.class == clazz) {
            return new TRSPTourBalance(initialSolution.getCostDelegate(),
                    get(BALANCE_COST_DELEGATE_MEASURE), get(BALANCE_COST_DELEGATE_PENALTY));
        } else {
            return newInstanceSafe(ALNS_COST_DELEGATE, classes, args);
        }
    }

    /**
     * Instantiate a new cost delegate for the initialization of the type defined by {@link #INIT_COST_DELEGATE}
     * 
     * @return a new ALNS cost delegate
     * @see #INIT_COST_DELEGATE
     */
    public TRSPCostDelegate newInitCostDelegate() {
        return newInstance(INIT_COST_DELEGATE);
    }

    /**
     * Instantiate a new cost delegate for the set covering of the type defined by {@link #SC_COST_DELEGATE}
     * 
     * @return a new ALNS cost delegate
     * @see #SC_COST_DELEGATE
     */
    public TRSPCostDelegate newSCCostDelegate() {
        return newInstance(SC_COST_DELEGATE);
    }

    @Override
    public <T> T set(vroom.common.utilities.params.ParameterKey<? super T> key, T value) {
        if (key == RUN_SEEDS && mMainRndStream != null)
            throw new IllegalStateException(
                    "Cannot set the RUN_SEEDS once the random stream has been initialized");
        return super.set(key, value);
    }

    @Override
    public Object setNoCheck(ParameterKey<?> key, Object value) {
        return super.setNoCheck(key, value);
    }

    @Override
    public void setRandomStream(RandomStream stream) {
        throw new UnsupportedOperationException("Cannot set the base random stream");
    }
}
