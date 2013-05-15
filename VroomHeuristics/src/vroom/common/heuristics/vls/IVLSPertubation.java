package vroom.common.heuristics.vls;

import vroom.common.utilities.optimization.IInstance;
import vroom.common.utilities.optimization.IParameters;
import vroom.common.utilities.optimization.ISolution;

/**
 * <code>IPertubation</code> is the interface for all classes that will introduce some perturbation/variability/mutation
 * in a mSolution.
 * <p/>
 * Implementations should declare a constructor taking an instance of {@link VLSGlobalParameters} as unique argument
 * <p>
 * Creation date: 26-Abr-2010 10:11:53 a.m.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public interface IVLSPertubation<S extends ISolution> {

    /**
     * Perturb a mSolution.
     * <p/>
     * This method should introduce some random changes in the given mSolution
     * 
     * @param state
     *            the current state of the local search
     * @param instance
     *            the current instance
     * @param mSolution
     *            The mSolution to be pertubated
     * @param params
     *            optional parameters
     */
    public void pertub(IVLSState<S> state, IInstance instance, S solution, IParameters params);

}