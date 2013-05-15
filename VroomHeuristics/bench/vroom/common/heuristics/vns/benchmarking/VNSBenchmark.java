/**
 * 
 */
package vroom.common.heuristics.vns.benchmarking;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import umontreal.iro.lecuyer.rng.MRG32k3a;
import vroom.common.heuristics.ConstraintHandler;
import vroom.common.heuristics.GenericNeighborhood;
import vroom.common.heuristics.GenericNeighborhoodHandler;
import vroom.common.heuristics.GenericNeighborhoodHandler.Strategy;
import vroom.common.heuristics.cw.CWParameters;
import vroom.common.heuristics.cw.algorithms.RandomizedSavingsHeuristic;
import vroom.common.heuristics.cw.kernel.ClarkeAndWrightHeuristic;
import vroom.common.heuristics.vns.VariableNeighborhoodSearch;
import vroom.common.heuristics.vns.VariableNeighborhoodSearch.VNSVariant;
import vroom.common.heuristics.vrp.OrOptNeighborhood;
import vroom.common.heuristics.vrp.StringExchangeNeighborhood;
import vroom.common.heuristics.vrp.SwapNeighborhood;
import vroom.common.heuristics.vrp.TwoOptNeighborhood;
import vroom.common.heuristics.vrp.VRPParameters;
import vroom.common.heuristics.vrp.constraints.CapacityConstraint;
import vroom.common.modeling.dataModel.ListRoute.ArrayListRoute;
import vroom.common.modeling.dataModel.Fleet;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.dataModel.Solution;
import vroom.common.modeling.io.ChristofidesPersistenceHelper;
import vroom.common.modeling.io.FlatFilePersistenceHelper;
import vroom.common.modeling.io.NovoaPersistenceHelper;
import vroom.common.modeling.io.TSPLibPersistenceHelper;
import vroom.common.utilities.BestKnownSolutions;
import vroom.common.modeling.util.DefaultSolutionFactory;
import vroom.common.modeling.util.SolutionChecker;
import vroom.common.utilities.StatCollector;
import vroom.common.utilities.StatCollector.Label;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.common.utilities.logging.Logging;
import vroom.common.utilities.optimization.INeighborhood;
import vroom.common.utilities.optimization.OptimizationSense;

