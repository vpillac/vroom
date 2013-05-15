/**
 * 
 */
package vroom.optimization.online.jmsa.components;

import vroom.optimization.online.jmsa.IActualRequest;
import vroom.optimization.online.jmsa.IScenario;
import vroom.optimization.online.jmsa.MSAGlobalParameters;
import vroom.optimization.online.jmsa.utils.MSALogging;

/**
 * Creation date: Mar 2, 2010 - 11:06:16 AM<br/>
 * <code>RequestValidatorBase</code> is the class responsible for the checking of a new request against the current
 * scenario pool. It is used to determine whether a request can be serviced or not.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public abstract class RequestValidatorBase extends MSAComponentBase {

    /**
     * Creates a new <code>RequestValidatorBase</code>
     * 
     * @param componentManager
     */
    public RequestValidatorBase(ComponentManager<?, ?> componentManager) {
        super(componentManager);
    }

    /**
     * Validation of a request
     * 
     * @param request
     *            the request that has to be validated
     * @return <code>true</code> if the request can be serviced, <code>false</code> otherwise
     */
    public boolean canBeServiced(IActualRequest request) {
        double minCompScen = getComponentManager().getParentMSA().getParameters()
                .get(MSAGlobalParameters.MIN_COMPATIBLE_SCEN_PROP)
                * getComponentManager().getParentMSAProxy().getScenarioPool().size();

        int compScen = 0;
        for (IScenario s : getComponentManager().getParentMSAProxy().getScenarioPool()) {
            compScen += isScenarioCompatible(s, request) ? 1 : 0;
            if (compScen >= minCompScen) {
                break;
            }
        }

        MSALogging
                .getComponentsLogger()
                .info("RequestValidatorBase.canBeServiced: Request:%s, number of compatible scenarios: %s, minimum required: %s",
                        request, compScen, minCompScen);

        return compScen >= minCompScen;
    }

    /**
     * Compatibility test between a scenario and a request
     * 
     * @param scenario
     *            the considered scenario
     * @param request
     *            the request to be tested againg <code>scenario</code>
     * @return <code>true</code> if the given <code>scenario</code> can accommodate the given <code>request</code>,
     *         <code>false</code> otherwise
     */
    public abstract boolean isScenarioCompatible(IScenario scenario, IActualRequest request);

    /**
     * This method is called whenever a request is accepted by the MSA procedure
     * 
     * @param request
     *            the accepted request
     */
    public void requestAccepted(IActualRequest request) {

    }

    /**
     * This method is called whenever a request is rejected by the MSA procedure
     * 
     * @param request
     *            the rejected request
     */
    public void requestRejected(IActualRequest request) {

    }
}
