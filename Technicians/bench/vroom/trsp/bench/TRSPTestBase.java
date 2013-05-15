package vroom.trsp.bench;

import static vroom.trsp.util.TRSPGlobalParameters.ALNS_COST_DELEGATE;
import static vroom.trsp.util.TRSPGlobalParameters.INIT_COST_DELEGATE;
import static vroom.trsp.util.TRSPGlobalParameters.SC_COST_DELEGATE;
import gurobi.GRB.DoubleParam;
import gurobi.GRBException;

import java.io.File;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import vroom.common.heuristics.LocalSearchBase;
import vroom.common.utilities.BestKnownSolutions;
import vroom.common.modeling.visualization.VisualizationFrame;
import vroom.common.utilities.Utilities.Math.DeviationMeasure;
import vroom.common.utilities.gurobi.GRBEnvProvider;
import vroom.common.utilities.logging.Logging;
import vroom.trsp.ALNSSCSolver;
import vroom.trsp.datamodel.CVRPTWSolutionChecker;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPRequest;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.datamodel.TRSPSolutionChecker;
import vroom.trsp.datamodel.TRSPSolutionCheckerBase;
import vroom.trsp.datamodel.TRSPTour;
import vroom.trsp.datamodel.costDelegates.TRSPCostDelegate;
import vroom.trsp.datamodel.costDelegates.TRSPDistance;
import vroom.trsp.datamodel.costDelegates.TRSPTourBalance;
import vroom.trsp.datamodel.costDelegates.TRSPWorkingTime;
import vroom.trsp.optimization.TRSPUtilities;
import vroom.trsp.optimization.constructive.TRSPpInsertion;
import vroom.trsp.optimization.localSearch.TRSPShift;
import vroom.trsp.util.TRSPGlobalParameters;
import vroom.trsp.util.TRSPLogging;
import vroom.trsp.visualization.TRSPInstanceGraph;
import vroom.trsp.visualization.TRSPVisualization;

public abstract class TRSPTestBase implements Runnable {

    private final TRSPGlobalParameters    mParams;

    private final String                  mInstanceFile;

    private final TRSPInstance            mInstance;

    private final boolean                 mCVRPTW;
    private final TRSPSolutionCheckerBase mSolChecker;

    private final boolean                 mShowVisualization;

    private final boolean                 mFileLogging;

    private final boolean                 mCheckAfterMoves;

    private final BestKnownSolutions      mBKS;

    /**
     * Getter for <code>params</code>
     * 
     * @return the params
     */
    public TRSPGlobalParameters getParams() {
        return mParams;
    }

    /**
     * Getter for <code>instanceFile</code>
     * 
     * @return the instanceFile
     */
    public String getInstanceFile() {
        return mInstanceFile;
    }

    /**
     * Getter for the {@code  instance}
     * 
     * @return the instance
     */
    public TRSPInstance getInstance() {
        return mInstance;
    }

    /**
     * Getter for the best known solution instance
     * 
     * @return the best known solution instance
     */
    public BestKnownSolutions getBKS() {
        return mBKS;
    }

    /**
     * Getter for <code>cVRPTW</code>
     * 
     * @return the cVRPTW
     */
    public boolean isCVRPTW() {
        return mCVRPTW;
    }

    /**
     * Getter for <code>solChecker</code>
     * 
     * @return the solChecker
     */
    public TRSPSolutionCheckerBase getSolChecker() {
        return mSolChecker;
    }

    /**
     * Getter for <code>showVisualization</code>
     * 
     * @return the showVisualization
     */
    public boolean isShowVisualization() {
        return mShowVisualization;
    }

    /**
     * Getter for <code>fileLogging</code>
     * 
     * @return the fileLogging
     */
    public boolean isFileLoggingEnabled() {
        return mFileLogging;
    }

    /**
     * Getter for <code>checkAfterMoves</code>
     * 
     * @return the checkAfterMoves
     */
    public boolean isCheckAfterMoves() {
        return mCheckAfterMoves;
    }

    /**
     * Creates a new <code>TRSPTestBase</code>
     * 
     * @param instanceFile
     * @param showVisualization
     * @param fileLogging
     * @param checkAfterMoves
     */
    public TRSPTestBase(String instanceFile, boolean showVisualization, boolean fileLogging,
            boolean checkAfterMoves) {
        mInstanceFile = instanceFile;
        mShowVisualization = showVisualization;
        mFileLogging = fileLogging;
        mCheckAfterMoves = checkAfterMoves;

        mCVRPTW = mInstanceFile.contains("cvrptw") || mInstanceFile.contains("cvrp");
        mSolChecker = mCVRPTW ? CVRPTWSolutionChecker.INSTANCE : TRSPSolutionChecker.INSTANCE;
        mParams = new TRSPGlobalParameters();

        mInstance = TRSPUtilities.readInstance(instanceFile, mParams.isCVRPTW());
        mBKS = TRSPUtilities.getBKS(getInstanceFile());

        setupGlobalParameters();

        TRSPpInsertion.setCheckSolutionAfterMove(mCheckAfterMoves);
        TRSPShift.setCheckSolutionAfterMove(mCheckAfterMoves);
        LocalSearchBase.setCheckSolutionAfterMove(mCheckAfterMoves);
    }