/**
 * <code>VNSBenchmark</code> is a test class for the VLS implementation based on Solomon instances
 * <p>
 * Creation date: Jun 28, 2010 - 4:51:36 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class VNSBenchmark {

    public final static String                       SOLOMON_INSTANCES_PATH      = "../Instances/vrptw/solomon";

    public final static String                       AUGERAT_INSTANCES_PATH      = "../Instances/cvrp/augerat";
    public final static String                       AUGERAT_SOLUTION_FILE       = "../Instances/cvrp/augerat.sol";

    public final static String                       CHRISTOFIDES_INSTANCES_PATH = "../Instances/cvrp/christodifes-mingozzi-toth";
    public final static String                       CHRISTOFIDES_SOLUTION_FILE  = "../Instances/cvrp/christodifes-mingozzi-toth.sol";

    public final static String                       NOVOA_INSTANCES_PATH        = "../Instances/vrpsd/novoa";
    public final static int[]                        NOVOA_SIZES                 = new int[] { 5,
            8, 20, 40, 60                                                       };
    public final static int[]                        NOVOA_SETS                  = new int[] { 1 };
    public final static int[]                        NOVOA_CAPACITIES            = new int[] { 0, 1 };

    public final static String                       OUTPUT_FORMAT               = "./benchmarks/vns/%1$s-%2$ty%2$tm%2$td_%2$tk%2$tM.csv";

    // private final static String INSTANCE_PATH = NOVOA_INSTANCES_PATH;
    // private final static String BKS_FILE = "../Instances/vrpsd/novoa.sol";
    // private final static String BENCH_NAME = "novoa";
    // private final static String INSTANCE_FILTER = "i_";
    // private final static boolean RESET_ON_SIZE_CHANGE = true;
    // protected final static FlatFilePersistenceHelper INSTANCE_READER = new
    // NovoaPersistenceHelper();

    // private final static String INSTANCE_PATH = AUGERAT_INSTANCES_PATH;
    // private final static String BKS_FILE = AUGERAT_SOLUTION_FILE;
    // private final static String BENCH_NAME = "augerat";
    // private final static String INSTANCE_FILTER = "P-";
    // private final static boolean RESET_ON_SIZE_CHANGE = false;
    // protected final static FlatFilePersistenceHelper INSTANCE_READER = new
    // TSPLibPersistenceHelper();

    private final static String                      INSTANCE_PATH               = CHRISTOFIDES_INSTANCES_PATH;
    private final static String                      BKS_FILE                    = CHRISTOFIDES_SOLUTION_FILE;
    private final static String                      BENCH_NAME                  = "christofides";
    private final static String                      INSTANCE_FILTER             = "vrpnc";
    private final static boolean                     RESET_ON_SIZE_CHANGE        = false;
    protected final static FlatFilePersistenceHelper INSTANCE_READER             = new ChristofidesPersistenceHelper();

    private static final boolean                     UPDATE_BKS                  = false;

    public static final Label<?>[]                   STATS_LABELS                = new Label<?>[] {
            new Label<String>("Variant", String.class),
            new Label<String>("strategy", String.class),
            new Label<String>("instance", String.class), new Label<Integer>("run", Integer.class),
            new Label<Integer>("size", Integer.class), new Label<Double>("obj", Double.class),
            new Label<Double>("bks", Double.class), new Label<Double>("gap", Double.class),
            new Label<Long>("cpu_time", Long.class), new Label<Integer>("used_veh", Integer.class),
            new Label<Integer>("num_veh", Integer.class),
            new Label<Boolean>("too_many_veh", Boolean.class)                   };

    protected final static LoggerHelper              LOGGER                      = LoggerHelper
                                                                                         .getLogger(VNSBenchmark.class
                                                                                                 .getSimpleName());

    private final static File                        INSTANCES_DIRECTORY         = new File(
                                                                                         INSTANCE_PATH);

    protected final static List<File>                INSTANCES_FILES;

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

    @SuppressWarnings({ "unchecked", "unused", "rawtypes" })
    public static void main(String[] args) {
        Logging.setupRootLogger(LoggerHelper.LEVEL_WARN, LoggerHelper.LEVEL_WARN, false);
        GenericNeighborhood.setCheckSolutionAfterMove(false);

        BestKnownSolutions bestKnownSolutions = null;
        if (BKS_FILE != null) {
            bestKnownSolutions = new BestKnownSolutions(BKS_FILE);
        }

        File statFile = new File(String.format(OUTPUT_FORMAT, BENCH_NAME,
                new Date(System.currentTimeMillis())));

        StatCollector statCollector = new StatCollector(statFile, true, false, INSTANCE_PATH,
                STATS_LABELS);

        // # runs per instance
        int runsPerInstance = 50;

        // Constraint Handler
        ConstraintHandler<Solution<ArrayListRoute>> ctrHandler = new ConstraintHandler<Solution<ArrayListRoute>>();
        ctrHandler.addConstraint(new CapacityConstraint<Solution<ArrayListRoute>>());

        // Neighborhoods
        // swap
        INeighborhood<Solution<ArrayListRoute>, ?> swap = new SwapNeighborhood<Solution<ArrayListRoute>>(
                ctrHandler);
        // 2-opt
        TwoOptNeighborhood<Solution<ArrayListRoute>> twoOpt = new TwoOptNeighborhood<Solution<ArrayListRoute>>(
                ctrHandler);
        // Or-opt
        OrOptNeighborhood<Solution<ArrayListRoute>> orOpt = new OrOptNeighborhood<Solution<ArrayListRoute>>(
                ctrHandler);
        // string-exchange
        StringExchangeNeighborhood<Solution<ArrayListRoute>> strEx = new StringExchangeNeighborhood<Solution<ArrayListRoute>>(
                ctrHandler);

        List<INeighborhood<Solution<ArrayListRoute>, ?>> gvnsNeigh = new LinkedList<INeighborhood<Solution<ArrayListRoute>, ?>>();
        List<INeighborhood<Solution<ArrayListRoute>, ?>> gvnsLSNeigh = new LinkedList<INeighborhood<Solution<ArrayListRoute>, ?>>();
        List<INeighborhood<Solution<ArrayListRoute>, ?>> vndNeigh = new LinkedList<INeighborhood<Solution<ArrayListRoute>, ?>>();

        gvnsLSNeigh.add(swap);
        gvnsLSNeigh.add(twoOpt);
        gvnsLSNeigh.add(orOpt);
        gvnsNeigh.add(strEx);
        vndNeigh.add(swap);
        vndNeigh.add(twoOpt);
        vndNeigh.add(orOpt);
        vndNeigh.add(strEx);

        // Collections.reverse(neighborhoods);

        // GVNS
        // VariableNeighborhoodSearch<Solution<ArrayListRoute>> gvns = new
        // VariableNeighborhoodSearch<Solution<ArrayListRoute>>(
        // OptimizationSense.MINIMIZATION, null, gvnsNeigh);
        VariableNeighborhoodSearch<Solution<ArrayListRoute>> gvnsLS = VariableNeighborhoodSearch
                .newVNS(VNSVariant.VND, OptimizationSense.MINIMIZATION, null, new MRG32k3a(),
                        gvnsLSNeigh);
        VariableNeighborhoodSearch<Solution<ArrayListRoute>> gvns = VariableNeighborhoodSearch
                .newVNS(VNSVariant.GVNS, OptimizationSense.MINIMIZATION, gvnsLS, new MRG32k3a(),
                        gvnsNeigh);
        VariableNeighborhoodSearch<Solution<ArrayListRoute>> vnd = VariableNeighborhoodSearch
                .newVNS(VNSVariant.VND, OptimizationSense.MINIMIZATION, null, new MRG32k3a(),
                        vndNeigh);

        VariableNeighborhoodSearch[] vnsVariants = new VariableNeighborhoodSearch[] { gvns };// ,
                                                                                             // vnd
                                                                                             // };

        Map<VariableNeighborhoodSearch<Solution<ArrayListRoute>>, String> vnsDescriptions = new HashMap<VariableNeighborhoodSearch<Solution<ArrayListRoute>>, String>();
        vnsDescriptions.put(gvns, "GVNS");
        vnsDescriptions.put(vnd, "VND");

        // VNS Params
        VRPParameters params = new VRPParameters(Long.MAX_VALUE, Integer.MAX_VALUE, true, true,
                null);

        System.out.println("Loading instances from " + INSTANCE_PATH);
        List<IVRPInstance> instances = readInstances();
        System.out.println("===========================================================");
        System.out.println(" Best known solutions:");
        for (IVRPInstance i : instances) {
            System.out.printf("%s : %s %s\n", i.getName(), bestKnownSolutions.getBKS(i.getName()),
                    bestKnownSolutions.isOptimal(i.getName()) ? "(opt)" : "");
        }
        System.out.println("===========================================================");

        System.out.println();

        // Initial solutions
        System.out.println("Generating initial solutions");
        CWParameters cwParams = new CWParameters();
        cwParams.set(CWParameters.SOLUTION_FACTORY_CLASS, DefaultSolutionFactory.class);
        cwParams.set(CWParameters.RANDOM_SEED, 0l);
        int initSol = 0;
        Solution<ArrayListRoute>[] initialSolutions = (Solution<ArrayListRoute>[]) new Solution<?>[instances
                .size() * runsPerInstance];
        for (IVRPInstance instance : instances) {
            ClarkeAndWrightHeuristic<Solution<ArrayListRoute>> cw = new ClarkeAndWrightHeuristic<Solution<ArrayListRoute>>(
                    cwParams, RandomizedSavingsHeuristic.class, ctrHandler);
            for (int run = 0; run < runsPerInstance; run++) {
                cw.initialize(instance);
                cw.run();
                initialSolutions[initSol++] = cw.getSolution();
            }
        }
        System.out.println("Done");
        System.out.println();

        // Benchmark runs
        System.out.printf("Running benchmarks with %s run per instance\n", runsPerInstance);
        System.out.println("Stats printed in file " + statFile.toString());
        System.out.println();

        Map<VariableNeighborhoodSearch<Solution<ArrayListRoute>>, List<RunParams>> runParams = new HashMap<VariableNeighborhoodSearch<Solution<ArrayListRoute>>, List<RunParams>>();

        // Setup the runs
        // VNS
        List<RunParams> tmpParams = new LinkedList<VNSBenchmark.RunParams>();
        // for (Strategy strat : Strategy.values()) {
        // for (int i = 0; i < 2; i++) {
        // if (strat != Strategy.RANDOM || i == 0) {
        // tmpParams.add(new RunParams(strat, strat == Strategy.SEQUENTIAL && i
        // == 1,
        // i == 0));
        // }
        // }
        // }
        // runParams.put(vnd, tmpParams);
        // GVNS
        tmpParams = new LinkedList<VNSBenchmark.RunParams>();
        tmpParams.add(new RunParams(Strategy.EFFICIENCY_BASED, false, false));
        runParams.put(gvns, tmpParams);
        // // VND
        // ((GenericNeighborhoodHandler<?>)
        // gvnsLS.getNeighHandler()).setStrategy(Strategy.SEQUENTIAL);

        List<RunParams> selectedVNSRunParams = null;
        for (VariableNeighborhoodSearch<Solution<ArrayListRoute>> selectedVNS : vnsVariants) {
            selectedVNSRunParams = runParams.get(selectedVNS);

            String selectedVNSDesc = vnsDescriptions.get(selectedVNS);

            System.out.println("######################################################");
            System.out.println("VNS: " + selectedVNSDesc);

            for (RunParams p : selectedVNSRunParams) {
                ((GenericNeighborhoodHandler) selectedVNS.getNeighHandler()).setStrategy(p.strat);

                System.out.println("-------------------------------------------------");
                System.out.println("Running benchmarks for strategy " + p.strat);
                selectedVNS.getNeighHandler().reset();

                if (p.strat != Strategy.SEQUENTIAL) {
                    if (p.reset) {
                        System.out.println(" with neighborhood handler reset");
                    } else {
                        System.out.println(" without neighborhood handler reset");
                    }

                } else {
                    if (p.reverse) {
                        System.out.println(" reversed order");
                        Collections.reverse(selectedVNS.getNeighHandler().getComponents());
                    } else {
                        System.out.println(" normal order");
                    }
                }

                int numRuns = 0;

                int rejectedCount = 0;

                double costAverage = 0;
                double timeAverage = 0;
                double vnsTimeAverage = 0;
                double gapAverage = 0;
                double gapMax = 0;
                double gapMin = Double.MAX_VALUE;

                initSol = 0;
                int prevSize = 0;
                for (IVRPInstance instance : instances) {
                    if (RESET_ON_SIZE_CHANGE && prevSize != instance.getRequestCount()) {
                        // Reset if the instance size has changed
                        selectedVNS.getNeighHandler().reset();
                    }
                    prevSize = instance.getRequestCount();

                    for (int run = 0; run < runsPerInstance; run++) {
                        if (p.reset) {
                            selectedVNS.getNeighHandler().reset();
                        }

                        VNSRun bench = new VNSRun(instance, selectedVNS, ctrHandler, params);

                        bench.setSolution(initialSolutions[initSol++].clone());

                        // Coherence check
                        if (bench.getSolution().getParentInstance() != instance) {
                            throw new IllegalStateException("Inconsistency in initial solutions");
                        }

                        bench.run();

                        Solution<ArrayListRoute> sol = bench.getSolution();

                        LinkedList<ArrayListRoute> emptyRoutes = new LinkedList<ArrayListRoute>();
                        for (ArrayListRoute r : sol) {
                            if (r.length() == 2) {
                                emptyRoutes.add(r);
                            }
                        }
                        for (ArrayListRoute r : emptyRoutes) {
                            sol.removeRoute(r);
                        }

                        String check = SolutionChecker.checkSolution(sol, false, true, true);
                        boolean acceptable = INSTANCE_READER instanceof NovoaPersistenceHelper
                                || sol.getRouteCount() <= instance.getFleet().size()
                                && check == null;

                        if (check != null) {
                            System.out.println(check);
                        }

                        double time = bench.getVNSTimer().readTimeMS();
                        double cost = SolutionChecker.calculateCost(sol);
                        if (acceptable) {
                            costAverage += cost;
                            timeAverage += bench.getGlobalTimer().readTimeMS();
                            vnsTimeAverage += time;
                        } else {
                            rejectedCount++;
                        }
                        double bks = -1;
                        double gap = -1;
                        if (acceptable && bestKnownSolutions != null) {
                            if (bestKnownSolutions.getBKS(instance.getName()) != null) {
                                bks = bestKnownSolutions.getBKS(instance.getName());
                                gap = (cost - bks) / bks;

                                if (gap < -1e-5) {
                                    if (bestKnownSolutions.isOptimal(instance.getName())) {
                                        System.err
                                                .printf("Found a solution better than optimal: %s - %s (bks:%s gap:%s) %s\n",
                                                        instance.getName(), cost, bks, gap, sol);
                                        System.exit(1);
                                    }

                                    System.out
                                            .printf("New best solution found: %s - %s (bks:%s gap:%s) %s\n",
                                                    instance.getName(), cost, bks, gap, sol);
                                    bestKnownSolutions.setBestKnownSolution(instance.getName(),
                                            cost, false, null, null, null);
                                }

                                gapAverage += gap;
                                gapMax = gap > gapMax ? gap : gapMax;
                                gapMin = gap < gapMin ? gap : gapMin;
                            } else {
                                System.out.printf(
                                        "New best solution found: %s - %s (no previous)\n",
                                        instance.getName(), cost);
                                bestKnownSolutions.setBestKnownSolution(instance.getName(), cost,
                                        false, null, null, null);
                            }
                        }

                        statCollector.collect(
                                selectedVNSDesc,
                                p.toString(),
                                String.format("%s-%s", instance.getName(), instance.getFleet()
                                        .getVehicle().getCapacity()), run,
                                instance.getRequestCount(), cost, bks, gap, time,
                                sol.getRouteCount(), instance.getFleet().size(), !acceptable);

                        numRuns++;

                    }

                    // Save the BKS
                    if (UPDATE_BKS && bestKnownSolutions != null) {
                        try {
                            bestKnownSolutions.save("Solutions found sith VNS");
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }

                costAverage /= numRuns - rejectedCount;
                timeAverage /= numRuns - rejectedCount;
                vnsTimeAverage /= numRuns - rejectedCount;
                gapAverage /= numRuns - rejectedCount;

                // Let the log messages be printed
                Object o = new Object();
                synchronized (o) {
                    try {
                        o.wait(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                System.out.println(" --------");
                System.out.printf("Total runs        : %s\n", numRuns);
                System.out.printf("Rejected solutions: %s\n", rejectedCount);
                System.out.printf("Average cost      : %.1f\n", costAverage);
                System.out.printf("Average gap       : %.1f%s\n", gapAverage * 100, "%");
                System.out.printf("Min gap           : %.1f%s\n", gapMin * 100, "%");
                System.out.printf("Max gap           : %.1f%s\n", gapMax * 100, "%");
                System.out.printf("Average vns time  : %.1f ms\n", vnsTimeAverage);
                System.out.printf("Neighborhoods     : %s\n", selectedVNS.getNeighHandler());

                System.out.println(" --------");
                System.out.println();

                if (p.strat == Strategy.SEQUENTIAL && p.reverse) {
                    Collections.reverse(selectedVNS.getNeighHandler().getComponents());
                }
            }
        }
        if (UPDATE_BKS && bestKnownSolutions != null) {
            try {
                bestKnownSolutions.save("Solutions found sith VNS");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("FINISHED");

    }

    /**
     * Read the instances
     * 
     * @return a list of instances
     */
    private static List<IVRPInstance> readInstances() {
        List<IVRPInstance> instances = new LinkedList<IVRPInstance>();
        if (INSTANCE_READER instanceof NovoaPersistenceHelper) {
            try {
                instances = new LinkedList<IVRPInstance>(
                        ((NovoaPersistenceHelper) INSTANCE_READER).readInstances(NOVOA_SIZES,
                                NOVOA_CAPACITIES, NOVOA_SETS, null));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            for (File f : INSTANCES_FILES) {
                if (!f.getName().equals("1_info.txt") && !f.getName().equals(".svn")) {
                    try {
                        IVRPInstance[] ins;
                        if (INSTANCE_READER instanceof TSPLibPersistenceHelper) {
                            ins = new IVRPInstance[] { INSTANCE_READER.readInstance(f, 100) };
                            int k = Integer.valueOf(ins[0].getName().substring(
                                    ins[0].getName().lastIndexOf('k') + 1));
                            ins[0].setFleet(Fleet.newHomogenousFleet(k, ins[0].getFleet()
                                    .getVehicle()));
                        } else if (INSTANCE_READER instanceof NovoaPersistenceHelper) {
                            int s = Integer.valueOf(f.getName().substring(
                                    f.getName().indexOf('_') + 1, f.getName().indexOf('r')));
                            if (s <= 60) {
                                ins = new IVRPInstance[] {
                                        INSTANCE_READER.readInstance(f, Double
                                                .valueOf(NovoaPersistenceHelper
                                                        .getCapacity(s, 1, 0))),
                                        INSTANCE_READER.readInstance(f, Double
                                                .valueOf(NovoaPersistenceHelper
                                                        .getCapacity(s, 1, 1))) };
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
        }
        return instances;
    }

    private static class RunParams {
        private final Strategy strat;
        private final boolean  reverse;
        private final boolean  reset;

        /**
         * Creates a new <code>runParams</code>
         * 
         * @param strat
         * @param reverse
         * @param reset
         */
        public RunParams(Strategy strat, boolean reverse, boolean reset) {
            this.strat = strat;
            this.reverse = reverse;
            this.reset = reset;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return String.format("%s-%s", strat.toString().substring(0, 3), reverse ? "Rev"
                    : reset ? "R" : "NR");
        }
    }
}
