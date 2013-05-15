/**
 * 
 */
package vroom.trsp.optimization.biobj;

import java.util.Iterator;
import java.util.SortedSet;

import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.optimization.biobj.ParetoFront.ParetoSolution;

/**
 * <code>HierarchicalParetoSelector</code> selects the best solution according to the second objective allowing a fixed
 * degradation with respect to the best solution of the first objective.
 * <p>
 * Creation date: Dec 13, 2011 - 1:55:03 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class HierarchicalParetoSelector implements IParetoSelector {

    private final double mAllowedDegradation;

    /**
     * Returns the allowed degradation on the first objective.
     * <p>
     * For instance, return a value of {@code  1} if no degradation is allowed, {@code  1.1} if a degradation of up to 1%
     * is allowed, and {@link Double#POSITIVE_INFINITY} if the first objective should be ignored
     * </p>
     * 
     * @return the allowed degradation on the first objective
     */
    public double getAllowedDegradation() {
        return mAllowedDegradation;
    }

    /**
     * Creates a new <code>HierarchicalParetoSelector</code>
     * 
     * @param allowedDegradation
     *            the allowed degradation on the first objective, for instance, a value of {@code  1} if no degradation
     *            is allowed, {@code  1.1} if a degradation of up to 1% is allowed, and {@link Double#POSITIVE_INFINITY}
     *            if the first objective should be ignored
     */
    public HierarchicalParetoSelector(double allowedDegradation) {
        mAllowedDegradation = allowedDegradation;
    }

    /*
     * (non-Javadoc)
     * @see vroom.trsp.optimization.biobj.IParetoSelector#selectSolution(vroom.trsp.optimization.biobj.ParetoFront)
     */
    @Override
    public ParetoSolution selectParetoSolution(ParetoFront pareto) {
        double allowedDeg = mAllowedDegradation;
        SortedSet<ParetoSolution> solutions;
        if (allowedDeg == Double.POSITIVE_INFINITY) {
            // We use the solutions sorted according to the first objective
            // Thus the first feasible will be the best according to the second objective
            solutions = pareto.getSolutionsFirstObj();
            // We will select the first feasible solution
            allowedDeg = 1;
        } else {
            // We use the solutions sorted according to the second objective
            // Thus the first feasible will be the best according to the first objective
            solutions = pareto.getSolutionsSecondObj();
        }

        // Iterate over the solution set looking for the first feasible solution or the best solution with the minimum
        // number of unserved requests
        ParetoSolution selectedFeasSolution = null;
        ParetoSolution selectedSolution = null;
        double threshold = Double.NaN;
        for (ParetoSolution sol : solutions) {
            if (sol.getSolution().getUnservedCount() == 0) {
                if (allowedDeg == 1)
                    // We found the first (best) feasible solution, return it
                    return sol;
                if (selectedFeasSolution == null) {
                    // We found the best feasible solution
                    selectedFeasSolution = sol;
                    selectedSolution = sol;
                    // Evaluate the threshold on the first objective
                    threshold = sol.getFirstObjValue() * allowedDeg;
                } else if (sol.getFirstObjValue() < threshold) {
                    // We found a feasible solution that is acceptable regarding the threshold
                    // temporarily set it as the accepted solution
                    selectedFeasSolution = sol;
                    selectedSolution = sol;
                }
            } else if (selectedSolution == null
                    || sol.getSolution().getUnservedCount() < selectedSolution.getSolution()
                            .getUnservedCount()) {
                // We found an infeasible solution with the lowest number of unserved requests so far
                selectedSolution = sol;
            }

            if (selectedFeasSolution != null && sol.getFirstObjValue() > threshold)
                // We have a feasible solution and the current solution is above the threshold
                // Break the loop and return the selected feasible solution
                return selectedFeasSolution;
        }

        // Return the second choice (which is either the selectedFeasSolution or the best solution with the minimum
        // number of unserved requests)
        return selectedSolution;
    }

    @Override
    public TRSPSolution selectSolution(ParetoFront pareto) {
        ParetoSolution s = selectParetoSolution(pareto);
        return s != null ? s.getSolution() : null;
    }

    /**
     * Return the first feasible solution in {@code  subset}, or the first feasible solution of {@code  set}, or finally
     * the solution with the greatest number of served requests
     * 
     * @param subset
     *            a subset of solutions
     * @param set
     *            the complete set of solutions
     * @return the first feasible solution if any, or the first solution of {@code  set}
     * @deprecated this implementation has been replaced by {@link #selectParetoSolution(ParetoFront)}
     */
    @Deprecated
    protected ParetoSolution firstFeasibleSolution(SortedSet<ParetoSolution> subset,
            SortedSet<ParetoSolution> set) {
        // FIXME check if the solution selection is correct, in particular ensure that we dont miss feasible solutions
        // A way to do that would be to restrict first to the set of feasible solutions, and if it is empty then to
        // consider the whole set
        Iterator<ParetoSolution> it = subset.iterator();
        if (!it.hasNext())
            return null;
        ParetoSolution s = null;
        ParetoSolution best = null;
        while (it.hasNext()) {
            s = it.next();
            if (s.getSolution().getUnservedCount() == 0)
                return s;
            else if (best == null
                    || s.getSolution().getUnservedCount() < best.getSolution().getUnservedCount())
                best = s;
        }
        return best;
    }

}
