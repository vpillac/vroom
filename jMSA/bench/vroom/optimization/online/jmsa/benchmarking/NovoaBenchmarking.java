/**
 * 
 */
package vroom.optimization.online.jmsa.benchmarking;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;

import vroom.common.heuristics.cw.CWLogging;
import vroom.common.heuristics.utils.HeuristicsLogging;
import vroom.common.heuristics.vls.VLSLogging;
import vroom.common.modeling.dataModel.IVRPSolution;
import vroom.common.modeling.dataModel.Solution;
import vroom.common.modeling.io.NovoaPersistenceHelper;
import vroom.common.modeling.io.NovoaPersistenceHelper.DemandDistribution;
import vroom.common.utilities.StatCollector;
import vroom.common.utilities.StatCollector.Label;
import vroom.common.utilities.Stopwatch;
import vroom.common.utilities.Utilities;
import vroom.common.utilities.callbacks.CallbackStack;
import vroom.common.utilities.logging.LogPrintStream;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.optimization.online.jmsa.MSABase;
import vroom.optimization.online.jmsa.MSAGlobalParameters;
import vroom.optimization.online.jmsa.MSASequential;
import vroom.optimization.online.jmsa.benchmarking.NovoaRun.ReturnStatus;
import vroom.optimization.online.jmsa.utils.MSALogging;
import vroom.optimization.online.jmsa.vrp.VRPParameterKeys;
import vroom.optimization.online.jmsa.vrp.vrpsd.VRPSDConsensus;

