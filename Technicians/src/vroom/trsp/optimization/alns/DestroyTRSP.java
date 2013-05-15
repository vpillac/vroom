/**
 *
 */
package vroom.trsp.optimization.alns;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Set;

import vroom.common.heuristics.alns.IDestroy;
import vroom.common.utilities.optimization.IParameters;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.datamodel.TRSPTour;

/**
 * <code>DestroyTRSP</code> is the base class for <em>destroy</em> procedures used within an ALNS for the TRSP.
 * <p>
 * It provides common processes such as the removal of unnecessary depot trips
 * </p>
 * <p>
 * Creation date: May 24, 2011 - 10:34:22 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public abstract class DestroyTRSP implements IDestroy<TRSPSolution> {

    /**
     * Creates a new <code>DestroyTRSP</code>
     * 
     * @param simulator
     */
    public DestroyTRSP() {
    }

    private boolean mRunning = false;

    @Override
    public final IDestroyResult<TRSPSolution> destroy(TRSPSolution solution,
            IParameters parameters, double size) {
        if (mRunning)
            throw new ConcurrentModificationException(
                    "This destroy procedure is already used by another thread");
        mRunning = true;

        ArrayList<Integer> removable = new ArrayList<>(solution.getInstance()
                .getUnservedReleasedRequests().size());
        for (int i : solution.getInstance().getUnservedReleasedRequests()) {
            if (canBeRemoved(solution, i))
                removable.add(i);
        }

        Set<Integer> remRequests;
        if (!removable.isEmpty()) {

            int numReq = (int) (size * removable.size());
            remRequests = doDestroy(solution, parameters, removable, numReq);

            // Remove unnecessary trips to depot
            for (TRSPTour t : solution) {
                if (t.isMainDepotVisited() && !t.isVisitToMainDepotRequired()
                        && !solution.getInstance().isServedOrAssignedOrRejected(t.getMainDepotId()))
                    t.removeNode(t.getMainDepotId());
            }
        } else {
            remRequests = Collections.emptySet();
        }

        mRunning = false;
        return new TRSPDestroyResult(remRequests);
    }

    /**
     * Remove <code>size*{@link TRSPInstance#getRequestCount() requestCount}</code> requests from <code>solution</code>
     * and return them in a set.
     * <p>
     * Implementations do no need to remove depot trips, nor to add removed requests to the
     * {@link TRSPSolution#getUnservedRequests() unserved pool}.
     * </p>
     * 
     * @param solution
     *            the solution
     * @param parameters
     *            the parameters
     * @param removableReq
     *            the ids of the requests that can be removed
     * @param numReq
     *            the number of requests to be removed
     * @return a set containing the removed requests
     * @see #destroy(TRSPSolution, IParameters, double)
     */
    protected abstract Set<Integer> doDestroy(TRSPSolution solution, IParameters parameters,
            List<Integer> removableReq, int numReq);

    /**
     * Check if a request can be removed from the current solution. The method will return {@code false} for requests
     * that are already {@linkplain TRSPSolution#getUnservedRequests() unserved}, and in the dynamic case this method
     * will return {@code false} for requests that have already been served, or assigned, or rejected.
     * 
     * @param request
     *            the request id
     * @return {@code true} if the request can be removed, {@code false} otherwise
     */
    private boolean canBeRemoved(TRSPSolution solution, int request) {
        return !solution.getInstance().isServedOrAssignedOrRejected(request)
                && !solution.getUnservedRequests().contains(request);
    }

    /**
     * Remove a request from the solution
     * 
     * @param solution
     *            the solution in which the request will be removed
     * @param request
     *            the request to be removed
     * @return the tour in which the request was, or <code>null</code> if the request was not found
     */
    public TRSPTour removeRequest(TRSPSolution solution, int request) {
        TRSPTour tour = solution.getVisitingTour(request);
        if (tour != null) {
            tour.removeNode(request);
            solution.markAsUnserved(request);
        }
        return tour;
    }

    @Override
    public void dispose() {
        // Do nothing
    }

    @Override
    public abstract DestroyTRSP clone();

    /**
     * Store the evaluation of a request
     */
    protected static class Evaluation implements Comparable<Evaluation> {
        final int      request;
        final TRSPTour tour;
        double         eval;

        /**
         * Creates a new <code>Relatedness</code>
         * 
         * @param request
         * @param tour
         * @param eval
         */
        public Evaluation(int request, TRSPTour tour, double eval) {
            this.request = request;
            this.eval = eval;
            this.tour = tour;
        }

        @Override
        public int compareTo(Evaluation o) {
            return Double.compare(this.eval, o.eval);
        }

        @Override
        public String toString() {
            return String.format("r%s(%s)", request, eval);
        }
    }
}
