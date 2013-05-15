/**
 *
 */
package vroom.trsp.optimization.matheuristic;

import gurobi.GRB;
import gurobi.GRB.DoubleAttr;
import gurobi.GRB.IntAttr;
import gurobi.GRB.StringAttr;
import gurobi.GRBException;
import gurobi.GRBModel;
import gurobi.GRBVar;

import java.io.File;
import java.text.DecimalFormat;
import java.util.concurrent.Callable;

import vroom.common.utilities.IDisposable;
import vroom.common.utilities.StatCollector;
import vroom.common.utilities.StatCollector.Label;
import vroom.common.utilities.gurobi.GRBStatCollector;
import vroom.common.utilities.gurobi.GRBUtilities;
import vroom.common.utilities.lp.SolverStatus;
import vroom.trsp.util.TRSPLogging;

/**
 * <code>TRSPGRBStatCollector</code> contains instrumenting logic to monitor a {@link GRBModel gurobi model} for the
 * TRSP
 * <p>
 * Creation date: Aug 25, 2011 - 4:48:45 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TRSPGRBStatCollector implements Callable<SolverStatus>, IDisposable {

    public static final double[]   TARGET_GAPS   = new double[] { 0.1, 0.05, 0.025, 0.01, 0.005, 0.0025, 0.001 };

    public static final long[]     TIME_SLICES   = new long[] { 10, 30, 60, 300, 600, 1800 };

    public static final int        TT_LAB_OFFSET = 10;
    public static final Label<?>[] TT_LABELS     = new Label<?>[TARGET_GAPS.length + TT_LAB_OFFSET + TIME_SLICES.length];
    static {
        TT_LABELS[0] = new Label<String>("Instance", String.class);
        TT_LABELS[1] = new Label<Boolean>("StartSol", Boolean.class);
        TT_LABELS[2] = new Label<SolverStatus>("Status", SolverStatus.class);
        TT_LABELS[3] = new Label<Integer>("Columns", Integer.class);
        TT_LABELS[4] = new Label<Integer>("RemColumns", Integer.class);
        TT_LABELS[5] = new Label<Integer>("Rows", Integer.class);
        TT_LABELS[6] = new Label<Double>("Start", Double.class);
        TT_LABELS[7] = new Label<Double>("Objective", Double.class);
        TT_LABELS[8] = new Label<Double>("ObjBound", Double.class);
        TT_LABELS[9] = new Label<Double>("Gap", Double.class);
        for (int i = 0; i < TARGET_GAPS.length; i++) {
            TT_LABELS[i + TT_LAB_OFFSET] = new Label<Long>(100 * TARGET_GAPS[i] + "%", Long.class);
        }
        for (int i = 0; i < TIME_SLICES.length; i++) {
            TT_LABELS[i + TT_LAB_OFFSET + TARGET_GAPS.length] = new Label<Double>(TIME_SLICES[i] + "s", Double.class,
                    new DecimalFormat("###0.000000"));
        }
    }

    private GRBModel               mModel;
    private GRBBenchCallback       mCallback;
    private SolverStatus           mStatus;
    private double                 mInitialObj;
    private boolean                mStartDefined;
    private boolean                mRunning;

    private final StatCollector    mTargetCollector;
    private final String           mDetailStatFile;
    private final String           mStatComment;

    /**
     * Creates a new <code>GurobiRun</code>
     * 
     * @param statComment
     *            a comment for the time to target statistics file
     * @param detStatFile
     *            the path of the detailed statistic file
     * @param ttStatFile
     *            the path of the time to target statistics file
     */
    public TRSPGRBStatCollector(String statComment, String detStatFile, String ttStatFile) {
        this(null, statComment, detStatFile, ttStatFile);
    }

    /**
     * Creates a new <code>GurobiRun</code>
     * 
     * @param model
     *            the model to be instrumented
     * @param statComment
     *            a comment for the time to target statistics file
     * @param detStatFile
     *            the path of the detailed statistic file
     * @param ttStatFile
     *            the path of the time to target statistics file
     */
    public TRSPGRBStatCollector(GRBModel model, String statComment, String detStatFile, String ttStatFile) {
        mTargetCollector = new StatCollector(new File(ttStatFile), true, true, statComment, TT_LABELS);

        mDetailStatFile = detStatFile;
        mStatComment = statComment;

        setModel(model);
    }

    /**
     * Sets the model and attach the corresponding callbacks to it
     * 
     * @param model
     *            the model to be monitored
     */
    public void setModel(GRBModel model) {
        mModel = model;
        mStatus = null;

        if (mModel == null)
            return;

        // Read the initial solution value
        double initObj = 0;
        boolean startDef = false;
        String name = "model";
        try {
            name = mModel.get(StringAttr.ModelName);
            GRBVar vars[] = mModel.getVars();
            double starts[] = mModel.get(DoubleAttr.Start, vars);
            double costs[] = mModel.get(DoubleAttr.Obj, vars);
            for (int i = 0; i < starts.length; i++) {
                if (starts[i] != GRB.UNDEFINED) {
                    startDef = false;
                    initObj += costs[i] * starts[i];
                }
            }

        } catch (GRBException e) {
            TRSPLogging.getRunLogger().exception("GurobiBenchmarking.main", e);
        }
        mInitialObj = initObj;
        mStartDefined = startDef;

        mCallback = new GRBBenchCallback(mDetailStatFile, name, mStatComment, mStartDefined, TARGET_GAPS, TIME_SLICES,
                mInitialObj);
        mModel.setCallback(mCallback);
    }

    @Override
    public SolverStatus call() throws Exception {
        if (mRunning)
            throw new IllegalStateException("Model is already beeing solved");
        mRunning = true;

        try {
            mModel.optimize();
            mRunning = false;
            collectStats();
        } catch (Exception e) {
            mRunning = false;
            throw e;
        }
        return mStatus;
    }

    /**
     * Collect and write the statistics once the model was optimized
     * 
     * @throws IllegalStateException
     *             if the optimization is not finished
     */
    public synchronized void collectStats() {
        if (mRunning)
            throw new IllegalStateException("Model optimization has not terminated");

        try {
            mStatus = GRBUtilities.convertStatus(mModel.get(IntAttr.Status));
        } catch (GRBException e1) {
            mStatus = SolverStatus.UNKNOWN_STATUS;
            TRSPLogging.getBaseLogger().exception("GurobiRun.collectStats", e1);
        }
        mCallback.solverStopped(mStatus);

        // Collect stats
        Object[] stats = new Object[TT_LABELS.length];
        try {
            stats[0] = mModel.get(StringAttr.ModelName);
            double obj = mModel.get(DoubleAttr.ObjVal);
            double bnd = mModel.get(DoubleAttr.ObjBound);
            stats[7] = obj;
            stats[8] = bnd;
            stats[9] = Math.abs((obj - bnd) / obj);
        } catch (GRBException e) {
            TRSPLogging.getBaseLogger().exception("GurobiRun.collectStats", e);
        }
        stats[1] = mStartDefined;
        stats[2] = mStatus;
        stats[3] = mModel.getVars().length;
        stats[4] = mCallback.getRemovedColumns();
        stats[5] = mModel.getConstrs().length;
        stats[6] = mInitialObj;
        for (int j = 0; j < TARGET_GAPS.length; j++) {
            stats[j + TT_LAB_OFFSET] = mCallback.getTimeToTarget(j);
        }
        for (int j = 0; j < TIME_SLICES.length; j++) {
            stats[j + TT_LAB_OFFSET + TARGET_GAPS.length] = mCallback.getImpAtTime(j);
        }
        // TRSPLogging.getRunLogger().info(" Collecting stats: %s", Utilities.toShortString(stats));

        mTargetCollector.collect(stats);

        if (mCallback.getRemovedColumns() < mModel.getVars().length
                && (mStatus == SolverStatus.OPTIMAL || mStatus == SolverStatus.INTERRUPTED || mStatus == SolverStatus.TIME_LIMIT)) {
            mCallback.flush();
        }
    }

    @Override
    public void dispose() {
        mTargetCollector.close();
        mCallback.closeCollector();
    }

    public static class GRBBenchCallback extends GRBStatCollector {
        private final String   mComment;
        private final boolean  mStart;

        private final double[] mGapTargets;
        private final long[]   mTimesToTarget;
        private int            mCurrentTarget;

        private final long[]   mTimeSlices;
        private final double[] mImpAtTime;
        private final double   mInitialSol;
        private int            mCurrentSlice;

        private int            mRemovedColumns;

        /**
         * Returns the number of columns removed by presolve
         * 
         * @return the number of columns removed by presolve
         */
        public int getRemovedColumns() {
            return mRemovedColumns;
        }

        /**
         * Creates a new <code>GRBBenchCallback</code>
         * 
         * @param detailStatFile
         * @param fileComment
         * @param runComment
         * @param start
         * @param targets
         *            the target gap values, in descending order
         */
        public GRBBenchCallback(String detailStatFile, String fileComment, String runComment, boolean start,
                double[] targets, long[] timeSlices, double initialSol) {
            super(detailStatFile, fileComment, true, false);
            mComment = runComment;
            mStart = start;

            mGapTargets = targets;
            mTimesToTarget = new long[mGapTargets.length];
            for (int i = 0; i < mTimesToTarget.length; i++) {
                mTimesToTarget[i] = -1;
            }

            mTimeSlices = timeSlices;
            mImpAtTime = new double[mTimeSlices.length];
            for (int i = 0; i < mImpAtTime.length; i++) {
                mImpAtTime[i] = -2;

            }
            mInitialSol = initialSol;

            mCurrentTarget = 0;
            mCurrentSlice = 0;
        }

        @Override
        protected void callback() {
            super.callback();
            switch (where) {
            case GRB.Callback.PRESOLVE:
                try {
                    mRemovedColumns = getIntInfo(GRB.Callback.PRE_COLDEL);
                } catch (GRBException e) {
                    TRSPLogging.getRunLogger().exception("GRBBenchCallback.callback", e);
                }
                break;
            case GRB.Callback.MIPNODE:
            case GRB.Callback.MIPSOL:
                long time = -1;
                try {
                    time = (long) getDoubleInfo(GRB.Callback.RUNTIME);
                } catch (GRBException e) {
                    TRSPLogging.getRunLogger().exception("GRBBenchCallback.callback", e);
                }
                while (mCurrentTarget < mTimesToTarget.length && getGap() < mGapTargets[mCurrentTarget]) {
                    mTimesToTarget[mCurrentTarget++] = time;
                    if (mCurrentTarget == mTimesToTarget.length)
                        // Abort the MIP search
                        abort();
                }
                while (mCurrentSlice < mTimeSlices.length && time >= mTimeSlices[mCurrentSlice]) {
                    mImpAtTime[mCurrentSlice++] = getCurrentImp();
                }
            }
        }

        /**
         * Returns the current improvement with respect to the initial solution
         * 
         * @return the current improvement with respect to the initial solution
         */
        public double getCurrentImp() {
            return (mInitialSol - getIncumbent()) / mInitialSol;
        }

        @Override
        protected Label<?>[] getAdditionalLabels() {
            return new Label<?>[] { new Label<String>("Instance", String.class),
                    new Label<Boolean>("StartSol", Boolean.class),

            };
        }

        @Override
        protected Object[] getAdditionalStatArray() throws GRBException {
            return new Object[] { mComment, mStart };
        }

        /**
         * Returns the time recorded to get to the specified target gap value
         * 
         * @param target
         *            the index of the target gap value
         * @return the time recorded to get to the specified target gap value
         */
        public long getTimeToTarget(int target) {
            return mTimesToTarget[target];
        }

        /**
         * Returns the improvement on initial solution recorded at a specific time
         * 
         * @param time
         *            the index of the target time
         * @return the improvement on initial solution recorded at a specific time
         */
        public double getImpAtTime(int time) {
            return mImpAtTime[time];
        }

        /**
         * Finalize the collection of statistics
         * 
         * @param status
         *            the status of the solver
         */
        public void solverStopped(SolverStatus status) {
            if (status == SolverStatus.OPTIMAL) {
                long finalTime = mTimesToTarget[0] >= 0 ? mTimesToTarget[0] : 0;
                for (int i = 0; i < mTimesToTarget.length; i++) {
                    if (mTimesToTarget[i] >= 0)
                        finalTime = mTimesToTarget[i];
                    else
                        mTimesToTarget[i] = finalTime;
                }
            }
            if (status == SolverStatus.OPTIMAL || status == SolverStatus.TIME_LIMIT
                    || status == SolverStatus.INTERRUPTED) {
                for (int i = 0; i < mImpAtTime.length; i++) {
                    if (mImpAtTime[i] < 0)
                        mImpAtTime[i] = getCurrentImp();
                }
            }
        }
    }

}
