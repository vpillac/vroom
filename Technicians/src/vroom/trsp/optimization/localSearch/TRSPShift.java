/**
 *
 */
package vroom.trsp.optimization.localSearch;

import java.util.LinkedList;
import java.util.List;

import vroom.common.heuristics.NeighborhoodBase;
import vroom.common.utilities.optimization.IInstance;
import vroom.common.utilities.optimization.IMove;
import vroom.common.utilities.optimization.IParameters;
import vroom.trsp.datamodel.ITRSPTour;
import vroom.trsp.datamodel.ITourIterator;
import vroom.trsp.datamodel.TRSPDetailedSolutionChecker;
import vroom.trsp.datamodel.TRSPTour;
import vroom.trsp.optimization.TRSPMove;
import vroom.trsp.optimization.constraints.TourConstraintHandler;
import vroom.trsp.optimization.localSearch.TRSPShift.TRSPShiftMove;
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
public class TRSPShift extends NeighborhoodBase<ITRSPTour, TRSPShiftMove> {

    /**
     * <code>TRSPShiftMove</code> is a representation a <code>shift</code> move for the TRSP.
     * <p>
     * Creation date: Mar 17, 2011 - 1:45:02 PM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    public static class TRSPShiftMove extends TRSPMove {

        /** The id of the shifted node */
        private final int     mNode;
        /** The id of the node new successor */
        private final int     mNewSucc;
        /** <code>true</code> if the shift is forward, <code>false</code> if it is backward */
        private final boolean mForward;

        private List<Integer> mChangedSequence;

        /**
         * Creates a new <code>TRSPShiftMove</code>.
         * 
         * @param tour
         *            the tour on which the new move will be defined
         * @param improvement
         *            the improvement resulting from this move
         * @param node
         *            the first node
         * @param newSucc
         *            the second node
         */
        protected TRSPShiftMove(TRSPTour tour, double improvement, int node, int newSucc,
                boolean forward) {
            super(improvement, tour);
            mNode = node;
            mNewSucc = newSucc;
            mForward = forward;
            mChangedSequence = null;
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
         * <p>
         * For performance reasons this sequence is stored after the first call. The tour should therefore not be
         * modified between two successive calls
         * </p>
         * 
         * @return a list containing the changed sequence resulting from the execution of this <code>1-shift</code> move
         *         on the given <code>tour</code>.
         */
        public synchronized List<Integer> getChangedSequence() {
            TRSPTour tour = (TRSPTour) getTour();

            if (mChangedSequence != null)
                return mChangedSequence;

            mChangedSequence = new LinkedList<Integer>();
            if (isForward()) {
                mChangedSequence.add(tour.getPred(getNode()));
                ITourIterator it = tour.iterator(tour.getSucc(getNode()));
                while (it.hasNext()) {
                    int next = it.next();
                    if (next == getNewSucc()) // Insert the node
                        mChangedSequence.add(getNode());
                    mChangedSequence.add(next);
                }
                // if newSucc==UNDEFINED it means the node is moved to the end of the mTour
                if (getNewSucc() == ITRSPTour.UNDEFINED)
                    mChangedSequence.add(getNode());
            } else {
                // changedSequence.add(mTour.getPred(node));
                mChangedSequence.add(tour.getPred(getNewSucc()));
                mChangedSequence.add(getNode());
                ITourIterator it = tour.iterator(getNewSucc());
                while (it.hasNext()) {
                    int next = it.next();
                    if (next != getNode()) // Skip the node
                        mChangedSequence.add(next);
                }
            }
            return mChangedSequence;
        }

        @Override
        public String toString() {
            return String.format("%s(%s:%s-%s,%.3f)", getMoveName(), mForward ? "F" : "B", mNode,
                    mNewSucc, getImprovement());
        }
    }

    /**
     * Creates a new <code>TRSPShift</code>
     */
    public TRSPShift() {
        super();
    }

    /**
     * Creates a new <code>TRSPShift</code>
     * 
     * @param constraintHandler
     */
    public TRSPShift(TourConstraintHandler constraintHandler) {
        super(constraintHandler);
    }

    @Override
    public boolean executeMove(ITRSPTour itour, IMove move) {
        TRSPShiftMove m = (TRSPShiftMove) move;
        TRSPTour tour = (TRSPTour) itour;

        TRSPLogging.getNeighborhoodLogger().lowDebug("TRSPShift.executeMove: executing move %s",
                move);
        if (tour != m.getTour())
            throw new IllegalStateException("Cannot execute this move on a different tour");

        int node = m.mNode, newSucc = m.mNewSucc;

        // Check if the shift is valid
        if (!tour.isVisited(node))
            throw new IllegalArgumentException("Node " + node + " is not present in this tour");
        if (newSucc != ITRSPTour.UNDEFINED && !tour.isVisited(newSucc))
            throw new IllegalArgumentException("New successor " + node
                    + " is not present in this tour");
        if (tour.getSucc(node) == newSucc)
            return false;

        double oldCost = tour.getTotalCost();
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

            if (Math.abs(oldCost - m.getImprovement() - tour.getTotalCost()) > 1e-3) {
                TRSPLogging
                        .getNeighborhoodLogger()
                        .warn("TRSPShift.executeMove: The tour cost has not changed as expected after move %s (expected %.3f, was %.3f)",
                                m, -m.getImprovement(), tour.getTotalCost() - oldCost);
            }

        }

