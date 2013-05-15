/**
 * 
 */
package vroom.common.utilities.optimization;

/**
 * <code>ImprovingAcceptanceCriterion</code> is an implementation of {@link IAcceptanceCriterion} that only accepts
 * solution improving the objective function
 * <p>
 * Creation date: 11 juil. 2010 - 19:54:07
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class ImprovingAcceptanceCriterion implements IAcceptanceCriterion {

    /** optimization sense **/
    private final OptimizationSense mOptimizationSense;

    /**
     * Getter for optimization sense
     * 
     * @return the optimization sense
     */
    public OptimizationSense getOptimizationSense() {
        return mOptimizationSense;
    }

    private boolean mAcceptTie = false;

    /**
     * Accept solutions with the same objective function (default value is <code>false</code>)
     * 
     * @param acceptTie
     */
    public void setAcceptTie(boolean acceptTie) {
        mAcceptTie = acceptTie;
    }

    public ImprovingAcceptanceCriterion(OptimizationSense optimizationSense) {
        super();
        // if (optimizationSense == null)
        // throw new IllegalArgumentException("Argument optimizationSense cannot be null");
        mOptimizationSense = optimizationSense;
    }

    /*
     * (non-Javadoc)
     * @see
     * vroom.common.utilities.optimization.IAcceptanceCriterion#accept(vroom.common.utilities.optimization.ISolution,
     * vroom.common.utilities.optimization.ISolution)
     */
    @Override
    public boolean accept(ISolution oldSolution, ISolution newSolution) {
        return mOptimizationSense
                .isBetter(oldSolution.getObjectiveValue(), newSolution.getObjectiveValue(), mAcceptTie);
    }

    @Override
    public void initialize() {
    }

    @Override
    public void reset() {
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    @Override
    public boolean accept(ISolution solution, IMove move) {
        return accept(solution, null, move);
    }

    @Override
    public boolean accept(ISolution solution, INeighborhood<?, ?> neighborhood, IMove move) {
        return move.isImproving();
    }

    @Override
    public double getImprovement(ISolution oldSolution, ISolution newSolution) {
        return mOptimizationSense.getImprovement(oldSolution.getObjectiveValue(), newSolution.getObjectiveValue());
    }

    @Override
    public ImprovingAcceptanceCriterion clone() {
        ImprovingAcceptanceCriterion clone = new ImprovingAcceptanceCriterion(getOptimizationSense());
        clone.setAcceptTie(mAcceptTie);
        return clone;
    }
}
