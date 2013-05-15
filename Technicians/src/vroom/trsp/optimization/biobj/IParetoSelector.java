/**
 * 
 */
package vroom.trsp.optimization.biobj;

import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.optimization.biobj.ParetoFront.ParetoSolution;

/**
 * The interface <code>IParetoSelector</code> defines a selection process that will return a unique solution from a
 * pareto front
 * <p>
 * Creation date: Dec 13, 2011 - 1:48:06 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public interface IParetoSelector {

    /**
     * Select a unique solution from a Pareto front
     * 
     * @param pareto
     *            the Pareto front
     * @return a unique solution from {@code  pareto}
     */
    public TRSPSolution selectSolution(ParetoFront pareto);

    /**
     * Select a unique pareto solution from a Pareto front
     * 
     * @param pareto
     *            the Pareto front
     * @return a unique pareto solution from {@code  pareto}
     */
    public ParetoSolution selectParetoSolution(ParetoFront pareto);

}
