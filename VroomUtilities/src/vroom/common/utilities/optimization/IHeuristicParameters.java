/**
 * 
 */
package vroom.common.utilities.optimization;

/**
 * <code>IHeuristicParameters</code> is an extension of {@link IParameters} for higher level heuristics
 * <p>
 * Creation date: Jun 28, 2010 - 6:48:00 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public interface IHeuristicParameters extends IParameters {

    /**
     * Main stopping criterion used in the corresponding heuristic
     * 
     * @return an instance of {@link IStoppingCriterion} to be used as main stopping criterion of the heuristic
     */
    public IStoppingCriterion getStoppingCriterion();
}
