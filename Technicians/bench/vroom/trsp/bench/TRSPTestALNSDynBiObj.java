package vroom.trsp.bench;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import vroom.common.heuristics.alns.ALNSLogger;
import vroom.common.heuristics.alns.ParallelALNS;
import vroom.common.heuristics.vns.VariableNeighborhoodSearch;
import vroom.common.heuristics.vns.VariableNeighborhoodSearch.VNSVariant;
import vroom.common.utilities.FileBufferedWriter;
import vroom.common.utilities.Stopwatch;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.common.utilities.logging.Logging;
import vroom.common.utilities.optimization.OptimizationSense;
import vroom.trsp.ALNSSCSolver;
import vroom.trsp.DynBiObjSolver;
import vroom.trsp.datamodel.ITRSPTour;
import vroom.trsp.datamodel.TRSPDetailedSolutionChecker;
import vroom.trsp.datamodel.TRSPRequest;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.datamodel.TRSPTour;
import vroom.trsp.datamodel.costDelegates.TRSPLevenshteinDistance;
import vroom.trsp.io.DynamicTRSPPersistenceHelper;
import vroom.trsp.optimization.alns.RepairRegret;
import vroom.trsp.optimization.alns.TRSPBiObjALNSLogger;
import vroom.trsp.optimization.alns.TRSPDestroyResult;
import vroom.trsp.optimization.biobj.ALNSParetoFront;
import vroom.trsp.optimization.biobj.LevenshteinPR;
import vroom.trsp.optimization.biobj.ParetoFront;
import vroom.trsp.optimization.biobj.ParetoFront.ParetoSolution;
import vroom.trsp.optimization.localSearch.TRSPShift;
import vroom.trsp.optimization.localSearch.TRSPTwoOpt;
import vroom.trsp.util.TRSPGlobalParameters;
import vroom.trsp.util.TRSPLogging;

public class TRSPTestALNSDynBiObj extends TRSPTestALNSSC {

    // private static String sInstance = "../Instances/trsp/pillac/crew25/C101.100_25-5-5-5.txt";
    // private static String sRDFile = "../Instances/trsp/pillac/crew25/dyn/C101.100_25-5-5-5_rd_10.txt";
    // private static String sInstance = "../Instances/cvrptw/solomon/RC101.txt";
    // private static String sRDFile = "../Instances/dvrptw/lackner_rd/RC101_rd_10.txt";
    private static String                         sInstance = "../Instances/cvrptw/solomon/R101.txt";
    private static String                         sRDFile   = "../Instances/dvrptw/lackner_rd/R101_rd_90.txt";

    private VariableNeighborhoodSearch<ITRSPTour> mVNS;
    private LevenshteinPR                         mPR;
    private FileBufferedWriter                    mWriter;
    private HashSet<ParetoSolution>               mPRSolutions;
    private TRSPSolution                          mRefSolution;

    private boolean                               mFirstPhase;

    @Override
    public void setupGlobalParameters() {
        super.setupGlobalParameters();

        getParams().set(TRSPGlobalParameters.RUN_BIOBJ, true);
        getParams().set(TRSPGlobalParameters.RUN_REL_DATE_FOLDER, "../Instances/dvrptw/lackner_rd");

        getParams().set(TRSPGlobalParameters.SC_ENABLED, false);
        getParams().set(TRSPGlobalParameters.ALNS_MAX_IT, 5000);
        getParams().set(TRSPGlobalParameters.THREAD_COUNT, 1);

        getParams().set(TRSPGlobalParameters.RUN_SOLVER, DynBiObjSolver.class);
    }

    public TRSPTestALNSDynBiObj(String instanceFile, String rdFile, boolean showVisualization,
            boolean fileLogging, boolean checkAfterMoves) {
        super(instanceFile, showVisualization, fileLogging, checkAfterMoves);
        try {
            DynamicTRSPPersistenceHelper.readRelDates(getInstance(), new File(rdFile), isCVRPTW());
        } catch (IOException e) {
            TRSPLogging.getBaseLogger().fatal("TRSPTestALNSDynBiObj.TRSPTestALNSSCDyna", e);
        }
        try {
            mWriter = new FileBufferedWriter("./bi_obj.csv");
            mWriter.writeLine("total_dist;pareto_alns;pareto_pr;pareto_final;solutions_pr;solutions_prls;unserved");
            mWriter.flush();
        } catch (IOException e) {
            TRSPLogging.getBaseLogger().exception("TRSPTestALNSDynBiObj.TRSPTestALNSDynBiObj", e);
        }

        getInstance().setupSimulator(getSolver().getSolCostDelegate(), getParams());
    }

    @Override
    void setupSolver() {
        if (mFirstPhase)
            setSolver(new ALNSSCSolver(getInstance(), getParams()));
        else
            setSolver(new DynBiObjSolver(getInstance(), getParams()));
        setupLS();
        setupPR();
    }

