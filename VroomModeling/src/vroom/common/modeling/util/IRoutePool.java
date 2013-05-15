/*
 * National ICT Australia - http://www.nicta.com.au - All Rights Reserved
 */
package vroom.common.modeling.util;

import java.util.List;

import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.IRoute;
import vroom.common.modeling.dataModel.ImmutableRoute;
import vroom.common.utilities.ICloneable;
import vroom.common.utilities.IDisposable;

/**
 * The interface <code>IRoutePool</code> defines a pool that will contain a collection of routes
 * <p>
 * Creation date: May 4, 2013 - 2:23:14 PM
 * 
 * @author vpillac, <a href="http://www.nicta.com.au">National ICT Australia</a>
 * @version 1.0
 * @param <V>
 */
public interface IRoutePool<V extends INodeVisit> extends IDisposable, Iterable<ImmutableRoute<V>>,
        ICloneable<IRoutePool<V>> {

    /**
     * Returns the number of hash collisions detected The result is only valid is {@link #sCountCollisions} is set to
     * {@code true}
     * 
     * @return the number of hash collisions detected
     * @throws IllegalStateException
     *             if HashRoutePool.sCountCollisions is set to false
     */
    public abstract int getCollisionsCount();

    /**
     * Add a number of routes to this pool
     * 
     * @param routes
     * @return the number of routes that were actually added to this pool
     * @author vpillac
     */
    public abstract int add(Iterable<? extends IRoute<V>> routes);

    /**
     * Return all the routes contained in this pool
     * 
     * @return all the routes contained in this pool
     * @author vpillac
     */
    public abstract List<ImmutableRoute<V>> getAllRoutes();

    /**
     * Remove all the routes contained in this pool
     * 
     * @author vpillac
     */
    public abstract void clear();

    /**
     * Return the number of routes in this pool
     * 
     * @return the number of routes in this pool
     * @author vpillac
     */
    public abstract int size();

    /**
     * Create a clone of this pool
     * 
     * @return a clone of this pool
     * @author vpillac
     */
    @Override
    public abstract IRoutePool<V> clone();

}