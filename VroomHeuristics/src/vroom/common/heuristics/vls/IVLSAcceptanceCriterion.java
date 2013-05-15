package vroom.common.heuristics.vls;

import vroom.common.utilities.optimization.IInstance;
import vroom.common.utilities.optimization.ISolution;

/**
 * <code>IVLSAcceptanceCriterion</code> is an interface for classes that will decide whether a solution should be accepted or not.
 * <p/>
 * Implementations should declare a constructor taking an instance of {@link VLSGlobalParameters} as unique argument
 * <p>
 * Creation date: 26-Abr-2010 10:11:54 a.m.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public interface IVLSAcceptanceCriterion {

    /**
     * Test whether a mSolution should be accepted or not.
     * 
     * @param state
     *            the current state of the local search
     * @param instance
     *            the problem instance
     * @param mSolution
     *            the mSolution to be tested
     * @return <code>true</code> if the given mSolution is to be accepted
     */
    public boolean acceptSolution(IVLSState<?> state, IInstance instance, ISolution solution);

}