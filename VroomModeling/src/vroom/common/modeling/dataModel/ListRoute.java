package vroom.common.modeling.dataModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * <code>ListRoute</code> is an extension of {@link RouteBase} that use a {@link List} to store the sequence of
 * {@link INodeVisit}.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #created 15-Feb-2010 11:29:49 a.m.
 */
public abstract class ListRoute extends RouteBase {

    /** The m visits. */
    final List<INodeVisit> mNodes;

    /**
     * Creates a new <code>ArrayListRoute</code> associated with the given <code>parentSolution</code> and
     * <code>vehicle</code>.
     * 
     * @param parentSolution
     *            the {@link Solution} that contains this route
     * @param vehicle
     *            the {@link Vehicle} associated wiht this solution
     * @see RouteBase#RouteBase(Solution, Vehicle)
     */
    public ListRoute(IVRPSolution<?> parentSolution, Vehicle vehicle) {
        super(parentSolution, vehicle);
        mNodes = newList();
    }

    abstract List<INodeVisit> newList();

    /*
     * (non-Javadoc)
     * 
     * @see
     * vroom.common.modeling.dataModel.RouteBase#appendNodeImplem(vroom.modelling
     * .VroomModelling.dataModel.INodeVisit)
     */
    @Override
    protected synchronized boolean appendNodeImplem(INodeVisit node) {
        return mNodes.add(node);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * vroom.common.modeling.dataModel.RouteBase#appendImplem(java.util.List)
     */
    @Override
    protected boolean appendNodesImplem(List<? extends INodeVisit> nodes) {
        return mNodes.addAll(nodes);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * vroom.common.modeling.dataModel.RouteBase#appendRouteImplem(vroom.modelling
     * .VroomModelling.dataModel.IRoute)
     */
    @Override
    protected boolean appendRouteImplem(IRoute<? extends INodeVisit> appendedRoute) {
        return mNodes.addAll(appendedRoute.getNodeSequence());
    }

    @Override
    public boolean contains(INodeVisit node) {
        return mNodes.contains(node);
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.RouteBase#extractNodeImplem(int)
     */
    @Override
    protected synchronized INodeVisit[] extractNodeImplem(int index) {
        INodeVisit[] r = new INodeVisit[] { index > 0 ? getNodeAt(index - 1) : null, null,
                index < length() - 1 ? getNodeAt(index + 1) : null };
        r[1] = mNodes.remove(index);
        return r;
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.RouteBase#extractSubrouteImplem(int,
     * int)
     */
    @Override
    protected synchronized Object[] extractNodesImplem(int start, int end) {
        Object[] r = new Object[] { start > 0 ? getNodeAt(start - 1) : null, null,
                end < length() - 1 ? getNodeAt(end + 1) : null };

        // Creates a copy of the sublist
        r[1] = new ArrayList<INodeVisit>(mNodes.subList(start, end + 1));

        // Remove the range
        mNodes.subList(start, end + 1).clear();

        return r;
    }

    @Override
    public INodeVisit getFirstNode() {
        return length() > 0 ? getNodeAt(0) : null;
    }

    @Override
    public INodeVisit getLastNode() {
        return length() > 0 ? getNodeAt(length() - 1) : null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.RouteBase#getNodeAtImplem(int)
     */
    @Override
    protected synchronized INodeVisit getNodeAtImplem(int index) {
        return mNodes.get(index);
    }

    @Override
    public int getNodePosition(INodeVisit node) {
        return mNodes.indexOf(node);
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.RouteBase#getNodeSequence()
     */
    @Override
    public List<INodeVisit> getNodeSequence() {
        return new ArrayList<INodeVisit>(mNodes);
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.RouteBase#insertNodeImplem(int,
     * vroom.common.modeling.dataModel.INodeVisit)
     */
    @Override
    protected synchronized INodeVisit[] insertNodeImplem(int index, INodeVisit node) {
        INodeVisit[] r = new INodeVisit[] { index > 0 ? getNodeAt(index - 1) : null,
                index < length() ? getNodeAt(index) : null };
        mNodes.add(index, node);
        return r;
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.RouteBase#insertSubrouteImplem(int,
     * java.util.List)
     */
    @Override
    protected synchronized INodeVisit[] insertNodesImplem(int index,
            List<? extends INodeVisit> subroute) {
        INodeVisit[] r = new INodeVisit[] { index > 0 ? getNodeAt(index - 1) : null,
                index < length() ? getNodeAt(index) : null };
        mNodes.addAll(index, subroute);
        return r;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public synchronized ListIterator<INodeVisit> iterator() {
        return mNodes.listIterator();
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.RouteBase#length()
     */
    @Override
    public synchronized int length() {
        return mNodes.size();
    }

    @Override
    protected synchronized INodeVisit[] removeImplem(INodeVisit node) {
        int idx = mNodes.indexOf(node);
        if (idx == -1) {
            return null;
        }

        INodeVisit[] neigh = new INodeVisit[2];
        neigh[0] = idx > 0 ? getNodeAt(idx - 1) : null;
        neigh[1] = idx < length() - 1 ? getNodeAt(idx + 1) : null;

        mNodes.remove(idx);

        return neigh;
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.RouteBase#revertSubRouteImplem(int,
     * int)
     */
    @Override
    protected synchronized INodeVisit[] reverseSubrouteImplem(int start, int end) {
        INodeVisit[] r = new INodeVisit[] { start > 0 ? getNodeAt(start - 1) : null,
                getNodeAt(start), getNodeAt(end), end < length() - 1 ? getNodeAt(end + 1) : null };
        Collections.reverse(mNodes.subList(start, end + 1));
        return r;
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.RouteBase#setNodeAtImplem(int,
     * vroom.common.modeling.dataModel.INodeVisit)
     */
    @Override
    protected synchronized INodeVisit[] setNodeAtImplem(int index, INodeVisit node) {
        INodeVisit[] r = new INodeVisit[] { index > 0 ? getNodeAt(index - 1) : null, null,
                index < length() - 1 ? getNodeAt(index + 1) : null };
        r[1] = mNodes.set(index, node);
        return r;
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.RouteBase#subrouteImplem(int, int)
     */
    @Override
    protected synchronized List<INodeVisit> subrouteImplem(int start, int end) {
        return new ArrayList<INodeVisit>(mNodes.subList(start, end + 1));
    }

    /**
     * The class <code>ArrayListRoute</code> is an extension of {@link ListRoute} that relies on an instance of
     * {@link ArrayList} to store the list of node visits.
     * <p>
     * Creation date: Apr 30, 2013 - 1:14:11 PM
     * 
     * @author vpillac, <a href="http://www.nicta.com.au">National ICT Australia</a>
     * @version 1.0
     */
    public static class ArrayListRoute extends ListRoute {

        @Override
        List<INodeVisit> newList() {
            return new ArrayList<INodeVisit>();
        }

        public ArrayListRoute(IVRPSolution<?> parentSolution, Vehicle vehicle) {
            super(parentSolution, vehicle);
        }

        @Override
        public ArrayListRoute clone() {
            ArrayListRoute clone = new ArrayListRoute(getParentSolution(), getVehicle());

            clone.appendRoute(this);

            return clone;
        }

        /*
         * (non-Javadoc)
         * 
         * @see vroom.common.modeling.dataModel.RouteBase#extractSubRoute(int,
         * int)
         */
        @SuppressWarnings("unchecked")
        @Override
        protected Object[] extractSubrouteImplem(int start, int end) {
            ArrayListRoute result = new ArrayListRoute(getParentSolution(), getVehicle());

            Object[] r = extractNodesImplem(start, end);
            result.appendNodes((List<INodeVisit>) r[1]);
            r[1] = result;
            return r;
        }
    }

    /**
     * The class <code>LinkedListRoute</code> is an extension of {@link ListRoute} that relies on an instance of
     * {@link LinkedList} to store the list of node visits.
     * <p>
     * Creation date: Apr 30, 2013 - 1:15:43 PM
     * 
     * @author vpillac, <a href="http://www.nicta.com.au">National ICT Australia</a>
     * @version 1.0
     */
    public static class LinkedListRoute extends ListRoute {
        @Override
        List<INodeVisit> newList() {
            return new LinkedList<>();
        }

        public LinkedListRoute(IVRPSolution<?> parentSolution, Vehicle vehicle) {
            super(parentSolution, vehicle);
        }

        @Override
        public LinkedListRoute clone() {
            LinkedListRoute clone = new LinkedListRoute(getParentSolution(), getVehicle());

            clone.appendRoute(this);

            return clone;
        }

        /*
         * (non-Javadoc)
         * 
         * @see vroom.common.modeling.dataModel.RouteBase#extractSubRoute(int,
         * int)
         */
        @SuppressWarnings("unchecked")
        @Override
        protected Object[] extractSubrouteImplem(int start, int end) {
            LinkedListRoute result = new LinkedListRoute(getParentSolution(), getVehicle());

            Object[] r = extractNodesImplem(start, end);
            result.appendNodes((List<INodeVisit>) r[1]);
            r[1] = result;
            return r;
        }

        /*
         * (non-Javadoc)
         * 
         * @see vroom.common.modeling.dataModel.RouteBase#extractNodeImplem(int)
         */
        @Override
        protected synchronized INodeVisit[] extractNodeImplem(int index) {
            if (index == 0) {
                INodeVisit[] nodes = new INodeVisit[3];
                nodes[1] = ((LinkedList<INodeVisit>) mNodes).removeFirst();
                nodes[2] = mNodes.isEmpty() ? null : ((LinkedList<INodeVisit>) mNodes).getFirst();
                return nodes;
            } else if (index == length() - 1) {
                INodeVisit[] nodes = new INodeVisit[3];
                nodes[1] = ((LinkedList<INodeVisit>) mNodes).removeLast();
                nodes[0] = mNodes.isEmpty() ? null : ((LinkedList<INodeVisit>) mNodes).getLast();
                return nodes;
            } else {
                return super.extractNodeImplem(index);
            }
        }
    }

}// end ArrayListRoute