/**
 *
 */
package vroom.trsp.optimization.localSearch;

import vroom.common.heuristics.Move;
import vroom.common.heuristics.NeighborhoodBase;
import vroom.common.utilities.optimization.IInstance;
import vroom.common.utilities.optimization.IMove;
import vroom.common.utilities.optimization.IParameters;
import vroom.trsp.datamodel.ITRSPTour;
import vroom.trsp.datamodel.TRSPTour;
import vroom.trsp.datamodel.TRSPTour.TRSPTourIterator;
import vroom.trsp.optimization.TRSPMove;
import vroom.trsp.optimization.constraints.TourConstraintHandler;
import vroom.trsp.optimization.localSearch.TRSPTwoOpt.TRSPTwoOptMove;

/**
 * <code>TRSPTwoOpt</code> is an implementation of the 2-opt neighborhood for the TRSP.
 * <p>
 * Creation date: May 3, 2011 - 2:18:11 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TRSPTwoOpt extends NeighborhoodBase<ITRSPTour, TRSPTwoOptMove> implements
        TRSPTourNeighborhood<TRSPTwoOptMove> {

    /**
     * <code>TRSPTwoOptMove</code> is a specialization of {@link Move} to the {@link TRSPTwoOpt} neighborhood
     * <p>
     * Creation date: May 9, 2011 - 11:27:25 AM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    public static class TRSPTwoOptMove extends TRSPMove {

        /** the id of the tail of the first removed edge (i,j) **/
        private final int mFirst;

        /**
         * Getter for the id of the tail of the first removed edge (i,j)
         * 
         * @return the id of the tail of the first removed edge (i,j)
         */
        public int getFirst() {
            return this.mFirst;
        }

        /** the id of the tail of the second removed edge (m,n) **/
        private final int mSecond;

        /**
         * Getter for the id of the tail of the second removed edge (m,n)
         * 
         * @return the id of the tail of the second removed edge (m,n)
         */
        public int getSecond() {
            return this.mSecond;
        }

        /**
         * Creates a new <code>TRSPTwoOptMove</code>
         * 
         * @param first
         *            the if of the tail of the first removed edge (i,j)
         * @param second
         *            the if of the tail of the second removed edge (m,n), assumed to be a successor of
         *            <code>first</code>
         */
        public TRSPTwoOptMove(TRSPTour tour, int first, int second) {
            super(Double.NaN, tour);
            mFirst = first;
            mSecond = second;
        }

        @Override
        public String getMoveName() {
            return "2opt";
        }

        @Override
        public String toString() {
            return String.format("2opt[%s,%s](%.3f)", getFirst(), getSecond(), getImprovement());
        }

    }

    /**
     * Creates a new <code>TwoOptNeighborhood</code>
     * 
     * @param constraintHandler
     */
    public TRSPTwoOpt(TourConstraintHandler constraintHandler) {
        super(constraintHandler);
    }

    @Override
    public boolean executeMove(ITRSPTour itour, IMove imove) {
        TRSPTwoOptMove move = (TRSPTwoOptMove) imove;
        TRSPTour tour = (TRSPTour) itour;

        int i = move.getFirst();
        int j = tour.getSucc(i);

        int m = move.getSecond();
        int n = tour.getSucc(m);

        // Reverse j-m subtour
        TRSPTourIterator it = tour.iterator(j);
        int pred = it.next();
        while (it.hasNext() && pred != m) {
            int suc = it.next();
            tour.setPred(pred, suc);
            tour.setSucc(suc, pred);
            pred = suc;
        }

        // Relink
        // (i,m)
        tour.setSucc(i, m);
        tour.setPred(m, i);
        // (n,j)
        tour.setSucc(j, n);
        tour.setPred(n, j);

        // Propagate the update
        tour.propagateUpdate(i, n);
        // Reevaluate tour
        tour.getCostDelegate().evaluateTour(tour, true);

        checkSolution(itour, move, false, true, "");

        return false;
    }

    @Override
    public void pertub(IInstance instance, ITRSPTour tour, IParameters params) {
        throw new UnsupportedOperationException("pertub is not implemented yet " + params);
    }

    @Override
    protected TRSPTwoOptMove randomNonImproving(ITRSPTour tour, IParameters params) {
        throw new UnsupportedOperationException("randomNonImproving is not implemented yet "
                + params);
    }

    @Override
    protected TRSPTwoOptMove randomFirstImprovement(ITRSPTour tour, IParameters params) {
        throw new UnsupportedOperationException("randomFirstImprovement is not implemented yet "
                + params);
    }

    @Override
    protected TRSPTwoOptMove deterministicBestImprovement(ITRSPTour tour, IParameters params) {
        return deterministicExploration(tour, params, false);
    }

    @Override
    protected TRSPTwoOptMove deterministicFirstImprovement(ITRSPTour tour, IParameters params) {
        return deterministicExploration(tour, params, true);
    }

    protected TRSPTwoOptMove deterministicExploration(ITRSPTour itour, IParameters params,
            boolean first) {
        if (itour.length() < 4)
            // No 2-opt move can be defined with a tour with less than 4 nodes
            return null;

        TRSPTwoOptMove move = null;
        TRSPTour tour = (TRSPTour) itour;

        TRSPTourIterator firstIt = tour.iterator();

        // Skip first node (depot)
        // firstIt.next();

        while (firstIt.hasNext()) {
            int i = firstIt.next();

            TRSPTourIterator secondIt = tour.iterator(i);

            // Skip first node (i)
            if (secondIt.hasNext())
                secondIt.next();
            else
                break;
            // Skip second node (j)
            // if (secondIt.hasNext())
            // secondIt.next();
            // else
            // break;

            while (secondIt.hasNext()) {
                int m = secondIt.next();
                if (!secondIt.hasNext())
                    // m is the last node
                    break;

                TRSPTwoOptMove tmp = new TRSPTwoOptMove(tour, i, m);
                tour.getCostDelegate().evaluateMove(tmp);

                if (tmp.isImproving()) {
                    // Found an improving move
                    // Evaluate feasibility
                    boolean feas = getConstraintHandler().isFeasible(tour, tmp);
                    if (feas) {
                        if (params.acceptFirstImprovement()) {
                            // First improvement
                            return tmp;
                        } else if (move == null || tmp.getImprovement() > move.getImprovement()) {
                            // Update best move
                            move = tmp;
                        }
                    } else {
                        // The move is infeasible, prune the rest of the (i,*) neighborhood
                        break;
                    }
                }
            }
        }

        return move;
    }

    @Override
    public String getShortName() {
        return "twoOpt";
    }

}
