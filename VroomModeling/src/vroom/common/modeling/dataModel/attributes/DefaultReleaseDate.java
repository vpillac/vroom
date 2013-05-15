package vroom.common.modeling.dataModel.attributes;

import vroom.common.modeling.dataModel.ITimeStamp;

/**
 * <code>DefaultReleaseDate</code> is an implementation of {@link IReleaseDate} based on a {@link ITimeStamp} to
 * represent the time.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #created 15-Feb-2010 11:29:49 a.m.
 */
public class DefaultReleaseDate implements IReleaseDate {

    /** The time stamp used for this release date. */
    private final ITimeStamp mTimeStamp;

    /**
     * Instantiates a new default release date.
     * 
     * @param timeStamp
     *            the time stamp
     */
    public DefaultReleaseDate(ITimeStamp timeStamp) {
        mTimeStamp = timeStamp;
    }

    /* (non-Javadoc)
     * @see edu.uniandes.copa.utils.IObjectWithName#getName()
     */
    @Override
    public String getName() {
        return "Release Date";
    }

    /**
     * Gets the time stamp.
     * 
     * @return the time stamp used for this release date
     */
    public ITimeStamp getTimeStamp() {
        return mTimeStamp;
    }

    /* (non-Javadoc)
     * @see vroom.common.modeling.dataModel.attributes.IReleaseDate#doubleValue()
     */
    @Override
    public double doubleValue() {
        return getTimeStamp().doubleValue();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getTimeStamp().toString();
    }

}// end DefaultReleaseDate