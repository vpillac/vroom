package vroom.optimization.online.jmsa.benchmarking;

import java.awt.Dimension;
import java.awt.Font;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.lf5.LF5Appender;

import umontreal.iro.lecuyer.rng.MRG32k3a;
import umontreal.iro.lecuyer.rng.RandomStream;
import vroom.common.heuristics.GenericNeighborhood;
import vroom.common.heuristics.NeighborhoodBase;
import vroom.common.heuristics.cw.CWLogging;
import vroom.common.heuristics.vls.VLSLogging;
import vroom.common.heuristics.vns.VariableNeighborhoodSearch;
import vroom.common.modeling.dataModel.DynamicInstance;
import vroom.common.modeling.dataModel.Request;
import vroom.common.modeling.io.NovoaPersistenceHelper;
import vroom.common.modeling.io.NovoaPersistenceHelper.DemandDistribution;
import vroom.common.utilities.ExtendedReentrantLock;
import vroom.common.utilities.Stopwatch;
import vroom.common.utilities.callbacks.CallbackStack;
import vroom.common.utilities.dataModel.ObjectWithIdComparator;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.common.utilities.logging.Logging;
import vroom.common.utilities.logging.UnlimitedAsyncAppender;
import vroom.common.utilities.ssj.RandomGeneratorManager;
import vroom.optimization.online.jmsa.IActualRequest;
import vroom.optimization.online.jmsa.IDistinguishedSolution;
import vroom.optimization.online.jmsa.MSABase;
import vroom.optimization.online.jmsa.MSAGlobalParameters;
import vroom.optimization.online.jmsa.MSASequential;
import vroom.optimization.online.jmsa.events.MSACallbackBase;
import vroom.optimization.online.jmsa.events.MSACallbackEvent;
import vroom.optimization.online.jmsa.events.MSACallbackEvent.EventTypes;
import vroom.optimization.online.jmsa.events.ResourceEvent;
import vroom.optimization.online.jmsa.utils.MSALogging;
import vroom.optimization.online.jmsa.vrp.MSAVRPInstance;
import vroom.optimization.online.jmsa.vrp.VRPActualRequest;
import vroom.optimization.online.jmsa.vrp.VRPParameterKeys;
import vroom.optimization.online.jmsa.vrp.VRPPoolCleanerBase;
import vroom.optimization.online.jmsa.vrp.VRPRequestSamplerBase;
import vroom.optimization.online.jmsa.vrp.VRPRequestValidatorBase;
import vroom.optimization.online.jmsa.vrp.VRPScenario;
import vroom.optimization.online.jmsa.vrp.visu.MSAVisualizationFrame;
import vroom.optimization.online.jmsa.vrp.vrpsd.VRPSDActualRequest;
import vroom.optimization.online.jmsa.vrp.vrpsd.VRPSDConsensus;
import vroom.optimization.online.jmsa.vrp.vrpsd.VRPSDResourceHandler;
import vroom.optimization.online.jmsa.vrp.vrpsd.VRPSDSVScenarioOptimizer;
import vroom.optimization.online.jmsa.vrp.vrpsd.VRPSDScenarioGenerator;
import vroom.optimization.online.jmsa.vrp.vrpsd.VRPSDScenarioUpdater;

/**
 * <code>NovoaRun</code> is a class used to run an MSA procedure on a simulation of a Novoa instance
 * <p>
 * Creation date: May 11, 2010 - 11:26:27 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 2.0
 */
public class NovoaRun implements Runnable {

    public static class NovoaRunThread extends Thread {
        private MSABase<?, ?>       mMSA;
        private ReturnStatus        mStatus;
        private String              mMessage;

        private final ReentrantLock mLock            = new ReentrantLock();
        private final Condition     mStatusCondition = mLock.newCondition();

        public Condition getStatusCondition() {
            return mStatusCondition;
        }

        public void setMSA(MSABase<?, ?> mSA) {
            mMSA = mSA;
        }

        public ReturnStatus getStatus() {
            return mStatus;
        }

        private void setStatus(ReturnStatus status) {
            mLock.lock();
            mStatus = status;
            mStatusCondition.signalAll();
            mLock.unlock();
        }

        public NovoaRunThread(ThreadGroup group, Runnable target, String name) {
            super(group, target, name);
            mMessage = "na";
            setStatus(ReturnStatus.UNKNOWN);
        }

