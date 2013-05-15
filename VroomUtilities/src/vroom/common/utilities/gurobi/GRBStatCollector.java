/**
 *
 */
package vroom.common.utilities.gurobi;

import static gurobi.GRB.Callback.RUNTIME;
import gurobi.GRB;
import gurobi.GRBCallback;
import gurobi.GRBException;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Arrays;

import vroom.common.utilities.StatCollector;
import vroom.common.utilities.StatCollector.Label;

/**
 * <code>GRBStatCollectot</code>
 * <p>
 * Creation date: Aug 25, 2011 - 9:42:34 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class GRBStatCollector extends GRBCallback {

    private double                 mIncumbent;
    private double                 mBestBound;
    private double                 mNodLft;

    public final static Label<?>[] LABELS = new Label<?>[] {
            new Label<Long>("Time", Long.class), //
            new Label<Long>("ExplNodes", Long.class),//
            new Label<Long>("UnexplNodes", Long.class),//
            new Label<Integer>("SolCount", Integer.class),//
            new Label<Double>("Incumbent", Double.class, new DecimalFormat("###0.0000")),//
            new Label<Double>("BestBd", Double.class, new DecimalFormat("###0.0000")), //
            new Label<Double>("Gap", Double.class, new DecimalFormat("###0.000000")),
            new Label<String>("Where", String.class)

                                          };

    private final StatCollector    mCollector;

    /**
     * Creates a new <code>GRBStatCollectot</code>
     * 
     * @param file
     *            the file in which stats will be collected
     * @param comment
     *            a comment for the file
     */
    public GRBStatCollector(String file, String comment) {
        this(file, comment, false, true);
    }

    /**
     * Creates a new <code>GRBStatCollectot</code>
     * 
     * @param detailStatFile
     *            the file in which stats will be collected
     * @param comment
     *            a comment for the file
     * @param append
     *            <code>true</code> if the new stats should be appended to the end of an existing file
     * @param autoFlush
     *            <code>true</code> if the new stats should be written immediately to the file
     * @see StatCollector#StatCollector(File, boolean, boolean, String, Label...)
     */
    public GRBStatCollector(String detailStatFile, String comment, boolean append, boolean autoFlush) {
        Label<?>[] labels = LABELS;
        Label<?>[] add = getAdditionalLabels();
        if (add.length > 0) {
            labels = Arrays.copyOf(labels, labels.length + add.length);
            for (int i = 1; i <= add.length; i++) {
                labels[labels.length - i] = add[add.length - i];
            }
        }

        mCollector = new StatCollector(new File(detailStatFile), autoFlush, append, comment, labels);
    }

    /**
     * Returns the current gap
     * 
     * @return the current gap
     */
    public double getGap() {
        return Math.abs((mIncumbent - mBestBound) / mIncumbent);
    }

    /**
     * Returns the current incumbent value
     * 
     * @return the current incumbent value
     */
    public double getIncumbent() {
        return mIncumbent;
    }

    /**
     * Returns the current best bound
     * 
     * @return the current best bound
     */
    public double getBestBound() {
        return mBestBound;
    }

    /**
     * Returns additional labels for the {@link StatCollector}
     * 
     * @return additional labels for the {@link StatCollector}
     */
    protected Label<?>[] getAdditionalLabels() {
        return new Label<?>[0];
    }

    /* (non-Javadoc)
     * @see gurobi.GRBCallback#callback()
     */
    @Override
    protected void callback() {
        switch (where) {
        case GRB.Callback.MIPNODE:
        case GRB.Callback.MIPSOL:
            try {
                if (updateStats())
                    collectStats();
            } catch (GRBException e) {
                GRBLogging.CALLBACK.exception("GRBStatCollectot.callback", e);
            }
            break;
        }
    }

    /**
     * Update the stored statistics
     * 
     * @return <code>true</code> if one of the statistics has changed
     * @throws GRBException
     */
    private boolean updateStats() throws GRBException {
        boolean changed = false;

        int objbnd = 0, objbst = 0;
        switch (where) {
        case GRB.Callback.MIPNODE:
            objbnd = GRB.Callback.MIPNODE_OBJBND;
            objbst = GRB.Callback.MIPNODE_OBJBST;
            mNodLft = getDoubleInfo(GRB.Callback.MIP_NODLFT);
            break;
        case GRB.Callback.MIPSOL:
            objbnd = GRB.Callback.MIPSOL_OBJBND;
            objbst = GRB.Callback.MIPSOL_OBJBST;
        }

        double bnd = getDoubleInfo(objbnd);
        double inc = getDoubleInfo(objbst);

        if (bnd != mBestBound) {
            mBestBound = bnd;
            changed = true;
        }
        if (inc != mIncumbent) {
            mIncumbent = inc;
            changed = true;
        }

        return changed;
    }

    /**
     * Returns an array of objects containing the stats to be collected
     * 
     * @return an array of objects containing the stats to be collected
     * @throws GRBException
     */
    private final Object[] getStatArray() throws GRBException {
        int nodcnt = 0, solcnt = 0;
        switch (where) {
        case GRB.Callback.MIPNODE:
            nodcnt = GRB.Callback.MIPNODE_NODCNT;
            solcnt = GRB.Callback.MIPNODE_SOLCNT;
            break;
        case GRB.Callback.MIPSOL:
            nodcnt = GRB.Callback.MIPSOL_NODCNT;
            solcnt = GRB.Callback.MIPSOL_SOLCNT;
        }

        return new Object[] { (long) getDoubleInfo(RUNTIME),//
                (long) getDoubleInfo(nodcnt), //
                (long) mNodLft, //
                getIntInfo(solcnt), //
                mIncumbent, //
                mBestBound, //
                getGap(), GRBUtilities.callbackWhereToString(where) };
    }

    /**
     * Returns an array of objects containing the additional stats to be collected
     * 
     * @return an array of objects containing the additional stats to be collected
     * @throws GRBException
     */
    protected Object[] getAdditionalStatArray() throws GRBException {
        return new Object[0];
    }

    /**
     * Collect the statistics in the stat file
     * 
     * @throws GRBException
     */
    private final void collectStats() throws GRBException {
        Object[] stats = getStatArray();
        Object[] add = getAdditionalStatArray();
        if (add.length > 0) {
            stats = Arrays.copyOf(stats, stats.length + add.length);
            for (int i = 1; i <= add.length; i++) {
                stats[stats.length - i] = add[add.length - i];
            }
        }

        mCollector.collect(stats);
    }

    /**
     * Flush the collected stats to the destination stat file
     */
    public void flush() {
        mCollector.flush();
    }

    /**
     * Close the underlying file writer
     */
    public void closeCollector() {
        mCollector.close();
    }
}
