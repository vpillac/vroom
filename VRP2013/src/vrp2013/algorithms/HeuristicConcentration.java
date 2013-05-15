/**
 * 
 */
package vrp2013.algorithms;

import ilog.concert.IloColumnArray;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.concert.IloObjectiveSense;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.UnknownObjectException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.IRoute;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.dataModel.IVRPRequest;
import vroom.common.modeling.dataModel.ImmutableRoute;
import vroom.common.modeling.dataModel.RouteBase;
import vroom.common.modeling.util.HashRoutePool;
import vroom.common.modeling.util.IRoutePool;
import vroom.common.modeling.util.ISolutionFactory;
import vroom.common.utilities.Utilities;
import vroom.common.utilities.Utilities.AbsolutePrecision;
import vrp2013.util.VRPLogging;
import vrp2013.util.VRPSolution;

/**
 * The class <code>HeuristicConcentration</code> is an implementation of an heuristic concentration that builds a
 * solution to a VRP problem using a pool of routes and solving a set covering problem with the CPLEX solver.
 * <p>
 * Creation date: May 4, 2013 - 11:06:37 AM
 * 
 * @author Victor Pillac, <a href="http://www.nicta.com.au">National ICT Australia</a>, <a
 *         href="http://www.victorpillac.com">www.victorpillac.com</a>
 * @version 1.0
 */
public class HeuristicConcentration implements IVRPOptimizationAlgorithm {

    private final IVRPInstance               mInstance;
    private List<ImmutableRoute<INodeVisit>> mRoutes;
    private HashSet<Integer>                 mIncumbentRoutes;
    private IloNumVar[]                      mVars;
    private final IloRange[]                 mCoverCtrs;
    private IloCplex                         mCplex;
    private VRPSolution                      mSolution;
    private IRoutePool<INodeVisit>           mRoutePool;

    private final ISolutionFactory           mSolutionFactory;

    /**
     * Return the solution found by the heuristic concentration
     * 
     * @return the solution found by the heuristic concentration
     */
    @Override
    public VRPSolution getBestSolution() {
        return mSolution;
    }

    /**
     * Creates a new <code>HeuristicConcentration</code>
     * 
     * @param instance
     */
    public HeuristicConcentration(IVRPInstance instance, ISolutionFactory soluitonFactory) {
        mInstance = instance;
        mSolutionFactory = soluitonFactory;
        mCoverCtrs = new IloRange[Utilities.getMaxId(instance.getRequests()) + 1];

    }

    /**
     * Initialize the heuristic concentration with the given {@code  routePool} and {@code  incumbent}
     * 
     * @param routePool
     *            the pool of routes defining the Set Covering model
     * @param incumbent
     *            an incumbent (starting solution) for the MIP solver (can be {@code null})
     * @throws IloException
     */
    public void initialize(IRoutePool<INodeVisit> routePool, VRPSolution incumbent)
            throws IloException {
        // Protect the list of routes from any direct modification
        mRoutePool = routePool;
        mRoutes = new ArrayList<>(routePool.getAllRoutes());
        // Store the hash code of the routes from the incumbent solution
        mIncumbentRoutes = new HashSet<>();
        if (incumbent != null) {
            if (routePool instanceof HashRoutePool) {
                for (RouteBase r : incumbent) {
                    mIncumbentRoutes.add(((HashRoutePool<?>) routePool).getHasher().hash(r));
                }
            } else {
                for (RouteBase r : incumbent) {
                    ImmutableRoute<INodeVisit> ir = new ImmutableRoute<>(r, r.hashCode());
                    mRoutes.add(ir);
                    mIncumbentRoutes.add(ir.hashCode());
                }
            }
        }
        mRoutes = Collections.unmodifiableList(mRoutes);

        mSolution = incumbent;

        if (!mRoutes.isEmpty())
            buildModel();
    }

    @Override
    public VRPSolution call() throws IloException {
        if (mCplex != null) {
            mCplex.solve();

            buildSolution();
        }

        return getBestSolution();
    }

    /**
     * Build the model
     * 
     * @throws IloException
     */
    private void buildModel() throws IloException {
        mCplex = new IloCplex();

        mCplex.setOut(null);

        createAndAddCoverCtrs();

        createAndAddVars();
    }

