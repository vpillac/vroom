package vroom.common.utilities.optimization;

import vroom.common.utilities.ILockable;

/**
 * <code>ISolution</code> is an interface for classes representing a solution of a problem.
 * <p>
 * Creation date: 26-Abr-2010 10:11:55 a.m. * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los
 * Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 * href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * 
 * @version 1.0
 */
public interface ISolution extends ILockable, Cloneable {

    /**
     * Getter for the fitness value of this solution.
     * 
     * @return the fitness value
     */
    public Comparable<?> getObjective();

    /**
     * Getter for the objective value of this solution.
     * 
     * @return a <code>double</code> representing the objective value of this solution
     */
    public double getObjectiveValue();

    /**
     * Cloning of this solution
     * <p/>
     * The resulting clone should not share any reference with this instance.
     * 
     * @return a hard copy of this instance
     */
    public ISolution clone();

    /**
     * Hash this solution into an integer.
     * <p>
     * At the difference of {@link #hashCode()} this method should return a different int whenever this solution is
     * modified
     * </p>
     * 
     * @return an hash for the current state of this solution
     */
    public int hashSolution();
}