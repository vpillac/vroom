package vroom.common.modeling.dataModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vroom.common.modeling.util.CostCalculationDelegate;

/**
 * <code>StaticInstance</code> is an extension of {@link InstanceBase} that
 * represents a static instance of a VRP problem.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los
 *         Andes</a> - <a href="http://copa.uniandes.edu.co">Copa</a>, <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #created 15-Feb-2010 11:29:51 a.m.
 */
public class StaticInstance extends InstanceBase {

	/** The requests. */
	private final HashMap<Integer, IVRPRequest> mRequests;

	/**
	 * Instantiates a new static instance.
	 * 
	 * @param name
	 *            the name of this instance
	 * @param id
	 *            the id of this instance
	 * @param fleet
	 *            the fleet
	 * @param depots
	 *            the depots
	 * @param routingPronlem
	 *            the associated routing problem
	 * @param costHelper
	 *            the cost helper
	 * @see InstanceBase#InstanceBase(int, String,
	 *      VehicleRoutingProblemDefinition, Fleet, List, IPlanningPeriod,
	 *      CostCalculationDelegate)
	 */
	public StaticInstance(String name, int id, Fleet<?> fleet,
			List<Depot> depots, VehicleRoutingProblemDefinition routingPronlem,
			CostCalculationDelegate costHelper) {
		super(id, name, routingPronlem, fleet, depots, null, costHelper);
		mRequests = new HashMap<Integer, IVRPRequest>();
	}

	/**
	 * Creates a new empty <code>StaticInstance</code>
	 * 
	 * @param name
	 *            the name of this instance
	 * @param id
	 *            the id of this instance
	 * @param routingPronlem
	 *            the associated routing problem
	 * @see InstanceBase#InstanceBase(int, String,
	 *      VehicleRoutingProblemDefinition)
	 */
	public StaticInstance(String name, int id,
			VehicleRoutingProblemDefinition routingProblem) {
		super(id, name, routingProblem);
		mRequests = new HashMap<Integer, IVRPRequest>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * vroom.common.modeling.dataModel.InstanceBase#addRequest(vroom.common
	 * .modelling.dataModel.Request)
	 */
	@Override
	protected boolean addRequestInternal(IVRPRequest request) {
		mRequests.put(request.getID(), request);
		// TODO lock the instance
		return true;
	}

	// /* (non-Javadoc)
	// * @see
	// vroom.common.modeling.dataModel.InstanceBase#removeRequest(vroom.common.modeling.dataModel.Request)
	// */
	// @Override
	// public boolean removeRequest(IRequest request) {
	// return this.mRequests.remove(request);
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see vroom.common.modeling.dataModel.InstanceBase#getRequestCount()
	 */
	@Override
	public int getRequestCount() {
		return mRequests.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see vroom.common.modeling.dataModel.InstanceBase#getRequests()
	 */
	@Override
	public List<IVRPRequest> getRequests() {
		return new ArrayList<IVRPRequest>(mRequests.values());
	}

	/**
	 * Returns the mapping between ids and request
	 * 
	 * @return the mapping between ids and request
	 */
	Map<Integer, IVRPRequest> getRequestsMap() {
		return mRequests;
	}

	@Override
	public IVRPRequest getRequest(int id) {
		return mRequests.get(id);
	}
}// end StaticInstance