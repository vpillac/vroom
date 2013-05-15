/**
 *
 */
package vroom.common.heuristics;

import vroom.common.heuristics.utils.HeuristicsLogging;
import vroom.common.utilities.optimization.IAcceptanceCriterion;
import vroom.common.utilities.optimization.IInstance;
import vroom.common.utilities.optimization.IMove;
import vroom.common.utilities.optimization.INeighborhood;
import vroom.common.utilities.optimization.IParameters;
import vroom.common.utilities.optimization.ISolution;
import vroom.common.utilities.optimization.ImprovingAcceptanceCriterion;

/**
 * <code>GenericNeighborhood</code> is a base class for implementations of {@link INeighborhood} that contains a set of
 * constraints and a generic implementation of {@link INeighborhood#localSearch(IInstance, ISolution, IParameters)} and
 * {@link INeighborhood#localSearch(ISolution, IParameters)}
 * <p>
 * Creation date: Jun 22, 2010 - 9:21:21 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public abstract class NeighborhoodBase<S extends ISolution, M extends Move> implements
        INeighborhood<S, M> {

    /** A flag to toggle mSolution checking after each move */
    private static boolean sCheckSolutionAfterMove = false;

    /**
     * Setter for <code>checkSolutionAfterMove</code>
     * 
     * @param checkSolutionAfterMove
     *            the checkSolutionAfterMove to set
     */
    public static void setCheckSolutionAfterMove(boolean checkSolutionAfterMove) {
        sCheckSolutionAfterMove = checkSolutionAfterMove;
        if (isCheckSolutionAfterMove()) {
            HeuristicsLogging
                    .getNeighborhoodsLogger()
                    .warn("NeighborhoodBase.CheckSolutionAfterMove is set to true, set to false to increase performance (set in %s)",
                            Thread.currentThread().getStackTrace()[2]);
        }
    }

    /**
     * Getter for <code>checkSolutionAfterMove</code>
     * 
     * @return the checkSolutionAfterMove
     */
    public static boolean isCheckSolutionAfterMove() {
        return sCheckSolutionAfterMove;
    }

    /** A constraint handler for this neighborhood **/
    private final ConstraintHandler<S> mConstraintHandler;

    /**
     * Getter for the constraint handler
     * 
     * @return A constraint handler for this neighborhood
     */
    public ConstraintHandler<S> getConstraintHandler() {
        return this.mConstraintHandler;
    }

    /**
     * Creates a new <code>GenericNeighborhood</code>
     * 
     * @param constraintHandler
     *            the constraint handler to be used in this neighborhood
     */
    public NeighborhoodBase(ConstraintHandler<S> constraintHandler) {
        mConstraintHandler = constraintHandler;
    }

    /**
     * Creates a new <code>GenericNeighborhood</code> with a default constraint handler
     */
    public NeighborhoodBase() {
        this(new ConstraintHandler<S>());
    }

    @Override
    public M exploreNeighborhood(S solution, IParameters params) {
        switch (params.getStrategy()) {
        case DET_FIRST_IMPROVEMENT:
            return deterministicFirstImprovement(solution, params);
        case DET_BEST_IMPROVEMENT:
            return deterministicBestImprovement(solution, params);
        case RND_FIRST_IMPROVEMENT:
            return randomFirstImprovement(solution, params);
        case RND_NON_IMPROVING:
            return randomNonImproving(solution, params);
        default:
            throw new IllegalArgumentException("Unsupported strategy: " + params.getStrategy());
        }
    };

    /**
     * Random exploration of the neighborhood accepting non improving moves
     * 
     * @param solution
     *            the solution which neighborhood will be explored
     * @param params
     *            the local search parameters
     * @return a random move
     */
    protected abstract M randomNonImproving(S solution, IParameters params);

    /**
     * Random exploration of the neighborhood accepting the first improving moves
     * 
     * @param solution
     *            the solution which neighborhood will be explored
     * @param params
     *            the local search parameters
     * @return a random improving move
     */
    protected abstract M randomFirstImprovement(S solution, IParameters params);

    /**
     * Deterministic exploration of the neighborhood accepting the best moves
     * 
     * @param solution
     *            the solution which neighborhood will be explored
     * @param params
     *            the local search parameters
     * @return the most improving move
     */
    protected abstract M deterministicBestImprovement(S solution, IParameters params);

    /**
     * Deterministic exploration of the neighborhood accepting the first improving moves
     * 
     * @param solution
     *            the solution which neighborhood will be explored
     * @param params
     *            the local search parameters
     * @return an improving move
     */
    protected abstract M deterministicFirstImprovement(S solution, IParameters params);

    @Override
    public boolean localSearch(S solution, IParameters params) {
        IAcceptanceCriterion acceptance = getAcceptanceCriterion(params);

        // if (params.acceptFirstImprovement()) {
        // // First improvement
        // M move = exploreNeighborhood(solution, params);
        // if (move != null && acceptance.accept(solution, this, move)) {
        // // Execute first improving move
        // executeMove(solution, move);
        // return true;
        // } else {
        // return false;
        // }
        // } else {
        // Thorough exploration
        boolean changed = false;
        M move = exploreNeighborhood(solution, params);
        while (move != null && acceptance.accept(solution, this, move)) {
            // Execute the best move
            executeMove(solution, move);
            changed = true;
            move = exploreNeighborhood(solution, params);
        }
        return changed;
        // }
    }

    @Override
    public S localSearch(IInstance instance, S solution, IParameters param) {
        @SuppressWarnings("unchecked")
        S clone = (S) solution.clone();
        localSearch(clone, param);
        return clone;
    };

    /**
     * Return the currently defined acceptance criterion
     * 
     * @param params
     *            optional parameters
     * @return the {@link IAcceptanceCriterion} defined in <code>params</code> if any, or an instance of
     *         {@link ImprovingAcceptanceCriterion}
     */
    protected IAcceptanceCriterion getAcceptanceCriterion(IParameters params) {
        return params != null && params.getAcceptanceCriterion() != null ? params
                .getAcceptanceCriterion() : new ImprovingAcceptanceCriterion(null);
    }

    /**
     * Check a solution for feasibility
     * 
     * @param solution
     * @param move
     * @param before
     *            <code>true</code> if this is a pre-check
     * @param prevState
     *            the previous solution state, i.e., {@code true} if the value returned by the pre-check
     * @param prevSol
     *            the solution as it was before the move was executed
     * @return {@code true} if the solution is feasible, false otherwise
     */
    protected boolean checkSolution(S solution, IMove move, boolean before, boolean prevState,
            String prevSol) {
        if (isCheckSolutionAfterMove() && prevState) {
            String errCtr = getConstraintHandler().getInfeasibilityExplanation(solution);

            if (errCtr != null && prevState) {
                if (!before) {
                    HeuristicsLogging
                            .getNeighborhoodsLogger()
                            .warn("%s: solution is infeasible/inconsistent %s executing move %s (err: %s, sol:%s)",
                                    this.getClass().getSimpleName(), before ? "before" : "after",
                                    move, errCtr, before ? solution : prevSol);
                }
                return false;
            } else {
                return true;
            }
        }
        return prevState;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void dispose() {
        // Do nothing
    }

}
