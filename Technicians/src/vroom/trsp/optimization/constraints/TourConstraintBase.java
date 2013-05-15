/**
 * 
 */
package vroom.trsp.optimization.constraints;

import vroom.common.utilities.optimization.IMove;
import vroom.trsp.datamodel.ITRSPTour;
import vroom.trsp.optimization.InsertionMove;
import vroom.trsp.optimization.localSearch.TRSPShift.TRSPShiftMove;
import vroom.trsp.optimization.localSearch.TRSPTwoOpt.TRSPTwoOptMove;

/**
 * The class <code>TourConstraintBase</code> contains common logic for implementations of the {@link ITourConstraint}
 * interface.
 * <p>
 * Creation date: Sep 27, 2011 - 1:39:45 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public abstract class TourConstraintBase implements ITourConstraint {

    /*
     * (non-Javadoc)
     * @see vroom.common.utilities.optimization.IConstraint#isFeasible(java.lang.Object)
     */
    @Override
    public boolean isFeasible(ITRSPTour tour) {
        return checkFeasibility(tour).isFeasible();
    }

    @Override
    public int firstInfeasibleNode(ITRSPTour tour) {
        return checkFeasibility(tour).getInfeasibleNode();
    }

    @Override
    public final String getInfeasibilityExplanation(ITRSPTour tour) {
        return checkFeasibility(tour).getInfeasibility();
    }

    /**
     * Checks the feasibility of a tour
     * 
     * @param tour
     *            the tour to be checked
     * @return an object describing the feasibility of {@code  tour}
     */
    protected abstract FeasibilityState checkFeasibility(ITRSPTour tour);

    @Override
    public boolean isFeasible(ITRSPTour tour, IMove move) {
        // We assume that the tour data is updated and coherent
        if (move instanceof InsertionMove) {
            return isInsFeasible(tour, (InsertionMove) move);
        } else if (move instanceof TRSPShiftMove) {
            return isShiftFeasible(tour, (TRSPShiftMove) move);
        } else if (move instanceof TRSPTwoOptMove) {
            return isTwoOptFeasible(tour, (TRSPTwoOptMove) move);
        } else {
            throw new UnsupportedOperationException(String.format("Unsupported move: %s", move));
        }
    }

    /*
     * (non-Javadoc)
     * @see vroom.common.utilities.optimization.IConstraint#getInfeasibilityExplanation(java.lang.Object,
     * vroom.common.utilities.optimization.IMove)
     */
    @Override
    public String getInfeasibilityExplanation(ITRSPTour solution, IMove move) {
        if (!isFeasible(solution, move))
            return this.getClass().getSimpleName() + ":infeasible";
        else
            return this.getClass().getSimpleName() + ":feasible";
    }

    protected abstract boolean isTwoOptFeasible(ITRSPTour tour, TRSPTwoOptMove move);

    protected abstract boolean isShiftFeasible(ITRSPTour tour, TRSPShiftMove move);

    protected abstract boolean isInsFeasible(ITRSPTour tour, InsertionMove move);

    @Override
    public abstract int checkFeasibility(ITRSPTour tour, IMove move);

    /**
     * The class <code>FeasibilityState</code> is used to reduce overhead when checking the feasibility of a solution or
     * move.
     * <p>
     * Creation date: Oct 5, 2011 - 1:57:27 PM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    protected static class FeasibilityState {
        private final String   mFormat;
        private final int      mInfeasibleNode;
        private final Object[] mArgs;
        private final boolean  mFeasible;

        /**
         * Creates a new <code>FeasibilityState</code>
         * 
         * @param infeasibleNode
         *            the node at which the tour becomes infeasible (optional)
         * @param format
         *            the format string for the infeasibility explanation, if <code>null</code> the state is assumed to
         *            be feasible
         * @param args
         *            arguments for the format string
         */
        protected FeasibilityState(int infeasibleNode, String format, Object... args) {
            mFormat = format;
            mInfeasibleNode = infeasibleNode;
            mArgs = args;
            mFeasible = format == null;
        }

        /**
         * Creates a new <code>FeasibilityState</code> representing a feasible state
         */
        protected FeasibilityState() {
            this(ITRSPTour.UNDEFINED, null, (Object[]) null);
        }

        /**
         * Returns <code>true</code> if this instance corresponds to a feasible state, <code>false</code> otherwise
         * 
         * @return <code>true</code> if this instance corresponds to a feasible state, <code>false</code> otherwise
         */
        protected boolean isFeasible() {
            return mFeasible;
        }

        /**
         * Returns the infeasibility explanation for this state, or <code>null</code> if the state is feasible
         * 
         * @return the infeasibility explanation for this state, or <code>null</code> if the state is feasible
         */
        protected String getInfeasibility() {
            return mFeasible ? null : String.format(mFormat, mArgs);
        }

        /**
         * Returns the first node at which the tour becomes infeasible
         * 
         * @return the first node at which the tour becomes infeasible
         */
        public int getInfeasibleNode() {
            return mInfeasibleNode;
        }

    }
}
