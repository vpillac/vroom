package vroom.common.heuristics.vls;

import vroom.common.utilities.optimization.IInstance;
import vroom.common.utilities.optimization.ISolution;

/**
 * The Class <code>SimpleAcceptationCriterion</code> implements an {@linkplain IVLSAcceptanceCriterion acceptance
 * criteria} that only accept solutions that improve the best fitness value.
 * <p>
 * Creation date: Apr 26, 2010 - 10:48:41 AM
 * 
 * @author Victor Pillac
 * @version 1.0
 */
public class SimpleAcceptanceCriterion implements IVLSAcceptanceCriterion {

    private final boolean mMinimization;

    /**
     * Instantiates a new simple acceptance criteria.
     */
    public SimpleAcceptanceCriterion(VLSGlobalParameters params) {
        mMinimization = params.get(VLSGlobalParameters.OPTIMIZATION_DIRECTION) < 0;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public boolean acceptSolution(IVLSState<?> state, IInstance instance, ISolution solution) {
        ISolution bestSol = state.getBestSolution(state.getCurrentPhase());

        if (bestSol == null) {
            return true;
        } else if (solution == null) {
            return false;
        } else {
            double comp = ((Comparable) bestSol.getObjective()).compareTo(solution
                    .getObjective());

            return mMinimization ? comp >= 0 : comp <= 0;
        }
    }

    @Override
    public String toString() {
        return "improv";
    }
}// end SimpleAcceptationCriteria