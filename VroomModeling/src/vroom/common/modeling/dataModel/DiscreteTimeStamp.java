package vroom.common.modeling.dataModel;

/**
 * <code>DiscreteTimeStamp</code> represents an instant in time as the index of a time interval.
 * 
 * @see IPlanningPeriod
 * @see DiscretePlanningPeriod
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:07:29 a.m.
 */
public class DiscreteTimeStamp implements ITimeStamp {

    /** The corresponding interval *. */
    private final int mInterval;

    /**
     * Creates a time stamp corresponding to the given interval.
     * 
     * @param interval
     *            the index of the interval for this time spamp
     * @see DiscretePlanningPeriod
     */
    public DiscreteTimeStamp(int interval) {
        mInterval = interval;
    }

    /* (non-Javadoc)
     * @see vroom.common.modeling.dataModel.ITimeStamp#doubleValue()
     */
    @Override
    public double doubleValue() {
        return mInterval;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "" + doubleValue();
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(ITimeStamp o) {
        return Double.compare(doubleValue(), o.doubleValue());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ITimeStamp && compareTo((ITimeStamp) obj) == 0;
    }

}// end DiscreteTimeStamp