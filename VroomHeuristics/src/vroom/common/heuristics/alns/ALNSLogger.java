/**
 *
 */
package vroom.common.heuristics.alns;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import vroom.common.utilities.StatCollector;
import vroom.common.utilities.StatCollector.Label;
import vroom.common.utilities.ToStringComparator;
import vroom.common.utilities.Utilities;
import vroom.common.utilities.callbacks.CallbackBase;
import vroom.common.utilities.callbacks.ICallbackEvent;
import vroom.common.utilities.optimization.IInstance;
import vroom.common.utilities.optimization.ISolution;

/**
 * <code>ALNSLogger</code> is a simple logger that can be attached to a an {@link AdaptiveLargeNeighborhoodSearch ALNS}
 * to write a simple log of the procedure in various csv files.
 * <p>
 * Creation date: May 27, 2011 - 4:16:30 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class ALNSLogger<S extends ISolution> extends
        CallbackBase<AdaptiveLargeNeighborhoodSearch<S>, ALNSEventType> {

    /** The default labels for the solution logger */
    protected static final Label<?>[] SOL_DEFAULT_LABELS  = new Label<?>[] { //
                                                          new Label<String>("time", String.class), //
            new Label<Long>("elapsed_time", Long.class), //
            new Label<Integer>("iteration", Integer.class),//
            new Label<String>("event", String.class),//
            new Label<Double>("best_sol", Double.class),//
            new Label<Double>("current_sol", Double.class),//
            new Label<Double>("tmp_sol", Double.class)   //
                                                          };

    /** The default labels for the component logger */
    protected static final Label<?>[] COMP_DEFAULT_LABELS = new Label<?>[] { //
                                                          new Label<String>("time", String.class), //
            new Label<Long>("elapsed_time", Long.class), //
            new Label<Integer>("iteration", Integer.class),//
            new Label<String>("event", String.class)     //
                                                          };

    private StatCollector             mSolCol;

    private StatCollector             mCompCol;
    private List<IDestroy<?>>         mDestroyComp;
    private List<IRepair<?>>          mRepairComp;

    private final String              mDestDir;

    private IInstance                 mInstance;

    private ISolution                 mSolution;

    /**
     * Creates a new <code>ALNSLogger</code>
     * 
     * @param destDir
     *            the destination dir for log files
     */
    public ALNSLogger(String destDir) {
        super();
        mDestDir = destDir;
    }

    /**
     * Register this logger to an ALNS
     * 
     * @param alns
     *            the ALNS which events will be logged
     */
    public void registerToALNS(AdaptiveLargeNeighborhoodSearch<S> alns) {
        alns.registerCallback(this, ALNSEventType.STARTED);
        alns.registerCallback(this, ALNSEventType.IT_FINISHED);
        alns.registerCallback(this, ALNSEventType.COMP_UPDATED);
        alns.registerCallback(this, ALNSEventType.FINISHED);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ICallbackEvent<AdaptiveLargeNeighborhoodSearch<S>, ALNSEventType> event) {
        ALNSCallbackEvent<S> e = (ALNSCallbackEvent<S>) event;

        S best, current, tmp;
        String time = String.format("%1$tk:%1$tM:%1$tS", new Date(e.getTimeStamp()));
        AdaptiveLargeNeighborhoodSearch<?> alns = e.getSource();
        switch (e.getType()) {
        case STARTED:
            mInstance = (IInstance) e.getParams()[0];
            mSolution = (S) e.getParams()[1];
            createCollectors(alns);
            collectSolStats(e, null, null, null);
            collectCompStats(e, null, null, null);
            break;
        case IT_STARTED:
            break;
        case DESTROYED:
            best = (S) e.getParams()[0];
            current = (S) e.getParams()[1];
            tmp = (S) e.getParams()[2];
            collectSolStats(e, best, current, tmp);
            break;
        case REPAIRED:
            best = (S) e.getParams()[0];
            current = (S) e.getParams()[1];
            tmp = (S) e.getParams()[2];
            collectSolStats(e, best, current, tmp);
            break;
        case SOL_NEW_BEST:
            best = (S) e.getParams()[0];
            collectSolStats(e, best, best, best);
            break;
        case SOL_NEW_CURRENT:
            best = (S) e.getParams()[0];
            current = (S) e.getParams()[1];
            collectSolStats(e, best, current, current);
            break;
        case SOL_REJECTED:
            best = (S) e.getParams()[0];
            current = (S) e.getParams()[1];
            tmp = (S) e.getParams()[2];
            collectSolStats(e, best, current, tmp);
            break;
        case IT_FINISHED:
            best = (S) e.getParams()[0];
            current = (S) e.getParams()[1];
            tmp = (S) e.getParams()[2];
            collectSolStats(e, best, current, tmp);
            break;
        case COMP_UPDATED:
            best = (S) e.getParams()[0];
            current = (S) e.getParams()[1];
            tmp = (S) e.getParams()[2];
            collectCompStats(e, best, current, tmp);
            break;
        case FINISHED:
            best = (S) e.getParams()[1];
            collectSolStats(e, best, best, best);
            collectCompStats(e, best, best, best);
            mSolCol.flush();
            mCompCol.flush();
            break;
        default:
            break;
        }
    }

    private void createCollectors(AdaptiveLargeNeighborhoodSearch<?> alns) {
        String fileName = String.format("%1$ty%1$tm%1$td_%1$tH-%1$tM_%2$s",
                new Date(System.currentTimeMillis()), mInstance.getName());

        // Solution logging
        // -----------------------------
        File solFile = new File(Utilities.getUnifiedOutputFilePath(mDestDir, mInstance.getName(), "sol",
                "csv"));
        Label<?>[] cust = getAdditionalSolLabels(alns);

        Label<?>[] labels = Arrays.copyOf(SOL_DEFAULT_LABELS, SOL_DEFAULT_LABELS.length
                + cust.length);
        for (int i = 0; i < labels.length - SOL_DEFAULT_LABELS.length && i < cust.length; i++)
            labels[SOL_DEFAULT_LABELS.length + i] = cust[i];

        mSolCol = new StatCollector(solFile, false, false, String.format("Instance: %s\nALNS:\n%s",
                fileName, alns),//
                labels);
        // -----------------------------

        // Components logging
        // -----------------------------
        File compFile = new File(Utilities.getUnifiedOutputFilePath(mDestDir, mInstance.getName(), "comp",
                "csv"));

        // Destroy components
        mDestroyComp = new ArrayList<IDestroy<?>>(alns.getDestroyComponents().getComponents());
        Collections.sort(mDestroyComp, ToStringComparator.INSTANCE);
        // Repair components
        mRepairComp = new ArrayList<IRepair<?>>(alns.getRepairComponents().getComponents());
        Collections.sort(mRepairComp, ToStringComparator.INSTANCE);

        cust = getAdditionalCompLabels(alns);
        labels = Arrays
                .copyOf(COMP_DEFAULT_LABELS, COMP_DEFAULT_LABELS.length + mDestroyComp.size()
                        + mRepairComp.size() + cust.length);
        // Copy the components name to labels
        int i = COMP_DEFAULT_LABELS.length;
        for (IDestroy<?> d : mDestroyComp)
            labels[i++] = new Label<Double>(d.getName() + "-w", Double.class, new DecimalFormat(
                    "###0.000000"));
        for (IRepair<?> d : mRepairComp)
            labels[i++] = new Label<Double>(d.getName() + "-w", Double.class, new DecimalFormat(
                    "###0.000000"));
        for (Label<?> l : cust)
            labels[i++] = l;
        // -----------------------------

        mCompCol = new StatCollector(compFile, false, false, String.format(
                "Instance: %s\nALNS:\n%s", fileName, alns),//
                labels);

    }

    /**
     * Creation of an array of custom labels for the solution logger
     * 
     * @param alns
     *            the ALNS which events will be logged
     * @return an array of custom labels
     * @see #getAdditionalSolStats(ALNSCallbackEvent)
     */
    protected Label<?>[] getAdditionalSolLabels(AdaptiveLargeNeighborhoodSearch<?> alns) {
        return new Label<?>[0];
    }

    /**
     * Creation of an array of custom labels for the component logger
     * 
     * @param alns
     *            the ALNS which events will be logged
     * @return an array of custom labels
     * @see #getAdditionalSolStats(ALNSCallbackEvent)
     */
    protected Label<?>[] getAdditionalCompLabels(AdaptiveLargeNeighborhoodSearch<?> alns) {
        return new Label<?>[0];
    }

    /**
     * Collect the basic and custom statistics
     * 
     * @param e
     * @param bestSol
     * @param currentSol
     * @param tmpSol
     */
    private void collectSolStats(ALNSCallbackEvent<S> e, S bestSol, S currentSol, S tmpSol) {
        String time = String.format("%1$tk:%1$tM:%1$tS", new Date(e.getTimeStamp()));

        double bs = bestSol != null ? bestSol.getObjectiveValue() : Double.NaN;
        double cs = currentSol != null ? currentSol.getObjectiveValue() : Double.NaN;
        double ts = tmpSol != null ? tmpSol.getObjectiveValue() : Double.NaN;

        // SOLUTION STATS
        Object[] values = new Object[mSolCol.getLabels().length];
        int k = 0;
        values[k++] = time;
        values[k++] = e.getElapsedTime();
        values[k++] = e.getCurrentIteration();
        values[k++] = e.getDescription();
        values[k++] = bs;
        values[k++] = cs;
        values[k++] = ts;
        for (Object o : getAdditionalSolStats(e, bestSol, currentSol, tmpSol))
            values[k++] = o;
        mSolCol.collect(values);
    }

    /**
     * Collect the basic and custom statistics
     * 
     * @param e
     * @param bestSol
     * @param currentSol
     * @param tmpSol
     */
    @SuppressWarnings("unchecked")
    private void collectCompStats(ALNSCallbackEvent<S> e, S bestSol, S currentSol, S tmpSol) {
        String time = String.format("%1$tk:%1$tM:%1$tS", new Date(e.getTimeStamp()));

        // COMPONENTS STATS
        Object[] values = new Object[mCompCol.getLabels().length];
        int k = 0;
        values[k++] = time;
        values[k++] = e.getElapsedTime();
        values[k++] = e.getCurrentIteration();
        values[k++] = e.getDescription();
        // values[k++] = bestSol;
        // values[k++] = currentSol;
        // values[k++] = tmpSol;
        for (IDestroy<?> d : mDestroyComp)
            values[k++] = e.getSource().getDestroyComponents().getWeight((IDestroy<S>) d);
        for (IRepair<?> r : mRepairComp)
            values[k++] = e.getSource().getRepairComponents().getWeight((IRepair<S>) r);
        for (Object o : getAdditionalCompStats(e))
            values[k++] = o;
        mCompCol.collect(values);
    }

    /**
     * Get custom statistics on the solution.
     * 
     * @param e
     *            the logged event
     * @param bestSol
     *            the best sol
     * @param currentSol
     *            the current sol
     * @param tmpSol
     *            the tmp sol
     * @return an array containing the additional statistics to be logged
     * @see #getAdditionalSolLabels(AdaptiveLargeNeighborhoodSearch)
     */
    protected Object[] getAdditionalSolStats(ALNSCallbackEvent<S> e, S bestSol, S currentSol,
            S tmpSol) {
        return new Object[0];
    }

    /**
     * Get custom statistics on the components
     * 
     * @param e
     *            the logged event
     * @return an array containing the additional statistics to be logged
     * @see #getAdditionalSolLabels(AdaptiveLargeNeighborhoodSearch)
     */
    protected Object[] getAdditionalCompStats(ALNSCallbackEvent<S> e) {
        return new Object[0];
    }

}
