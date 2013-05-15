/**
 * 
 */
package vroom.trsp.optimization.localSearch;

import vroom.common.heuristics.ConstraintHandler;
import vroom.common.heuristics.Move;
import vroom.common.heuristics.NeighborhoodBase;
import vroom.common.utilities.optimization.IInstance;
import vroom.common.utilities.optimization.IMove;
import vroom.common.utilities.optimization.INeighborhood;
import vroom.common.utilities.optimization.IParameters;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.datamodel.TRSPTour;
import vroom.trsp.optimization.InsertionMove;
import vroom.trsp.optimization.RemoveMove;
import vroom.trsp.optimization.constraints.TourConstraintHandler;
import vroom.trsp.optimization.localSearch.TRSPRelocate.RelocateMove;

/**
 * <code>TRSPRelocate</code> is an implementation of {@link INeighborhood} that remove a request from a tour and insert
 * it in a different tour.
 * <p>
 * Creation date: Feb 8, 2012 - 3:32:33 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TRSPRelocate extends NeighborhoodBase<TRSPSolution, RelocateMove> {

    private final TourConstraintHandler mTourCtrHandler;

    /**
     * Creates a new <code>TRSPRelocate</code>
     * 
     * @param constraintHandler
     */
    public TRSPRelocate(ConstraintHandler<TRSPSolution> constraintHandler,
            TourConstraintHandler tourCtrHandler) {
        super(constraintHandler);
        mTourCtrHandler = tourCtrHandler;
    }

    @Override
    public boolean executeMove(TRSPSolution solution, IMove imove) {
        RelocateMove mve = (RelocateMove) imove;

        ((TRSPTour) mve.getRemove().getTour()).removeNode(mve.getRemove().getNodeId());
        ((TRSPTour) mve.getInsertion().getTour()).insertAfter(
                mve.getInsertion().getInsertionPred(), mve.getInsertion().getNodeId());

        return true;
    }

    @Override
    public void pertub(IInstance instance, TRSPSolution solution, IParameters params) {
        throw new UnsupportedOperationException("pertub is not implemented yet " + params);
    }

    @Override
    protected RelocateMove randomNonImproving(TRSPSolution solution, IParameters params) {
        throw new UnsupportedOperationException("randomNonImproving is not implemented yet "
                + params);
    }

    @Override
    protected RelocateMove randomFirstImprovement(TRSPSolution solution, IParameters params) {
        throw new UnsupportedOperationException("randomFirstImprovement is not implemented yet "
                + params);
    }

    @Override
    protected RelocateMove deterministicBestImprovement(TRSPSolution solution, IParameters params) {
        return deterministicExploration(solution, params, false);
    }

    @Override
    protected RelocateMove deterministicFirstImprovement(TRSPSolution solution, IParameters params) {
        return deterministicExploration(solution, params, true);
    }

    /**
     * Performs a deterministic exploration of the neighborhood
     * 
     * @param solution
     * @param params
     * @param first
     * @return the first/best improving move
     */
    protected RelocateMove deterministicExploration(TRSPSolution solution, IParameters params,
            boolean first) {
        RelocateMove bestMve = null;

        for (TRSPTour src : solution) {
            for (Integer r : src) {
                if (solution.getInstance().isDepot(r))
                    continue;

                RemoveMove rem = new RemoveMove(r, src);

                src.getCostDelegate().evaluateMove(rem);

                for (TRSPTour dest : solution) {
                    if (dest == src)
                        continue;
                    InsertionMove ins = InsertionMove.findInsertion(r, dest,
                            dest.getCostDelegate(), mTourCtrHandler, true, true);
                    RelocateMove mve = new RelocateMove(rem, ins);
                    if (mve.isImproving() && getConstraintHandler().isFeasible(solution, mve)) {
                        if (first)
                            return mve;
                        else if (bestMve == null || mve.compareTo(bestMve) > 0)
                            bestMve = mve;
                    }
                }
            }
        }

        return bestMve;
    }

    @Override
    public String getShortName() {
        return "reloc";
    }

    /**
     * The class <code>RelocateMove</code> represents a reassign move.
     * <p>
     * Creation date: Feb 8, 2012 - 3:37:02 PM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    public static class RelocateMove extends Move {

        private final RemoveMove    mRemove;
        private final InsertionMove mInsertion;

        /**
         * Getter for <code>remove</code>
         * 
         * @return the remove
         */
        public RemoveMove getRemove() {
            return mRemove;
        }

        /**
         * Getter for <code>insertion</code>
         * 
         * @return the insertion
         */
        public InsertionMove getInsertion() {
            return mInsertion;
        }

        /**
         * Creates a new <code>RelocateMove</code>
         * 
         * @param remove
         * @param insertion
         */
        public RelocateMove(RemoveMove remove, InsertionMove insertion) {
            mRemove = remove;
            mInsertion = insertion;
            setImprovement(mRemove.getImprovement() + mInsertion.getImprovement());
        }

        @Override
        public String getMoveName() {
            return "reassign";
        }

        @Override
        public String toString() {
            return String.format("rel(%s:%s->%s@%s,%.3f)", mRemove.getTour().getTechnicianId(),
                    mRemove.getNodeId(), mInsertion.getTour().getTechnicianId(),
                    mInsertion.getInsertionPred(), getImprovement());
        }

    }
}
