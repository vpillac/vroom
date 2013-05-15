package vroom.optimization.online.jmsa.events;

import vroom.optimization.online.jmsa.components.ScenarioOptimizerParam;

/**
 * <code>OptimiseEvent</code> is a non-preemptive event that triggers the reoptimization of the scenarios currently
 * present in the pool.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:07:00 a.m.
 */
public class OptimizeEvent extends MSAEvent {

    /**
     * The parameters that will be passed to the scenario optimization procedure
     **/
    private ScenarioOptimizerParam mParameters;

    /**
     * Getter for the parameters that will be passed to the scenario optimization procedure
     * 
     * @return the value of parameters
     */
    public ScenarioOptimizerParam getParameters() {
        return mParameters;
    }

    /**
     * Setter for the parameters that will be passed to the scenario optimization procedure
     * 
     * @param parameters
     *            the value to be set for parameters
     */
    public void setParameters(ScenarioOptimizerParam parameters) {
        mParameters = parameters;
    }

    protected OptimizeEvent(IMSAEventFactory source) {
        super(IMSAEventFactory.PRIORITY_OPTIMIZE, 0, source);
    }

    @Override
    public boolean isPreemptive() {
        return false;
    }
}