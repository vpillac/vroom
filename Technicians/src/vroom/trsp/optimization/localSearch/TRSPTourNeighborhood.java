/**
 * 
 */
package vroom.trsp.optimization.localSearch;

import vroom.common.utilities.optimization.IMove;
import vroom.common.utilities.optimization.INeighborhood;
import vroom.trsp.datamodel.ITRSPTour;
import vroom.trsp.datamodel.TRSPTour;

/**
 * <code>TRSPTourNeighborhood</code> is an interface for neighborhoods that will operate on instances of
 * {@link TRSPTour}.
 * <p>
 * Creation date: Feb 24, 2011 - 10:22:48 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public interface TRSPTourNeighborhood<M extends IMove> extends INeighborhood<ITRSPTour, M> {

}
