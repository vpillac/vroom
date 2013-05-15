package vroom.optimization.online.jmsa.components;

import vroom.optimization.online.jmsa.IDistinguishedSolution;

/**
 * <code>SolutionBuilderBase</code> is the base type for classes that are responsible for the creation of a
 * distinguished solution based on the scenario pool
 * 
 * @param S
 *            the type of scenario that will be handled by instances of this class
 * @param D
 *            the type of the mSolution to build
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:06:53 a.m.
 */
public abstract class SolutionBuilderBase extends MSAComponentBase {

    public SolutionBuilderBase(ComponentManager<?, ?> componentManager) {
        super(componentManager);
    }

    /**
     * Building of a distinguished mSolution from the given <code>pool</code> and based on the given
     * <code>instance</code>
     * 
     * @param param
     *            an optional parameter for the building of the distinguished plan
     */
    public abstract IDistinguishedSolution buildDistinguishedPlan(ISolutionBuilderParam param);

}