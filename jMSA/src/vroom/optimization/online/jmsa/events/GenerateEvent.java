package vroom.optimization.online.jmsa.events;

import vroom.optimization.online.jmsa.components.ScenarioGeneratorParam;

/**
 * <code>GenerateEvent</code> is a non-preemptive event that triggers the generation of new scenarios
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:07:03 a.m.
 */
public class GenerateEvent extends MSAEvent {

    /** The parameters that will be passed to the scenario generation procedure **/
    private ScenarioGeneratorParam mParameters;

    /**
     * Getter for the parameters that will be passed to the scenario generation procedure
     * 
     * @return the value of parameters
     */
    public ScenarioGeneratorParam getParameters() {
        return mParameters;
    }

    /**
     * Setter for the parameters that will be passed to the scenario generation procedure
     * 
     * @param parameters
     *            the value to be set for parameters
     */
    public void setParameters(ScenarioGeneratorParam parameters) {
        mParameters = parameters;
    }

    protected GenerateEvent(IMSAEventFactory source) {
        super(IMSAEventFactory.PRIORITY_GENERATE, 0, source);
    }

    @Override
    public boolean isPreemptive() {
        return false;
    }

}// end GenerateEvent