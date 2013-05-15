/**
 * 
 */
package vroom.common.utilities.optimization;

/**
 * <code>IConstraint</code> is an interface for classes able to check {@linkplain ISolution solutions} or
 * {@linkplain IMove moves}
 * <p>
 * Creation date: Jun 22, 2010 - 9:03:02 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 * @param <S>
 *            the type of {@link ISolution} to which this constraint applies
 */
public interface IConstraint<S> {

    /**
     * Check if a solution is feasible for this constraint
     * 
     * @param solution
     *            the solution to be tested
     * @return <code>true</code> if <code>solution</code> satisfies this constraint
     */
    public boolean isFeasible(S solution);

    /**
     * Check if a {@linkplain IMove move} is feasible for the given <code>solution</code>
     * 
     * @param solution
     *            the base solution
     * @param move
     *            the IMove to be tested
     * @return <code>true</code> if applying <code>move</code> to the given <code>solution</code> will result in a
     *         feasible solution
     */
    public boolean isFeasible(S solution, IMove move);

    /**
     * Check if a solution satisfies this constraint and return an explanation if not
     * 
     * @param solution
     *            the solution to be tested
     * @return a string describing the solution infeasibility, or <code>null</code> if the solution is feasible
     */
    public String getInfeasibilityExplanation(S solution);

    /**
     * Check if a IMove on a given solution satisfies this constraint and return an explanation if not.
     * 
     * @param solution
     *            the solution to be tested
     * @param IMove
     *            the IMove to be tested
     * @return a string describing the IMove infeasibility, or <code>null</code> if the solution is feasible
     */
    public String getInfeasibilityExplanation(S solution, IMove move);
}
