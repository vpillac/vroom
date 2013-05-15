/**
 * 
 */
package vroom.common.modeling.dataModel.attributes;

/**
 * <code>OpenTimeWindow</code> is an implementation of {@link ITimeWindow} modeling an open time window (without
 * specific start or end)
 * <p>
 * Creation date: Nov 7, 2011 - 3:12:47 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class OpenTimeWindow implements ITimeWindow {

    /* (non-Javadoc)
     * @see vroom.common.utilities.dataModel.IObjectWithName#getName()
     */
    @Override
    public String getName() {
        return "TW";
    }

    /* (non-Javadoc)
     * @see vroom.common.modeling.dataModel.attributes.ITimeWindow#endAsDouble()
     */
    @Override
    public double endAsDouble() {
        return Double.POSITIVE_INFINITY;
    }

    /* (non-Javadoc)
     * @see vroom.common.modeling.dataModel.attributes.ITimeWindow#isSoftEnd()
     */
    @Override
    public boolean isSoftEnd() {
        return false;
    }

    /* (non-Javadoc)
     * @see vroom.common.modeling.dataModel.attributes.ITimeWindow#isSoftStart()
     */
    @Override
    public boolean isSoftStart() {
        return false;
    }

    /* (non-Javadoc)
     * @see vroom.common.modeling.dataModel.attributes.ITimeWindow#startAsDouble()
     */
    @Override
    public double startAsDouble() {
        return Double.NEGATIVE_INFINITY;
    }

    /* (non-Javadoc)
     * @see vroom.common.modeling.dataModel.attributes.ITimeWindow#isWithinTW(double)
     */
    @Override
    public boolean isWithinTW(double arrivalTime) {
        return true;
    }

    /* (non-Javadoc)
     * @see vroom.common.modeling.dataModel.attributes.ITimeWindow#isFeasible(double)
     */
    @Override
    public boolean isFeasible(double arrivalTime) {
        return true;
    }

    /* (non-Javadoc)
     * @see vroom.common.modeling.dataModel.attributes.ITimeWindow#getViolation(double)
     */
    @Override
    public double getViolation(double arrivalTime) {
        return 0;
    }

    /* (non-Javadoc)
     * @see vroom.common.modeling.dataModel.attributes.ITimeWindow#getWaiting(double)
     */
    @Override
    public double getWaiting(double arrivalTime) {
        return 0;
    }

    /* (non-Javadoc)
     * @see vroom.common.modeling.dataModel.attributes.ITimeWindow#getEarliestStartOfService(double)
     */
    @Override
    public double getEarliestStartOfService(double arrivalTime) {
        return arrivalTime;
    }

    @Override
    public String toString() {
        return "na";
    }

    @Override
    public double width() {
        return endAsDouble() - startAsDouble();
    }

}