        @Override
        public void run() {
            try {
                setStatus(ReturnStatus.RUNNING);
                super.run();
                mMessage = "ok";
                setStatus(ReturnStatus.NORMAL);
            } catch (Exception e) {
                mMessage = e.getClass().getSimpleName();
                setStatus(ReturnStatus.EXCEPTION);
                mMSA.stop();
                LOGGER.exception("NovoaRunThread.run MSA:%s", e, mMSA);
            } catch (OutOfMemoryError e) {
                mMSA.stop();
                mMSA.getProxy().getScenarioPool().clear();
                System.gc();
                mMessage = e.getClass().getSimpleName();
                setStatus(ReturnStatus.EXCEPTION);
                e.printStackTrace();
            }
        }

        public String getMessage() {
            return mMessage;
        }

    }

    public static class NovoaRunThreadFactory implements ThreadFactory {

        private static int sThreadId = 0;

        @Override
        public NovoaRunThread newThread(Runnable r) {
            sThreadId++;
            NovoaRunThread t = new NovoaRunThread(Thread.currentThread().getThreadGroup(), r,
                    String.format("NovoaRun-%s", sThreadId));
            t.setMSA((MSABase<?, ?>) r);
            return t;
        }

    }

    protected static final NovoaRunThreadFactory sThreadFactory = new NovoaRunThreadFactory();

    /**
     * <code>ServiceSimulationCallback</code> is a callback that will be associated to
     * {@linkplain EventTypes#MSA_NEW_DISTINGUISHED_SOLUTION new distinguished mSolution} events and will simulate the
     * servicing of a request
     * <p>
     * Creation date: 3 mai 2010 - 16:02:03
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp" >SLP</a>
     * @version 1.0
     */
    private class ServiceSimulationCallback extends MSACallbackBase {

        private ServiceSimulationCallback() {
        }

        /*
         * (non-Javadoc)
         * @see vroom.optimization.online.jmsa.events.MSACallbackBase#execute(edu .uniandes
         * .copa.jMSA.events.MSACallbackEvent, java.lang.Object[])
         */
        @Override
        public synchronized void execute(MSACallbackEvent event) {
            switch (event.getType()) {
            case MSA_NEW_DISTINGUISHED_SOLUTION:
                if (event.getParams()[1] != null && !mSimulation.isBusy()) {
                    mSimulation.setSolution((IDistinguishedSolution) event.getParams()[1]);
                }

                break;
            case EVENTS_RESOURCE:
                ResourceEvent resEv = (ResourceEvent) event.getParams()[0];
                switch (resEv.getType()) {
                case STOP:
                    info("Resource STOP detected, ending the MSA procedure");
                    stop();
                    break;

                default:
                    // DO NOTHING
                    break;
                }
                break;
            case MSA_END:
            }

        }

        @Override
        public int getPriority() {
            return 0;
        }

        @Override
        public boolean isExecutedSynchronously() {
            return false;
        }

    }

    private class ServiceSimulationRunnable implements Runnable {

        private boolean                mRunning = true;
        private IDistinguishedSolution mSolution;

        public boolean isBusy() {
            return mSolution != null;
        }

        @Override
        public void run() {
            Object lock = new Object();
            mRunning = true;

            synchronized (lock) {
                while (mRunning) {
                    while (mRunning && mSolution == null) {
                        try {
                            lock.wait(200);
                        } catch (InterruptedException e) {
                            exception("Exception caught in simulation thread", e);
                        }
                    }
                    if (mSolution != null) {
                        simulateService();
                        mSolution = null;
                    }
                }
            }
        }

        protected synchronized void setSolution(IDistinguishedSolution solution) {
            mSolution = solution;
        }

