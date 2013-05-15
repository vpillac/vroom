/*
 * jCW : a java library for the development of saving based heuristics
 */
package vroom.common.heuristics.cw.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import vroom.common.heuristics.cw.CWLogging;
import vroom.common.heuristics.cw.IJCWArc;
import vroom.common.heuristics.cw.JCWArc;
import vroom.common.heuristics.cw.kernel.ClarkeAndWrightHeuristic;
import vroom.common.heuristics.cw.kernel.ISavingsAlgorithm;
import vroom.common.heuristics.cw.kernel.RouteMergingMove;
import vroom.common.modeling.dataModel.IArc;
import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.IRoute;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.dataModel.IVRPSolution;
import vroom.common.modeling.dataModel.NodeInsertion;
import vroom.common.modeling.dataModel.Solution;
import vroom.common.modeling.util.CostCalculationDelegate;
import vroom.common.utilities.Stopwatch;

/**
 * <code>SavingsAlgorithmBase</code> is an abstraction for all savings algorithms.
 * <p>
 * It provides common elements that are required in such algorithm.
 * <p>
 * Creation date: Apr 16, 2010 - 10:10:01 AM
 * 
 * @author Victor Pillac <br/>
 *         <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <br/>
 *         <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public abstract class SavingsAlgorithmBase<S extends IVRPSolution<?>> implements
        ISavingsAlgorithm<S> {

    /**
     * The route configuration byte as defined by:
     * <p/>
     * <TABLE BORDER="1">
     * <TR>
     * <TH>Config Byte</TH>
     * <TH>Tail Route</TH>
     * <TH>Head Route</TH>
     * </TR>
     * <TR>
     * <TD>0</TD>
     * <TD>0-t-..-0</TD>
     * <TD>0-h-..-0 <em>or</em> 0-h-0</TD>
     * </TR>
     * <TR>
     * <TD>1</TD>
     * <TD>0-..-t-0 <em>or</em> 0-t-0</TD>
     * <TD>0-h-..-0 <em>or</em> 0-h-0</TD>
     * </TR>
     * <TR>
     * <TD>2</TD>
     * <TD>0-t-..-0</TD>
     * <TD>0-..-h-0</TD>
     * </TR>
     * <TR>
     * <TD>4</TD>
     * <TD>0-..-t-0 <em>or</em> 0-t-0</TD>
     * <TD>0-..-h-0</TD>
     * </TR>
     * </TABLE>
     * <p/>
     * Where the notation 0-X-..-0 (0-..X-0) mean that the node X is in first (last) position. <br/>
     * Singleton tail routes of form 0-t-0 are assumed to be of type 0-..-t-0, while singleton head routes 0-h-0 are
     * considered as 0-h-..-0
     * 
     * @param tailRoute
     *            the route associated with the tail node <code>t</code> of the arc
     * @param headRoute
     *            the route associated with the head node <code>h</code> of the arc
     * @param linkingArc
     *            the arc <code>(t,h)</code> linking the two routes
     * @return the route configuration as defined above.
     */
    public static byte getConfiguration(IRoute<INodeVisit> tailRoute, IRoute<INodeVisit> headRoute,
            IJCWArc linkingArc) {
        byte config = 0;

        if (tailRoute.length() > 3 // Case of singleton routes
                && linkingArc.getTailNode() == tailRoute.getNodeAt(1)) {
            config = 0;
        } else {
            config = 1;
        }
        if (linkingArc.getHeadNode() == headRoute.getNodeAt(1)) {
            config += 0;
        } else {
            config += 2;
        }

        return config;
    }

    /** A timer for this algorithm *. */
    private final Stopwatch         mTimer;

    protected final Set<INodeVisit> mNodes;

    /**
     * Getter for timer.
     * 
     * @return A timer for this algorithm
     */
    Stopwatch getTimer() {
        return this.mTimer;
    }

    /** A mapping between nodes and the routes to which they belong */
    private final Map<INodeVisit, IRoute<?>> mRouteMapping;

    /** A mapping between nodes and their interior flag */
    private final Map<INodeVisit, Boolean>   mInteriorFlagMapping;

    /**
     * Shortcut for vrpInstance.
     * 
     * @return The instance from which the data will be read, as defined in
     *         {@link ClarkeAndWrightHeuristic#getInstance()}
     */
    protected IVRPInstance getInstance() {
        return getParentHeuristic().getInstance();
    }

    /** The mSolution manipulated by this instance *. */
    private S mSolution;

    /**
     * Getter for mSolution.
     * 
     * @return The mSolution manipulated by this instance
     */
    @Override
    public S getSolution() {
        return mSolution;
    }

    /** The parent Clark and Wright heuristic *. */
    final ClarkeAndWrightHeuristic<S> mParentHeuristic;

    /**
     * Getter for the parent heuristic.
     * 
     * @return The parent Clark and Wright heuristic
     */
    @Override
    public ClarkeAndWrightHeuristic<S> getParentHeuristic() {
        return this.mParentHeuristic;
    }

    /**
     * Creates a new <code>SavingsAlgorithmBase</code>.
     * 
     * @param parentHeuristic
     *            the parent heuristic
     */
    public SavingsAlgorithmBase(ClarkeAndWrightHeuristic<S> parentHeuristic) {
        this.mParentHeuristic = parentHeuristic;
        this.mTimer = new Stopwatch();
        this.mRouteMapping = new HashMap<INodeVisit, IRoute<?>>();
        this.mInteriorFlagMapping = new HashMap<INodeVisit, Boolean>();
        this.mNodes = new HashSet<INodeVisit>();
    }

    /**
     * Initialize the data structures for a new run
     */
    @SuppressWarnings("unchecked")
    @Override
    public void initialize(S solution) {
        this.mSolution = solution != null ? solution : (S) getParentHeuristic()
                .getSolutionFactory().newSolution(getInstance());
        this.mInteriorFlagMapping.clear();
        this.mRouteMapping.clear();
    }

    @Override
    public void run() {
        runHeuristic();
    }

    /**
     * Run the heuristic itself (called by {@link #run()})
     */
    abstract protected void runHeuristic();

    /**
     * Calculates the deterministic savings of feasible merges. If a merging is not feasible the method eliminates the
     * arc from the list
     * 
     * @param arcs
     *            the feasible merging
     * @return a {@linkplain PriorityQueue prioritized queue} containing the feasible mergings ordered from the best to
     *         the worst
     */
    protected List<IJCWArc> calculateSavings(Set<? extends IArc> arcs) {
        CWLogging.getAlgoLogger().debug(" Start savings calculation...");

        if (arcs.size() == 0) {
            // Special case to prevent exception
            return new LinkedList<IJCWArc>();
        }

        ArrayList<IJCWArc> orderedMergings = new ArrayList<IJCWArc>(arcs.size());

        for (IArc currentArc : arcs) {
            if (!currentArc.getTailNode().isDepot() && !currentArc.getHeadNode().isDepot()
                    && this.mNodes.contains(currentArc.getHeadNode())
                    && this.mNodes.contains(currentArc.getTailNode())) {

                if (currentArc instanceof IJCWArc) {
                    orderedMergings.add((IJCWArc) currentArc);
                } else {
                    IJCWArc newArc = new JCWArc(currentArc, getSaving(currentArc));
                    orderedMergings.add(newArc);
                    CWLogging.getAlgoLogger().lowDebug("\t Arc: (%s,%s) \tSaving: %s",
                            newArc.getTailNode(), newArc.getHeadNode(), newArc.getSaving());
                }
            }
        }

        Collections.sort(orderedMergings);

        return orderedMergings;
    }

    /**
     * Register a node as belonging to a route
     * 
     * @param node
     * @param route
     * @return the route previously associated with node
     */
    protected IRoute<?> assignNodeToRoute(INodeVisit node, IRoute<?> route) {
        return mRouteMapping.put(node, route);
    }

    /**
     * Getter for the route associated with a node
     * 
     * @param node
     * @return the route in to which the given node belongs
     */
    protected IRoute<?> getContainingRoute(INodeVisit node) {
        return mRouteMapping.get(node);
    }

    /**
     * Get the interior flag of a given node
     * 
     * @param node
     * @return <code>true</code> is the given node is interior
     */
    protected boolean isNodeInterior(INodeVisit node) {
        return !node.isDepot() && mInteriorFlagMapping.get(node);
    }

    /**
     * Set the node interior flag to <code>true</code>
     * 
     * @param node
     */
    protected void setInterior(INodeVisit node) {
        mInteriorFlagMapping.put(node, true);
    }

    /**
     * Set the node interior flag to <code>false</code>
     * 
     * @param node
     */
    protected void setExterior(INodeVisit node) {
        mInteriorFlagMapping.put(node, false);
    }

    /**
     * Savings calculation
     * 
     * @param arc
     * @return the saving associated with the given arc
     */
    protected double getSaving(IArc arc) {
        CostCalculationDelegate cd = getParentHeuristic().getInstance().getCostDelegate();
        INodeVisit d = getParentHeuristic().getInstance().getDepotsVisits().iterator().next();
        return cd.getCost(arc.getTailNode(), d) + cd.getCost(d, arc.getHeadNode())
                - cd.getCost(arc.getTailNode(), arc.getHeadNode());
    }

    /**
     * Check the feasibility of a merging represented by the given arc
     * 
     * @param currentArc
     *            the merging arc which feasibility has to be tested
     * @param tailRoute
     *            the route to which the tail belongs
     * @param headRoute
     *            the route to which the head belongs
     * @return
     */
    protected boolean checkFeasibility(IJCWArc currentArc, IRoute<INodeVisit> tailRoute,
            IRoute<INodeVisit> headRoute) {
        RouteMergingMove move = new RouteMergingMove(currentArc, tailRoute, headRoute);

        return tailRoute != headRoute && !isNodeInterior(currentArc.getTailNode())
                && !isNodeInterior(currentArc.getHeadNode())
                && (getInstance().isSymmetric()
                // Only configuration 1 is valid for directed graphs
                || getConfiguration(tailRoute, headRoute, currentArc) == 1)
                && getParentHeuristic().getConstraintHandler().isFeasible(getSolution(), move);
    }

    /**
     * Explanation for the infeasibility, used for debugging
     * 
     * @param currentArc
     *            the merging arc which feasibility has to be tested
     * @param tailRoute
     *            the route to which the tail belongs
     * @param headRoute
     *            the route to which the head belongs
     * @return a string describing the infeasibility
     */
    protected String getInfeasExpl(IJCWArc currentArc, IRoute<INodeVisit> tailRoute,
            IRoute<INodeVisit> headRoute) {
        StringBuilder explanation = new StringBuilder();
        if (tailRoute == headRoute) {
            explanation.append("both tail and head ends belong to the same route");
        }
        if (isNodeInterior(currentArc.getTailNode())) {
            if (explanation.length() > 0) {
                explanation.append(", ");
            }
            explanation.append("tail node is interior");
        }
        if (isNodeInterior(currentArc.getHeadNode())) {
            if (explanation.length() > 0) {
                explanation.append(", ");
            }
            explanation.append("head node is interior");
        }
        if (!getInstance().isSymmetric()
        // Only configuration 1 is valid for directed graphs
                && getConfiguration(tailRoute, headRoute, currentArc) != 1) {
            if (explanation.length() > 0) {
                explanation.append(", ");
            }
            explanation
                    .append("this merging will require to reverse a route which is not permited in asymmetric instances");
        }

        if (explanation.length() == 0) {
            explanation.append("side constraints");
        }

        return explanation.length() > 0 ? explanation.toString() : null;
    }

    /**
     * Merge routes.
     * 
     * @param linkingArc
     *            the merging arc
     * @param tailRoute
     *            the route to which the tail belongs
     * @param headRoute
     *            the route to which the head belongs
     */
    protected void mergeRoutes(IJCWArc linkingArc, IRoute<INodeVisit> tailRoute,
            IRoute<INodeVisit> headRoute) {

        int config = getConfiguration(tailRoute, headRoute, linkingArc);

        switch (config) {
        case 0: // 0: 0>t..>0 and 0>h..>0 -> 0>..th..>0
            // Reverse tail and append head to tail
            tailRoute.reverseRoute();
            break;

        case 2:
            // 2: 0>t..>0 and 0>..h>0 -> 0>..th..>0
            // Reverse tail and head and append head to tail
            tailRoute.reverseRoute();
            headRoute.reverseRoute();
            break;

        case 1:
            // 1: 0>..t>0 and 0>h..>0 -> 0>..th..>0
            // Append head to tail
            break;

        case 3:
            // 3: 0>..t>0 and 0>..h>0 -> 0>..th..>0
            // Reverse head and append to tail
            headRoute.reverseRoute();
            break;
        }

        // Remove the depot from the resulting route
        tailRoute.extractNode(tailRoute.length() - 1);

        // Set the first node as interior if there is more than 1 node in the
        // route
        if (tailRoute.length() > 2) {
            setInterior(tailRoute.getLastNode());
        }

        Iterator<INodeVisit> it = headRoute.iterator();
        it.next();// Ignoring the first node (depot)
        // Set the first node as interior if there is more than 1 node in the
        // route
        boolean first = true && headRoute.length() > 3;
        while (it.hasNext()) {
            INodeVisit node = it.next();

            // Append node to the resulting route
            tailRoute.appendNode(node);

            assignNodeToRoute(node, tailRoute);

            if (first) {
                // Set the first node of the appended route as interior
                setInterior(node);
                first = false;
            }
        }

        // Remove the deprecated route
        getSolution().removeRoute(headRoute);

    }

    /**
     * Generate a general report for the result of the algorithm.
     * 
     * @return a basic report including: -Total number of routes -Total execution time -Total traveled distance of each
     *         route (deterministic part of the objective function) -Total expected recursion cost of each route
     *         (stochastic part of the objective function) -Total traveled distance -Total expected recursion cost
     *         -Total cost Format the mSolution using a data structure compatible with RoutePlotter: -Column A: Route
     *         identifier -Column B: Node identifier
     */
    @Override
    public String generateGeneralReport() {
        StringBuilder report = new StringBuilder();
        report.append("======================================================\n");
        report.append("Total number of routes 	" + getSolution().getRouteCount());
        report.append("\n");
        report.append("Total execution time   	" + getTimer().readTimeMS());
        report.append("\n");
        report.append("======================================================\n");
        report.append("General Report:\n");
        report.append("--------------------------------------------------------------------------\n");
        report.append("ID\tDistance\tTravel  \tLoad\n");
        report.append("--------------------------------------------------------------------------\n");

        return report.toString();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean repairSolutionForLimitedFleet() {
        int numV = getParentHeuristic().getInstance().getFleet().size();

        IVRPSolution<?> sol = getSolution().clone();

        while (sol.getRouteCount() > numV) {

            Solution<IRoute<?>> tmp = (Solution<IRoute<?>>) sol.clone();

            LinkedList<IRoute<?>> routes = new LinkedList<IRoute<?>>();
            for (IRoute<?> r : tmp) {
                routes.add(r);
            }

            // Sort routes according to a load*length criteria
            Collections.sort(routes, new Comparator<IRoute<?>>() {
                @Override
                public int compare(IRoute<?> o1, IRoute<?> o2) {
                    double coefLoad1 = o1.getLoad() * o1.length();
                    double coefLoad2 = o2.getLoad() * o2.length();
                    return (int) Math.round(coefLoad1 - coefLoad2);
                }
            });

            boolean failed = true;

            while (failed && !routes.isEmpty()) {
                IRoute<?> removedRoute = routes.pop();

                failed = false;

                // Try to reinsert removed nodes
                for (INodeVisit n : removedRoute) {
                    if (!n.isDepot()) {
                        NodeInsertion bestIns = null;
                        // Check candidate reinsertion routes
                        for (IRoute<?> candidate : tmp) {
                            if (candidate != removedRoute) {
                                // TODO check feasibility with constraint
                                // handler
                                if (candidate.canAccommodateRequest(n.getParentRequest())) {
                                    NodeInsertion ins = candidate.getBestNodeInsertion(n, 1,
                                            candidate.length() - 1);
                                    if (ins != null
                                            && (bestIns == null || ins.getCost() < bestIns
                                                    .getCost())) {
                                        bestIns = ins;
                                    }
                                }
                            }
                        }
                        if (bestIns != null) {
                            ((IRoute<INodeVisit>) bestIns.getRoute()).insertNode(bestIns, n);
                        } else {
                            failed = true;
                            break;
                        }
                    }
                }

                if (!failed) {
                    tmp.removeRoute(removedRoute);
                    sol = tmp;
                }
            }

            if (routes.isEmpty()) {
                break;
            }
        }

        boolean feas = true;
        if (sol.getRouteCount() > numV) {
            feas = false;
            CWLogging.getAlgoLogger().info(
                    "repairSolutionForLimitedFleet: Unable to repair solution %s", getSolution());
        }

        getSolution().clear();
        for (IRoute<?> route : sol) {
            ((IVRPSolution<IRoute<?>>) getSolution()).addRoute(route);
        }
        return feas;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
