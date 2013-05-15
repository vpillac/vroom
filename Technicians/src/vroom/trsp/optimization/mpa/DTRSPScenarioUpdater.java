/**
 * 
 */
package vroom.trsp.optimization.mpa;

import java.util.LinkedList;
import java.util.List;

import vroom.optimization.online.jmsa.IActualRequest;
import vroom.optimization.online.jmsa.IMSARequest;
import vroom.optimization.online.jmsa.IScenario;
import vroom.optimization.online.jmsa.components.ComponentManager;
import vroom.optimization.online.jmsa.components.ScenarioUpdaterBase;
import vroom.trsp.MPASolver;
import vroom.trsp.datamodel.ITRSPNode;
import vroom.trsp.datamodel.ITRSPTour;
import vroom.trsp.datamodel.TRSPRequest;
import vroom.trsp.datamodel.TRSPTour;
import vroom.trsp.datamodel.TRSPTour.TRSPTourIterator;
import vroom.trsp.optimization.InsertionMove;
import vroom.trsp.optimization.alns.RepairRegret;
import vroom.trsp.optimization.mpa.DTRSPSolution.DTRSPTour;

/**
 * <code>DTRSPScenarioUpdater</code>
 * <p>
 * Creation date: Feb 7, 2012 - 11:33:52 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class DTRSPScenarioUpdater extends ScenarioUpdaterBase {

    private final MPASolver    mSolver;
    private final RepairRegret mRepairRegret;

    public DTRSPScenarioUpdater(ComponentManager<?, ?> componentManager) {
        super(componentManager);
        mSolver = getMSAProxy().getParameters().get(MPASolver.TRSP_MPA_SOLVER);
        mRepairRegret = new RepairRegret(mSolver.getParams(), mSolver.getTourCtrHandler(), 3, false);
    }

    @Override
    public boolean insertRequest(IScenario scenario, IMSARequest request) {
        DTRSPSolution scen = (DTRSPSolution) scenario;
        TRSPRequest req = (TRSPRequest) request;

        InsertionMove mve = InsertionMove.findInsertion(req.getID(), scen, scen.getCostDelegate(),
                mSolver.getTourCtrHandler(), true, true);

        if (mve != null && mve.isFeasible()) {
            InsertionMove.executeMove(mve);
            scen.markAsServed(req.getID());
            return true;
        } else {
            scen.markAsUnserved(req.getID());
            return false;
        }
    }

    /**
     * Schedule a decision: check all scenarios and set the earliest departure time of {@code  node} to {@code  time}
     * <p>
     * The goal is to fix the earliest departure time until the assignment decision is reevaluated.
     * </p>
     * 
     * @param node
     *            the node that will be visited next by {@code  technician}
     * @param technician
     *            the technician that is expected to visit {@code  node}
     * @param time
     *            the time at which the assignment decision will be reevaluated
     */
    public void scheduleDecision(ITRSPNode node, int technician, double time) {
        LinkedList<IScenario> outdatedScen = new LinkedList<>();
        for (IScenario scen : getMSAProxy().getScenarioPool()) {
            DTRSPSolution sol = (DTRSPSolution) scen;

            boolean feasible = enforceDecision(scen, node, technician);
            if (feasible) {
                DTRSPTour tour = sol.getTour(technician);
                // We freeze the node and update the earliest departure time
                sol.freeze(node.getID(), tour.getEarliestArrivalTime(node.getID()), time);
            } else {
                outdatedScen.add(scen);
            }
        }
        getMSAProxy().getScenarioPool().removeScenarios(outdatedScen);
    }

    @Override
    public boolean enforceDecision(IScenario scenario, IActualRequest request, int resourceId) {
        DTRSPSolution scen = (DTRSPSolution) scenario;

        ITRSPNode node = (ITRSPNode) request;
        int nodeId = node.getID();

        TRSPTour visitingTour = scen.getVisitingTour(nodeId);
        int current = mSolver.getInstance().getSimulator().getCurrentNode(resourceId).getID();
        TRSPTour targetTour = scen.getTour(resourceId);

        if (nodeId == scen.getInstance().getHomeDuplicate(
                targetTour.getTechnician().getHome().getID())) {
            // The node is the technician home, remove all unserved requests
            TRSPTourIterator it = targetTour.iterator();
            while (it.hasNext()) {
                int n = it.next();
                if (!scen.getInstance().isServedOrAssignedOrRejected(n)) {
                    it.remove();
                    scen.markAsUnserved(n);
                }
            }
            if (targetTour.getLastNode() != nodeId)
                targetTour.appendNode(nodeId, node.getArrivalTime());
        } else if (visitingTour != targetTour || current != visitingTour.getPred(nodeId)) {
            if (visitingTour != null)
                visitingTour.removeNode(nodeId);

            targetTour.insertAfter(current, nodeId);
        }
        scen.markAsServed(nodeId);
        // Freeze the node
        scen.freeze(nodeId, node.getArrivalTime(),
                targetTour.getTimeWindow(nodeId).getEarliestStartOfService(node.getArrivalTime())
                        + targetTour.getServiceTime(nodeId));

        fixTour(scen, targetTour);
        return mSolver.getSolCtrHandler().isFeasible(scen);
    }

    /**
     * Check if a tour is feasible, and if not attempt to fix it
     * 
     * @param scenario
     * @param tour
     * @return {@code true} if the tour was repaired, {@code false} if some requests were left unserved
     */
    public boolean fixTour(DTRSPSolution scenario, TRSPTour tour) {
        int infeasNode = mSolver.getSolCtrHandler().getTourConstraintHandler()
                .firstInfeasibleNode(tour);
        // Attempt to repair the solution if it is not feasible
        if (infeasNode != ITRSPTour.UNDEFINED && !scenario.getInstance().isMainDepot(infeasNode)) {// [12/08/03] ignore
                                                                                                   // the main depot
            // Incrementally check the feasibility of targetTour, remove infeasible requests when detected, attempt to
            // reinsert them
            List<Integer> removedNodes = tour.truncate(tour.getPred(infeasNode));
            for (Integer n : removedNodes)
                scenario.markAsUnserved(n);
            return mRepairRegret.repair(scenario, null, null);
        } else {
            return true;
        }
    }

    @Override
    public boolean startServicingUpdate(IScenario scenario, int resourceId) {
        // Nothing to do
        return true;
    }

    @Override
    public boolean stopServicingUpdate(IScenario scenario, int resourceId) {
        // Nothing to do
        return true;
    }

    @Override
    public boolean startOfServiceUpdate(IScenario scenario, int resourceId, IActualRequest request) {
        // Nothing to do
        return true;
    }

    @Override
    public boolean endOfServiceUpdate(IScenario scenario, int resourceId,
            IActualRequest servedRequest) {
        // Nothing to do
        return true;
    }

}