        private synchronized void simulateService() {
            info("New distinguished mSolution: " + mSolution);
            info(" Pending requets: " + mInstance.getPendingRequests().size());

            for (int resource = 0; resource < mInstance.getFleet().size(); resource++) {
                VRPActualRequest currentReq = (VRPActualRequest) mSolution.getNextRequest(resource);
                if ((currentReq == null || currentReq.isDepot())
                        && mInstance.getPendingRequests().isEmpty()) {
                    info("Resource has finished servicing all requests");
                    mMSA.getEventFactory().raiseResourceStop(resource, currentReq);
                } else if (currentReq != null) {

                    info("Enforce decision: " + currentReq);
                    mMSA.getEventFactory().raiseRequestAssignedEvent(resource, currentReq);

                    VRPActualRequest lastReq = mInstance.getLastAssignedRequest(0);
                    int travelTime = lastReq != null ? (int) Math.ceil((mInstance.getCostDelegate()
                            .getDistance(lastReq, currentReq) * mTravelInvertSpeed)) : 1;

                    info("Simulating travel time");
                    waitSec(travelTime);

                    double d[] = null;
                    if (currentReq instanceof VRPSDActualRequest) {
                        final double[] dem = new double[] { mDemands.get(currentReq) };
                        d = dem;
                        info("Start of service event: %s actual demands: %s", currentReq,
                                Arrays.toString(dem));

                    } else {
                        info("Start of service event: " + currentReq);
                    }
                    mMSA.getEventFactory().raiseStartOfServiceEvent(resource, currentReq, d);

                    info("Simulating service time");
                    waitSec(SERVICE_TIME);
                    info("End of service event: " + currentReq);
                    mMSA.getEventFactory().raiseEndOfServiceEvent(resource, currentReq);
                }
            }
        }

        public void stop() {
            mRunning = false;
        }

    }

    /** A return status for the run */
    public static enum ReturnStatus {
        NORMAL, TIME_LIMIT, EXCEPTION, RUNNING, UNKNOWN
    };

    public static String                INSTANCE_DIRECTORY     = "../Instances/vrpsd/novoa/";
    /** Instance name format string: [S]i_[n]r[nodeJ].dat */
    public static final String          INSTANCE_FORMAT        = "%si_%sr%s.dat";
    public static final String          LOG_FILE_FORMAT_STRING = "log/%1$ty%1$tm%1$td_%1$tk-%1$tM_run_log.log";

    public static Layout                LOG_FILE_LAYOUT        = new PatternLayout(
                                                                       "%c %-30d{HH:mm:ss} %-5p%n [%-10t] : %m%n");

    protected static final LoggerHelper LOGGER                 = LoggerHelper
                                                                       .getLogger("NovoaBenchmark");

    public static int                   SERVICE_TIME           = 5;

    // 0 i_20r1c91 (easy?)
    // static int sCap = 91;
    // static int sRep = 1;
    // static int sSet = 1;
    // static int sSize = 20;
    // static int sRun = 0;

    // 0 i_30r1c137 (easy)
    static int                          sCap                   = 137;
    static int                          sRep                   = 1;
    static int                          sSet                   = 1;
    static int                          sSize                  = 30;
    static int                          sRun                   = 0;

    // run 38 i_30r2c87 (hard)
    // static int sCap = 87;
    // static int sRep = 2;
    // static int sSet = 1;
    // static int sSize = 30;
    // static int sRun = 38;

    // run 1 i_40r1c116
    // static int sCap = 116;
    // static int sRep = 1;
    // static int sSet = 1;
    // static int sSize = 40;
    // static int sRun = 1;

    // 94 i_40r1c116
    // static int sCap = 116;
    // static int sRep = 1;
    // static int sSet = 1;
    // static int sSize = 40;
    // static int sRun = 94;

    // run set size rep cap
    // 46 1 40 1 116
    // static int sCap = 116;
    // static int sRep = 1;
    // static int sSet = 1;
    // static int sSize = 40;
    // static int sRun = 46;

    // static int sCap = 9;
    // static int sRep = 3;
    // static int sSet = 1;
    // static int sSize = 5;
    // static int sRun = 25;

    // 28 i_40r3c116 (hard?)
    // static int sCap = 116;
    // static int sRep = 3;
    // static int sSet = 1;
    // static int sSize = 40;
    // static int sRun = 28;

    // 12 i_40r5c183 (hard)
    // static int sCap = 183;
    // static int sRep = 5;
    // static int sSet = 1;
    // static int sSize = 40;
    // static int sRun = 12;

    public static int                   TRAVEL_TIME            = 5;

    /**
     * log an error message when an exception is caught
     * 
     * @param context
     * @param e
     * @param args
     */
    public void exception(String context, Exception e, Object... args) {
        Object[] argst = new Object[args.length + 4];
        argst[0] = getMsa() != null && getMsa().getInstance() != null ? getMsa().getInstance()
                .getName() : "na";
        argst[1] = mRunId;
        argst[2] = e.getClass().getSimpleName();
        argst[3] = e.getMessage();
        for (int i = 4; i < argst.length; i++) {
            argst[i] = args[i - 4];
        }
        LOGGER.error("NovoaRun[%s-%s] > Exception caught in " + context + " %s: %s", e, argst);
    }

