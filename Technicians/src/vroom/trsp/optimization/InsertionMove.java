package vroom.trsp.optimization;

import vroom.common.heuristics.NeighborhoodBase;
import vroom.common.utilities.optimization.IMove;
import vroom.trsp.datamodel.ITRSPTour;
import vroom.trsp.datamodel.TRSPDetailedSolutionChecker;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.datamodel.TRSPTour;
import vroom.trsp.datamodel.TRSPTour.TRSPTourIterator;
import vroom.trsp.datamodel.costDelegates.TRSPCostDelegate;
import vroom.trsp.optimization.constraints.TourConstraintHandler;
import vroom.trsp.optimization.constraints.TourConstraintHandler.FeasibilityState;
import vroom.trsp.util.TRSPLogging;

/**
 * The class <code>InsertionMove</code> is a representation of the insertion of a node in a {@linkplain TRSPTour tour}.
 * <p>
 * When evaluating an {@link InsertionMove} the cost of an eventual trip to the depot, as defined by
 * {@link InsertionMove#isDepotTrip()} and {@link InsertionMove#getDepotSucc()} should be considered
 * </p>
 * <p>
 * Creation date: Mar 24, 2011 - 1:49:51 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class InsertionMove extends TRSPMove {

    private final int     mNodeId;

    private final int     mInsertionPred;
    private final int     mInsertionSucc;

    private final boolean mDepotTrip;
    private final int     mDepotSucc;

    private double        mSecondaryImprovement;

    /**
     * Returns <code>true</code> if this move is feasible, <code>false</code> otherwise
     * 
     * @return <code>true</code> if this move is feasible, <code>false</code> otherwise
     */
    public boolean isFeasible() {
        return getInsertionPred() != ITRSPTour.UNDEFINED
                || getInsertionSucc() != ITRSPTour.UNDEFINED;
    }

    /**
     * Creates a new infeasible <code>InsertionMove</code>
     * 
     * @param node
     *            the infeasible node
     */
    public InsertionMove(int node, TRSPTour tour) {
        this(node, tour, Double.POSITIVE_INFINITY, ITRSPTour.UNDEFINED, ITRSPTour.UNDEFINED);
    }

    /**
     * Creates a new <code>InsertionMove</code>
     * 
     * @param node
     *            the inserted node
     * @param tour
     *            the tour in which the node should be inserted
     * @param cost
     *            the cost of the insertion
     * @param insertionPred
     *            the node after which the <code>node</code> should be inserted
     * @param insertionSucc
     *            the node before which the <code>node</code> should be inserted
     * @param move
     *            the inserted move
     */
    public InsertionMove(int node, TRSPTour tour, double cost, int insertionPred, int insertionSucc) {
        this(node, tour, cost, insertionPred, insertionSucc, ITRSPTour.UNDEFINED, false);
    }

    /**
     * Creates a new <code>InsertionMove</code>
     * 
     * @param node
     *            the inserted node
     * @param tour
     *            the tour in which the node should be inserted
     * @param cost
     *            the cost of the insertion
     * @param insertionPred
     *            the node after which the <code>node</code> should be inserted
     * @param insertionSucc
     *            the node before which the <code>move</code> should be inserted
     * @param depotSucc
     *            the node before which a trip to the central depot will be planned
     * @param move
     *            the inserted move
     */
    public InsertionMove(int node, TRSPTour tour, double cost, int insertionPred,
            int insertionSucc, int depotSucc) {
        this(node, tour, cost, insertionPred, insertionSucc, depotSucc, true);
    }

    /**
     * Creates a new <code>InsertionMove</code>
     * 
     * @param node
     *            the inserted node
     * @param tour
     *            the tour in which the node should be inserted
     * @param cost
     *            the cost of the insertion
     * @param insertionPred
     *            the node after which the <code>node</code> should be inserted
     * @param insertionSucc
     *            the node before which the <code>move</code> should be inserted
     * @param depotSucc
     *            the node before which a trip to the central depot will be planned
     * @param move
     *            the inserted move
     * @param depotTrip
     *            {@code true} if a trip to the central depot should be planned
     */
    private InsertionMove(int node, TRSPTour tour, double cost, int insertionPred,
            int insertionSucc, int depotSucc, boolean depotTrip) {
        super(-cost, tour);
        mNodeId = node;

        mInsertionPred = insertionPred;
        mInsertionSucc = insertionSucc;

        mDepotTrip = depotTrip;
        mDepotSucc = depotSucc;
    }

    @Override
    public String toString() {
        return String.format("ins(%3s@%3s:(%3s,%3s) (%3s),%.3f)", //
                getNodeId(), getTour().getTechnicianId(),//
                getInsertionPred(), getInsertionSucc(), isDepotTrip() ? getDepotSucc() : "-", //
                getImprovement()// , getSecondaryImprovement()
                );
    }

    @Override
    public String getMoveName() {
        return "ins";
    }

    /**
     * Getter for the insertion <code>evaluation</code> (cost)
     * 
     * @return the evaluation
     */
    public double getCost() {
        return -getImprovement();
    }

    /**
     * Getter for the improvement relative to a possible second objective
     * 
     * @return the secondary improvement
     */
    public double getSecondaryImprovement() {
        return mSecondaryImprovement;
    }

    /**
     * Setter for the improvement relative to a possible second objective
     * 
     * @param secondaryImprovement
     *            the secondary improvement to set
     */
    public void setSecondaryImprovement(double secondaryImprovement) {
        mSecondaryImprovement = secondaryImprovement;
    }

    /**
     * Returns the node to be inserted
     * 
     * @return the node to be inserted
     */
    public int getNodeId() {
        return mNodeId;
    }

    /**
     * Returns the move after which the move is to be inserted, or {@link ITRSPTour#UNDEFINED} if the move should be
     * inserted at the beginning of the tour
     * 
     * @return the move after which the move is to be inserted, or {@link ITRSPTour#UNDEFINED} if the move should be
     *         inserted at the beginning of the tour
     */
    public int getInsertionPred() {
        return mInsertionPred;
    }

    /**
     * Returns the node before which the move is to be inserted, or {@link ITRSPTour#UNDEFINED} if the node should be
     * appended
     * 
     * @return the node before which the move is to be inserted, or {@link ITRSPTour#UNDEFINED} if the node should be
     *         appended
     */
    public int getInsertionSucc() {
        return mInsertionSucc;
    }

    /**
     * Returns <code>true</code> if a trip to the depot has to be added
     * 
     * @return <code>true</code> if a trip to the depot has to be added
     */
    public boolean isDepotTrip() {
        return mDepotTrip;
    }

    /**
     * Returns the node before which the trip to the depot has to be added
     * 
     * @return the node before which the trip to the depot has to be added
     */
    public int getDepotSucc() {
        return mDepotSucc;
    }

    /**
     * Returns the node after which the trip to the depot has to be inserted
     * 
     * @return the node after which the trip to the depot has to be inserted
     */
    public int getDepotPred() {
        return getDepotSucc() == getNodeId() ? getInsertionPred() : ((TRSPTour) getTour())
                .getPred(getDepotSucc());
    }

    /**
     * Find a feasible insertion of a node in a solution.
     * 
     * @param node
     *            the node which insertion is to be evaluated
     * @param costDelegate
     *            the cost delegate to evaluate the cost of insertion
     * @param constraintHandler
     *            the constraint handler
     * @param pruneSearch
     *            <code>true</code> if the search should be pruned when possible
     * @param bestInsertion
     *            {@code true} if the method should return the best insertion
     * @param tour
     *            the tour in which the node should be inserted
     * @return the insertion move
     */
    public static InsertionMove findInsertion(int node, TRSPSolution solution,
            TRSPCostDelegate costDelegate, TourConstraintHandler constraintHandler,
            boolean pruneSearch, boolean bestInsertion) {
        InsertionMove best = null;
        for (TRSPTour tour : solution) {
            if (tour.isVisited(node))
                throw new IllegalStateException(String.format("Tour %s already visits node %s",
                        tour.getTechnicianId(), node));
            InsertionMove mve = findInsertion(node, tour, costDelegate, constraintHandler,
                    pruneSearch, bestInsertion);
            if (!bestInsertion && mve != null)
                return mve;
            if (mve != null && best == null || best.compareTo(mve) < 0)
                best = mve;
        }
        return best;
    }

    /**
     * Find a feasible insertion of a node in a tour.
     * 
     * @param node
     *            the node which insertion is to be evaluated
     * @param tour
     *            the tour in which the node should be inserted
     * @param costDelegate
     *            the cost delegate to evaluate the cost of insertion
     * @param constraintHandler
     *            the constraint handler
     * @param pruneSearch
     *            <code>true</code> if the search should be pruned when possible
     * @param bestInsertion
     *            {@code true} for the best insertion, {@code false} for the first feasible solution
     * @return the insertion move
     */
    public static InsertionMove findInsertion(int node, TRSPTour tour,
            TRSPCostDelegate costDelegate, TourConstraintHandler constraintHandler,
            boolean pruneSearch, boolean bestInsertion) {
        if (tour.isVisited(node))
            throw new IllegalStateException("The tour " + tour + " already visits the node " + node);

        if (!tour.getInstance().isCompatible(tour.getTechnicianId(), node))
            // The technician does not have the skills to serve this move
            return new InsertionMove(node, tour);

        if (tour.length() == 0)
            return new InsertionMove(node, tour, 0, ITRSPTour.UNDEFINED, ITRSPTour.UNDEFINED);
        if (tour.length() == 1)
            return new InsertionMove(node, tour, 0, tour.getFirstNode(), ITRSPTour.UNDEFINED);

        InsertionMove best = null;
        // ---------------------------------------------------------------
        // Look for a feasible insertion point with minimal cost
        // ---------------------------------------------------------------
        if (tour.isMainDepotVisited()
                || tour.getInstance().hasRequiredTools(tour.getTechnician().getID(), node)
                || tour.getInstance().hasRequiredSpareParts(tour.getTechnician().getID(), node)) {
            TRSPTourIterator it = tour.iterator();
            // Skip the first node (depot)
            int pred = it.next();
            while (it.hasNext()) {
                int succ = it.next();
                InsertionMove move = new InsertionMove(node, tour, 0, pred, succ);
                // We assume that the cost evaluation is faster than the feasibility check
                double cost = -costDelegate.evaluateMove(move);
                if ((best == null || best.getCost() > cost)) {
                    FeasibilityState feasibility = constraintHandler.checkFeasibility(tour, move,
                            null);
                    if (feasibility.isFeasible()) {
                        if (!bestInsertion)
                            return move;
                        best = move;
                    } else if (pruneSearch
                            && !constraintHandler.checkFeasibility(tour, move, feasibility)
                                    .isForwardFeasible()) {
                        break;
                    }
                }
                pred = succ;
            }
        }
        if (best != null)
            return best;
        if (tour.isMainDepotVisited() || node == tour.getMainDepotId())
            // The main depot is already visited, no feasible insertion could be found, or the inserted node was the
            // main depot
            return new InsertionMove(node, tour);

        // ---------------------------------------------------------------
        // Look for an insertion point with minimal cost allowing visit to depot
        // ---------------------------------------------------------------
        if (tour.getInstance().isMainDepotTripAllowed()) {
            TRSPTourIterator it = tour.iterator();
            // Skip the first node (depot)
            int pred = it.next();
            boolean pruned = false;
            while (it.hasNext() && !pruned) {
                int succ = it.next();
                TRSPTourIterator depotIt = tour.iterator();
                // Skip the first node (depot)
                depotIt.next();
                int depotSucc = ITRSPTour.UNDEFINED;
                boolean abort = false;
                while (depotIt.hasNext() && !abort && !pruned) {
                    depotSucc = depotIt.next();
                    if (depotSucc == succ) {
                        depotSucc = node;
                        // Bugfix: Artificially abort the loop if the depot successor is the inserted node
                        abort = true;
                    }
                    InsertionMove move = new InsertionMove(node, tour, 0, pred, succ, depotSucc);
                    // We assume that the cost evaluation is faster than the feasibility check
                    double cost = -costDelegate.evaluateMove(move);
                    if ((best == null || best.getCost() > cost)) {
                        FeasibilityState feasibility = constraintHandler.checkFeasibility(tour,
                                move, null);
                        if (feasibility.isFeasible()) {
                            if (!bestInsertion)
                                return move;
                            best = move;
                        } else if (pruneSearch
                                && !constraintHandler.checkFeasibility(tour, move, feasibility)
                                        .isForwardFeasible()) {
                            pruned = true;
                            break;
                        }
                    }
                }
                pred = succ;
            }
        }

        if (best != null)
            return best;

        return new InsertionMove(node, tour);
    }

    /**
     * Execute an insertion move
     * 
     * @param move
     *            the insertion to be executed
     * @return <code>true</code> if the insertion was executed successfully, <code>false</code> otherwise
     */
    public static boolean executeMove(InsertionMove move) {
        TRSPTour tour = (TRSPTour) move.getTour();
        if (!move.isFeasible())
            // This assignment is not feasible
            return false;

        if (!move.isDepotTrip()) {
            if (tour.insertBefore(move.getInsertionSucc(), move.getNodeId())) {
                tour.getSolution().markAsServed(move.getNodeId());
                checkAfterMove(move);
                return true;
            }
        } else {
            if (tour.insertBefore(move.getInsertionSucc(), move.getNodeId())) {
                tour.insertBefore(move.getDepotSucc(), tour.getMainDepotId());
                tour.getSolution().markAsServed(move.getNodeId());
                checkAfterMove(move);
                return true;
            }
        }
        return false;
    }

    private static void checkAfterMove(InsertionMove move) {
        if (move.getTour().getSolution().getVisitingTour(move.getNodeId()) == null)
            throw new IllegalStateException("The request has not been inserted properly");
        if (NeighborhoodBase.isCheckSolutionAfterMove()) {
            String err = TRSPDetailedSolutionChecker.INSTANCE.checkSolution(move.getTour()
                    .getSolution());
            if (!err.isEmpty())
                TRSPLogging.getOptimizationLogger().warn(
                        "InsertionMove.executeMove: Incoherencies after move %s : %s", move, err);
        }
    }

    @Override
    public int compareTo(IMove o) {
        if (o instanceof InsertionMove) {
            int comp = Double.compare(getImprovement(), o.getImprovement());
            // Resolve ties using secondary improvement
            return comp != 0 ? comp : Double.compare(getSecondaryImprovement(),
                    ((InsertionMove) o).getSecondaryImprovement());
        } else
            return super.compareTo(o);
    }

}