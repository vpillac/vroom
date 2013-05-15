/**
 * 
 */
package vroom.optimization.online.jmsa.vrp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.IVRPRequest;
import vroom.common.modeling.dataModel.Node;
import vroom.common.modeling.dataModel.Request;
import vroom.common.modeling.dataModel.Vehicle;
import vroom.common.modeling.util.CostCalculationDelegate;
import vroom.common.utilities.IObservable;
import vroom.common.utilities.IObserver;
import vroom.common.utilities.Update;
import vroom.common.utilities.ValueUpdate;
import vroom.optimization.online.jmsa.vrp.vrpsd.VRPSDActualRequest;

/**
 * Creation date: May 3, 2010 - 10:54:10 AM<br/>
 * <code>VRPShrunkRequest</code> is an extension of {@link VRPRequest} used to
 * represent a virtual request that aggregate the currently served requests.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los
 *         Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class VRPShrunkRequest extends VRPRequest implements IObserver,
		IObservable {

	private final LinkedList<VRPActualRequest> mShrunkNodes;

	private final double[] mDemands;

	private IVRPRequest mShrunkRequest;

	private boolean mServed;

	private double mCost;

	private final CostCalculationDelegate mCostDelegate;

	private final Vehicle mVehicle;

	/**
	 * Creates a new <code>VRPShrunkRequest</code>
	 * 
	 * @param nProd
	 */
	public VRPShrunkRequest(Vehicle vehicle, MSAVRPInstance instance) {
		super(null);
		mShrunkNodes = new LinkedList<VRPActualRequest>();
		mDemands = new double[vehicle.getCompartmentCount()];
		mServed = true;
		mCost = 0;
		mCostDelegate = instance.getCostDelegate();
		mVehicle = vehicle;
	}

	/**
	 * Shrunk an additional request in this shrunk node
	 * 
	 * @param request
	 *            the request to be shrunk
	 */
	public synchronized void shrunkRequest(VRPActualRequest request) {
		if (!isServed()) {
			throw new IllegalStateException(
					"The last shrunk request has not been served");
		}

		double[] oldDemands = Arrays.copyOf(mDemands, mDemands.length);

		// Update the aggregated distance
		if (!mShrunkNodes.isEmpty()) {
			mCost += mCostDelegate.getCost(getNodeVisit(), request, mVehicle);
		}

		if (request.isDepot()) {
			mShrunkNodes.clear();
			mShrunkNodes.add(request);

			for (int p = 0; p < mDemands.length; p++) {
				mDemands[p] = 0;
			}

			updateShrunkRequest(oldDemands);

			// Prevent node repetition
		} else if (isEmpty() || !mShrunkNodes.getLast().equals(request)) {

			mShrunkNodes.add(request);
			for (int p = 0; p < mDemands.length; p++) {
				if (!(request instanceof VRPSDActualRequest)
						|| ((VRPSDActualRequest) request).isDemandKnown(p)) {
					// Ignore unknown demands to prevent infeasible shrunk node
					mDemands[p] += request.getDemand(p);
				}
			}

			mServed = false;

			updateShrunkRequest(oldDemands);

			request.addObserver(this);
		}
	}

	/**
	 * Return <code>true</code> if this shrunk node is empty
	 * 
	 * @return <code>true</code> if there is no shrunk node in this instance
	 */
	public boolean isEmpty() {
		return mShrunkNodes.isEmpty();
	}

	/**
	 * Size of the shrunk node
	 * 
	 * @return the number of request that have been shrunk in this node
	 */
	public int size() {
		return mShrunkNodes.size();
	}

	/**
	 * Getter for <code>served</code> flag
	 * 
	 * @return <code>true</code> if the last request has been served
	 */
	public boolean isServed() {
		return mServed;
	}

	/**
	 * Mark the last request as served
	 */
	public void markAsServed() {
		mServed = true;
	}

	/**
	 * Getter for the first shrunk node visit
	 * 
	 * @return the first {@link INodeVisit} of this shrunk node
	 */
	public INodeVisit getFirstNode() {
		return mShrunkNodes.getFirst();
	}

	/**
	 * Getter for the last shrunk node visit
	 * 
	 * @return the last {@link INodeVisit} of this shrunk node
	 */
	@Override
	public INodeVisit getNodeVisit() {
		return mShrunkNodes.getLast();
	}

	/**
	 * Getter for the last shrunk node
	 * 
	 * @return the last {@link Node} of this shrunk node
	 */
	@Override
	public Node getNode() {
		return super.getNode();
	}

	/**
	 * Getter for the vehicle to which this shrunk node is associated
	 * 
	 * @return the vehicle to which this shrunk node is associated
	 */
	public Vehicle getVehicle() {
		return mVehicle;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see vroom.optimization.online.jmsa.vrp.VRPMSARequest#getParentRequest()
	 */
	@Override
	public IVRPRequest getParentRequest() {
		return mShrunkRequest;
	}

	/**
	 * Getter for the aggregated cost of this shrunk node
	 * 
	 * @return the sum of the cost between the shrunk nodes
	 */
	public double getAggregatedCost() {
		return mCost;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see vroom.optimization.online.jmsa.vrp.VRPMSARequest#getDemand()
	 */
	@Override
	public double getDemand() {
		return getDemand(0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see vroom.optimization.online.jmsa.vrp.VRPMSARequest#getDemand(int)
	 */
	@Override
	public double getDemand(int product) {
		return mDemands[product];
	}

	/**
	 * Getter for the total demand of this node
	 * 
	 * @return a copy of the demands array
	 */
	public double[] getDemands() {
		return Arrays.copyOf(mDemands, mDemands.length);
	}

	/**
	 * Getter for the list of shrunk nodes
	 * 
	 * @return a copy of the shrunk node list
	 */
	public List<VRPActualRequest> getShrunkNodes() {
		return new ArrayList<VRPActualRequest>(mShrunkNodes);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see vroom.common.utilities.Cloneable#cloneObject()
	 */
	@Override
	public INodeVisit clone() {
		throw new UnsupportedOperationException("Cannot clone a shrunk request");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see vroom.common.utilities.IObserver#update(vroom.common.utilities
	 * .IObservable, java.lang.Object)
	 */
	@Override
	public void update(IObservable source, Update update) {
		if (source instanceof VRPSDActualRequest
				&& update instanceof ValueUpdate) {
			// VRPSDActualRequest req = (VRPSDActualRequest) source;
			// double[] oldDem = (double[]) ((ValueUpdate)
			// update).getOldValue();
			// double[] newDem = (double[]) ((ValueUpdate)
			// update).getNewValue();
			//
			// double[] oldCDemands = Arrays.copyOf(this.mDemands,
			// this.mDemands.length);
			//
			// for (int p = 0; p < this.mDemands.length; p++) {
			// if (req.isDemandKnown(p)) {
			// if (!req.equals(mShrunkNodes.getLast())) {
			// // assumes that the demand was ignored at first
			// this.mDemands[p] -= oldDem[p];
			// }
			// this.mDemands[p] += newDem[p];
			// }
			// }
			//
			// updateShrunkRequest(oldCDemands);
			updateDemands();
		}
	}

	/**
	 * Recalculate the accumulated demands.
	 * <p>
	 * Note that {@link VRPSDActualRequest} which demand is not known yet will
	 * have a demand of 0
	 * </p>
	 */
	protected void updateDemands() {
		double[] oldCDemands = Arrays.copyOf(mDemands, mDemands.length);

		for (int p = 0; p < mDemands.length; p++) {
			mDemands[p] = 0;
		}

		for (VRPActualRequest r : mShrunkNodes) {
			for (int p = 0; p < mDemands.length; p++) {
				if (!(r instanceof VRPSDActualRequest)
						|| ((VRPSDActualRequest) r).isDemandKnown(p)) {
					mDemands[p] += r.getDemand(p);
				}
			}
		}

		updateShrunkRequest(oldCDemands);
	}

	/**
	 * Update the underlying shrunk request
	 * 
	 * @param oldDemands
	 *            the previous demands
	 */
	protected void updateShrunkRequest(double[] oldDemands) {
		boolean changed = false;

		for (int p = 0; p < oldDemands.length; p++) {
			if (oldDemands[p] != getDemand(p)) {
				changed = true;
				break;
			}
		}

		mShrunkRequest = new Request(-mShrunkNodes.getLast().getID(),
				mShrunkNodes.getLast().getNode());

		if (mShrunkNodes.getLast().isDepot()) {
			for (int p = 0; p < mDemands.length; p++) {
				mDemands[p] = 0;
			}
		}

		mShrunkRequest.setDemands(mDemands);

		// Ignore if no change
		if (changed) {
			notifyObservers(new ValueUpdate(PROP_DEMANDS, oldDemands, mDemands));
		}
	}

	/**
	 * A shrunk node is always considered to be fixed
	 * 
	 * @return <code>true</code> in any case
	 */
	@Override
	public boolean isFixed() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see vroom.optimization.online.jmsa.vrp.VRPMSARequest#toString()
	 */
	@Override
	public String toString() {
		return String.format("AG:%s-%s D=%s", toShortString(), getNode()
				.getLocation(), Arrays.toString(mDemands));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see vroom.optimization.online.jmsa.vrp.VRPMSARequest#hashCode()
	 */
	@Override
	public int hashCode() {
		return superHashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * vroom.optimization.online.jmsa.vrp.VRPMSARequest#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return this == obj;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see vroom.optimization.online.jmsa.vrp.VRPMSARequest#toShortString()
	 */
	@Override
	public String toShortString() {
		StringBuilder b = new StringBuilder(mShrunkNodes.size());

		b.append('[');

		Iterator<VRPActualRequest> it = mShrunkNodes.iterator();
		while (it.hasNext()) {
			b.append(it.next().toShortString());

			if (it.hasNext()) {
				b.append(',');
			}
		}

		b.append(']');

		return b.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see vroom.optimization.online.jmsa.vrp.VRPMSARequest#isDepot()
	 */
	@Override
	public boolean isDepot() {
		return getNodeVisit() == null || getNodeVisit().isDepot();
	}

	@Override
	public int getID() {
		return mShrunkRequest != null ? mShrunkRequest.getID() : 0;
	}
}
