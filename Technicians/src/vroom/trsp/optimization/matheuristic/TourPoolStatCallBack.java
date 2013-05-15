/**
 *
 */
package vroom.trsp.optimization.matheuristic;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import vroom.common.heuristics.alns.ALNSEventType;
import vroom.common.heuristics.alns.AdaptiveLargeNeighborhoodSearch;
import vroom.common.heuristics.alns.IDestroy;
import vroom.common.heuristics.alns.IRepair;
import vroom.common.utilities.MutableInt;
import vroom.common.utilities.StatCollector;
import vroom.common.utilities.StatCollector.Label;
import vroom.common.utilities.callbacks.CallbackBase;
import vroom.common.utilities.callbacks.ICallbackEvent;
import vroom.trsp.datamodel.ITRSPSolutionHasher;
import vroom.trsp.datamodel.ITRSPTour;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.datamodel.TRSPTour;

/**
 * JAVADOC <code>SolutionPoolCallBack</code>
 * <p>
 * Creation date: Aug 12, 2011 - 11:51:53 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TourPoolStatCallBack extends
        CallbackBase<AdaptiveLargeNeighborhoodSearch<TRSPSolution>, ALNSEventType> {

    private final ITRSPSolutionHasher   mHasher;

    private final Map<Integer, TourDef> mStats;

    private IDestroy<?>                 mLastDestroy;

    private int                         mGenCols;

    public static final Label<?>[]      LABELS = new Label<?>[] {
            new Label<String>("Operator", String.class),//
            new Label<Integer>("GenTours", Integer.class),//
            new Label<Integer>("NonRepeated", Integer.class),//
            new Label<Integer>("Improvements", Integer.class),//
            new Label<Integer>("Exlusive", Integer.class), //
            new Label<Double>("Diversity", Double.class, new DecimalFormat("###0.00000")), //
            new Label<Double>("AbsoluteContr", Double.class, new DecimalFormat("###0.00000")), //
            new Label<Double>("RelativeContr", Double.class, new DecimalFormat("###0.00000")), //
            new Label<Integer>("GenCols", Integer.class), //
            new Label<Integer>("PoolSize", Integer.class) //
                                               };

    /**
     * Getter for the hasher used to hash tours
     * 
     * @return the hasher used to hash tours
     */
    public ITRSPSolutionHasher getHasher() {
        return mHasher;
    }

    /**
     * Creates a new <code>SolutionPoolCallBack</code>
     * 
     * @param instance
     * @param numIterations
     */
    public TourPoolStatCallBack(ITRSPSolutionHasher hasher) {
        super();

        mHasher = hasher;

        mStats = new HashMap<Integer, TourDef>();

        mGenCols = 0;
    }

    @Override
    public void execute(
            ICallbackEvent<AdaptiveLargeNeighborhoodSearch<TRSPSolution>, ALNSEventType> event) {
        if (event.getType() == ALNSEventType.DESTROYED) {
            mLastDestroy = (IDestroy<?>) event.getParams()[3];
        }
        if (event.getType() == ALNSEventType.REPAIRED) {
            IRepair<?> repair = (IRepair<?>) event.getParams()[3];
            for (TRSPTour tour : (TRSPSolution) event.getParams()[2]) {
                if (tour.length() > 2) {
                    int hash = mHasher.hash(tour);
                    TourDef def = mStats.get(hash);
                    if (def == null) {
                        def = new TourDef(tour, hash);
                        mStats.put(hash, def);
                    }
                    def.addStat(mLastDestroy, repair, tour.getTotalCost());
                    mGenCols++;
                }
            }
        }
    }

    /**
     * Store the statistics collected by the callback in a file
     * 
     * @param statFile
     *            the file in which statistics will be written
     * @param comment
     *            a comment for the file
     * @param poolSize
     *            the final size of the tour pool
     */
    public void collectStats(File statFile, String comment, int poolSize) {
        StatCollector collector = new StatCollector(statFile, false, false, comment, LABELS);

        Map<String, int[]> stats = new HashMap<String, int[]>();
        for (TourDef stat : mStats.values()) {
            for (String key : stat.mOpCount.keySet()) {
                int[] val = stats.get(key);
                if (val == null) {
                    val = new int[4];
                    stats.put(key, val);
                }
                // Gen tours
                val[0] += stat.mOpCount.get(key).intValue();
                // Non-repeated
                if (stat.mOpCount.get(key).intValue() == 1)
                    val[1]++;
                // Improvements
                val[2] += stat.mOpImpCount.get(key).intValue();
                // Exclusive
                if (stat.isUnique())
                    val[3] += 1;
            }
        }
        ArrayList<String> keys = new ArrayList<String>(stats.keySet());
        Collections.sort(keys, new Comparator<String>() {

            @Override
            public int compare(String o1, String o2) {
                boolean c1 = o1.contains("+");
                boolean c2 = o2.contains("+");
                if ((c1 && c2) || !c1 && !c2) {
                    return o1.compareTo(o2);
                }
                return c1 ? 1 : -1;
            }
        });
        for (String key : keys) {
            int[] val = stats.get(key);
            Object[] values = new Object[LABELS.length];
            int i = 0;
            values[i++] = key;
            values[i++] = val[0];// Gen tours
            values[i++] = val[1];// Non repeated
            values[i++] = val[2];// Improv
            values[i++] = val[3];// Exclusive
            values[i++] = val[1] / ((double) mGenCols);
            values[i++] = val[1] / ((double) poolSize);
            values[i++] = val[3] / ((double) poolSize);
            values[i++] = mGenCols;
            values[i++] = poolSize;
            collector.collect(values);
        }
        collector.flush();
        collector.close();

    }

    private static class TourDef {
        private final int                     mHash;
        private double                        mBestCost;
        private final Map<String, MutableInt> mOpCount;
        private final Map<String, MutableInt> mOpImpCount;
        private int                           mCount;

        private TourDef(ITRSPTour tour, int hash) {
            mHash = hash;
            mOpCount = new HashMap<String, MutableInt>();
            mOpImpCount = new HashMap<String, MutableInt>();

            mBestCost = Double.POSITIVE_INFINITY;

            mCount = 0;
        }

        private void addStat(IDestroy<?> destroy, IRepair<?> repair, double cost) {
            String combkey = String.format("%s+%s", destroy.getName(), repair.getName());
            boolean added = false;
            added |= addStat(destroy.getName(), cost);
            added |= addStat(repair.getName(), cost);
            added |= addStat(combkey, cost);
            if (cost < mBestCost)
                mBestCost = cost;
            if(added)
            mCount++;
        }

        /**
         * Update the stats related to key
         * @param key a key for the operator
         * @param cost the cost of the generated tour
         * @return <code>true</code> if a new entry was created; <code>false</code> otherwise
         */
        private boolean addStat(String key, double cost) {
            MutableInt val = mOpCount.get(key);
            boolean added =false;
            if (val == null) {
                val = new MutableInt(1);
                mOpCount.put(key, val);
                val = new MutableInt(0);
                mOpImpCount.put(key, val);
                added = true;
            } else
                val.increment();

            if (cost < mBestCost) {
                mOpImpCount.get(key).increment();
            }
            return added;
        }

        /**
         * @return <code>true</code> if this tour was found only once
         */
        private boolean isUnique() {
            return mCount == 1;
        }
    }

}
