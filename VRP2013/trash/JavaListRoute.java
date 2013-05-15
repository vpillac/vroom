/**
 * 
 */
package vrp2013.datamodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * <code>JavaListRoute</code> is a base implementation of {@link Route} based on an instance of {@link List}
 * <p>
 * Creation date: 09/04/2013 - 9:09:59 PM
 * 
 * @author Victor Pillac, <a href="http://www.nicta.com.au">National ICT Australia</a>, <a
 *         href="http://www.victorpillac.com">www.victorpillac.com</a>
 * @version 1.0
 */
public abstract class JavaListRoute<L extends List<Integer>> extends Route {

    private final L mList;

    JavaListRoute(int vehicle, L list) {
        super(vehicle);
        mList = list;
    }

    /* (non-Javadoc)
     * @see vrp2013.datamodel.Route#contains(int)
     */
    @Override
    public boolean contains(int node) {
        return mList.contains(node);
    }

    /* (non-Javadoc)
     * @see vrp2013.datamodel.Route#getNodeSequence()
     */
    @Override
    public List<Integer> getNodeSequence() {
        return Collections.unmodifiableList(mList);
    }

    /* (non-Javadoc)
     * @see vrp2013.datamodel.Route#insertNodesAt(java.util.List, int)
     */
    @Override
    public void insertNodesAt(List<Integer> subroute, int index) {
        mList.addAll(index, subroute);
    }

    /* (non-Javadoc)
     * @see vrp2013.datamodel.Route#insertNodeAt(int, int)
     */
    @Override
    public void insertNodeAt(int node, int index) {
        mList.add(index, node);
    }

    /* (non-Javadoc)
     * @see vrp2013.datamodel.Route#remove(int)
     */
    @Override
    public void remove(int node) {
        mList.remove((Integer) node);
    }

    /* (non-Javadoc)
     * @see vrp2013.datamodel.Route#getSubroute(int, int)
     */
    @Override
    public List<Integer> getSubroute(int start, int end) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see vrp2013.datamodel.Route#extractSubroute(int, int)
     */
    @Override
    public List<Integer> extractSubroute(int start, int end) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see vrp2013.datamodel.Route#getNodeAt(int)
     */
    @Override
    public int getNodeAt(int index) {
        return mList.get(index);
    }

    /* (non-Javadoc)
     * @see vrp2013.datamodel.Route#setNodeAt(int, int)
     */
    @Override
    public int setNodeAt(int index, int node) {
        return mList.set(index, node);
    }

    /* (non-Javadoc)
     * @see vrp2013.datamodel.Route#extractNodeAt(int)
     */
    @Override
    public int extractNodeAt(int index) {
        return mList.remove(index);
    }

    /* (non-Javadoc)
     * @see vrp2013.datamodel.Route#append(java.util.List)
     */
    @Override
    public void append(List<Integer> nodes) {
        mList.addAll(nodes);
    }

    /* (non-Javadoc)
     * @see vrp2013.datamodel.Route#append(int)
     */
    @Override
    public void append(int node) {
        mList.add(node);
    }

    /* (non-Javadoc)
     * @see vrp2013.datamodel.Route#iterator()
     */
    @Override
    public ListIterator<Integer> iterator() {
        return mList.listIterator();
    }

    /* (non-Javadoc)
     * @see vrp2013.datamodel.Route#length()
     */
    @Override
    public int length() {
        return mList.size();
    }

    /* (non-Javadoc)
     * @see vrp2013.datamodel.Route#getLastNode()
     */
    @Override
    public int getLastNode() {
        return mList.get(length() - 1);
    }

    /* (non-Javadoc)
     * @see vrp2013.datamodel.Route#getFirstNode()
     */
    @Override
    public int getFirstNode() {
        return mList.get(0);
    }

    /* (non-Javadoc)
     * @see vrp2013.datamodel.Route#getNodePosition(int)
     */
    @Override
    public int getNodePosition(int node) {
        return mList.indexOf(node);
    }

    /* (non-Javadoc)
     * @see vrp2013.datamodel.Route#clone()
     */
    @Override
    public abstract JavaListRoute<L> clone();

    /**
     * <code>ArrayListRoute</code> is an implementation of {@link Route} based on an instance of {@link ArrayList}
     * <p>
     * Creation date: 09/04/2013 - 9:17:16 PM
     * 
     * @author Victor Pillac, <a href="http://www.nicta.com.au">National ICT Australia</a>, <a
     *         href="http://www.victorpillac.com">www.victorpillac.com</a>
     * @version 1.0
     */
    public static class ArrayListRoute extends JavaListRoute<ArrayList<Integer>> {

        public ArrayListRoute(int vehicle) {
            super(vehicle, new ArrayList<Integer>());
        }

        private ArrayListRoute(int vehicle, ArrayList<Integer> list) {
            super(vehicle, list);
        }

        @Override
        public ArrayListRoute clone() {
            return new ArrayListRoute(getVehicle(), super.mList);
        }

    }

    /**
     * <code>ArrayListRoute</code> is an implementation of {@link Route} based on an instance of {@link LinkedList}
     * <p>
     * Creation date: 09/04/2013 - 9:17:16 PM
     * 
     * @author Victor Pillac, <a href="http://www.nicta.com.au">National ICT Australia</a>, <a
     *         href="http://www.victorpillac.com">www.victorpillac.com</a>
     * @version 1.0
     */
    public static class LinkedListRoute extends JavaListRoute<LinkedList<Integer>> {

        public LinkedListRoute(int vehicle) {
            super(vehicle, new LinkedList<Integer>());
        }

        private LinkedListRoute(int vehicle, LinkedList<Integer> list) {
            super(vehicle, list);
        }

        @Override
        public LinkedListRoute clone() {
            return new LinkedListRoute(getVehicle(), super.mList);
        }

    }
}
