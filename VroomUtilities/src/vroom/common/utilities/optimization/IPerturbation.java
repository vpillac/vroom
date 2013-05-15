package vroom.common.utilities.optimization;


/**
 * <code>IPerturbation</code> is an interface defining perturbation/shaking operators
 * <p>
 * Creation date: 2 juil. 2010 - 21:23:54
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public interface IPerturbation<S extends ISolution> {

    /**
     * Perturb a solution.
     * <p>
     * This method will introduce random modifications to the given solution
     * 
     * @param instance
     *            the reference instance
     * @param solution
     *            the solution to be modified
     * @param parameters
     *            optional parameters
     */
    public void pertub(IInstance instance, S solution, IParameters parameters);

}
