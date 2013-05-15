/**
 * 
 */
package vroom.common.heuristics.vrp;

import vroom.common.modeling.dataModel.IVRPSolution;

/**
 * <code>SwapMove</code>
 * <p>
 * Creation date: Jul 1, 2010 - 2:49:09 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los
 *         Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class SwapMove extends PairMove<IVRPSolution<?>> {

	/**
	 * Creates a new <code>SwapMove</code>
	 * 
	 * @param improvement
	 * @param mSolution
	 * @param routeI
	 * @param routeJ
	 * @param nodeI
	 * @param nodeJ
	 */
	public SwapMove(double improvement, IVRPSolution<?> solution, int routeI,
			int routeJ, int i, int j) {
		super(improvement, solution, routeI, routeJ, i, j);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see vroom.common.heuristics.Move#getMoveName()
	 */
	@Override
	public String getMoveName() {
		return "swap";
	}

}
