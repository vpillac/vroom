/**
 *
 */
package vroom.trsp.bench;

import static vroom.trsp.util.TRSPGlobalParameters.ALNS_COST_DELEGATE;
import static vroom.trsp.util.TRSPGlobalParameters.BALANCE_COST_DELEGATE_MEASURE;
import static vroom.trsp.util.TRSPGlobalParameters.INIT_COST_DELEGATE;

import java.io.File;

import vroom.common.heuristics.NeighborhoodBase;
import vroom.common.heuristics.alns.ALNSLogger;
import vroom.common.heuristics.alns.AdaptiveLargeNeighborhoodSearch;
import vroom.common.heuristics.alns.ParallelALNS;
import vroom.common.heuristics.alns.SimpleSolutionPool;
import vroom.common.utilities.Stopwatch;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.common.utilities.logging.Logging;
import vroom.common.utilities.optimization.OptimizationSense;
import vroom.trsp.ALNSSCSolver;
import vroom.trsp.datamodel.TRSPDetailedSolutionChecker;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.datamodel.costDelegates.TRSPDistance;
import vroom.trsp.datamodel.costDelegates.TRSPWorkingTime;
import vroom.trsp.optimization.TRSPUtilities;
import vroom.trsp.optimization.alns.TRSPBiObjALNSLogger;
import vroom.trsp.util.TRSPGlobalParameters;
import vroom.trsp.util.TRSPLogging;

