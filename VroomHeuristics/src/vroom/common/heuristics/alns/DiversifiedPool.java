package vroom.common.heuristics.alns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import umontreal.iro.lecuyer.rng.RandomStream;
import vroom.common.utilities.IDistance;
import vroom.common.utilities.Utilities;
import vroom.common.utilities.Utilities.Random;
import vroom.common.utilities.optimization.IInstance;
import vroom.common.utilities.optimization.IParameters;
import vroom.common.utilities.optimization.ISolution;
import vroom.common.utilities.optimization.OptimizationSense;
import vroom.common.utilities.optimization.SAAcceptanceCriterion;

/**
 * <code>DiversifiedPool</code> is an implementation of {@link IPALNSSolutionPool} that maintains a fixed size set of
 * solutions ordered according to their objective value and their contribution to a diversity metric.
 * <p>
 * To work properly this implementation requires that solutions are not modified once added to the pool, and it uses
 * solutions' {@linkplain #hashCode() hash code} to assert if two solutions are equals.
 * </p>
 * <p>
 * Creation date: Feb 29, 2012 - 10:57:19 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 * @param <S>
 */
public class DiversifiedPool<S extends ISolution> implements IPALNSSolutionPool<S> {

    public static boolean                    sAutoAdjustWeights = true;

    private final OptimizationSense          mSense;

    private final int                        mMaxSize;

    private final IDistance<S>               mDistance;

    private final ArrayList<Solution>        mWrappedSol;
    // private final TreeSet<Solution> mDivSol;
    // private final TreeSet<Solution> mObjSol;
    private final ArrayList<Solution>        mDivSol;
    private final ArrayList<Solution>        mObjSol;

    private final double[][]                 mDistances;

    private final HashMap<Solution, Integer> mIdMapping;
    private final HashSet<Integer>           mSolHashes;
    private int                              mFreeId;

    /** The relative weight of the cost ranking component */
    private double                           mAlpha;
    /** "cooling rate" for the relative weight */
    private double                           mCoolingRate;

    @SuppressWarnings("unchecked")
    public DiversifiedPool(OptimizationSense sense, ALNSGlobalParameters params) {
        this((IDistance<S>) params.newInstance(ALNSGlobalParameters.DIVERSITY_METRIC), sense,
                params.get(ALNSGlobalParameters.PALNS_POOL_SIZE));
    }

    public DiversifiedPool(IDistance<S> distance, OptimizationSense sense, int maxSize) {
        mAlpha = 0.5;
        mSense = sense;
        mMaxSize = maxSize;
        mWrappedSol = new ArrayList<>(maxSize + 1);
        // mDivSol = new TreeSet<>(mDivComp);
        // mObjSol = new TreeSet<>(mObjComp);
        mDivSol = new ArrayList<>();
        mObjSol = new ArrayList<>();
        mDistance = distance;
        mDistances = new double[maxSize + 1][maxSize + 1];
        mIdMapping = new HashMap<>(maxSize * 2);
        mSolHashes = new HashSet<>(maxSize * 2);
        mFreeId = 0;
    }

    @Override
    public void initialize(IInstance instance, S solution, IParameters params) {
        if (SAAcceptanceCriterion.class
                .isAssignableFrom(params.getAcceptanceCriterion().getClass())) {
            // Copy the cooling rate from the acceptance criterion
            mCoolingRate = ((SAAcceptanceCriterion) params.getAcceptanceCriterion())
                    .getCoolingRate();
        } else {
            // Ensure that the final alpha is 10 times lower than the initial value
            mCoolingRate = Math.pow(0.1, 1d / params.getMaxIterations());
        }
    }

