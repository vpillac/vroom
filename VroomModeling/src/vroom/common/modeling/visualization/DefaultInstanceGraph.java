package vroom.common.modeling.visualization;

import java.util.ListIterator;

import vroom.common.modeling.dataModel.Arc;
import vroom.common.modeling.dataModel.IArc;
import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.IRoute;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.dataModel.IVRPSolution;
import vroom.common.utilities.IObservable;
import vroom.common.utilities.IObserver;
import vroom.common.utilities.ObserverManager;
import vroom.common.utilities.Update;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

/**
 * The Class <code>DefaultInstanceGraph</code> is a specialization of {@link DirectedSparseGraph} which can be used to
 * represent an {@link IVRPInstance} graphically by using the Jung library.
 * <p>
 * Creation date: Sep 29, 2010 - 10:34:52 AM.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class DefaultInstanceGraph extends DirectedSparseGraph<INodeVisit, IArc> implements
        IObserver, IObservable {

    /** The Constant serialVersionUID. */
    private static final long     serialVersionUID = 1L;

    /** The Obs handler. */
    private final ObserverManager mObsHandler;
    /** The Instance. */
    private IVRPInstance          mInstance;

    /**
     * Creates a new <code>DefaultInstanceGraph</code>.
     * 
     * @param instance
     *            the instance to be represented
     */
    public DefaultInstanceGraph(IVRPInstance instance) {
        super();
        mInstance = instance;
        mObsHandler = new ObserverManager(this);

        // Add all nodes and depots
        for (INodeVisit n : getInstance().getNodeVisits()) {
            addVertex(n);
        }

        for (INodeVisit n : getInstance().getDepotsVisits()) {
            addVertex(n);
        }

    }

    /**
     * Add the arcs from a solution to this graph.
     * 
     * @param solution
     *            the solution which arcs will be added
     */
    public void addSolution(IVRPSolution<?> solution) {
        for (IRoute<?> r : solution) {
            ListIterator<? extends INodeVisit> it = r.iterator();

            INodeVisit pred = it.next(), succ = null;
            while (it.hasNext()) {
                succ = it.next();
                addEdge(new Arc(pred, succ, getInstance().getCost(pred, succ), true), pred, succ);
                pred = succ;
            }
        }
    }

    /**
     * Gets the single instance of DynamicInstanceGraph.
     * 
     * @return single instance of DynamicInstanceGraph
     */
    public IVRPInstance getInstance() {
        return mInstance;
    }

    /*
     * (non-Javadoc)
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