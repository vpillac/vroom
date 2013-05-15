package vroom.common.heuristics.vns.benchmarking;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import umontreal.iro.lecuyer.rng.MRG32k3a;
import vroom.common.heuristics.ConstraintHandler;
import vroom.common.heuristics.cw.CWParameters;
import vroom.common.heuristics.cw.algorithms.RandomizedSavingsHeuristic;
import vroom.common.heuristics.cw.kernel.ClarkeAndWrightHeuristic;
import vroom.common.heuristics.vns.VNSParameters;
import vroom.common.heuristics.vns.VariableNeighborhoodSearch;
import vroom.common.heuristics.vns.VariableNeighborhoodSearch.VNSVariant;
import vroom.common.heuristics.vrp.OrOptNeighborhood;
import vroom.common.heuristics.vrp.StringExchangeNeighborhood;
import vroom.common.heuristics.vrp.SwapNeighborhood;
import vroom.common.heuristics.vrp.TwoOptNeighborhood;
import vroom.common.heuristics.vrp.constraints.CapacityConstraint;
import vroom.common.heuristics.vrp.constraints.FixedNodesConstraint;
import vroom.common.modeling.dataModel.ListRoute.ArrayListRoute;
import vroom.common.modeling.dataModel.DoublyLinkedRoute;
import vroom.common.modeling.dataModel.Fleet;
import vroom.common.modeling.dataModel.IRoute;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.dataModel.IVRPSolution;
import vroom.common.modeling.dataModel.Solution;
import vroom.common.modeling.dataModel.Vehicle;
import vroom.common.modeling.io.ChristofidesPersistenceHelper;
import vroom.common.modeling.io.FlatFilePersistenceHelper;
import vroom.common.modeling.io.NovoaPersistenceHelper;
import vroom.common.modeling.io.TSPLibPersistenceHelper;
import vroom.common.modeling.util.DefaultSolutionFactory;
import vroom.common.modeling.util.ISolutionFactory;
import vroom.common.modeling.util.SolutionChecker;
import vroom.common.utilities.StatCollector;
import vroom.common.utilities.StatCollector.Label;
import vroom.common.utilities.Stopwatch;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.common.utilities.optimization.INeighborhood;
import vroom.common.utilities.optimization.OptimizationSense;

