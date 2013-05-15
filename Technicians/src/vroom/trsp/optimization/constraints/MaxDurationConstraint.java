package vroom.trsp.optimization.constraints;

import vroom.common.utilities.Constants;
import vroom.common.utilities.optimization.IMove;
import vroom.trsp.datamodel.ITRSPTour;
import vroom.trsp.datamodel.TRSPTour;
import vroom.trsp.datamodel.costDelegates.TRSPWorkingTime;
import vroom.trsp.optimization.InsertionMove;
import vroom.trsp.optimization.localSearch.TRSPShift.TRSPShiftMove;
import vroom.trsp.optimization.localSearch.TRSPTwoOpt.TRSPTwoOptMove;

/**
 * The class <code>MaxDurationConstraint</code> implements a constraint enforcing a maximum duration for a tour.
 * <p>
 * Creation date: Mar 22, 2012 - 2:17:46 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class MaxDurationConstraint extends TourConstraintBase {

    private final double          mMaxDuration;

    private final TRSPWorkingTime mWTDelegate;

    /**
     * Creates a new <code>MaxDurationConstraint</code>
     * 
     * @param maxDuration
     */
    public MaxDurationConstraint(double maxDuration) {
        mMaxDuration = maxDuration;
        mWTDelegate = new TRSPWorkingTime();
    }

    @Override
    public int firstInfeasibleNode(ITRSPTour tour) {
        if (TRSPTour.class.isAssignableFrom(tour.getClass())) {
            TRSPTour t = (TRSPTour) tour;
            for (int i : t) {
                if (t.getEarliestDepartureTime(i) > mMaxDuration)
                    return i;
            }
            return ITRSPTour.UNDEFINED;
        } else {
            return isFeasible(tour) ? ITRSPTour.UNDEFINED : tour.getFirstNode();
        }
    }

    @Override
    protected FeasibilityState checkFeasibility(ITRSPTour tour) {
        double excess = mMaxDuration - mWTDelegate.evaluateTour(tour, false);

        if (Constants.isStrictlyPositive(excess))
            return new FeasibilityState(tour.getFirstNode(), "Maximum duration exceeded by %.3f",
                    excess);
        else
            return new FeasibilityState();
    }

    @Override
    protected boolean isTwoOptFeasible(ITRSPTour tour, TRSPTwoOptMove move) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean isShiftFeasible(ITRSPTour tour, TRSPShiftMove move) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean isInsFeasible(ITRSPTour tour, InsertionMove move) {
        double delta = mWTDelegate.evaluateInsMove(move);
        double duration = mWTDelegate.evaluateTour(tour, false);
        return Constants.isLowerThan(duration - delta, mMaxDuration);
    }

    @Override
    public int checkFeasibility(ITRSPTour tour, IMove move) {
        return isFeasible(tour, move) ? 3 : 2;
    }

}