    /**
     * log an error message when an exception is caught
     * 
     * @param context
     * @param e
     * @param args
     */
    public void error(String message, Object... args) {
        Object[] argst = new Object[args.length + 2];
        argst[0] = getMsa() != null && getMsa().getInstance() != null ? getMsa().getInstance()
                .getName() : "na";
        argst[1] = mRunId;
        for (int i = 3; i < argst.length; i++) {
            argst[i] = args[i - 3];
        }
        LOGGER.error("NovoaRun[%s-%s] > " + message, argst);
    }

    /**
     * Log an info message
     * 
     * @param message
     */
    public void info(String message, Object... args) {
        Object[] argst = new Object[args.length + 2];
        argst[0] = getMsa() != null && getMsa().getInstance() != null ? getMsa().getInstance()
                .getName() : "na";
        argst[1] = mRunId;
        for (int i = 2; i < argst.length; i++) {
            argst[i] = args[i - 2];
        }
        LOGGER.info("NovoaRun[%s-%s] > " + message, argst);
    }

    /**
     * Load default parameters for the MSA procedure
     * 
     * @param params
     */
    public static void loadDefaultParameters(MSAGlobalParameters params) {
        VRPParameterKeys.registerRequiredParameters();
        params.resetDefaultValues();

        params.set(MSAGlobalParameters.POOL_CLEANER_CLASS, VRPPoolCleanerBase.class);
        params.set(MSAGlobalParameters.REQUEST_VALIDATOR_CLASS, VRPRequestValidatorBase.class);
        params.set(MSAGlobalParameters.REQUEST_SAMPLER_CLASS, VRPRequestSamplerBase.class);
        params.set(MSAGlobalParameters.POOL_SIZE, 50);
        params.set(MSAGlobalParameters.POOL_INITIAL_PROPORTION, 0.7);
        params.set(MSAGlobalParameters.SCENARIO_GENERATOR_CLASS, VRPSDScenarioGenerator.class);
        params.set(VRPParameterKeys.ACTUAL_REQUEST_CLASS, VRPSDActualRequest.class);
        params.set(MSAGlobalParameters.SCENARIO_OPTIMIZER_CLASS, VRPSDSVScenarioOptimizer.class);
        params.set(MSAGlobalParameters.SCENARIO_UPDATER_CLASS, VRPSDScenarioUpdater.class);
        params.set(MSAGlobalParameters.SOLUTION_BUILDER_CLASS, VRPSDConsensus.class);
        params.set(MSAGlobalParameters.RANDOM_SEED, 0l);
    }

