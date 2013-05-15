/**
 * 
 */
package vroom.trsp.bench;

import static vroom.trsp.util.TRSPGlobalParameters.THREAD_COUNT;
import gurobi.GRB.DoubleParam;
import gurobi.GRB.IntParam;
import gurobi.GRBException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import vroom.common.heuristics.alns.DiversifiedPool;
import vroom.common.modeling.io.DynamicPersistenceHelper;
import vroom.common.utilities.BatchThreadPoolExecutor;
import vroom.common.utilities.BestKnownSolutions;
import vroom.common.utilities.ProgressMonitor;
import vroom.common.utilities.StatCollector;
import vroom.common.utilities.Utilities;
import vroom.common.utilities.gurobi.GRBEnvProvider;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.common.utilities.logging.Logging;
import vroom.common.utilities.params.ParameterKey;
import vroom.common.utilities.params.ParametersFilePersistenceDelegate;
import vroom.trsp.bench.mpa.DTRSPRunMPA;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.io.ITRSPPersistenceHelper;
import vroom.trsp.optimization.TRSPUtilities;
import vroom.trsp.util.TRSPGlobalParameters;
import vroom.trsp.util.TRSPLogging;

/**
 * The class <code>TRSPBench</code> contains the main method to run benchmarks
 * <p>
 * Creation date: Sep 30, 2011 - 1:23:27 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TRSPBench implements Runnable {

    public static boolean    sFileLoggingEnabled = false;

    public static boolean    sVerbose            = false;

    private static TRSPBench sInstance;

    /**
     * Return the singleton instance of {@link TRSPBench} for this jvm
     * 
     * @return the singleton instance of {@link TRSPBench} for this jvm
     */
    public static TRSPBench getInstance() {
        return sInstance;
    }

    /**
     * Setup the benchmarking environment
     * 
     * @param params
     * @param noStat
     * @param fileCom
     */
    public static void setup(TRSPGlobalParameters params, boolean noStat, String fileCom) {
        if (sInstance != null)
            throw new IllegalStateException("Environment already setup");
        DiversifiedPool.sAutoAdjustWeights = params
                .get(TRSPGlobalParameters.ALNS_PALNS_DIV_AUTOADJUST);
        sInstance = new TRSPBench(params, noStat, fileCom);
    }

    private ProgressMonitor mProgressMonitor;

    /**
     * Returns the progress monitor of the benchmarks
     * 
     * @return the progress monitor of the benchmarks
     */
    public ProgressMonitor getProgress() {
        return mProgressMonitor;
    }

    private final TRSPGlobalParameters mParams;
    private final List<TRSPRunBase>    mRuns;
    private StatCollector              mCollector;
    private final String               mFileComment;
    private final BestKnownSolutions   mBKS;
    private final boolean              mNoStats;

    private File                       mStatFile;

    private boolean                    mStatAppend;

    /**
     * Set the file in which stats will be written, this will automatically set the {@link #mStatAppend statAppend} flag
     * to {@code true}
     * 
     * @param file
     */
    public void setStatFile(String file) {
        mStatFile = new File(file);
        mStatAppend = true;
    }

    /**
     * Returns the comment appended to the file name
     * 
     * @return the comment appended to the file name
     */
    public String getFileComment() {
        return mFileComment;
    }

    public BestKnownSolutions getBKS() {
        return mBKS;
    }

    protected TRSPBench(TRSPGlobalParameters params, boolean noStat, String fileCom) {
        mParams = params;
        mNoStats = noStat;
        mRuns = new LinkedList<TRSPRunBase>();
        mFileComment = fileCom;
        mProgressMonitor = new ProgressMonitor(0, false);

        // Params
        File outParamFile = new File(String.format("results/trsp_bench_%s_%s.cfg",
                Utilities.Time.getDateString(), fileCom));
        if (!noStat)
            try {
                params.saveParameters(outParamFile, false);
            } catch (IOException e2) {
                TRSPLogging.getRunLogger().exception("TRSPBench.main", e2);
            }

        if (sFileLoggingEnabled) {
            // Logging
            FileAppender logFileAppender;
            try {
                logFileAppender = new FileAppender(LoggerHelper.DEFAULT_CONSOLE_LAYOUT,
                        String.format("./log/trsp_bench_%s_%s.log", Utilities.Time.getDateString(),
                                fileCom), true);
                logFileAppender.setThreshold(LoggerHelper.LEVEL_WARN);
                Logger.getRootLogger().addAppender(logFileAppender);
            } catch (IOException e1) {
                TRSPLogging.getRunLogger().exception("TRSPBench.main", e1);
            }
        }

        // Get the best known solutions file
        String bksFile = mParams.get(TRSPGlobalParameters.RUN_BKS_FILE);
        mBKS = bksFile != null ? new BestKnownSolutions(bksFile) : null;
        mStatFile = new File(String.format("results/trsp_bench_%s_%s.csv",
                Utilities.Time.getDateString(), mFileComment));
    }

    public void createRuns() {
        List<File> files = null;
        String instanceFolder = mParams.get(TRSPGlobalParameters.RUN_INSTANCE_FOLDER);
        TRSPLogging.getRunLogger().info("Loading instances from directory %s (pattern: %s)",
                instanceFolder, mParams.get(TRSPGlobalParameters.RUN_FILE_PATTERN));
        try {
            files = Utilities.listFiles(instanceFolder,
                    mParams.get(TRSPGlobalParameters.RUN_FILE_PATTERN));
        } catch (Exception e) {
            TRSPLogging.getRunLogger().exception("TRSPBench.main", e);
            Logging.awaitLogging(60000);
            System.exit(1);
        }

        Map<String, List<File>> rdFileMapping = null;
        if (mParams.isDynamic()) {
            try {
                rdFileMapping = DynamicPersistenceHelper.getRelDateFiles(
                        mParams.get(TRSPGlobalParameters.RUN_REL_DATE_FOLDER),
                        mParams.get(TRSPGlobalParameters.RUN_DODS));
            } catch (FileNotFoundException e1) {
                TRSPLogging.getRunLogger().exception("TRSPBench.main", e1);
            }
        }

        // Get the correct instance reader
        // -------------------
        ITRSPPersistenceHelper reader = TRSPUtilities.getPersistenceHelper(instanceFolder);
        // -------------------

        // final TRSPGRBStatCollector grbCollector = new
        // TRSPGRBStatCollector(comment, detailStats, timeTargetStats);
        int runId = 0;
        Set<Integer> skipped = skippedRuns();
        for (File instanceFile : files) {
            try {
                TRSPInstance instance = reader.readInstance(instanceFile, mParams.isCVRPTW());

                List<File> rdFiles = rdFileMapping != null ? rdFileMapping.get(instance.getName())
                        : null;
                if (rdFiles == null)
                    rdFiles = Arrays.asList(new File(""));

                for (File rdFile : rdFiles) {
                    TRSPGlobalParameters paramsRun = mParams.clone();
                    long[] seeds = mParams.get(TRSPGlobalParameters.RUN_SEEDS);
                    for (int r = 0; r < mParams.get(TRSPGlobalParameters.RUN_NUM_REPLICAS); r++) {
                        if (skipped.contains(runId)) {
                            TRSPLogging.getRunLogger().info(
                                    "TRSPBench.main : Added run %s (%s, %s, %s, %s) ",
                                    runId,
                                    instance.getName(),
                                    rdFile.getName(),
                                    r,
                                    Utilities.toShortString(paramsRun
                                            .get(TRSPGlobalParameters.RUN_SEEDS)));

                        } else {
                            mRuns.add((TRSPRunBase) mParams.newInstance(
                                    TRSPGlobalParameters.RUN_CLASS, runId, instanceFile, rdFile,
                                    paramsRun, mBKS, r, rdFile.getName()));
                            TRSPLogging.getRunLogger().info(
                                    "TRSPBench.main : Added run %s (%s, %s, %s, %s) ",
                                    runId,
                                    instance.getName(),
                                    rdFile.getName(),
                                    r,
                                    Utilities.toShortString(paramsRun
                                            .get(TRSPGlobalParameters.RUN_SEEDS)));
                        }

                        runId++;
                        // Increment the seeds to have different runs
                        seeds = Arrays.copyOf(seeds, seeds.length);
                        for (int i = 0; i < seeds.length; i++)
                            seeds[i] += 1;
                        paramsRun = paramsRun.clone();
                        paramsRun.set(TRSPGlobalParameters.RUN_SEEDS, seeds);
                    }
                }
            } catch (Exception e) {
                TRSPLogging.getRunLogger().exception("TRSPBench.main", e);
            }
        }

        // Stats
        if (!mRuns.isEmpty()) {
            String comment = String.format("TRSP benchmark (%s)",
                    mParams.get(TRSPGlobalParameters.RUN_INSTANCE_FOLDER));
            mCollector = new StatCollector(mNoStats ? null : mStatFile, true, mStatAppend, comment,
                    mRuns.get(0).getLabels());
        }
    }

    /**
     * Return a set containing the ids of the runs to be skipped based on the existing stat file
     * 
     * @return a set containing the ids of the runs to be skipped
     */
    private Set<Integer> skippedRuns() {
        if (!mStatAppend || !mStatFile.exists())
            return Collections.emptySet();

        HashSet<Integer> skipped = new HashSet<Integer>();
        try {
            BufferedReader statReader = new BufferedReader(new FileReader(mStatFile));
            String line = statReader.readLine();
            while (line != null && !line.startsWith("run_id"))
                line = statReader.readLine();
            line = statReader.readLine();
            while (line != null) {
                skipped.add(Integer.valueOf(line.split(";")[0]));
                line = statReader.readLine();
            }
        } catch (IOException e) {
            TRSPLogging.getRunLogger().exception("TRSPBench.skippedRuns", e);
        }

        TRSPLogging.getRunLogger().info("TRSPBench.skippedRuns: Loaded skipped from file %s: %s",
                mStatFile.getName(), Utilities.toShortString(skipped));

        return skipped;
    }

    /**
     * Setup the {@link GRBEnvProvider#getEnvironment() GRBEnv}
     * 
     * @param params
     *            global parameters
     */
    public static void setupGRBEnv(TRSPGlobalParameters params) {
        try {
            GRBEnvProvider.getEnvironment().readParams(
                    params.get(TRSPGlobalParameters.SC_GRBENV_FILE));
            GRBEnvProvider.getEnvironment().set(IntParam.OutputFlag, 0);
            GRBEnvProvider.getEnvironment().set(DoubleParam.TimeLimit,
                    params.get(TRSPGlobalParameters.SC_MAX_TIME));
            GRBEnvProvider.getEnvironment().set(IntParam.Threads, params.getThreadCount());
        } catch (GRBException e2) {
            TRSPLogging.getRunLogger().exception("TRSPBench.", e2);
        }
    }

    /**
     * The main benchmarking method
     * 
     * @param args
     */
    public static void main(String[] args) {
        Level benchmarkLevel = LoggerHelper.LEVEL_INFO;
        Level algoLevel = LoggerHelper.LEVEL_ERROR;
        String fileCom = "";
        sFileLoggingEnabled = true;
        boolean noStat = false;

        // Read configuration file
        // ------------------------------------------------------------
        TRSPGlobalParameters params = new TRSPGlobalParameters();
        if (args.length < 1) {
            System.err.println("Must at least specify a configuration file");
            System.exit(1);
        }
        String paramFile = args[0];
        try {
            params.loadParameters(new File(paramFile));
        } catch (Exception e1) {
            e1.printStackTrace();
            System.exit(1);
        }
        // ------------------------------------------------------------

        String statFile = null;
        // Read command line arguments
        // ------------------------------------------------------------
        for (int i = 1; i < args.length; i++) {
            String a = args[i];
            if ("-n".equals(a) || "--no_stats".equals(a)) {
                noStat = true;
            } else if (a.equals("-t")) {
                int nt = Integer.valueOf(args[++i]);
                System.out.println("TRSPBench.main : Overridden the number of threads to " + nt);
                params.set(THREAD_COUNT, nt);
            } else if (a.startsWith("--profile")) {
                noStat = true;
                params.set(THREAD_COUNT, 1);
                BatchThreadPoolExecutor.sDebugSequential = true;
            } else if (a.equals("-c")) {
                fileCom = args[++i];
            } else if (a.equals("-s")) {
                // Ignore see next case
            } else if (a.equals("-a")) {
                statFile = args[++i];
                TRSPLogging.getSetupLogger().info("TRSPBench.main: continuing from file %s",
                        statFile);
                // Check if the config file exists
                File configFile = new File(statFile.replace(".csv", ".cfg"));
                if (configFile.exists()) {
                    try {
                        params.loadParameters(configFile);
                        TRSPLogging.getSetupLogger().info(
                                "TRSPBench.main: loaded parameters from file %s", configFile);
                    } catch (Exception e) {
                        TRSPLogging.getSetupLogger().exception("TRSPBench.main", e);
                    }
                }
            } else if (a.contains("=")) {
                try {
                    String[] p = a.split("=");
                    ParameterKey<?> key = params.getRegisteredKey(p[0]);
                    if (key == null)
                        throw new IllegalArgumentException("Unknown parameter key:" + p[0]);
                    params.setNoCheck(key,
                            ParametersFilePersistenceDelegate.castProperty(key, p[1]));
                    System.out.printf("TRSPBench.main : Set parameter %s=%s\n", key, p[1]);
                } catch (Exception e1) {
                    e1.printStackTrace();
                    System.exit(1);
                }
            } else if (a.equals("-v")) {
                sVerbose = true;
            } else {
                System.out.println("Unknown argument " + a);
            }
        }
        // ------------------------------------------------------------

        // Check the number of threads
        if (params.get(TRSPGlobalParameters.THREAD_COUNT) > Runtime.getRuntime()
                .availableProcessors())
            params.set(TRSPGlobalParameters.THREAD_COUNT, Runtime.getRuntime()
                    .availableProcessors());
        if (params.get(TRSPGlobalParameters.RUN_DEBUG))
            benchmarkLevel = LoggerHelper.LEVEL_DEBUG;
        setupLoggers(benchmarkLevel, algoLevel, LoggerHelper.LEVEL_INFO);

        TRSPLogging.getRunLogger().info("Benchmark started with configuration file %s", args[0]);
        TRSPLogging.getRunLogger().info(" Number of run threads         : %s",
                params.get(TRSPGlobalParameters.RUN_THREADS));
        TRSPLogging.getRunLogger().info(" Number of optimization threads: %s",
                params.get(TRSPGlobalParameters.THREAD_COUNT));

        if (params.get(TRSPGlobalParameters.SC_ENABLED))
            setupGRBEnv(params);

        setup(params, noStat, fileCom);
        if (statFile != null) {
            TRSPBench.getInstance().setStatFile(statFile);
        }
        TRSPBench.getInstance().createRuns();

        TRSPLogging.getRunLogger().info(" Added %s runs", getInstance().mRuns.size());

        getInstance().run();

        TRSPLogging.getRunLogger().info("FINISHED");

        Logging.awaitLogging(60000);
        System.exit(0);
    }

    protected static void setupLoggers(Level benchmarkLevel, Level algoLevel, Level consoleLevel) {
        LoggerHelper.DEFAULT_CONSOLE_LAYOUT = new PatternLayout(
                "%d{dd|HH:mm:ss} %-5p %-15c [%-10t] : %m%n");
        //
        // LoggerHelper.setupRootLogger(benchmarkLevel, benchmarkLevel, true);
        //
        // HeuristicsLogging.getRunLogger().setLevel(algoLevel);
        // AdaptiveLargeNeighborhoodSearch.getLogger().setLevel(algoLevel);
        // TRSPLogging.getRunLogger().setLevel(algoLevel);
        Logger.getRootLogger().setLevel(LoggerHelper.LEVEL_WARN);

        ConsoleAppender appender = new ConsoleAppender(LoggerHelper.DEFAULT_CONSOLE_LAYOUT);
        appender.setThreshold(consoleLevel != null ? consoleLevel : benchmarkLevel);
        TRSPLogging.getRunLogger().addAppender(appender);
        Logging.getSetupLogger().addAppender(appender);

        Logging.getSetupLogger().setLevel(LoggerHelper.LEVEL_WARN);
        TRSPLogging.getRunLogger().setLevel(benchmarkLevel);
        TRSPLogging.getSimulationLogger().setLevel(benchmarkLevel);

        if (sVerbose) {
            System.out.println("Enabled verbose logging");
            DTRSPRunMPA.setupVerboseLoggers();
        }
    }

    @Override
    public void run() {
        runAndCollect();
    }

    protected void runAndCollect() {
        mProgressMonitor = new ProgressMonitor(mRuns.size(), true);
        getProgress().start();
        if (mParams.get(TRSPGlobalParameters.RUN_DEBUG))
            runAndCollectDebug();
        if (mParams.get(TRSPGlobalParameters.RUN_THREADS) > 1)
            runAndCollectParallel();
        else
            runAndCollectSequential();
        getProgress().stop();
    }

    /**
     * Execute all the runs sequentially in an exception safe setting
     * 
     * @param runs
     * @param collector
     */
    protected void runAndCollectSequential() {
        ListIterator<TRSPRunBase> it = mRuns.listIterator();
        while (it.hasNext()) {
            TRSPRunBase run = it.next();
            try {
                run.call();
                run.collectStats(mCollector, false);
            } catch (Exception e) {
                TRSPLogging.getRunLogger().exception("TRSPBench.runAndCollect (%s)", e, run);
                try {
                    run.collectStats(mCollector, true);
                } catch (Exception e1) {
                    TRSPLogging.getRunLogger().exception("TRSPBench.runAndCollect (%s)", e1, run);
                }
            } finally {
                // Free up resources
                run.dispose();
                it.remove();
                System.gc();
            }
        }

    }

    /**
     * Execute all runs without catching any exception
     * 
     * @param runs
     * @param collector
     */
    protected void runAndCollectDebug() {
        for (TRSPRunBase run : mRuns) {
            try {
                run.call();
            } catch (Exception e) {
                TRSPLogging.getBaseLogger().exception("TRSPBench.runAndCollectDebug", e);
            }
            run.collectStats(mCollector, false);
            // Free up resources
            run.dispose();
        }
    }

    /**
     * Execute all runs in a {@link Executor}
     * 
     * @param runs
     * @param executor
     * @param collector
     */
    protected void runAndCollectParallel() {
        BatchThreadPoolExecutor executor = new BatchThreadPoolExecutor(
                mParams.get(TRSPGlobalParameters.RUN_THREADS), "bench");
        Map<TRSPRunBase, Future<TRSPSolution>> futures;
        try {
            futures = executor.submitBatch(mRuns, false);
        } catch (InterruptedException e2) {
            TRSPLogging.getBaseLogger().exception("TRSPBench.runAndCollect", e2);
            return;
        }

        while (!mRuns.isEmpty()) {
            Iterator<TRSPRunBase> runIt = mRuns.iterator();
            while (runIt.hasNext()) {
                TRSPRunBase run = runIt.next();
                Future<TRSPSolution> f = futures.get(run);

                if (f.isDone()) {
                    try {
                        f.get();
                        run.collectStats(mCollector, false);
                    } catch (Exception e) {
                        TRSPLogging.getRunLogger()
                                .exception("TRSPBench.runAndCollect (%s)", e, run);
                        try {
                            run.collectStats(mCollector, true);
                        } catch (Exception e1) {
                            TRSPLogging.getRunLogger().exception("TRSPBench.runAndCollect (%s)",
                                    e1, run);
                        }
                    } finally {
                        // Free up resources
                        run.dispose();
                        futures.remove(run);
                        runIt.remove();
                    }
                }
            }
            // Allow extra time
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                TRSPLogging.getBaseLogger().exception("TRSPBench.runAndCollect", e);
            }
        }
    }

    public List<TRSPRunBase> getRuns() {
        return mRuns;
    }
}
