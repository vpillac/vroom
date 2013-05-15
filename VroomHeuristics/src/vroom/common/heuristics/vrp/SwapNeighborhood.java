/**
 * 
 */
package vroom.common.heuristics.vrp;

import java.util.LinkedList;

import vroom.common.heuristics.ConstraintHandler;
import vroom.common.heuristics.GenericNeighborhood;
import vroom.common.heuristics.vrp.constraints.FixedNodesConstraint;
import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.IRoute;
import vroom.common.modeling.dataModel.IVRPSolution;
import vroom.common.modeling.dataModel.Vehicle;
import vroom.common.modeling.util.CostCalculationDelegate;
import vroom.common.utilities.optimization.IConstraint;
import vroom.common.utilities.optimization.IMove;
import vroom.common.utilities.optimization.IParameters;

/**
 * <code>SwapNeighborhood</code>
 * <p>
 * Creation date: Jul 2, 2010 - 11:05:07 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class SwapNeighborhood<S extends IVRPSolution<?>> extends GenericNeighborhood<S, SwapMove> {

    /**
     * Creates a new <code>SwapNeighborhood</code>
     * 
     * @param constraintHandler
     */
    public SwapNeighborhood(ConstraintHandler<S> constraintHandler) {
        super(constraintHandler);
        boolean add = true;
        for (IConstraint<?> ctr : getConstraintHandler()) {
            if (ctr instanceof FixedNodesConstraint<?>) {
                add = false;
                break;
            }
        }

        if (add) {
            getConstraintHandler().addConstraint(new FixedNodesConstraint<S>());
        }
    }

    /**
     * Creates a new <code>SwapNeighborhood</code>
     */
    public SwapNeighborhood() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.heuristics.GenericNeighborhood#
     * deterministicFirstImprovementExploration
     * (vroom.common.utilities.optimization.ISolution,
     * vroom.common.utilities.optimization.IParameters)
     */
    @Override
    public SwapMove deterministicFirstImprovement(S solution, IParameters params) {
        SwapMove move;
        // Iterate over all routes
        for (int rI = 0; rI < solution.getRouteCount(); rI++) {
            for (int rJ = rI; rJ < solution.getRouteCount(); rJ++) {
                // Iterate over all nodes of the first route
                for (int i = 0; i < solution.getRoute(rI).length(); i++) {
                    // Iterate over the nodes of the second route
                    for (int j = rI == rJ ? i + 1 : 0; j < solution.getRoute(rJ).length(); j++) {
                        move = new SwapMove(Double.NEGATIVE_INFINITY, solution, rI, rJ, i, j);
                        evaluateCandidateMove(move);
                        if ((getAcceptanceCriterion(params).accept(solution, this, move) || params
                                .acceptNonImproving())
                                && getConstraintHandler().isFeasible(solution, move)) {
                            return move;
                        }
                    }
                }
            }
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * vroom.common.heuristics.GenericNeighborhood#generateCandidateList(vroom.
     * common.utilities.optimization.ISolution,
     * vroom.common.utilities.optimization.IParameters)
     */
    @Override
    protected LinkedList<SwapMove> generateCandidateList(S solution, IParameters params) {
        LinkedList<SwapMove> candidates = new LinkedList<SwapMove>();
        // Iterate over all routes
        for (int rI = 0; rI < solution.getRouteCount(); rI++) {
            for (int rJ = rI; rJ < solution.getRouteCount(); rJ++) {
                // Iterate over all nodes of the first route
                for (int i = 0; i < solution.getRoute(rI).length(); i++) {
                    // Iterate over the nodes of the second route
                    for (int j = rI == rJ ? i + 1 : 0; j < solution.getRoute(rJ).length(); j++) {
                        candidates.add(new SwapMove(Double.NEGATIVE_INFINITY, solution, rI, rJ, i,
                                j));
                    }
                }
            }
        }
        return candidates;
    }

    /**
     * Evaluate the cost-distance improvement for a given move
     * 
     * @param move
     * @return the cost-distance improvement for a given move
     */
    @Override
    protected double evaluateCandidateMove(SwapMove move) {
        /* The resulting improvement (= minus cost) */
        double improv = 0;

        CostCalculationDelegate costHelper = move.mSolution.getParentInstance().getCostDelegate();
        IRoute<?> rI = move.mSolution.getRoute(move.getRouteI());
        IRoute<?> rJ = move.mSolution.getRoute(move.getRouteJ());
        Vehicle vI = rI.getVehicle();
        Vehicle vJ = rJ.getVehicle();

        INodeVisit nI = rI.getNodeAt(move.getI());
        INodeVisit nJ = rJ.getNodeAt(move.getJ());

        /* <code>true</code> if successive nodes */
        boolean succ = Math.abs(move.getI() - move.getJ()) == 1
                && move.getRouteI() == move.getRouteJ();

        if (move.getI() > 0) {
            improv -= -costHelper.getCost( // Remove (nodeI-1,nodeI) in routeI
                    rI.getNodeAt(move.getI() - 1), nI, vI) + costHelper.getCost( // Add
                                                                                 // (nodeI-1,nodeJ)
                                                                                 // in
                                                                                 // routeI
                    rI.getNodeAt(move.getI() - 1), nJ, vI);
        }
        if (move.getJ() > 0 && !succ) {
            improv -= -costHelper.getCost( // Remove (nodeJ-1,nodeJ) in routeJ #
                    rJ.getNodeAt(move.getJ() - 1), nJ, vJ) + costHelper.getCost( // Add
                                                                                 // (nodeJ-1,nodeI)
                                                                                 // in
                                                                                 // routeJ
                                                                                 // #
                    rJ.getNodeAt(move.getJ() - 1), nI, vJ);
        }
        if (move.getI() < rI.length() - 1 && !succ) {
            improv -= -costHelper.getCost( // Remove (nodeI,nodeI+1) in routeI #
                    nI, rI.getNodeAt(move.getI() + 1), vI) + costHelper.getCost( // Add
                                                                                 // (nodeJ,nodeI+1)
                                                                                 // in
                                                                                 // routeI
                                                                                 // #
                    nJ, rI.getNodeAt(move.getI() + 1), vI);
        }
        if (move.getJ() < rJ.length() - 1) {
            improv -= -costHelper.getCost( // Remove (nodeJ,nodeJ+1) in routeJ
                    nJ, rJ.getNodeAt(move.getJ() + 1), vJ) + costHelper.getCost( // Add
                                                                                 // (nodeI,nodeJ+1)
                                                                                 // in
                                                                                 // routeJ
                    nI, rJ.getNodeAt(move.getJ() + 1), vJ);
        }

        move.setImprovement(improv);

        return improv;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * vroom.common.heuristics.INeighborhood#executeMove(vroom.common.utilities
     * .optimization.ISolution, vroom.common.heuristics.Move)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected boolean executeMoveImplem(S solution, IMove move) {
        if (!(move instanceof SwapMove)) {
            throw new IllegalArgumentException("The move must be of type SwapMove");
        }

        int i = ((SwapMove) move).getI();
        int j = ((SwapMove) move).getJ();
        IRoute rI = solution.getRoute(((SwapMove) move).getRouteI());
        IRoute rJ = solution.getRoute(((SwapMove) move).getRouteJ());

        // Auxiliary method to solve type safety problems
        return executeMove(solution, i, j, rI, rJ);
    }

    /**
     * Auxiliary method for type safety
     * 
     * @param <V>
     * @param mSolution
     * @param nodeI
     * @param nodeJ
     * @param rI
     * @param rJ
     * @return
     */
    protected <V extends INodeVisit> boolean executeMove(S solution, int i, int j, IRoute<V> rI,
            IRoute<V> rJ) {
        if (i != j || rI != rJ) {
            rJ.setNodeAt(j, rI.setNodeAt(i, rJ.getNodeAt(j)));
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String getShortName() {
        return "swap";
    }
}
