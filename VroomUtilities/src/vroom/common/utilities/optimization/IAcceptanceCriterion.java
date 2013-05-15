/**
 * 
 */
package vroom.common.utilities.optimization;

import vroom.common.utilities.ICloneable;

/**
 * <code>IAcceptanceCriterion</code> is the interface for classes defining an acceptance criterion, which is used to
 * determine whether a new solution should replace the previous one.
 * <p>
 * Creation date: 11 juil. 2010 - 19:44:30
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public interface IAcceptanceCriterion extends ICloneable<IAcceptanceCriterion> {

    /**
     * Initialize the acceptance criterion
     */
    public void initialize();

    /**
     * Reset this acceptance criterion
     */
    public void reset();

    /**
     * Determine whether a new solution should replace the previous one
     * 
     * @param oldSolution
     *            the previous solution
     * @param newSolution
     *            the new solution
     * @return <code>true</code> if <code>newSolution</code> should be accepted to replace <code>oldSolution</code>
     */
    public boolean accept(ISolution oldSolution, ISolution newSolution);

    /**
     * Determine whether a move should can be considered as improving or not
     * 
     * @param solution
     *            the current solution
     * @param move
     *            the move to be executed on the given solution
     * @return <code>true</code> if the move is considered as improving
     */
    public boolean accept(ISolution solution, IMove move);

    /**
     * Determine whether a move should can be considered as improving or not
     * 
     * @param solution
     *            the current solution
     * @param neighborhood
     *            the considered neighborhood
     * @param move
     *            the move to be executed on the given solution
     * @return <code>true</code> if the move is considered as improving
     */
    public boolean accept(ISolution solution, INeighborhood<?, ?> neighborhood, IMove move);

    /**
     * Returns a decimal value representing the improvement from {@code  oldSolution} to {@code  newSolution}, this value
     * is positive is the solution was improved, and negative if it was deteriorated
     * 
     * @param oldSolution
     * @param newSolution
     * @return a decimal value representing the improvement from {@code  oldSolution} to {@code  newSolution}
     */
    public double getImprovement(ISolution oldSolution, ISolution newSolution);

}