        return true;
    }

    @Override
    public void pertub(IInstance instance, ITRSPTour tour, IParameters params) {
        throw new UnsupportedOperationException("pertub is not implemented yet " + params);
    }

    @Override
    protected TRSPShiftMove randomNonImproving(ITRSPTour tour, IParameters params) {
        throw new UnsupportedOperationException("randomNonImproving is not implemented yet "
                + params);
    }

    @Override
    protected TRSPShiftMove randomFirstImprovement(ITRSPTour tour, IParameters params) {
        throw new UnsupportedOperationException("randomFirstImprovement is not implemented yet "
                + params);
    }

    @Override
    protected TRSPShiftMove deterministicBestImprovement(ITRSPTour tour, IParameters params) {
        return deterministicExploration(tour, params, false);
    }

    @Override
    protected TRSPShiftMove deterministicFirstImprovement(ITRSPTour tour, IParameters params) {
        return deterministicExploration(tour, params, true);
    }

    protected TRSPShiftMove deterministicExploration(ITRSPTour tour, IParameters params,
            boolean first) {
        // backward movements
        // ---------------------------------------------------
        TRSPShiftMove move = exploreBackward(tour, params);

        // forward movements
        // ---------------------------------------------------
        if (move == null || !move.isImproving()) {
            move = exploreForward(tour, params);
        }

        return move;
    }

    /**
     * Explore the sub-neighborhood composed of backward shifts
     * 
     * @param tour
     *            the optimized tour
     * @param params
     *            local search parameters
     * @return the best/first improving backward shift (depending on the {@link IParameters#acceptFirstImprovement()
     *         params})
     */
    protected TRSPShiftMove exploreBackward(ITRSPTour itour, IParameters params) {
        if (itour.length() < 1)
            return null;

        TRSPTour tour = (TRSPTour) itour;

        // We start from the beginning of the tour
        ITourIterator mainIt = tour.iterator();
        int node = ITRSPTour.UNDEFINED;
        int candSucc = ITRSPTour.UNDEFINED;
        TRSPShiftMove bestMove = null;

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

            // Ignore depots
            skip = tour.getInstance().isDepot(node);

            ITourIterator insIt = null;
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

            // Consider all nodes that are before the node
            while (!skip && insIt.hasNext() && candSucc != node) {
                // Evaluate the maximum lateness if node is shifted before candSucc
                TRSPShiftMove move = new TRSPShiftMove(tour, Double.NaN, node, candSucc, false);
                tour.getCostDelegate().evaluateMove(move);// evaluateMaxLateness(tour, node, candSucc, false);
                // We found an improving move
                if (params.getAcceptanceCriterion().accept(tour, move)
                        && getConstraintHandler().isFeasible(tour, move)) {
                    if (params.acceptFirstImprovement())
                        return move;
                    else if (bestMove == null || move.getImprovement() > bestMove.getImprovement())
                        bestMove = move;
                }

                // Move to next node
                candSucc = insIt.next();
            }

        }

        return bestMove;
    }

    /**
     * Explore the sub-neighborhood composed of forward shifts of non-violated requests.
     * 
     * @param tour
     *            the optimized tour
     * @param params
     *            local search parameters
     * @return the best/first improving forward shifts of non-violated request (depending on the
     *         {@link IParameters#acceptFirstImprovement() params})
     */
    protected TRSPShiftMove exploreForward(ITRSPTour itour, IParameters params) {
        if (itour.length() < 1)
            return null;

        TRSPTour tour = (TRSPTour) itour;

        // We start from the beginning of the tour
        ITourIterator mainIt = tour.iterator();
        int node = ITRSPTour.UNDEFINED;
        int candSucc = ITRSPTour.UNDEFINED;
        TRSPShiftMove bestMove = null;

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
            skip = tour.getInstance().isDepot(node) || tour.getSucc(node) == ITRSPTour.UNDEFINED
                    || tour.getSucc(tour.getSucc(node)) == ITRSPTour.UNDEFINED;

            ITourIterator insIt = null;
            if (!skip) {
                // Insertion iterator
                insIt = tour.iterator(tour.getSucc(tour.getSucc(node)));
                // No candidate insertion point
                if (!insIt.hasNext())
                    skip = true;
                else
                    candSucc = insIt.next();
            }

            // Consider all nodes that are after the node
            while (!skip) {
                // End the loop after this iteration
                if (candSucc == ITRSPTour.UNDEFINED)
                    skip = true;

                TRSPShiftMove move = new TRSPShiftMove(tour, Double.NaN, node, candSucc, true);
                tour.getCostDelegate().evaluateMove(move);
                // We found an improving move
                if (params.getAcceptanceCriterion().accept(tour, move)
                        && getConstraintHandler().isFeasible(tour, move)) {
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
        }

        return bestMove;
    }

    @Override
    public String getShortName() {
        return "shift";
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