    @Override
    public boolean add(S solution, boolean mainIteration) {
        Solution newSol = new Solution(solution);
        if (!mainIteration)
            updateAlpha();
        if (mSolHashes.contains(newSol.hashCode()))
            return false;

        if (!mainIteration && getBest() != null && !mSense.isBetter(getBest(), solution))
            return false;

        evaluateDistancesAdd(newSol);

        if (size() == getMaxSize()) {
            // The pool is full
            // Add the new solution
            addInternal(newSol);

            evaluateFitness();

            // remove the worst to maintain the pool size
            Solution worst = removeWorstInternal();
            return worst != newSol;
        } else {
            // Pool is not full, add the solution to it
            addInternal(newSol);
            mFreeId++;
            return true;
        }
    }

    /**
     * Update the ratio
     */
    private void updateAlpha() {
        if (sAutoAdjustWeights) {
            mAlpha *= mCoolingRate;
        }
    }

    /**
     * Evaluate the distances when a new solution is added
     * 
     * @param newSol
     */
    private void evaluateDistancesAdd(Solution newSol) {
        for (Entry<Solution, Integer> e : mIdMapping.entrySet()) {
            mDistances[mFreeId][e.getValue()] = mDistance.evaluateDistance(newSol.getSolution(), e
                    .getKey().getSolution());
            newSol.mAvgDiversity += mDistances[mFreeId][e.getValue()];

            if (mDistance.isSymmetric())
                mDistances[e.getValue()][mFreeId] = mDistances[mFreeId][e.getValue()];
            else
                mDistances[mFreeId][e.getValue()] = mDistance.evaluateDistance(
                        newSol.getSolution(), e.getKey().getSolution());

            e.getKey().mAvgDiversity = (e.getKey().mAvgDiversity * size() + mDistances[e.getValue()][mFreeId])
                    / (size() + 1);
        }
        newSol.mAvgDiversity /= size() + 1;
    }

    /**
     * Update the stored distances when a solution is removed
     * 
     * @param remSol
     */
    private void evaluateDistancesRem(Solution remSol) {
        for (Entry<Solution, Integer> e : mIdMapping.entrySet()) {
            e.getKey().mAvgDiversity = (e.getKey().mAvgDiversity * (size() + 1) - mDistances[e
                    .getValue()][mFreeId]) / size();
        }
    }

    private void evaluateFitness() {
        // Sort the collections
        Collections.sort(mDivSol, mDivComp);

        // Update the ranks
        int i = 0;
        for (Solution s : mObjSol)
            s.mObjRank = i++;
        i = 0;
        for (Solution s : mDivSol)
            s.mDivRank = i++;

        // Update the fitness
        for (Solution s : mWrappedSol)
            s.mFitness = s.mObjRank == 0 ? 0 : s.mDivRank == 0 ? 0 : ((1 - mAlpha) * s.mObjRank)
                    / mWrappedSol.size() + (mAlpha * s.mDivRank) / mWrappedSol.size();

        // Sort the collection
        Collections.sort(mWrappedSol);
    }

    /**
     * Add a solution to this pool
     * 
     * @param s
     * @return {@code true} if the solution was added, {@code false} if it was already present
     */
    private void addInternal(Solution s) {
        mWrappedSol.add(s);
        mObjSol.add(s);
        Collections.sort(mObjSol, mObjComp);
        mDivSol.add(s);
        mIdMapping.put(s, mFreeId);
        mSolHashes.add(s.hashCode());
    }

    private Solution removeWorstInternal() {
        Solution worst = mWrappedSol.remove(mWrappedSol.size() - 1);
        mObjSol.remove(worst);
        Collections.sort(mObjSol, mObjComp);
        mDivSol.remove(worst);
        // if (!mObjSol.remove(worst))
        // throw new IllegalStateException("Unable to remove solution " + worst + " from ObjSol");
        // if (!mDivSol.remove(worst))
        // throw new IllegalStateException("Unable to remove solution " + worst + " from DivSol");
        // if (!getSolutionsInternal().remove(worst.getSolution()))
        // throw new IllegalStateException("Unable to remove solution " + worst
        // + " from parent solutions");
        mFreeId = mIdMapping.get(worst);
        mIdMapping.remove(worst);
        mSolHashes.remove(worst.hashCode());
        evaluateDistancesRem(worst);
        return worst;
    }

