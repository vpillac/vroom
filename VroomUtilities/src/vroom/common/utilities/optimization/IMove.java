package vroom.common.utilities.optimization;

import vroom.common.utilities.Constants;

/**
 * <code>IMove</code> is a general description for <em>moves</em> as used in neighborhood exploration.
 * <p>
 * It extends {@link Comparable} and implementations should ensure that {@link #compareTo(IMove)} returns a positive
 * value if this move will generate a greater improvement.
 * </p>
 * <p>
 * Creation date: Jun 18, 2010 - 1:28:36 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public interface IMove extends Comparable<IMove> {

    /**
     * Getter for the objective function delta.
     * <p>
     * Positive values represent improvements, negative values deterioration
     * </p>
     * 
     * @return the variation in the objective function resulting from this move
     * @see #isImproving()
     */
    public double getImprovement();

    /**
     * Setter for the objective function delta.
     * <p>
     * Positive values represent improvements, negative values deterioration
     * </p>
     * 
     * @return the variation in the objective function resulting from this move
     * @see #isImproving()
     */
    public void setImprovement(double imp);

    /**
     * Test if this move is improving
     * 
     * @return <code>true</code> if the improvement resulting from this move is higher that the zero tolerance defined
     *         in {@link Constants#getZeroTolerance()}
     */
    public boolean isImproving();

    /**
     * Move description
     * 
     * @return a name for this move
     */
    public String getMoveName();

}