/**
 * 
 */
package vroom.trsp.optimization.biobj;

import vroom.common.utilities.optimization.OptimizationSense;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.datamodel.costDelegates.TRSPCostDelegate;

/**
 * <code>ALNSParetoFront</code> is an extension of {@link ParetoFront} that extend the dominance relation by considering
 * the feasibility of a solution.
 * <p>
 * Creation date: Dec 16, 2011 - 12:54:21 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class ALNSParetoFront extends ParetoFront {

    /**
     * Creates a new <code>ALNSParetoFront</code>
     * 
     * @param firstCostDelegate
     * @param firstObjSense
     * @param secondCostDelegate
     * @param secondObjSense
     * @see ParetoFront#ParetoFront(TRSPCostDelegate, OptimizationSense, TRSPCostDelegate, OptimizationSense)
     */
    public ALNSParetoFront(TRSPCostDelegate firstCostDelegate, OptimizationSense firstObjSense,
            TRSPCostDelegate secondCostDelegate, OptimizationSense secondObjSense) {
        super(firstCostDelegate, firstObjSense, secondCostDelegate, secondObjSense);
    }

    // @Override
    // public boolean add(TRSPSolution solution) {
    // // If the solution is feasible, delegate to the supertype implementation
    // if (solution.getUnservedCount() == 0)
    // return super.add(solution);
    //
    // ParetoSolution sol = wrapSolution(solution);
    //
    // // Solution is already present in this front
    // if (getAllSolutions().contains(sol))
    // return false;
    //
    // // The Pareto front is empty
    // if (size() == 0) {
    // // Add the solution to the front
    // addInternal(sol);
    // return true;
    // }
    //
    // SortedSet<ParetoSolution> bestFirstObj = getFirstObjSol().tailSet(sol, true);
    //
    // boolean nonDominated = bestFirstObj.isEmpty();
    // if (!nonDominated) {
    // // The solution MAY BE dominated
    // // Check the closest feasible solution on the first objective
    // for (ParetoSolution s : bestFirstObj)
    // if (s.getSolution().getUnservedCount() == 0) {
    // if (s.dominates(sol))
    // // A feasible solution dominates the candidate solution
    // nonDominated = false;
    // else
    // // No feasible solution dominates the candidate solution
    // nonDominated = true;
    // break;
    // }
    // }// else The solution is the best in one of the two objectives
    //
    // if (nonDominated) {
    // // Check for strictly dominated solutions
    // HashSet<ParetoSolution> dominatedSol = new HashSet<ParetoSolution>(getFirstObjSol().headSet(sol, true));
    // // dominatedSol.retainAll(getSecondObjSol().headSet(sol, true));
    //
    // getFirstObjSol().removeAll(dominatedSol);
    // // getSecondObjSol().removeAll(dominatedSol);
    // getAllSolutions().removeAll(dominatedSol);
    //
    // addInternal(sol);
    // return true;
    // } else {
    // // The solution is dominated
    // return false;
    // }
    // }

    @Override
    public ParetoSolution wrapSolution(TRSPSolution sol) {
        return new ALNSParetoSolution(sol, getFirstCostDelegate().evaluateSolution(sol, true, false),
                getSecondCostDelegate().evaluateSolution(sol, true, false));
    }

    /**
     * <code>ALNSParetoSolution</code> is a specialization of {@link ParetoSolution} for the context of ALNS in which
     * the dominance relation has to be extended to consider both feasible and infeasible solutions
     * <p>
     * Creation date: Jan 4, 2012 - 2:58:02 PM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    public class ALNSParetoSolution extends ParetoSolution {

        public ALNSParetoSolution(TRSPSolution solution, double firstObjValue, double secondObjValue) {
            super(solution, firstObjValue, secondObjValue);
        }

        @Override
        public boolean dominates(ParetoSolution sol) {
            if (!super.dominates(sol))
                return false;

            boolean tf = getSolution().getUnservedCount() == 0;
            boolean sf = sol.getSolution().getUnservedCount() == 0;
            return tf || (!tf && !sf);
        }
    }
}
