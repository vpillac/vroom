/**
 * 
 */
package vroom.common.heuristics.vrp;

import java.util.LinkedList;

import vroom.common.heuristics.ConstraintHandler;
import vroom.common.heuristics.GenericNeighborhood;
import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.IRoute;
import vroom.common.modeling.dataModel.IVRPSolution;
import vroom.common.modeling.dataModel.Vehicle;
import vroom.common.modeling.util.CostCalculationDelegate;
import vroom.common.utilities.optimization.IMove;
import vroom.common.utilities.optimization.IParameters;

/**
 * <code>StringExchangeNeighborhood</code> is a generic implementation of the string-exchange neighborhood.
 * <p>
 * Creation date: Jul 8, 2010 - 3:30:54 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class StringExchangeNeighborhood<S extends IVRPSolution<?>> extends
        GenericNeighborhood<S, StringExchangeMove<S>> {

    /** A default value for the maximum string length */
    public static int MAXIMUM_STRING_LENGTH = 4;

    /** the maximum string length to be evaluated **/
    private int       mMaxLength            = MAXIMUM_STRING_LENGTH;

    /**
     * Getter for the maximum string length
     * 
     * @return the maximum string length to be evaluated
     */
    public int getMaxLength() {
        return this.mMaxLength;
    }

    /**
     * Setter for the maximum string length to be evaluated
     * 
     * @param maxLength
     *            the value to be set for the maximum string length
     */
    public void setMaxLength(int maxLength) {
        this.mMaxLength = maxLength;
    }

    /** the minimum string length to be evaluated **/
    private int mMinLength = 1;

    /**
     * Getter for the minimum string length
     * 
     * @return the minimum string length to be evaluated
     */
    public int getMinLength() {
        return this.mMinLength;
    }

    /**
     * Setter for the minimum string length to be evaluated
     * 
     * @param minLength
     *            the value to be set for the minimum string length
     */
    public void setMinLength(int minLength) {
        this.mMinLength = minLength;
    }

    /**
     * Creates a new <code>StringExchangeNeighborhood</code> based on the given constraint handler
     * 
     * @param ctrHandler
     *            a constraint handler
     */
    public StringExchangeNeighborhood(ConstraintHandler<S> constraintHandler) {
        this(constraintHandler, MAXIMUM_STRING_LENGTH);
    }

    /**
     * Creates a new <code>StringExchangeNeighborhood</code> based on the given constraint handler
     * 
     * @param ctrHandler
     *            a constraint handler
     * @param maxLength
     *            the maximum length of the string to be considered
     */
    public StringExchangeNeighborhood(ConstraintHandler<S> constraintHandler, int maxLength) {
        super(constraintHandler);
        setMaxLength(maxLength);
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
        StringExchangeMove<S> ex = (StringExchangeMove<S>) move;

        if (ex.getFirstRoute() == ex.getSecondRoute()) {

        }

        IRoute r1 = solution.getRoute(ex.getFirstRoute());
        IRoute r2 = solution.getRoute(ex.getSecondRoute());

        return executeMove(r1, ex.getNodeI(), ex.getNodeJ(), r2, ex.getNodeK(), ex.getNodeL(),
                ex.isReverseFirst(), ex.isReverseSecond());
    }

    /**
     * Auxiliary method for type safety
     * 
     * @param <V>
     * @param r1
     * @param nodeI
     * @param nodeJ
     * @param r2
     * @param nodeK
     * @param nodeL
     * @param rev1
     * @param rev2
     * @return
     */
    private <V extends INodeVisit> boolean executeMove(IRoute<V> r1, int nodeI, int nodeJ,
            IRoute<V> r2, int nodeK, int nodeL, boolean rev1, boolean rev2) {

        IRoute<V> string2 = r2.extractSubroute(nodeK, nodeL);
        IRoute<V> string1 = r1.extractSubroute(nodeI, nodeJ);

        if (rev1) {
            string1.reverseRoute();
        }
        if (rev2) {
            string2.reverseRoute();
        }

        r1.insertSubroute(nodeI, string2);

        if (r1 == r2) {
            nodeK = nodeK - (nodeJ - nodeI) + nodeL - nodeK;
        }

        r2.insertSubroute(nodeK, string1);

        return true;
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
    public StringExchangeMove<S> deterministicFirstImprovement(S solution, IParameters params) {
        // Iterate over all routes
        for (int r1 = 0; r1 < solution.getRouteCount(); r1++) {
            IRoute<?> route1 = solution.getRoute(r1);
            // Iterate over all nodes of the first route
            for (int i = 0; i < route1.length(); i++) {
                if (!route1.getNodeAt(i).isFixed()) {
                    // Iterate over the possible segments of the first route
                    for (int j = i + getMinLength() - 1; j < Math.min(i + getMaxLength() + 1,
                            route1.length()); j++) {
                        if (route1.getNodeAt(j).isFixed()) {
                            break;
                        } else {
                            // Iterate over all routes
                            for (int r2 = r1; r2 < solution.getRouteCount(); r2++) {
                                IRoute<?> route2 = solution.getRoute(r2);
                                for (int k = r1 == r2 ? j + 1 : 0; k < route2.length(); k++) {
                                    if (!route2.getNodeAt(k).isFixed()) {
                                        // Iterate over the possible segments of
                                        // the second route
                                        for (int l = k
                                                + Math.max(getMinLength() - 1, i == j ? 1 : 0); l < Math
                                                .min(k + getMaxLength() + 1, route2.length()); l++) {
                                            if (route2.getNodeAt(l).isFixed()) {
                                                break;
                                            } else {
                                                StringExchangeMove<S> move = new StringExchangeMove<S>(
                                                        solution, r1, r2, i, j, k, l);
                                                boolean feasible = false;
                                                if (getConstraintHandler().isFeasible(solution,
                                                        move)) {
                                                    evaluateCandidateMove(move);
                                                    feasible = true;
                                                }
                                                if (feasible
                                                        && (getAcceptanceCriterion(params).accept(
                                                                solution, this, move) || params
                                                                .acceptNonImproving())) {
                                                    return move;
                                                } else {
                                                    move = null;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    // FIXME implement random exploration
    // @Override
    // public StringExchangeMove<S> randomExploration(S solution,
    // IParameters params) {
    //
    // StringExchangeMove<S> move = null;
    // while (move==null) {
    //
    //
    //
    // }
    // return move;
    // };

    /*
     * (non-Javadoc)
     * 
     * @see
     * vroom.common.heuristics.GenericNeighborhood#generateCandidateList(vroom.
     * common.utilities.optimization.ISolution,
     * vroom.common.utilities.optimization.IParameters)
     */
    @Override
    protected LinkedList<StringExchangeMove<S>> generateCandidateList(S solution, IParameters params) {
        LinkedList<StringExchangeMove<S>> candidates = new LinkedList<StringExchangeMove<S>>();

        // Iterate over all routes
        for (int r1 = 0; r1 < solution.getRouteCount(); r1++) {
            IRoute<?> route1 = solution.getRoute(r1);
            // Iterate over all nodes of the first route
            for (int i = 0; i < route1.length(); i++) {
                if (!route1.getNodeAt(i).isFixed()) {
                    // Iterate over the possible segments of the first route
                    for (int j = i; j < Math.min(i + getMaxLength() + 1, route1.length()); j++) {
                        if (route1.getNodeAt(j).isFixed()) {
                            break;
                        } else {
                            // Iterate over all routes
                            for (int r2 = r1; r2 < solution.getRouteCount(); r2++) {
                                IRoute<?> route2 = solution.getRoute(r2);
                                for (int k = r1 == r2 ? j + 1 : 0; k < route2.length(); k++) {
                                    if (!route2.getNodeAt(k).isFixed()) {
                                        // Iterate over the possible segments of
                                        // the second route
                                        for (int l = k + (i == j ? 1 : 0); l < Math.min(k
                                                + getMaxLength() + 1, route2.length()); l++) {
                                            if (route2.getNodeAt(l).isFixed()) {
                                                break;
                                            } else {
                                                candidates.add(new StringExchangeMove<S>(solution,
                                                        r1, r2, i, j, k, l));
                                            }
                                        }
                                    }
                                }
                            }
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
     * @see
     * vroom.common.heuristics.GenericNeighborhood#evaluateCandidateMove(vroom.
     * common.heuristics.Move)
     */
    @Override
    protected double evaluateCandidateMove(StringExchangeMove<S> cand) {
        double improv = 0;

        // Assumes cost symmetry
        if (!cand.getSolution().getParentInstance().isSymmetric()) {
            throw new UnsupportedOperationException(
                    "This implementation only support symetric instances");
        }

        IRoute<?> r1 = cand.getSolution().getRoute(cand.getFirstRoute());
        Vehicle v1 = r1.getVehicle();
        IRoute<?> r2 = cand.getSolution().getRoute(cand.getSecondRoute());
        Vehicle v2 = r2.getVehicle();

        if (v1.getVariableCost() != v2.getVariableCost()) {
            throw new UnsupportedOperationException(
                    "This implementation only support identical vehicles");
        }

        boolean consec = r1 == r2 && cand.getNodeJ() + 1 == cand.getNodeK();

        INodeVisit i = r1.getNodeAt(cand.getNodeI());
        INodeVisit pi = cand.getNodeI() > 0 ? r1.getNodeAt(cand.getNodeI() - 1) : null;
        INodeVisit j = r1.getNodeAt(cand.getNodeJ());
        INodeVisit sj = cand.getNodeJ() < r1.length() - 1 ? r1.getNodeAt(cand.getNodeJ() + 1)
                : null;
        INodeVisit k = consec ? sj : r2.getNodeAt(cand.getNodeK());
        INodeVisit pk = consec ? j : cand.getNodeK() > 0 ? r2.getNodeAt(cand.getNodeK() - 1) : null;
        INodeVisit l = r2.getNodeAt(cand.getNodeL());
        INodeVisit sl = cand.getNodeL() < r2.length() - 1 ? r2.getNodeAt(cand.getNodeL() + 1)
                : null;

        CostCalculationDelegate cd = cand.getSolution().getParentInstance().getCostDelegate();

        if (pi != null) {
            improv += cd.getCost(pi, i, v1);
        }
        if (sj != null) {
            improv += cd.getCost(j, sj, v1);
        }

        if (pk != null && !consec) {
            improv += cd.getCost(pk, k, v2);
        }
        if (sl != null) {
            improv += cd.getCost(l, sl, v2);
        }

        boolean reverseFirst = false, reverseSecond = false;
        double cjsl = sl != null ? cd.getCost(j, sl, v2) : 0; // (j,l+1)
        double cpik = pi != null ? cd.getCost(pi, k, v1) : 0; // (i-1,k)

        double cpil = pi != null ? cd.getCost(pi, l, v1) : 0; // (i-1,l)
        double cisl = sl != null ? cd.getCost(i, sl, v2) : 0; // (i,l+1)
        if (!consec) {
            // No reverse
            double clsj = sj != null ? cd.getCost(l, sj, v1) : 0; // (l,j+1) //*
            // ..,i-1,k,..,l,j+1,..
            double nrev2 = cpik + clsj;
            double cpki = pk != null ? cd.getCost(pk, i, v2) : 0; // (k-1,i) //*
            // ..,k-1,i,..,j,l,..
            double nrev1 = cpki + cjsl;

            // Reverse
            double cksj = sj != null ? cd.getCost(k, sj, v1) : 0; // (k,j+1) //*
            // ..,i-1,l,..,k,j+1,..
            double rev2 = cpil + cksj;
            double cpkj = pk != null ? cd.getCost(pk, j, v2) : 0; // (k-1,j) //*
            // ..,k-1,j,..,i,l+1,..
            double rev1 = cpkj + cisl;

            if (rev2 < nrev2) {
                reverseSecond = true;
                improv -= rev2;
            } else {
                improv -= nrev2;
            }
            if (rev1 < nrev1) {
                reverseFirst = true;
                improv -= rev1;
            } else {
                improv -= nrev1;
            }
        } else {

            double cli = cd.getCost(l, i, v1); // (l,i)
            double clj = cd.getCost(l, j, v1); // (l,j)
            double cki = cd.getCost(k, i, v1); // (k,i)
            double ckj = cd.getCost(k, j, v1); // (k,j)

            double[] costs = new double[4];

            // Consecutive subroutes
            // No rev 1 - No rev 2
            // ..,i-1,k,..,l,i,..,j,l+1,..
            costs[0] = cpik + cli + cjsl;
            // Rev 1 - No rev 2
            // ..,i-1,k,..,l,j,..,i,l+1,..
            costs[1] = cpik + clj + cisl;
            // No rev 1 - Rev 2
            // ..,i-1,l,..,k,i,..,j,l+1,..
            costs[2] = cpil + cki + cjsl;
            // Rev 1 - Rev 2
            // ..,i-1,l,..,k,j,..,i,l+1,..
            costs[3] = cpil + ckj + cisl;

            double min = Double.MAX_VALUE;
            int best = -1;
            for (int m = 0; m < costs.length; m++) {
                if (costs[m] < min) {
                    min = costs[m];
                    best = m;
                }
            }

            reverseFirst = best == 1 || best == 3;
            reverseSecond = best == 2 || best == 3;

            improv -= min;

        }

        cand.setReverseFirst(reverseFirst);
        cand.setReverseSecond(reverseSecond);

        cand.setImprovement(improv);

        return improv;
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
        return "strExg-" + getMaxLength();
    }

}