    /**
     * Main method that run a test
     * 
     * @param args
     *            set instanceSize replica capacity
     */
    public static void main(String[] args) {
        NeighborhoodBase.setCheckSolutionAfterMove(false);
        GenericNeighborhood.setCheckSolutionAfterMove(false);

        setupFonts(24);

        setupLoggers(LoggerHelper.LEVEL_DEBUG, LoggerHelper.LEVEL_DEBUG, false, false, true);
        LOGGER.setLevel(LoggerHelper.LEVEL_DEBUG);
        VariableNeighborhoodSearch.LOGGER.setLevel(LoggerHelper.LEVEL_WARN);
        ExtendedReentrantLock.LOGGER.setLevel(LoggerHelper.LEVEL_LOW_DEBUG);

        PerfectInformationSolver pisolver = new PerfectInformationSolver();

        // VLSLogging.getBaseLogger().setLevel(LoggerHelper.LEVEL_LOW_DEBUG);
        // CWLogging.getBaseLogger().setLevel(LoggerHelper.LEVEL_INFO);

        int run = sRun;
        int set = sSet;
        int size = sSize;
        int rep = sRep;
        int cap = NovoaPersistenceHelper.getCapacityIdx(size, set, sCap);

        // DemandDistribution demDist = DemandDistribution.NORMAL;
        DemandDistribution demDist = DemandDistribution.UNIFORM;

        if (args.length == 4) {
            try {
                set = Integer.valueOf(args[0]);
                size = Integer.valueOf(args[1]);
                rep = Integer.valueOf(args[2]);
                cap = Integer.valueOf(args[3]);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                System.out.println("Arguments: set instanceSize replica capacity");
                System.exit(1);
            }
        } else {
            System.out.println("Arguments: set instanceSize replica capacity");
        }

        MSAGlobalParameters params = new MSAGlobalParameters();
        loadDefaultParameters(params);
        params.set(MSAGlobalParameters.MAX_THREADS, Runtime.getRuntime().availableProcessors() - 2);
        params.set(MSAGlobalParameters.MIN_THREADS, Runtime.getRuntime().availableProcessors() - 2);
        // params.set(MSAGlobalParameters.MAX_THREADS, 1);
        // params.set(MSAGlobalParameters.MIN_THREADS, 1);
        params.set(MSAGlobalParameters.SOLUTION_BUILDER_CLASS, VRPSDConsensus.class);
        // params.set(MSAGlobalParameters.SOLUTION_BUILDER_CLASS, VRPSDDetourRegret.class);
        // params.set(MSAGlobalParameters.SOLUTION_BUILDER_CLASS, VRPSDSmartConsensus.class);
        // params.set(MSAGlobalParameters.SOLUTION_BUILDER_CLASS,
        // VRPSDSampledRegret.class);

        // params.set(MSAGlobalParameters.POOL_SIZE, 10);

        NovoaRun novoa = null;
        try {
            novoa = new NovoaRun(set, size, rep, cap, run, params, demDist);
        } catch (IOException e) {
            LOGGER.exception("NovoaRun.main", e);
            System.exit(1);
        }
        // Visualization
        MSAVisualizationFrame frame = new MSAVisualizationFrame(novoa.mMSA,
                pisolver.solvePerfectInformation(run, size, rep, cap, set, Integer.MAX_VALUE,
                        false, true, demDist));

        frame.pack();
        frame.setSize(new Dimension(1200, 700));

        frame.setVisible(true);

        // novoa.setTravelInvertSpeed(5);
        novoa.setTimeLimit(30);
        novoa.run();

        Object o = new Object();
        synchronized (o) {
            try {
                o.wait(10 * 1000);
            } catch (InterruptedException e) {

            }
        }

        CallbackStack.stopAllThreads();

        System.out.println(novoa.getStatus());

        novoa = null;
        // frame.detach();
        Runtime.getRuntime().gc();

    }

