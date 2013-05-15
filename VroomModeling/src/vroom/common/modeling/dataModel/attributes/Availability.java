/**
 * 
 */
package vroom.common.modeling.dataModel.attributes;

import java.util.Date;

/**
 * <code>Availability</code> models the availability range of vehicle
 * <p>
 * Creation date: Jan 25, 2012 - 4:23:07 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class Availability implements IVehicleAttribute {

    private final Date mStart;
    private final Date mEnd;

    /**
     * Getter for <code>start</code>
     * 
     * @return the start
     */
    public Date getStart() {
        return mStart;
    }

    /**
     * Getter for <code>end</code>
     * 
     * @return the end
     */
    public Date getEnd() {
        return mEnd;
    }

    public Availability(Date start, Date end) {
        super();
        mStart = start;
        mEnd = end;
    }

    /* (non-Javadoc)
     * @see vroom.common.utilities.dataModel.IObjectWithName#getName()
     */
    @Override
    public String getName() {
        return "Availability";
    }

    @Override
    public String toString() {
        return String.format("[%s,%s]", getStart() == null ? "-" : getStart(),
                getEnd() == null ? "-" : getEnd());
    }
}
