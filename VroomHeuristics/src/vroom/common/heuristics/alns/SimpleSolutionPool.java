/**
 * 
 */
package vroom.common.heuristics.alns;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import umontreal.iro.lecuyer.rng.RandomStream;
import vroom.common.utilities.Utilities;
import vroom.common.utilities.Utilities.Random;
import vroom.common.utilities.optimization.IInstance;
import vroom.common.utilities.optimization.IParameters;
import vroom.common.utilities.optimization.ISolution;
import vroom.common.utilities.optimization.OptimizationSense;
import vroom.common.utilities.optimization.SolutionComparator;

/**
 * <code>SimpleSolutionPool</code> is an implementation of {@link IPALNSSolutionPool} that maintain a fixed size set of
 * solutions ordered according to their objective value.
 * <p>
 * Creation date: Nov 17, 2011 - 4:10:47 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class SimpleSolutionPool<S extends ISolution> implements IPALNSSolutionPool<S> {

    private final SortedSet<S>      mSolutions;
    private final Set<S>            mSolutionsView;

    private final OptimizationSense mSense;

    private final int               mMaxSize;

    /**
     * Creates a new <code>SimpleSolutionPool</code> based on a {@link TreeSet} and {@link SolutionComparator}
     * 
     * @param sense
     *            the optimization sense
     * @param maxSize
     *            the maximum number of solutions to keep in this pool
     */
    public SimpleSolutionPool(OptimizationSense sense, ALNSGlobalParameters params) {
        this(sense, params.get(ALNSGlobalParameters.PALNS_POOL_SIZE), new TreeSet<S>(
                new SolutionComparator<S>(sense)));
    }

    /**
     * Creates a new <code>SimpleSolutionPool</code>
     * 
     * @param sense
     *            the optimization sense
     * @param maxSize
     *            the maximum number of solutions to keep in this pool
     * @param solutions
     *            an initial collection of solutions for this pool
     */
    public SimpleSolutionPool(OptimizationSense sense, Integer maxSize, SortedSet<S> solutions) {
        mSolutions = solutions;
        mSense = sense;
        mMaxSize = maxSize;
        mSolutionsView = Collections.unmodifiableSet(mSolutions);
    }

    @Override
    public void initialize(IInstance instance, S solution, IParameters params) {
    };

    @Override
    public boolean add(S solution, boolean mainIteration) {
        if (size() == mMaxSize) {
            // The pool is full
            S worst = getSolutionsInternal().first();
            if (mSense.isBetter(worst.getObjectiveValue(), solution.getObjectiveValue(), false)) {
                // The solution is better than the worst solution from the pool, add it
                boolean added = getSolutionsInternal().add(solution);
                if (added)
                    // The solution was not present, remove the worst to maintain the pool size
                    getSolutionsInternal().remove(worst);
                return added;
            } else {
                // The solution is worst than the worst solution from the pool, reject it
                return false;
            }
        } else {
            // Pool is not full, add the solution to it
            return getSolutionsInternal().add(solution);
        }
    }

    @Override
    public void addAll(Collection<S> solutions, boolean mainIteration) {
        for (S s : solutions)
            add(s, mainIteration);
    }

    @Override
    public Collection<S> subset(int size, RandomStream rndStream) {
        return Random.sample(getSolutions(), size, rndStream);
    }

    @Override
    public final Iterator<S> iterator() {
        return getSolutions().iterator();
    }

    @Override
    public final Set<S> getSolutions() {
        return mSolutionsView;
    }

    /**
     * Return the actual set containing the solutions (not the view returned by {@link #getSolutions()})
     * 
     * @return the set containing the solutions
     * @see #getSolutions()
     */
    protected SortedSet<S> getSolutionsInternal() {
        return mSolutions;
    }

    @Override
    public S getBest() {
        return mSolutions.last();
    }

    @Override
    public final int size() {
        return mSolutions.size();
    }

    /**
     * Returns the maximum size of this solution pool
     * 
     * @return the maximum size of this solution pool
     */
    public final int getMaxSize() {
        return mMaxSize;
    }

    /**
     * Returns the optimization sense
     * 
     * @return the optimization sense
     */
    public final OptimizationSense getSense() {
        return mSense;
    }

    @Override
    public final boolean isFull() {
        return size() >= getMaxSize();
    }

    @Override
    public void clear() {
        mSolutions.clear();
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder(size() * 10);
        b.append("[");
        for (S sol : getSolutions()) {
            b.append(Utilities.format(sol.getObjective()));
            b.append(",");
        }
        if (b.length() > 1)
            b.setCharAt(b.length() - 1, ']');
        else
            b.append(']');

        return String.format("size:%s/%s %s", size(), mMaxSize, b);
    }
}
