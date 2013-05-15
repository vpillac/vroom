package vroom.optimization.online.jmsa.components;

import vroom.optimization.online.jmsa.IScenario;

/**
 * <code>ScenarioOptimizerBase</code> is the base type for classes that will provide optimization procedures for scenarios<br/>
 * The implementation of this method should consider the possible paralelization of scenario optimization
 * 
 * @param S
 *            the type of scenario that will be optimized by instances of this class
 * @param I
 *            the type of instance that will be used by instances of this class.
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a href="http://copa.uniandes.edu.co">Copa</a>, <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:06:53 a.m.
 */
public abstract class ScenarioOptimizerBase<S extends IScenario> extends MSAComponentBase {

    public ScenarioOptimizerBase(ComponentManager<S, ?> componentManager) {
        super(componentManager);
    }

    /**
     * Initialize a single scenario.
     * 
     * @param scenario
     *            the scenario to be initialized
     * @param params
     *            parameters for the scenario optimization
     * @return <code>true</code> if the optimization procedure finished correctly, <code>false</code> otherwise
     */
    public abstract boolean initialize(S scenario, ScenarioOptimizerParam params);

    /**
     * Optimize a single scenario.
     * 
     * @param params
     *            an optional parameter for the optimization of scenarios
     * @param scenario
     *            the scenario to be optimized
     * @return <code>true</code> if the optimization procedure finished correctly, <code>false</code> otherwise
     */
    public abstract boolean optimize(S scenario, ScenarioOptimizerParam params);
}