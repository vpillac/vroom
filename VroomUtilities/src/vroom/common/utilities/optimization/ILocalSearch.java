package vroom.common.utilities.optimization;

import vroom.common.utilities.IDisposable;

/**
 * <code>ILocalSearch</code> is the interface for all classes that will perform a local search on a mSolution.
 * <p>
 * Creation date: 26-Abr-2010 10:11:52 a.m
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 2.0
 */
public interface ILocalSearch<S extends ISolution> extends IDisposable {

    /**
     * Perform a local search on a given solution.
     * 
     * @param instance
     *            the instance on which information will be read
     * @param solution
     *            the solution on which the local search will be performed
     * @param param
     *            the current state of the VLS search
     * @return the new solution found by this procedure
     */
    public S localSearch(IInstance instance, S solution, IParameters param);

}