/**
 * 
 */
package vroom.common.heuristics.alns;

import java.util.Collection;

import umontreal.iro.lecuyer.rng.RandomStream;
import vroom.common.utilities.optimization.IInstance;
import vroom.common.utilities.optimization.IParameters;
import vroom.common.utilities.optimization.ISolution;

/**
 * <code>IPALNSSolutionPool</code> is the interface for classes that will handle the solution pool in the
 * {@link ParallelALNS}
 * <p>
 * Creation date: Nov 17, 2011 - 4:15:55 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public interface IPALNSSolutionPool<S extends ISolution> extends Iterable<S> {

    /**
     * Initialize the solution pool
     * 
     * @param instance
     *            the instance at hand
     * @param solution
     *            the initial solution passed to local search
     * @param params
     *            parameters passed to the local search
     * @see ParallelALNS#localSearch(IInstance, ISolution, IParameters)
     */
    public void initialize(IInstance instance, S solution, IParameters params);

    /**
     * Add a solution to this pool
     * 
     * @param solution
     *            the solution to add
     * @param mainIteration
     *            {@code true} if the solution is added at the end of a main iteration
     * @return {@code true} if the solution was added, {@code false} if it was rejected
     */
    public boolean add(S solution, boolean mainIteration);

    /**
     * Add a collection of solutions to this pool
     * 
     * @param solutions
     *            the solutions to add
     * @param mainIteration
     *            TODO
     */
    public void addAll(Collection<S> solutions, boolean mainIteration);

    /**
     * Returns a subset of {@code  size} solutions from this pool
     * 
     * @param size
     *            the desired size of the subset of solutions
     * @param rndStream
     *            a random stream
     * @return a subset of {@code  size} solutions from this pool
     */
    public Collection<S> subset(int size, RandomStream rndStream);

    /**
     * Returns a view of the solutions contained in this pool
     * 
     * @return a view of the solutions contained in this pool
     */
    public Collection<S> getSolutions();

    /**
     * Returns the <em>best</em> solution from this pool according to some criteria
     * 
     * @return the <em>best</em> solution from this pool according to some criteria
     */
    public S getBest();

    /**
     * Returns the number of solutions currently present in this pool
     * 
     * @return the number of solutions currently present in this pool
     */
    public int size();

    /**
     * Remove all the solutions from this pool
     */
    public void clear();

    /**
     * Returns {@code true} if this pool contains its maximum number of solutions
     * 
     * @return {@code true} if this pool contains its maximum number of solutions
     */
    public boolean isFull();

}
