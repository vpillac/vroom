package vroom.optimization.online.jmsa.components;

import vroom.optimization.online.jmsa.IActualRequest;
import vroom.optimization.online.jmsa.IDistinguishedSolution;
import vroom.optimization.online.jmsa.IMSARequest;
import vroom.optimization.online.jmsa.IScenario;

/**
 * <code>PoolUpdaterBase</code> is the base type for classes that are responsible for inserting a new request in the
 * existing compatible scenarios, and remove incompatible scenarios
 * 
 * @param S
 *            the type of scenario that will be updated by instances of this class
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:06:51 a.m.
 */
public abstract class ScenarioUpdaterBase extends MSAComponentBase {

    public ScenarioUpdaterBase(ComponentManager<?, ?> componentManager) {
        super(componentManager);
    }

    /**
     * Perform the update of the given <code>scenario</code> by trying to insert the <code>request</code>
     * 
     * @param scenario
     *            the scenario in which the <code>request</code> will be insterted
     * @param request
     *            the request that will be inserted in the given <code>scenario</code>
     * @return <code>true</code> if the <code>request</code> was successfully inserted in the given
     *         <code>scenario</code>, <code>false</code> otherwise
     */
    public abstract boolean insertRequest(IScenario scenario, IMSARequest request);

    /**
     * Perform the update of the given <code>scenario</code> by inserting all requests from <code>requests</code>
     * 
     * @param scenario
     *            the scenario in which the <code>requests</code> will be insterted
     * @param requests
     *            the requests that will be inserted in the given <code>scenario</code>
     * @return <code>true</code> if the <code>requests</code> where successfully inserted in the given
     *         <code>scenario</code>, <code>false</code> otherwise
     */
    public final boolean insertRequests(IScenario scenario, IMSARequest... requests) {
        boolean b = true;
        for (IMSARequest r : requests) {
            b &= insertRequest(scenario, r);
        }

        return b;
    }

    /**
     * Enforce a decision by committing all resources from scenario to their corresponding first request of decision
     * 
     * @param scenario
     *            the scenario to be updated
     * @param decision
     *            the {@link IDistinguishedSolution} that is being enforced
     * @return <code>true</code> if the scenario is coherent with the <code>decision</code>, and <code>false</code> if
     *         it is not and should therefore be discarded
     * @see #enforceDecision(IScenario, IActualRequest, int)
     */
    public boolean enforceDecision(IScenario scenario, IDistinguishedSolution decision) {
        boolean b = true;

        for (int r = 0; r < scenario.getResourceCount(); r++) {
            b &= enforceDecision(scenario, decision.getNextRequest(r), r);
        }

        return b;
    }

    /**
     * Enforce a decision by committing the specified resource to the given request
     * 
     * @param scenario
     *            the scenario to be updated
     * @param request
     *            the request that will be served next by the specified resource
     * @param resourceId
     *            the id (index) of the resource that will be committed to <code>request</code>
     * @return <code>true</code> if the scenario is coherent with the <code>decision</code>, <em>nodeI.e.</em> if the
     *         specified resource can be committed to the given <code>request</code>, and <code>false</code> if it is
     *         not and should therefore be discarded
     */
    public abstract boolean enforceDecision(IScenario scenario, IActualRequest request,
            int resourceId);

    /**
     * Update a scenario after the start of servicing by a resource
     * 
     * @param scenario
     *            the scenario to be updated
     * @param resourceId
     *            the id (index) of the considered resource
     * @return <code>true</code> if the scenario is coherent with the new state of the specified resource
     */
    public abstract boolean startServicingUpdate(IScenario scenario, int resourceId);

    /**
     * Update a scenario after the end of servicing by a resource
     * 
     * @param scenario
     *            the scenario to be updated
     * @param resourceId
     *            the id (index) of the considered resource
     * @return <code>true</code> if the scenario is coherent with the new state of the specified resource
     */
    public abstract boolean stopServicingUpdate(IScenario scenario, int resourceId);

    /**
     * Update a scenario when a resource started the service of a request
     * 
     * @param scenario
     *            the scenario to be updated
     * @param resourceId
     *            the id (index) of the considered resource
     * @param request
     *            the request that is being served by the specified resource
     * @return <code>true</code> if the scenario is coherent with the servicing of the given request by the specified
     *         resource.
     */
    public abstract boolean startOfServiceUpdate(IScenario scenario, int resourceId,
            IActualRequest request);

    /**
     * Update a scenario after the servicing of a request by a resource
     * 
     * @param scenario
     *            the scenario to be updated
     * @param resourceId
     *            the id (index) of the considered resource
     * @param servedRequest
     *            the request that has been served by the specified resource
     * @return <code>true</code> if the scenario is coherent with the servicing of the given request by the specified
     *         resource.
     */
    public abstract boolean endOfServiceUpdate(IScenario scenario, int resourceId,
            IActualRequest servedRequest);

}