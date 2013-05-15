package vroom.common.modeling.dataModel;

/**
 * <code>DiscretePlanningPeriod</code> represents a planning period decomposed in a number of intervals of unit length.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:07:28 a.m.
 */
public class DiscretePlanningPeriod implements IPlanningPeriod {

    /** The number of intervals. */
    private final int mNumIntervals;

    /** The index of the first interval. */
    private final int mStart;

    /**
     * Creates a new planning period with unit intervals.
     * 
     * @param numIntervals
     *            the number of unit intervals
     * @see DiscretePlanningPeriod#DiscretePlanningPeriod(int, int)
     */
    public DiscretePlanningPeriod(int numIntervals) {
        this(0, numIntervals);
    }

    /**
     * Creates a new planning period with unit intervals and a start index (or offset).
     * 
     * @param start
     *            the index of the first period
     * @param numIntervals
     *            the number of unit intervals
     */
    public DiscretePlanningPeriod(int start, int numIntervals) {
        mStart = start;
        mNumIntervals = numIntervals;
    }

    /* (non-Javadoc)
     * @see vroom.common.modeling.dataModel.IPlanningPeriod#endAsLong()
     */
    @Override
    public long endAsLong() {
        return startAsLong() + size();
    }

    /* (non-Javadoc)
     * @see vroom.common.modeling.dataModel.IPlanningPeriod#intervalAsLong()
     */
    @Override
    public long intervalAsLong() {
        return 1;
    }

    /* (non-Javadoc)
     * @see vroom.common.modeling.dataModel.IPlanningPeriod#size()
     */
    @Override
    public int size() {
        return mNumIntervals;
    }

    /* (non-Javadoc)
     * @see vroom.common.modeling.dataModel.IPlanningPeriod#startAsLong()
     */
    @Override
    public long startAsLong() {
        return mStart;
    }

}// end DiscretePlanningPeriod