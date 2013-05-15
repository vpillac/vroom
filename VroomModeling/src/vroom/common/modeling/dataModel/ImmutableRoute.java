/*
 * National ICT Australia - http://www.nicta.com.au - All Rights Reserved
 */
/**
 * 
 */
package vroom.common.modeling.dataModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

/**
 * The class <code>ImmutableRoute</code> is a minimalist implementation of {@link IRoute} that stores an array of
 * {@link INodeVisit}
 * <p>
 * Creation date: May 2, 2013 - 5:19:47 PM
 * 
 * @author vpillac, <a href="http://www.nicta.com.au">National ICT Australia</a>
 * @version 1.0
 */
public class ImmutableRoute<V extends INodeVisit> implements IRoute<V> {

    private final Vehicle  mVehicle;

    private final double   mCost;
    private final double[] mLoads;

    private final List<V>  mNodes;

    private final int      mHash;

    /**
     * Creates a new <code>ImmutableRoute</code>
     * 
     * @param route
     * @param hash
     * @author vpillac
     */
    public ImmutableRoute(IRoute<V> route, int hash) {
        mHash = hash;
        mVehicle = route.getVehicle();
        mCost = route.getCost();
        double[] l = route.getLoads();
        mLoads = Arrays.copyOf(l, l.length);

        ArrayList<V> nodes = new ArrayList<>(route.length());
        for (V n : route)
            nodes.add(n);
        mNodes = Collections.unmodifiableList(nodes);
    }

    @Override
    public IVRPSolution<?> getParentSolution() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Vehicle getVehicle() {
        return mVehicle;
    }

    @Override
    public double getCost() {
        return mCost;
    }

    @Override
    public void calculateCost(boolean force) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateCost(double delta) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateLoad(int product, double delta) {
        throw new UnsupportedOperationException();

    }

    @Override
    public synchronized final double getLoad() {
        return mLoads[0];
    }

    @Override
    public synchronized final double getLoad(int product) {
        return mLoads[product];
    }

    @Override
    public synchronized final double[] getLoads() {
        return Arrays.copyOf(mLoads, mLoads.length);
    }

    @Override
    public void calculateLoad(boolean force) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canAccommodateRequest(IVRPRequest request) {
        return false;
    }

    @Override
    public V getNodeAt(int index) {
        return mNodes.get(index);
    }

    @Override
    public int getNodePosition(INodeVisit node) {
        return mNodes.indexOf(node);
    }

    @Override
    public V getFirstNode() {
        return mNodes.get(0);
    }

    @Override
    public V getLastNode() {
        return mNodes.get(mNodes.size() - 1);
    }

    @Override
    public List<V> subroute(int start, int end) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean appendNode(V node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean appendRoute(IRoute<? extends V> appendedRoute) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean appendNodes(List<? extends V> node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V setNodeAt(int index, V node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V extractNode(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IRoute<V> extractSubroute(int start, int end) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<V> extractNodes(int start, int end) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NodeInsertion getBestNodeInsertion(INodeVisit node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NodeInsertion getBestNodeInsertion(INodeVisit node, int min, int max) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean bestInsertion(V node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean insertNode(int index, V node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean insertNode(NodeInsertion ins, V node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean insertSubroute(int index, IRoute<? extends V> subroute) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean insertNodes(int index, List<? extends V> nodes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean swapNodes(int node1, int node2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reverseSubRoute(int start, int end) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reverseRoute() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int length() {
        return mNodes.size();
    }

    @Override
    public List<V> getNodeSequence() {
        return mNodes;
    }

    @Override
    public boolean contains(INodeVisit node) {
        return mNodes.contains(node);
    }

    @Override
    public boolean remove(INodeVisit node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListIterator<V> iterator() {
        return mNodes.listIterator();
    }

    @Override
    public String getNodeSeqString() {
        StringBuilder sb = new StringBuilder(length() * 3);

        sb.append("<");

        for (INodeVisit v : this) {
            sb.append(v.getID());
            sb.append(',');
        }
        if (length() > 0) {
            sb.setCharAt(sb.length() - 1, '>');
        } else {
            sb.append('>');
        }

        return sb.toString();
    }

    @Override
    public synchronized String toString() {
        StringBuilder b = new StringBuilder(length() * 5);

        b.append(String.format("cost:%.2f", getCost()));
        b.append(" length:");
        b.append(length());
        b.append(" load:");
        b.append(Arrays.toString(getLoads()));
        b.append(" ");
        b.append(getNodeSeqString());

        return b.toString();
    }

    @Override
    public ImmutableRoute<V> clone() {
        return new ImmutableRoute<>(this, mHash);
    }

    @Override
    public int hashCode() {
        return mHash;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ImmutableRoute<?> && ((ImmutableRoute<?>) obj).mHash == this.mHash;
    }

}