    @SuppressWarnings("unchecked")
    void setupLS() {
        mVNS = VariableNeighborhoodSearch.<ITRSPTour> newVNS(VNSVariant.VND,
                OptimizationSense.MINIMIZATION, null, getParams().getALNSRndStream(),//
                new TRSPShift(getSolver().getTourCtrHandler()),//
                new TRSPTwoOpt(getSolver().getTourCtrHandler())//
                );
    }

    void setupPR() {
        mPR = new LevenshteinPR(getParams());
    }

    @Override
    ALNSLogger<TRSPSolution> newFileLogger() {
        TRSPSolution solution = getSolver().getInitSol();
        return isFileLoggingEnabled() ? //
        new TRSPBiObjALNSLogger("results/alns", getParams().newALNSCostDelegate(solution),
                new TRSPLevenshteinDistance(solution)) //
                : null;
    }

    void firstPhase() {
        mFirstPhase = true;
        setupSolver();

        // Move to an intermediate state
        int threshold = Math.max(getInstance().getSimulator().getUnreleasedCount() / 2, 1);
        while (getInstance().getSimulator().getUnreleasedCount() > threshold) {
            getInstance().getSimulator().nextRelease();
        }

        TRSPLogging.getRunLogger().info(" Released requests   : %s/%s",
                getInstance().getReleasedRequests().size(), getInstance().getRequestCount());
        TRSPLogging.getRunLogger().info(" --- FIRST PHASE");
        // Find an initial solution
        init();
        mRefSolution = getSolver().getInitSol();
        alns();
        mFirstPhase = false;
    }

    @Override
    void setupALNS() {
        if (mFirstPhase) {
            // We want a relatively poor solution to have various non-dominated solutions in the second phase
            getParams().set(TRSPGlobalParameters.ALNS_MAX_IT, 10);
            super.setupALNS();
        } else {
            getParams().set(TRSPGlobalParameters.ALNS_MAX_IT, 25000);
            super.setupALNS();
            ((ParallelALNS<TRSPSolution>) getSolver().getALNS()).setSolPool(new ALNSParetoFront(
                    getSolver().getALNSCostDelegate(mRefSolution), OptimizationSense.MINIMIZATION,
                    new TRSPLevenshteinDistance(mRefSolution), OptimizationSense.MINIMIZATION));
        }
    }

    @Override
    public void run() {
        Stopwatch timer = new Stopwatch();
        timer.start();

        TRSPLogging.getRunLogger().info("TRSP TEST STARTED WITH INSTANCE %s", getInstance());
        TRSPLogging.getRunLogger().info("==========================================");
        TRSPLogging.getRunLogger().info(" Instance            : %s", getInstance().getName());

        firstPhase();
        TRSPLogging.getRunLogger().info(" --- SECOND PHASE");

        mRefSolution = getSolver().getALNSSol().clone();
        mRefSolution.getCostDelegate().evaluateSolution(mRefSolution, true, true);

        TRSPSolution initSolution = getSolver().getALNSSol().clone();

        // Update the state
        getInstance().getSimulator().updateState(initSolution);

        // Next release
        Collection<TRSPRequest> release = getInstance().getSimulator().nextRelease();
        HashSet<Integer> relIds = new HashSet<Integer>();
        // Insert the released requests
        RepairRegret bi = new RepairRegret(getParams(), getSolver().getTourCtrHandler(), 1, false);
        for (TRSPRequest r : release) {
            relIds.add(r.getID());
            initSolution.markAsUnserved(r.getID());
        }
        bi.repair(initSolution, new TRSPDestroyResult(relIds), null);
        initSolution.getCostDelegate().evaluateSolution(initSolution, true, true);

        // Set the initial solution of our solver
        getSolver().setInitSol(initSolution);
        TRSPLogging.getRunLogger().info("  Newly released     : %s", relIds);
        TRSPLogging.getRunLogger().info(" ALNS Start solution : %s", initSolution);
        TRSPLogging.getRunLogger().info(" Reference solution  : %s", mRefSolution);
        TRSPLogging.getRunLogger().info(" Levenshtein Distance: %s",
                TRSPLevenshteinDistance.evaluateLevenshteinDistance(mRefSolution, initSolution));

        // Run the ALNS again
        alns();
        TRSPLogging.getRunLogger().info(" ALNS Solution pool  : %s",
                ((ParallelALNS<?>) getSolver().getALNS()).getSolPool());
        TRSPLogging.getRunLogger().info(" Final solution      : %s", getSolver().getALNSSol());
        TRSPLogging.getRunLogger().info(
                " Levenshtein Distance: %s",
                TRSPLevenshteinDistance.evaluateLevenshteinDistance(mRefSolution, getSolver()
                        .getALNSSol()));
        // Write the pareto front
        for (ParetoSolution s : getPareto().getSolutionsFirstObj())
            writeSolution(s, 1);

        // Path relinking
        pr();
        // Write the pareto front after PR
        for (ParetoSolution s : getPareto().getSolutionsFirstObj())
            writeSolution(s, 2);

        ls();

        printStats();

        // Write the finak pareto front solutions (after PR+LS)
        for (ParetoSolution s : getPareto().getSolutionsFirstObj())
            writeSolution(s, 3);

        try {
            mWriter.flush();
            mWriter.close();
        } catch (IOException e) {
            TRSPLogging.getBaseLogger().exception("TRSPTestALNSDynBiObj.run", e);
        }
    }

