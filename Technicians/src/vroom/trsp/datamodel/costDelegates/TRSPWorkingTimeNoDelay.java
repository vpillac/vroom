/**
 *
 */
package vroom.trsp.datamodel.costDelegates;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import vroom.common.utilities.optimization.IMove;
import vroom.trsp.datamodel.ITRSPTour;
import vroom.trsp.datamodel.ITourIterator;
import vroom.trsp.datamodel.TRSPDistanceMatrix;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPRequest;
import vroom.trsp.datamodel.TRSPTour;
import vroom.trsp.datamodel.TRSPTour.TRSPTourIterator;
import vroom.trsp.datamodel.Technician;
import vroom.trsp.optimization.InsertionMove;
import vroom.trsp.optimization.RemoveMove;
import vroom.trsp.optimization.localSearch.TRSPShift.TRSPShiftMove;
import vroom.trsp.optimization.localSearch.TRSPTwoOpt.TRSPTwoOptMove;

/**
 * <code>TRSPWorkingTime</code> is an implementation of {@link TRSPCostDelegate} that calculates and maintain the
 * cumulated working time at each node.
 * <p>
 * This class sums the {@linkplain TRSPRequest#getServiceTime() service time} and
 * {@linkplain TRSPDistanceMatrix#getTravelTime(int, int, Technician) travel time} at each node of a tour.
 * </p>
 * <p>
 * Creation date: Feb 28, 2011 - 9:07:52 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TRSPWorkingTimeNoDelay extends TRSPCostDelegate {

    public TRSPWorkingTimeNoDelay() {
        super();
    }

    /**
     * Evaluate a portion of the tour starting at the given <code>node</code>
     * 
     * @param tour
     *            the tour to be evaluated
     * @param node
     *            the node at which reevaluation should start
     * @param updateTour
     *            <code>true</code> if the tour stored costs have to be updated
     */
    @Override
    protected double evaluateTRSPTour(TRSPTour tour, int node, boolean updateTour) {

        // Special case for singleton route
        if (tour.length() == 1) {
            node = tour.getFirstNode();
            double wt = tour.getTimeWindow(node).getEarliestStartOfService(
                    tour.getEarliestStartTime())
                    + tour.getServiceTime(node);
            if (updateTour) {
                tour.setCumulativeCost(node, wt);
                tour.setTotalCost(wt);
            }
            return wt;
        }

        // Cumulated working time
        double wt = node != ITRSPTour.UNDEFINED ? tour.getCumulativeCost(node) : tour
                .getEarliestStartTime();
        // Iterator over the tour
        TRSPTourIterator it = node != ITRSPTour.UNDEFINED ? tour.iterator(node) : tour.iterator();
        if (!it.hasNext()) {
            if (updateTour)
                tour.setTotalCost(wt);
            return wt;
        }
        // First node
        int pred = it.next();
        while (it.hasNext()) {
            node = it.next();
            // If time windows are enforced the vehicle has to wait until TW start
            wt = tour.getTimeWindow(pred).getEarliestStartOfService(wt);
            // Add the service time of predecessor
            wt += tour.getServiceTime(pred);
            // Add the travel time
            wt += tour.getTravelTime(pred, node);
            if (updateTour)
                // Set the cumulative cost
                tour.setCumulativeCost(node, wt);
            pred = node;
        }
        // If time windows are enforced the vehicle has to wait at the last depot until TW start
        wt = tour.getTimeWindow(pred).getEarliestStartOfService(wt);
        // Add the service time of the last node
        wt += tour.getServiceTime(pred);

        if (updateTour)
            // Set the total cost
            tour.setTotalCost(wt);

        return wt;
    }

    @Override
    protected double evaluateGenericTour(ITRSPTour tour) {
        if (tour.length() == 0)
            return 0;
        TRSPInstance ins = tour.getInstance();
        Technician tech = ins.getTechnician(tour.getTechnicianId());

        // Cumulated working time
        double wt = 0;
        // Iterator over the tour
        ITourIterator it = tour.iterator();

        // First node
        int pred = it.next();
        while (it.hasNext()) {
            int node = it.next();
            // If time windows are enforced the vehicle has to wait until TW start
            wt = ins.getTimeWindow(pred).getEarliestStartOfService(wt);
            // Add the service time of predecessor
            wt += ins.getServiceTime(pred);
            // Add the travel time
            wt += ins.getCostDelegate().getTravelTime(pred, node, tech);
            pred = node;
        }
        // If time windows are enforced the vehicle has to wait at the last depot until TW start
        wt = ins.getTimeWindow(pred).getEarliestStartOfService(wt);
        // Add the service time of the last node
        wt += ins.getServiceTime(pred);

        return wt;
    }

    @Override
    public double evaluateDetour(ITRSPTour itour, int predecessor, int node, int successor,
            boolean isRemoval) {
        final boolean isTRSPTour = TRSPTour.class.isAssignableFrom(itour.getClass());

        // Evaluate the arrival time at the inserted node
        // ----------------------------------
        double arrivalTimeAtPred = Double.NaN;
        final double detourAarrivalTimeAtNode;
        ITourIterator tourIt = null;
        if (predecessor == ITRSPTour.UNDEFINED) {
            detourAarrivalTimeAtNode = itour.getInstance().getTechnician(itour.getTechnicianId())
                    .getHome().getTimeWindow().startAsDouble();
            tourIt = itour.iterator();
        } else {
            if (isTRSPTour) {
                // Faster implementation using stored arrival times
                // --
                TRSPTour tour = (TRSPTour) itour;
                if (!tour.isAutoUpdated())
                    throw new IllegalStateException(
                            "The tour must have its autoupdate flag set to true for proper working");

                tourIt = tour.iterator(successor);

                // Evaluate arrival time at the predecessor
                arrivalTimeAtPred = tour.getEarliestArrivalTime(predecessor);
            } else {
                arrivalTimeAtPred = itour.getInstance().getTechnician(itour.getTechnicianId())
                        .getHome().getTimeWindow().startAsDouble();

                // Evaluate the arrival time up to the predecessor
                tourIt = itour.iterator();
                int pred = tourIt.next();
                int n = ITRSPTour.UNDEFINED;
                while (tourIt.hasNext() && n != predecessor) {
                    n = tourIt.next();
                    arrivalTimeAtPred = itour.getInstance().calculateArrivalTime(n, pred,
                            arrivalTimeAtPred, itour.getTechnicianId());
                    pred = n;
                }
                if (n != predecessor && !tourIt.hasNext())
                    throw new IllegalStateException(
                            "Reached the end of the tour without finding the predecessor");
            }
            // Evaluate the arrival time at node
            detourAarrivalTimeAtNode = itour.getInstance().calculateArrivalTime(node, predecessor,
                    arrivalTimeAtPred, itour.getTechnicianId());
        }

        // Evaluate arrival time at the successor
        // ----------------------------------
        final double detourArrivalTimeSucc = itour.getInstance().calculateArrivalTime(successor,
                node, detourAarrivalTimeAtNode, itour.getTechnicianId());

        if (!isRemoval && isTRSPTour && predecessor != ITRSPTour.UNDEFINED) {
            // Faster implementation using stored waiting times
            // --

            // The detour time will be compensated by the waiting time (if feasible)
            // FIXME check if the calculation of the insertion cost is correct
            return Math.max(0,
                    detourArrivalTimeSucc - ((TRSPTour) itour).getEarliestArrivalTime(successor)
                            - ((TRSPTour) itour).getWaitingTime(predecessor, itour.getLastNode()));
        } else {
            // Evaluate the arrival time at the last node
            int pred = tourIt.next();// Will return "successor" as we stopped at "predecessor"
            int n = ITRSPTour.UNDEFINED;
            double detourArrivalTimeAtLast = detourArrivalTimeSucc;
            double directArrivalTimeAtLast = itour.getInstance().calculateArrivalTime(successor,
                    predecessor, arrivalTimeAtPred, itour.getTechnicianId());
            while (tourIt.hasNext()) {
                n = tourIt.next();
                detourArrivalTimeAtLast = itour.getInstance().calculateArrivalTime(n, pred,
                        detourArrivalTimeAtLast, itour.getTechnicianId());
                directArrivalTimeAtLast = itour.getInstance().calculateArrivalTime(n, pred,
                        directArrivalTimeAtLast, itour.getTechnicianId());
                pred = n;
            }
            return detourArrivalTimeAtLast - directArrivalTimeAtLast;
        }
    }

    /**
     * Evaluates the {@linkplain IMove#getImprovement() improvement} of a {@link InsertionMove}
     * 
     * @param move
     *            the move to be evaluated
     * @return the {@linkplain IMove#getImprovement() improvement} resulting from the execution of the specified
     *         <code>move</code>
     */
    @Override
    protected double evaluateInsMove(InsertionMove move) {
        if (TRSPTour.class.isAssignableFrom(move.getTour().getClass())) {
            TRSPTour tour = (TRSPTour) move.getTour();
            double improvement = 0;
            if (!move.isDepotTrip()) {
                // Simple insertion
                improvement = -evaluateDetour(tour, move.getInsertionPred(), move.getNodeId(),
                        move.getInsertionSucc(), false);
            } else {
                LinkedList<Integer> changedSequence = new LinkedList<Integer>();
                changedSequence.add(0);
                int pred = ITRSPTour.UNDEFINED;
                if (move.getDepotSucc() == move.getNodeId()) {
                    // The trip to the depot immediately precedes the new request
                    changedSequence.add(move.getNodeId());
                    pred = move.getInsertionPred();
                } else {
                    // The trip to the depot is inserted before the new request
                    TRSPTourIterator it = tour.iterator(move.getDepotSucc());
                    int next = it.next();
                    while (next != move.getInsertionSucc() && it.hasNext()) {
                        changedSequence.add(next);
                        next = it.next();
                    }
                    // The trip to the depot immediately precedes the new request
                    changedSequence.add(move.getNodeId());
                    pred = tour.getPred(move.getDepotSucc());
                }
                // Add the rest of the route
                TRSPTourIterator it = tour.iterator(move.getInsertionSucc());
                while (it.hasNext()) {
                    changedSequence.add(it.next());
                }
                // Earliest arrival time at the depot
                double arrivalTime = tour.getEarliestDepartureTime(pred)
                        + tour.getTravelTime(pred, 0);
                Iterator<Integer> seq = changedSequence.iterator();
                pred = seq.next();
                while (seq.hasNext()) {
                    int node = seq.next();

                    arrivalTime = tour.getTimeWindow(pred).getEarliestStartOfService(arrivalTime);
                    arrivalTime += tour.getServiceTime(pred) + tour.getTravelTime(pred, node);
                    pred = node;
                }
                // Evaluate end of service at the last node
                arrivalTime = tour.getTimeWindow(tour.getLastNode()).getEarliestStartOfService(
                        arrivalTime)
                        + tour.getServiceTime(tour.getLastNode());

                improvement = tour.getTotalCost() - arrivalTime;
            }

            move.setImprovement(improvement);

            return improvement;
        } else {
            throw new UnsupportedOperationException("Unsupported tour type: "
                    + move.getTour().getClass());
        }
    }

    /**
     * Evaluates the {@linkplain IMove#getImprovement() improvement} of a {@link TRSPShiftMove}
     * 
     * @param move
     *            the move to be evaluated
     * @return the {@linkplain IMove#getImprovement() improvement} resulting from the execution of the specified
     *         <code>move</code>
     */
    @Override
    protected double evaluateShiftMove(TRSPShiftMove move) {
        TRSPTour tour = (TRSPTour) move.getTour();

        if (!tour.isAutoUpdated())
            throw new IllegalStateException(
                    "The tour must have its autoupdate flag set to true for proper working");

        // Build the changed sequence
        List<Integer> changedSequence = move.getChangedSequence();

        // The changed sequence predecessor (unchanged)
        ListIterator<Integer> it = changedSequence.listIterator();

        // The last node that is unchanged
        int lastUnchanged = it.next();

        // Departure time at the last unchanged node
        double newEarliestDeparture = lastUnchanged != ITRSPTour.UNDEFINED ? tour
                .getEarliestDepartureTime(lastUnchanged) : tour.getEarliestStartTime();

        int pred = lastUnchanged;
        while (it.hasNext()) {
            int n = it.next();
            if (pred != ITRSPTour.UNDEFINED)
                // Add the travel time from predecessor
                newEarliestDeparture += tour.getTravelTime(pred, n);
            // Evaluate the new earliest departure time at node n
            newEarliestDeparture = tour.getTimeWindow(n).getEarliestStartOfService(
                    newEarliestDeparture)
                    + tour.getServiceTime(n);

            pred = n;
        }

        double improvement = tour.getTotalCost() - newEarliestDeparture;
        move.setImprovement(improvement);
        return improvement;

    }

    /**
     * Evaluates the {@linkplain IMove#getImprovement() improvement} of a {@link TRSPTwoOptMove}
     * 
     * @param move
     *            the move to be evaluated
     * @return the {@linkplain IMove#getImprovement() improvement} resulting from the execution of the specified
     *         <code>move</code>
     */
    @Override
    protected double evaluateTwoOptMove(TRSPTwoOptMove move) {

        TRSPTour tour = (TRSPTour) move.getTour();

        if (!tour.isAutoUpdated())
            throw new IllegalStateException(
                    "The tour must have its autoupdate flag set to true for proper working");

        int n = tour.getSucc(move.getSecond());

        // The arrival time at the considered node
        double time = tour.getEarliestArrivalTime(move.getFirst());

        TRSPTourIterator it = tour.iterator(move.getFirst());
        int pred = it.next();
        while (pred != n && it.hasNext()) {
            int node = it.next();
            // Update the arrival time
            time = tour.getTimeWindow(pred).getEarliestStartOfService(time)
                    + tour.getServiceTime(pred) + tour.getTravelTime(pred, node);
            pred = node;
        }

        it = tour.iterator(n);
        // Skip the first node (n)
        it.next();
        while (it.hasNext()) {
            int node = it.next();
            // Update the arrival time
            time = tour.getTimeWindow(pred).getEarliestStartOfService(time)
                    + tour.getServiceTime(pred) + tour.getTravelTime(pred, node);
            pred = node;
        }

        return tour.getTotalCost() - time;
    }

    @Override
    protected double evaluateRemMove(RemoveMove move) {
        TRSPTour tour = (TRSPTour) move.getTour();

        if (!tour.isAutoUpdated())
            throw new IllegalStateException(
                    "The tour must have its autoupdate flag set to true for proper working");
        int pred = tour.getPred(move.getNodeId());

        // The new end of service time
        double time = tour.getEarliestDepartureTime(pred);
        TRSPTourIterator it = tour.iterator(tour.getSucc(move.getNodeId()));
        while (it.hasNext()) {
            int node = it.next();
            // Add the travel time
            time += tour.getTravelTime(pred, node);
            // Consider possible waiting time and service time
            time = tour.getTimeWindow(node).getEarliestStartOfService(time)
                    + tour.getServiceTime(node);
            pred = node;
        }

        return tour.getTotalCost() - time;
    }

    @Override
    public boolean isInsertionSeqDependent() {
        return true;
    }
}
