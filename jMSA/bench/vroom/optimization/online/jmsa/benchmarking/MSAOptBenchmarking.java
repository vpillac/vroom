package vroom.optimization.online.jmsa.benchmarking;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import vroom.common.heuristics.ConstraintHandler;
import vroom.common.heuristics.cw.CWParameters;
import vroom.common.heuristics.cw.kernel.ClarkeAndWrightHeuristic;
import vroom.common.heuristics.vrp.TwoOptNeighborhood;
import vroom.common.heuristics.vrp.constraints.CapacityConstraint;
import vroom.common.heuristics.vrp.constraints.FixedNodesConstraint;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.dataModel.IVRPRequest;
import vroom.common.modeling.dataModel.IVRPSolution;
import vroom.common.modeling.dataModel.NodeVisit;
import vroom.common.modeling.io.NovoaPersistenceHelper.DemandDistribution;
import vroom.common.modeling.util.SolutionChecker;
import vroom.common.modeling.visualization.VRPVisualizationUtilities;
import vroom.common.utilities.StatCollector;
import vroom.common.utilities.StatCollector.Label;
import vroom.common.utilities.Stopwatch;
import vroom.common.utilities.Utilities;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.common.utilities.optimization.IParameters.LSStrategy;
import vroom.common.utilities.optimization.SimpleParameters;
import vroom.optimization.online.jmsa.MSABase;
import vroom.optimization.online.jmsa.MSAGlobalParameters;
import vroom.optimization.online.jmsa.MSASequential;
import vroom.optimization.online.jmsa.components.ScenarioOptimizerParam;
import vroom.optimization.online.jmsa.vrp.MSAVRPInstance;
import vroom.optimization.online.jmsa.vrp.VRPActualRequest;
import vroom.optimization.online.jmsa.vrp.VRPSampledRequest;
import vroom.optimization.online.jmsa.vrp.VRPScenario;
import vroom.optimization.online.jmsa.vrp.optimization.MSACWSavingsHeuristic;

