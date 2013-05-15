/**
 * 
 */
package vroom.trsp.optimization.constraints;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import vroom.common.heuristics.ConstraintHandler;
import vroom.common.utilities.IntegerSet;
import vroom.common.utilities.Utilities;
import vroom.common.utilities.optimization.IConstraint;
import vroom.common.utilities.optimization.IMove;
import vroom.trsp.datamodel.ITRSPTour;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.datamodel.TRSPTour;

/**
 * <code>TourConstraintHandler</code> is an extension of {@link ConstraintHandler} that takes into account the
 * specificities of {@link ITourConstraint}.
 * <p>
 * Creation date: Apr 7, 2011 - 5:07:44 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TourConstraintHandler extends ConstraintHandler<ITRSPTour> implements ITourConstraint {

    /**
     * Creates a new <code>TourConstraintHandler</code> with the given constraints.
     * <p>
     * Please note that constraints will be checked in the same order they were added
     * </p>
     * 
     * @param ctr
     *            a set of constraints to add to the new handler
     */
    public TourConstraintHandler(ITourConstraint... ctr) {
        super(ctr);
    }

    /**
     * Creates a new constraint handler for the specified instance.
     * 
     * @param instance
     * @return a constraint handler containing the constraints that apply to {@code  instance}
     */
    public static TourConstraintHandler newConstraintHandler(TRSPInstance instance) {
        ArrayList<ITourConstraint> ctr = new ArrayList<>();
        if (instance.isDynamic())
            ctr.add(new ServicedRequestsConstraint());
        if (!instance.isCVRPTW()) {
            ctr.add(new HomeConstraint());
            if (instance.getSkillCount() > 0)
                ctr.add(new SkillsConstraint());
            ctr.add(new TWConstraint());
            if (instance.getToolCount() > 0)
                ctr.add(new ToolsConstraint());
        } else {
            ctr.add(new TWConstraint());
        }
        if (instance.getMaxTourDuration() < Double.POSITIVE_INFINITY)
            ctr.add(new MaxDurationConstraint(instance.getMaxTourDuration()));

        if (instance.getSpareCount() > 0)
            ctr.add(new SparePartsConstraint());

        return new TourConstraintHandler(ctr.toArray(new ITourConstraint[ctr.size()]));
    }

    /**
     * Check the feasibility of a solution
     * 
     * @param solution
     *            the solution to be checked
     * @param allowUnserved
     *            {@code true} if unserved requests are allowed, {@code false} if a solution with unserved requests is
     *            considered as infeasible
     * @return {@code true} if the {@code  solution} is feasible, {@code false} otherwise
     */
    public boolean isFeasible(TRSPSolution solution, boolean allowUnserved) {
        if (!allowUnserved && solution.getUnservedCount() != 0)
            return false;
        for (TRSPTour trspTour : solution) {
            if (!isFeasible(trspTour))
                return false;
        }
        return true;
    }

    @Override
    public int firstInfeasibleNode(ITRSPTour tour) {
        IntegerSet infeasibleNodes = new IntegerSet(tour.getSolution().getInstance().getMaxId());
        for (IConstraint<ITRSPTour> c : this) {
            if (c instanceof ITourConstraint) {
                int inf = ((ITourConstraint) c).firstInfeasibleNode(tour);
                if (inf != ITRSPTour.UNDEFINED)
                    infeasibleNodes.add(inf);
            }
        }

        if (infeasibleNodes.size() == 0)
            return ITRSPTour.UNDEFINED;
        else if (infeasibleNodes.size() == 1)
            return infeasibleNodes.iterator().next();
        else {
            for (Integer i : tour)
                if (infeasibleNodes.contains(i))
                    return i;
            return ITRSPTour.UNDEFINED;
        }

    }

    @Override
    public int checkFeasibility(ITRSPTour tour, IMove move) {
        int state = 3;
        for (IConstraint<ITRSPTour> c : this) {
            if (c instanceof ITourConstraint) {
                int feasibility = ((ITourConstraint) c).checkFeasibility(tour, move);
                state &= feasibility;
                if (state == 0)
                    return state;
            }
        }
        return state;
    }

    /**
     * Check the feasibility of a move for a particular tour.
     * <p>
     * If <code>previousState</code> is <code>null</code>, this method will stop constraint checking as soon as the
     * infeasibility of the move is proven, if not it will stop when the move is proven to be both infeasible and
     * forward infeasible.
     * 
     * @param tour
     *            the tour at hand
     * @param move
     *            the move to be checked
     * @param previousState
     *            the state returned by a previous call to this method, or <code>null</code>
     * @return the feasibility state of the move.
     * @see ITourConstraint#checkFeasibility(ITRSPTour, IMove)
     */
    public FeasibilityState checkFeasibility(ITRSPTour tour, IMove move,
            FeasibilityState previousState) {
        FeasibilityState feasState = null;
        if (previousState == null) {
            // First pass: check all constraints until the infeasibility of the move is detected
            feasState = new FeasibilityState(size());
            for (IConstraint<ITRSPTour> c : this) {
                if (feasState.isFeasible()) {
                    if (c instanceof ITourConstraint) {
                        feasState.updateState(((ITourConstraint) c).checkFeasibility(tour, move));
                    } else {
                        feasState.updateFeasible(c.isFeasible(tour, move));
                    }
                } else {
                    feasState.addUncheckedConstraint(c);
                }
            }
        } else {
            // Second pass: check all remaining constraints
            feasState = previousState;
            Iterator<IConstraint<ITRSPTour>> it = feasState.getUncheckedConstraints().iterator();
            while (feasState.continueCtrCheck() && it.hasNext()) {
                IConstraint<ITRSPTour> c = it.next();
                if (c instanceof ITourConstraint) {
                    feasState.updateState(((ITourConstraint) c).checkFeasibility(tour, move));
                } else {
                    feasState.updateFeasible(c.isFeasible(tour, move));
                }
                it.remove();
            }
        }
        return feasState;
    }

    public static final class FeasibilityState {
        private boolean                            mFeasible;
        private boolean                            mForwardFeasible;
        private final List<IConstraint<ITRSPTour>> mUncheckedConstraints;

        /**
         * Returns <code>true</code> if the move is feasible, <code>false</code> otherwise
         * 
         * @return <code>true</code> if the move is feasible, <code>false</code> otherwise
         */
        public boolean isFeasible() {
            return mFeasible;
        }

        /**
         * Update this state
         * 
         * @param stateUpdate
         */
        private void updateState(int stateUpdate) {
            updateFeasible(stateUpdate % 2 == 1);
            updateForwardFeasible(stateUpdate >= 2);
        }

        /**
         * Returns <code>true</code> if the move is forward feasible, <code>false</code> otherwise
         * 
         * @return <code>true</code> if the move is forward feasible, <code>false</code> otherwise
         * @see ITourConstraint#checkFeasibility(ITRSPTour, IMove)
         */
        public boolean isForwardFeasible() {
            return mForwardFeasible;
        }

        /**
         * Update the feasible flag
         * 
         * @param feasible
         */
        private void updateFeasible(boolean feasible) {
            mFeasible &= feasible;
        }

        /**
         * Update the forward feasible flag
         * 
         * @param feasible
         */
        private void updateForwardFeasible(boolean forwardFeasible) {
            mForwardFeasible &= forwardFeasible;
        }

        /**
         * Return <code>true</code> if the constraint checking process should continue (no definitive result),
         * <code>false</code> otherwise
         * 
         * @return <code>true</code> if the constraint checking process should continue (no definitive result),
         *         <code>false</code> otherwise
         */
        private boolean continueCtrCheck() {
            return mFeasible || mForwardFeasible;
        }

        /**
         * Add a constraint to the set of unchecked constraints
         * 
         * @param ctr
         */
        private void addUncheckedConstraint(IConstraint<ITRSPTour> ctr) {
            mUncheckedConstraints.add(ctr);
        }

        /**
         * Returns the list of unchecked constraints
         * 
         * @return the list of unchecked constraints
         */
        private List<IConstraint<ITRSPTour>> getUncheckedConstraints() {
            return mUncheckedConstraints;
        }

        /**
         * Creates a new <code>FeasibilityState</code>
         */
        public FeasibilityState(int ctrCount) {
            mFeasible = true;
            mForwardFeasible = true;
            mUncheckedConstraints = new ArrayList<IConstraint<ITRSPTour>>(ctrCount);
        }

        @Override
        public String toString() {
            return String.format("feas:%s,fwdFeas:%s,unchecked:%s", isFeasible(),
                    isForwardFeasible(), Utilities.toShortString(getUncheckedConstraints()));
        }
    }

}
