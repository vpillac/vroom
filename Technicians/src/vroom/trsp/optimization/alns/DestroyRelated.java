/**
 *
 */
package vroom.trsp.optimization.alns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import vroom.common.heuristics.alns.IDestroy;
import vroom.common.utilities.IntegerSet;
import vroom.common.utilities.math.QuickSelect;
import vroom.common.utilities.optimization.IParameters;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.datamodel.TRSPTour;

/**
 * <code>DestroyRelated</code> is a generic implementation of {@link IDestroy} for relatedness based destroy procedures
 * <p>
 * Creation date: May 26, 2011 - 11:25:03 AM.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public abstract class DestroyRelated extends DestroyTRSP {

    /** the randomization parameter *. */
    private final double mRandomization;

    /**
     * Getter for the randomization parameter <em>p</em>.
     * <p>
     * p ≥ 1 is a parameter that controls the level of randomness (the lower p, the more randomness is introduced)
     * </p>
     * 
     * @return the parameter <em>p</em> that control the level of randomization
     */
    public double getRandomization() {
        return this.mRandomization;
    }

    /**
     * Creates a new <code>DestroyRelated</code>
     * 
     * @param randomization
     *            the parameter <em>p</em> that control the level of randomization (p&ge;1, p=1 for no randomization)
     */
    public DestroyRelated(double randomization) {
        super();
        mRandomization = randomization;
    }

    /*
     * (non-Javadoc)
     * @see vroom.common.heuristics.alns.IDestroy#destroy(vroom.common.utilities.optimization.ISolution,
     * vroom.common.utilities.optimization.IParameters, double)
     */
    @Override
    public Set<Integer> doDestroy(TRSPSolution solution, IParameters parameters, List<Integer> removableReq, int numReq) {
        if (solution.getUnservedCount() == solution.getInstance().getRequestCount())
            return Collections.emptySet();
        TRSPTour lastModifiedTour = null;

        // Set of candidate requests to remove
        IntegerSet candidates = new IntegerSet(removableReq);

        // Set of removed requests
        ArrayList<Integer> remRequests = new ArrayList<Integer>(numReq);

        // Initialize data structures (delegated to subclasses)
        initialize(solution, parameters, numReq, candidates);

        // Select the initial seed request
        int seedIdx = parameters.getRandomStream().nextInt(0, candidates.size() - 1);
        int seed = removableReq.get(seedIdx);

        // Remove the seed
        lastModifiedTour = removeRequest(solution, seed);
        if (lastModifiedTour != null) {
            remRequests.add(seed);
            candidates.remove(seed);
            requestRemoved(seed, lastModifiedTour.getTechnician().getID(), solution);
        } else {
            throw new IllegalStateException("Could not remove request " + seed);
        }

        while (remRequests.size() < numReq) {
            // Select a new seed
            seed = remRequests.get(parameters.getRandomStream().nextInt(0, remRequests.size() - 1));
            // Evaluate the relatedness values
            Relatedness[] evals = evaluateRelatedRequests(seed, solution, candidates);
            // The k-th most related request will be selected
            int k = (int) Math.floor(Math.pow(parameters.getRandomStream().nextDouble(), getRandomization())
                    * candidates.size());
            // Select the k-th most related request
            Relatedness sel = quickselect(evals, k + 1, candidates);

            if (sel == null)
                // There are no more request to evaluate
                break;

            int req = sel.request2;

            // Find the tour serving the request and remove it
            lastModifiedTour = removeRequest(solution, req);
            if (lastModifiedTour != null) {
                remRequests.add(req);
                candidates.remove(req);
                requestRemoved(req, lastModifiedTour.getTechnician().getID(), solution);
            } else {
                throw new IllegalStateException("Attempting to remove a request that is not in any tour: " + req);
            }
        }
        return new HashSet<Integer>(remRequests);
    }

    /**
     * Returns the <code>k</code> smallest values of an array
     * 
     * @param values
     *            an array of double
     * @param k
     *            the number of elements to extract
     * @param filter
     *            the indexes to consider
     * @return the <code>n</code> biggest elements of <code>values</code> sorted in ascending order
     */
    public static <T extends Comparable<? super T>> T quickselect(T[] values, int k, Set<Integer> filter) {
        if (values.length == 0 || k == 0)
            return null;

        if (k == 1) {
            // More efficient implementation
            return min(values, filter);
        }

        T[] filteredValues = Arrays.copyOf(values, filter.size());
        int j = 0;
        for (int i : filter) {
            filteredValues[j++] = values[i];
        }

        return QuickSelect.quickSelect(filteredValues, k, true);
    }

    /**
     * Returns the minimum value of an array
     * 
     * @param values
     *            an array of value
     * @param filter
     *            the indexes to consider
     * @return the maximum of all elements of <code>values</code>
     */
    public static <T extends Comparable<? super T>> T min(T[] values, Set<Integer> filter) {
        if (values.length == 0)
            throw new IllegalArgumentException("Must have at least one value");
        T min = null;
        for (int i : filter) {
            if (values[i] != null && (min == null || values[i].compareTo(min) < 0))
                min = values[i];
        }
        return min;
    }

    /**
     * This method is called at the beginning of the execution of {@link #doDestroy(TRSPSolution, IParameters,
     * List<Integer>, int)}. It can be overridden in subclasses to initialize data structures.
     * 
     * @param solution
     *            the solution that will be destroyed
     * @param params
     *            the parameters of the destroy procedure
     * @param remRequests
     *            the number of requests that will be removed
     * @param candidates
     *            the requests candidate for removal
     */
    protected void initialize(TRSPSolution solution, IParameters params, int remRequests, Set<Integer> candidates) {
        // Do nothing
    }

    /**
     * This method is called whenever a request is removed from a tour and can be overridden by subclasses, e.g., to
     * update data structures.
     * 
     * @param request
     *            the request that was removed
     * @param solution
     *            the current solution
     */
    protected void requestRemoved(int request, int tour, TRSPSolution solution) {
        // Do nothing
    }

    /**
     * Evaluation of the candidate set
     * 
     * @param seed
     *            the seed request
     * @param solution
     *            the current solution
     * @param candidates
     *            the set of candidate requests
     * @return an array containing the evaluation of at least all the requests from the <code>candidate</code> set
     */
    protected abstract Relatedness[] evaluateRelatedRequests(int seed, TRSPSolution solution, Set<Integer> candidates);

    @Override
    public abstract DestroyRelated clone();

    @Override
    public String toString() {
        return String.format("%s[p:%s]", getName(), getRandomization());
    }

    /**
     * Store the relatedness value <code>r<sub>ij</sub></code>
     */
    protected static class Relatedness extends Evaluation {
        final int request2;

        /**
         * Creates a new <code>Relatedness</code>
         * 
         * @param i
         * @param j
         * @param rij
         */
        public Relatedness(int i, int j, double rij) {
            super(i, null, rij);
            this.request2 = j;
        }

        @Override
        public String toString() {
            return String.format("r%s,%s=%s", request, request2, eval);
        }
    }
}
