/**
 *
 */
package vroom.trsp.optimization.alns;

import java.util.Set;

import vroom.common.heuristics.alns.IDestroy;
import vroom.common.utilities.optimization.IInstance;
import vroom.common.utilities.optimization.IParameters;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.datamodel.TRSPTour;

/**
 * <code>DestroyTimeRelated</code> is an implementation of {@link IDestroy} based on a measure of the relatedness
 * depending on the time of visit of requests.
 * <p>
 * Creation date: May 30, 2011 - 9:46:32 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class DestroyTimeRelated extends DestroyRelated {

    private Relatedness[][] mEvaluations;

    /**
     * Creates a new <code>DestroyRelated</code>
     * 
     * @param randomization
     *            the parameter <em>p</em> that control the level of randomization (p&ge;1, p=1 for no randomization)
     */
    public DestroyTimeRelated(double randomization) {
        super(randomization);
    }

    /* (non-Javadoc)
     * @see vroom.common.heuristics.alns.IDestroy#initialize(vroom.common.utilities.optimization.IInstance)
     */
    @Override
    public void initialize(IInstance instance) {
        TRSPInstance i = (TRSPInstance) instance;
        mEvaluations = new Relatedness[i.getMaxId()][i.getMaxId()];
    }

    @Override
    protected void initialize(TRSPSolution solution, IParameters params, int remRequests, Set<Integer> candidates) {
        super.initialize(solution, params, remRequests, candidates);

        // Evaluate all candidates
        for (int i : candidates) {
            for (int j : candidates) {
                if (i >= j)
                    continue;
                // Compatible technicians
                Set<Integer> Ki = solution.getInstance().getCompatibleTechnicians(i);
                Set<Integer> Kj = solution.getInstance().getCompatibleTechnicians(j);
                // At least one technician in common
                boolean compatible = false;
                for (int t : Ki)
                    if (Kj.contains(t)) {
                        compatible = true;
                        break;
                    }
                double rij = Double.POSITIVE_INFINITY;

                if (compatible) {
                    TRSPTour ti = solution.getVisitingTour(i);
                    TRSPTour tj = solution.getVisitingTour(j);
                    rij = Math.abs(ti.getEarliestArrivalTime(i) - tj.getEarliestArrivalTime(j));
                }
                mEvaluations[i][j] = new Relatedness(i, j, rij);
                mEvaluations[j][i] = new Relatedness(j, i, rij);
            }
        }
    }

    /* (non-Javadoc)
     * @see vroom.trsp.optimization.alns.DestroyRelated#evaluateRelatedRequests(int, vroom.trsp.datamodel.TRSPSolution, java.util.Set)
     */
    @Override
    protected Relatedness[] evaluateRelatedRequests(int seed, TRSPSolution solution, Set<Integer> candidates) {
        return mEvaluations[seed];
    }

    @Override
    public String getName() {
        return "rel-time";
    };

    @Override
    public void dispose() {
        mEvaluations = null;
    }

    @Override
    public DestroyTimeRelated clone() {
        DestroyTimeRelated clone = new DestroyTimeRelated(getRandomization());
        clone.mEvaluations = new Relatedness[mEvaluations.length][mEvaluations.length];
        return clone;
    }
}
