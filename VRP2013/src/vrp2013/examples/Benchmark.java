package vrp2013.examples;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.io.VRPRepPersistenceHelper;
import vroom.common.modeling.util.IRoutePool;
import vroom.common.modeling.util.ISolutionFactory;
import vroom.common.utilities.BestKnownSolutions;
import vroom.common.utilities.ProgressMonitor;
import vroom.common.utilities.StatCollector;
import vroom.common.utilities.StatCollector.Label;
import vroom.common.utilities.Stopwatch;
import vroom.common.utilities.Utilities;
import vroom.common.utilities.logging.LoggerHelper;
import vrp2013.algorithms.GRASP;
import vrp2013.algorithms.HeuristicConcentration;
import vrp2013.algorithms.ParallelGRASP;
import vrp2013.algorithms.IVRPOptimizationAlgorithm;
import vrp2013.util.SolutionFactories;
import vrp2013.util.VRPLogging;
import vrp2013.util.VRPSolution;

/**
 * The class <code>Benchmark</code> contains a main method to execute an algorithm over a set of instances and report
 * results
 * <p>
 * Creation date: 06/05/2013 - 12:46:23 PM
 * 
 * @author Victor Pillac, <a href="http://www.nicta.com.au">National ICT Australia</a>, <a
 *         href="http://www.victorpillac.com">www.victorpillac.com</a>
 * @version 1.0
 */
public class Benchmark {

    public static final VRPRepPersistenceHelper PERSISTENCE_HELPER = new VRPRepPersistenceHelper();

    public static void main(String[] args) {
        LoggerHelper.setupRootLogger(LoggerHelper.LEVEL_WARN, LoggerHelper.LEVEL_DEBUG, true);
        VRPLogging.getOptLogger().setLevel(LoggerHelper.LEVEL_WARN);
        VRPLogging.getBenchLogger().setLevel(LoggerHelper.LEVEL_INFO);

        String instanceDir = "./instances/cvrp/augerat-et-al";
        String bksFile = "./instances/cvrp/augerat.sol";

        BestKnownSolutions bks = new BestKnownSolutions(bksFile);

        List<File> instanceFiles = listInstanceFiles(instanceDir);
        List<BenchmarkRun> runs = new LinkedList<>();
        for (File f : instanceFiles) {
            runs.addAll(createRuns(f, bks));
        }

        executeRuns(runs);
    }

    /**
     * List the instance files contained in a directory
     * 
     * @param instanceDir
     * @return a list of the instance files contained in instanceDir
     */
    public static List<File> listInstanceFiles(String instanceDir) {
        try {
            return Utilities.listFiles(instanceDir, ".+xml.zip");
        } catch (FileNotFoundException e) {
            VRPLogging.getBenchLogger().exception("Benchmark.listInstanceFiles", e);
            return new LinkedList<>();
        }
    }

    /**
     * Create the require runs for a given instance file
     * 
     * @param instanceFile
     * @param bks
     * @return
     */
    public static List<BenchmarkRun> createRuns(File instanceFile, BestKnownSolutions bks) {
        LinkedList<BenchmarkRun> runs = new LinkedList<>();
        int initSeed = 0;
        int iterations = 50;

        IVRPInstance instance = null;
        try {
            instance = PERSISTENCE_HELPER.readInstance(instanceFile);
        } catch (Exception e) {
            VRPLogging.getBenchLogger().exception("Benchmark.createRuns", e);
            return new LinkedList<>();
        }

        runs.add(new BenchmarkRun(new GRASP(instance, SolutionFactories.ARRAY_LIST_SOL_FACTORY,
                initSeed, iterations, null), bks));
        runs.add(new BenchmarkRun(new GRASP(instance, SolutionFactories.LINKED_LIST_SOL_FACTORY,
                initSeed, iterations, null), bks));
        runs.add(new BenchmarkRun(new ParallelGRASP(instance,
                SolutionFactories.ARRAY_LIST_SOL_FACTORY, initSeed, iterations, null), bks));
        runs.add(new BenchmarkRun(new ParallelGRASP(instance,
                SolutionFactories.LINKED_LIST_SOL_FACTORY, initSeed, iterations, null), bks));

        return runs;
    }

