/**
 * 
 */
package vroom.common.modeling.dataModel.attributes;

/**
 * <code>Duration</code> is a generic attribute representing a duration
 * <p>
 * Creation date: Apr 19, 2010 - 3:40:30 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class Duration implements INodeAttribute, IVehicleAttribute, IRequestAttribute {

    /** A double representing the duration of this attribute **/
    private final double mDuration;

    /**
     * Getter for the duration
     * 
     * @return A double representing the duration of this attribute
     */
    public double getDuration() {
        return mDuration;
    }

    /**
     * Creates a new <code>Duration</code>
     * 
     * @param duration
     */
    public Duration(double duration) {
        mDuration = duration;
    }

    /* (non-Javadoc)
     * @see vroom.common.utilities.dataModel.IObjectWithName#getName()
     */
    @Override
    public String getName() {
        return "Duration";
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("%.3f", mDuration);
    }

}
