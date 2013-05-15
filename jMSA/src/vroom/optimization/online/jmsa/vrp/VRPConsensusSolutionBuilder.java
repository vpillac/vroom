/*
 * 
 */
package vroom.optimization.online.jmsa.vrp;

import vroom.optimization.online.jmsa.DistinguishedSolutionBase;
import vroom.optimization.online.jmsa.IActualRequest;
import vroom.optimization.online.jmsa.IDistinguishedSolution;
import vroom.optimization.online.jmsa.components.ComponentManager;
import vroom.optimization.online.jmsa.components.ConsensusSolutionBuilder;
import vroom.optimization.online.jmsa.components.ISolutionBuilderParam;

/**
 * <code>VRPConsensusSolutionBuilder</code> is a base implementation of the
 * Consensus algorithm introduced by Bent and Van Hentenryck (2004) for VRP
 * problems.
 * <p>
 * Creation date: Sep 9, 2010 - 11:29:50 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los
 *         Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class VRPConsensusSolutionBuilder extends ConsensusSolutionBuilder {

	/**
	 * Creates a new <code>VRPConsensusSolutionBuilder</code>
	 * 
	 * @param componentManager
	 */
	public VRPConsensusSolutionBuilder(ComponentManager<?, ?> componentManager) {
		super(componentManager);
	}

	@Override
	public IDistinguishedSolution buildDistinguishedPlan(
			ISolutionBuilderParam param) {
		IDistinguishedSolution s = super.buildDistinguishedPlan(param);

		// No suitable request was found, force return to the first depot
		if (s.getNextRequest() == null) {
			s = new DistinguishedSolutionBase(new VRPActualRequest(
					getInstance().getDepotsVisits().iterator().next()));
		}

		return s;
	}

	@Override
	protected boolean isRequestFeasible(IActualRequest req) {
		boolean feasible = true;
		VRPActualRequest r = (VRPActualRequest) req;

		for (int p = 0; p < getInstance().getFleet().getVehicle()
				.getCompartmentCount(); p++) {
			if (r.getDemand(p) + getInstance().getCurrentLoad(0, p) > getInstance()
					.getFleet().getVehicle().getCapacity(p)) {
				feasible = false;
				break;
			}
		}

		return super.isRequestFeasible(req) && feasible;
	}

	protected MSAVRPInstance getInstance() {
		return (MSAVRPInstance) getComponentManager().getParentMSAProxy()
				.getInstance();
	}
}
