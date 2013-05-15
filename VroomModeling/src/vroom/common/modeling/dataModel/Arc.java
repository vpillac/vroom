/**
 * 
 */
package vroom.common.modeling.dataModel;

/**
 * <code>RouteArc</code>
 * <p>
 * Creation date: Apr 28, 2010 - 4:34:51 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class Arc implements IArc {

    /** The tail node visit **/
    private final INodeVisit mTailNodeVisit;

    /* (non-Javadoc)
     * @see vroom.common.modeling.dataModel.IArc#getTailNode()
     */
    @Override
    public INodeVisit getTailNode() {
        return mTailNodeVisit;
    }

    /** The head node visit **/
    private final INodeVisit mHeadNodeVisit;

    /* (non-Javadoc)
     * @see vroom.common.modeling.dataModel.IArc#getHeadNode()
     */
    @Override
    public INodeVisit getHeadNode() {
        return mHeadNodeVisit;
    }

    /** the distance separating the origin and head **/
    private final double mDistance;

    /* (non-Javadoc)
     * @see vroom.common.modeling.dataModel.IArc#getDistance()
     */
    @Override
    public double getDistance() {
        return mDistance;
    }

    /** A flag defining whether this arc is directed or not **/
    private final boolean mDirected;

    /* (non-Javadoc)
     * @see vroom.common.modeling.dataModel.IArc#isDirected()
     */
    @Override
    public boolean isDirected() {
        return mDirected;
    }

    /**
     * Creates a new <code>RouteArc</code>
     * 
     * @param tailNodeVisit
     *            the tail node visit
     * @param headNodeVisit
     *            the head node visit
     * @param distance
     *            the distance covered by this arc
     * @param directed
     *            <code>true</code> if the arc is directed
     */
    public Arc(INodeVisit tailNodeVisit, INodeVisit headNodeVisit, double distance, boolean directed) {
        mTailNodeVisit = tailNodeVisit;
        mHeadNodeVisit = headNodeVisit;
        mDistance = distance;
        mDirected = directed;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("(%s,%s),d=%.2f,%s", getTailNode().getNode().getID(), getHeadNode()
                .getNode().getID(), getDistance(), isDirected() ? "->" : "<->");
    }

}
