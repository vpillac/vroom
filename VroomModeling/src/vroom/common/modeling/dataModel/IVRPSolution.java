package vroom.common.modeling.dataModel;

import java.util.Iterator;

import vroom.common.utilities.optimization.ISolution;

public interface IVRPSolution<R extends IRoute<?>> extends Iterable<R>, ISolution, Cloneable {

    /**
     * Addition of a route to this solution.
     * 
     * @param route
     *            the route to be added to this solution
     */
    public void addRoute(R route);

    /**
     * Removal of a route from this solution
     * 
     * @param route
     *            the route to be removed
     */
    public void removeRoute(IRoute<?> route);

    /**
     * Gets the cost.
     * 
     * @return the total cost of this solution
     */
    public double getCost();

    /**
     * Getter for parentInstance : The parent instance for this solution.
     * 
     * @return the value of parentInstance
     */
    public IVRPInstance getParentInstance();

    /**
     * Gets the route.
     * 
     * @param index
     *            the index (id) of the desired route
     * @return the @link{edu.uniandes.copa.routing.dataModel.IRoute} of index (id)
     */
    public R getRoute(int index);

    /**
     * Convenience method for the number of routes.
     * 
     * @return the number of routes in this solution
     */
    public int getRouteCount();

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString();

    /* (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<R> iterator();

    /**
     * Remove all the routes from this solution
     */
    public void clear();

    public abstract void addRoute(R route, int index);

    @Override
    public IVRPSolution<R> clone();
}