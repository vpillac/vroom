package vroom.common.modeling.dataModel;

import java.util.LinkedList;
import java.util.List;

import vroom.common.modeling.dataModel.attributes.Duration;
import vroom.common.modeling.dataModel.attributes.ITimeWindow;
import vroom.common.modeling.dataModel.attributes.NodeAttributeKey;
import vroom.common.modeling.dataModel.attributes.RequestAttributeKey;
import vroom.common.utilities.Utilities;

/**
 * <code>NodeVisit</code> is a wrapper class used to add information on the
 * {@link Node} when it is visited by a route.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los
 *         Andes</a> - <a href="http://copa.uniandes.edu.co">Copa</a>, <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:07:26 a.m.
 */
public class NodeVisit implements INodeVisit {

	/** The node visited. */
	private final Node mNode;

	/**
	 * The request corresponding to the wrapped node, <code>null</code> if the
	 * node is a depot.
	 * 
	 * @see #mNode
	 */
	private final IVRPRequest mParentRequest;

	/** a list of predecessors **/
	private final List<INodeVisit> mPredecesors;

	/*
	 * (non-Javadoc)
	 * 
	 * @see vroom.common.modeling.dataModel.INodeVisit#getPredecesors()
	 */
	@Override
	public List<INodeVisit> getPredecesors() {
		return mPredecesors;
	}

	/** a list of successors **/
	private final List<INodeVisit> mSuccessors;

	/*
	 * (non-Javadoc)
	 * 
	 * @see vroom.common.modeling.dataModel.INodeVisit#getSuccessors()
	 */
	@Override
	public List<INodeVisit> getSuccessors() {
		return mSuccessors;
	}

	/**
	 * a flag defining whether this visit is a pickup or delivery for PD
	 * problems
	 **/
	private final boolean mPickup;

	/*
	 * (non-Javadoc)
	 * 
	 * @see vroom.common.modeling.dataModel.INodeVisit#isPickup()
	 */
	@Override
	public boolean isPickup() {
		return mPickup;
	}

	/** The fixed flag */
	private boolean mFixed;

	/*
	 * (non-Javadoc)
	 * 
	 * @see vroom.common.modeling.dataModel.INodeVisit#isFixed()
	 */
	@Override
	public boolean isFixed() {
		return mFixed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see vroom.common.modeling.dataModel.INodeVisit#fix()
	 */
	@Override
	public void fix() {
		mFixed = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see vroom.common.modeling.dataModel.INodeVisit#free()
	 */
	@Override
	public void free() {
		mFixed = false;
	}

	/**
	 * Creates a new <code>NodeVisit</code> associated with a depot.
	 * 
	 * @param depot
	 *            the depot that is associated with this visit
	 */
	public NodeVisit(Depot depot) {
		mParentRequest = null;
		mNode = depot;
		mPredecesors = new LinkedList<INodeVisit>();
		mSuccessors = new LinkedList<INodeVisit>();
		mPickup = true;
		fix();
	}

	/**
	 * Creates a new <code>NodeVisit</code>
	 * 
	 * @param node
	 * @param parentRequest
	 * @param predecesors
	 * @param successors
	 */
	protected NodeVisit(Node node, IVRPRequest parentRequest, boolean pickup) {
		mNode = node;
		mParentRequest = parentRequest;
		mPredecesors = new LinkedList<INodeVisit>();
		mSuccessors = new LinkedList<INodeVisit>();
		mPickup = pickup;
		free();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see vroom.common.modeling.dataModel.INodeVisit#getNode()
	 */
	@Override
	public Node getNode() {
		return mNode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see vroom.common.modeling.dataModel.INodeVisit#getParentRequest()
	 */
	@Override
	public IVRPRequest getParentRequest() {
		return mParentRequest;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see vroom.common.modeling.dataModel.INodeVisit#isDepot()
	 */
	@Override
	public boolean isDepot() {
		return mNode instanceof Depot;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (mParentRequest != null) {
			return String.format("%s[r:%s]", mParentRequest
					.isOriginDestination() ? isPickup() ? "P" : "D" : "",
					mParentRequest);
		} else {
			return mNode.toString();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(INodeVisit o) {
		return getNode() != null && o.getNode() != null ? getNode().getID()
				- o.getNode().getID() : 0;
	}

	@Override
	public NodeVisit clone() {
		NodeVisit clone;
		if (isDepot()) {
			clone = new NodeVisit((Depot) getNode());
		} else {
			clone = new NodeVisit(getNode(), getParentRequest(), isPickup());
		}

		return clone;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return getID();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof INodeVisit)) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		INodeVisit v = (INodeVisit) obj;
		return Utilities.equal(mParentRequest, v.getParentRequest())
				&& Utilities.equal(mNode, v.getNode());
	}

	/**
	 * Factory method to create the node visits associated with a request.
	 * 
	 * @param request
	 * @return an array containing the corresponding node visit, or the node
	 *         visit of the pickup and the node visit of teh delivery
	 */
	public static NodeVisit[] createNodeVisits(IVRPRequest request) {
		NodeVisit o = new NodeVisit(request.getNode(), request, true);
		if (request.isOriginDestination()) {
			NodeVisit d = new NodeVisit(request.getDestinationNode(), request,
					false);

			o.getSuccessors().add(d);
			d.getPredecesors().add(o);

			return new NodeVisit[] { o, d };
		} else {
			return new NodeVisit[] { o };
		}
	}

	/**
	 * Factory method to create the node visit associated with a request.
	 * 
	 * @param request
	 * @return the corresponding node visit
	 */
	public static NodeVisit createNodeVisit(IVRPRequest request) {
		return new NodeVisit(request.getNode(), request, true);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see vroom.common.utilities.dataModel.IObjectWithID#getID()
	 */
	@Override
	public int getID() {
		if (isDepot()) {
			return getNode().getID();
		} else {
			int id = getParentRequest().getID();
			return isPickup() ? id : -id;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see vroom.common.modeling.dataModel.INodeVisit#getDemand(int)
	 */
	@Override
	public double getDemand(int product) {
		return getParentRequest() != null ? getParentRequest().getDemand(
				product) : 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see vroom.common.modeling.dataModel.INodeVisit#getDemand()
	 */
	@Override
	public double getDemand() {
		return getParentRequest() != null ? getParentRequest().getDemand() : 0;
	}

	@Override
	public double getServiceTime() {
		if (getParentRequest() == null)
			return 0;
		Duration st = getParentRequest().getAttribute(
				RequestAttributeKey.SERVICE_TIME);
		return st != null ? st.getDuration() : 0;
	}

	@Override
	public ITimeWindow getTimeWindow() {
		ITimeWindow tw;
		if (getParentRequest().isOriginDestination()) {
			tw = getNode().getAttribute(NodeAttributeKey.TIME_WINDOW);
			if (tw == null)
				tw = getParentRequest().getAttribute(
						NodeAttributeKey.TIME_WINDOW);
		} else
			tw = getParentRequest().getAttribute(NodeAttributeKey.TIME_WINDOW);

		return tw;
	}
}// end NodeVisit