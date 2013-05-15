/*
 * National ICT Australia - http://www.nicta.com.au - All Rights Reserved
 */
/**
 * 
 */
package vroom.common.modeling.dataModel;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import vroom.common.modeling.util.IRoutePool;

/**
 * The class <code>ListRoutePool</code> is an implementation of {@link IRoutePool} that stores all routes in a list
 * <p>
 * Creation date: May 4, 2013 - 2:30:24 PM
 * 
 * @author vpillac, <a href="http://www.nicta.com.au">National ICT Australia</a>
 * @version 1.0
 */
public class ListRoutePool<V extends INodeVisit> implements IRoutePool<V> {

    private final LinkedList<ImmutableRoute<V>> mPool;

    public ListRoutePool() {
        mPool = new LinkedList<>();
    }

    @Override
    public void dispose() {
        clear();
    }

    @Override
    public Iterator<ImmutableRoute<V>> iterator() {
        return getAllRoutes().iterator();
    }

    @Override
    public int getCollisionsCount() {
        return 0;
    }

    @Override
    public int add(Iterable<? extends IRoute<V>> routes) {
        int count = 0;
        for (IRoute<V> r : routes) {
            count++;
            mPool.add(new ImmutableRoute<>(r, r.hashCode()));
        }

        return count;
    }

    @Override
    public List<ImmutableRoute<V>> getAllRoutes() {
        return Collections.unmodifiableList(mPool);
    }

    @Override
    public void clear() {
        mPool.clear();
    }

    @Override
    public int size() {
        return mPool.size();
    }

    @Override
    public ListRoutePool<V> clone() {
        ListRoutePool<V> clone = new ListRoutePool<>();
        clone.mPool.addAll(this.mPool);
        return clone;
    }

}
