package vroom.optimization.online.jmsa.vrp;

import java.util.LinkedList;
import java.util.List;

import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.IVRPRequest;
import vroom.common.modeling.dataModel.Node;
import vroom.common.modeling.dataModel.attributes.ITimeWindow;
import vroom.common.utilities.IObservable;
import vroom.common.utilities.IObserver;
import vroom.common.utilities.ObserverManager;
import vroom.common.utilities.Update;
import vroom.common.utilities.Wrapper;
import vroom.optimization.online.jmsa.IActualRequest;
import vroom.optimization.online.jmsa.IMSARequest;
import vroom.optimization.online.jmsa.ISampledRequest;

/**
 * Creation date: Apr 29, 2010 - 10:22:36 AM<br/>
 * <code>VRPRequest</code>
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los
 *         Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public abstract class VRPRequest implements IMSARequest, Wrapper<IVRPRequest>,
		INodeVisit, IObservable {

	public static final String PROP_DEMANDS = "Demands";

	/** the corresponding node visit **/
	private final INodeVisit mINodeVisit;

	/**
	 * Getter for the node visit
	 * 
	 * @return the corresponding node visit
	 */
	protected INodeVisit getNodeVisit() {
		return mINodeVisit;
	}

	/**
	 * Creates a new <code>MSAVRPRequest</code>
	 * 
	 * @param nodeVisit
	 */
	protected VRPRequest(INodeVisit nodeVisit) {
		mINodeVisit = nodeVisit;
		mObservers = new ObserverManager(this);
	}

	/**
	 * Getter for the demand of the default product
	 * 
	 * @return the demand of the wrapped request
	 */
	@Override
	public double getDemand() {
		return getNodeVisit() == null ? 0 : getNodeVisit().getDemand();
	}

	/**
	 * Getter for the demand of a specific product
	 * 
	 * @param product
	 * @return the demand of the wrapped request for the specified product
	 */
	@Override
	public double getDemand(int product) {
		return getNodeVisit() == null ? 0 : getNodeVisit().getDemand(product);
	}

	@Override
	public double getServiceTime() {
		return getNodeVisit() == null ? 0 : getNodeVisit().getServiceTime();
	}

	@Override
	public ITimeWindow getTimeWindow() {
		return getNodeVisit() == null ? null : getNodeVisit().getTimeWindow();
	}

	@Override
	public IVRPRequest getWrappedObject() {
		return getParentRequest();
	}

	@Override
	public Node getNode() {
		return getNodeVisit() == null ? null : getNodeVisit().getNode();
	}

	@Override
	public IVRPRequest getParentRequest() {
		return getNodeVisit() == null ? null : getNodeVisit()
				.getParentRequest();
	}

	@Override
	public List<INodeVisit> getPredecesors() {
		return getNodeVisit() == null ? new LinkedList<INodeVisit>()
				: getNodeVisit().getPredecesors();
	}

	@Override
	public List<INodeVisit> getSuccessors() {
		return getNodeVisit().getSuccessors();
	}

	@Override
	public boolean isDepot() {
		return getNodeVisit() == null ? false : getNodeVisit().isDepot();
	}

	@Override
	public boolean isFixed() {
		return getNodeVisit() == null ? false : getNodeVisit().isFixed();
	}

	@Override
	public void fix() {
		if (getNodeVisit() != null) {
			getNodeVisit().fix();
		}
	}

	@Override
	public void free() {
		if (getNodeVisit() != null) {
			getNodeVisit().free();
		}
	}

	@Override
	public boolean isPickup() {
		return getNodeVisit() == null ? false : getNodeVisit().isPickup();
	}

	@Override
	public int compareTo(INodeVisit o) {
		return getNodeVisit() == null ? 0 : getNodeVisit().compareTo(o);
	}

	@Override
	public int getID() {
		return getNodeVisit() == null ? 0 : getNodeVisit().getID();
	}

	@Override
	public String toString() {
		return String
				.format("%s:%s", this instanceof IActualRequest ? "AR"
						: this instanceof ISampledRequest ? "SR" : "AG",
						getNodeVisit());
	}

	@Override
	public int hashCode() {
		if (!isDepot()) {
			return getNodeVisit() == null ? super.hashCode() : getNodeVisit()
					.hashCode();
		} else {
			return superHashCode();
		}
	}

	/**
	 * @return the hashcode of this object as defined in
	 *         {@link Object#hashCode()}
	 */
	protected int superHashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!isDepot()) {
			return this == obj || getNodeVisit().equals(obj);
		} else {
			return super.equals(obj);
		}
	}

	/**
	 * Short string description for this request
	 * 
	 * @return the underlying request id as a string
	 */
	public String toShortString() {
		return getNode() != null ? "" + getNode().getID() : "";
	}

	// -----------------------------------------------
	// Observable interface implementation
	// -----------------------------------------------
	private final ObserverManager mObservers;

	@Override
	public final void addObserver(IObserver o) {
		mObservers.addObserver(o);
	}

	@Override
	public final void removeAllObservers() {
		mObservers.removeAllObservers();

	}

	@Override
	public final void removeObserver(IObserver o) {
		mObservers.removeObserver(o);

	}

	protected final void notifyObservers(Update update) {
		mObservers.notifyObservers(update);
	}

	@Override
	public abstract INodeVisit clone();
}