    public static void executeRuns(List<BenchmarkRun> runs) {
        StatCollector collector = new StatCollector(new File(Utilities.getUnifiedOutputFilePath(
                "./", "vrp2013", "bench", "csv")), true, false, "VRP 2013 Benchmarks",
                BenchmarkRun.STAT_LABELS);
        ProgressMonitor progress = new ProgressMonitor(runs.size(), true);
        progress.start();
        for (BenchmarkRun run : runs) {
            try {
                VRPLogging.getBenchLogger().info("%s starting run %s (%s)", progress,
                        run.mOpt.getClass().getSimpleName(), run.getInstance().getName());
                progress.iterationFinished();
                run.call();
                run.collectStats(collector);
                run.dispose();
            } catch (Exception e) {
                VRPLogging.getBenchLogger().exception("Benchmark.executeRuns", e);
            }
        }

        VRPLogging.getBenchLogger().info("TERMINATED");
        collector.flush();
        collector.close();
    }

    /**
     * <code>BenchmarkRun</code>
     * <p>
     * Creation date: 06/05/2013 - 3:20:11 PM
     * 
     * @author Victor Pillac, <a href="http://www.nicta.com.au">National ICT Australia</a>, <a
     *         href="http://www.victorpillac.com">www.victorpillac.com</a>
     * @version 1.0
     */
    public static class BenchmarkRun implements Callable<VRPSolution>, IVRPOptimizationAlgorithm {
        public static final Label<?>[]          STAT_LABELS = new Label<?>[] {
                                                                    new Label<String>("Instance",
                                                                            String.class),
                                                                    new Label<String>("Opt_class",
                                                                            String.class),
                                                                    new Label<String>("Sol_class",
                                                                            String.class),
                                                                    new Label<Double>("BKS",
                                                                            Double.class),
                                                                    new Label<Integer>("Opt_IT",
                                                                            Integer.class),
                                                                    new Label<Double>("Opt_OBJ",
                                                                            Double.class),
                                                                    new Label<Double>("Opt_GAP",
                                                                            Double.class),
                                                                    new Label<Integer>("Opt_COLS",
                                                                            Integer.class),
                                                                    new Label<Double>(
                                                                            "PostOpt_OBJ",
                                                                            Double.class),
                                                                    new Label<Double>(
                                                                            "PostOpt_GAP",
                                                                            Double.class),
                                                                    new Label<Double>("Opt_Time_s",
                                                                            Double.class),
                                                                    new Label<Double>(
                                                                            "PostOpt_Time_s",
                                                                            Double.class), };

        private final IVRPOptimizationAlgorithm mOpt;
        private final HeuristicConcentration          mPostOpt;
        private final Stopwatch                 mOptSW      = new Stopwatch();
        private final Stopwatch                 mPostOptSW  = new Stopwatch();
        private final BestKnownSolutions        mBKS;

        public BenchmarkRun(IVRPOptimizationAlgorithm opt, BestKnownSolutions bks) {
            mOpt = opt;
            mBKS = bks;
            mPostOpt = new HeuristicConcentration(mOpt.getInstance(), mOpt.getSolutionFactory());
        }

        @Override
        public VRPSolution call() throws Exception {
            mOptSW.start();
            mOpt.call();
            mOptSW.stop();

            mPostOpt.initialize(mOpt.getRoutePool(), mOpt.getBestSolution());
            mPostOptSW.start();
            mPostOpt.call();
            mPostOptSW.stop();

            return getBestSolution();
        }

        @Override
        public IRoutePool<INodeVisit> getRoutePool() {
            return mOpt.getRoutePool();
        }

        @Override
        public VRPSolution getBestSolution() {
            return mPostOpt.getBestSolution();
        }

        @Override
        public IVRPInstance getInstance() {
            return mOpt.getInstance();
        }

        /**
         * Collect the statistics from this run
         * 
         * @param collector
         */
        public void collectStats(StatCollector collector) {
            Double bks = mBKS.getBKS(getInstance().getName());
            if (bks == null)
                bks = Double.NaN;
            collector.collect(getInstance().getName(),//
                    mOpt.getClass().getSimpleName(),//
                    mOpt.getBestSolution().getRoute(0).getClass().getSimpleName(),//
                    bks,//
                    mOpt.getIterations(),//
                    mOpt.getBestSolution().getCost(),//
                    (mOpt.getBestSolution().getCost() - bks) / bks,//
                    mPostOpt.getRoutePool().size(),//
                    mPostOpt.getBestSolution().getCost(),//
                    (mPostOpt.getBestSolution().getCost() - bks) / bks,//
                    mOptSW.readTimeS(),//
                    mPostOptSW.readTimeS()//
                    );
        }

        @Override
        public int getIterations() {
            return 1;
        }

        @Override
        public ISolutionFactory getSolutionFactory() {
            return mOpt.getSolutionFactory();
        }

        @Override
        public void dispose() {
            mOpt.dispose();
            mPostOpt.dispose();
        }
    }
}
