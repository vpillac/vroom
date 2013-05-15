package vroom.common.modeling.dataModel;

import java.util.Date;

/**
 * <code>DateTimeStamp</code> represents a time stamp expressed as a <code>Date</code>.
 * 
 * @see java.util.Date
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:07:28 a.m.
 */
public class DateTimeStamp implements ITimeStamp {

    /** The date of this time stamp. */
    private final Date mDate;

    /**
     * Creates a new time stamp based on a {@link Date}.
     * 
     * @param date
     *            the date of this time stamp
     * @throws IllegalArgumentException
     *             is the <code>date</code> argument is <code>null</code>
     */
    public DateTimeStamp(Date date) {
        if (date == null) {
            throw new IllegalArgumentException("Argument date cannot be null");
        }
        mDate = date;
    }

    /**
     * Long value.
     * 
     * @return a representing the instant in time of this time stamp
     */
    @Override
    public double doubleValue() {
        return mDate.getTime();
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

}// end DateTimeStamp