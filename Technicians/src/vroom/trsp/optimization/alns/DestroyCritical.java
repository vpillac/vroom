/**
 * 
 */
package vroom.trsp.optimization.alns;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import vroom.common.heuristics.alns.IDestroy;
import vroom.common.utilities.math.QuickSelect;
import vroom.common.utilities.optimization.IInstance;
import vroom.common.utilities.optimization.IParameters;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.datamodel.TRSPTour;
import vroom.trsp.datamodel.costDelegates.TRSPDistance;
import vroom.trsp.optimization.RemoveMove;

/**
 * <code>DestroyCritical</code> is an implementation of {@link IDestroy} that attempt to remove requests that are the
 * most costly in the current solution.
 * <p>
 * Creation date: Jun 9, 2011 - 1:21:19 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class DestroyCritical extends DestroyTRSP {

    private final TRSPDistance mDistanceCD;

    /** the randomization parameter *. */
    private final double       mRandomization;

    /**
     * Getter for the randomization parameter.
     * 
     * @return the parameter <em>p</em> that control the level of randomization
     */
    public double getRandomization() {
        return this.mRandomization;
    }

    /**
     * Creates a new <code>DestroyCritical</code>
     * 
     * @param randomization
     *            the parameter <em>p</em> that control the level of randomization (p&ge;1, p=1 for no randomization)
     */
    public DestroyCritical(double randomization) {
        super();
        mRandomization = randomization;
        mDistanceCD = new TRSPDistance();
    }

    /*
     * (non-Javadoc)
     * @see vroom.common.heuristics.alns.IALNSComponent#initialize(vroom.common.utilities.optimization.IInstance)
     */
    @Override
    public void initialize(IInstance instance) {
        // Do nothing
    }

    /*
     * (non-Javadoc)
     * @see vroom.common.heuristics.alns.IALNSComponent#getName()
     */
    @Override
    public String getName() {
        return "crit";
    }

    /*
     * (non-Javadoc)
     * @see vroom.trsp.optimization.alns.DestroyTRSP#doDestroy(vroom.trsp.datamodel.TRSPSolution,
     * vroom.common.utilities.optimization.IParameters, double)
     */
    @Override
    protected Set<Integer> doDestroy(TRSPSolution solution, IParameters parameters, List<Integer> removableReq,
            int numReq) {
        if (solution.getUnservedCount() == solution.getInstance().getRequestCount())
            return Collections.emptySet();
        TRSPTour lastModifiedTour = null;
        Set<Integer> removed = new HashSet<Integer>();

        TRSPInstance instance = solution.getInstance();

        Evaluation[] evaluations = new Evaluation[instance.getMaxId()];
        int removableCount = removableReq.size();

        // Evaluate all the removable requests
        @SuppressWarnings("unchecked")
        ArrayList<Evaluation>[] tourEval = (ArrayList<Evaluation>[]) new ArrayList<?>[solution.getTourCount()];
        for (Integer req : removableReq) {
            TRSPTour tour = solution.getVisitingTour(req);
            if (tour == null)
                throw new IllegalStateException("Request " + req + " is not visited by any tour in solution "
                        + solution);
            // FIXME Check if it is a good idea to use the distance as proxy
            evaluations[req] = new Evaluation(req, tour, mDistanceCD.evaluateMove(new RemoveMove(req, tour)));
            if (tourEval[tour.getTechnicianId()] == null)
                tourEval[tour.getTechnicianId()] = new ArrayList<Evaluation>(removableReq.size() / 2);
            tourEval[tour.getTechnicianId()].add(evaluations[req]);
        }

        // Sort the candidate list
        // Collections.sort(candidates);

        // Remove requests
        while (removed.size() < numReq && removableCount > 0) {
            // The k-th most related request will be selected
            int k = (int) Math.floor(Math.pow(parameters.getRandomStream().nextDouble(), getRandomization())
                    * removableCount);
            // Select the k-th most related request

            Evaluation selectedEval = QuickSelect.quickSelect(evaluations, k + 1, true);
            selectedEval.eval = Double.NaN; // Mark the evaluation as already removed
            evaluations[k] = null;
            lastModifiedTour = selectedEval.tour;
            removed.add(selectedEval.request);

            // Remove the request from the tour
            lastModifiedTour = removeRequest(solution, selectedEval.request);
            removableCount--;

            // if (mDistanceCD.isInsertionSeqDependent()) {
            // Re-evaluate all the requests in the modified tour
            for (Evaluation e : tourEval[lastModifiedTour.getTechnicianId()]) {
                if (!Double.isNaN(e.eval)) // Ignore evalutations that were already removed
                    e.eval = mDistanceCD.evaluateMove(new RemoveMove(e.request, e.tour));
            }
            // } else {
            // // Evaluate the predecessor
            // int pred = selectedEval.tour.getPred(selectedEval.request);
            // int succ = selectedEval.tour.getSucc(selectedEval.request);
            // if (canBeRemoved(solution, pred)) {
            // RemoveMove rem = new RemoveMove(pred, lastModifiedTour);
            // double eval = solution.getCostDelegate().evaluateMove(rem);
            // evaluations[pred].eval = eval;
            // }
            //
            // // Evaluate the successor
            // if (canBeRemoved(solution, succ)) {
            // RemoveMove rem = new RemoveMove(succ, lastModifiedTour);
            // double eval = solution.getCostDelegate().evaluateMove(rem);
            // evaluations[succ].eval = eval;
            // }
            // }
        }

        return removed;
    }

    @Override
    public DestroyCritical clone() {
        return new DestroyCritical(getRandomization());
    }

    @Override
    public String toString() {
        return String.format("%s[p:%s]", getName(), getRandomization());
    }
}
