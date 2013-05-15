/**
 * 
 */
package vroom.trsp.datamodel.costDelegates;

import vroom.common.utilities.IDistance;
import vroom.common.utilities.LevenshteinDistance;
import vroom.trsp.datamodel.ITRSPTour;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.datamodel.TRSPTour;

/**
 * <code>TRSPLevenshteinDistance</code> is an implementation of {@link TRSPCostDelegate} that measure the
 * <em>distance</em> from a reference solution.
 * <p>
 * Creation date: Nov 22, 2011 - 11:25:27 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TRSPLevenshteinDistance extends TRSPCostDelegate implements IDistance<TRSPSolution> {

    /** the reference solution */
    private final TRSPSolution mRefSolution;

    /**
     * Returns the reference solution
     * 
     * @return the reference solution
     */
    public TRSPSolution getRefSolution() {
        return mRefSolution;
    }

    /**
     * Creates a new <code>TRSPLevenshteinDistance</code> with no reference solution
     */
    public TRSPLevenshteinDistance() {
        this(null);
    }

    /**
     * Creates a new <code>TRSPLevenshteinDistance</code>
     * 
     * @param refSolution
     */
    public TRSPLevenshteinDistance(TRSPSolution refSolution) {
        mRefSolution = refSolution;
    }

    /* (non-Javadoc)
     * @see vroom.trsp.datamodel.costDelegates.TRSPCostDelegate#evaluateGenericTour(vroom.trsp.datamodel.ITRSPTour)
     */
    @Override
    protected double evaluateGenericTour(ITRSPTour tour) {
        return evaluateLevenshteinDistance(mRefSolution.getTour(tour.getTechnicianId()), tour);
    }

    /* (non-Javadoc)
     * @see vroom.trsp.datamodel.costDelegates.TRSPCostDelegate#evaluateTRSPTour(vroom.trsp.datamodel.TRSPTour, int, boolean)
     */
    @Override
    protected double evaluateTRSPTour(TRSPTour tour, int node, boolean updateTour) {
        double value = evaluateGenericTour(tour);
        if (updateTour)
            tour.setTotalCost(value);
        return value;
    }

    /* (non-Javadoc)
     * @see vroom.trsp.datamodel.costDelegates.TRSPCostDelegate#evaluateDetour(vroom.trsp.datamodel.ITRSPTour, int, int, int, boolean)
     */
    @Override
    public double evaluateDetour(ITRSPTour tour, int i, int n, int j,
            boolean isRemoval) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see vroom.trsp.datamodel.costDelegates.TRSPCostDelegate#isInsertionSeqDependent()
     */
    @Override
    public boolean isInsertionSeqDependent() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the <a href="http://en.wikipedia.org/wiki/Levenshtein_distance">Levenshtein distance</a> between two
     * solutions
     * 
     * @param reference
     *            the reference tour
     * @param solution
     *            the solution to be evaluated
     * @return the Levenshtein distance between {@code  reference} and {@code  solution}
     */
    public static int evaluateLevenshteinDistance(TRSPSolution reference, TRSPSolution solution) {
        int dist = 0;
        for (TRSPTour t : reference) {
            dist += evaluateLevenshteinDistance(t, solution.getTour(t.getTechnicianId()));
        }
        return dist;
    }

    /**
     * Returns the <a href="http://en.wikipedia.org/wiki/Levenshtein_distance">Levenshtein distance</a> between two
     * tours
     * 
     * @param reference
     *            the reference tour
     * @param tour
     *            the tour to be evaluated
     * @return the Levenshtein distance between {@code  reference} and {@code  tour}
     */
    public static int evaluateLevenshteinDistance(ITRSPTour reference, ITRSPTour tour) {
        return LevenshteinDistance.getDistance(reference.asList(), tour.asList());
    }

    @Override
    public double evaluateDistance(TRSPSolution obj1, TRSPSolution obj2) {
        return evaluateLevenshteinDistance(obj1, obj2);
    }

    @Override
    public boolean isSymmetric() {
        return true;
    }
}
