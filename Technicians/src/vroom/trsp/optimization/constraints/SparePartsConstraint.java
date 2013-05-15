/**
 *
 */
package vroom.trsp.optimization.constraints;

import java.util.Arrays;

import vroom.common.utilities.optimization.IConstraint;
import vroom.common.utilities.optimization.IMove;
import vroom.trsp.datamodel.ITRSPTour;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPTour;
import vroom.trsp.datamodel.TRSPTour.TRSPTourIterator;
import vroom.trsp.optimization.InsertionMove;
import vroom.trsp.optimization.localSearch.TRSPShift.TRSPShiftMove;
import vroom.trsp.optimization.localSearch.TRSPTwoOpt.TRSPTwoOptMove;
import vroom.trsp.util.TRSPLogging;

/**
 * <code>SparePartsConstraint</code> is an implementation of {@link IConstraint} that checks if moves are feasible in
 * terms of spare parts constraints.
 * <p>
 * Creation date: Mar 28, 2011 - 10:32:52 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class SparePartsConstraint extends TourConstraintBase {

    /**
     * Check the feasibility of a move
     * 
     * @param tour
     * @param move
     * @return 1 if move is feasible, -1 if the technician does not have the spare parts and no visit to depot is
     *         planned, -2 if the spare part constraint is violated at one of the successors
     */
    private int checkInsFeasibility(ITRSPTour itour, InsertionMove move) {
        if (!TRSPTour.class.isAssignableFrom(itour.getClass())) {
            TRSPLogging
                    .getOptimizationLogger()
                    .warn(String
                            .format("SparePartsConstraint.checkInsFeasibility: unsupported tour class (%s)",
                                    itour.getClass()));
            return 1;
        }
        TRSPTour tour = (TRSPTour) itour;

        if (move.isDepotTrip() || !tour.getInstance().isRequest(move.getNodeId()))
            return 1;
        if (!tour.isMainDepotVisited()
                && !move.isDepotTrip()
                && !tour.getInstance().hasRequiredSpareParts(tour.getTechnicianId(),
                        move.getNodeId()))
            return -1;

        int pred = move.getInsertionPred();
        TRSPInstance instance = tour.getInstance();

        for (int s = 0; s < instance.getSpareCount(); s++) {
            // Available spare parts
            int av = tour.getAvailableSpareParts(pred, s) // Will return the initial spare parts if pred ==
                                                          // UNDEFINED
                    - itour.getInstance().getRequest(move.getNodeId()).getSparePartRequirement(s);
            if (tour.getRequiredSpareParts(move.getInsertionSucc(), s) > av)
                return -2;
        }

        return 1;
    }

    @Override
    protected boolean isInsFeasible(ITRSPTour tour, InsertionMove move) {
        return checkInsFeasibility(tour, move) == 1;
    }

    @Override
    protected boolean isTwoOptFeasible(ITRSPTour itour, TRSPTwoOptMove move) {
        if (!TRSPTour.class.isAssignableFrom(itour.getClass())) {
            TRSPLogging.getOptimizationLogger().warn(
                    String.format(
                            "SparePartsConstraint.isTwoOptFeasible: unsupported tour class (%s)",
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

        // Spare parts available at node i
        int[] spares = new int[tour.getInstance().getSpareCount()];
        for (int s = 0; s < spares.length; s++) {
            spares[s] = tour.getAvailableSpareParts(move.getFirst(), s);
        }
        // Start with node m and iterate backward
        int node = it.previous();
        while (it.hasNext() && node != tour.getMainDepotId()) {
            for (int s = 0; s < spares.length; s++) {
                spares[s] -= tour.getRequiredSpareParts(node, s);
                if (spares[s] < 0)
                    // The technician does not have the required spare parts for this request
                    return false;
            }
        }

        // No incompatible request was found, the move is feasible
        return true;
    }

    @Override
    protected boolean isShiftFeasible(ITRSPTour itour, TRSPShiftMove move) {
        if (!TRSPTour.class.isAssignableFrom(itour.getClass())) {
            TRSPLogging.getOptimizationLogger().warn(
                    String.format(
                            "SparePartsConstraint.isShiftFeasible: unsupported tour class (%s)",
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
                    for (int p = 0; p < tour.getInstance().getSpareCount(); p++) {
                        // The total requirement for this part
                        int reqTotal = tour.getRequiredSpareParts(tour.getFirstNode(), p);
                        // The requirement for the subtour that will be after the main depot visit
                        int reqFinal = tour.getRequiredSpareParts(move.getNewSucc(), p);
                        // Check if the technician has the spare parts parts for the subtour that will be before the
                        // main depot visit
                        if (reqTotal - reqFinal > tour.getTechnician().getAvailableSpareParts(p))
                            return false;
                    }
                    return true;
                }
            } else {
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
                        // The node is shifted before the depot visit, check if the technician will have the
                        // required spare parts for the nodes in between
                        for (int p = 0; p < tour.getInstance().getSpareCount(); p++) {
                            // The requirement for the whole tour
                            int reqTotal = tour.getRequiredSpareParts(tour.getFirstNode(), p);
                            // The requirement for the subtour that is after the main depot visit
                            int reqFinal = tour.getRequiredSpareParts(tour.getMainDepotId(), p);
                            // Check if the technician has the spare parts parts for the subtour that will be before the
                            // main depot visit
                            if (reqTotal
                                    - reqFinal
                                    + tour.getInstance().getRequest(move.getNode())
                                            .getSparePartRequirement(p) > tour.getTechnician()
                                    .getAvailableSpareParts(p))
                                return false;
                        }
                        return true;
                    }
                }
            }
        }
    }

    @Override
    protected FeasibilityState checkFeasibility(ITRSPTour tour) {
        TRSPInstance instance = tour.getInstance();

        int[] spare = tour.getInstance().getFleet().getVehicle(tour.getTechnicianId())
                .getSpareParts();

        for (int node : tour) {
            if (instance.isMainDepot(node)) {
                Arrays.fill(spare, Integer.MAX_VALUE);
            } else if (!instance.isDepot(node)) {
                for (int i = 0; i < spare.length; i++) {
                    spare[i] -= instance.getRequest(node).getSparePartRequirement(i);
                    if (spare[i] < 0) {
                        return new FeasibilityState(node,
                                "Missing spare part %s to serve request %s", i, node);
                    }
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
            // -1 if the technician does not have the spare parts and no visit to depot is planned
                    (feasibilty != -1 ? 2 : 0);
        } else {
            return isFeasible(tour, move) ? 3 : 1;
        }
    }

}
