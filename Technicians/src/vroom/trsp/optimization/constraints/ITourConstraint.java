/**
 * 
 */
package vroom.trsp.optimization.constraints;

import vroom.common.utilities.optimization.IConstraint;
import vroom.common.utilities.optimization.IMove;
import vroom.trsp.datamodel.ITRSPTour;

/**
 * <code>ITourConstraint</code> is an extension of {@link IConstraint} that add extra methods used to fasten local
 * search procedures in the TRSP
 * <p>
 * Creation date: Apr 7, 2011 - 5:01:23 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public interface ITourConstraint extends IConstraint<ITRSPTour> {

    /**
     * Checks if the move is feasible and if the neighborhood exploration could be aborted.
     * <p>
     * Returns <code>isFeasible*1 + isFwdFeasible*2</code>, where <code>isFeasible=1</code> iif the <code>move</code> is
     * feasible, and <code>isFwdFeasible=1</code> iif a similar move using the next node in the tour could lead to a
     * feasible move.
     * </p>
     * <p>
     * In other words, if <code>isFwdFeasible=0</code> the sequential exploration of the neighborhood can be aborted as
     * no further move will be feasible.
     * </p>
     * 
     * @param tour
     *            the considered tour
     * @param move
     *            the evaluated move
     * @return an array <code>[isFeasible, isFwdFeasible]</code>
     */
    public int checkFeasibility(ITRSPTour tour, IMove move);

    /**
     * Check if a tour is feasible and return the first node at which the tour becomes infeasible.
     * 
     * @param tour
     * @return the first node at which the tour becomes infeasible, or {@link ITRSPTour#UNDEFINED} if the tour is
     *         feasible.
     */
    public abstract int firstInfeasibleNode(ITRSPTour tour);

}