    private static void setupFonts(float size) {
        // UIManager.put("TextField.font", new FontUIResource("Arial", Font.PLAIN, 17));
        UIDefaults defaults = UIManager.getDefaults();
        Enumeration<?> keys = defaults.keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = defaults.get(key);
            if (value != null && value instanceof Font) {
                UIManager.put(key, null);
                Font font = UIManager.getFont(key);
                if (font != null) {
                    UIManager.put(key, new FontUIResource(font.deriveFont(size)));
                }
                System.out.println(key);
            }
        }
    }

    /**
     * Setup the main loggers for the novoa run
     * 
     * @param msaLoggerLevel
     *            {@link Level} for the MSA logger
     * @param rootLoggerLevel
     *            {@link Level} for the root logger
     * @param addSwingAppender
     *            <code>true</code> if a swing appender should be added
     * @param addFileAppender
     *            <code>true</code> if a default file appender should be added
     * @param async
     *            <code>true</code> if appending should be done in an asynchronous way (recomended)
     */
    public static void setupLoggers(Level msaLoggerLevel, Level rootLoggerLevel,
            boolean addSwingAppender, boolean addFileAppender, boolean async) {

        Level min = msaLoggerLevel;
        if (msaLoggerLevel.isGreaterOrEqual(rootLoggerLevel)) {
            min = rootLoggerLevel;
        }

        Logging.setupRootLogger(min, LoggerHelper.LEVEL_LOW_DEBUG, async);

        // Configuring loggers

        // MSA
        Logging.setLoggerLevel(MSALogging.BASE_LOGGER, msaLoggerLevel);

        // CW
        Logging.setLoggerLevel(CWLogging.BASE_LOGGER, LoggerHelper.LEVEL_WARN);

        // VLS
        Logging.setLoggerLevel(VLSLogging.BASE_LOGGER, LoggerHelper.LEVEL_WARN);

        // Test
        LOGGER.setLevel(msaLoggerLevel);

        if (addSwingAppender || addFileAppender) {
            UnlimitedAsyncAppender appender = new UnlimitedAsyncAppender();
            appender.setBufferSize(2048);

            if (addSwingAppender) {
                LF5Appender swingAppender = new LF5Appender();
                appender.addAppender(swingAppender);
            }

            if (addFileAppender) {
                try {
                    FileAppender fileAppender = new FileAppender(LOG_FILE_LAYOUT, String.format(
                            LOG_FILE_FORMAT_STRING, new Date(System.currentTimeMillis())), true);
                    appender.addAppender(fileAppender);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            MSALogging.getBaseLogger().addAppender(appender);
            CWLogging.getBaseLogger().addAppender(appender);
            VLSLogging.getBaseLogger().addAppender(appender);
        }
    }

    /**
     * Pause the current thread for the given time
     * 
     * @param sec
     */
    void waitSec(int sec) {
        info("Waiting " + sec + "s");
        try {
            Thread.sleep(sec * 1000);
        } catch (InterruptedException e) {
            exception("Exception caught while waiting", e);
        }
    }

    private final Map<VRPActualRequest, Double>        mDemands;

    private final MSAGlobalParameters                  mGlobalParams;

    private final MSAVRPInstance                       mInstance;

    private final MSABase<VRPScenario, MSAVRPInstance> mMSA;

    private final RandomGeneratorManager               mRNDGenManager;

    private final RandomStream                         mRNDStream;

    private final DynamicInstance                      mSimInstance;

    private final ServiceSimulationRunnable            mSimulation;

    private double                                     mTravelInvertSpeed = 40;

    private ReturnStatus                               mReturnStatus;

    private int                                        mTimeLimit;

    private final int                                  mRunId;
    private NovoaRunThread                             mMsaThread;

    /**
     * Creates a new <code>NovoaRunningTest</code>
     * 
     * @param set
     *            the instance set (1 or 2)
     * @param size
     *            the instance size (5,8,20,30,40,60,100,150)
     * @param num
     *            the instance number (1 to 5)
     * @param cap
     *            the vehicle capacity (0 or 1)
     * @param runId
     * @throws IOException
     *             if the instance could not be read
     * @see NovoaPersistenceHelper#readInstance(int, int, int, int, Long)
     */
    public NovoaRun(int set, int size, int num, int cap, int runId, MSAGlobalParameters params,
            DemandDistribution dist) throws IOException {

        mRunId = runId;

        long[] seeds = NovoaBenchmarking.getSeeds(runId, size, num, cap, set);

        // 2 hours time limit
        setTimeLimit(60 * 2);

        // Read instance
        // ----------------------------------------------------------------------
        if (params != null) {
            mGlobalParams = params;
        } else {
            mGlobalParams = new MSAGlobalParameters();

            loadDefaultParameters(mGlobalParams);
        }

        // Read instance
        // ----------------------------------------------------------------------
        NovoaPersistenceHelper instanceLoader = new NovoaPersistenceHelper();
        instanceLoader.setDemandDistribution(dist);
        mInstance = new MSAVRPInstance(instanceLoader.readInstance(size, set, num, cap, null),
                mGlobalParams);
        // ----------------------------------------------------------------------
        info("");
        info("----------------------------------------");
        info("Loaded instance:");
        info("----------------------------------------");
        info(" Name: " + mInstance.getName());
        info(" Size: " + mInstance.getRequestCount());
        info(" Vehicle Capacity: " + mInstance.getFleet().getVehicle(0).getCapacity());
        info(" Seeds: " + Arrays.toString(seeds));

        // Random number generators
        // ----------------------------------------------------------------------
        MRG32k3a.setPackageSeed(seeds);
        mRNDStream = new MRG32k3a("SimulationStream");
        ((MRG32k3a) mRNDStream).setSeed(seeds);

        mRNDGenManager = new RandomGeneratorManager();
        mRNDGenManager.setRandomStream(mRNDStream);

        // ----------------------------------------------------------------------
        // Sample demands
        // ----------------------------------------------------------------------
        mDemands = NovoaSimulationDemands.getDemands(seeds, mInstance);
        List<VRPActualRequest> pendingRequests = mInstance.getPendingRequests();
        Collections.sort(pendingRequests, new ObjectWithIdComparator());
        for (VRPActualRequest req : pendingRequests) {
            info(" \t%s  ->  d=%s", req, mDemands.get(req));

        }
        // ----------------------------------------------------------------------

        // Seeds for the MSA procedure
        long[] seedsL = new long[seeds.length];
        for (int i = 0; i < seedsL.length; i++) {
            seedsL[i] = (long) Math.floor(mRNDStream.nextDouble() * 4294944443l);
        }
        mGlobalParams.set(MSAGlobalParameters.POOL_SIZE, Math.min(size * 10, 100));
        mGlobalParams.set(MSAGlobalParameters.RANDOM_SEEDS, seedsL);
        // ----------------------------------------------------------------------

        // Create the simulation instance
        // ----------------------------------------------------------------------
        mSimInstance = new DynamicInstance(getInstance().getName(), getInstance().getID(),
                getInstance().getRoutingProblem());

        mSimInstance.setFleet(getInstance().getFleet());
        mSimInstance.setDepots(Collections.singletonList(getInstance().getDepot(0)));
        mSimInstance.setCostHelper(getInstance().getCostDelegate());

        for (Entry<VRPActualRequest, Double> entry : mDemands.entrySet()) {
            // Create a copy of the request
            Request r = new Request(entry.getKey().getID(), entry.getKey().getNode());
            r.setDemands(entry.getValue());

            mSimInstance.addRequest(r);
        }
        // ----------------------------------------------------------------------

        // Auto adjust travel speed for faster simulations
        // ----------------------------------------------------------------------
        mTravelInvertSpeed = Math.max(3, size / 10);

        info("  Travel speed: %ss/unit", mTravelInvertSpeed);

        info("----------------------------------------");
        // ----------------------------------------------------------------------

        // Initialize the MSA procedure
        // ----------------------------------------------------------------------
        mMSA = new MSASequential<VRPScenario, MSAVRPInstance>(mInstance, mGlobalParams);

        mMSA.setEventHanlder(ResourceEvent.class, new VRPSDResourceHandler(mMSA.getProxy()));

        mSimulation = new ServiceSimulationRunnable();

        try {
            mMSA.registerCallback(EventTypes.MSA_NEW_DISTINGUISHED_SOLUTION,
                    new ServiceSimulationCallback());
            mMSA.registerCallback(EventTypes.EVENTS_RESOURCE, new ServiceSimulationCallback());

            mMSA.registerCallback(EventTypes.MSA_NEW_DISTINGUISHED_SOLUTION,
                    new DistinguishedSolutionCallback("solutions.out"));

            mMSA.registerCallback(EventTypes.EVENTS_RESOURCE, new ResourceCallback(
                    "resources_log.out"));
        } catch (IOException e) {
            MSALogging.getBaseLogger().warn(
                    "Exception caught in method MSARunningTest.MSARunningTest", e);
        }
        // ----------------------------------------------------------------------

    }

    /**
     * @return a string describing the client sampled demands
     */
    public String getDemands() {
        StringBuffer s = new StringBuffer(mDemands.size() * 5);

        List<Entry<VRPActualRequest, Double>> dems = new ArrayList<Entry<VRPActualRequest, Double>>(
                mDemands.entrySet());

        Collections.sort(dems, new Comparator<Entry<VRPActualRequest, Double>>() {
            @Override
            public int compare(Entry<VRPActualRequest, Double> o1,
                    Entry<VRPActualRequest, Double> o2) {
                return o1.getKey().getID() - o2.getKey().getID();
            }
        });

        for (Entry<VRPActualRequest, Double> e : dems) {
            s.append(String.format("[%s:%s] ", e.getKey().getID(), e.getValue()));
        }
        return s.toString();
    }

    /**
     * Getter for the MSA instance, requests do not contain the realization of the demand.
     * 
     * @return the instance being used by the MSA procedure
     */
    public MSAVRPInstance getInstance() {
        return mInstance;
    }

    /**
     * Getter for the {@link MSABase} instance
     * 
     * @return the msa
     */
    public MSABase<VRPScenario, MSAVRPInstance> getMsa() {
        return mMSA;
    }

    /**
     * Getter for the simulation instance, which contains the realization of the demands.
     * 
     * @return an instance with the demand realizations
     */
    public DynamicInstance getSimulationInstance() {
        return mSimInstance;
    }

    public double getTravelInvertSpeed() {
        return mTravelInvertSpeed;
    }

    /**
     * Getter for <code>timeLimit</code>
     * 
     * @return the timeLimit
     */
    public int getTimeLimit() {
        return mTimeLimit;
    }

    /**
     * Setter for time limit
     * 
     * @param timeLimit
     *            the time limit to set in minutes
     */
    public void setTimeLimit(int timeLimit) {
        mTimeLimit = timeLimit;
    }

    /**
     * Getter for <code>returnStatus</code>
     * 
     * @return the returnStatus
     */
    public ReturnStatus getStatus() {
        return mReturnStatus;
    }

    /**
     * Return status string
     * 
     * @return a string describing the return status string
     */
    public String getStatusString() {
        return String.format("%s[%s]", getStatus(),
                mMsaThread == null ? "null" : mMsaThread.getMessage());
    }

    /**
     * Log the state of the MSA
     */
    private void printState() {
        info("Final state of the MSA procedure: %s", mMSA);

        info("");
        info("----------------------------------------");
        info("Served requests:");
        for (IActualRequest r : mInstance.getServedRequests()) {
            info("  %s", r);
        }
        info("----------------------------------------");
        info("Pending requests:");
        for (IActualRequest r : mInstance.getPendingRequests()) {
            info("  %s", r);
        }
        info("----------------------------------------");
        info("");
        info("----------------------------------------");
        info("Solution:");
        info(mMSA.getCurrentSolution().toString());
        info("----------------------------------------");
    }

    /**
     * Start the MSA procedure
     */
    @Override
    public void run() {
        mReturnStatus = ReturnStatus.RUNNING;

        Thread simThread = new Thread(mSimulation, "SimThread");
        simThread.setDaemon(true);
        simThread.start();

        Stopwatch timer = new Stopwatch(getTimeLimit() * 60000l);
        timer.start();

        info("");
        info("Starting the MSA");
        mMsaThread = sThreadFactory.newThread(mMSA);
        mMsaThread.start();

        info("");

        info("Waiting for the MSA to initialize");
        while (!mMSA.isInitialized()) {
            mMSA.acquireLock();
            try {
                mMSA.getInitializedCondition().await();
            } catch (InterruptedException e) {
                mReturnStatus = ReturnStatus.EXCEPTION;
                exception("NovoaRun.run while waiting for initialization", e);
                mMSA.stop();
                break;
            }
            mMSA.releaseLock();
        }

        info("Raising resource start event");
        mMSA.getEventFactory().raiseResourceStart(0, mInstance.getDepotsVisits().iterator().next());

        waitSec(1);

        info("Raising decision event");
        mMSA.getEventFactory().raiseDecisionEvent();
        // The rest of the simulation will be done in the
        // SimulationCallback

        info("Waiting for the MSA to finish");

        while (mMSA.isRunning() && mMsaThread.getStatus() == ReturnStatus.RUNNING) {
            try {
                mMsaThread.mLock.lock();
                mMsaThread.getStatusCondition().await(5, TimeUnit.SECONDS);
                mMsaThread.mLock.unlock();

                if (timer.hasTimedOut()) {
                    error("NovoaRun.run Time limit reached: %s", timer);
                    mMSA.stop();
                    mReturnStatus = ReturnStatus.TIME_LIMIT;
                    break;
                }
            } catch (InterruptedException e) {
                LOGGER.exception("NovoaRun.run", e);
                mReturnStatus = ReturnStatus.EXCEPTION;
                stop();
                break;
            }
        }

        if (mReturnStatus == ReturnStatus.RUNNING) {
            mReturnStatus = mMsaThread.getStatus();
        }

        // while (mMSA.isRunning()) {
        // mMSA.acquireLock();
        // try {
        // mMSA.getRunningCondition().await(1, TimeUnit.SECONDS);
        // } catch (InterruptedException e) {
        // exception("NovoaRun.run while waiting for initialization", e);
        // mReturnStatus = ReturnStatus.EXCEPTION;
        // stop();
        // break;
        // }
        // mMSA.releaseLock();
        // if (timer.hasTimedOut()) {
        // error("NovoaRun.run Time limit reached: %s", timer);
        // mMSA.stop();
        // mReturnStatus = ReturnStatus.TIME_LIMIT;
        // if (mExceptionSafe) {
        // mMSAExecutor.shutdownNow();
        // } else {
        // // Nothing can be done
        // }
        // break;
        // }
        // }

        printState();
        mSimulation.stop();
    }

    public void setTravelInvertSpeed(double speed) {
        mTravelInvertSpeed = speed;
    }

    /**
     * Stop the MSA procedure
     */
    private void stop() {
        mMSA.stop();
        mSimulation.stop();
    }
}
