package vroom.common.modeling.dataModel;

import java.util.Date;

/**
 * <code>DatePlanningPeriod</code> represents a time window with two <code>Date</code> object: its start and its end.
 * 
 * @see java.util.Date
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:35:02 a.m.
 */
public class DatePlanningPeriod implements IPlanningPeriod {

    /** The start date of this planning period. */
    private final Date mEndDate;

    /** The interval for this planning period. */
    private final long mInterval;

    /** The start date of this planning period. */
    private final Date mStartDate;

    /**
     * Creates a new planning period.
     * 
     * @param startDate
     *            the start date of this planning period
     * @param endDate
     *            the end date of this planning period
     * @param interval
     *            a time interval (in ms) that will be used to decompose this planning period
     * @throws IllegalArgumentException
     *             if one of the dates is <code>null</code> or the interval is <= 0
     */
    public DatePlanningPeriod(Date startDate, Date endDate, long interval) {
        if (startDate == null) {
            throw new IllegalArgumentException("Argument startDate cannot be null");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("Argument endDate cannot be null");
        }
        if (interval <= 0) {
            throw new IllegalArgumentException("Argument interval has to be stricly positive");
        }
        if (startDate.after(endDate)) {
            throw new IllegalArgumentException("startDate must be before endDate");
        }

        mStartDate = startDate;
        mEndDate = endDate;
        mInterval = interval;
    }

    /**
     * End as long.
     * 
     * @return a long value representing the end date of this planning period
     */
    @Override
    public long endAsLong() {
        return mEndDate.getTime();
    }

    /**
     * Gets the end date.
     * 
     * @return the endDate
     */
    public Date getEndDate() {
        return mEndDate;
    }

    /**
     * Gets the interval.
     * 
     * @return the interval
     */
    public long getInterval() {
        return mInterval;
    }

    /**
     * Gets the start date.
     * 
     * @return the startDate
     */
    public Date getStartDate() {
        return mStartDate;
    }

    /**
     * Interval as long.
     * 
     * @return a long value representing the interval length of this planning period
     */
    @Override
    public long intervalAsLong() {
        return mInterval;
    }

    /**
     * Size.
     * 
     * @return the number of intervals in this planning period
     */
    @Override
    public int size() {
        return (int) ((endAsLong() - startAsLong()) / intervalAsLong());
    }

    /**
     * Start as long.
     * 
     * @return a long value representing the start date of this planning period
     */
    @Override
    public long startAsLong() {
        return mStartDate.getTime();
    }

    @Override
    public String toString() {
        return String.format("[%s,%s]", getStartDate(), getEndDate());
    }
}// end DatePlanningPeriod