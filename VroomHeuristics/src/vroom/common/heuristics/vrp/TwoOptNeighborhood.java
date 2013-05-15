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
 * <code>TwoOptNeighborhood</code> is a generic implementation of the 2-Opt neighborhood.
 * <p>
 * Creation date: Jun 18, 2010 - 4:19:34 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TwoOptNeighborhood<S extends IVRPSolution<?>> extends
        GenericNeighborhood<S, TwoOptMove> {

    /**
     * Creates a new <code>TwoOptNeighborhood</code>
     * 
     * @param constraintHandler
     */
    public TwoOptNeighborhood(ConstraintHandler<S> constraintHandler) {
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
     * Creates a new <code>TwoOptNeighborhood</code>
     */
    public TwoOptNeighborhood() {
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected boolean executeMoveImplem(S solution, IMove move) {
        if (!(move instanceof TwoOptMove)) {
            throw new IllegalArgumentException("The move must be of type TwoOptMove");
        }

        boolean r;

        TwoOptMove twoOptMove = (TwoOptMove) move;

        IRoute rI = solution.getRoute(twoOptMove.getRouteI());
        IRoute rJ = null;
        int i = twoOptMove.getI();
        int j = twoOptMove.getJ();

        // Intra route move
        if (twoOptMove.getRouteI() == twoOptMove.getRouteJ()) {
            // Remove (nodeI,nodeI+1) and (nodeJ,nodeJ+1)
            // Reconnect nodeI with nodeJ and nodeI+1 with nodeJ+1
            rI.reverseSubRoute(i + 1, j);

            // rI.updateCost(-twoOptMove.getImprovement());

            r = true;
            // Inter route move
        } else {
            rJ = solution.getRoute(twoOptMove.getRouteJ());

            // Auxiliary method to solve type safety problems
            r = executeMove(rI, rJ, i, j, twoOptMove.isStar());
        }
        return r;
    }

    /**
     * Auxiliary method used for type safety
     * 
     * @param <V>
     * @param rI
     * @param rJ
     * @param nodeI
     * @param nodeJ
     * @param star
     * @return
     */
    protected <V extends INodeVisit> boolean executeMove(IRoute<V> rI, IRoute<V> rJ, int i, int j,
            boolean star) {

        if (!star) {
            // standard 2-opt
            // nodeI linked with nodeJ
            // nodeI+1 linked with nodeJ+1

            IRoute<V> startJ = rJ.extractSubroute(0, j);
            IRoute<V> endI = rI.extractSubroute(i + 1, rI.length() - 1);

            startJ.reverseRoute();
            endI.reverseRoute();

            rI.appendRoute(startJ);
            rJ.insertSubroute(0, endI);

        } else {
            // special 2-opt*
            // nodeI linked with nodeJ+1
            // nodeI+1 linked with nodeJ

            IRoute<V> endJ = rJ.extractSubroute(j + 1, rJ.length() - 1);
            IRoute<V> endI = rI.extractSubroute(i + 1, rI.length() - 1);

            rI.appendRoute(endJ);
            rJ.appendRoute(endI);
        }

        return true;
    }

    /**
     * Compute the (best) 2-opt move defined by the arcs <code>(nodeI,nodeI+1)</code> in route <code>rJ</code> and
     * <code>(nodeJ,nodeJ+1)</code> in route <code>rJ</code>
     * 
     * @param mSolution
     * @param rI
     * @param rJ
     * @param nodeI
     * @param nodeJ
     * @return the (best) 2-opt move for the given routes and nodes
     */
    protected TwoOptMove newMove(IVRPSolution<?> solution, int rI, int rJ, int i, int j) {
        CostCalculationDelegate costHelper = solution.getParentInstance().getCostDelegate();
        Vehicle v1 = solution.getRoute(rI).getVehicle();
        Vehicle v2 = solution.getRoute(rJ).getVehicle();

        INodeVisit a = solution.getRoute(rI).getNodeAt(i);
        INodeVisit b = solution.getRoute(rI).getNodeAt(i + 1);
        INodeVisit c = solution.getRoute(rJ).getNodeAt(j);
        INodeVisit d = solution.getRoute(rJ).getNodeAt(j + 1);

        double improv = 0;
        boolean star = false;

        // 2-opt intra/inter-route
        // nodeI (a) linked with nodeJ (c)
        // nodeI+1 (b) linked with nodeJ+1 (d)
        improv = -costHelper.getCost(a, c, v1) - costHelper.getCost(b, d, v2)
                + costHelper.getCost(a, b, v1) + costHelper.getCost(c, d, v2);

        if (rI != rJ) {
            // special 2-opt*
            // nodeI (a) linked with nodeJ+1 (d)
            // nodeI+1 (b) linked with nodeJ (c)
            double improvStar = -costHelper.getCost(a, d, v1) - costHelper.getCost(b, c, v2)
                    + costHelper.getCost(a, b, v1) + costHelper.getCost(c, d, v2);
            if (improvStar > improv) {
                improv = improvStar;
                star = true;
            }
        }

        return new TwoOptMove(improv, solution, rI, rJ, i, j, star);
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
    public TwoOptMove deterministicFirstImprovement(S solution, IParameters params) {
        // Iterate over all routes
        for (int r1 = 0; r1 < solution.getRouteCount(); r1++) {
            for (int r2 = r1; r2 < solution.getRouteCount(); r2++) {
                // Iterate over the nodes of the first route
                for (int i = 0; i < solution.getRoute(r1).length() - 1; i++) {
                    // Iterate over the nodes of the second route
                    for (int j = r1 != r2 ? 0 : i + 2; j < solution.getRoute(r2).length() - 1; j++) {
                        TwoOptMove move = new TwoOptMove(Double.NEGATIVE_INFINITY, solution, r1,
                                r2, i, j, false);
                        move.setImprovement(evaluateCandidateMove(move));
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
     * vroom.common.heuristics.GenericNeighborhood#evaluateCandidateMove(vroom.
     * common.heuristics.Move)
     */
    @Override
    protected double evaluateCandidateMove(TwoOptMove cand) {
        CostCalculationDelegate costHelper = cand.mSolution.getParentInstance().getCostDelegate();
        Vehicle v1 = cand.mSolution.getRoute(cand.getRouteI()).getVehicle();
        Vehicle v2 = cand.mSolution.getRoute(cand.getRouteJ()).getVehicle();

        INodeVisit a = cand.mSolution.getRoute(cand.getRouteI()).getNodeAt(cand.getI());
        INodeVisit b = cand.mSolution.getRoute(cand.getRouteI()).getNodeAt(cand.getI() + 1);
        INodeVisit c = cand.mSolution.getRoute(cand.getRouteJ()).getNodeAt(cand.getJ());
        INodeVisit d = cand.mSolution.getRoute(cand.getRouteJ()).getNodeAt(cand.getJ() + 1);

        double improv = 0;
        boolean star = false;

        // 2-opt intra/inter-route
        // nodeI (a) linked with nodeJ (c)
        // nodeI+1 (b) linked with nodeJ+1 (d)
        improv = -costHelper.getCost(a, c, v1) - costHelper.getCost(b, d, v2)
                + costHelper.getCost(a, b, v1) + costHelper.getCost(c, d, v2);

        if (cand.getRouteI() != cand.getRouteJ()) {
            // special 2-opt*
            // nodeI (a) linked with nodeJ+1 (d)
            // nodeI+1 (b) linked with nodeJ (c)
            double improvStar = -costHelper.getCost(a, d, v1) - costHelper.getCost(b, c, v2)
                    + costHelper.getCost(a, b, v1) + costHelper.getCost(c, d, v2);
            if (improvStar > improv) {
                improv = improvStar;
                star = true;
            }
        }

        cand.setStar(star);
        return improv;
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
    protected LinkedList<TwoOptMove> generateCandidateList(S solution, IParameters params) {
        LinkedList<TwoOptMove> candidates = new LinkedList<TwoOptMove>();

        // Iterate over all routes
        for (int r1 = 0; r1 < solution.getRouteCount(); r1++) {
            for (int r2 = r1; r2 < solution.getRouteCount(); r2++) {
                // Iterate over the nodes of the first route
                for (int i = 0; i < solution.getRoute(r1).length() - 1; i++) {
                    // Iterate over the nodes of the second route
                    for (int j = r1 != r2 ? 0 : i + 2; j < solution.getRoute(r2).length() - 1; j++) {
                        candidates.add(new TwoOptMove(Double.NEGATIVE_INFINITY, solution, r1, r2,
                                i, j, false));
                    }
                }
            }
        }

        return candidates;
    }

    @Override
    public String getShortName() {
        return "twoOpt";
    }

}
