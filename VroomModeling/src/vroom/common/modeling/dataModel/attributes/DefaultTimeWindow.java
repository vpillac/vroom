package vroom.common.modeling.dataModel.attributes;

import vroom.common.modeling.dataModel.ITimeStamp;

/**
 * <code>DefaultTimeWindow</code> uses {@link ITimeStamp} to represent the start and end of the corresponding time
 * window.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:07:13 a.m.
 */
public class DefaultTimeWindow extends TimeWindowBase {

    /** The end time of this time window *. */
    private final ITimeStamp mEnd;

    /** The start time of this time window *. */
    private final ITimeStamp mStart;

    /**
     * Creates a new time window based on {@link ITimeStamp}.
     * 
     * @param start
     *            the start time of this time window
     * @param end
     *            the end time of this time window
     * @param softStart
     *            <code>true</code> if the time windows start is soft, <code>false</code> otherwise
     * @param softEnd
     *            <code>true</code> if the time windows end is soft, <code>false</code> otherwise
     */
    public DefaultTimeWindow(ITimeStamp start, ITimeStamp end, boolean softStart, boolean softEnd) {
        super(softStart, softEnd);
        mStart = start;
        mEnd = end;
    }

    /*
     * (non-Javadoc)
     * @see vroom.common.modeling.dataModel.attributes.ITimeWindow#endAsLong()
     */
    @Override
    public double endAsDouble() {
        return getEnd().doubleValue();
    }

    /**
     * Getter for name : The end time of this time window.
     * 
     * @return the value of name
     */
    public ITimeStamp getEnd() {
        return mEnd;
    }

    /*
     * (non-Javadoc)
     * @see edu.uniandes.copa.utils.IObjectWithName#getName()
     */
    @Override
    public String getName() {
        return "Time Window";
    }

    /**
     * Getter for start : The start time of this time window.
     * 
     * @return the value of start
     */
    public ITimeStamp getStart() {
        return mStart;
    }

    /*
     * (non-Javadoc)
     * @see vroom.common.modeling.dataModel.attributes.ITimeWindow#startAsLong()
     */
    @Override
    public double startAsDouble() {
        return getStart().doubleValue();
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("%s%s,%s%s", isSoftStart() ? '(' : '[', getStart(), getEnd(), isSoftEnd() ? ')' : ']');
    }
}// end DefaultTimeWindow