/**
 * <code>RouteDataStructureBenchmarking</code> is used to test performance of different route data structures
 * <p>
 * Creation date: 9/09/2010 - 10:21:19
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
@SuppressWarnings("unused")
public class RouteDataStructureBenchmark implements Runnable {
    public final static String                            CHRISTOFIDES_INSTANCES_PATH = "../Instances/cvrp/christodifes-mingozzi-toth";
    public final static String                            CHRISTOFIDES_SOLUTION_FILE  = "../Instances/cvrp/christodifes-mingozzi-toth.sol";

    private final static String                           INSTANCE_PATH               = CHRISTOFIDES_INSTANCES_PATH;
    private final static String                           BKS_FILE                    = CHRISTOFIDES_SOLUTION_FILE;
    private final static String                           BENCH_NAME                  = "christofides";
    private final static String                           INSTANCE_FILTER             = "vrpnc";
    private final static boolean                          RESET_ON_SIZE_CHANGE        = false;
    protected final static FlatFilePersistenceHelper      INSTANCE_READER             = new ChristofidesPersistenceHelper();

    private final static File                             INSTANCES_DIRECTORY         = new File(
                                                                                              INSTANCE_PATH);

    protected final static List<File>                     INSTANCES_FILES;

    static {
        String[] children = INSTANCES_DIRECTORY.list();
        if (children == null) {
            // Either dir does not exist or is not a directory
            INSTANCES_FILES = null;
            System.err.println("Invalid directory :" + INSTANCES_DIRECTORY);
            System.exit(1);
        } else {
            INSTANCES_FILES = new LinkedList<File>();
            for (String element : children) {
                if (element.startsWith(INSTANCE_FILTER)) {
                    INSTANCES_FILES.add(new File(INSTANCES_DIRECTORY.getAbsolutePath()
                            + File.separator + element));
                }
            }
        }
    }

    private static String                                 sStatFile                   = "benchmarks/route_banch.csv";

    private static final Label<?>[]                       LABELS                      = new Label<?>[] {
            new Label<String>("comment", String.class),
            new Label<Integer>("run_id", Integer.class),
            new Label<String>("instance", String.class), new Label<Double>("obj", Double.class),
            new Label<Double>("cw_time", Double.class),
            new Label<Double>("vns_time", Double.class), new Label<Long>("total_time", Long.class) };

    private static StatCollector                          sCollector                  = new StatCollector(
                                                                                              new File(
                                                                                                      sStatFile),
                                                                                              true,
                                                                                              false,
                                                                                              "RouteDataStructurBenchmarks",
                                                                                              LABELS);

    private final ISolutionFactory                        mSolutionFactory;

    private final IVRPInstance                            mInstance;

    private final VariableNeighborhoodSearch<Solution<?>> mVNS;

    private final ClarkeAndWrightHeuristic<Solution<?>>   mCW;

    private Solution<?>                                   mSolution;

    private final int                                     mRunId;

    private final String                                  mRunComment;

    private final long                                    mSeed;

    /**
     * Creates a new <code>RouteDataStructureBenchmarking</code>
     * 
     * @param solutionFactories
     */
    protected RouteDataStructureBenchmark(int runId, String comment,
            ISolutionFactory solutionFactory, IVRPInstance instance, long seed) {
        super();
        mRunId = runId;
        mRunComment = comment;
        mSolutionFactory = solutionFactory;
        mInstance = instance;
        mSeed = seed;
        List<INeighborhood<Solution<?>, ?>> neighborhoods = new LinkedList<INeighborhood<Solution<?>, ?>>();

        ConstraintHandler<Solution<?>> ctr = new ConstraintHandler<Solution<?>>();
        ctr.addConstraint(new FixedNodesConstraint<Solution<?>>());
        ctr.addConstraint(new CapacityConstraint<Solution<?>>());

        neighborhoods.add(new SwapNeighborhood<Solution<?>>(ctr));
        neighborhoods.add(new TwoOptNeighborhood<Solution<?>>(ctr));
        neighborhoods.add(new OrOptNeighborhood<Solution<?>>(ctr));
        neighborhoods.add(new StringExchangeNeighborhood<Solution<?>>(ctr));

        mVNS = VariableNeighborhoodSearch.newVNS(VNSVariant.VND, OptimizationSense.MINIMIZATION,
                null, new MRG32k3a(), neighborhoods);

        CWParameters params = new CWParameters();
        params.set(CWParameters.RANDOM_SEED, seed);
        mCW = new ClarkeAndWrightHeuristic<Solution<?>>(params, RandomizedSavingsHeuristic.class,
                ctr);
        mCW.setSolutionFactory(mSolutionFactory);
    }

    @Override
    public void run() {
        VNSParameters params = new VNSParameters(60000, 10000, false, 60000, 100, null, null);

        System.out
                .printf("%s\t %s (seed:%s) %s\n", mRunId, mInstance.getName(), mSeed, mRunComment);

        mCW.initialize(mInstance);

        Stopwatch t = new Stopwatch();
        t.start();
        mCW.run();
        mSolution = mCW.getSolution();
        t.stop();
        double cwTime = t.readTimeMS();

        t.restart();
        mVNS.localSearch(mInstance, mSolution, params);
        t.stop();

        double vnsTime = t.readTimeMS();

        SolutionChecker.checkSolution(mSolution, true, true, true);
        sCollector.collect(mRunComment, mRunId, mInstance.getName(), mSolution.getCost(), cwTime,
                vnsTime, cwTime + vnsTime);
    }

    public static void main(String[] args) {
        int nRuns = 5;

        System.out.println("=================================");
        System.out.println("Running benchmarks for instances in folder");
        System.out.println(INSTANCES_DIRECTORY);
        System.out.println("Stats saved in   : " + sStatFile);
        System.out.println("Runs per instance: " + nRuns);
        System.out.println("=================================");

        LoggerHelper.setupRootLogger(LoggerHelper.LEVEL_WARN, LoggerHelper.LEVEL_WARN, true);

        LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 2, 1, TimeUnit.MINUTES, workQueue);

        List<IVRPInstance> instances = readInstances();

        ISolutionFactory llFact = new DefaultSolutionFactory() {
            @Override
            public IRoute<?> newRoute(IVRPSolution<?> solution, Vehicle vehicle, Object... params) {
                return new DoublyLinkedRoute(solution, vehicle);
            };
        };
        ISolutionFactory alFact = new DefaultSolutionFactory() {
            @Override
            public IRoute<?> newRoute(IVRPSolution<?> solution, Vehicle vehicle, Object... params) {
                return new ArrayListRoute(solution, vehicle);
            };
        };

        System.out.println("=================================");
        int runId = 0;
        for (IVRPInstance instance : instances) {
            for (int seed = 0; seed < nRuns; seed++) {
                executor.execute(new RouteDataStructureBenchmark(runId, "LinkedList", llFact,
                        instance, seed));
                executor.execute(new RouteDataStructureBenchmark(runId, "ArrayList", alFact,
                        instance, seed));
                runId++;
            }
        }

        executor.shutdown();

        try {
            executor.awaitTermination(60, TimeUnit.MINUTES);
            System.out.println("=================================");
            System.out.println("FINISHED");
            System.out.println("=================================");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }

    private static List<IVRPInstance> readInstances() {

        List<IVRPInstance> instances = new LinkedList<IVRPInstance>();
        for (File f : INSTANCES_FILES) {
            if (!f.getName().equals("1_info.txt") && !f.getName().equals(".svn")) {
                try {
                    IVRPInstance[] ins;
                    if (INSTANCE_READER instanceof TSPLibPersistenceHelper) {
                        ins = new IVRPInstance[] { INSTANCE_READER.readInstance(f, 100) };
                        int k = Integer.valueOf(ins[0].getName().substring(
                                ins[0].getName().lastIndexOf('k') + 1));
                        ins[0].setFleet(Fleet.newHomogenousFleet(k, ins[0].getFleet().getVehicle()));
                    } else if (INSTANCE_READER instanceof NovoaPersistenceHelper) {
                        int s = Integer.valueOf(f.getName().substring(f.getName().indexOf('_') + 1,
                                f.getName().indexOf('r')));
                        if (s <= 60) {
                            ins = new IVRPInstance[] {
                                    INSTANCE_READER.readInstance(f, Double
                                            .valueOf(NovoaPersistenceHelper.getCapacity(s, 1, 0))),
                                    INSTANCE_READER.readInstance(f, Double
                                            .valueOf(NovoaPersistenceHelper.getCapacity(s, 1, 1))) };
                        } else {
                            ins = new IVRPInstance[0];
                        }
                    } else {
                        ins = new IVRPInstance[] { INSTANCE_READER.readInstance(f) };
                        if (ins[0].getDepot(0).getTimeWindow() != null) {
                            // Ignore distance constrained instances
                            ins = new IVRPInstance[0];
                        }
                    }

                    for (IVRPInstance i : ins) {
                        System.out.printf(" %s (%s clients) Fleet:%s\n", i.getName(),
                                i.getRequestCount(), i.getFleet());
                        instances.add(i);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return instances;
    }
}
