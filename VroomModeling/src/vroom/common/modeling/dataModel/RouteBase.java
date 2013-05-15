package vroom.common.modeling.dataModel;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import vroom.common.modeling.util.DefaultRouteCostDelegate;
import vroom.common.modeling.util.IRouteCostDelegate;
import vroom.common.modeling.util.SolutionChecker;

/**
 * <code>RouteBase</code> is a base type for classes used to represent a route.
 * <p>
 * In this implementation a route is modeled as a sequence of {@link INodeVisit} that are used to add additional
 * information on the actual serving of a request at a node by a vehicle.
 * </p>
 * <p>
 * The Class <code>RouteBase</code> defines a set of basic operations that are used by common optimization to modify a
 * route, with for instance the insertion or removal of single {@link INodeVisit} or subroutes.
 * </p>
 * 
 * @see Vehicle
 * @see INodeVisit
 * @see Node
 * @see Request
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 - created 15-Feb-2010 11:29:51 a.m.
 */
public abstract class RouteBase implements IRoute<INodeVisit> {

    private static boolean checkRoute   = false;

    /** The m cost changed. */
    private boolean        mCostChanged = true;

    /** The m cap changed. */
    private boolean        mCapChanged  = true;

    /**
     * Sets the changed flags to true.
     */
    protected synchronized final void setChanged() {
        mCostChanged = true;
        mCapChanged = true;
    }

    /** The cost. */
    private double             mCost;

    /** The loads */
    private final double[]     mLoads;

    /** The parent solution. */
    private IVRPSolution<?>    mParentSolution;

    /** The vehicle. */
    private final Vehicle      mVehicle;

    /** the route cost calculation delegate **/
    private IRouteCostDelegate mCostDelegate;

    /**
     * Getter for the route cost calculation delegate
     * 
     * @return the cost delegate
     */
    public IRouteCostDelegate getCostDelegate() {
        return this.mCostDelegate;
    }

    /**
     * Setter for the route cost calculation delegate.
     * <p>
     * The route cost will automatically be re-evaluated
     * </p>
     * 
     * @param delegate
     *            the value to be set for the route cost calculation delegate
     */
    public void setCostDelegate(IRouteCostDelegate delegate) {
        if (delegate == null)
            throw new IllegalArgumentException("The cost delegate cannot be null");
        this.mCostDelegate = delegate;
        this.mCostDelegate.evaluateRoute(this);
    }

