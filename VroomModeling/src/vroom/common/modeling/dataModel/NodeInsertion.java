package vroom.common.modeling.dataModel;

/**
 * Creation date: Mar 8, 2010 - 9:38:53 AM<br/>
 * <code>NodeInsertion</code> is a container class used to represent an insertion in this route.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class NodeInsertion {

    /** The node. */
    private final INodeVisit mNode;

    /** The cost. */
    private final double     mCost;

    /** The route in which this insertion has been calculated */
    private final IRoute<?>  mRoute;

    /** The position inside the route. */
    private final int        mPosition;

    /**
     * Creates a new <code>NodeInsertion</code>.
     * 
     * @param node
     *            the node
     * @param cost
     *            the cost
     * @param position
     *            the position
     * @param route
     *            the corresponding route
     */
    public NodeInsertion(INodeVisit node, double cost, int position, IRoute<?> route) {
        mNode = node;
        mCost = cost;
        mPosition = position;
        mRoute = route;
    }

    /**
     * Getter for <code>node</code>
     * 
     * @return the node to be inserted
     */
    public INodeVisit getNode() {
        return mNode;
    }

    /**
     * Getter for <code>cost</code>
     * 
     * @return the cost corresponding to this insertion
     */
    public double getCost() {
        return mCost;
    }

    /**
     * Getter for <code>route</code>
     * 
     * @return the route the route for which this insertion has been calculated
     */
    public IRoute<?> getRoute() {
        return mRoute;
    }

    /**
     * Getter for <code>position</code>
     * 
     * @return the position the insertion position
     */
    public int getPosition() {
        return mPosition;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("%s@[%s:%s](c=%.4f)", mNode.getID(), mRoute.hashCode(), mPosition,
                mCost);
    }
}