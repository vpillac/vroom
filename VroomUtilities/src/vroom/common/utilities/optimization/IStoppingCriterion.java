/**
 * 
 */
package vroom.common.utilities.optimization;

import vroom.common.utilities.ICloneable;

/**
 * <code>IStoppingCriterion</code> is the interface for classes that act as stopping conditions in an algorithm.
 * <p>
 * Creation date: 1 mai 2010 - 16:24:30
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public interface IStoppingCriterion extends ICloneable<IStoppingCriterion> {

    /**
     * Stopping condition.
     * <p>
     * Please note that in a multithreading context this method should return <code>true</code> if
     * <code>Thread.currentThread().isInterrupted()</code> returns <code>true</code>
     * </p>
     * 
     * @return <code>true</code> if the modeled stopping condition is met and the algorithm should be stopped
     * @see Thread#isInterrupted();
     */
    public boolean isStopCriterionMet();

    /**
     * Returns the current number of iterations
     * 
     * @return the current number of iterations
     */
    public int getIterationCount();

    /**
     * Returns the number of iterations after which the {@link #isStopCriterionMet()} will return {@code true}
     * 
     * @return the maximum number of iterations
     */
    public int getMaxIterations();

    /**
     * Returns the current elapsed time (in ms)
     * 
     * @return the current elapsed time (in ms)
     */
    public double getCurrentTime();

    /**
     * Returns the time (in ms) after which the {@link #isStopCriterionMet()} will return {@code true}
     * 
     * @return the maximum time
     */
    public long getMaxTime();

    /**
     * Update the stopping condition
     * 
     * @param iterations
     *            the number of iterations that were executed
     * @param args
     *            optional arguments
     */
    public void update(int iterations, Object... args);

    /**
     * Update the stopping condition
     * 
     * @param args
     *            optional arguments
     */
    public void update(Object... args);

    /**
     * Reset this stopping condition
     */
    public void reset();

    /**
     * Initialize the stopping condition
     */
    public void init();
}
