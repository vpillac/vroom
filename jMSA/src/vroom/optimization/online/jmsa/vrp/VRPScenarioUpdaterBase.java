package vroom.optimization.online.jmsa.vrp;

import vroom.common.modeling.dataModel.NodeInsertion;
import vroom.optimization.online.jmsa.IActualRequest;
import vroom.optimization.online.jmsa.IMSARequest;
import vroom.optimization.online.jmsa.IScenario;
import vroom.optimization.online.jmsa.components.ComponentManager;
import vroom.optimization.online.jmsa.components.ScenarioUpdaterBase;
import vroom.optimization.online.jmsa.utils.MSALogging;

/**
 * Creation date: 18-Feb-2010 10:51:14 a.m.<br/>
 * <code>VRPRequestInserterBase</code> is a basic implementation of
 * {@link ScenarioUpdaterBase} that insert a request in the best position.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los
 *         Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0, only supports single node Requests (no pickup and delivery)
 */
public class VRPScenarioUpdaterBase extends ScenarioUpdaterBase {

	/**
	 * Creates a new component
	 * 
	 * @param componentManager
	 *            the parent component manager
	 */
	public VRPScenarioUpdaterBase(ComponentManager<?, ?> componentManager) {
		super(componentManager);

	}

	protected MSAVRPInstance getInstance() {
		return (MSAVRPInstance) getComponentManager().getParentMSAProxy()
				.getInstance();
	}

	@Override
	public boolean insertRequest(IScenario scenario, IMSARequest request) {
		scenario.acquireLock();

		VRPScenario s = (VRPScenario) scenario;
		VRPRequest r = (VRPRequest) request;

		NodeInsertion ins = s.getBestInsertion(r);

		boolean b;

		// Insert the node in the best position
		if (ins != null) {
			MSALogging
					.getComponentsLogger()
					.lowDebug(
							"VRPRequestInserterBase.insertRequest: Inserting request %s in position %s of route %s of scenario %s",
							request, ins.getPosition(), ins.getRoute(),
							scenario);
			b = ((VRPScenarioRoute) ins.getRoute()).insertNode(ins, r);
			if (r instanceof VRPActualRequest) {
				s.actualRequestAdded((VRPScenarioRoute) ins.getRoute(),
						(VRPActualRequest) r);
			}
		} else {
			b = false;
		}

		scenario.releaseLock();

		return b;
	}

	@Override
	public boolean endOfServiceUpdate(IScenario scenario, int resourceId,
			IActualRequest servedRequest) {

		boolean r = false;

		VRPScenario s = (VRPScenario) scenario;

		s.acquireLock();

		// Check that the last fixed request is the good one
		if (s.getLastFixedRequest(resourceId) == null
		// Newly generated scenarios are assumed to be coherent with the system
		// state
				|| s.getLastFixedRequest(resourceId).getID() == servedRequest
						.getID()) {
			// Mark the last request as served
			s.markLastVisitAsServed(resourceId);
			r = true;
		}

		s.releaseLock();

		return r;
	}

	@Override
	public boolean startServicingUpdate(IScenario scenario, int resourceId) {
		if (scenario instanceof VRPScenario
				&& resourceId < ((VRPScenario) scenario).getRouteCount()) {
			scenario.acquireLock();

			if (((VRPScenario) scenario).getRoute(resourceId).getFirstNode()
					.isDepot()) {
				// Fix the first request (depot)
				((VRPScenario) scenario).fixFirstActualRequest(resourceId);
				// Mark the first request as served as the vehicle left the
				// depot
				((VRPScenario) scenario).markLastVisitAsServed(resourceId);
				scenario.releaseLock();
				return true;
			}
		}
		scenario.releaseLock();
		return false;
	}

	@Override
	public boolean stopServicingUpdate(IScenario scenario, int resourceId) {
		if (scenario instanceof VRPScenario) {

			scenario.acquireLock();
			boolean b = ((VRPScenario) scenario).getRoute(resourceId).length() == 0
					|| ((VRPScenario) scenario).getRoute(resourceId).length() == 1
					&& ((VRPScenario) scenario).getRoute(resourceId)
							.getFirstNode().isDepot();
			scenario.releaseLock();
			return b;
		} else {
			return false;
		}
	}

	@Override
	public boolean enforceDecision(IScenario scenario, IActualRequest request,
			int resourceId) {
		boolean b = false;

		scenario.acquireLock();

		if (scenario.getFirstActualRequest(resourceId) == null) {
			if (request == null) {
				return true;
			} else {
				return false;
			}
		}

		if (scenario.getFirstActualRequest(resourceId).getID() == request
				.getID()) {
			scenario.fixFirstActualRequest(resourceId);

			b = true;
		}

		scenario.releaseLock();

		return b;
	}

	@Override
	public boolean startOfServiceUpdate(IScenario scenario, int resourceId,
			IActualRequest request) {
		return true;
	}

}// end VRPRequestInserterBase