/**
 * 
 */
package vroom..optimization.biobj;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import umontreal.iro.lecuyer.rng.RandomStream;
import vroom.common.heuristics.alns.IPALNSSolutionPool;
import vroom.common.utilities.ILockable;
import vroom.common.utilities.Utilities;
import vroom.common.utilities.Utilities.Random;
import vroom.common.utilities.optimization.OptimizationSense;
import vroom..datamodel.Solution;
import vroom..datamodel.costDelegates.CostDelegate;

/**
 * <code>ParetoFront</code> is an implementation of {@link IPALNSSolutionPool} that maintain a Pareto front for
 * {@link Solution solutions} according to 2 objectives.
 * <p>
 * Creation date: Nov 23, 2011 - 1:55:52 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class ParetoFrontOld implements IPALNSSolutionPool<Solution>, vroom.common.utilities.Cloneable<ParetoFrontOld>,
        Cloneable, ILockable {

    private final HashSet<ParetoSolution> mAllSolutions;
    private final TreeSet<ParetoSolution> mFirstObjSol;
    private final TreeSet<ParetoSolution> mSecondObjSol;

    private final OptimizationSense       mFirstObjSense;
    private final OptimizationSense       mSecondObjSense;

    private final CostDelegate        mFirstCostDelegate;
    private final CostDelegate        mSecondCostDelegate;

    /**
     * Getter for <code>allSolutions</code>
     * 
     * @return the allSolutions
     */
    HashSet<ParetoSolution> getAllSolutions() {
        return mAllSolutions;
    }

    /**
     * Getter for <code>firstObjSol</code>
     * 
     * @return the firstObjSol
     */
    TreeSet<ParetoSolution> getFirstObjSol() {
        return mFirstObjSol;
    }

    /**
     * Getter for <code>secondObjSol</code>
     * 
     * @return the secondObjSol
     */
    TreeSet<ParetoSolution> getSecondObjSol() {
        return mSecondObjSol;
    }

    /**
     * Getter for <code>firstObjSense</code>
     * 
     * @return the firstObjSense
     */
    OptimizationSense getFirstObjSense() {
        return mFirstObjSense;
    }

    /**
     * Getter for <code>secondObjSense</code>
     * 
     * @return the secondObjSense
     */
    OptimizationSense getSecondObjSense() {
        return mSecondObjSense;
    }

    /**
     * Getter for <code>firstCostDelegate</code>
     * 
     * @return the firstCostDelegate
     */
    CostDelegate getFirstCostDelegate() {
        return mFirstCostDelegate;
    }

    /**
     * Getter for <code>secondCostDelegate</code>
     * 
     * @return the secondCostDelegate
     */
    CostDelegate getSecondCostDelegate() {
        return mSecondCostDelegate;
    }

    /**
     * Creates a new <code>ParetoFront</code>
     * 
     * @param firstCostDelegate
     *            the cost delegate for the first objective function
     * @param firstObjSense
     *            the sense of optimization for the first objective function
     * @param secondCostDelegate
     *            the cost delegate for the second objective function
     * @param secondObjSense
     *            the sense of optimization for the second objective function
     */
    public ParetoFrontOld(CostDelegate firstCostDelegate, OptimizationSense firstObjSense,
            CostDelegate secondCostDelegate, OptimizationSense secondObjSense) {
        mFirstCostDelegate = firstCostDelegate;
        mFirstObjSense = firstObjSense;
        mSecondCostDelegate = secondCostDelegate;
        mSecondObjSense = secondObjSense;

        mFirstObjSol = new TreeSet<ParetoSolution>(new ParetoSolutionComparator(true, mFirstObjSense));
        mSecondObjSol = new TreeSet<ParetoSolution>(new ParetoSolutionComparator(false, mSecondObjSense));
        mAllSolutions = new HashSet<ParetoFrontOld.ParetoSolution>();

        mLock = new ReentrantLock();
    }

    /**
     * Creates a new <code>ParetoFront</code> by cloning {@code  original}
     * 
     * @param original
     */
    @SuppressWarnings("unchecked")
    private ParetoFrontOld(ParetoFrontOld original) {
        mFirstCostDelegate = original.mFirstCostDelegate;
        mFirstObjSense = original.mFirstObjSense;
        mSecondCostDelegate = original.mSecondCostDelegate;
        mSecondObjSense = original.mSecondObjSense;

        mFirstObjSol = (TreeSet<ParetoSolution>) original.mFirstObjSol.clone();
        mSecondObjSol = (TreeSet<ParetoSolution>) original.mSecondObjSol.clone();
        mAllSolutions = (HashSet<ParetoSolution>) original.mAllSolutions.clone();

        mLock = new ReentrantLock();
    }

    /**
     * Wraps a {@link Solution solution} in a {@link ParetoSolution pareto solution} and store its cost depending on
     * both objectives
     * 
     * @param sol
     *            the solution wrap
     * @return the wrapped solution
     */
    public ParetoSolution wrapSolution(Solution sol) {
        return new ParetoSolution(sol, mFirstCostDelegate.evaluateSolution(sol, true, false),
                mSecondCostDelegate.evaluateSolution(sol, true, false));
    }

    /**
     * Creates a point in the objective function space wrapped in a {@link ParetoSolution} with no associated
     * {@link Solution}
     * 
     * @param firstObj
     *            the first objective value
     * @param secondObj
     *            the second objective value
     * @return a point in the objective function space
     */
    public ParetoSolution newParetoPoint(double firstObj, double secondObj) {
        return new ParetoSolution(null, firstObj, secondObj);
    }

    @Override
    public boolean add(Solution solution) {
        ParetoSolution sol = wrapSolution(solution);

        if (mAllSolutions.contains(sol))
            return false;

        if (size() == 0) {
            // Add the solution to the front
            addInternal(sol);
            return true;
        }

        SortedSet<ParetoSolution> bestFirstObj = mFirstObjSol.tailSet(sol, true);
        SortedSet<ParetoSolution> bestSecondObj = mSecondObjSol.tailSet(sol, true);

        if (bestFirstObj.isEmpty() || bestSecondObj.isEmpty()
                || (!bestFirstObj.first().dominates(sol) && !bestSecondObj.first().dominates(sol))) {
            // The solution is the best in one of the two objectives
            // Or the closest solutions better on the first/second objective
            // do no strictly dominate the solution

            // Check for strictly dominated solutions
            HashSet<ParetoSolution> dominatedSol = new HashSet<ParetoSolution>(mFirstObjSol.headSet(sol, true));
            dominatedSol.retainAll(mSecondObjSol.headSet(sol, true));

            mFirstObjSol.removeAll(dominatedSol);
            mSecondObjSol.removeAll(dominatedSol);
            mAllSolutions.removeAll(dominatedSol);

            addInternal(sol);
            return true;
        } else {
            // The solution is dominated
            return false;
        }
    }

    void addInternal(ParetoSolution sol) {
        mFirstObjSol.add(sol);
        mSecondObjSol.add(sol);
        mAllSolutions.add(sol);
    }

    @Override
    public void addAll(Collection<Solution> solutions) {
        for (Solution sol : solutions) {
            add(sol);
        }

    }

    @Override
    public Collection<Solution> subset(int size, RandomStream rndStream) {
        ArrayList<ParetoSolution> wrappedSubset = Random.sample(mFirstObjSol, size, rndStream);
        ArrayList<Solution> subset = new ArrayList<Solution>(wrappedSubset.size());
        for (ParetoSolution sol : wrappedSubset) {
            subset.add(sol.getSolution());
        }
        return subset;
    }

    @Override
    public Solution getBest() {
        return mFirstObjSol.last().getSolution();
    }

    @Override
    public int size() {
        return mFirstObjSol.size();
    }

    @Override
    public void clear() {
        mFirstObjSol.clear();
        mSecondObjSol.clear();
    }

    /**
     * Return the solutions in this pareto front in the form of a CSV string
     * 
     * @return the solutions in this pareto front in the form of a CSV string
     */
    public String toCSVString() {
        StringBuilder sb = new StringBuilder(size() * 50);
        sb.append("hash;first_obj;second_obj\n");
        for (ParetoSolution s : mFirstObjSol) {
            sb.append(s.hashCode());
            sb.append(";");
            sb.append(s.getFirstObjValue());
            sb.append(";");
            sb.append(s.getSecondObjValue());
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return Utilities.toShortString(mFirstObjSol);
    }

    /**
     * Returns the solutions in this Pareto front sorted according to the first objective
     * <p>
     * The {@linkplain SortedSet#last() last solution} is the best
     * <p>
     * 
     * @return the solutions in this Pareto front sorted according to the first objective
     */
    public SortedSet<ParetoSolution> getSolutionsFirstObj() {
        return Collections.unmodifiableSortedSet(mFirstObjSol);
    }

    /**
     * Returns the solutions in this Pareto front sorted according to the second objective
     * <p>
     * The {@linkplain SortedSet#last() last solution} is the best
     * <p>
     * 
     * @return the solutions in this Pareto front sorted according to the second objective
     */
    public SortedSet<ParetoSolution> getSolutionsSecondObj() {
        return Collections.unmodifiableSortedSet(mSecondObjSol);
    }

    /**
     * Returns the best solution according to the first objective
     * 
     * @return the best solution according to the first objective
     */
    public ParetoSolution getBestFirstObj() {
        return mFirstObjSol.last();
    }

    /**
     * Returns the best solution according to the second objective
     * 
     * @return the best solution according to the second objective
     */
    public ParetoSolution getBestSecondObj() {
        return mSecondObjSol.last();
    }

    @Override
    public Iterator<Solution> iterator() {
        return getSolutions().iterator();
    }

    @Override
    public List<Solution> getSolutions() {
        ArrayList<Solution> sol = new ArrayList<Solution>(size());
        for (ParetoSolution s : mFirstObjSol)
            sol.add(s.getSolution());
        return sol;
    }

    /**
     * <code>ParetoSolution</code> is used to attach two objective values to a solution. It assumes that the solution
     * objective values will not change after the object creation.
     * <p>
     * Creation date: Nov 23, 2011 - 3:02:39 PM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    public class ParetoSolution {
        private final double       mFirstObjValue;
        private final double       mSecondObjValue;
        private final Solution mSolution;

        /**
         * Creates a new <code>ParetoSolution</code>
         * 
         * @param solution
         * @param firstObjValue
         * @param secondObjValue
         */
        public ParetoSolution(Solution solution, double firstObjValue, double secondObjValue) {
            mSolution = solution;
            mFirstObjValue = firstObjValue;
            mSecondObjValue = secondObjValue;
        }

        /**
         * Getter for <code>firstObjValue</code>
         * 
         * @return the firstObjValue
         */
        public double getFirstObjValue() {
            return mFirstObjValue;
        }

        /**
         * Getter for <code>secondObjValue</code>
         * 
         * @return the secondObjValue
         */
        public double getSecondObjValue() {
            return mSecondObjValue;
        }

        /**
         * Check if this solution strictly dominates {@code  sol}
         * 
         * @param sol
         *            the solution to be tested
         * @return true if this solution strictly dominates {@code  sol}
         */
        public boolean dominates(ParetoSolution sol) {
            return ParetoFrontOld.this.mFirstObjSense.isBetter(sol.getFirstObjValue(), getFirstObjValue(), true)
                    && ParetoFrontOld.this.mSecondObjSense.isBetter(sol.getSecondObjValue(), getSecondObjValue(), true);
        }

        /**
         * Getter for <code>solution</code>
         * 
         * @return the solution
         */
        public Solution getSolution() {
            return mSolution;
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            long temp;
            temp = Double.doubleToLongBits(mFirstObjValue);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(mSecondObjValue);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            return result;
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ParetoSolution other = (ParetoSolution) obj;
            if (Double.doubleToLongBits(mFirstObjValue) != Double.doubleToLongBits(other.mFirstObjValue))
                return false;
            if (Double.doubleToLongBits(mSecondObjValue) != Double.doubleToLongBits(other.mSecondObjValue))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return String.format("%s [%.3f,%.3f]", mSolution != null ? mSolution.hashCode() : null, mFirstObjValue,
                    mSecondObjValue);
        }
    }

    /**
     * <code>ParetoSolutionComparator</code> is a comparator for {@link ParetoSolution} that is either based on the
     * first or second objective.
     * <p>
     * Creation date: Nov 23, 2011 - 3:03:14 PM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    public static class ParetoSolutionComparator implements Comparator<ParetoSolution> {
        private final boolean           mFirstObj;

        private final OptimizationSense mSense;

        /**
         * Creates a new <code>ParetoSolutionComparator</code>
         * 
         * @param firstObj
         * @param sense
         */
        public ParetoSolutionComparator(boolean firstObj, OptimizationSense sense) {
            mFirstObj = firstObj;
            mSense = sense;
        }

        @Override
        public int compare(ParetoSolution o1, ParetoSolution o2) {
            return mFirstObj ? mSense.compare(o1.getFirstObjValue(), o2.getFirstObjValue()) : mSense.compare(
                    o1.getSecondObjValue(), o2.getSecondObjValue());
        }
    }

    @Override
    public ParetoFrontOld clone() {
        return new ParetoFrontOld(this);
    }

    // ------------------------------------
    // ILockable interface implementation
    // ------------------------------------
    /** A lock to be used by this instance */
    private final ReentrantLock mLock;

    @Override
    public boolean tryLock(long timeout) {
        try {
            return getLockInstance().tryLock(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    @Override
    public void acquireLock() {
        try {
            if (!getLockInstance().tryLock(TRY_LOCK_TIMOUT, TRY_LOCK_TIMOUT_UNIT)) {
                throw new IllegalStateException(String.format(
                        "Unable to acquire lock on this instance of %s (%s) after %s %s", this.getClass()
                                .getSimpleName(), hashCode(), TRY_LOCK_TIMOUT, TRY_LOCK_TIMOUT_UNIT));
            }
        } catch (InterruptedException e) {
            throw new IllegalStateException(String.format("Unable to acquire lock on this instance of %s (%s)", this
                    .getClass().getSimpleName(), hashCode()), e);
        }
    }

    @Override
    public void releaseLock() {
        if (mLock.isLocked()) {
            this.mLock.unlock();
        }
    }

    @Override
    public boolean isLockOwnedByCurrentThread() {
        return this.mLock.isHeldByCurrentThread();
    }

    @Override
    public ReentrantLock getLockInstance() {
        return this.mLock;
    }
    // ------------------------------------

}
