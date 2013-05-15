/**
 *
 */
package vroom.trsp.optimization.constraints;

import java.util.List;
import java.util.ListIterator;

import vroom.common.utilities.optimization.IConstraint;
import vroom.common.utilities.optimization.IMove;
import vroom.trsp.datamodel.ITRSPTour;
import vroom.trsp.datamodel.ITourIterator;
import vroom.trsp.datamodel.TRSPDistanceMatrix;
import vroom.trsp.datamodel.TRSPTour;
import vroom.trsp.datamodel.TRSPTour.TRSPTourIterator;
import vroom.trsp.datamodel.Technician;
import vroom.trsp.optimization.InsertionMove;
import vroom.trsp.optimization.localSearch.TRSPShift.TRSPShiftMove;
import vroom.trsp.optimization.localSearch.TRSPTwoOpt.TRSPTwoOptMove;
import vroom.trsp.util.TRSPLogging;

/**
 * <code>TWConstraint</code> is an implementation of {@link IConstraint} that checks if moves are feasible in terms of
 * time window constraints.
 * <p>
 * Creation date: Mar 28, 2011 - 10:49:11 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TWConstraint extends TourConstraintBase {

    @Override
    protected boolean isTwoOptFeasible(ITRSPTour itour, TRSPTwoOptMove move) {
        if (!TRSPTour.class.isAssignableFrom(itour.getClass())) {
            TRSPLogging.getOptimizationLogger().warn(
                    String.format("TWConstraint.isTwoOptFeasible: unsupported tour class (%s)",
                            itour.getClass()));
            return true;
        }
        TRSPTour tour = (TRSPTour) itour;

        final int i = move.getFirst();
        final int j = tour.getSucc(i);
        final int m = move.getSecond();
        final int n = tour.getSucc(m);

        // The arrival time at the head of the first arc
        double arrival = tour.getEarliestArrivalTime(i);

        // Check the reversed subtour
        // We start with (i,m) then iterates to (j+1,j)
        int pred = i;
        TRSPTourIterator it = tour.iterator(m);
        while (pred != j && it.hasPrevious()) {
            int node = it.previous();
            // Update the arrival time
            arrival = tour.getTimeWindow(pred).getEarliestStartOfService(arrival)
                    + tour.getServiceTime(pred) + tour.getTravelTime(pred, node);
            if (!tour.getTimeWindow(node).isFeasible(arrival))
                // Found a violated request
                return false;
            pred = node;
        }
        // Check (j,n)
        // Earliest arrival time at node n
        arrival = tour.getTimeWindow(j).getEarliestStartOfService(arrival) + tour.getServiceTime(j)
                + tour.getTravelTime(j, n);
        // compare it against the latest feasible time
        if (arrival > tour.getLatestFeasibleArrivalTime(n))
            // The arrival time will not allow to visit the remaining nodes
            return false;
        else
            // The TW constraint is satisfied
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
    protected boolean isShiftFeasible(ITRSPTour itour, TRSPShiftMove move) {
        if (!TRSPTour.class.isAssignableFrom(itour.getClass())) {
            TRSPLogging.getOptimizationLogger().warn(
                    String.format("TWConstraint.isShiftFeasible: unsupported tour class (%s)",
                            itour.getClass()));
            return true;
        }
        TRSPTour tour = (TRSPTour) itour;

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
        double newArrivalTime = lastUnchanged != ITRSPTour.UNDEFINED ? tour
                .getEarliestDepartureTime(lastUnchanged) : tour.getEarliestStartTime();

        // If the move is backward we always allow skip
        boolean allowSkip = false;
        int pred = lastUnchanged;
        while (it.hasNext()) {
            int n = it.next();
            if (pred != ITRSPTour.UNDEFINED)
                // Add the service time and travel time from predecessor
                newArrivalTime = tour.getTimeWindow(pred).getEarliestStartOfService(newArrivalTime)
                        + tour.getServiceTime(pred) + tour.getTravelTime(pred, n);
            if (!tour.getTimeWindow(n).isFeasible(newArrivalTime))
                return false;
            if (allowSkip && newArrivalTime <= tour.getEarliestDepartureTime(n))
                // We can skip the evaluation of the rest of the tour as we are sure that we will arrive earlier at each
                // node
                return true;

            // We allow skip after we checked the moved node
            allowSkip |= n == move.getNode();
            pred = n;
        }

        return true;
    }

    /**
     * Check if a {@linkplain InsertionMove shift move} is feasible for the given <code>solution</code>
     * 
     * @param solution
     *            the base solution
     * @param move
     *            the {@link InsertionMove} to be tested
     * @return <code>true</code> if applying <code>move</code> to the given <code>solution</code> will result in a
     *         feasible mSolution
     */
    @Override
    public boolean isInsFeasible(ITRSPTour tour, InsertionMove move) {
        return checkInsFeasibility(tour, move) == 1;
    }

    /**
     * Check if a {@linkplain InsertionMove shift move} is feasible for the given <code>solution</code>
     * 
     * @param solution
     *            the base solution
     * @param move
     *            the {@link InsertionMove} to be tested
     * @return 1 if move is feasible, -1 if there is no arc with successor, -2 if there is no arc with predecessor, -3
     *         if the node tw is violated, -4 if the tw of a successor is violated, and -5 if the tw of an intermediary
     *         node is violated
     */
    protected int checkInsFeasibility(ITRSPTour itour, InsertionMove move) {
        if (!TRSPTour.class.isAssignableFrom(itour.getClass())) {
            TRSPLogging.getOptimizationLogger().warn(
                    String.format("TWConstraint.isShiftFeasible: unsupported tour class (%s)",
                            itour.getClass()));
            return 1;
        }
        TRSPTour tour = (TRSPTour) itour;

        final int node = move.getNodeId();
        // Check if arc exists
        if ((move.getInsertionSucc() != ITRSPTour.UNDEFINED && !tour.getInstance().isArcTWFeasible(
                move.getInsertionPred(), node))//
                || (move.getInsertionPred() != ITRSPTour.UNDEFINED && !tour.getInstance()
                        .isArcTWFeasible(node, move.getInsertionSucc())))
            return -1;

        final int pred = move.getInsertionPred();
        double arrivalTime = 0;

        if (pred == ITRSPTour.UNDEFINED) {
            // Earliest arrival time at the inserted request (equal to the start of shift of the technician)
            arrivalTime = tour.getEarliestStartTime();
            if (move.isDepotTrip() && move.getDepotSucc() == node)
                arrivalTime += tour.getTravelTime(tour.getMainDepotId(), node)
                        + tour.getServiceTime(tour.getMainDepotId());
        } else {
            // Check if arc exists
            if (!tour.getInstance().isArcTWFeasible(pred, node))
                return -2;
            // Earliest arrival time at the inserted request depending on the previous request and possible depot trip
            if (move.isDepotTrip()) {

                if (move.getDepotSucc() == node) {
                    // The depot is visited immediately before
                    arrivalTime = tour.getEarliestDepartureTime(pred)
                            + tour.getTravelTime(pred, tour.getMainDepotId())
                            + tour.getServiceTime(tour.getMainDepotId())
                            + tour.getTravelTime(tour.getMainDepotId(), node);
                } else {
                    final int depotSucc = move.getDepotSucc();
                    final int depotPred = tour.getPred(move.getDepotSucc());
                    // The depot is visited earlier in the tour
                    // Waiting times method
                    // -----------------------------------
                    // Extra time required to visit depot (we assume there is no waiting at the depot)
                    final double depotTrip = tour.getTravelTime(depotPred, tour.getMainDepotId())
                            + tour.getServiceTime(tour.getMainDepotId())
                            + tour.getTravelTime(tour.getMainDepotId(), depotSucc);

                    // Check that the depot insertion is feasible
                    if (tour.getEarliestDepartureTime(depotPred) + depotTrip > tour
                            .getLatestFeasibleArrivalTime(depotSucc))
                        return -5;

                    // Delta in the arrival time at the depot successor
                    final double delta = depotTrip - tour.getTravelTime(depotPred, depotSucc);

                    double predArrivalTime = tour.getEarliestArrivalTime(pred);
                    // New arrival time at the insertion predecessor
                    predArrivalTime = Math.max(predArrivalTime,
                            predArrivalTime + delta - tour.getWaitingTime(depotPred, pred));

                    // Earliest arrival time at the inserted node
                    arrivalTime = tour.getTimeWindow(pred).getEarliestStartOfService(
                            predArrivalTime)
                            + tour.getServiceTime(pred) + tour.getTravelTime(pred, node);
                }
            } else {
                // The depot is not visited
                arrivalTime = tour.getEarliestDepartureTime(pred) + tour.getTravelTime(pred, node);
            }
        }
        // Check if the earliest arrival time at the request is compatible with its time window
        if (!tour.getTimeWindow(node).isFeasible(arrivalTime))
            return -3;
        else if (move.getInsertionSucc() == ITRSPTour.UNDEFINED)
            // The insertion consists in appending the node, nothing else to check
            return 1;

        // Estimate the earliest arrival time to the insertionSucc request
        arrivalTime = tour.getTimeWindow(node).getEarliestStartOfService(arrivalTime)
                + tour.getServiceTime(node) + tour.getTravelTime(node, move.getInsertionSucc());
        // Check if the earliest arrival is compatible with the latest feasible arrival time
        if (arrivalTime > tour.getLatestFeasibleArrivalTime(move.getInsertionSucc()))
            return -4;
        else
            return 1;
    }

    @Override
    protected FeasibilityState checkFeasibility(ITRSPTour itour) {
        if (itour.length() == 0)
            return null;
        else if (TRSPTour.class.isAssignableFrom(itour.getClass())) {
            // Fast check for instances of TRSPTour using stored info
            TRSPTour tour = (TRSPTour) itour;
            for (int n : tour) {
                if (!tour.getTimeWindow(n).isFeasible(tour.getEarliestArrivalTime(n))) {
                    return new FeasibilityState(n, "%s:TW violated (twe:%s ea:%.1f)", n, tour
                            .getTimeWindow(n).endAsDouble(), tour.getEarliestArrivalTime(n));
                }
            }
            return new FeasibilityState();
        } else {
            TRSPDistanceMatrix cd = itour.getInstance().getCostDelegate();
            Technician tech = itour.getInstance().getFleet().getVehicle(itour.getTechnicianId());
            double time = itour.getInstance().getTimeWindow(itour.getFirstNode()).startAsDouble();
            ITourIterator it = itour.iterator();
            int pred = it.next();
            while (it.hasNext()) {
                int n = it.next();
                time = itour.getInstance().getTimeWindow(pred).getEarliestStartOfService(time);
                time += itour.getInstance().getServiceTime(pred);
                time += cd.getTravelTime(pred, n, tech);
                if (!itour.getInstance().getTimeWindow(n).isFeasible(time)) {
                    return new FeasibilityState(n, "%s:TW violated (twe:%s ea:%.1f)", n, itour
                            .getInstance().getTimeWindow(n).endAsDouble(), time);
                }

                pred = n;
            }
            return new FeasibilityState();
        }
    }

    @Override
    public int checkFeasibility(ITRSPTour tour, IMove move) {
        if (InsertionMove.class.isAssignableFrom(move.getClass())) {
            InsertionMove mve = (InsertionMove) move;
            int feasibility = checkInsFeasibility(tour, mve);
            int fwdFeas;
            if (mve.isDepotTrip()) {
                // No deduction can be done
                fwdFeas = 2;
            } else {
                // The move is not fwd feasible if:
                // -2 there is no arc with predecessor
                // -3 the node tw is violated
                fwdFeas = (feasibility != -3 && feasibility != -2) ? 2 : 0;
            }
            return feasibility == 1 ? 1 + fwdFeas : fwdFeas;
        } else {
            return isFeasible(tour, move) ? 3 : 2;
        }

    }

}