    /**
     * Create and add to the model the decision variables defining which routes are selected
     * 
     * @throws IloException
     */
    private void createAndAddVars() throws IloException {
        // Variable definition
        // lower bound
        double xlb = 0;
        // upper bound
        double xub = 1;
        // type
        IloNumVarType xt = IloNumVarType.Bool;
        // name
        String xnames[] = new String[mRoutes.size()];
        // coefficient in the objective function
        double xobj[] = new double[mRoutes.size()];
        // starting value from the incumbent
        double xstart[] = new double[mRoutes.size()];

        // coefficients of the variables in the cover constraints
        double[][] matrixCoef = new double[mCoverCtrs.length][mRoutes.size()];

        // Definition of each variable
        for (int i = 0; i < mRoutes.size(); i++) {
            xobj[i] = mRoutes.get(i).getCost();
            xnames[i] = "x_" + i;

            // Add a 1 coefficient in the covering constraint of each visited request
            for (INodeVisit n : mRoutes.get(i)) {
                if (n.getParentRequest() != null) {
                    matrixCoef[n.getParentRequest().getID()][i] = 1;
                }
            }

            // Set the start value of the variable if the route is present in the incumbent
            if (mIncumbentRoutes.contains(mRoutes.get(i).hashCode())) {
                xstart[i] = 1;
            }
        }

        // Definition of the columns objective
        mCplex.addObjective(IloObjectiveSense.Minimize);
        IloColumnArray cols = mCplex.columnArray(mCplex.getObjective(), xobj);
        // Definition of the columns coefficients in the cover constraints
        for (IVRPRequest r : mInstance.getRequests()) {
            cols = cols.and(mCplex.columnArray(mCoverCtrs[r.getID()], matrixCoef[r.getID()]));
        }

        // Add the variables to the model
        mVars = mCplex.numVarArray(cols, xlb, xub, xt, xnames);

        // Sets the incumbent
        mCplex.addMIPStart(mVars, xstart);
    }

    /**
     * Create and add to the model the covering constraints that ensure that all requests are served
     * 
     * @throws IloException
     */
    private void createAndAddCoverCtrs() throws IloException {
        for (IVRPRequest r : mInstance.getRequests()) {
            mCoverCtrs[r.getID()] = mCplex.addGe(mCplex.constant(0), 1, "cover_" + r.getID());
        }
    }

    /**
     * Builds a solution to the original VRP problem from the set covering solution
     * 
     * @throws UnknownObjectException
     * @throws IloException
     */
    private void buildSolution() throws UnknownObjectException, IloException {
        double[] vals = mCplex.getValues(mVars);
        List<ImmutableRoute<INodeVisit>> selectedRoutes = new LinkedList<>();
        for (int i = 0; i < vals.length; i++) {
            if (AbsolutePrecision.isStrictlyPositive(vals[i]))
                selectedRoutes.add(mRoutes.get(i));
        }

        // Check that each request is visited exactly once
        Map<IRoute<?>, HashSet<INodeVisit>> removedNodes = new HashMap<>();
        IRoute<?>[] visitingRoutes = new IRoute<?>[mCoverCtrs.length];
        IRoute<?> prevRoute = null;
        for (IRoute<?> route : selectedRoutes) {
            removedNodes.put(route, new HashSet<INodeVisit>());
            int i = 0;
            for (INodeVisit n : route) {
                if (n.getParentRequest() != null) {
                    if ((prevRoute = visitingRoutes[n.getParentRequest().getID()]) != null) {
                        VRPLogging
                                .getOptLogger()
                                .info("HeuristicConcentration.buildSolution: request %3s is served more than once, fixing the solution",
                                        n.getParentRequest().getID());
                        // The request is covered by another route
                        // Compare the removal profit of both routes
                        if (remProfit(route, i, mInstance) > remProfit(prevRoute,
                                prevRoute.getNodePosition(n), mInstance)) {
                            removedNodes.get(route).add(n);
                        } else {
                            removedNodes.get(prevRoute).add(n);
                            visitingRoutes[n.getParentRequest().getID()] = route;
                        }
                    } else {
                        visitingRoutes[n.getParentRequest().getID()] = route;
                    }
                }
                i++;
            }
        }

        mSolution = (VRPSolution) mSolutionFactory.newSolution(getInstance());
        for (ImmutableRoute<INodeVisit> r : selectedRoutes) {
            RouteBase route = (RouteBase) mSolutionFactory.newRoute(mSolution, r.getVehicle());

            for (INodeVisit n : r) {
                if (!removedNodes.get(r).contains(n))
                    route.appendNode(n);
            }

            mSolution.addRoute(route);
        }
    }

    /**
     * Evaluate the decrease in cost following the removal of the node at position {@code  pos} in {@code  route}
     * 
     * @param route
     * @param pos
     * @param instance
     * @return the decrease in cost following the removal of the node at position {@code  pos} in {@code  route}
     */
    private double remProfit(IRoute<?> route, int pos, IVRPInstance instance) {
        double profit = 0;
        INodeVisit pred = route.getNodeAt(pos - 1);
        INodeVisit node = route.getNodeAt(pos);
        INodeVisit succ = route.getNodeAt(pos + 1);

        profit += instance.getCost(pred, node, route.getVehicle());
        profit += instance.getCost(node, succ, route.getVehicle());
        profit -= instance.getCost(pred, succ, route.getVehicle());

        return profit;
    }

    /**
     * Exports the model to a file
     * 
     * @param file
     * @throws IloException
     */
    public void exportModel(String file) throws IloException {
        if (mCplex != null)
            mCplex.exportModel(file);
    }

    @Override
    public IRoutePool<INodeVisit> getRoutePool() {
        return mRoutePool;
    }

    @Override
    public IVRPInstance getInstance() {
        return mInstance;
    }

    @Override
    public int getIterations() {
        return 1;
    }

    @Override
    public ISolutionFactory getSolutionFactory() {
        return mSolutionFactory;
    }

    @Override
    public void dispose() {
        mRoutePool.dispose();
    }
}