/**
 * <code>TRSPTestALNSSC</code>
 * <p>
 * Creation date: Mar 23, 2011 - 5:19:37 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TRSPTestALNSSC extends TRSPTestBase {

    private static String sInstance = "../Instances/cvrptw/solomon/C101.txt";

    ALNSSCSolver          mSolver;

    /**
     * Creates a new <code>TRSPTestALNSSC</code>
     * 
     * @param instanceFile
     * @param showVisualization
     * @param fileLogging
     * @param checkAfterMoves
     */
    public TRSPTestALNSSC(String instanceFile, boolean showVisualization, boolean fileLogging,
            boolean checkAfterMoves) {
        super(instanceFile, showVisualization, fileLogging, checkAfterMoves);
    }

    @Override
    public void setupGlobalParameters() {
        super.setupGlobalParameters();
        try {
            getParams().loadParameters(new File("config/bench/trsp_test.cfg"));
        } catch (Exception e) {
            e.printStackTrace();
            Logging.awaitLogging(60000);
            System.exit(1);
        }

        getParams().set(TRSPGlobalParameters.SC_ENABLED, false);
        getParams().set(TRSPGlobalParameters.RUN_CVRPTW, isCVRPTW());
        getParams().set(TRSPGlobalParameters.THREAD_COUNT, 4);
        getParams().set(TRSPGlobalParameters.ALNS_PARALLEL, Boolean.TRUE);
        getParams().set(TRSPGlobalParameters.ALNS_MAX_IT, 25000);
        // sCVRPTW Setup
        if (getParams().isCVRPTW()) {
            getParams().set(INIT_COST_DELEGATE, TRSPDistance.class);
            getParams().set(ALNS_COST_DELEGATE, TRSPDistance.class);
        } else {
            // getParams().set(INIT_COST_DELEGATE, TRSPDistance.class);
            getParams().set(INIT_COST_DELEGATE, TRSPWorkingTime.class);
            // getParams().set(ALNS_COST_DELEGATE, TRSPTourBalance.class);
            getParams().set(ALNS_COST_DELEGATE, TRSPWorkingTime.class);

        }
    }

    void setupSolver() {
        setSolver((ALNSSCSolver) getParams().newInstance(TRSPGlobalParameters.RUN_SOLVER,
                getInstance(), getParams()));
    }

    void init() {
        TRSPLogging.getBaseLogger().info("Running the Initialization");
        getSolver().initialization();
    }

    ALNSLogger<TRSPSolution> newFileLogger() {
        return isFileLoggingEnabled() ? new TRSPBiObjALNSLogger("results/alns", getParams().get(
                BALANCE_COST_DELEGATE_MEASURE)) : null;
    }

    void setupALNS() {
        getSolver().setALNSLogger(newFileLogger());

        getSolver().setupALNS();
        TRSPLogging.getBaseLogger().info("Running the ALNS");
        getSolver().getALNS().setSolutionChecker(new TRSPDetailedSolutionChecker(false));
        ((ParallelALNS<TRSPSolution>) getSolver().getALNS())
                .setSolPool(new SimpleSolutionPool<TRSPSolution>(OptimizationSense.MINIMIZATION,
                        getSolver().getALNSGlobalParams()));
    }

    void alns() {
        setupALNS();

        getSolver().alns();
    }

    void sc() {
        TRSPLogging.getBaseLogger().info("Pool size: %s", getSolver().getTourPool().size());
        TRSPLogging.getBaseLogger().info("Running a post optimization");

        getSolver().setupPostOp();
        getSolver().postOp();
    }

    void printStats() {
        Double bksD = TRSPUtilities.getBKS(getInstanceFile()).getBKS(getInstance().getName());
        double bks = bksD != null ? bksD : Double.NaN;
        double gap = bksD != null ? (getSolver().getFinalSolution().getObjectiveValue() - bks)
                * 100 / bks : Double.NaN;
        TRSPLogging.getBaseLogger().info(
                "FINISHED: Init=%s (%s) ALNS=%s (%s) Post Opt=%s (%s) BKS=%s Gap=%.2f%%",
                getSolver().getInitSol().getObjectiveValue(),
                getSolver().getInitSol().getUnservedCount(), //
                getSolver().getALNSSol().getObjectiveValue(),
                getSolver().getALNSSol().getUnservedCount(), //
                getSolver().getFinalSolution().getObjectiveValue(),
                getSolver().getFinalSolution().getUnservedCount(), bks, gap);
    }

    @Override
    public void run() {
        Stopwatch timer = new Stopwatch();
        timer.start();

        TRSPLogging.getBaseLogger().info("TRSP TEST STARTED WITH INSTANCE %s", getInstance());
        TRSPLogging.getBaseLogger().info("==========================================");

        setupSolver();

        // Initialization
        // -------------------------------------
        init();
        // Display instance and solution
        printSolution(getSolver().getInitSol(), getSolver());
        showFrame(getInstance(), getSolver().getInitSol(), String.format("Initial solution (%.2f)",
                getSolver().getInitSol().getObjectiveValue()));

        // ALNS
        // -------------------------------------
        alns();
        // Display instance and solution
        printSolution(getSolver().getALNSSol(), getSolver());
        showFrame(getInstance(), getSolver().getALNSSol(),
                String.format("ALNS solution (%.2f)", getSolver().getALNSSol().getObjectiveValue()));

        timer.stop();

        // Postop
        // -------------------------------------
        // sc();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            TRSPLogging.getBaseLogger().exception("TRSPTestALNSSC.main", e);
        }

        printStats();
    }

    public static void main(String[] args) {
        Logging.setupRootLogger(LoggerHelper.LEVEL_WARN, LoggerHelper.LEVEL_DEBUG, true);
        TRSPLogging.getRunLogger().setLevel(LoggerHelper.LEVEL_INFO);
        AdaptiveLargeNeighborhoodSearch.getLogger().setLevel(LoggerHelper.LEVEL_DEBUG);

        NeighborhoodBase.setCheckSolutionAfterMove(true);

        TRSPTestALNSSC test = new TRSPTestALNSSC(sInstance, false, false, false);

        test.printParams();

        test.run();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            TRSPLogging.getBaseLogger().exception("TRSPTestALNSSC.main", e);
        }

        Logging.awaitLogging(60000);
        System.exit(0);
    }

    /**
     * Getter for <code>solver</code>
     * 
     * @return the solver
     */
    protected ALNSSCSolver getSolver() {
        return mSolver;
    }

    /**
     * Setter for <code>solver</code>
     * 
     * @param solver
     *            the solver to set
     */
    protected void setSolver(ALNSSCSolver solver) {
        mSolver = solver;
    }
}
