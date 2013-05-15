/*
 * jCW : a java library for the development of saving based heuristics
 */
package vroom.common.heuristics.cw.algorithms;

import java.util.List;

import vroom.common.heuristics.cw.CWLogging;
import vroom.common.heuristics.cw.IJCWArc;
import vroom.common.heuristics.cw.kernel.ClarkeAndWrightHeuristic;
import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.IRoute;
import vroom.common.modeling.dataModel.IVRPSolution;

/**
 * <code>BasicSavingsHeuristic</code> is a simple implementation of a saving heuristic that considers the candidate
 * mergings in a sequential order.
 * <p>
 * Creation date: Jun 24, 2010 - 2:56:04 PM
 * 
 * @author Jorge E. Mendoza, <a href="http://www.uco.fr">Universite Catholique de l'Ouest</a>
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class BasicSavingsHeuristic<S extends IVRPSolution<?>> extends SavingsAlgorithmBase<S> {

    /**
     * Creates a new <code>BasicSavingsHeuristic</code>.
     * 
     * @param parentHeuristic
     *            the parent heuristic of this instance from which instance getVrpInstance() and parameters will be read
     * @see SavingsAlgorithmBase#SavingsAlgorithmBase(ClarkeAndWrightHeuristic)
     */
    public BasicSavingsHeuristic(ClarkeAndWrightHeuristic<S> parentHeuristic) {
        super(parentHeuristic);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void runHeuristic() {
        CWLogging.getAlgoLogger().info("BasicSavingsHeuristic.run: Execution started");
        getTimer().restart();

        CWLogging.getAlgoLogger().debug("BasicSavingsHeuristic.run: Initialization started");

        mNodes.clear();
        mNodes.addAll(getInstance().getNodeVisits());

        getSolution().clear();

        // Create a round trip route for each node
        for (INodeVisit node : mNodes) {
            IRoute<INodeVisit> currentRoute = (IRoute<INodeVisit>) getParentHeuristic()
                    .getSolutionFactory().newRoute(getSolution(),
                            getInstance().getFleet().getVehicle());

            currentRoute.appendNode(getInstance().getDepotsVisits().iterator().next());

            currentRoute.appendNode(node);
            setExterior(node);
            assignNodeToRoute(node, currentRoute);

            currentRoute.appendNode(getInstance().getDepotsVisits().iterator().next());

            ((IVRPSolution<IRoute<?>>) getSolution()).addRoute(currentRoute);
        }

        // Calculate the savings
        List<IJCWArc> savingsQueue = calculateSavings(getInstance().getArcs());

        // CWLogging.getAlgoLogger().info("savingsQueue: " + savingsQueue);
        CWLogging.getAlgoLogger().debug("BasicSavingsHeuristic.run: Route merging");

        // Heuristic
        for (IJCWArc arc : savingsQueue) {
            INodeVisit tail = arc.getTailNode();
            INodeVisit head = arc.getHeadNode();
            IRoute<INodeVisit> tailRoute = (IRoute<INodeVisit>) getContainingRoute(tail);
            IRoute<INodeVisit> headRoute = (IRoute<INodeVisit>) getContainingRoute(head);

            // if (CWLogging.getAlgoLogger().isEnabledFor(LoggerHelper.LEVEL_LOW_DEBUG)) {
            CWLogging.getAlgoLogger().lowDebug("\t Current Solution %s", getSolution());
            // }

            CWLogging.getAlgoLogger().lowDebug("\t Largest saving available %s", arc);

            // Check the feasibility
            if (checkFeasibility(arc, tailRoute, headRoute)) {
                // Merge routes
                mergeRoutes(arc, tailRoute, headRoute);
                CWLogging.getAlgoLogger().lowDebug("\t \t > Routes merged");
            } else {
                // if (CWLogging.getAlgoLogger().isEnabledFor(LoggerHelper.LEVEL_LOW_DEBUG)) {
                // String explanation = getInfeasExpl(arc, tailRoute, headRoute);
                // CWLogging.getAlgoLogger().lowDebug("\t \t > Infeasible: %s", explanation);
                // }
            }
            if (getSolution().getRouteCount() == 1)
                break;
        }

        // if(getSolution().getRouteCount()>getParentHeuristic().getInstance().getFleet().size()){
        // repairSolutionForLimitedFleet();
        // }

        getTimer().stop();

        CWLogging.getAlgoLogger().info(
                "BasicSavingsHeuristic.run: Execution finished, total time %s, solution: %s",
                getTimer().readTimeMS(), getSolution());

    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.heuristics.jcw.kernel.IJCWSavingsAlgorithm#
     * generateConstraintsReport()
     */
    @Override
    public String generateConstraintsReport() {
        return "NA";
    }

}
