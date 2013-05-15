/**
 * 
 */
package vroom.trsp.optimization.constraints;

import java.util.List;

import vroom.common.modeling.dataModel.attributes.AttributeWithIdSet;
import vroom.common.utilities.optimization.IConstraint;
import vroom.common.utilities.optimization.IMove;
import vroom.trsp.datamodel.ITRSPTour;
import vroom.trsp.datamodel.ITourIterator;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPTour;
import vroom.trsp.datamodel.TRSPTour.TRSPTourIterator;
import vroom.trsp.optimization.InsertionMove;
import vroom.trsp.optimization.localSearch.TRSPShift.TRSPShiftMove;
import vroom.trsp.optimization.localSearch.TRSPTwoOpt.TRSPTwoOptMove;
import vroom.trsp.util.TRSPLogging;

/**
 * <code>ToolsConstraint</code> is an implementation of {@link IConstraint} that checks if moves are feasible in terms
 * of tool constraints.
 * <p>
 * Creation date: Mar 28, 2011 - 9:50:27 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class ToolsConstraint extends TourConstraintBase {

    /**
     * Check if a {@linkplain InsertionMove insertion move} is feasible for the given <code>tour</code>
     * 
     * @param tour
     *            the base tour
     * @param move
     *            the {@link InsertionMove} to be tested
     * @return <code>true</code> if applying <code>move</code> to the given <code>tour</code> will result in a feasible
     *         tour
     */
    @Override
    public boolean isInsFeasible(ITRSPTour tour, InsertionMove move) {
        return checkInsFeasibility(tour, move) == 1;
    }

    /**
     * Check an insertion move feasibility
     * 
     * @param tour
     * @param move
     * @return 1 if move is feasible, -1 if the technician does not have the required tools and no visit to main depot
     *         is planned, -2 if the required tools are not available at this point
     */
    private int checkInsFeasibility(ITRSPTour itour, InsertionMove move) {
        if (!TRSPTour.class.isAssignableFrom(itour.getClass())) {
            TRSPLogging.getOptimizationLogger().warn(
                    String.format(
                            "ToolsConstraint.checkInsFeasibility: unsupported tour class (%s)",
                            itour.getClass()));
            return 1;
        }
        TRSPTour tour = (TRSPTour) itour;
        InsertionMove m = move;
        if (m.isDepotTrip() || !tour.getInstance().isRequest(move.getNodeId()))
            return 1;
        if (!tour.isMainDepotVisited()
                && !m.isDepotTrip()
                && !tour.getInstance()
                        .hasRequiredTools(tour.getTechnician().getID(), m.getNodeId()))
            return -1;
        int pred = m.getInsertionPred();
        if (pred == ITRSPTour.UNDEFINED) {
            if (tour.getInstance().hasRequiredTools(tour.getTechnician().getID(), m.getNodeId()))
                return 1;
            else
                return -2;
        } else {
            for (int tool : itour.getInstance().getRequest(m.getNodeId()).getToolSet())
                if (!tour.isToolAvailable(pred, tool))
                    return -2;
        }
        return 1;
    }

    /**
     * Check if a {@linkplain TRSPTwoOptMove 2-opt move} is feasible for the given <code>tour</code>
     * 
     * @param tour
     *            the base tour
     * @param move
     *            the {@link TRSPTwoOptMove} to be tested
     * @return <code>true</code> if applying <code>move</code> to the given <code>tour</code> will result in a feasible
     *         tour
     */
    @Override
    public boolean isTwoOptFeasible(ITRSPTour itour, TRSPTwoOptMove move) {
        if (!TRSPTour.class.isAssignableFrom(itour.getClass())) {
            TRSPLogging.getOptimizationLogger().warn(
                    String.format("ToolsConstraint.isTwoOptFeasible: unsupported tour class (%s)",
                            itour.getClass()));
            return true;
        }
        TRSPTour tour = (TRSPTour) itour;

        if (!tour.isMainDepotVisited() // The main depot is not visited, the tour is already feasible
                || tour.isMainDepotVisited(tour.getSucc(move.getFirst())) // The visit to the main depot is done before
                                                                          // the first
                // affected node
                || !tour.isMainDepotVisited(move.getSecond())) // The visit to the main depot is done
                                                               // after the last affected node
            return true;

        // The main depot is visited between j and m
        TRSPTourIterator it = tour.iterator(move.getSecond());

        // Start with node m and iterate backward
        int node = it.previous();
        while (node != tour.getMainDepotId() && it.hasNext()) {
            if (!tour.getInstance().hasRequiredTools(tour.getTechnician().getID(), node))
                // The technician does not have the required tools for this request
                return false;
        }

        // No incompatible request was found, the move is feasible
        return true;
    }

    /**
     * Check if a {@linkplain TRSPShiftMove shift move} is feasible for the given <code>solution</code>
     * 
     * @param solution
     *            the base solution
     * @param move
     *            the {@link TRSPShiftMove} to be tested
     * @return <code>true</code> if applying <code>move</code> to the given <code>solution</code> will result in a
     *         feasible mSolution
     */
    @Override
    public boolean isShiftFeasible(ITRSPTour itour, TRSPShiftMove move) {
        if (!TRSPTour.class.isAssignableFrom(itour.getClass())) {
            TRSPLogging.getOptimizationLogger().warn(
                    String.format("ToolsConstraint.isShiftFeasible: unsupported tour class (%s)",
                            itour.getClass()));
            return true;
        }
        TRSPTour tour = (TRSPTour) itour;

        if (!tour.isMainDepotVisited()) {
            // The main depot is not visited, we assume that the tour is and will remain feasible
            return true;
        } else {
            // The main depot is visited
            if (move.getNode() == tour.getMainDepotId()) {
                // The shifted node is the main depot
                if (!move.isForward()) {
                    // A backward move of the main depot is always feasible
                    return true;
                } else {
                    // Check if the requests between the current depot position and its new one require a visit to
                    // the depot to be served
                    List<Integer> seq = move.getChangedSequence();
                    for (int s : seq) {
                        if (s == tour.getMainDepotId())
                            // The requests after the main depot will be feasible
                            return true;
                        else if (!tour.getInstance().hasRequiredTools(tour.getTechnician().getID(),
                                s))
                            // This request will be before the depot visit but the technician does not have the required
                            // skills
                            return false;
                    }
                    throw new IllegalStateException(
                            "Illegal state when checking the feasibility of move " + move);
                }
            } else {
                if (tour.getInstance().hasRequiredTools(tour.getTechnician().getID(),
                        move.getNode()))
                    // The technician already has the tools to serve the shifted node, the move is feasible in all cases
                    return true;

                int pred = move.getNewSucc() != ITRSPTour.UNDEFINED ? tour.getPred(move
                        .getNewSucc()) : tour.getLastNode();
                if (tour.isMainDepotVisited(move.getNode()) == tour.isMainDepotVisited(pred)) {
                    // Both the shifted node and its new predecessor are on the same "side" of the tour (either before
                    // or after the depot visit), the move is therefore feasible
                    return true;
                } else {
                    // The shifted node and its new predecessor are on different "side" of the tour (one is before
                    // while the other is after the depot visit)
                    if (move.isForward()) {
                        // The node will be after the depot visit, the move is therefore feasible
                        return true;
                    } else {
                        // The node is shifted before the depot visit, while the technician does not have the required
                        // tools (tested before)
                        return false;
                    }
                }
            }
        }
    }

    @Override
    public boolean isFeasible(ITRSPTour tour) {
        TRSPInstance instance = tour.getInstance();
        AttributeWithIdSet tools = instance.getFleet().getVehicle(tour.getTechnicianId())
                .getToolSet();

        ITourIterator it = tour.iterator();

        boolean checkTools = true;

        while (it.hasNext() && checkTools) {
            int node = it.next();
            if (instance.isMainDepot(node)) {
                return true;
            } else if (instance.isRequest(node)) {
                if (!instance.getRequest(node).getToolSet().isCompatibleWith(tools)) {
                    // Tool i is required but missing
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    protected FeasibilityState checkFeasibility(ITRSPTour tour) {
        TRSPInstance instance = tour.getInstance();

        AttributeWithIdSet tools = instance.getFleet().getVehicle(tour.getTechnicianId())
                .getToolSet();

        ITourIterator it = tour.iterator();
        boolean checkTools = true;

        while (it.hasNext() && checkTools) {
            int node = it.next();
            if (instance.isMainDepot(node)) {
                // Assume all tools are now available
                return new FeasibilityState();
            } else if (instance.isRequest(node)) {
                if (!instance.getRequest(node).getToolSet().isCompatibleWith(tools)) {
                    // Tool i is required but missing
                    return new FeasibilityState(node, "Missing tools for request %s (%s)", node,
                            instance.getRequest(node).getToolSet());
                }

            }
        }

        return new FeasibilityState();
    }

    @Override
    public int checkFeasibility(ITRSPTour tour, IMove move) {
        // We assume that the tour data is updated and coherent
        if (move instanceof InsertionMove) {
            int feasibilty = checkInsFeasibility(tour, (InsertionMove) move);
            return (feasibilty == 1 ? 1 : 0) +
            // -1 if the technician does not have the required tools and no visit to main depot is planned
                    (feasibilty != -1 ? 2 : 0);
        } else {
            return isFeasible(tour, move) ? 3 : 2;
        }
    }

}
