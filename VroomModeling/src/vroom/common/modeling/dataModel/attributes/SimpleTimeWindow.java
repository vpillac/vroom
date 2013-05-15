/**
 * 
 */
package vroom.common.modeling.dataModel.attributes;

/**
 * The Class <code>SimpleTimeWindow</code> is an implementation of {@link ITimeWindow} based on two double values.
 * <p>
 * Creation date: Feb 11, 2011 - 1:59:17 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class SimpleTimeWindow extends TimeWindowBase {

    /** The end time of this time window *. */
    private final double mEnd;

    /** The start time of this time window *. */
    private final double mStart;

    /**
     * Creates a new time window based on {@link double}.
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
    public SimpleTimeWindow(double start, double end, boolean softStart, boolean softEnd) {
        super(softStart, softEnd);
        mStart = start;
        mEnd = end;
    }

    /**
     * Creates a new hard time window based on {@link double}.
     * 
     * @param start
     *            the start time of this time window
     * @param end
     *            the end time of this time window
     */
    public SimpleTimeWindow(double start, double end) {
        super(false, false);
        mStart = start;
        mEnd = end;
    }

    /*
     * (non-Javadoc)
     * @see vroom.common.modeling.dataModel.attributes.ITimeWindow#endAsLong()
     */
    @Override
    public double endAsDouble() {
        return mEnd;
    }

    /**
     * Getter for name : The end time of this time window.
     * 
     * @return the value of name
     */
    public double getEnd() {
        return mEnd;
    }

    /**
     * Getter for start : The start time of this time window.
     * 
     * @return the value of start
     */
    public double getStart() {
        return mStart;
    }

    /*
     * (non-Javadoc)
     * @see vroom.common.modeling.dataModel.attributes.ITimeWindow#startAsLong()
     */
    @Override
    public double startAsDouble() {
        return mStart;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("%s%s,%s%s", isSoftStart() ? '(' : '[', getStart(), getEnd(), isSoftEnd() ? ')' : ']');
    }

}