    /**
     * Creates a new <code>VRPRoute</code> associated with the given <code>parentSolution</code> and
     * <code>vehicle</code>.
     * 
     * @param parentSolution
     *            the {@link Solution} that contains this route
     * @param vehicle
     *            the {@link Vehicle} associated wiht this solution
     */
    public RouteBase(IVRPSolution<?> parentSolution, Vehicle vehicle) {
        super();
        mParentSolution = parentSolution;
        mVehicle = vehicle;

        mLoads = new double[getVehicle().getCompartmentCount()];
        mCostDelegate = new DefaultRouteCostDelegate(parentSolution.getParentInstance().getCostDelegate());
        setChanged();
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.Route#getParentSolution()
     */
    @Override
    public IVRPSolution<?> getParentSolution() {
        return mParentSolution;
    }

    /**
     * Changes the parent solution of this route.
     * <p>
     * Useful to copy a route to a new solution without copying the actual information
     * </p>
     * <p>
     * Warning: This method will remove this route from the current parent solution, but will not add it to the new one
     * </p>
     * 
     * @param parentSolution
     *            the new parent solution
     */
    public void changeParentSolution(IVRPSolution<?> parentSolution) {
        if (parentSolution == null) {
            throw new IllegalArgumentException("Argument parentSolution cannot be null");
        }

        if (mParentSolution != null) {
            mParentSolution.removeRoute(this);
        }
        mParentSolution = parentSolution;
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.Route#getVehicle()
     */
    @Override
    public Vehicle getVehicle() {
        return mVehicle;
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.Route#getCost()
     */
    @Override
    public synchronized final double getCost() {
        // calculateCost(false);

        return mCost;
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.Route#calculateCost(boolean)
     */
    @Override
    public final void calculateCost(boolean force) {
        if (force || mCostChanged) {
            mCost = 0;

            if (length() > 1) {
                Iterator<INodeVisit> it = iterator();

                INodeVisit pred = it.next();

                while (it.hasNext()) {
                    INodeVisit succ = it.next();

                    mCost += getArcCost(pred, succ);

                    pred = succ;
                }
            }
            mCostChanged = false;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.Route#updateCost(double)
     */
    @Override
    public final synchronized void updateCost(double delta) {
        mCost += delta;
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.Route#getRemainingCapacity()
     */
    @Override
    public synchronized final double getLoad() {
        // calculateLoad(false);
        return mLoads[0];
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.Route#getRemainingCapacity(int)
     */
    @Override
    public synchronized final double getLoad(int product) {
        // calculateLoad(false);
        return mLoads[product];
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.Route#getRemainingCapacities()
     */
    @Override
    public synchronized final double[] getLoads() {
        // calculateLoad(false);
        return Arrays.copyOf(mLoads, mLoads.length);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * vroom.common.modeling.dataModel.Route#calculateRemaingCapacities(boolean
     * )
     */
    @Override
    public final synchronized void calculateLoad(boolean force) {
        if (force || mCapChanged) {
            for (int i = 0; i < mLoads.length; i++) {
                mLoads[i] = 0;
                for (INodeVisit v : this) {
                    if (v.getParentRequest() != null) {
                        mLoads[i] += v.getDemand(i);
                    }
                }
            }

            mCapChanged = false;
        }
    }

    /**
     * Changes the stored load by a given delta.
     * 
     * @param product
     *            the considered product
     * @param delta
     *            the value to be added to the currently stored load
     */
    @Override
    public final synchronized void updateLoad(int product, double delta) {
        mLoads[product] += delta;
    }

    /**
     * Changes the stored load by a given delta.
     * 
     * @param isInsertion
     * @param nodes
     *            the nodes
     */
    final public synchronized void updateLoad(boolean isInsertion, INodeVisit... nodes) {
        double sign = isInsertion ? 1 : -1;
        for (INodeVisit n : nodes) {
            for (int p = 0; p < mLoads.length; p++) {
                double demand = n.getDemand(p);
                updateLoad(p, sign * demand);
            }
        }

    }

    /**
     * Changes the stored load by a given delta.
     * 
     * @param isInsertion
     * @param nodes
     *            the nodes
     */
    final public synchronized void updateLoad(boolean isInsertion, Collection<? extends INodeVisit> nodes) {
        double sign = isInsertion ? 1 : -1;
        for (INodeVisit n : nodes) {
            for (int p = 0; p < mLoads.length; p++) {
                double demand = n.getDemand(p);
                updateLoad(p, sign * demand);
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * vroom.common.modeling.dataModel.Route#canAccommodateRequest(vroom.modelling
     * .VroomModelling.dataModel.Request)
     */
    @Override
    public boolean canAccommodateRequest(IVRPRequest request) {
        calculateLoad(false);

        for (int p = 0; p < mLoads.length; p++) {
            if (getLoad(p) + request.getDemand(p) > getVehicle().getCapacity(p)) {
                return false;
            }
        }

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.Route#getNodeAt(int)
     */
    @Override
    public synchronized INodeVisit getNodeAt(int index) {
        // Check indices
        String e = checkIndex(index, "index", false);
        if (e != null) {
            throw new IllegalArgumentException(e);
        }

        return getNodeAtImplem(index);
    }

    /**
     * Implementation of {@link #getNodeAt(int)}<br/>
     * Index do not need to be checked in this method as it is done in the parent method.
     * 
     * @param index
     *            the index of the desired vertex
     * @return the that is at position
     */
    protected abstract INodeVisit getNodeAtImplem(int index);

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.Route#subroute(int, int)
     */
    @Override
    public synchronized List<INodeVisit> subroute(int start, int end) {
        // Check indices
        String e = checkSequenceIndexes(start, "start", end, "end", false);
        if (e != null) {
            throw new IllegalArgumentException(e);
        }

        return subrouteImplem(start, end);
    }

    /**
     * Implementation of {@link #subroute(int, int)} Indexes do not need to be checked in this method as it is done in
     * the parent method.
     * 
     * @param start
     *            the position of the first node of the subroute
     * @param end
     *            the position of the last node of the subroute
     * @return a list containing the nodes in the positions between
     */
    protected abstract List<INodeVisit> subrouteImplem(int start, int end);

    /*
     * Route manipulation methods
     */
    /*
     * (non-Javadoc)
     * 
     * @see
     * vroom.common.modeling.dataModel.Route#appendNode(vroom.common.modeling.
     * dataModel.INodeVisit)
     */
    @Override
    public boolean appendNode(INodeVisit node) {
        INodeVisit last = getLastNode();
        boolean b = appendNodeImplem(node);

        if (b) {
            updateLoad(true, node);
            getCostDelegate().nodeInserted(this, last, node, null);
        }

        if (checkRoute) {
            String err = SolutionChecker.checkRoute(this, false, false, false);
            if (err != null) {
                System.out.println("RouteBase.appendNode: " + err);
            }
        }
        return b;
    }

    /**
     * Implementation of {@link #appendNode(INodeVisit)}.
     * 
     * @param node
     *            the node to be added at the end of the route
     * @return if the node was successfully added, otherwise
     */
    protected abstract boolean appendNodeImplem(INodeVisit node);

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.IRoute#append(java.util.List)
     */
    @Override
    public boolean appendNodes(List<? extends INodeVisit> nodes) {
        if (nodes.isEmpty()) {
            return true;
        }

        INodeVisit last = getLastNode();

        boolean b = appendNodesImplem(nodes);

        if (b) {
            updateLoad(true, nodes);
            getCostDelegate().routeInserted(this, last, nodes, null);
        }

        if (checkRoute) {
            String err = SolutionChecker.checkRoute(this, false, false, false);
            if (err != null) {
                System.out.println("RouteBase.appendNodes: " + err);
            }
        }

        return b;
    }

    /**
     * Implementation of {@link #appendNodes(List)}.
     * 
     * @param nodes
     *            the nodes to be added at the end of the route
     * @return <code>true</code> if the nodes were successfully added,
     */
    protected abstract boolean appendNodesImplem(List<? extends INodeVisit> nodes);

    /*
     * (non-Javadoc)
     * 
     * @see
     * vroom.common.modeling.dataModel.IRoute#appendRoute(vroom.common.modeling
     * .dataModel.IRoute)
     */
    @Override
    public boolean appendRoute(IRoute<? extends INodeVisit> appendedRoute) {
        // Ignore empty route
        if (appendedRoute.length() == 0) {
            return true;
        }

        INodeVisit last = getLastNode();

        boolean b = appendRouteImplem(appendedRoute);

        if (b) {
            // Update the load
            for (int p = 0; p < mLoads.length; p++) {
                updateLoad(p, appendedRoute.getLoad(p));
            }
            getCostDelegate().routeInserted(this, last, appendedRoute, null);
        }
        if (checkRoute) {
            String err = SolutionChecker.checkRoute(this, false, false, false);
            if (err != null) {
                System.out.println("RouteBase.appendRoute: " + err);
            }
        }

        return b;
    }

    /**
     * Implementation of {@link #appendRoute(IRoute)}
     * 
     * @param appendedRoute
     *            the route to be appended to this route
     * @return <code>true</code>if the route was successfully appended
     */
    protected abstract boolean appendRouteImplem(IRoute<? extends INodeVisit> appendedRoute);

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.Route#setNodeAt(int,
     * vroom.common.modeling.dataModel.INodeVisit)
     */
    @Override
    public synchronized final INodeVisit setNodeAt(int index, INodeVisit node) {
        String e = checkIndex(index, "index", true);
        if (e != null) {
            throw new IllegalArgumentException(e);
        }

        INodeVisit[] r = setNodeAtImplem(index, node);

        updateLoad(false, r[1]);
        updateLoad(true, node);
        getCostDelegate().nodeReplaced(this, r[0], r[1], node, r[2]);

        setChanged();
        if (checkRoute) {
            String err = SolutionChecker.checkRoute(this, false, false, false);
            if (err != null) {
                System.out.println("RouteBase.setNodeAt: " + err);
            }
        }

        return r[1];
    }

    /**
     * Implementation of {@link #setNodeAt(int, INodeVisit)}<br/>
     * Indexes do not need to be checked in this method as it is done in the parent method.
     * 
     * @param index
     *            the index of the node to be set
     * @param node
     *            the node that will be visited at position <code>index</code>
     * @return an array <code>[predecessor,previousNode,successor]</code>
     */
    protected abstract INodeVisit[] setNodeAtImplem(int index, INodeVisit node);

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.Route#extractNode(int)
     */
    @Override
    public final INodeVisit extractNode(int index) {
        // Check indices
        String e = checkIndex(index, "index", false);
        if (e != null) {
            throw new IllegalArgumentException(e);
        }

        INodeVisit[] r = extractNodeImplem(index);
        INodeVisit node = r[1];

        updateLoad(false, node);

        getCostDelegate().nodeRemoved(this, r[0], node, r[2]);

        setChanged();
        if (checkRoute) {
            String err = SolutionChecker.checkRoute(this, false, false, false);
            if (err != null) {
                System.out.println("RouteBase.bestInsertion: " + err);
            }
        }
        return node;
    }

    /**
     * A default implementation of {@link #extractNodes(int, int)}.<br/>
     * Indexes do not need to be checked in this method as it is done in the parent method
     * 
     * @param index
     *            the index of the node to be extracted
     * @return an array containing <code>[pred,node,succ]</code>
     */
    protected abstract INodeVisit[] extractNodeImplem(int index);

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.IRoute#extractSubRoute(int, int)
     */
    @Override
    public final RouteBase extractSubroute(int start, int end) {
        // Check indices
        String e = checkSequenceIndexes(start, "start", end, "end", false);
        if (e != null) {
            throw new IllegalArgumentException(e);
        }

        Object[] r = extractSubrouteImplem(start, end);

        RouteBase route = (RouteBase) r[1];

        getCostDelegate().subrouteRemoved(this, (INodeVisit) r[0], route, (INodeVisit) r[2]);

        for (int p = 0; p < mLoads.length; p++) {
            updateLoad(p, -route.getLoad(p));
        }

        setChanged();
        if (checkRoute) {
            String err = SolutionChecker.checkRoute(this, false, false, false);
            if (err != null) {
                System.out.println("RouteBase.extractSubroute: " + err);
            }
        }

        return route;

    }

    /**
     * A default implementation of {@link #extractSubroute(int, int)}.<br/>
     * Indexes do not need to be checked in this method as it is done in the parent method
     * 
     * @param start
     *            the subroute start
     * @param end
     *            the subroute end
     * @return an array containing <code>[pred,subroute,succ]</code>
     */
    protected abstract Object[] extractSubrouteImplem(int start, int end);

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.Route#extractSubroute(int, int)
     */
    @Override
    public final List<INodeVisit> extractNodes(int start, int end) {
        // Check indices
        String e = checkSequenceIndexes(start, "start", end, "end", false);
        if (e != null) {
            throw new IllegalArgumentException(e);
        }

        setChanged();

        Object[] r = extractNodesImplem(start, end);

        @SuppressWarnings("unchecked")
        List<INodeVisit> nodes = (List<INodeVisit>) r[1];

        getCostDelegate().subrouteRemoved(this, (INodeVisit) r[0], nodes, (INodeVisit) r[2]);

        updateLoad(false, nodes);

        if (checkRoute) {
            String err = SolutionChecker.checkRoute(this, false, false, false);
            if (err != null) {
                System.out.println("RouteBase.extractNodes: " + err);
            }
        }

        return nodes;
    }

    /**
     * Implementation of {@link #extractNodes(int, int)}.<br/>
     * Indexes do not need to be checked in this method as it is done in the parent method
     * 
     * @param start
     *            the index of first node of the subroute
     * @param end
     *            the index of the last node of the subroute
     * @return an array containing <code>[INodeVisit pred, List&lt;INodeVisit&gt; subroute, INodeVisit succ]</code>
     */
    protected abstract Object[] extractNodesImplem(int start, int end);

    @Override
    public boolean remove(INodeVisit node) {
        INodeVisit[] neigh = removeImplem(node);

        if (neigh != null) {
            updateLoad(false, node);
            INodeVisit p = neigh[0];
            INodeVisit s = neigh[1];
            getCostDelegate().nodeRemoved(this, p, node, s);
            if (checkRoute) {
                String err = SolutionChecker.checkRoute(this, false, true, true);
                if (err != null) {
                    System.out.println("RouteBase.remove: " + err);
                }
            }
            return true;
        } else {
            return false;
        }

    }

    /**
     * Implementation of {@link #remove(INodeVisit)}
     * 
     * @param node
     *            the node to be removed
     * @return an array containing the predecessor and successor of the removed node, or <code>null</code> if the node
     *         was not in this route
     */
    protected abstract INodeVisit[] removeImplem(INodeVisit node);

    @Override
    public final NodeInsertion getBestNodeInsertion(INodeVisit node) {
        return getBestNodeInsertion(node, 0, length());
    }

    @Override
    public final NodeInsertion getBestNodeInsertion(INodeVisit node, int min, int max) {
        if (length() == 0) {
            return new NodeInsertion(node, 0, 0, this);
        }

        String s = checkSequenceIndexes(min, "min", max, "max", true);
        if (s != null) {
            throw new IllegalArgumentException(s);
        }
        if (min == max) {
            double cost = 0;
            if (min == 0) {
                cost = getParentSolution().getParentInstance().getCost(node, getFirstNode(), getVehicle());
            } else if (max == length()) {
                cost = getParentSolution().getParentInstance().getCost(getLastNode(), node, getVehicle());
            } else {
                cost = getParentSolution().getParentInstance().getInsertionCost(node, getNodeAt(min - 1),
                        getNodeAt(min), getVehicle());
            }
            return new NodeInsertion(node, cost, min, this);
        }

        double cost = Integer.MAX_VALUE;
        double c;
        int pos = -1;

        if (length() == 0) {
            cost = 0;
            pos = 0;
        } else if (length() == 1) {
            // Assume that the first node is a depot
            cost = getParentSolution().getParentInstance().getCost(getFirstNode(), node, getVehicle());
            pos = 1;
        } else {
            if (min == 0 && getFirstNode().isFixed()) {
                min = 1;
            }

            Iterator<INodeVisit> route = iterator();

            INodeVisit pred = null, succ = null;

            int index = 0;
            while (index < min) {
                pred = route.next();
                index++;
            }

            while (index <= max) {
                if (route.hasNext()) {
                    succ = route.next();
                } else {
                    succ = null;
                }

                if (pred == null) {
                    // First position insertion
                    c = getParentSolution().getParentInstance().getCost(node, succ, getVehicle());
                } else if (succ == null) {
                    // Last position insertion
                    if (!getLastNode().isFixed()) {
                        c = getParentSolution().getParentInstance().getCost(pred, node, getVehicle());
                    } else {
                        c = Double.MAX_VALUE;
                    }
                } else {
                    c = getParentSolution().getParentInstance().getInsertionCost(node, pred, succ, getVehicle());
                }

                if (c < cost) {
                    cost = c;
                    pos = index;
                }

                index++;
                pred = succ;

                if (succ == null) {
                    break;
                }
            }

        }

        return new NodeInsertion(node, cost, pos, this);
    }

    @Override
    public boolean bestInsertion(INodeVisit node) {
        NodeInsertion ins = getBestNodeInsertion(node);

        INodeVisit[] r = insertNodeImplem(ins.getPosition(), node);
        getCostDelegate().nodeInserted(this, r[0], node, r[1]);
        updateLoad(true, node);
        if (checkRoute) {
            String err = SolutionChecker.checkRoute(this, false, false, false);
            if (err != null) {
                System.out.println("RouteBase.bestInsertion: " + err);
            }
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.Route#insertNode(int,
     * vroom.common.modeling.dataModel.INodeVisit)
     */
    @Override
    public final boolean insertNode(int index, INodeVisit node) {
        // Check indices
        String e = checkIndex(index, "index", true);
        if (e != null) {
            throw new IllegalArgumentException(e);
        }

        INodeVisit pred = index > 0 ? getNodeAt(index - 1) : null;
        INodeVisit succ = index < length() - 1 ? getNodeAt(index + 1) : null;

        double delta = getParentSolution().getParentInstance().getInsertionCost(node, pred, succ, getVehicle());

        return insertNode(new NodeInsertion(node, delta, index, this), node);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * vroom.common.modeling.dataModel.Route#insertNode(vroom.common.modeling.
     * dataModel.RouteBase.NodeInsertion,
     * vroom.common.modeling.dataModel.INodeVisit)
     */
    @Override
    public final boolean insertNode(NodeInsertion ins, INodeVisit node) {
        INodeVisit[] r = insertNodeImplem(ins.getPosition(), node);

        getCostDelegate().nodeInserted(this, r[0], node, r[1]);
        updateLoad(true, node);
        if (checkRoute) {
            String err = SolutionChecker.checkRoute(this, false, false, false);
            if (err != null) {
                System.out.println("RouteBase.insertNode: " + err);
            }
        }
        return true;
    }

    /**
     * Implementation of the {@link #insertNode(int, INodeVisit)} method.<br/>
     * Indexes do not need to be checked in this method as it is done in the parent method
     * 
     * @param index
     *            the index at which the given node should be inserted.
     * @param node
     *            the node that will be visited at position <code>index</code>
     * @return an array <code>[predecessor,successor]</code>
     */
    protected abstract INodeVisit[] insertNodeImplem(int index, INodeVisit node);

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.IRoute#insertSubroute(int,
     * vroom.common.modeling.dataModel.IRoute)
     */
    @Override
    public boolean insertSubroute(int index, IRoute<? extends INodeVisit> subroute) {
        // Ignore empty routes
        if (subroute.length() == 0) {
            return true;
        }

        // Check indices
        String e = checkIndex(index, "index", true);
        if (e != null) {
            throw new IllegalArgumentException(e);
        }

        INodeVisit[] r = insertNodesImplem(index, subroute.getNodeSequence());

        // Update the load
        for (int p = 0; p < mLoads.length; p++) {
            updateLoad(p, subroute.getLoad(p));
        }

        getCostDelegate().routeInserted(this, r[0], subroute, r[1]);

        if (checkRoute) {
            String err = SolutionChecker.checkRoute(this, false, false, false);
            if (err != null) {
                System.out.println("RouteBase.insertSubroute: " + err);
            }
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.Route#insertSubroute(int,
     * java.util.List)
     */
    @Override
    public final boolean insertNodes(int index, List<? extends INodeVisit> subroute) {
        if (subroute.isEmpty()) {
            return true;
        }

        // Check indices
        String e = checkIndex(index, "index", true);
        if (e != null) {
            throw new IllegalArgumentException(e);
        }

        INodeVisit[] r = insertNodesImplem(index, subroute);

        // Update the load
        updateLoad(true, subroute);
        getCostDelegate().routeInserted(this, r[0], subroute, r[1]);

        setChanged();
        if (checkRoute) {
            String err = SolutionChecker.checkRoute(this, false, false, false);
            if (err != null) {
                System.out.println("RouteBase.insertNodes: " + err);
            }
        }
        return true;

    }

    /**
     * Implementation of {@link #insertNodes(int, List)}.<br/>
     * Indexes do not need to be checked in this method as it is done in the parent method
     * 
     * @param index
     *            the position at which the <code>subroute</code> will be inserted.
     * @param subroute
     *            the sequence of nodes to be inserter.
     * @return an array <code>[predecessor, successor]</code>
     */
    protected abstract INodeVisit[] insertNodesImplem(int index, List<? extends INodeVisit> subroute);

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.Route#swapNodes(int, int)
     */
    @Override
    public final boolean swapNodes(int i, int j) {
        if (i == j) {
            return true;
        }

        // Check indices
        String e = checkIndex(i, "node1", false);
        if (e != null) {
            throw new IllegalArgumentException(e);
        }
        e = checkIndex(j, "node2", false);
        if (e != null) {
            throw new IllegalArgumentException(e);
        }

        // Check nodes order
        if (j < i) {
            int t = i;
            i = j;
            j = t;
        }

        INodeVisit[] r = swapNodesImplem(i, j);
        getCostDelegate().nodesSwapped(this, r[0], r[1], r[2], r[3], r[4], r[5]);

        if (checkRoute) {
            String err = SolutionChecker.checkRoute(this, false, false, false);
            if (err != null) {
                System.out.println("RouteBase.swapNodes: " + err);
            }
        }

        setChanged();
        return true;
    }

    /**
     * Default implementation of {@link #swapNodes(int, int)}.<br/>
     * Indexes do not need to be checked in this method as it is done in the parent method: <code>node1 < node2</code>
     * 
     * @param node1
     *            the index of the first node
     * @param node2
     *            the index of the second node
     * @return an array <code>[pred1,node1,succ1,pred2,node2,succ2]</code>
     * @see IRouteCostDelegate#nodesSwapped(IRoute, INodeVisit, INodeVisit, INodeVisit, INodeVisit, INodeVisit,
     *      INodeVisit)
     */
    INodeVisit[] swapNodesImplem(int node1, int node2) {
        INodeVisit n2 = getNodeAt(node2);
        INodeVisit[] r = setNodeAtImplem(node1, n2);

        INodeVisit[] s = setNodeAtImplem(node2, r[1]);

        return new INodeVisit[] { r[0], r[1], r[2], s[0], s[1], s[2] };
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.Route#revertSubRoute(int, int)
     */
    @Override
    public void reverseSubRoute(int start, int end) {
        if (start != end) {

            // Check indices
            String e = checkSequenceIndexes(start, "start", end, "end", false);
            if (e != null) {
                throw new IllegalArgumentException(e);
            }

            INodeVisit[] r = reverseSubrouteImplem(start, end);

            setChanged();
            getCostDelegate().subrouteReversed(this, r[0], r[1], r[2], r[3]);

            if (checkRoute) {
                String err = SolutionChecker.checkRoute(this, false, true, true);
                if (err != null) {
                    System.out.println("RouteBase.reverseSubRoute: " + err);
                }
            }
        }
    }

    /**
     * Implementation of {@link #reverseSubRoute(int, int)}.<br/>
     * Indexes do not need to be checked in this method as it is done in the parent method:<br/>
     * <code>0 <= start <= end < this.length()</code>
     * 
     * @param start
     *            the position of the first node of the desired subroute
     * @param end
     *            the index
     * @return an array <code>[pred,first,last,succ]</code>
     */
    protected abstract INodeVisit[] reverseSubrouteImplem(int start, int end);

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.IRoute#reverseRoute()
     */
    @Override
    public void reverseRoute() {
        reverseSubrouteImplem(0, length() - 1);
    }

    /*
     * Index checks
     */
    /**
     * Check if <code>index</code> is a valid index.<br/>
     * <code>0<idnex<this.length()</code>
     * 
     * @param index
     *            the index to checked
     * @param argName
     *            the name of the corresponding argument
     * @param allowLength
     *            <code>true</code> if <code>index</code> can take the value of length (used in set* and insert methods)
     * @return a String describing the detected error if any, otherwise
     */
    synchronized String checkIndex(int index, String argName, boolean allowLength) {
        if (!(allowLength && index == length()) && (index < 0 || index >= length())) {
            return String.format("%1$s is out of range (%1$s=%2$s, length=%3$s)", argName, index, length());
        } else {
            return null;
        }
    }

    /**
     * Check if <code>index1</code> and <code>index2</code> are valids subroute indexes:<br/>
     * <code>0<idnex1<=index2<this.length()</code>
     * 
     * @param index1
     *            the first index of the subroute
     * @param arg1Name
     *            the name of the argument corresponding to <code>index1</code>
     * @param index2
     *            the last index of the subroute
     * @param arg2Name
     *            the name of the argument corresponding to <code>index2</code>
     * @param allowLength
     *            <code>true</code> if index2 can be equal to the route length
     * @return a String describing the detected error if any, otherwise
     */
    String checkSequenceIndexes(int index1, String arg1Name, int index2, String arg2Name, boolean allowLength) {
        String r = null;

        String i1 = checkIndex(index1, arg1Name, allowLength);
        if (i1 != null) {
            r = i1;
        }

        String i2 = checkIndex(index2, arg2Name, allowLength);
        if (i2 != null) {
            r = r == null ? i2 : r + "," + i2;
        }
        if (index1 > index2) {
            String le = String.format("%1$s should be lower than %2$s (%1$s=%3$s while %2$s=%4$s)", arg1Name, arg2Name,
                    index1, index2);
            r = r == null ? le : r + "," + le;
        }

        return r;
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

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.Route#length()
     */
    @Override
    public abstract int length();

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.Route#getNodeSequence()
     */
    @Override
    public abstract List<INodeVisit> getNodeSequence();

    /*
     * Static utility methods
     */
    /**
     * Reverts a {@link List} of {@link INodeVisit}.
     * 
     * @param subroute
     *            the list of nodes to be revered
     * @return a list of {@link INodeVisit} such as
     */
    public static List<INodeVisit> revertSubRoute(List<INodeVisit> subroute) {
        LinkedList<INodeVisit> reverted = new LinkedList<INodeVisit>();
        for (INodeVisit n : subroute) {
            reverted.addFirst(n);
        }

        return reverted;
    }

    /**
     * Utility method for cost calculation
     * 
     * @param i
     * @param j
     * @return the vehicle dependent cost of traveling arc (i,j) in this route
     */
    public double getArcCost(int i, int j) {
        return i == j ? 0 : getArcCost(getNodeAt(i), getNodeAt(j));
    }

    /**
     * Utility method for cost calculation
     * 
     * @param n1
     * @param n2
     * @return the vehicle dependent cost of traveling arc (n1,n2) in this route
     */
    public double getArcCost(INodeVisit n1, INodeVisit n2) {
        return n1 == n2 ? 0 : getParentSolution().getParentInstance().getCostDelegate().getCost(n1, n2, getVehicle());
    }

    /**
     * Utility method for the calculation of a node sequence cost
     * 
     * @param subroute
     * @return the cost of the given subroute if traveled by the vehicle associted with this toute
     */
    public double getSubrouteCost(List<? extends INodeVisit> subroute) {
        if (subroute.isEmpty()) {
            return 0;
        }

        double cost = 0;

        Iterator<? extends INodeVisit> it = subroute.iterator();
        INodeVisit pred = it.next();
        INodeVisit succ;
        while (it.hasNext()) {
            succ = it.next();
            cost += getArcCost(pred, succ);
            pred = succ;
        }

        return cost;
    }

    @Override
    public abstract boolean contains(INodeVisit node);

    @Override
    public abstract RouteBase clone();
}// end VRPRoute