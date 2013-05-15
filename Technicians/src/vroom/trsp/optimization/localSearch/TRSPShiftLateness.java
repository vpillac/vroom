/**
 *
 */
package vroom.trsp.optimization.localSearch;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import vroom.common.heuristics.Move;
import vroom.common.heuristics.NeighborhoodBase;
import vroom.common.utilities.optimization.IInstance;
import vroom.common.utilities.optimization.IMove;
import vroom.common.utilities.optimization.IParameters;
import vroom.trsp.datamodel.ITRSPTour;
import vroom.trsp.datamodel.TRSPDetailedSolutionChecker;
import vroom.trsp.datamodel.TRSPTour;
import vroom.trsp.datamodel.TRSPTour.TRSPTourIterator;
import vroom.trsp.optimization.localSearch.TRSPShiftLateness.TRSPShiftLatenessMove;
import vroom.trsp.util.TRSPLogging;

/**
 * <code>TRSPShift</code> is an implementation of the <code>shift</code> neighborhood for the TRSP problem and data
 * model.
 * <p>
 * Creation date: Mar 17, 2011 - 1:30:00 PM
 * </p>
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TRSPShiftLateness extends NeighborhoodBase<TRSPTour, TRSPShiftLatenessMove> {

    /**
     * <code>TRSPShiftLatenessMove</code> is a representation a <code>shift</code> move for the TRSP.
     * <p>
     * Creation date: Mar 17, 2011 - 1:45:02 PM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    public static class TRSPShiftLatenessMove extends Move {

        /** The id of the shifted node */
        private final int     mNode;
        /** The id of the node new successor */
        private final int     mNewSucc;
        /** <code>true</code> if the shift is forward, <code>false</code> if it is backward */
        private final boolean mForward;
        /** An optional field describing the change in updated lateness */
        private Lateness      mUpdatedLateness;

        /**
         * Creates a new <code>TRSPShiftLatenessMove</code>.
         * 
         * @param improvement
         *            the improvement resulting from this move
         * @param node
         *            the first node
         * @param newSucc
         *            the second node
         */
        protected TRSPShiftLatenessMove(double improvement, int node, int newSucc, boolean forward) {
            super(improvement);
            mNode = node;
            mNewSucc = newSucc;
            mForward = forward;
        }

        /**
         * Getter for <code>updatedLateness</code>
         * 
         * @return the updatedLateness
         */
        public Lateness getUpdatedLateness() {
            return mUpdatedLateness;
        }

        /**
         * Setter for <code>updatedLateness</code>
         * 
         * @param updatedLateness
         *            the updatedLateness to set
         */
        public void setUpdatedLateness(Lateness updatedLateness) {
            mUpdatedLateness = updatedLateness;
        }

        /**
         * Getter for <code>node</code>
         * 
         * @return the node
         */
        public int getNode() {
            return mNode;
        }

        /**
         * Getter for <code>newSucc</code>
         * 
         * @return the newSucc
         */
        public int getNewSucc() {
            return mNewSucc;
        }

        /**
         * Getter for <code>forward</code>
         * 
         * @return the forward
         */
        public boolean isForward() {
            return mForward;
        }

        @Override
        public String getMoveName() {
            return "shift";
        }

        /**
         * Build the new sequence of node that will result from this <code>1-shift</code> move. Nodes which visit order
         * in unchanged are ignored, except the immediate predecessor of the first changed node, which will appear
         * first.
         * 
         * @param tour
         *            the considered tour
         * @return a list containing the changed sequence resulting from the execution of this <code>1-shift</code> move
         *         on the given <code>tour</code>.
         */
        public List<Integer> changedSequence(TRSPTour tour) {
            LinkedList<Integer> changedSequence = new LinkedList<Integer>();
            if (isForward()) {
                changedSequence.add(tour.getPred(getNode()));
                TRSPTourIterator it = tour.iterator(tour.getSucc(getNode()));
                while (it.hasNext()) {
                    int next = it.next();
                    if (next == getNewSucc()) // Insert the node
                        changedSequence.add(getNode());
                    changedSequence.add(next);
                }
                // if newSucc==UNDEFINED it means the node is moved to the end of the tour
                if (getNewSucc() == ITRSPTour.UNDEFINED)
                    changedSequence.add(getNode());
            } else {
                // changedSequence.add(tour.getPred(node));
                changedSequence.add(tour.getPred(getNewSucc()));
                changedSequence.add(getNode());
                TRSPTourIterator it = tour.iterator(getNewSucc());
                while (it.hasNext()) {
                    int next = it.next();
                    if (next != getNode()) // Skip the node
                        changedSequence.add(next);
                }
            }
            return changedSequence;
        }

        @Override
        public String toString() {
            return String.format("%s(%s:%s-%s,%.3f)", getMoveName(), mForward ? "F" : "B", mNode,
                    mNewSucc, getImprovement());
        }
    }

    @Override
    public boolean executeMove(TRSPTour tour, IMove move) {
        TRSPShiftLatenessMove m = (TRSPShiftLatenessMove) move;
        TRSPLogging.getNeighborhoodLogger().lowDebug("TRSPShift.executeMove: executing move %s",
                move);

        int node = m.mNode, newSucc = m.mNewSucc;

        // Check if the shift is valid
        if (!tour.isVisited(node))
            throw new IllegalArgumentException("Node " + node + " is not present in this tour");
        if (newSucc != ITRSPTour.UNDEFINED && !tour.isVisited(newSucc))
            throw new IllegalArgumentException("New successor " + node
                    + " is not present in this tour");
        if (tour.getSucc(node) == newSucc)
            return false;

        // Node predecessor and successor
        int pred = tour.getPred(node);
        int succ = tour.getSucc(node);
        // New predecessor
        int newPred;
        if (newSucc != ITRSPTour.UNDEFINED)
            newPred = tour.getPred(newSucc);
        else
            // If newSucc is UNDEFINED then the node is appended to the end of the tour
            newPred = tour.getLastNode();

        // Extract node
        if (succ != ITRSPTour.UNDEFINED)
            tour.setPred(succ, pred);
        else
            tour.setLast(pred);

        if (pred != ITRSPTour.UNDEFINED)
            tour.setSucc(pred, succ);
        else
            tour.setFirst(succ);

        // Insert node
        if (newPred != ITRSPTour.UNDEFINED)
            tour.setSucc(newPred, node);
        else
            tour.setFirst(node);

        if (newSucc != ITRSPTour.UNDEFINED)
            tour.setPred(newSucc, node);
        else
            tour.setLast(node);

        tour.setPred(node, newPred);
        tour.setSucc(node, newSucc);

        // Propagate tools and spare parts
        tour.propagateUpdate(m.mForward ? succ : node, m.mForward ? node : pred);

        tour.getCostDelegate().nodeShifted(tour, node, pred, m.mForward);

        // tour.setTotalCost(tour.getTotalCost() + m.getImprovement());

        if (isCheckSolutionAfterMove()) {
            String check = TRSPDetailedSolutionChecker.INSTANCE.checkTour(tour);
            if (!check.isEmpty())
                TRSPLogging.getNeighborhoodLogger()
                        .warn("TRSPShift.executeMove: Incoherencies in tour after move %s (%s)", m,
                                check);

            double maxLateness = tour.getMaxLateness();
            if (m.mUpdatedLateness != null
                    && Math.abs(m.mUpdatedLateness.getMaxLateness() - maxLateness) > 1e-3) {
                TRSPLogging
                        .getNeighborhoodLogger()
                        .warn("TRSPShift.executeMove: The maximum lateness has not changed as expected after move %s (expected %.3f, was %.3f)",
                                m, m.mUpdatedLateness.getMaxLateness(), maxLateness);
            }

        }

        return true;
    }

    @Override
    public void pertub(IInstance instance, TRSPTour tour, IParameters params) {
        throw new UnsupportedOperationException("pertub is not implemented yet " + params);
    }

    @Override
    protected TRSPShiftLatenessMove randomNonImproving(TRSPTour tour, IParameters params) {
        throw new UnsupportedOperationException("randomNonImproving is not implemented yet "
                + params);
    }

    @Override
    protected TRSPShiftLatenessMove randomFirstImprovement(TRSPTour tour, IParameters params) {
        throw new UnsupportedOperationException("randomFirstImprovement is not implemented yet "
                + params);
    }

    @Override
    protected TRSPShiftLatenessMove deterministicBestImprovement(TRSPTour tour, IParameters params) {
        return deterministicExploration(tour, params, true);
    }

    @Override
    protected TRSPShiftLatenessMove deterministicFirstImprovement(TRSPTour tour, IParameters params) {
        return deterministicExploration(tour, params, true);
    }

    protected TRSPShiftLatenessMove deterministicExploration(TRSPTour tour, IParameters params,
            boolean first) {
        throw new UnsupportedOperationException("deterministicExploration is not implemented yet "
                + params);
    }

    @Override
    public boolean localSearch(TRSPTour tour, IParameters params) {
        /*
         * This local search procedure is inspired by the algorithm described in
         * Da Silva, R. F. & Urrutia, S.
         * A General VNS heuristic for the traveling salesman problem with time windows
         * Discrete Optimization, 2010, 7, 203 - 211
         */

        boolean improved = false;

        // The set of violated requests
        Set<Integer> violated = new HashSet<Integer>();
        // The maximum lateness and associated request

        boolean explored = false;
        TRSPShiftLatenessMove move = null;
        explored = false;
        while (!explored) {
            // Evaluate the current maximum lateness
            double maxLateness = 0;
            int maxLatenessReq = ITRSPTour.UNDEFINED;

            if (move == null) {
                for (int node : tour) {
                    if (!tour.getInstance().isDepot(node) && tour.getLateness(node) > 0) {
                        violated.add(node);
                        if (tour.getLateness(node) > maxLateness) {
                            maxLateness = tour.getLateness(node);
                            maxLatenessReq = node;
                        }
                    }
                }
            } else {
                if (move.getUpdatedLateness() != null) {
                    maxLateness = move.getUpdatedLateness().getMaxLateness();
                    maxLatenessReq = move.getUpdatedLateness().getMaxLatenessReq();
                }
            }

            // backward movements of violated requests
            // ---------------------------------------------------
            move = exploreBackwardViolated(tour, violated, maxLateness, maxLatenessReq, params);

            // forward movements of non-violated requests
            // ---------------------------------------------------
            if (move == null || !move.isImproving()) {
                move = exploreForwardNonViolated(tour, violated, maxLateness, maxLatenessReq,
                        params);
            }

            // backward movements of non-violated requests
            // ---------------------------------------------------
            if (move == null || !move.isImproving()) {

            }
            // forward movements of violated requests
            // ---------------------------------------------------
            if (move == null || !move.isImproving()) {

            }

            if (move != null && move.isImproving()) {
                executeMove(tour, move);

                // maxLateness = move.mUpdatedLateness.maxLateness;
                // maxLatenessReq = move.mUpdatedLateness.maxLatenessReq;
                explored = false;
            } else {
                explored = true;
            }
        }
        return improved;
    }

    /**
     * Explore the sub-neighborhood composed of backward shifts of violated requests.
     * 
     * @param tour
     *            the optimized tour
     * @param violated
     *            a set of violated request
     * @param maxLateness
     *            the maximum lateness in the tour
     * @param maxLatenessReq
     *            the request with the maximum lateness
     * @param params
     *            local search parameters
     * @return the best/first improving backward shifts of violated request (depending on the
     *         {@link IParameters#acceptFirstImprovement() params})
     */
    protected TRSPShiftLatenessMove exploreBackwardViolated(TRSPTour tour, Set<Integer> violated,
            final double maxLateness, final int maxLatenessReq, IParameters params) {
        if (tour.length() < 1)
            return null;

        // We start from the beginning of the tour
        // TODO start from the end of the tour?
        TRSPTourIterator mainIt = tour.iterator();
        int node = ITRSPTour.UNDEFINED;
        int candSucc = ITRSPTour.UNDEFINED;
        TRSPShiftLatenessMove bestMove = null;

        boolean skip = false;
        if (mainIt.hasNext()) {
            node = mainIt.next();
            // Skip depot(s) and the first request which cannot be shifted backward
            while (mainIt.hasNext() && tour.getInstance().isDepot(node))
                node = mainIt.next();
        } else
            // No backward shift can be found
            return null;

        while (mainIt.hasNext()) {
            // Main iterator
            node = mainIt.next();

            // Ignore non-violated requests and depots
            skip = !violated.contains(node) || tour.getInstance().isDepot(node);

            TRSPTourIterator insIt = null;
            if (!skip) {
                // Insertion iterator
                insIt = tour.iterator();
                // No candidate insertion point
                if (!insIt.hasNext())
                    skip = true;
                else {
                    candSucc = insIt.next();
                    // Skip depot(s)
                    while (insIt.hasNext() && tour.getInstance().isDepot(candSucc))
                        candSucc = insIt.next();
                }
            }

            // Temporary values for lateness used to evaluate the new maximum lateness after a candidate move
            double maxLatenessTmp = Long.MIN_VALUE;
            int maxLatenessReqTmp = ITRSPTour.UNDEFINED;

            // Consider all nodes that are before the node
            while (!skip && insIt.hasNext() && candSucc != node) {
                // Evaluate the maximum lateness if node is shifted before candSucc
                TRSPShiftLatenessMove move = new TRSPShiftLatenessMove(Double.NaN, node, candSucc,
                        false);
                tour.getCostDelegate().evaluateMove(move);// evaluateMaxLateness(tour, node, candSucc, false);
                // We found an improving move
                if (move.isImproving()) {
                    // The new max lateness will appear before
                    if (move.getUpdatedLateness().getMaxLateness() < maxLatenessTmp) {
                        move.setUpdatedLateness(new Lateness(maxLatenessTmp, maxLatenessReqTmp));
                    }
                    if (params.acceptFirstImprovement())
                        return move;
                    else if (bestMove == null || move.getImprovement() > bestMove.getImprovement())
                        bestMove = move;
                }

                // Update the maximum lateness found so far
                if (tour.getLateness(candSucc) > maxLatenessTmp) {
                    maxLatenessTmp = tour.getLateness(candSucc);
                    maxLatenessReqTmp = candSucc;
                }

                // Move to next node
                candSucc = insIt.next();
            }

            if (node == maxLatenessReq)
                // This was the node with the maximum lateness
                // Moving following nodes will not improve maximum lateness
                break;
        }

        return bestMove;
    }

    /**
     * Explore the sub-neighborhood composed of forward shifts of non-violated requests.
     * 
     * @param tour
     *            the optimized tour
     * @param violated
     *            a set of violated request
     * @param maxLateness
     *            the maximum lateness in the tour
     * @param maxLatenessReq
     *            the request with the maximum lateness
     * @param params
     *            local search parameters
     * @return the best/first improving forward shifts of non-violated request (depending on the
     *         {@link IParameters#acceptFirstImprovement() params})
     */
    protected TRSPShiftLatenessMove exploreForwardNonViolated(TRSPTour tour, Set<Integer> violated,
            double maxLateness, int maxLatenessReq, IParameters params) {
        if (tour.length() < 1)
            return null;

        // We start from the beginning of the tour
        // TODO start from the end of the tour?
        TRSPTourIterator mainIt = tour.iterator();
        int node = ITRSPTour.UNDEFINED;
        int candSucc = ITRSPTour.UNDEFINED;
        TRSPShiftLatenessMove bestMove = null;

        boolean skip = false;
        if (mainIt.hasNext()) {
            node = mainIt.next();
            // Skip depot(s) and the first request which cannot be shifted backward
            while (mainIt.hasNext() && tour.getInstance().isDepot(node))
                node = mainIt.next();
        } else
            // No shift can be found
            return null;

        while (mainIt.hasNext()) {
            // Main iterator
            node = mainIt.next();

            // Ignore violated requests, depots, and the last node
            skip = violated.contains(node) || tour.getInstance().isDepot(node)
                    || tour.getSucc(node) == ITRSPTour.UNDEFINED
                    || tour.getSucc(tour.getSucc(node)) == ITRSPTour.UNDEFINED;

            TRSPTourIterator insIt = null;
            if (!skip) {
                // Insertion iterator
                insIt = tour.iterator(tour.getSucc(tour.getSucc(node)));
                // No candidate insertion point
                if (!insIt.hasNext())
                    skip = true;
                else
                    candSucc = insIt.next();
            }
            // Temporary values for lateness used to evaluate the new maximum lateness after a candidate move
            double maxLatenessTmp = Long.MIN_VALUE;
            int maxLatenessReqTmp = ITRSPTour.UNDEFINED;

            // Consider all nodes that are after the node
            while (!skip) {
                // End the loop after this iteration
                if (candSucc == ITRSPTour.UNDEFINED)
                    skip = true;

                // Evaluate the maximum lateness if node is shifted before candSucc
                TRSPShiftLatenessMove move = new TRSPShiftLatenessMove(Double.NaN, node, candSucc,
                        true);
                tour.getCostDelegate().evaluateMove(move);
                // We found an improving move
                if (move.isImproving()) {
                    // The new max lateness will appear before
                    if (params.acceptFirstImprovement())
                        return move;
                    else if (bestMove == null || move.getImprovement() > bestMove.getImprovement())
                        bestMove = move;
                }

                // Move to next node
                if (insIt.hasNext())
                    candSucc = insIt.next();
                else
                    // Append to the end of the tour
                    candSucc = ITRSPTour.UNDEFINED;
            }

            // Update the maximum lateness found so far
            if (tour.getLateness(node) > maxLatenessTmp) {
                maxLatenessTmp = tour.getLateness(node);
                maxLatenessReqTmp = node;
            }
        }

        return bestMove;
    }

    @Override
    public String getShortName() {
        return "shiftLate";
    }

    /**
     * <code>Lateness</code> is a simple container used when evaluating the max lateness in a tour.
     * <p>
     * Creation date: Mar 22, 2011 - 11:15:52 AM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    public static class Lateness {
        private final double maxLateness;
        private final int    maxLatenessReq;

        /**
         * Creates a new <code>Lateness</code>
         * 
         * @param maxLateness
         * @param maxLatenessReq
         */
        public Lateness(double maxLateness, int maxLatenessReq) {
            super();
            this.maxLateness = maxLateness;
            this.maxLatenessReq = maxLatenessReq;
        }

        /**
         * Getter for <code>maxLateness</code>
         * 
         * @return the maxLateness
         */
        public double getMaxLateness() {
            return maxLateness;
        }

        /**
         * Getter for <code>maxLatenessReq</code>
         * 
         * @return the maxLatenessReq
         */
        public int getMaxLatenessReq() {
            return maxLatenessReq;
        }

    }
}
