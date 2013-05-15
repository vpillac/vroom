/**
 *
 */
package vroom.common.heuristics;

import java.util.LinkedList;

import umontreal.iro.lecuyer.rng.RandomPermutation;
import vroom.common.heuristics.utils.HeuristicsLogging;
import vroom.common.modeling.dataModel.IVRPSolution;
import vroom.common.modeling.util.SolutionChecker;
import vroom.common.utilities.optimization.IAcceptanceCriterion;
import vroom.common.utilities.optimization.IInstance;
import vroom.common.utilities.optimization.IMove;
import vroom.common.utilities.optimization.IParameters;
import vroom.common.utilities.optimization.ISolution;

/**
 * <code>GenericNeighborhood</code> is a generic implementation that provide a frame for neighborhood exploration.
 * <p>
 * It is useful for rapid prototyping.
 * </p>
 * <p>
 * Creation date: Jun 22, 2010 - 9:21:21 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public abstract class GenericNeighborhood<S extends ISolution, M extends Move> extends
        NeighborhoodBase<S, M> {

    /**
     * Creates a new <code>GenericNeighborhood</code>
     * 
     * @param constraintHandler
     *            the constraint handler to be used in this neighborhood
     */
    public GenericNeighborhood(ConstraintHandler<S> constraintHandler) {
        super(constraintHandler);
    }

    /**
     * Creates a new <code>GenericNeighborhood</code> with a default constraint handler
     */
    public GenericNeighborhood() {
        super();
    }

    /**
     * A generic exploration procedure that enumerates all possible moves and then select one depending on the given
     * parameters
     * 
     * @param solution
     * @param params
     * @return a move
     */
    private final M genericExploration(S solution, IParameters params) {
        M move = null;
        // Create a list of candidate moves
        LinkedList<M> candidates = generateCandidateList(solution, params);

        if (params.randomize() && (params.acceptFirstImprovement() || params.acceptNonImproving())) {
            // Shuffle candidate list
            RandomPermutation.shuffle(candidates, params.getRandomStream());
        }

        IAcceptanceCriterion acceptance = getAcceptanceCriterion(params);

        while (!candidates.isEmpty()) {
            // Pop the next candidate
            M cand = candidates.pop();

            // Evaluate the move
            cand.setImprovement(evaluateCandidateMove(cand));

            // Improving move or first improving move
            if ((params.acceptNonImproving() || acceptance.accept(solution, this, cand)
                    && (getConstraintHandler().isFeasible(solution, cand)))
                    && (move == null || (move != null && cand.getImprovement() > move
                            .getImprovement()))) {
                move = cand;

                if (params.acceptFirstImprovement() || params.acceptNonImproving()) {
                    break;
                }
            }
        }

        return move;
    }

    @Override
    protected M randomFirstImprovement(S solution, IParameters params) {
        return genericExploration(solution, params);
    };

    @Override
    protected M deterministicFirstImprovement(S solution, IParameters params) {
        return genericExploration(solution, params);
    };

    @Override
    public M deterministicBestImprovement(S solution, IParameters params) {
        return genericExploration(solution, params);
    };

    /**
     * A specialized implementation of {@link #exploreNeighborhood(ISolution, IParameters)} for random non-improving
     * exploration.
     * 
     * @param mSolution
     *            mSolution the mSolution which neighborhood has to be explored
     * @param params
     *            optional parameters for the neighborhood exploration
     * @return a random move in this neighborhood, not necesarily improving
     * @see #exploreNeighborhood(ISolution, IParameters)
     */
    @Override
    public M randomNonImproving(S solution, IParameters params) {
        // Create a list of candidate moves
        LinkedList<M> candidates = generateCandidateList(solution, params);

        if (params.randomize() && (params.acceptFirstImprovement() || params.acceptNonImproving())) {
            // Shuffle candidate list
            RandomPermutation.shuffle(candidates, params.getRandomStream());
        }

        while (!candidates.isEmpty()) {
            // Pop the next candidate
            M cand = candidates.pop();

            // Improving move or first improving move
            if (getConstraintHandler().isFeasible(solution, cand)) {
                return cand;
            }

            // Evaluate the move
            cand.setImprovement(evaluateCandidateMove(cand));
        }

        return null;
    }

    /**
     * Generate a list of candidate moves.
     * <p>
     * Implementations should not evaluate moves nor check their feasibility as it is the responsibility of the
     * {@link #exploreNeighborhood(ISolution, IParameters)} method
     * </p>
     * 
     * @return a list containing candidate moves
     */
    protected abstract LinkedList<M> generateCandidateList(S solution, IParameters params);

    /**
     * Evaluate a candidate move by calculating the associated improvement
     * 
     * @param cand
     *            a candidate move
     * @return the improvement associated with the given candidate move
     */
    protected abstract double evaluateCandidateMove(M cand);

    @Override
    public final boolean executeMove(S solution, IMove move) {
        String prevSol = isCheckSolutionAfterMove() ? solution.toString() : null;
        boolean prev = checkSolution(solution, move, true, true, prevSol);
        boolean b = executeMoveImplem(solution, move);
        return b && checkSolution(solution, move, false, prev, prevSol);
    };

    /**
     * Implementation of {@link #executeMove(ISolution, Move)}
     * 
     * @param solution
     * @param move
     * @return
     */
    protected abstract boolean executeMoveImplem(S solution, IMove move);

    @Override
    public void pertub(IInstance instance, S solution, IParameters parameters) {
        M move = randomNonImproving(solution, parameters);
        if (move != null) {
            executeMove(solution, move);
        }
    };

    /**
     * Check a solution for feasibility
     * 
     * @param solution
     * @param move
     * @param before
     *            <code>true</code> if this is a pre-check
     * @param prevState
     * @param prevSol
     * @return
     */
    @Override
    protected boolean checkSolution(S solution, IMove move, boolean before, boolean prevState,
            String prevSol) {
        if (isCheckSolutionAfterMove() && prevState) {
            String errChck = SolutionChecker.checkSolution((IVRPSolution<?>) solution, true, true,
                    true);

            String errCtr = getConstraintHandler().getInfeasibilityExplanation(solution);

            String err = errChck != null ? errCtr != null ? errChck + " ; " + errCtr : errChck
                    : errCtr;

            if (err != null && prevState) {
                if (!before) {
                    HeuristicsLogging
                            .getNeighborhoodsLogger()
                            .debug("%s: solution is infeasible/inconsistent %s executing move %s (err: %s, sol:%s)",
                                    this.getClass().getSimpleName(), before ? "before" : "after",
                                    move, err, before ? solution : prevSol);
                }
                return false;
            } else {
                return true;
            }
        }
        return prevState;
    }

    @Override
    public void dispose() {
        // Do nothing
    }
}