/**
 * <code>NovoaBenchmarking</code>
 * <p>
 * Creation date: May 10, 2010 - 5:05:14 PM.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class NovoaBenchmarking implements Runnable {

    public static int                                       sAttemps      = 5;

    public static NovoaPersistenceHelper.DemandDistribution sDistribution = DemandDistribution.UNIFORM;

    /**
     * Path of the file containing the default values for the {@link MSAGlobalParameters}
     */
    public static String                                    sDefaultsFile = "./data/NovoaDefaults.conf";

    /**
     * Getter for this class logger
     * 
     * @return the logger associated with this class
     */
    public static LoggerHelper getLogger() {
        return LoggerHelper.getLogger(NovoaBenchmarking.class);
    }

    /** A per run time limit (in min) */
    public static int sRunTimeLimit = 15;

    /**
     * The Class <code>BenchmarkRun</code> is used for a single run of MSA on a Novoa instance
     * <p>
     * Creation date: Oct 8, 2010 - 12:01:15 PM.
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp" >SLP</a>
     * @version 1.0
     */
    public class BenchmarkRun implements Runnable {

        /** <code>[run, set, size, num, cap]</code> */
        private final Integer[]           runInfo;

        /** The seeds. */
        private final long[]              seeds;

        /** The params. */
        private final MSAGlobalParameters params;

        /** The comment. */
        private final String              comment;

        /**
         * Creates a new <code>BenchmarkRun</code>
         * 
         * @param run
         *            : <code>[run, set, size, num, cap]</code>
         * @param params
         * @param comment
         */
        public BenchmarkRun(Integer[] run, MSAGlobalParameters params, String comment) {
            runInfo = run;
            this.params = params;
            this.comment = comment;

            seeds = getSeeds(getRun(), getSize(), getRep(), getCap(), getSet());
        }

        private int getRun() {
            return runInfo[0];
        }

        private int getSize() {
            return runInfo[2];
        }

        private int getRep() {
            return runInfo[3];
        }

        private int getCap() {
            return runInfo[4];
        }

        private int getSet() {
            return runInfo[1];
        }

        /**
         * Collect statistics.
         * 
         * @param novoa
         *            the novoa
         * @param perfInf
         *            the perfect information cost
         */
        protected void collectStats(NovoaRun novoa, double perfInf) {
            Solution<?> solution = (Solution<?>) novoa.getMsa().getCurrentSolution();

            int failures = novoa.getInstance().getRequestCount() - getSize();

            double cost = novoa.getStatus() == ReturnStatus.NORMAL ? solution.getCost() : -1;
            double gap = novoa.getStatus() == ReturnStatus.NORMAL ? (cost - perfInf) / perfInf : -1;

            mStatsCollector.collect(Integer.valueOf(getRun()), novoa.getInstance().getName(),
                    getSize(), (int) novoa.getInstance().getFleet().getVehicle().getCapacity(),
                    Double.valueOf(novoa.getMsa().getTimer().readTimeS()), Double.valueOf(cost),
                    Double.valueOf(perfInf), Integer.valueOf(failures),
                    Double.valueOf(novoa.getTravelInvertSpeed()), comment, Arrays.toString(seeds),
                    novoa.getStatus().toString(), Double.valueOf(gap));
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {

            boolean completed = false;
            int attempts = 0;

            NovoaRun novoa = null;
            double perfInf = -1;

            if (sComparePerfInf) {
                perfInf = mPerfInf.solvePerfectInformation(getRun(), getSize(), getRep(), getCap(),
                        getSet(), 60 * 5, false, true, sDistribution);
            }

            Stopwatch timer = new Stopwatch();

            while (!completed && attempts < sAttemps) {
                attempts++;
                timer.restart();

                try {
                    novoa = new NovoaRun(getSet(), getSize(), getRep(), getCap(), getRun(), params,
                            sDistribution);
                    System.gc();
                } catch (IOException e) {
                    getLogger().exception("NovoaBenchmarking.BenchmarkRun.run", e);
                }

                if (sFastRun) {
                    novoa.setTravelInvertSpeed(1);
                }

                Runtime runtime = Runtime.getRuntime();

                long maxMemory = runtime.maxMemory() / (1024 * 1024);
                long allocatedMemory = runtime.totalMemory() / (1024 * 1024);
                long freeMemory = runtime.freeMemory() / (1024 * 1024);

                double p = (100f * mProgress) / mTotalRuns;

                long expectedTime = (long) (mTimer.readTimeMS() * (((double) mTotalRuns)
                        / mProgress - 1));

                StringBuilder log = new StringBuilder();
                log.append("-------------------------------------");
                log.append(String.format("\n Instance: %s run #%s (%s)  %.2f (etc: %s) %s", novoa
                        .getInstance().getName(), getRun(), Thread.currentThread().getName(), p,
                        Utilities.Time.millisecondsToString(expectedTime, 3, false, false),
                        new Date(System.currentTimeMillis())));
                log.append(String.format("\n  > Comment : %s", comment));
                log.append(String.format("\n  > Memory (f/a/m/tf): %sm/%sm/%sm/%sm", freeMemory,
                        allocatedMemory, maxMemory, (freeMemory + maxMemory - allocatedMemory)));
                log.append("\n-------------------------------------");
                print(log.toString());

                try {
                    novoa.setTimeLimit(sRunTimeLimit);
                    novoa.run();

                    completed = novoa.getStatus() == ReturnStatus.NORMAL;
                } catch (Exception e) {
                    novoa.getMsa().stop();
                    getLogger().exception("NovoaBenchmarking.BenchmarkRun.run", e);
                }

                IVRPSolution<?> solution = (IVRPSolution<?>) novoa.getMsa().getCurrentSolution();

                timer.stop();
                p = (100f * mProgress) / mTotalRuns;
                log = new StringBuilder();
                log.append("-------------------------------------");
                log.append(String.format("\n Instance: %s run #%s (%s)  %.2f (etc: %s) %s", novoa
                        .getInstance().getName(), getRun(), Thread.currentThread().getName(), p,
                        Utilities.Time.millisecondsToString(expectedTime, 3, false, false),
                        new Date(System.currentTimeMillis())));
                log.append(String.format("\n  > Comment : %s", comment));
                log.append(String.format("\n  > Run Time: %smin (status: %s)",
                        timer.readTimeS() / 60, novoa.getStatusString()));
                log.append(String.format("\n  > Solution: %s", solution));
                log.append(String.format("\n  > PerfInf : %s (gap: %.2f)", perfInf,
                        (solution.getCost() - perfInf) / perfInf * 100));
                log.append(String.format("\n  > Feasible: %s", mChecker.checkSolution(solution)));

                // Free memory
                if (!completed) {
                    if (attempts < sAttemps)
                        log.append(String.format("\n  > SIMULATION FAILED, NEW ATTEMPT (%s/%s)",
                                attempts, sAttemps));
                    else
                        log.append(String.format(
                                "\n  > SIMULATION FAILED AFTER %s ATTEMPT(S), ABORTING ", attempts));

                }
                log.append("\n-------------------------------------");
                print(log.toString());
            }

            collectStats(novoa, perfInf);
            if (completed) {
                synchronized (mRunList) {
                    mRunList.remove(runInfo);
                }
                writePendingRuns();
            }

            mProgress++;

            novoa = null;
            System.gc();
        }

        @Override
        public String toString() {
            return String.format("NovoaRun: set:%s size:%s rep:%s cap:%s run:%s", getSet(),
                    getSize(), getRep(), getCap(), getRun());
        }
    }

    /**
     * A factory for creating NovoaBenchmarkThread objects.
     */
    public static class NovoaBenchmarkThreadFactory implements ThreadFactory {

        /** The Thread id. */
        private static int         sThreadId = 0;

        /** The Group. */
        private static ThreadGroup sGroup    = new ThreadGroup(Thread.currentThread()
                                                     .getThreadGroup(), "NovoaBenchmarks");

        /*
         * (non-Javadoc)
         * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
         */
        @Override
        public Thread newThread(Runnable r) {
            sThreadId++;

            ThreadGroup group = new ThreadGroup(sGroup, String.format("BenchGroup-" + sThreadId,
                    sThreadId));

            return new Thread(group, r, "BenchThread-" + sThreadId);
        }
    }

    /**
     * <code>NovoaBenchmarkParameters</code> encapsulate information about a particular set of runs.
     */
    public static class NovoaBenchmarkSettings {
        protected final MSAGlobalParameters mGlobalParameters;
        protected final String              mComment;

        public NovoaBenchmarkSettings(MSAGlobalParameters globalParameters, String comment) {
            super();
            mGlobalParameters = globalParameters;
            mComment = comment;
        }
    }

    /** The default logger layout. */
    public static Layout DEFAULT_LOGGER_LAYOUT = new PatternLayout(
                                                       "%d{dd/MM HH:mm:ss} %-15c %-5p [%-10t] : %m%n");

    /**
     * Gets the default parameters from file {@link #sDefaultsFile}
     * 
     * @return the default parameters
     */
    public static MSAGlobalParameters getDefaultParameters() {
        VRPParameterKeys.registerRequiredParameters();
        MSAGlobalParameters params = new MSAGlobalParameters();
        try {
            params.loadParameters(new File(sDefaultsFile));
        } catch (Exception e) {
            getLogger().exception(
                    "NovoaBenchmarking.getDefaultParameters, loading defaults defined in NovoaRun",
                    e);
            params = new MSAGlobalParameters();
            NovoaRun.loadDefaultParameters(params);
        }
        return params;
    }

    /**
     * Creates a unique first seed from the given parameters.
     * 
     * @param run
     *            max value: 1000
     * @param size
     *            max value 200
     * @param rep
     *            max value 5
     * @param cap
     *            max value 2
     * @param set
     *            max value 2
     * @return a unique first seed for the given parameters
     */
    public static long getFirstSeed(int run, int size, int rep, int cap, int set) {
        int lset = 1;

        int lsize = 0;
        int hsize = 200;

        int lrep = 1;
        int hrep = 5;

        int lcap = 0;
        int hcap = 2;

        int lrun = 0;
        int hrun = 10000;

        long firstSeed = set - lset;
        firstSeed *= hsize - lsize + 1;
        firstSeed += size - lsize;
        firstSeed *= hrep - lrep + 1;
        firstSeed += rep - lrep;
        firstSeed *= hcap - lcap + 1;
        firstSeed += cap - lcap;
        firstSeed *= hrun - lrun + 1;
        firstSeed += run - lrun;

        // Check if the seed is valid
        if (firstSeed > MSABase.MAX_LAST_SEEDS - 6) {
            MSALogging
                    .getSetupLogger()
                    .warn("NovoaBenchmarking.getFirstSeed first seed is greater than the limit defined for MRG32ka (%s>%s)",
                            firstSeed, MSABase.MAX_LAST_SEEDS - 6);
        }

        // Use modulo operation to prevent exception in SSJ
        return (firstSeed * 6l) % (MSABase.MAX_LAST_SEEDS - 6l);
        // return firstSeed * 6l;
    }

    /**
     * Creates a set of seeds from the given parameters.
     * 
     * @param run
     *            max value: 1000
     * @param size
     *            max value 200
     * @param rep
     *            max value 5
     * @param cap
     *            max value 2
     * @param set
     *            max value 2
     * @return a set of seeds from the given parameters
     * @see #getFirstSeed(int, int, int, int, int)
     */
    public static long[] getSeeds(int run, int size, int rep, int cap, int set) {
        long seed = getFirstSeed(run, size, rep, cap, set);
        return new long[] { seed, seed + 1, seed + 2, seed + 3, seed + 4, seed + 5 };
    }

    /**
     * @param run
     * @param size
     * @param rep
     * @param cap
     * @param set
     * @return
     */
    public static String getInstanceCode(int run, int size, int rep, int cap, int set) {
        return getInstanceCode(run, size, rep, cap, set, sDistribution);
    }

    /**
     * @param run
     * @param size
     * @param rep
     * @param cap
     * @param set
     * @param distribution
     * @return
     */
    public static String getInstanceCode(int run, int size, int rep, int cap, int set,
            DemandDistribution distribution) {
        String base = String.format("%sc%s-%s",
                NovoaPersistenceHelper.getInstanceName(size, set, rep).replaceFirst(".dat", ""),
                NovoaPersistenceHelper.getCapacity(size, set, cap), run);
        if (distribution == DemandDistribution.UNIFORM) {
            return base;
        } else {
            return String.format("%s-%s", base, distribution.toString().substring(0, 2));
        }
    }

    /**
     * The main method.
     * 
     * @param args
     *            <ul>
     *            <li>-e [sizes] [sets] nruns</li>
     *            <li>-f instance_file</li>
     *            <li>-i configuration_file</li>
     *            <li>-t number_of_threads</li>
     *            <li>-c file_comment</li>
     *            <li>-d distribution [UNIFORM,NORMAL]</li>
     *            </ul>
     * @see NovoaRun#NovoaRun(int, int, int, int, long[])
     */
    public static void main(String[] args) {
        VRPParameterKeys.registerRequiredParameters();
        LoggerHelper.setupRootLogger(LoggerHelper.LEVEL_ERROR, LoggerHelper.LEVEL_ERROR, false);

        LinkedList<NovoaBenchmarkSettings> settings = new LinkedList<NovoaBenchmarkSettings>();

        MSAGlobalParameters p = getDefaultParameters();
        p.set(MSAGlobalParameters.SOLUTION_BUILDER_CLASS, VRPSDConsensus.class);
        settings.add(new NovoaBenchmarkSettings(p, "C"));

        // p = getDefaultParameters();
        // p.set(MSAGlobalParameters.SOLUTION_BUILDER_CLASS,
        // VRPSDSmartConsensus.class);
        // settings.add(new NovoaBenchmarkSettings(p, "SmartC"));

        Runtime.getRuntime().availableProcessors();
        NovoaBenchmarking benchmark = null;
        String comment = "na";

        String benchFile = null, configFiles = null;
        int[] sizes = null;
        int[] sets = null;
        int nRuns = -1, nThreads = Runtime.getRuntime().availableProcessors();

        int i = 0;
        try {
            while (i < args.length) {
                // A benchmark file is used
                if (args[i].equals("-f") && i < args.length - 1) {
                    i++;
                    benchFile = args[i++];
                }
                // Explicit runs definition
                else if (args[i].equals("-e") && i < args.length - 3) {
                    i++;
                    sizes = vroom.common.utilities.Utilities.toIntArray(args[i++]);
                    sets = vroom.common.utilities.Utilities.toIntArray(args[i++]);
                    nRuns = Integer.parseInt(args[i++]);
                }
                // Configuration files
                else if (args[i].equals("-i") && i < args.length - 1) {
                    i++;
                    configFiles = args[i++];
                }
                // Number of threads
                else if (args[i].equals("-t") && i < args.length - 1) {
                    i++;
                    nThreads = Integer.parseInt(args[i++]);
                }
                // Comment
                else if (args[i].equals("-c") && i < args.length - 1) {
                    i++;
                    comment = args[i++];
                }
                // Distribution
                else if (args[i].equals("-d") && i < args.length - 1) {
                    i++;
                    sDistribution = DemandDistribution.valueOf(args[i++]);
                }
                // Time limit
                else if (args[i].equals("-l") && i < args.length - 1) {
                    i++;
                    sRunTimeLimit = Integer.parseInt(args[i++]);
                } else {
                    i++;
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Wrong arguments");
            printUsage();
            System.exit(1);
        }

        // Configuration files
        if (configFiles != null) {
            settings.clear();
            if (configFiles.startsWith("[")) {
                String[] files = configFiles.replaceAll("[", "").replaceAll("]", "").split(",");
                for (String f : files) {
                    System.out.print(" Loading configuration file " + f);
                    MSAGlobalParameters params = getDefaultParameters();
                    try {
                        File configFile = new File(f);
                        params.loadParameters(configFile);
                        settings.add(new NovoaBenchmarkSettings(params, f));
                        System.out.printf(" : OK\n");
                    } catch (Exception e) {
                        System.out.printf(" : EROOR - %s\n", e.getClass().getSimpleName());
                        getLogger().exception("NovoaBenchmarking.main", e);
                    }
                }
            } else {
                MSAGlobalParameters params = getDefaultParameters();
                try {
                    System.out.print(" Loading configuration file " + configFiles);
                    params.loadParameters(new File(configFiles));
                    Object o = params.get(VRPParameterKeys.ACTUAL_REQUEST_CLASS);
                    System.out.println(o);
                    settings.add(new NovoaBenchmarkSettings(params, configFiles));
                    System.out.printf(" : OK\n");
                    o = params.get(VRPParameterKeys.ACTUAL_REQUEST_CLASS);
                    System.out.println(o);
                } catch (Exception e) {
                    System.out.printf(" : EROOR - %s\n", e.getClass().getSimpleName());
                    getLogger().exception("NovoaBenchmarking.main", e);
                    System.exit(1);
                }
            }
        } else {
        }

        // Benchmark file
        if (benchFile != null) {
            try {
                System.out.println(" Loading benchmark file " + benchFile);
                benchmark = new NovoaBenchmarking(benchFile, comment, settings);
            } catch (IOException e) {
                getLogger().exception("NovoaBenchmarking.main", e);
            }
        }
        // Explicit definition
        else if (sizes != null) {
            try {
                System.out.printf(" Benchmarks: sets:%s sizes:%s nRuns:%s", Arrays.toString(sets),
                        Arrays.toString(sizes), nRuns);
                benchmark = new NovoaBenchmarking(sizes, sets, nRuns, comment, settings);
            } catch (IOException e) {
                getLogger().exception("NovoaBenchmarking.main", e);
            }
        } else {
            System.out.println("Wrong arguments");
            printUsage();
        }

        if (benchmark != null) {
            benchmark.mThreadCount = Math.min(nThreads, Runtime.getRuntime().availableProcessors());
            benchmark.run();
        } else {
            System.exit(1);
        }

    }

    /**
     * Prints the main method usage (argument list)
     */
    private static void printUsage() {
        System.out.println("Arguments: ");
        System.out.println(" At one of the two:");
        System.out.println("  -f benchFile            : benchmark file");
        System.out.println("  -e [sizes] [sets] nruns : benchmark definitions");
        System.out.println(" Optional:");
        System.out.println("  -i configFile           : configuration file");
        System.out.println("  -t num                  : number of threads");
        System.out.println("  -c comment              : comment for the log/stats files");
        System.out.println("  -d distribution         : demand distribution [NORMAL,UNIFORM]");
    }

    /**
     * Print a message
     * 
     * @param format
     *            the format
     * @param args
     *            the args
     */
    protected static void print(String format, Object... args) {
        String string = String.format(format, args);
        System.out.println(string);
    }

    /** The Progress. */
    private int                                mTotalRuns, mProgress;

    /** The Timer. */
    private final Stopwatch                    mTimer;

    /**
     * The string that will be used to create the output file path: <br/>
     * <code>"%1$s/stats_%2$ty%2$tm%2$td_%2$tk-%2$tM.csv"</code> which will be converted to: <br/>
     * <code>outputPath/stats_yymmdd_hh-mm.csv</code>
     */
    public static final String                 OUTPUT_FILE_FORMAT_STRING  = "%1$s/%2$s_stats-%3$s.csv";

    /**
     * The string that will be used to create the pending run file: <br/>
     * <code>"%1$s/stats_%2$ty%2$tm%2$td_%2$tk-%2$tM.csv"</code> which will be converted to: <br/>
     * <code>data/pending_yymmdd_hh-mm.csv</code>
     */
    public static final String                 PENDING_FILE_FORMAT_STRING = "%1$s/%2$s_pending-%3$s.csv";

    /** The Constant LABELS. */
    public static final Label<?>[]             LABELS                     = new Label<?>[] {
            new Label<Integer>("run_id", Integer.class),
            new Label<String>("instance_name", String.class),
            new Label<Integer>("size", Integer.class),
            new Label<Integer>("vehicle_capacity", Integer.class),
            new Label<Double>("running_time", Double.class),
            new Label<Double>("cost", Double.class), new Label<Double>("perf_inf", Double.class),
            new Label<Integer>("failures", Integer.class),
            new Label<Double>("speed", Double.class), new Label<String>("comment", String.class),
            new Label<String>("seeds", String.class), new Label<String>("status", String.class),
            new Label<Double>("gap_pi", Double.class)                    };

    /** The Stats collector. */
    private final StatCollector                mStatsCollector;

    /** The Stat file. */
    private final String                       mStatFile;

    /** The Constant STAT_FILES_PATH. */
    public static final String                 STAT_FILES_PATH            = "./results";

    /** The Constant LOG_FILES_PATH. */
    public static final String                 LOG_FILES_PATH             = "./log";

    /** The number of threads used in the benchmarks. */
    private int                                mThreadCount               = Runtime
                                                                                  .getRuntime()
                                                                                  .availableProcessors();

    /** <code>true</code> if the running times should be shortened. */
    public static boolean                      sFastRun                   = false;

    /**
     * <code>true</code> if an exact algorithm should be run to compare with perfect information.
     */
    public static boolean                      sComparePerfInf            = true;

    /** A time limit for the perfect information solver. */
    public static int                          sPerfInfTimeLimit          = 300;

    /**
     * The string that will be used to create the log file path: <br/>
     * <code>"%1$s/stats_%2$ty%2$tm%2$td_%2$tk-%2$tM.csv"</code> wich will be converted to: <br/>
     * <code>outputPath/stats_yymmdd_hh-mm.csv</code>
     */
    public static final String                 LOG_FILE_FORMAT_STRING     = "%1$s/%2$s_log-%3$s.log";

    /** The Constant ERR_FILE_FORMAT_STRING. */
    public static final String                 ERR_FILE_FORMAT_STRING     = "%1$s/%2$s_errors-%3$s.log";

    /** The Checker. */
    private final SolutionChecker              mChecker                   = new SolutionChecker();

    /** The Perf inf. */
    private final PerfectInformationSolver     mPerfInf;

    /** The Executor. */
    private ExecutorService                    mExecutor;

    /** The err. */
    private final LogPrintStream               out, err;

    /** The Run list. */
    private final LinkedList<Integer[]>        mRunList;

    /** The file containing a list of pending runs */
    private final File                         mPendingFile;

    private final List<NovoaBenchmarkSettings> mSettings;

    /**
     * Sets the number of thread used for benchmarking
     * 
     * @param threadCount
     */
    public void setThreadCount(int threadCount) {
        mThreadCount = threadCount;
    }

    /**
     * Gets the thread count.
     * 
     * @return the number of thread used for benchmarking
     */
    public int getThreadCount() {
        return mThreadCount;
    }

    /**
     * Instantiates a new novoa benchmarking.
     * 
     * @param benchFile
     *            the bench file containing the run information
     * @param fileComment
     *            the file comment
     * @param settings
     *            a list of settings for the benchmarks
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public NovoaBenchmarking(String benchFile, String fileComment,
            List<NovoaBenchmarkSettings> settings) throws IOException {
        this(fileComment, settings);

        BufferedReader in = new BufferedReader(new FileReader(benchFile));

        // Skip first line (headers)
        String line = in.readLine();

        line = in.readLine();
        int lineId = 1;
        while (line != null) {
            line = line.replaceAll("\\s*", "");
            String[] runInfo = line.split(";|,|\\s");

            if (runInfo.length != 5) {
                in.close();
                throw new IOException(
                        String.format(
                                "Error while parsing line %s, should be in format run,set,size,rep,cap (%s)",
                                lineId, line));
            }

            int set = Integer.valueOf(runInfo[1]);
            int size = Integer.valueOf(runInfo[2]);
            int cap = Integer.valueOf(runInfo[4]);
            cap = NovoaPersistenceHelper.getCapacityIdx(size, set, cap);
            mRunList.add(new Integer[] { Integer.valueOf(runInfo[0]), set, size,
                    Integer.valueOf(runInfo[3]), cap });

            line = in.readLine();
            lineId++;
        }

        in.close();

        writePendingRuns();
    }

    /**
     * Instantiates a new novoa benchmarking.
     * 
     * @param sizes
     *            the sizes
     * @param sets
     *            the sets
     * @param numRuns
     *            the num runs
     * @param fileComment
     *            the file comment
     * @param settings
     *            a list of settings for the benchmarks
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public NovoaBenchmarking(int[] sizes, int[] sets, int numRuns, String fileComment,
            List<NovoaBenchmarkSettings> settings) throws IOException {
        this(fileComment, settings);

        for (int set : sets) {
            for (int size : sizes) {
                for (int cap = 0; cap < 2; cap++) {
                    for (int num = 1; num <= 5; num++) {
                        for (int run = 0; run < numRuns; run++) {
                            mRunList.add(new Integer[] { run, set, size, num, cap });
                        }
                    }
                }
            }
        }

        writePendingRuns();
    }

    /**
     * Instantiates a new novoa benchmarking.
     * 
     * @param fileComment
     *            the file comment
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected NovoaBenchmarking(String fileComment, List<NovoaBenchmarkSettings> settings)
            throws IOException {
        LoggerHelper.DEFAULT_CONSOLE_LAYOUT = DEFAULT_LOGGER_LAYOUT;

        @SuppressWarnings({ "unchecked", "rawtypes" })
        MSABase msa = new MSASequential(null, getDefaultParameters());

        String comment = String.format("Date: %s\nDemand distribution: %s\nMSA:\n%s\n\n", new Date(
                System.currentTimeMillis()), sDistribution, msa.getComponentsDescription());

        print(comment);

        String date = String.format("%1$ty%1$tm%1$td_%1$tk-%1$tM",
                new Date(System.currentTimeMillis()));

        mStatFile = String.format(OUTPUT_FILE_FORMAT_STRING, STAT_FILES_PATH, date, fileComment);
        mStatsCollector = new StatCollector(new File(mStatFile), true, false, comment, LABELS);

        out = new LogPrintStream(new File(String.format(LOG_FILE_FORMAT_STRING, LOG_FILES_PATH,
                date, fileComment)), System.out);
        err = new LogPrintStream(new File(String.format(ERR_FILE_FORMAT_STRING, LOG_FILES_PATH,
                date, fileComment)), System.err);
        System.setOut(out);
        System.setErr(err);

        mPerfInf = new PerfectInformationSolver();

        mTimer = new Stopwatch();

        mRunList = new LinkedList<Integer[]>();

        mPendingFile = new File(String.format(PENDING_FILE_FORMAT_STRING, STAT_FILES_PATH, date,
                fileComment));

        mSettings = settings;

        setupLoggers();
    }

    /**
     * Write the list of pending runs in a file
     */
    private synchronized void writePendingRuns() {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(mPendingFile, false));
            out.write("run;set;size;rep;cap\n");
            synchronized (mRunList) {
                for (Integer[] run : mRunList) {
                    out.write(String.format("%s;%s;%s;%s;%s\n", run[0], run[1], run[2], run[3],
                            NovoaPersistenceHelper.getCapacity(run[2], run[1], run[4])));
                }
            }
            out.flush();
            out.close();
        } catch (IOException e) {
            getLogger().exception("NovoaBenchmarking.writePendingRuns", e);
        }
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        print("#=====================================#");
        print("#       Running Novoa Benchmarks      #");
        print("#=====================================#");
        print("  Output file  : " + mStatFile);
        print("  Pending file : " + mPendingFile);
        print("  Threads      : %s on %s available processors", mThreadCount, Runtime.getRuntime()
                .availableProcessors());
        print("  Max memory   : %sm", Runtime.getRuntime().maxMemory() / (1024 * 1024));
        print("  # Runs       : %s", mRunList.size() * mSettings.size());
        print("#=====================================#");

        mTimer.start();

        mTotalRuns = mRunList.size() * mSettings.size();

        // mExecutor = Executors.newFixedThreadPool(getThreadCount(), new
        // NovoaBenchmarkThreadFactory());
        mExecutor = Executors.newFixedThreadPool(1, new NovoaBenchmarkThreadFactory());
        for (Integer[] run : mRunList) {
            for (NovoaBenchmarkSettings s : mSettings) {
                s.mGlobalParameters.set(MSAGlobalParameters.MAX_THREADS, getThreadCount());
                s.mGlobalParameters.set(MSAGlobalParameters.MIN_THREADS, getThreadCount());
                mExecutor.execute(new BenchmarkRun(run, s.mGlobalParameters, s.mComment));
            }

        }
        mExecutor.shutdown();

        synchronized (this) {
            try {
                this.wait(1000);
            } catch (InterruptedException e) {
                getLogger().exception("NovoaBenchmarking.run", e);
            }
        }

        print("Active Threads:");
        Thread[] activeThreads = new Thread[Thread.currentThread().getThreadGroup().activeCount() + 5];

        Thread.enumerate(activeThreads);

        for (Thread thread : activeThreads) {
            if (thread != null) {
                print("[%s] \t %s", thread.getThreadGroup().getName(), thread.getName());
            }
        }

        try {
            mExecutor.awaitTermination(365, TimeUnit.DAYS);

            CallbackStack.stopAllThreads();

            mTimer.stop();

            print("#=====================================#");
            print("#               FINISHED              #");
            print("#=====================================#");
            print("# Running time:%s", mTimer.readTimeString());
            print(" Waiting 60sec for callbacks to finish");
            CallbackStack.stopAllThreads();
            // Wait 1 minute for the callbacks to finish
            synchronized (this) {
                try {
                    this.wait(60000);
                    // Force the VM to stop
                    System.exit(0);
                } catch (InterruptedException e) {
                    getLogger().exception("NovoaBenchmarking.run", e);
                }
            }
        } catch (InterruptedException e) {
            getLogger().exception("NovoaBenchmarking.run", e);
        }

        System.err.println("MSA Procedure did not terminated as expected");
        System.exit(1);
    }

    /**
     * Setup the main loggers for Novoa benchmarking routine.
     */
    public void setupLoggers() {
        LoggerHelper.setupRootLogger(LoggerHelper.LEVEL_WARN, LoggerHelper.LEVEL_WARN, true);

        // Configuring loggers
        Logger.getRootLogger().setLevel(LoggerHelper.LEVEL_WARN);

        // MSA
        MSALogging.getBaseLogger().setLevel(LoggerHelper.LEVEL_WARN);

        // CW
        CWLogging.getBaseLogger().setLevel(LoggerHelper.LEVEL_WARN);

        // VLS
        VLSLogging.getBaseLogger().setLevel(LoggerHelper.LEVEL_WARN);

        // Heuristics
        HeuristicsLogging.getBaseLogger().setLevel(LoggerHelper.LEVEL_WARN);

        WriterAppender logAppender = new WriterAppender(DEFAULT_LOGGER_LAYOUT, err);
        logAppender.setThreshold(LoggerHelper.LEVEL_WARN);
        logAppender.setImmediateFlush(true);

        Logger.getRootLogger().removeAllAppenders();
        Logger.getRootLogger().addAppender(logAppender);
    }
}
