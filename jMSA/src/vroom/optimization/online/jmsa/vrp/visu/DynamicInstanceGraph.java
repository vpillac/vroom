/**
 * 
 */
package vroom.optimization.online.jmsa.vrp.visu;

import vroom.common.modeling.dataModel.Arc;
import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.visualization.DefaultInstanceGraph;
import vroom.common.modeling.visualization.GraphUpdate;
import vroom.common.utilities.IObservable;
import vroom.common.utilities.Update;
import vroom.optimization.online.jmsa.vrp.MSAVRPInstance;
import vroom.optimization.online.jmsa.vrp.MSAVRPInstance.RequestUpdate;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

/**
 * The class <code>DynamicInstanceGraph</code> is a specialization of. {@link DirectedSparseGraph} used to represent a
 * {@link MSAVRPInstance} and its changes over time.
 * <p>
 * Creation date: Sep 27, 2010 - 5:41:25 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class DynamicInstanceGraph extends DefaultInstanceGraph {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    @Override
    public MSAVRPInstance getInstance() {
        return (MSAVRPInstance) super.getInstance();
    }

    /**
     * Instantiates a new dynamic instance graph.
     * 
     * @param instance
     *            the instance
     */
    public DynamicInstanceGraph(MSAVRPInstance instance) {
        super(instance);

        getInstance().addObserver(this);
    }

    @Override
    public void update(IObservable source, Update update) {
        if (source instanceof MSAVRPInstance) {
            MSAVRPInstance.RequestUpdate up = (RequestUpdate) update;

            switch (up.getUpdateType()) {
            case ADDED:
                addVertex(up.getRequest());
                notifyObservers(new GraphUpdate(null, up.getRequest()));
                break;
            case ASSIGNED:
                INodeVisit prev = up.getPrevRequest();
                if (prev != null) {
                    INodeVisit next = up.getRequest();

                    Arc arc = new Arc(prev, next, getInstance().getCost(prev, next), true);
                    addEdge(arc, prev, next);
                    notifyObservers(new GraphUpdate(arc, prev, next));
                }
                break;
            case SERVED:
                notifyObservers(new GraphUpdate(null, up.getRequest()));
                break;
            case REMOVED:
                notifyObservers(new GraphUpdate(null, up.getRequest()));
                break;
            default:
                notifyObservers(null);
                break;
            }
        }
    }

    @Override
    public void detach() {
        getInstance().removeObserver(this);
        super.detach();
    }

}
