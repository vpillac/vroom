/**
 *
 */
package vroom.common.modeling.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.IRoute;
import vroom.common.modeling.dataModel.ImmutableRoute;
import vroom.common.modeling.dataModel.VRPSolutionHasher;

/**
 * The class <code>HashRoutePool</code> is represents a pool of {@link IRoute} stored in a hash table
 * <p>
 * Creation date: May 2, 2013 - 5:15:06 PM
 * 
 * @author vpillac, <a href="http://www.nicta.com.au">National ICT Australia</a>
 * @version 1.0
 */
public class HashRoutePool<V extends INodeVisit> implements IRoutePool<V> {

    /** A flag that will enable the count of the number of hash collisions, default is {@code false} for performance */
    public static boolean     sCountCollisions = false;

    /**
     * The minimum size for routes, default is 3 (<code>&lt;depot,node,depot&gt;</code>), shorter routes will be ignored
     */
    public static int         sMinRouteSize    = 3;

    private final Map<?, ?>[] mRoutePool;

    private int               mSize;

    private int               mCollisionsCount = 0;

    /* (non-Javadoc)
     * @see vroom.common.modeling.util.IRoutePool#getCollisionsCount()
     */
    @Override
    public int getCollisionsCount() {
        if (!sCountCollisions)
            throw new IllegalStateException("HashRoutePool.sCountCollisions is set to false");
        return mCollisionsCount;
    }

    /** The hasher that will be used to calculate the hash of routes */
    private final VRPSolutionHasher mHasher;

    /**
     * Returns the hasher that will be used to calculate the hash of routes
     * 
     * @return the hasher that will be used to calculate the hash of routes
     */
    public VRPSolutionHasher getHasher() {
        return mHasher;
    }

    /**
     * Creates a new <code>HashRoutePool</code>
     * 
     * @param numVehicles
     *            the number of vehicles
     * @param expectedRoutesPerVehicle
     *            the expected number of routes per vehicle
     * @param hasher
     */
    public HashRoutePool(int numVehicles, int expectedRoutesPerVehicle, VRPSolutionHasher hasher) {
        mRoutePool = new Map<?, ?>[numVehicles];
        for (int i = 0; i < mRoutePool.length; i++) {
            mRoutePool[i] = new HashMap<Integer, ImmutableRoute<V>>(
                    (int) (expectedRoutesPerVehicle / 0.75) + 1, 0.75f);
        }
        mSize = 0;
        mHasher = hasher;
    }

    @SuppressWarnings("unchecked")
    private HashRoutePool(HashRoutePool<V> parent) {
        mRoutePool = new Map<?, ?>[parent.mRoutePool.length];
        for (int i = 0; i < mRoutePool.length; i++) {
            mRoutePool[i] = new HashMap<Integer, ImmutableRoute<V>>(
                    (HashMap<Integer, ImmutableRoute<V>>) parent.mRoutePool[i]);
        }
        mSize = parent.mSize;
        mHasher = parent.mHasher;
    }

    @SuppressWarnings("unchecked")
    private synchronized Map<Integer, ImmutableRoute<V>> getPool(int technician) {
        return (Map<Integer, ImmutableRoute<V>>) mRoutePool[technician];
    }

    /*
     * (non-Javadoc)
     * @see vroom.trsp.datamodel.RPool#add(java.util.Collection)
     */

    /* (non-Javadoc)
     * @see vroom.common.modeling.util.IRoutePool#add(java.lang.Iterable)
     */
    @Override
    public synchronized int add(Iterable<? extends IRoute<V>> routes) {
        int count = 0;
        for (IRoute<V> route : routes) {
            if (route.length() >= sMinRouteSize) {
                // Check for the presence of a route with the same hash
                Map<Integer, ImmutableRoute<V>> pool = getPool(route.getVehicle().getID());
                Integer hash = mHasher.hash(route);
                ImmutableRoute<V> prevRoute = pool.get(hash);

                if (prevRoute != null) {
                    // A route with the same hash already exists, compare objective values
                    if (prevRoute.getCost() > route.getCost()) {
                        // Previous route has a higher cost, replace it
                        pool.put(hash, new ImmutableRoute<V>(route, hash));
                    } else {
                        // Previous route has a lower cost, keep it
                    }
                    if (sCountCollisions) {
                        checkCollision(prevRoute, route);
                    }
                } else {
                    pool.put(hash, new ImmutableRoute<V>(route, hash));
                    count++;
                    mSize++;
                }

            }

        }

        return count;
    }

    /**
     * Check if {@code  prevRoute} visits the same requests as {@code  route}, if not increment the collision count
     * 
     * @param prevRoute
     * @param route
     */
    private void checkCollision(ImmutableRoute<V> prevRoute, IRoute<V> route) {
        if (prevRoute.length() != route.length()) {
            mCollisionsCount++;
            return;
        }

        ListIterator<V> itPrev = prevRoute.iterator();
        ListIterator<V> it = route.iterator();

        while (itPrev.hasNext()) {
            if (itPrev.next() != it.next()) {
                mCollisionsCount++;
                return;
            }
        }

    }

    /* (non-Javadoc)
     * @see vroom.common.modeling.util.IRoutePool#getAllRoutes()
     */
    @Override
    public synchronized List<ImmutableRoute<V>> getAllRoutes() {
        ArrayList<ImmutableRoute<V>> routes = new ArrayList<ImmutableRoute<V>>(size());

        for (int i = 0; i < mRoutePool.length; i++) {
            routes.addAll(getPool(i).values());
        }

        return routes;
    }

    /*
     * (non-Javadoc)
     * @see vroom.trsp.datamodel.RPool#size()
     */

    @Override
    public int size() {
        return mSize;
    }

    /* (non-Javadoc)
     * @see vroom.common.modeling.util.IRoutePool#clear()
     */
    @Override
    public synchronized void clear() {
        for (int i = 0; i < mRoutePool.length; i++) {
            getPool(i).clear();
        }
        mSize = 0;
    }

    /* (non-Javadoc)
     * @see vroom.common.modeling.util.IRoutePool#dispose()
     */
    @Override
    public synchronized void dispose() {
        clear();
    }

    /* (non-Javadoc)
     * @see vroom.common.modeling.util.IRoutePool#iterator()
     */
    @Override
    public Iterator<ImmutableRoute<V>> iterator() {
        return getAllRoutes().iterator();
    }

    @Override
    public HashRoutePool<V> clone() {
        return new HashRoutePool<>(this);
    }
}
