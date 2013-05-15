package vroom.common.utilities.callbacks;

import java.util.Date;

/**
 * <code>CallbackEventBase</code> is a general purposes implementation of {@link ICallbackEvent}.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:07:04 a.m.
 */
public class CallbackEventBase<S, E extends ICallbackEventTypes> implements ICallbackEvent<S, E> {

    private final E        mType;
    private final long     mTimeStamp;
    private final S        mSource;

    private final Object[] mParams;

    /**
     * Creates a new <code>CallbackEventBase</code>
     * 
     * @param type
     *            the type of event
     * @param source
     *            the source that generated this event
     * @param params
     *            optional parameters
     */
    public CallbackEventBase(E type, S source, Object... params) {
        super();

        if (type == null) {
            throw new IllegalArgumentException("Argument type cannot be null");
        }
        if (source == null) {
            throw new IllegalArgumentException("Argument source cannot be null");
        }

        mType = type;
        mSource = source;
        mTimeStamp = System.currentTimeMillis();
        mParams = params;
    }

    @Override
    public E getType() {
        return mType;
    }

    @Override
    public long getTimeStamp() {
        return mTimeStamp;
    }

    @Override
    public S getSource() {
        return mSource;
    }

    @Override
    public Object[] getParams() {
        return mParams;
    }

    @Override
    public String getDescription() {
        return mType.getDescription();
    }

    /**
     * @return the time stamp in the format HHhMMmSS
     */
    public String getTimeStampString() {
        return getTimeStampString(getTimeStamp());
    }

    /**
     * Utility method for the formating of a time stamp
     * 
     * @param timeStamp
     * @return a string describing the given time stamp with format hh:mm:ss
     */
    public static String getTimeStampString(long timeStamp) {
        return String.format("%1$tH:%1$tM:%1$tSs", new Date(timeStamp));
    }

    @Override
    public String toString() {
        return String.format("%s t:%s", getType(), getTimeStampString());
    }
}