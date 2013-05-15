/**
 *
 */
package vroom.trsp.datamodel.costDelegates;

import static java.lang.Math.max;
import static java.lang.Math.min;
import vroom.common.utilities.Utilities;
import vroom.common.utilities.optimization.IMove;
import vroom.trsp.datamodel.ITRSPTour;
import vroom.trsp.datamodel.ITourIterator;
import vroom.trsp.datamodel.TRSPDistanceMatrix;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPRequest;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.datamodel.TRSPSolutionCheckerBase;
import vroom.trsp.datamodel.TRSPTour;
import vroom.trsp.datamodel.Technician;
import vroom.trsp.optimization.InsertionMove;
import vroom.trsp.optimization.RemoveMove;
import vroom.trsp.optimization.localSearch.TRSPShift.TRSPShiftMove;
import vroom.trsp.optimization.localSearch.TRSPTwoOpt.TRSPTwoOptMove;
import vroom.trsp.util.TRSPLogging;

/**
 * <code>TRSPWorkingTime</code> is an implementation of {@link TRSPCostDelegate} that calculates and maintain the
 * cumulative working time at each node.
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
public class TRSPWorkingTime extends TRSPCostDelegate {

    public TRSPWorkingTime() {
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
     * @return the total cost of the tour
     */
    @Override
    protected double evaluateTRSPTour(TRSPTour tour, int node, boolean updateTour) {
        if (!tour.isAutoUpdated())
            if (updateTour)
                tour.updateTimeInformation();
            else
                throw new IllegalStateException(
                        "Cannot evaluate a tour that is not autoupdated with the updateFlag set to false");

        // We assume that the tour contains up to date information
        double duration = tour.getMinimalDuration();
        if (updateTour)
            tour.setTotalCost(duration);
        return duration;
    }

    @Override
    public double evaluateGenericTour(ITRSPTour tour) {
        if (tour.length() == 0)
            return 0;
        TRSPInstance ins = tour.getInstance();
        Technician tech = ins.getTechnician(tour.getTechnicianId());

        // Cumulative working time
        double arrivalTime = 0;
        // Cumulative waiting time
        double wait = 0;
        // Iterator over the tour
        ITourIterator it = tour.iterator();

        // First node
        int pred = it.next();
        while (it.hasNext()) {
            int node = it.next();
            // If time windows are enforced the vehicle has to wait until TW start
            if (pred != tour.getFirstNode())
                wait += ins.getTimeWindow(pred).getWaiting(arrivalTime);
            arrivalTime = ins.getTimeWindow(pred).getEarliestStartOfService(arrivalTime);
            // Add the service time of predecessor
            arrivalTime += ins.getServiceTime(pred);
            // Add the travel time
            arrivalTime += ins.getCostDelegate().getTravelTime(pred, node, tech);
            pred = node;
        }
        // If time windows are enforced the vehicle has to wait at the last depot until TW start
        arrivalTime = ins.getTimeWindow(pred).getEarliestStartOfService(arrivalTime);
        // Add the service time of the last node
        arrivalTime += ins.getServiceTime(pred);

        // Evaluate the slack
        double tws0 = tech.getHome().getTimeWindow().startAsDouble();
        double F0 = TRSPSolutionCheckerBase.evaluateFwdSlackTime(tour, tws0, tour.getFirstNode(),
                tour.getLastNode());

        // Evaluate the TW duration
        double wt = arrivalTime - (tws0 + min(F0, wait));

        return wt;
    }

    @Override
    public double evaluateDetour(ITRSPTour itour, int i, int r, int j, boolean isRemoval) {
        return evaluateDetour(itour, i, r, j, ITRSPTour.UNDEFINED, ITRSPTour.UNDEFINED,
                ITRSPTour.UNDEFINED, isRemoval);
    }

    /**
     * Evaluate the detour represented by the insertion of {@code  r} between {@code  m} and {@code  n} and {@code  q}
     * between {@code  i} and {@code  j}
     * 
     * @param itour
     * @param m
     * @param r
     * @param n
     * @param i
     * @param q
     * @param j
     * @param isRemoval
     *            true if the detour corresponds to a removal
     * @return the corresponding detour (change in cost)
     */
    public double evaluateDetour(ITRSPTour itour, int m, int r, int n, int i, int q, int j,
            boolean isRemoval) {
        if (!TRSPTour.class.isAssignableFrom(itour.getClass()))
            throw new UnsupportedOperationException("Unsupported tour type: " + itour.getClass());
        TRSPTour tour = (TRSPTour) itour;
        if (!tour.isAutoUpdated())
            throw new IllegalStateException(
                    "The tour needs to have its autoUpdated flag set to true");
        if (m == ITRSPTour.UNDEFINED)
            throw new IllegalStateException("Insertion as first node is not supported");

        final boolean simpleInsertion = q == ITRSPTour.UNDEFINED;

        final int pi0 = tour.getFirstNode();
        final int pil = tour.getLastNode();

        // Evaluate arrival time at the predecessor
        double Am = tour.getEarliestArrivalTime(m);
        if (isRemoval) {
            // throw new UnsupportedOperationException("Removal evaluation is not implemented yet");
            TRSPLogging.getComponentsLogger().warn(
                    "TRSPWorkingTime.evaluateDetour: removal evaluation is very costly");

            TRSPSolution clone = tour.getSolution().clone();
            TRSPTour tourClone = clone.getTour(tour.getTechnicianId());
            tourClone.removeNode(q);
            tourClone.removeNode(r);
            return evaluateTour(tour, false) - evaluateTour(tourClone, false);
        } else {
            // Special case : n==q - nodes are inserted next to each other
            final boolean adj = n == q;

            final int r1 = r;
            final int r2 = adj ? q : r;

            if (adj) {
                // We "merge" r and q in a single node R
                n = j;
            }

            final double An = tour.getEarliestArrivalTime(n);
            // Change in the arrival time at the successor
            // ----------------------------------------
            // Evaluate the arrival time at the inserted node
            double Ar1 = tour.getInstance().calculateArrivalTime(r1, m, Am, tour.getTechnicianId());
            double Ar2 = adj ? tour.getInstance().calculateArrivalTime(r2, r1, Ar1,
                    tour.getTechnicianId()) : Ar1;

            // Evaluate arrival time at n
            final double AnPrime = tour.getInstance().calculateArrivalTime(n, r2, Ar2,
                    tour.getTechnicianId());

            final double Deltan = AnPrime - An;
            // ----------------------------------------

            double Wr1 = tour.getTimeWindow(r1).getWaiting(Ar1);
            double Wr2 = adj ? tour.getTimeWindow(r2).getWaiting(Ar2) : 0;
            double Wr = Wr1 + Wr2;

            // New forward slack time
            // ----------------------------------------
            final double F0m = tour.getFwdSlackTime(pi0, m);
            double secondTerm;
            if (adj)
                // In the adj case the second term is the minimum between the expression
                // applied to r1 and to r2
                secondTerm = min(
                        tour.getTimeWindow(r1).endAsDouble() - Ar1 + tour.getWaitingTime(pi0, n),//
                        tour.getTimeWindow(r2).endAsDouble() - Ar2 + tour.getWaitingTime(pi0, n)
                                + Wr1);
            else
                secondTerm = tour.getTimeWindow(r2).endAsDouble() - Ar2
                        + tour.getWaitingTime(pi0, n);
            // ----------------------------------------

            if (simpleInsertion || adj) {
                // New total waiting time
                // ----------------------------------------
                final double W0nPrime = tour.getWaitingTime(pi0, n) + Wr;
                final double W0fPrime = tour.getWaitingTime(pi0, n) + Wr
                        + max(0, tour.getWaitingTime(m, pil) - Deltan);
                // ----------------------------------------

                // New forward slack time
                // ----------------------------------------
                final double thirdTerm = tour.getFwdSlackTime(n, pil)//
                        + W0nPrime + tour.getTimeWindow(n).getEarliestStartOfService(An)//
                        - AnPrime;
                final double F0fPrime = Utilities.Math.minDouble(F0m, secondTerm, thirdTerm);
                // ----------------------------------------

                // Final cost of the insertion
                // ----------------------------------------
                final double deltaWT = max(0, Deltan - tour.getWaitingTime(m, pil))//
                        - min(F0fPrime, W0fPrime) //
                        + min(tour.getFwdSlackTime(pi0), tour.getWaitingTime(pi0, pil));
                // ----------------------------------------

                return deltaWT;
            } else {
                // New arrival time at the last node
                // ----------------------------------------
                // New departure from i
                final double DiPrime = tour.getEarliestDepartureTime(i)
                        + max(0, Deltan - tour.getWaitingTime(m, j));
                // Arrival at q
                final double Aq = DiPrime + tour.getTravelTime(i, q);
                // New arrival at j
                final double AjPrime = tour.getInstance().calculateArrivalTime(j, q, Aq,
                        tour.getTechnicianId());
                // Change in arrival time at j
                final double Aj = tour.getEarliestArrivalTime(j);
                final double Deltaj = AjPrime - Aj;
                // ----------------------------------------

                final double Wq = tour.getTimeWindow(q).getWaiting(Aq);
                // New waiting time between pi0 and j (inclusive)
                // ---------------------------------------
                final double W0q = tour.getWaitingTime(pi0, n) + Wr
                        + max(0, tour.getWaitingTime(m, j) - Deltan);
                final double W0jPrime = tour.getWaitingTime(pi0, n) + Wr + Wq // Waiting time at q already takes into
                                                                              // account possible absorption of the
                                                                              // Deltan
                        + max(0, tour.getWaitingTime(m, j) - Deltan);
                // New total waiting time
                // ----------------------------------------
                final double W0fPrime = tour.getWaitingTime(pi0, n) //
                        + Wr + max(0, tour.getWaitingTime(m, j) - Deltan)//
                        + Wq + max(0, tour.getWaitingTime(i, pil) - Deltaj);
                // ----------------------------------------

                // New forward slack time
                // ----------------------------------------
                final double thirdTerm = tour.getFwdSlackTime(n, i) + tour.getWaitingTime(pi0, n)
                        + Wr + max(An, tour.getTimeWindow(n).startAsDouble()) - AnPrime;
                final double fourthTerm = tour.getTimeWindow(q).endAsDouble() - Aq + W0q;
                final double fifthTerm = tour.getFwdSlackTime(j, pil) + W0jPrime
                        + max(Aj, tour.getTimeWindow(j).startAsDouble()) - AjPrime;
                final double F0fPrime = Utilities.Math.minDouble(F0m, secondTerm, thirdTerm,
                        fourthTerm, fifthTerm);
                // ----------------------------------------

                // Final cost of the insertion
                // ----------------------------------------
                final double deltaWT = max(0, Deltaj - tour.getWaitingTime(i, pil))//
                        - min(F0fPrime, W0fPrime) //
                        + min(tour.getFwdSlackTime(pi0), tour.getWaitingTime(pi0, pil));
                // ----------------------------------------

                return deltaWT;
            }
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
    public double evaluateInsMove(InsertionMove move) {
        if (!TRSPTour.class.isAssignableFrom(move.getTour().getClass()))
            throw new UnsupportedOperationException("Unsupported tour type: "
                    + move.getTour().getClass());
        TRSPTour tour = (TRSPTour) move.getTour();
        if (!tour.isAutoUpdated())
            throw new IllegalStateException(
                    "The tour needs to have its autoUpdated flag set to true");

        double improvement = 0;
        if (!move.isDepotTrip()) {
            // Simple insertion
            improvement = -evaluateDetour(tour, move.getInsertionPred(), move.getNodeId(),
                    move.getInsertionSucc(), false);
        } else {
            // Double insertion
            improvement = -evaluateDetour(tour, move.getDepotPred(), move.getTour().getInstance()
                    .getMainDepotDuplicate(tour.getTechnicianId()), move.getDepotSucc(),
                    move.getInsertionPred(), move.getNodeId(), move.getInsertionSucc(), false);
        }

        move.setImprovement(improvement);

        return improvement;
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
        throw new UnsupportedOperationException("Not updated yet");
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
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    protected double evaluateRemMove(RemoveMove move) {
        // TODO implement evaluateRemMove
        throw new UnsupportedOperationException("Not implemented yet");
        // TRSPTour t = ((TRSPTour)move.getTour());
        // double before = t.getMinimalDuration();
        // int pred = t.getPred(move.getNodeId());
        // t.removeNode(move.getNodeId());

    }

    @Override
    public boolean isInsertionSeqDependent() {
        return true;
    }

    @Override
    public void nodeFrozen(TRSPTour tour, int node) {
        super.nodeFrozen(tour, node);
        evaluateTRSPTour(tour, node, true);
    }
}
