/**
 *
 */
package vroom.trsp.datamodel.costDelegates;

import java.util.List;
import java.util.ListIterator;

import vroom.common.utilities.optimization.IMove;
import vroom.trsp.datamodel.ITRSPTour;
import vroom.trsp.datamodel.ITourIterator;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPTour;
import vroom.trsp.datamodel.TRSPTour.TRSPTourIterator;
import vroom.trsp.datamodel.Technician;
import vroom.trsp.optimization.InsertionMove;
import vroom.trsp.optimization.localSearch.TRSPShift.TRSPShiftMove;

/**
 * <code>TRSPTWViolationDelegate</code> is an implementation of {@link TRSPCostDelegate} that evaluates the time window
 * violations (i.e. the lateness at each node).
 * <p>
 * Creation date: Mar 24, 2011 - 5:20:20 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TRSPTWViolationDelegate extends TRSPCostDelegate {

    /** the max flag **/
    private boolean mMaxValue;

    /**
     * Getter for the max flag: <code>true</code> if only the maximum TW violation is recorded, <code>false</code> if
     * its the sum of TW violations
     * 
     * @return the value of the maxValue flag
     */
    public boolean isMaxValue() {
        return this.mMaxValue;
    }

    /**
     * Setter for the max flag<code>true</code> if only the maximum TW violation is recorded, <code>false</code> if its
     * the sum of TW violations
     * 
     * @param maxValue
     *            the value to be set for the max flag
     */
    public void setMaxValue(boolean maxValue) {
        this.mMaxValue = maxValue;
    }

    /**
     * Creates a new <code>TRSPTWViolationDelegate</code>
     */
    public TRSPTWViolationDelegate(boolean maxValue) {
        super();
        this.mMaxValue = maxValue;
    }

    @Override
    protected double evaluateTRSPTour(TRSPTour tour, int node, boolean updateTour) {
        if (updateTour && !tour.isAutoUpdated())
            throw new IllegalStateException(
                    "The tour must have its autoupdate flag set to true for proper working");
        // Cumulated working time
        double cost = node != ITRSPTour.UNDEFINED ? tour.getCumulatedCost(node) : 0;
        // Iterator over the tour
        TRSPTourIterator it = node != ITRSPTour.UNDEFINED ? tour.iterator(node) : tour.iterator();
        if (!it.hasNext()) {
            if (updateTour)
                tour.setTotalCost(cost);
            return cost;
        }
        // First node
        while (it.hasNext()) {
            node = it.next();

            double tmp = tour.getInstance().isDepot(node) ? 0 : tour.getLateness(node);

            cost = isMaxValue() ? Math.max(tmp, cost) : cost + tmp;

            // Set the cumulated cost
            if (updateTour) {
                tour.setTotalCost(cost);
                tour.setCumulatedCost(node, cost);
            }
        }
        // Set the total cost

        if (updateTour)
            tour.setTotalCost(cost);

        return cost;
    }

    @Override
    protected double evaluateGenericTour(ITRSPTour tour) {
        double cost = 0;

        if (tour.length() == 0)
            return 0;
        TRSPInstance ins = tour.getInstance();
        Technician tech = ins.getTechnician(tour.getTechnicianId());

        // Cumulated working time
        double time = 0;
        // Iterator over the tour
        ITourIterator it = tour.iterator();

        // First node
        int pred = it.next();
        while (it.hasNext()) {
            int node = it.next();
            // If time windows are enforced the vehicle has to wait until TW start
            time = ins.getTimeWindow(pred).getEarliestStartOfService(time);
            // Add the service time of predecessor
            time += ins.getServiceTime(pred);
            // Add the travel time
            time += ins.getCostDelegate().getTravelTime(pred, node, tech);
            pred = node;

            double tmp = ins.getTimeWindow(node).getViolation(time);
            cost = isMaxValue() ? Math.max(tmp, cost) : cost + tmp;
        }
        // If time windows are enforced the vehicle has to wait at the last depot until TW start
        time = ins.getTimeWindow(pred).getEarliestStartOfService(time);
        // Add the service time of the last node
        time += ins.getServiceTime(pred);

        return cost;
    }

    @Override
    public double getInsertionCost(TRSPTour route, int predecessor, List<Integer> insertedTour,
            int successor) {
        throw new UnsupportedOperationException("This method is not implemented yet");
    }

    @Override
    public double getInsertionCost(TRSPTour tour, int predecessor, int node, int successor) {
        if (!tour.isAutoUpdated())
            throw new IllegalStateException(
                    "The tour must have its autoupdate flag set to true for proper working");

        double cost = 0;
        double arrivalTime;
        if (predecessor != ITRSPTour.UNDEFINED) {
            // Evaluate arrival time at node
            arrivalTime = tour.getEarliestDepartureTime(predecessor)
                    + tour.getTravelTime(predecessor, node);
            cost = tour.getLateness(predecessor);
        } else
            // Node is the first node of the tour
            arrivalTime = tour.getEarliestStartTime();

        // The lateness at this node
        double lateness = tour.getInstance().isDepot(node) ? 0 : tour.getTimeWindow(node)
                .getViolation(arrivalTime);
        cost = isMaxValue() ? Math.max(cost, lateness) : cost + lateness;

        // Propagate to following nodes
        int pred = node;
        if (successor != ITRSPTour.UNDEFINED) {
            TRSPTourIterator it = tour.iterator(successor);
            while (it.hasNext()) {
                node = it.next();
                // Evaluate arrival time at node
                arrivalTime = tour.getTimeWindow(pred).getEarliestStartOfService(arrivalTime)
                        + tour.getServiceTime(pred) + tour.getTravelTime(pred, node);
                // The lateness at this node
                lateness = tour.getInstance().isDepot(node) ? 0 : tour.getTimeWindow(node)
                        .getViolation(arrivalTime);
                cost = isMaxValue() ? Math.max(cost, lateness) : cost + lateness;

                pred = node;
            }
        }

        return cost - tour.getTotalCost();
    }

    @Override
    protected double evaluateInsMove(InsertionMove move) {
        // FIXME include the cost of the trip to depot
        if (move.isDepotTrip())
            throw new UnsupportedOperationException();
        double improvement = -getInsertionCost(move.getTour(),
                move.getTour().getPred(move.getInsertionSucc()), move.getRequest().getID(),
                move.getInsertionSucc());
        return improvement;
    }

    /**
     * Specific implementation of {@link #evaluateMove(IMove)} for {@link TRSPShiftMove}
     * 
     * @param tour
     *            the considered tour
     * @param move
     *            the move to be evaluated
     * @return the improvement resulting from the execution of <code>move</code> on <code>tour</code>
     */
    @Override
    public double evaluateShiftMove(TRSPShiftMove move) {
        if (!move.getTour().isAutoUpdated())
            throw new IllegalStateException(
                    "The tour must have its autoupdate flag set to true for proper working");

        // Build the changed sequence
        List<Integer> changedSequence = move.getChangedSequence();

        // The changed sequence predecessor (unchanged)
        ListIterator<Integer> it = changedSequence.listIterator();

        // The last node that is unchanged
        int lastUnchanged = it.next();

        // The evaluation of the tour at the last unchanged node
        double evaluation = lastUnchanged != ITRSPTour.UNDEFINED ? move.getTour().getCumulatedCost(
                lastUnchanged) : isMaxValue() ? Double.NEGATIVE_INFINITY : 0;

        double newEarliestDeparture = lastUnchanged != ITRSPTour.UNDEFINED ? move.getTour()
                .getEarliestDepartureTime(lastUnchanged) : move.getTour().getEarliestStartTime();

        int pred = lastUnchanged;

        while (it.hasNext()) {
            int n = it.next();
            if (pred != ITRSPTour.UNDEFINED)
                newEarliestDeparture += move.getTour().getTravelTime(pred, n);
            // The violation is equal to the difference between the arrival time and the TW end
            double newViolation = move.getTour().getTimeWindow(n)
                    .getViolation(newEarliestDeparture);
            newEarliestDeparture = move.getTour().getTimeWindow(n)
                    .getEarliestStartOfService(newEarliestDeparture)
                    + move.getTour().getServiceTime(n);

            if (isMaxValue()) {
                if (newViolation > evaluation) {
                    evaluation = newViolation;
                }
            } else {
                evaluation += newViolation;
            }
            pred = n;
        }

        double improvement = move.getTour().getTotalCost() - evaluation;
        return improvement;

    }

    @Override
    public boolean isInsertionSeqDependent() {
        return true;
    }
}