/**
 * <code>MSAOptBenchmarking</code>
 * <p>
 * Creation date: Sep 21, 2010 - 2:39:09 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class MSAOptBenchmarking {

    public final static LoggerHelper LOGGER = LoggerHelper.getLogger("MSAOptBenchmarking");

    public static final Label<?>[]   LABELS = new Label<?>[] { new Label<Integer>("run_id", Integer.class),
            new Label<String>("instance_name", String.class), new Label<Integer>("size", Integer.class),
            new Label<Integer>("vehicle_capacity", Integer.class), new Label<Long>("time_init", Long.class),
            new Label<Long>("time_opt", Long.class), new Label<Long>("time_cw2Opt", Long.class),
            new Label<Double>("optimal", Double.class), new Label<String>("seeds", String.class),
            new Label<Double>("gap_init", Double.class), new Label<Double>("gap_opt", Double.class),
            new Label<Double>("gap_cw2Opt", Double.class) };

    public static void main(String[] args) {
        NovoaRun.setupLoggers(LoggerHelper.LEVEL_WARN, LoggerHelper.LEVEL_WARN, false, true, false);
        LOGGER.setLevel(LoggerHelper.LEVEL_INFO);

        int numRuns = 100;
        int[] sets = { 1 };
        int[] sizes = { 30 };
        String fileComment = "na";

        if (args.length == 4) {
            sizes = vroom.common.utilities.Utilities.toIntArray(args[0]);
            sets = vroom.common.utilities.Utilities.toIntArray(args[1]);
            numRuns = Integer.parseInt(args[2]);
            fileComment = args[3];
        } else {
            System.err.println("Wrong number of arguments, usage: main [sizes] [sets] nruns comment");
            System.err.println("Using default values");
        }

        MSAGlobalParameters params = NovoaBenchmarking.getDefaultParameters();

        @SuppressWarnings({ "unchecked", "rawtypes" })
        MSABase msa = new MSASequential(null, params);

        String comment = String.format("Date: %s\nMSA:\n%s\n\nNum runs: %s", new Date(System.currentTimeMillis()),
                msa.getComponentsDescription(), numRuns);

        String file = String.format("results/optbench_%s.csv", fileComment);
        StatCollector collector = new StatCollector(new File(file), true, false, comment, LABELS);

        PerfectInformationSolver solver = new PerfectInformationSolver();

        CWParameters cwParams = new CWParameters();
        cwParams.set(CWParameters.RANDOM_SEED, params.get(MSAGlobalParameters.RANDOM_SEED));
        ClarkeAndWrightHeuristic<IVRPSolution<?>> cw = new ClarkeAndWrightHeuristic<IVRPSolution<?>>(cwParams,
                MSACWSavingsHeuristic.class, getConstraintHandler());

        LOGGER.info("=============================================================");
        LOGGER.info(" Starting benchmarks for instances %s in sets %s with %s runs", Arrays.toString(sizes),
                Arrays.toString(sets), numRuns);
        LOGGER.info(" Stats saved in file %s", file);
        LOGGER.info("=============================================================");

        Stopwatch mainTimer = new Stopwatch();
        mainTimer.start();
        double progress = 0;
        double perc = 100d / (sets.length * sizes.length * 2 * 5 * numRuns);
        for (int set : sets) {
            for (int size : sizes) {
                for (int cap = 0; cap < 2; cap++) {
                    for (int num = 1; num <= 5; num++) {
                        for (int run = 0; run < numRuns; run++) {
                            progress += perc;

                            long etc = (long) (mainTimer.readTimeMS() / progress * 100);

                            long[] seeds = NovoaBenchmarking.getSeeds(run, size, num, cap, set);
                            NovoaRun novoa = null;
                            try {
                                novoa = new NovoaRun(set, size, num, cap, run, params, DemandDistribution.UNIFORM);
                            } catch (IOException e) {
                                LOGGER.exception("MSAOptBenchmarking.main", e);
                                break;
                            }

                            MSAVRPInstance instance = new MSAVRPInstance(novoa.getSimulationInstance(), params);
                            LOGGER.info(" %.1f - instance: %s run: %s etc: %s", progress, instance.getName(), run,
                                    Utilities.Time.millisecondsToString(etc, 3, false, false));

                            Stopwatch timer = new Stopwatch();
                            double initTime;
                            double optTime, cw2OptTime;
                            double initObj, optObj, cw2OptObj;

                            List<VRPActualRequest> act = new LinkedList<VRPActualRequest>();
                            for (IVRPRequest r : novoa.getSimulationInstance().getRequests()) {
                                VRPActualRequest req = new VRPActualRequest(NodeVisit.createNodeVisits(r)[0]);
                                act.add(req);
                            }

                            List<VRPSampledRequest> samp = new LinkedList<VRPSampledRequest>();

                            VRPScenario solution = new VRPScenario(instance, act, samp);

                            timer.start();
                            novoa.getMsa()
                                    .getComponentManager()
                                    .getScenarioOptimizer()
                                    .initialize(solution,
                                            new ScenarioOptimizerParam(Integer.MAX_VALUE, Integer.MAX_VALUE, false));
                            timer.stop();
                            SolutionChecker.checkSolution(solution, true, true, true);
                            initObj = solution.getCost();
                            initTime = timer.readTimeMS();

                            timer.restart();
                            novoa.getMsa()
                                    .getComponentManager()
                                    .getScenarioOptimizer()
                                    .optimize(solution,
                                            new ScenarioOptimizerParam(Integer.MAX_VALUE, Integer.MAX_VALUE, false));
                            timer.stop();
                            SolutionChecker.checkSolution(solution, true, true, true);
                            optObj = solution.getCost();
                            optTime = timer.readTimeMS();

                            timer.restart();
                            cw2OptObj = cw2Opt(instance, cw);
                            timer.stop();
                            cw2OptTime = timer.readTimeMS();

                            double optimal = solver.solvePerfectInformation(run, size, num, cap, set,
                                    Integer.MAX_VALUE, false, true, DemandDistribution.UNIFORM);

                            collector.collect(run, instance.getName(), size, (int) novoa.getInstance().getFleet()
                                    .getVehicle(0).getCapacity(), initTime, optTime, cw2OptTime, optimal,
                                    Arrays.toString(seeds), (initObj - optimal) / optimal,
                                    (optObj - optimal) / optimal, (cw2OptObj - optimal) / optimal);

                            novoa = null;
                        }
                        Runtime.getRuntime().gc();
                    }
                }
            }
        }
        mainTimer.stop();
        LOGGER.info("=============================================================");
        LOGGER.info("                          FINISHED");
        LOGGER.info(" Total time:%s", mainTimer.readTimeString());
        LOGGER.info("=============================================================");

        System.exit(0);
    }

    public static double cw2Opt(IVRPInstance instance, ClarkeAndWrightHeuristic<IVRPSolution<?>> cw) {
        cw.initialize(instance);
        cw.run();

        TwoOptNeighborhood<IVRPSolution<?>> twoOpt = new TwoOptNeighborhood<IVRPSolution<?>>(getConstraintHandler());

        IVRPSolution<?> cwSol = cw.getSolution();

        IVRPSolution<?> twoOptSol = twoOpt.localSearch(instance, cwSol, new SimpleParameters(
                LSStrategy.DET_BEST_IMPROVEMENT, Long.MAX_VALUE, Integer.MAX_VALUE, cw.getRandomStream()));

        String err = SolutionChecker.checkSolution(twoOptSol, false, true, true);
        if (err != null) {
            VRPVisualizationUtilities.showVisualizationFrame(twoOptSol);
            throw new IllegalStateException("CW+2Opt solution is not feasible: " + err);
        }

        return twoOptSol.getCost();

    }

    public static ConstraintHandler<IVRPSolution<?>> getConstraintHandler() {
        ConstraintHandler<IVRPSolution<?>> constraintHandler = new ConstraintHandler<IVRPSolution<?>>();
        constraintHandler.addConstraint(new FixedNodesConstraint<IVRPSolution<?>>());
        constraintHandler.addConstraint(new CapacityConstraint<IVRPSolution<?>>());
        return constraintHandler;
    }
}
