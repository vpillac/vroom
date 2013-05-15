/**
 * 
 */
package vroom.optimization.online.jmsa.vrp;

import vroom.common.modeling.dataModel.IRoute;
import vroom.optimization.online.jmsa.IActualRequest;
import vroom.optimization.online.jmsa.IScenario;
import vroom.optimization.online.jmsa.components.ComponentManager;
import vroom.optimization.online.jmsa.components.RequestValidatorBase;

/**
 * Creation date: Mar 4, 2010 - 11:25:00 AM<br/>
 * <code>VRPRequestValidatorBase</code> is a base implementation of {@link RequestValidatorBase} for VRP problems
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class VRPRequestValidatorBase extends RequestValidatorBase {

    /**
     * Creates a new <code>VRPRequestValidatorBase</code>
     * 
     * @param componentManager
     */
    public VRPRequestValidatorBase(ComponentManager<?, ?> componentManager) {
        super(componentManager);
    }

    @Override
    public boolean isScenarioCompatible(IScenario scenario, IActualRequest request) {
        scenario.acquireLock();
        for (IRoute<?> r : (VRPScenario) scenario) {
            if (r.canAccommodateRequest(((VRPActualRequest) request).getWrappedObject())) {
                return true;
            }
        }
        scenario.releaseLock();
        return false;
    }

}
