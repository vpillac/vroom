package vroom.trsp.optimization.localSearch;

import vroom.common.heuristics.ConstraintHandler;
import vroom.common.heuristics.Move;
import vroom.common.heuristics.NeighborhoodBase;
import vroom.common.utilities.optimization.IInstance;
import vroom.common.utilities.optimization.IMove;
import vroom.common.utilities.optimization.IParameters;
import vroom.trsp.datamodel.ITRSPTour;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.datamodel.TRSPTour;
import vroom.trsp.optimization.localSearch.TRSPCompositeNeighborhood.CompositeMove;

public class TRSPCompositeNeighborhood<M extends IMove, N extends TRSPTourNeighborhood<M>> extends
        NeighborhoodBase<TRSPSolution, CompositeMove<M>> {

    private final N mNeighborhood;

    /**
     * Creates a new <code>TRSPCompositeNeighborhood</code>
     * 
     * @param constraintHandler
     * @param neighborhood
     */
    public TRSPCompositeNeighborhood(ConstraintHandler<TRSPSolution> constraintHandler,
            N neighborhood) {
        super(constraintHandler);
        mNeighborhood = neighborhood;
    }

    @Override
    public boolean executeMove(TRSPSolution solution, IMove move) {
        @SuppressWarnings("unchecked")
        CompositeMove<M> mve = (CompositeMove<M>) move;
        return mNeighborhood.executeMove(mve.getTour(), mve.getMove());
    }

    @Override
    public void pertub(IInstance instance, TRSPSolution solution, IParameters parameters) {
        int t = parameters.getRandomStream().nextInt(0, solution.getTourCount());
        mNeighborhood.pertub(instance, solution.getTour(t), parameters);
    }

    @Override
    protected CompositeMove<M> randomNonImproving(TRSPSolution solution, IParameters params) {
        throw new UnsupportedOperationException("randomNonImproving is not implemented yet "
                + params);
    }

    @Override
    protected CompositeMove<M> randomFirstImprovement(TRSPSolution solution, IParameters params) {
        throw new UnsupportedOperationException("randomFirstImprovement is not implemented yet "
                + params);
    }

    @Override
    protected CompositeMove<M> deterministicBestImprovement(TRSPSolution solution,
            IParameters params) {
        CompositeMove<M> best = null;
        for (TRSPTour t : solution) {
            M mve = mNeighborhood.exploreNeighborhood(t, params);
            if (mve != null && (best == null || mve.compareTo(best) > 0))
                best = new CompositeMove<M>(mve, t);
        }
        return best;
    }

    @Override
    protected CompositeMove<M> deterministicFirstImprovement(TRSPSolution solution,
            IParameters params) {
        for (TRSPTour t : solution) {
            M mve = mNeighborhood.exploreNeighborhood(t, params);
            if (mve != null && mve.isImproving())
                return new CompositeMove<M>(mve, t);
        }
        return null;
    }

    @Override
    public String getShortName() {
        return "composite";
    }

    /**
     * The class <code>CompositeMove</code> is a wrapper around a tour move
     * <p>
     * Creation date: Feb 8, 2012 - 4:15:47 PM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     * @param <MM>
     */
    public static class CompositeMove<MM extends IMove> extends Move {

        private final MM mMove;

        /**
         * Getter for <code>move</code>
         * 
         * @return the move
         */
        public MM getMove() {
            return mMove;
        }

        private final ITRSPTour mTour;

        /**
         * Getter for <code>tour</code>
         * 
         * @return the tour
         */
        public ITRSPTour getTour() {
            return mTour;
        }

        /**
         * Creates a new <code>CompositeMove</code>
         * 
         * @param move
         * @param tour
         */
        public CompositeMove(MM move, ITRSPTour tour) {
            mMove = move;
            mTour = tour;
        }

        @Override
        public int compareTo(IMove o) {
            return getMove().compareTo(o);
        }

        @Override
        public double getImprovement() {
            return getMove().getImprovement();
        }

        @Override
        public void setImprovement(double imp) {
            getMove().setImprovement(imp);
        }

        @Override
        public boolean isImproving() {
            return getMove().isImproving();
        }

        @Override
        public String getMoveName() {
            return getMove().getMoveName();
        }

    }

}
