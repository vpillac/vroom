/**
 * 
 */
package vroom.common.heuristics.vrp;

import vroom.common.heuristics.Move;
import vroom.common.modeling.dataModel.IVRPSolution;
import vroom.common.utilities.optimization.INeighborhood;

/**
 * <code>ISolutionNeighborhood</code>
 * <p>
 * Creation date: Apr 27, 2010 - 11:15:14 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public interface IVRPSolutionNeighborhood<M extends Move> extends INeighborhood<IVRPSolution<?>, M> {

}
