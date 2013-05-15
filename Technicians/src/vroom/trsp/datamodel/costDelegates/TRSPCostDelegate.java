/**
 * 
 */
package vroom.trsp.datamodel.costDelegates;

import java.util.List;
import java.util.ListIterator;

import vroom.common.utilities.optimization.IMove;
import vroom.trsp.datamodel.ITRSPTour;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.datamodel.TRSPTour;
import vroom.trsp.optimization.InsertionMove;
import vroom.trsp.optimization.RemoveMove;
import vroom.trsp.optimization.biobj.LevenshteinPR.AtomicPRMove;
import vroom.trsp.optimization.biobj.LevenshteinPR.PRMove;
import vroom.trsp.optimization.localSearch.TRSPShift.TRSPShiftMove;
import vroom.trsp.optimization.localSearch.TRSPTwoOpt.TRSPTwoOptMove;
import vroom.trsp.optimization.mpa.DTRSPSolution;

/**
 * <code>TRSPCostDelegate</code> is an interface for classes that will be responsible for the calculation and upate of
 * {@link TRSPTour} costs
 * <p>
 * Creation date: Feb 23, 2011 - 4:07:12 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public abstract class TRSPCostDelegate {

    /**
     * the penalize unserved request flag: <code>true</code> if a cost penalty should be applied when requests are
     * unserved
     **/
    private boolean mUnservedPenalized;

    /**
     * Getter for the penalize unserved request flag
     * 
     * @return <code>true</code> if a cost penalty should be applied when requests are unserved
     */
    public boolean isUnservedPenalized() {
        return this.mUnservedPenalized;
    }

    /**
     * Setter for the penalize unserved request flag.
     * 
     * @param penalize
     *            <code>true</code> if a cost penalty should be applied when requests are unserved
     */
    public void setPenalize(boolean penalize) {
        this.mUnservedPenalized = penalize;
    }

    /** the penalty added for each unserved request **/
    private double mUnservedPenalty;

    /**
     * Getter for the penalty added for each unserved request
     * 
     * @return the value of unservedPenalty
     */
    public double getUnservedPenalty() {
        return this.mUnservedPenalty;
    }

    /**
     * Sets the penalty added for each unserved request to {@code  0}
     */
    public synchronized void unsetUnservedPenalty() {
        this.mUnservedPenalty = 0;
    }

    /**
     * Sets the penalty added for each unserved request
     * 
     * @param solution
     *            the reference solution which cost will be used to evaluate the penalty
     * @param gamma
     *            the scaling factor for the unserved penalty
     */
    public synchronized void setUnservedPenalty(TRSPSolution solution, double gamma) {
        unsetUnservedPenalty();
        this.mUnservedPenalty = evaluateSolution(solution, true, false) * gamma;
    }

    /**
     * Creates a new <code>TRSPCostDelegate</code>
     */
    public TRSPCostDelegate() {
        mUnservedPenalized = false;
        mUnservedPenalty = 0;
    }

    /**
     * Evaluates the penalty associated with a solution
     * 
     * @param solution
     *            the solution to be evaluated
     * @return the penalty associated with <code>solution</code>, or 0 if the penalty is disabled
     */
    protected double evaluatePenalty(TRSPSolution solution) {
        return isUnservedPenalized() ? solution.getUnservedCount() * getUnservedPenalty() : 0;
    }

    /**
     * Evaluate the cost of a solution
     * 
     * @param solution
     *            the solution to be evaluated
     * @param evaluateTours
     *            <code>true</code> if the tour cost should be {@linkplain #evaluateTour(TRSPTour, boolean)
     *            re-evaluated}, or <code>false</code> if the store {@linkplain TRSPTour#getTotalCost() cost} should be
     *            used
     * @param updateTours
     *            see {@link #evaluateTour(TRSPTour, boolean)}, only applies if <code>evaluateTours=true</code>
     * @return the cost of <code>solution</code>
     */
    public double evaluateSolution(TRSPSolution solution, boolean evaluateTours, boolean updateTours) {
        double cost = 0;
        for (TRSPTour t : solution)
            cost += evaluateTours ? evaluateTour(t, updateTours) : t.getTotalCost();

        cost += evaluatePenalty(solution);

        return cost;
    }

    /**
     * Evaluate the cost of a tour, ignoring any previously stored values.
     * 
     * @param tour
     *            the tour that will be reevaluated
     * @param updateTour
     *            <code>true</code> if the tour stored costs have to be updated
     * @return the total cost of the tour
     */
    public double evaluateTour(ITRSPTour tour, boolean updateTour) {
        double cost = 0;
        // Ignore empty tours
        if (tour.length() != 0) {
            if (tour instanceof TRSPTour)
                cost = evaluateTRSPTour((TRSPTour) tour, ITRSPTour.UNDEFINED, updateTour);
            else
                cost = evaluateGenericTour(tour);
        }

        if (updateTour)
            tour.setTotalCost(cost);

        return cost;
    }

    /**
     * Evaluate a tour
     * 
     * @param tour
     *            the tour to be evaluated
     * @return the total cost of the tour
     */
    protected abstract double evaluateGenericTour(ITRSPTour tour);

    /**
     * Evaluate a portion of the {@link TRSPTour} starting at the given <code>node</code>
     * <p>
     * Implementations must ensure that if <code>node</code> is equal to {@link ITRSPTour#UNDEFINED} then the whole tour
     * will be reevaluated
     * </p>
     * 
     * @param tour
     *            the tour to be evaluated
     * @param node
     *            the node at which reevaluation should start
     * @param updateTour
     *            <code>true</code> if the tour stored costs have to be updated
     * @return the total cost of the tour
     */
    protected abstract double evaluateTRSPTour(TRSPTour tour, int node, boolean updateTour);

    /**
     * Updates the <code>tour</code> cost after a node was inserted.
     * 
     * @param tour
     *            the modified tour
     * @param predecessor
     *            the predecessor, <code>null</code> if the node was inserted at the beginning
     * @param node
     *            the inserted node
     * @param successor
     *            the successor, <code>null</code> if the node was appended
     */
    public void nodeInserted(TRSPTour tour, int predecessor, int node, int successor) {
        if (tour.isAutoUpdated())
            evaluateTRSPTour(tour, predecessor, true);
    }

    /**
     * Updates the {@code  tour} after a node was {@link DTRSPSolution#freeze() frozen}
     * 
     * @param tour
     *            the affected tour
     * @param node
     */
    public void nodeFrozen(TRSPTour tour, int node) {
    }

    /**
     * Updates the <code>tour</code> cost after a tour was inserted.
     * 
     * @param tour
     *            the modified tour
     * @param predecessor
     *            the predecessor, <code>null</code> if the modified tour was inserted at the beginning
     * @param insertedTour
     *            the inserted tour
     * @param successor
     *            the successor, <code>null</code> if the modified tour was appended
     */
    public void tourInserted(TRSPTour tour, int predecessor, List<Integer> insertedTour,
            int successor) {
        if (tour.isAutoUpdated())
            evaluateTRSPTour(tour, predecessor, true);
    }

    /**
     * Updates the <code>tour</code> cost after a node was removed.
     * 
     * @param tour
     *            the modified tour
     * @param predecessor
     *            the predecessor, <code>null</code> if the first node was removed
     * @param node
     *            the removed node
     * @param successor
     *            the successor, <code>null</code> if the last node was removed
     */
    public void nodeRemoved(TRSPTour tour, int predecessor, int node, int successor) {
        if (tour.isAutoUpdated())
            evaluateTRSPTour(tour, predecessor, true);
    }

    /**
     * Updates the <code>tour</code> cost after a node was replaced.
     * 
     * @param tour
     *            the modified tour
     * @param predecessor
     *            the predecessor, <code>null</code> if the first node was replaced
     * @param previousNode
     *            the node that was replaced
     * @param node
     *            the new node
     * @param successor
     *            the successor, <code>null</code> if the last node was replaced
     */
    public void nodeReplaced(TRSPTour tour, int predecessor, int previousNode, int node,
            int successor) {
        if (tour.isAutoUpdated())
            evaluateTRSPTour(tour, predecessor, true);
    }

    /**
     * Updates the <code>tour</code> cost after two nodes were swapped.
     * <p>
     * Note that <code>node1</code> have to appear before <code>node2</code> in the tour
     * </p>
     * 
     * @param tour
     *            the modified tour
     * @param pred1
     *            the predecessor of the first node
     * @param node1
     *            the first node
     * @param succ1
     *            the successor of the first node
     * @param pred2
     *            the predecessor of the second node
     * @param node2
     *            the second node
     * @param succ2
     *            the successor of the second node
     */
    public void nodesSwapped(TRSPTour tour, int pred1, int node1, int succ1, int pred2, int node2,
            int succ2) {
        if (tour.isAutoUpdated())
            evaluateTRSPTour(tour, pred1, true);
    }

    /**
     * Updates the <code>tour</code> cost after a node shift
     * 
     * @param tour
     *            the modified tour
     * @param node
     *            the shifted node
     * @param pred
     *            the former predecessor of the shifted <code>node</code>
     * @param forward
     *            <code>true</code> if the shift was forward, <code>false</code> otherwise
     */
    public void nodeShifted(TRSPTour tour, int node, int pred, boolean forward) {
        if (tour.isAutoUpdated())
            evaluateTRSPTour(tour, forward ? pred : tour.getPred(node), true);
    }

    /**
     * Updates the <code>tour</code> cost after a subtour was removed.
     * 
     * @param tour
     *            the modified tour
     * @param predecessor
     *            the predecessor, <code>null</code> if the first node was removed
     * @param removedTour
     *            the removed tour
     * @param successor
     *            the successor, <code>null</code> if the last node was removed
     */
    public void subtourRemoved(TRSPTour tour, int predecessor, List<Integer> removedTour,
            int successor) {
        if (tour.isAutoUpdated())
            evaluateTRSPTour(tour, predecessor, true);
    }

    /**
     * Updates the <code>tour</code> cost after a subtour was reversed.
     * 
     * @param tour
     *            the modified tour
     * @param predecessor
     *            the predecessor, <code>null</code> if the first node was included
     * @param first
     *            the first node of the reversed subtour
     * @param last
     *            the last node of the reversed subtour
     * @param successor
     *            the successor, <code>null</code> if the last node was included
     */
    public void subtourReversed(TRSPTour tour, int predecessor, int first, int last, int successor) {
        if (tour.isAutoUpdated())
            evaluateTRSPTour(tour, predecessor, true);
    }

    /**
     * Gets the detour cost of a node.
     * 
     * @param tour
     *            the considered tour
     * @param i
     *            the predecessor, {@link ITRSPTour#UNDEFINED} if head insertion
     * @param n
     *            the inserted node
     * @param j
     *            the successor, {@link ITRSPTour#UNDEFINED} if tail insertion
     * @param isRemoval
     *            <code>true</code> if the evaluation corresponds to the removal of <code>node</code>,
     *            <code>false</code> otherwise.
     * @return the detour cost (which sign is expected to be positive and is independent of the <code>isRemoval</code>
     *         argument)
     */
    public abstract double evaluateDetour(ITRSPTour tour, int i, int n, int j, boolean isRemoval);

    /**
     * Evaluates the {@link IMove#getImprovement()} of a move
     * 
     * @param move
     *            the move to be evaluated
     * @return the {@linkplain IMove#getImprovement() improvement} resulting from the execution of the specified
     *         <code>move</code> to the given <code>tour</code>
     * @throws UnsupportedOperationException
     *             if the <code>move</code> is not supported
     */
    public double evaluateMove(IMove move) throws UnsupportedOperationException {
        double imp;
        if (move instanceof InsertionMove)
            imp = evaluateInsMove((InsertionMove) move);
        else if (move instanceof PRMove)
            imp = evaluatePRMove((PRMove) move);
        else if (move instanceof TRSPShiftMove)
            imp = evaluateShiftMove((TRSPShiftMove) move);
        else if (move instanceof TRSPTwoOptMove)
            imp = evaluateTwoOptMove((TRSPTwoOptMove) move);
        else if (move instanceof RemoveMove)
            imp = evaluateRemMove((RemoveMove) move);
        else
            throw new UnsupportedOperationException(String.format(
                    "Move %s is not supported by this cost delegate (%s)", move, this.getClass()
                            .getSimpleName()));
        move.setImprovement(imp);
        return imp;
    }

    /**
     * Evaluate a {@link RemoveMove}.
     * <p>
     * Subclasses should override this method, which is called in {@link #evaluateMove(IMove)}.
     * </p>
     * 
     * @param move
     *            the move to be evaluated
     * @return the evaluation of the move
     */
    protected double evaluateRemMove(RemoveMove move) {
        throw new UnsupportedOperationException(String.format(
                "Move %s is not supported by this cost delegate (%s)", move, this.getClass()
                        .getSimpleName()));
    }

    /**
     * Evaluate a {@link TRSPTwoOptMove}.
     * <p>
     * Subclasses should override this method, which is called in {@link #evaluateMove(IMove)}.
     * </p>
     * 
     * @param move
     *            the move to be evaluated
     * @return the evaluation of the move
     */
    protected double evaluateTwoOptMove(TRSPTwoOptMove move) {
        throw new UnsupportedOperationException(String.format(
                "Move %s is not supported by this cost delegate (%s)", move, this.getClass()
                        .getSimpleName()));
    }

    /**
     * Evaluate a {@link TRSPShiftMove}.
     * <p>
     * Subclasses should override this method, which is called in {@link #evaluateMove(IMove)}.
     * </p>
     * 
     * @param move
     *            the move to be evaluated
     * @return the evaluation of the move
     */
    protected double evaluateShiftMove(TRSPShiftMove move) {
        throw new UnsupportedOperationException(String.format(
                "Move %s is not supported by this cost delegate (%s)", move, this.getClass()
                        .getSimpleName()));
    }

    /**
     * Evaluate a {@link InsertionMove}.
     * <p>
     * Subclasses should override this method, which is called in {@link #evaluateMove(IMove)}.
     * </p>
     * 
     * @param move
     *            the move to be evaluated
     * @return the evaluation of the move
     */
    protected double evaluateInsMove(InsertionMove move) {
        throw new UnsupportedOperationException(String.format(
                "Move %s is not supported by this cost delegate (%s)", move, this.getClass()
                        .getSimpleName()));
    }

    /**
     * Evaluate a {@link PRMove}.
     * 
     * @param move
     *            the move to be evaluated
     * @return the evaluation of the move
     */
    protected double evaluatePRMove(PRMove move) {
        double imp = 0;

        for (AtomicPRMove mve : move.getAtomicMoves()) {
            switch (mve.getType()) {
            case INS:
                if (mve.getInsertedNodes().size() == 1) {
                    InsertionMove ins = new InsertionMove(mve.getNewReq(), mve.getTour(),
                            Double.NaN, mve.getEditedReq(), mve.getTour().getSucc(
                                    mve.getEditedReq()));
                    imp += evaluateInsMove(ins);
                } else {
                    ListIterator<Integer> it = mve.getInsertedNodes().listIterator(
                            mve.getInsertedNodes().size());
                    // Head insertion
                    int node = it.previous();
                    int pred = mve.getEditedReq();
                    int succ = it.previous();
                    imp += evaluateInsMove(new InsertionMove(node, mve.getTour(), Double.NaN, pred,
                            succ));
                    while (it.hasPrevious()) {
                        pred = node;
                        node = succ;
                        succ = it.previous();
                        imp += evaluateInsMove(new InsertionMove(node, mve.getTour(), Double.NaN,
                                pred, succ));
                    }
                    pred = node;
                    node = succ;
                    succ = mve.getTour().getSucc(mve.getEditedReq());
                    imp += evaluateInsMove(new InsertionMove(node, mve.getTour(), Double.NaN, pred,
                            succ));
                }
                break;
            case DEL:
                imp += evaluateRemMove(new RemoveMove(mve.getEditedReq(), mve.getTour()));
                break;
            case SUB:
                imp += evaluateRemMove(new RemoveMove(mve.getEditedReq(), mve.getTour()));
                imp += evaluateInsMove(new InsertionMove(mve.getNewReq(), mve.getTour(),
                        Double.NaN, mve.getTour().getPred(mve.getEditedReq()), mve.getTour()
                                .getSucc(mve.getEditedReq())));
                break;
            default:
                throw new UnsupportedOperationException("Unsupported edit type :" + mve.getType());
            }
        }

        return imp;
    }

    /**
     * Returns <code>true</code> if the cost of an insertion depends on the sequence preceding the insertion.
     * 
     * @return <code>true</code> if the cost of an insertion depends on the sequence preceding the insertion.
     */
    public abstract boolean isInsertionSeqDependent();

    @Override
    public String toString() {
        return String.format("%s(p:%s|%.3f)", this.getClass().getSimpleName(),
                isUnservedPenalized(), getUnservedPenalty());
    }

}