    void pr() {
        ParetoFront pareto = (ParetoFront) ((ParallelALNS<TRSPSolution>) getSolver().getALNS())
                .getSolPool();

        mPRSolutions = new HashSet<ParetoFront.ParetoSolution>();
        TRSPLogging.getRunLogger().info(" Path relinking (%s solutions)", pareto.size());
        List<TRSPSolution> solutions = pareto.getSolutions();
        if (solutions.size() > 1) {
            List<TRSPSolution> path;
            for (TRSPSolution s : pareto)
                for (TRSPSolution t : pareto)
                    if (s != t) {
                        TRSPLogging.getRunLogger().info(" PR %s->%s", s.hashCode(), t.hashCode());
                        path = mPR.pathRelinking(s, t, null);

                        for (TRSPSolution sol : path) {
                            ParetoSolution pPRSol = pareto.wrapSolution(sol);
                            String err = getSolChecker().checkSolution(sol);
                            if (!err.isEmpty())
                                TRSPLogging.getRunLogger().info("  -Infeasible   : %10s(%s) %s",
                                        sol.hashCode(), err, pPRSol);
                            else {
                                mPRSolutions.add(pPRSol);
                                writeSolution(pPRSol, 4);
                                if (pareto.add(sol, false)) {
                                    TRSPLogging.getRunLogger().info("  +Non-dominated: %10s %s",
                                            sol.hashCode(), pPRSol);
                                } else {
                                    TRSPLogging.getRunLogger().info("   Dominated    : %10s %s",
                                            sol.hashCode(), pPRSol);
                                }
                            }
                        }
                        if (path.get(path.size() - 1).hashCode() != t.hashCode()) {
                            TRSPLogging.getRunLogger().warn("  Did not found the target solution");
                            TRSPLogging.getRunLogger().warn("  t :" + t.toShortString());
                            TRSPLogging.getRunLogger().warn(
                                    "  pr:" + path.get(path.size() - 1).toShortString());
                        }
                    }
        } else {
            TRSPLogging.getRunLogger().info(" Less than two solutions available for PR");
        }
    }

    TRSPSolution ls(TRSPSolution s) {
        TRSPSolution sol = s.clone();
        for (TRSPTour t : sol) {
            sol.importTour((TRSPTour) mVNS.localSearch(getInstance(), t, null));
        }
        return sol;
    }

    void ls() {
        TRSPLogging.getRunLogger().info(" Local search on all PR solutions (%s solutions)",
                mPRSolutions.size());

        for (ParetoSolution s : mPRSolutions) {
            TRSPSolution sol = ls(s.getSolution());
            String err = getSolChecker().checkSolution(sol);
            ParetoSolution pLSSol = getPareto().wrapSolution(sol);
            if (!err.isEmpty())
                TRSPLogging.getRunLogger().info("  -Infeasible   : %10s(%s) %s", sol.hashCode(),
                        err, pLSSol);
            else {
                writeSolution(pLSSol, 5);
                if (getPareto().add(sol, false))
                    TRSPLogging.getRunLogger().info("  +Non-dominated: %10s %s", sol.hashCode(),
                            pLSSol);
                else
                    TRSPLogging.getRunLogger().info("   Dominated    : %10s %s", sol.hashCode(),
                            pLSSol);
            }
        }
    }

    ParetoFront getPareto() {
        return ((ParetoFront) ((ParallelALNS<TRSPSolution>) getSolver().getALNS()).getSolPool());
    }

    void writeSolution(ParetoSolution s, int pos) {
        StringBuilder statString = new StringBuilder();
        statString.append(s.getFirstObjValue());
        for (int i = 0; i < pos; i++)
            statString.append(";");
        statString.append(s.getSecondObjValue());
        for (int i = pos; i < 6; i++)
            statString.append(";");
        statString.append(s.getSolution().getUnservedCount());

        try {
            mWriter.writeLine(statString.toString());
            mWriter.flush();
        } catch (IOException e) {
            TRSPLogging.getBaseLogger().exception("TRSPTestALNSDynBiObj.writeSolution", e);
        }
    }

    @Override
    void printStats() {
        super.printStats();
        TRSPLogging.getRunLogger().info(" Check solution: %s",
                TRSPDetailedSolutionChecker.INSTANCE.checkSolution(getSolver().getFinalSolution()));
    }

    public static void main(String[] args) {
        Logging.setupRootLogger(LoggerHelper.LEVEL_WARN, LoggerHelper.LEVEL_DEBUG, true);
        TRSPLogging.getRunLogger().setLevel(LoggerHelper.LEVEL_INFO);
        // AdaptiveLargeNeighborhoodSearch.getLogger().setLevel(LoggerHelper.LEVEL_DEBUG);

        TRSPTestALNSSC test = new TRSPTestALNSDynBiObj(sInstance, sRDFile, false, true, false);

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

}