    @Override
    public void clear() {
        mWrappedSol.clear();
        mObjSol.clear();
        mDivSol.clear();
        mIdMapping.clear();
        mSolHashes.clear();
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder(size() * 10);
        b.append("[");
        for (Solution sol : mWrappedSol) {
            b.append(sol.toString());
            b.append(",");
        }
        if (b.length() > 1)
            b.setCharAt(b.length() - 1, ']');
        else
            b.append(']');

        return String.format("size:%s/%s %s", size(), getMaxSize(), b);
    }

    public boolean checkPool() {
        HashSet<S> wrap = new HashSet<>();
        HashSet<S> obj = new HashSet<>();
        HashSet<S> div = new HashSet<>();
        HashSet<?> sets[] = new HashSet<?>[] { wrap, obj, div };

        for (Solution s : mWrappedSol)
            if (!wrap.add(s.getSolution()))
                return false;
        for (Solution s : mObjSol)
            if (!obj.add(s.getSolution()))
                return false;
        for (Solution s : mDivSol)
            if (!div.add(s.getSolution()))
                return false;

        for (int i = 0; i < sets.length; i++)
            for (int j = i + 1; j < sets.length; j++)
                if (!Utilities.Math.equals(sets[i], sets[j]))
                    return false;

        return true;

    }

    /**
     * <code>Solution</code> is a wrapping class that stores diversity measure and overall evaluation of a solution
     */
    private class Solution implements Comparable<Solution> {
        private final S   mSolution;
        private final int mHash;
        private double    mAvgDiversity;
        private int       mObjRank;
        private int       mDivRank;
        private double    mFitness;

        private Solution(S solution) {
            super();
            mSolution = solution;
            mHash = solution.hashSolution();
        }

        @Override
        public int compareTo(Solution s) {
            if (this == s)
                return 0;
            int comp = Double.compare(this.mFitness, s.mFitness);
            return comp == 0 ? DiversifiedPool.this.mObjComp.compare(this, s) : comp;
        }

        @Override
        public int hashCode() {
            return mHash;
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean equals(Object obj) {
            return mHash == ((Solution) obj).mHash;
        }

        private S getSolution() {
            return mSolution;
        }

        @Override
        public String toString() {
            return String.format("%.1f:%.1f (%s/%s/%.1f)", mSolution.getObjectiveValue(),
                    mAvgDiversity, mObjRank, mDivRank, mFitness);
        }
    }

    private final SolutionObjComparator mObjComp = new SolutionObjComparator();

    private class SolutionObjComparator implements Comparator<Solution> {
        @Override
        public int compare(Solution o1, Solution o2) {
            if (o1 == o2)
                return 0;
            // [12/07/13] removed specific case when o1==o2 to prevent exception in sorting methods (since 1.7)
            return -mSense.compare(o1.getSolution(), o2.getSolution());
        }
    }

    private final SolutionDivComparator mDivComp = new SolutionDivComparator();

    private class SolutionDivComparator implements Comparator<Solution> {
        @Override
        public int compare(Solution o1, Solution o2) {
            if (o1 == o2)
                return 0;
            // [12/07/13] removed specific case when o1==o2 to prevent exception in sorting methods (since 1.7)
            return -Double.compare(o1.mAvgDiversity, o2.mAvgDiversity);
        }
    }

    /**
     * Returns the max size for this pool
     * 
     * @return the max size for this pool
     */
    public int getMaxSize() {
        return mMaxSize;
    }

    @Override
    public Iterator<S> iterator() {
        return getSolutions().iterator();
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
    public Collection<S> getSolutions() {
        ArrayList<S> sol = new ArrayList<>(size());
        for (Solution s : mObjSol)
            sol.add(s.getSolution());
        return sol;
    }

    @Override
    public S getBest() {
        return size() > 0 ? mObjSol.get(0).getSolution() : null;
    }

    @Override
    public int size() {
        return mWrappedSol.size();
    }

    @Override
    public boolean isFull() {
        return size() >= getMaxSize();
    }
}
