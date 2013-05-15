package vroom.trsp.visualization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vroom.common.modeling.dataModel.Arc;
import vroom.common.modeling.dataModel.Depot;
import vroom.common.modeling.dataModel.IArc;
import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.IVRPRequest;
import vroom.common.modeling.dataModel.Node;
import vroom.common.modeling.dataModel.NodeVisit;
import vroom.common.modeling.dataModel.attributes.ITimeWindow;
import vroom.common.utilities.IObservable;
import vroom.common.utilities.IObserver;
import vroom.common.utilities.ObserverManager;
import vroom.common.utilities.Update;
import vroom.trsp.datamodel.ITRSPTour;
import vroom.trsp.datamodel.ITourIterator;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPRequest;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.datamodel.TRSPTour;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

/**
 * The Class <code>TRSPInstanceGraph</code> is a specialization of
 * {@link DirectedSparseGraph} which can be used to represent an
 * {@link TRSPInstance} graphically by using the Jung library.
 * <p>
 * Creation date: March 23, 2011 - 1:45:52 PM.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los
 *         Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TRSPInstanceGraph extends DirectedSparseGraph<INodeVisit, IArc>
		implements IObserver, IObservable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The Obs handler. */
	private final ObserverManager mObsHandler;
	/** The Instance. */
	private TRSPInstance mInstance;

	private Map<Integer, INodeVisit> mNodeVisits;

	/**
	 * Creates a new <code>TRSPInstanceGraph</code>.
	 * 
	 * @param instance
	 *            the instance to be represented
	 */
	public TRSPInstanceGraph(TRSPInstance instance) {
		super();
		mInstance = instance;
		mObsHandler = new ObserverManager(this);
		mNodeVisits = new HashMap<Integer, INodeVisit>();

		// Add all nodes and depots
		for (Depot n : getInstance().getDepots()) {
			INodeVisit v = new NodeVisit(n);
			mNodeVisits.put(n.getID(), v);
			mNodeVisits.put(getInstance().getHomeDuplicate(n.getID()), v);
			addVertex(v);
		}

		for (TRSPRequest n : getInstance().getRequests()) {
			final TRSPRequest r = n;
			INodeVisit v = new INodeVisit() {
				@Override
				public int getID() {
					return r.getID();
				}

				@Override
				public int compareTo(INodeVisit o) {
					return o.getID() - getID();
				}

				@Override
				public boolean isPickup() {
					return false;
				}

				@Override
				public boolean isFixed() {
					return false;
				}

				@Override
				public boolean isDepot() {
					return false;
				}

				@Override
				public List<INodeVisit> getSuccessors() {
					return null;
				}

				@Override
				public List<INodeVisit> getPredecesors() {
					return null;
				}

				@Override
				public IVRPRequest getParentRequest() {
					return null;
				}

				@Override
				public Node getNode() {
					return r.getNode();
				}

				@Override
				public double getDemand() {
					return getDemand(0);
				}

				@Override
				public double getDemand(int product) {
					return r.getSparePartRequirement(product);
				}

				@Override
				public void free() {
				}

				@Override
				public void fix() {
				}

				@Override
				public INodeVisit clone() {
					throw new UnsupportedOperationException();
				}

				@Override
				public double getServiceTime() {
					throw new UnsupportedOperationException();
				}

				@Override
				public ITimeWindow getTimeWindow() {
					throw new UnsupportedOperationException();
				}
			};
			mNodeVisits.put(n.getID(), v);
			addVertex(v);
		}

	}

	/**
	 * Add the arcs from a tour to this graph.
	 * 
	 * @param tour
	 *            the tour which arcs will be added
	 */
	public void addTour(ITRSPTour tour) {
		ITourIterator it = tour.iterator();

		int pred = it.next(), succ = -1;
		while (it.hasNext()) {
			succ = it.next();
			addEdge(new Arc(getNodeVisit(pred), getNodeVisit(succ),
					getInstance().getCostDelegate().getDistance(pred, succ),
					true), getNodeVisit(pred), getNodeVisit(succ));
			pred = succ;
		}
	}

	/**
	 * Add the arcs from a solution to this graph.
	 * 
	 * @param solution
	 *            the solution which arcs will be added
	 */
	public void addSolution(TRSPSolution solution) {
		for (TRSPTour tour : solution)
			addTour(tour);

	}

	private INodeVisit getNodeVisit(int id) {
		return mNodeVisits.get(id);
	}

	/**
	 * Gets the single instance of DynamicInstanceGraph.
	 * 
	 * @return single instance of DynamicInstanceGraph
	 */
	public TRSPInstance getInstance() {
		return mInstance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * vroom.common.utilities.IObserver#update(vroom.common.utilities.IObservable
	 * , vroom.common.utilities.Update)
	 */
	@Override
	public void update(IObservable source, Update update) {
		notifyObservers(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * vroom.common.utilities.IObservable#addObserver(vroom.common.utilities
	 * .IObserver)
	 */
	@Override
	public void addObserver(IObserver o) {
		mObsHandler.addObserver(o);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * vroom.common.utilities.IObservable#removeObserver(vroom.common.utilities
	 * .IObserver)
	 */
	@Override
	public void removeObserver(IObserver o) {
		mObsHandler.removeObserver(o);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see vroom.common.utilities.IObservable#removeAllObservers()
	 */
	@Override
	public void removeAllObservers() {
		mObsHandler.removeAllObservers();
	}

	/**
	 * Notify observers.
	 * 
	 * @param update
	 *            the update
	 */
	protected void notifyObservers(Update update) {
		mObsHandler.notifyObservers(update);
	}

	public void detach() {
		mInstance = null;

	}

}