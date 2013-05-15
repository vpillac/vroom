/**
 * 
 */
package vroom.optimization.online.jmsa.benchmarking;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.io.NovoaPersistenceHelper.DemandDistribution;
import vroom.common.utilities.BestKnownSolutions;
import vroom.common.utilities.StatCollector;
import vroom.common.utilities.StatCollector.Label;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.common.utilities.lp.SolverStatus;
import vroom.optimization.online.jmsa.MSAGlobalParameters;
import vroom.optimization.pl.symphony.vrp.CVRPSymphonySolver;

/**
 * <code>PerfectInformationSolver</code>
 * <p>
 * Creation date: Sep 17, 2010 - 11:25:28 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class PerfectInformationSolver {

    public final static LoggerHelper        LOGGER      = LoggerHelper
                                                                .getLogger(PerfectInformationSolver.class
                                                                        .getSimpleName());

    public static final String              SOL_FILE    = "data/perfinf/perf_inf.sol";
    public static final String              CSV_FILE    = "data/perfinf/perf_inf.csv";

    public static String                    TEMP_FOLDER = "tmp";

    public static final Label<?>[]          LABELS      = new Label<?>[] {
            new Label<Integer>("run_id", Integer.class),
            new Label<String>("instance_name", String.class),
            new Label<Integer>("size", Integer.class),
            new Label<Integer>("vehicle_capacity", Integer.class),
            new Label<Double>("running_time", Double.class),
            new Label<Double>("perf_inf", Double.class),
            new Label<Integer>("num_trucks", Integer.class),
            new Label<String>("seeds", String.class)   };

    private final static BestKnownSolutions mBKS;

    static {
        mBKS = new BestKnownSolutions(SOL_FILE);
        mBKS.setAllOptimal(true); // save extra verbose in file
    }

    private final MSAGlobalParameters       mParams;

    private final StatCollector             mCollector;

    private CVRPSymphonySolver              mSolver;

    private static final Lock               sIOLock     = new ReentrantLock();

    /**
     * Creates a new <code>PerfectInformationSolver</code>
     */
    public PerfectInformationSolver() {
        mParams = new MSAGlobalParameters();
        NovoaRun.loadDefaultParameters(mParams);
        mCollector = new StatCollector(new File(CSV_FILE), true, true,
                "Perfect Information Solutions", LABELS);

    }

    /**
     * Solve all the instances described by the arguments.
     * 
     * @param sizes
     *            instance sizes
     * @param caps
     *            instance capacities
     * @param sets
     *            instance sets
     * @param firstRun
     * @param lastRun
     * @param numThreads
     * @param timeout
     *            the maximum time per instance in seconds
     */
    public static void solvePerferctInformation(int[] sizes, int[] caps, int[] sets, int firstRun,
            int lastRun, int numThreads, final int timeout) {
        LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(numThreads, numThreads, 1,
                TimeUnit.MINUTES, workQueue);

        final LinkedBlockingQueue<PerfectInformationSolver> solvers = new LinkedBlockingQueue<PerfectInformationSolver>(
                numThreads);
        for (int s = 0; s < numThreads; s++) {
            solvers.add(new PerfectInformationSolver());
        }

        for (int set : sets) {
            for (int size : sizes) {
                for (int cap : caps) {
                    for (int num = 1; num <= 5; num++) {
                        for (int run = firstRun; run < lastRun; run++) {
                            final int setF = set, sizeF = size, capF = cap, numF = num, runF = run;
                            executor.execute(new Runnable() {
                                @Override
                                public void run() {
                                    PerfectInformationSolver s = solvers.poll();
                                    s.solvePerfectInformation(runF, sizeF, numF, capF, setF,
                                            timeout, false, true, DemandDistribution.UNIFORM);
                                    solvers.add(s);
                                }
                            });
                        }
                    }
                }
            }
        }
        executor.shutdown();
        try {
            executor.awaitTermination(7, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            LOGGER.exception("PerfectInformationSolver.solvePerferctInformation", e);
        }
    }

    /**
     * Solve a single instance and save the perfect information value in the {@link #SOL_FILE} file.
     * 
     * @param run
     *            run id
     * @param size
     *            instance size ( 5, 8, 20, 30, 40, 60, 100)
     * @param rep
     *            instance replica (1 - 5)
     * @param cap
     *            vehicle capacity (0, 1)
     * @param set
     *            instance set (0,1)
     * @param timeout
     *            the maximum time in seconds
     * @param force
     *            <code>true</code> if the optimization should be performed even if a solution already exists
     * @param write
     *            <code>true</code> if new solutions should be automatically writen to the solution file
     * @return the perfect information value for the specified instance
     */
    public synchronized double solvePerfectInformation(final int run, final int size,
            final int rep, final int cap, final int set, final int timeout, boolean force,
            boolean write) {
        return solvePerfectInformation(run, size, rep, cap, set, timeout, force, write,
                DemandDistribution.UNIFORM);
    }

    /**
     * Solve a single instance and save the perfect information value in the {@link #SOL_FILE} file.
     * 
     * @param run
     *            run id
     * @param size
     *            instance size ( 5, 8, 20, 30, 40, 60, 100)
     * @param rep
     *            instance replica (1 - 5)
     * @param cap
     *            vehicle capacity (0, 1)
     * @param set
     *            instance set (0,1)
     * @param timeout
     *            the maximum time in seconds
     * @param force
     *            <code>true</code> if the optimization should be performed even if a solution already exists
     * @param write
     *            <code>true</code> if new solutions should be automatically written to the solution file
     * @param distribution
     *            the demand distribution to use
     * @return the perfect information value for the specified instance
     */
    public synchronized double solvePerfectInformation(final int run, final int size,
            final int rep, final int cap, final int set, final int timeout, boolean force,
            boolean write, DemandDistribution distribution) {
        mSolver = new CVRPSymphonySolver();
        mSolver.setTempFolder(TEMP_FOLDER);
        mSolver.setTimeLimit(timeout * 1000);
        String code = NovoaBenchmarking.getInstanceCode(run, size, rep, cap, set, distribution);

        LOGGER.info("Solving instance : %s", code);

        sIOLock.lock();
        if (!force && mBKS.isOptimal(code)) {
            LOGGER.info(" -Perfect information already known: %s", mBKS.getBKS(code));
            sIOLock.unlock();
            return mBKS.getBKS(code);
        }

        // Look for an upper bound in the first 100 runs of the same instance
        double ub = -1;
        for (int r = 0; r < 100; r++) {
            Double bks = mBKS.getBKS(NovoaBenchmarking.getInstanceCode(r, size, rep, cap, set,
                    distribution));
            if (bks != null && bks > ub) {
                ub = bks;
            }
        }
        if (ub < 0) {
            ub = Double.POSITIVE_INFINITY;
        }
        sIOLock.unlock();

        long[] seeds = NovoaBenchmarking.getSeeds(run, size, rep, cap, set);
        NovoaRun novoa = null;
        try {
            novoa = new NovoaRun(set, size, rep, cap, run, mParams, distribution);
        } catch (IOException e1) {
            LOGGER.exception("PerfectInformationSolver.solvePerfectInformation", e1);
            return -1;
        }
        IVRPInstance instance = novoa.getSimulationInstance();
        SolverStatus status = SolverStatus.UNKNOWN_STATUS;
        try {
            LOGGER.debug("Setting the upper bound to %s", ub);
            mSolver.readInstance(instance);
            mSolver.setUpperBound(ub);
            status = mSolver.solve();
            // if (status == SolverStatus.INFEASIBLE) {
            // LOGGER.info("Problem is infeasible, relaxing the upper bound");
            // mSolver.setUpperBound(Double.POSITIVE_INFINITY);
            // status = mSolver.solve();
            // }
        } catch (Exception e) {
            LOGGER.exception("PerfectInformationSolver.solvePerfectInformation (%s)", e,
                    instance.getName());
        }
        if (status == SolverStatus.OPTIMAL) {
            LOGGER.info(" -Perfect information value: %s (%sms)", mSolver.getObjectiveValue(),
                    mSolver.getSolveTime());

            if (write) {
                sIOLock.lock();
                mBKS.setBestKnownSolution(code, mSolver.getObjectiveValue(), true, null, null, null);
                try {
                    mBKS.save("Perfect Information, solver: " + mSolver.getClass().getSimpleName());
                } catch (FileNotFoundException e) {
                    LOGGER.exception(
                            "PerfectInformationSolver.solvePerfectInformation-saveBKS(%s)", e,
                            instance.getName());
                } catch (IOException e) {
                    LOGGER.exception(
                            "PerfectInformationSolver.solvePerfectInformation-saveBKS (%s)", e,
                            instance.getName());
                }
                mCollector.collect(Integer.valueOf(run), instance.getName(), Integer.valueOf(size),
                        Integer.valueOf((int) instance.getFleet().getVehicle().getCapacity()),
                        Double.valueOf(mSolver.getSolveTime()),
                        Double.valueOf(mSolver.getObjectiveValue()),
                        Integer.valueOf(mSolver.getNumTrucks()), Arrays.toString(seeds));
                sIOLock.unlock();
            }
            return mSolver.getObjectiveValue();
        } else {
            LOGGER.warn("Error, solver status: %s", status);
            return Double.NaN;
        }
    }

    public static void main(String[] args) {
        int[] sizes = { 5 };
        int[] sets = { 1 };
        int[] runs = { 0, 99 };
        int numThreads = 1;

        if (args.length == 4) {
            sizes = vroom.common.utilities.Utilities.toIntArray(args[0]);
            sets = vroom.common.utilities.Utilities.toIntArray(args[1]);
            runs = vroom.common.utilities.Utilities.toIntArray(args[2]);
            numThreads = Integer.valueOf(args[3]);
        } else {
            System.err
                    .println("Wrong number of arguments, usage: main [sizes] [sets] [runs] numThreads");
            System.err.println("Using default values");
        }

        LoggerHelper.setupRootLogger(LoggerHelper.LEVEL_WARN, LoggerHelper.LEVEL_INFO, true);
        LOGGER.setLevel(LoggerHelper.LEVEL_INFO);
        CVRPSymphonySolver.LOGGER.setLevel(LoggerHelper.LEVEL_WARN);

        solvePerferctInformation(sizes, new int[] { 0, 1 }, sets, runs[0], runs[1], numThreads,
                60 * 10);
    }
}
