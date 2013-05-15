package vroom.common.heuristics;

import vroom.common.heuristics.vls.IVLSState;
import vroom.common.heuristics.vls.VLSGlobalParameters;
import vroom.common.utilities.optimization.IInstance;
import vroom.common.utilities.optimization.IParameters;
import vroom.common.utilities.optimization.ISolution;

/**
 * <code>IInitialization</code> is the interface for all classes that will responsible for the initialization of a new
 * mSolution in a VLS procedure.
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
public interface IInitialization<S extends ISolution> {

    /**
     * Initialize a new mSolution.
     * 
     * @param state
     *            the current state of the local search
     * @param instance
     *            the problem instance
     * @param params
     *            optional parameters
     * @return the s
     */
    public S newSolution(IVLSState<S> state, IInstance instance, IParameters params);

}