/**
 * 
 */
package vroom.common.modeling.dataModel.attributes;

import vroom.common.utilities.Constants;

/**
 * The class <code>TimeWindowBase</code> JAVADOC
 * <p>
 * Creation date: Mar 1, 2012 - 5:00:56 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public abstract class TimeWindowBase implements ITimeWindow {

    /**
     * <code>true</code> if the time windows end is soft, <code>false</code> otherwise.
     */
    private final boolean mSoftEnd;

    /**
     * <code>true</code> if the time windows start is soft, <code>false</code> otherwise.
     */
    private final boolean mSoftStart;

    /**
     * Creates a new time window based on {@link double}.
     * 
     * @param softStart
     *            <code>true</code> if the time windows start is soft, <code>false</code> otherwise
     * @param softEnd
     *            <code>true</code> if the time windows end is soft, <code>false</code> otherwise
     */
    public TimeWindowBase(boolean softStart, boolean softEnd) {
        mSoftStart = softStart;
        mSoftEnd = softEnd;
    }

    /**
     * Creates a new <code>TimeWindowBase</code>
     */
    public TimeWindowBase() {
        this(false, false);
    }

    /*
     * (non-Javadoc)
     * @see edu.uniandes.copa.utils.IObjectWithName#getName()
     */
    @Override
    public String getName() {
        return "Time Window";
    }

    /*
     * (non-Javadoc)
     * @see vroom.common.modeling.dataModel.attributes.ITimeWindow#isSoftEnd()
     */
    @Override
    public final boolean isSoftEnd() {
        return mSoftEnd;
    }

    /*
     * (non-Javadoc)
     * @see vroom.common.modeling.dataModel.attributes.ITimeWindow#isSoftStart()
     */
    @Override
    public final boolean isSoftStart() {
        return mSoftStart;
    }

    @Override
    public final boolean isWithinTW(double arrivalTime) {
        return Constants.isLowerThan(startAsDouble(), arrivalTime)
                && Constants.isLowerThan(arrivalTime, endAsDouble());
    }

    @Override
    public final boolean isFeasible(double arrivalTime) {
        return Constants.isLowerThan(arrivalTime, endAsDouble());
    }

    @Override
    public final double getEarliestStartOfService(double arrivalTime) {
        return Math.max(arrivalTime, startAsDouble());
    }

    @Override
    public final double getViolation(double arrivalTime) {
        return Math.max(0, arrivalTime - endAsDouble());
    }

    @Override
    public final double getWaiting(double arrivalTime) {
        return Math.max(0, startAsDouble() - arrivalTime);
    }

    @Override
    public double width() {
        return endAsDouble() - startAsDouble();
    }

}