    public void setupGlobalParameters() {
        try {
            // DEFAULT_PARAMS.loadParameters(new
            // File("config/bench/trsp_test.cfg"));
            // DEFAULT_PARAMS.loadParameters(new File("config/bench/bench_trsp_rchsc_twcheck_25sub.cfg"));
            mParams.loadParameters(new File("config/bench/trsp_test.cfg"));
        } catch (Exception e) {
            TRSPLogging.getBaseLogger().fatalException("TRSPTestBase.main", e);
            Logging.awaitLogging(60000);
            System.exit(1);
        }

        mParams.set(TRSPGlobalParameters.RUN_CVRPTW, mCVRPTW);
        mParams.set(TRSPGlobalParameters.SC_MAX_TIME, 300d);
        mParams.set(TRSPGlobalParameters.RCH_MAX_IT, 5000);
        mParams.set(TRSPGlobalParameters.RCH_GIANT_SPLIT, true);

        // sCVRPTW Setup
        if (mParams.isCVRPTW()) {
            mParams.set(TRSPGlobalParameters.RCH_MAX_IT, 25000);
            mParams.set(INIT_COST_DELEGATE, TRSPDistance.class);
            mParams.set(ALNS_COST_DELEGATE, TRSPDistance.class);
            mParams.set(SC_COST_DELEGATE, TRSPDistance.class);
            mParams.set(TRSPGlobalParameters.RCH_COST_DELEGATE, TRSPDistance.class);
        }

    }

    public void setupGurobiEnv() {
        try {
            GRBEnvProvider.getEnvironment().readParams(
                    mParams.get(TRSPGlobalParameters.SC_GRBENV_FILE));

            GRBEnvProvider.getEnvironment().set(DoubleParam.TimeLimit,
                    mParams.get(TRSPGlobalParameters.SC_MAX_TIME));
        } catch (GRBException e2) {
            TRSPLogging.getBaseLogger().exception("TRSPRun.main", e2);
        }
    }

    /**
     * Print information on the instance
     */
    public void checkInstance() {
        for (TRSPRequest r : getInstance().getRequests())
            if (getInstance().getCompatibleTechnicians(r.getID()).size() == 1)
                System.out.println(r.toString() + " -> "
                        + getInstance().getCompatibleTechnicians(r.getID()));
    }

    public void printParams() {
        TRSPLogging.getRunLogger().info(getParams().toString());
    }

    public void printSolution(final TRSPSolution solution, ALNSSCSolver run) {
        TRSPLogging.getBaseLogger().info(" Stored solution cost: " + solution.getObjectiveValue());
        String err = mSolChecker.checkSolution(solution);
        if (!err.isEmpty())
            TRSPLogging.getBaseLogger().warn(" Stored solution incoherencies: " + err);
        for (TRSPTour t : solution) {
            TRSPLogging.getBaseLogger().info("  %s", t);
            String infeas = run.getTourCtrHandler().getInfeasibilityExplanation(t);
            if (infeas != null)
                TRSPLogging.getBaseLogger().warn("  > Infeasibility: %s", infeas);
        }

        TRSPCostDelegate wtDelegate = new TRSPWorkingTime();
        TRSPDistance dDelegate = new TRSPDistance();
        TRSPTourBalance balanceDelegate = new TRSPTourBalance(wtDelegate, DeviationMeasure.StdDev);
        TRSPLogging.getBaseLogger().info(" Solution cost in terms of working time: %s",
                wtDelegate.evaluateSolution(solution, true, false));
        TRSPLogging.getBaseLogger().info(" Solution cost in terms of tour balancing (%s) : %s ",
                balanceDelegate, balanceDelegate.evaluateSolution(solution, true, false));
        TRSPLogging.getBaseLogger().info(" Solution cost in terms of distance (%s) : %s ",
                dDelegate, dDelegate.evaluateSolution(solution, true, false));
    }

    public void showFrame(final TRSPInstance instance, final TRSPSolution solution,
            final String title) {
        if (mShowVisualization) {

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    VisualizationFrame frame = TRSPVisualization.showVisualizationFrame(instance);
                    frame.setTitle(title);
                    ((TRSPInstanceGraph) frame.getInstanceViewer().getGraph())
                            .addSolution(solution);
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                }
            });
        }
    }

}
