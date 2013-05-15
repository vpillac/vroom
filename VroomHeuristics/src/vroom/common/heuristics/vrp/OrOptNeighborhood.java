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
import vroom.common.modeling.util.CostCalculationDelegate;
import vroom.common.utilities.optimization.IConstraint;
import vroom.common.utilities.optimization.IMove;
import vroom.common.utilities.optimization.IParameters;

/**
 * <code>OrOptNeighborhood</code> is an implementation of the Or-opt neighborhood.
 * <p>
 * Creation date: Jul 2, 2010 - 1:59:41 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class OrOptNeighborhood<S extends IVRPSolution<?>> extends
        GenericNeighborhood<S, OrOptMove<S>> {

    /** Default for the maximum length of the relocated segment */
    public static int DEFAULT_MAX_LENGTH = 3;

    /** the maximum length of the relocated segment **/
    private int       mMaxLength;

    /**
     * Getter for maxLength
     * 
     * @return the maximum length of the relocated segment
     */
    public int getMaxLength() {
        return this.mMaxLength;
    }

    /**
     * Setter for maxLength : the maximum length of the relocated segment
     * 
     * @param maxLength
     *            the value to be set for maxLength
     */
    public void setMaxLength(int maxLength) {
        this.mMaxLength = maxLength;
    }

    /**
     * Creates a new <code>OrOptNeighborhood</code>
     * 
     * @param constraintHandler
     */
    public OrOptNeighborhood(ConstraintHandler<S> constraintHandler) {
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

        setMaxLength(DEFAULT_MAX_LENGTH);
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
    protected LinkedList<OrOptMove<S>> generateCandidateList(S solution, IParameters params) {
        LinkedList<OrOptMove<S>> candidates = new LinkedList<OrOptMove<S>>();

        // Iterate over all routes
        for (int rI = 0; rI < solution.getRouteCount(); rI++) {
            IRoute<?> route = solution.getRoute(rI);
            // Iterate over all nodes of the route
            for (int i = 0; i < route.length(); i++) {
                if (!route.getNodeAt(i).isFixed()) {
                    // Iterate over the possible segments
                    for (int j = i; j < Math.min(i + getMaxLength() + 1, solution.getRoute(rI)
                            .length()); j++) {
                        if (route.getNodeAt(j).isFixed()) {
                            // containing a
                            // fixed node
                            break;
                        } else {
                            candidates.add(new OrOptMove<S>(solution, rI, i, j));
                        }
                    }
                }
            }
        }

        return candidates;
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
    public OrOptMove<S> deterministicFirstImprovement(S solution, IParameters params) {
        // Iterate over all routes
        for (int rI = 0; rI < solution.getRouteCount(); rI++) {
            IRoute<?> route = solution.getRoute(rI);
            // Iterate over all nodes of the route
            for (int i = 0; i < route.length(); i++) {
                if (!route.getNodeAt(i).isFixed()) {
                    // Iterate over the possible segments
                    for (int j = i; j < Math.min(i + getMaxLength() + 1, solution.getRoute(rI)
                            .length()); j++) {
                        if (route.getNodeAt(j).isFixed()) {
                            // containing a
                            // fixed node
                            break;
                        } else {
                            OrOptMove<S> move = new OrOptMove<S>(solution, rI, i, j);
                            evaluateCandidateMove(move);
                            if (getConstraintHandler().isFeasible(solution, move)
                                    && (getAcceptanceCriterion(params).accept(solution, this, move) || params
                                            .acceptNonImproving())) {
                                return move;
                            }
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
    protected double evaluateCandidateMove(OrOptMove<S> cand) {
        double bestImprov = Double.NEGATIVE_INFINITY;
        int bestRoute = -1, bestIns = -1;
        boolean bestInsRev = false;

        CostCalculationDelegate cd = cand.getSolution().getParentInstance().getCostDelegate();
        IRoute<?> route = cand.getSolution().getRoute(cand.getRouteI());
        INodeVisit nI = route.getNodeAt(cand.getI());
        INodeVisit nJ = route.getNodeAt(cand.getJ());

        boolean singleton = cand.getI() == cand.getJ();

        // Iterate over all routes
        for (int r = 0; r < cand.getSolution().getRouteCount(); r++) {
            IRoute<?> rIns = cand.getSolution().getRoute(r);
            // Iterate over possible insertion points
            for (int ins = 0; ins <= rIns.length(); ins++) {
                // Exclude fixed nodes and positions in the sequence
                if ((ins < rIns.length() && !rIns.getNodeAt(ins).isFixed() || ins == rIns.length()
                        && !rIns.getLastNode().isFixed())
                        && (r != cand.getRouteI() || ins > cand.getJ() + 1 || ins < cand.getI())) {
                    cand.setInsertion(r, ins, false);
                    // Check feasibility to prune infeasible insertions
                    // if (getConstraintHandler().checkMove(cand.getSolution(),
                    // cand)) {
                    double improv = 0;

                    INodeVisit nIns = ins < rIns.length() ? rIns.getNodeAt(ins) : null;
                    INodeVisit nPIns = ins > 0 ? rIns.getNodeAt(ins - 1) : null;

                    // Insertion cost if no reverse
                    // ADD (ins-1,i)
                    double nrCHead = nPIns != null ? cd.getCost(nPIns, nI, rIns.getVehicle()) : 0;
                    // ADD (j,ins)
                    double nrCTail = nIns != null ? cd.getCost(nJ, nIns, rIns.getVehicle()) : 0;

                    double rCHead = 0, rCTail = 0;
                    if (!singleton) {
                        // Insertion cost with reverse
                        // ADD (ins-1,j)
                        rCHead = ins > 0 ? cd.getCost(nPIns, nJ, rIns.getVehicle()) : 0;
                        // ADD (i,ins)
                        rCTail = ins < rIns.length() ? cd.getCost(nI, nIns, rIns.getVehicle()) : 0;
                    }
                    boolean reverse;
                    if (singleton || nrCHead + nrCTail < rCHead + rCTail) {
                        // Its cheaper not to reverse
                        reverse = false;
                        // Head insertion
                        improv -= nrCHead;

                        // Tail insertion
                        improv -= nrCTail;
                    } else {
                        // Its cheaper to reverse
                        reverse = true;
                        // Head insertion
                        // ADD (ins-1,i)
                        improv -= rCHead;

                        // Tail insertion
                        // ADD (j,ins)
                        improv -= rCTail;
                    }

                    if (nIns != null && nPIns != null) {
                        // REM (ins-1,ins)
                        improv += cd.getCost(nPIns, nIns, rIns.getVehicle());
                    }

                    if (improv > bestImprov
                            && getConstraintHandler().isFeasible(cand.getSolution(), cand)) {
                        bestRoute = r;
                        bestIns = ins;
                        bestImprov = improv;
                        bestInsRev = reverse;
                    }
                }
                // }

            }
        }

        // Extraction improvement
        // REM (i-1,i)
        if (cand.getI() > 0) {
            bestImprov += cd.getCost(route.getNodeAt(cand.getI() - 1), nI, route.getVehicle());
        }

        // REM (j,j+1)
        if (cand.getJ() < route.length() - 1) {
            bestImprov += cd.getCost(nJ, route.getNodeAt(cand.getJ() + 1), route.getVehicle());
        }

        // ADD (i-1,j+1)
        if (cand.getI() > 0 && cand.getJ() < route.length() - 1) {
            bestImprov -= cd.getCost(route.getNodeAt(cand.getI() - 1),
                    route.getNodeAt(cand.getJ() + 1), route.getVehicle());
        }

        // Set the best insertion
        cand.setInsertion(bestRoute, bestIns, bestInsRev);
        cand.setImprovement(bestImprov);

        return bestImprov;
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
        OrOptMove<?> mve = (OrOptMove<?>) move;

        if (mve.getInsertionRoute() < 0) {
            return false;
        }

        IRoute rSrc = solution.getRoute(mve.getRouteI());
        IRoute rDest = solution.getRoute(mve.getInsertionRoute());

        int i = mve.getI();
        int j = mve.getJ();
        int ins = mve.getInsertionIndex();

        return executeMove(rSrc, rDest, i, j, ins, mve.isStringReversed());
    }

    /**
     * Auxiliary method for type safety
     * 
     * @param rSrc
     * @param rDest
     * @param i
     * @param j
     * @param ins
     * @return
     */
    private <V extends INodeVisit> boolean executeMove(IRoute<V> rSrc, IRoute<V> rDest, int i,
            int j, int ins, boolean rev) {

        if (rSrc == rDest && ins > j) {
            // Shift the ins index
            ins -= j - i + 1;
        }

        IRoute<V> subroute = rSrc.extractSubroute(i, j);

        if (rev) {
            subroute.reverseRoute();
        }

        return rDest.insertSubroute(ins, subroute);
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.heuristics.GenericNeighborhood#toString()
     */
    @Override
    public String toString() {
        return String.format("%s (maxLenght:%s)", super.toString(), getMaxLength());
    }

    @Override
    public String getShortName() {
        return "OrOpt";
    }

}
