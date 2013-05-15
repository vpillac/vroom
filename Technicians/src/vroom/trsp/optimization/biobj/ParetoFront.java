/**
 * 
 */
package vroom.trsp.optimization.biobj;

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
import vroom.common.utilities.optimization.IInstance;
import vroom.common.utilities.optimization.IParameters;
import vroom.common.utilities.optimization.OptimizationSense;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.datamodel.costDelegates.TRSPCostDelegate;

/**
 * <code>ParetoFront</code> is an implementation of {@link IPALNSSolutionPool} that maintain a Pareto front for
 * {@link TRSPSolution solutions} according to 2 objectives.
 * <p>
 * Creation date: Nov 23, 2011 - 1:55:52 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class ParetoFront implements IPALNSSolutionPool<TRSPSolution>,
        vroom.common.utilities.ICloneable<ParetoFront>, Cloneable, ILockable {

    private final HashSet<ParetoSolution> mAllSolutions;
    private final TreeSet<ParetoSolution> mFirstObjSol;

    private final OptimizationSense       mFirstObjSense;
    private final OptimizationSense       mSecondObjSense;

    private final TRSPCostDelegate        mFirstCostDelegate;
    private final TRSPCostDelegate        mSecondCostDelegate;

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
    TRSPCostDelegate getFirstCostDelegate() {
        return mFirstCostDelegate;
    }

    /**
     * Getter for <code>secondCostDelegate</code>
     * 
     * @return the secondCostDelegate
     */
    TRSPCostDelegate getSecondCostDelegate() {
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
    public ParetoFront(TRSPCostDelegate firstCostDelegate, OptimizationSense firstObjSense,
            TRSPCostDelegate secondCostDelegate, OptimizationSense secondObjSense) {
        mFirstCostDelegate = firstCostDelegate;
        mFirstObjSense = firstObjSense;
        mSecondCostDelegate = secondCostDelegate;
        mSecondObjSense = secondObjSense;

        mFirstObjSol = new TreeSet<ParetoSolution>(new ParetoSolutionComparator(true,
                mFirstObjSense));
        mSecondObjSol = Collections.unmodifiableSortedSet(mFirstObjSol.descendingSet());
        mAllSolutions = new HashSet<ParetoFront.ParetoSolution>();

        mLock = new ReentrantLock();
    }

    /**
     * Creates a new <code>ParetoFront</code> by cloning {@code  original}
     * 
     * @param original
     */
    @SuppressWarnings("unchecked")
    private ParetoFront(ParetoFront original) {
        mFirstCostDelegate = original.mFirstCostDelegate;
        mFirstObjSense = original.mFirstObjSense;
        mSecondCostDelegate = original.mSecondCostDelegate;
        mSecondObjSense = original.mSecondObjSense;

        mFirstObjSol = (TreeSet<ParetoSolution>) original.mFirstObjSol.clone();
        mSecondObjSol = Collections.unmodifiableSortedSet(mFirstObjSol.descendingSet());
        mAllSolutions = (HashSet<ParetoSolution>) original.mAllSolutions.clone();

        mLock = new ReentrantLock();
    }

    /**
     * Wraps a {@link TRSPSolution solution} in a {@link ParetoSolution pareto solution} and store its cost depending on
     * both objectives
     * 
     * @param sol
     *            the solution wrap
     * @return the wrapped solution
     */
    public ParetoSolution wrapSolution(TRSPSolution sol) {
        return new ParetoSolution(sol, mFirstCostDelegate.evaluateSolution(sol, true, false),
                mSecondCostDelegate.evaluateSolution(sol, true, false));
    }

    /**
     * Creates a point in the objective function space wrapped in a {@link ParetoSolution} with no associated
     * {@link TRSPSolution}
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
    public void initialize(IInstance instance, TRSPSolution solution, IParameters params) {
    }

    @Override
    public boolean add(TRSPSolution solution, boolean mainIteration) {
        ParetoSolution sol = wrapSolution(solution);

        if (mAllSolutions.contains(sol))
            return false;

        if (size() == 0) {
            // Add the solution to the front
            addInternal(sol);
            return true;
        }

        SortedSet<ParetoSolution> bestFirstObj = mFirstObjSol.tailSet(sol, true);

        boolean nonDominated = true;
        if (bestFirstObj.isEmpty()) {
            nonDominated = true;
        } else {
            // Look for a dominating solution
            Iterator<ParetoSolution> bfit = bestFirstObj.iterator();
            while (bfit.hasNext() && nonDominated) {
                ParetoSolution bettersolfo = bfit.next();
                if (bettersolfo.dominates(sol))
                    nonDominated = false;
                // if (bettersolfo.getSolution().getUnservedCount() == 0)
                // break;
            }

        }

        if (nonDominated) { // && !bestSecondObj.first().dominates(sol))) {
            // The solution is the best the first objective objectives
            // Or is better than the closest solutions on the second objective

            // Check for strictly dominated solutions
            Iterator<ParetoSolution> it = mFirstObjSol.headSet(sol, true).descendingIterator();
            ParetoSolution s;
            while (it.hasNext() && sol.dominates(s = it.next())) {
                it.remove();
                mAllSolutions.remove(s);
            }

            // Add the solution internally
            addInternal(sol);
            return true;
        } else {
            // The solution is dominated
            return false;
        }
    }

    /**
     * Return {@code true} if no solution of the front is dominated by another, {@code false} otherwise
     * 
     * @return {@code true} if no solution of the front is dominated by another, {@code false} otherwise
     */
    public boolean checkPareto() {
        for (ParetoSolution s : mAllSolutions)
            for (ParetoSolution t : mAllSolutions)
                if (s != t && s.dominates(t))
                    return false;
        return true;
    }

    void addInternal(ParetoSolution sol) {
        mFirstObjSol.add(sol);
        mAllSolutions.add(sol);
    }

    @Override
    public void addAll(Collection<TRSPSolution> solutions, boolean mainIteration) {
        for (TRSPSolution sol : solutions) {
            add(sol, false);
        }

    }

    @Override
    public Collection<TRSPSolution> subset(int size, RandomStream rndStream) {
        ArrayList<ParetoSolution> wrappedSubset = Random.sample(mFirstObjSol, size, rndStream);
        ArrayList<TRSPSolution> subset = new ArrayList<TRSPSolution>(wrappedSubset.size());
        for (ParetoSolution sol : wrappedSubset) {
            subset.add(sol.getSolution());
        }
        return subset;
    }

    @Override
    public TRSPSolution getBest() {
        return mFirstObjSol.last().getSolution();
    }

    @Override
    public int size() {
        return mFirstObjSol.size();
    }

    @Override
    public boolean isFull() {
        return false;
    }

    @Override
    public void clear() {
        mFirstObjSol.clear();
        mAllSolutions.clear();
        // mSecondObjSol.clear();
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
     * Returns the best solution according to the first objective
     * 
     * @return the best solution according to the first objective
     */
    public ParetoSolution getBestFirstObj() {
        return mFirstObjSol.last();
    }

    private final SortedSet<ParetoSolution> mSecondObjSol;

    /**
     * Returns the solutions in this Pareto front sorted according to the second objective
     * <p>
     * The {@linkplain SortedSet#last() last solution} is the best
     * <p>
     * 
     * @return the solutions in this Pareto front sorted according to the second objective
     */
    public SortedSet<ParetoSolution> getSolutionsSecondObj() {
        return mSecondObjSol;
    }

    /**
     * Returns the best solution according to the second objective
     * 
     * @return the best solution according to the second objective
     */
    public ParetoSolution getBestSecondObj() {
        return mFirstObjSol.first();
    }

    @Override
    public Iterator<TRSPSolution> iterator() {
        return getSolutions().iterator();
    }

    @Override
    public List<TRSPSolution> getSolutions() {
        ArrayList<TRSPSolution> sol = new ArrayList<TRSPSolution>(size());
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
        private final TRSPSolution mSolution;

        /**
         * Creates a new <code>ParetoSolution</code>
         * 
         * @param solution
         * @param firstObjValue
         * @param secondObjValue
         */
        public ParetoSolution(TRSPSolution solution, double firstObjValue, double secondObjValue) {
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
            OptimizationSense fs = ParetoFront.this.mFirstObjSense;
            OptimizationSense ss = ParetoFront.this.mFirstObjSense;
            return ((fs.isBetter(sol.getFirstObjValue(), getFirstObjValue(), false) && ss.isBetter(
                    sol.getSecondObjValue(), getSecondObjValue(), true)) || (fs.isBetter(
                    sol.getFirstObjValue(), getFirstObjValue(), true) && ss.isBetter(
                    sol.getSecondObjValue(), getSecondObjValue(), false)));
        }

        /**
         * Getter for <code>solution</code>
         * 
         * @return the solution
         */
        public TRSPSolution getSolution() {
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
            if (Double.doubleToLongBits(mFirstObjValue) != Double
                    .doubleToLongBits(other.mFirstObjValue))
                return false;
            if (Double.doubleToLongBits(mSecondObjValue) != Double
                    .doubleToLongBits(other.mSecondObjValue))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return String.format("%s [%s,%s (%s)]",
                    mSolution != null ? mSolution.hashCode() : null,
                    Utilities.format(mFirstObjValue), Utilities.format(mSecondObjValue),
                    mSolution != null ? mSolution.getUnservedCount() : "");
            // return String.format("%s [%.3f,%.3f (%s)]", mSolution != null ? mSolution.hashCode() : null,
            // mFirstObjValue, mSecondObjValue, mSolution != null ? mSolution.getUnservedCount() : "");
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
            return mFirstObj ? mSense.compare(o1.getFirstObjValue(), o2.getFirstObjValue())
                    : mSense.compare(o1.getSecondObjValue(), o2.getSecondObjValue());
        }
    }

    @Override
    public ParetoFront clone() {
        return new ParetoFront(this);
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
                        "Unable to acquire lock on this instance of %s (%s) after %s %s", this
                                .getClass().getSimpleName(), hashCode(), TRY_LOCK_TIMOUT,
                        TRY_LOCK_TIMOUT_UNIT));
            }
        } catch (InterruptedException e) {
            throw new IllegalStateException(String.format(
                    "Unable to acquire lock on this instance of %s (%s)", this.getClass()
                            .getSimpleName(), hashCode()), e);
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
