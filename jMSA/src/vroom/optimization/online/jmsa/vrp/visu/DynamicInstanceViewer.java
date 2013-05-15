/**
 * 
 */
package vroom.optimization.online.jmsa.vrp.visu;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;

import org.apache.commons.collections15.Transformer;

import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.attributes.RequestAttributeKey;
import vroom.common.modeling.visualization.DefaultInstanceViewer;
import vroom.common.utilities.IObservable;
import vroom.common.utilities.IObserver;
import vroom.common.utilities.Update;
import vroom.optimization.online.jmsa.vrp.VRPActualRequest;
import vroom.optimization.online.jmsa.vrp.vrpsd.VRPSDActualRequest;

/**
 * <code>DynamicInstanceViewer</code>
 * <p>
 * Creation date: Sep 27, 2010 - 6:02:21 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class DynamicInstanceViewer extends DefaultInstanceViewer implements IObserver {

    private static final long          serialVersionUID        = 1L;

    public static Color                sColorDepot             = Color.DARK_GRAY;
    public static Color                sColorUnassignedRequest = Color.WHITE;
    public static Color                sColorAssignedRequest   = Color.LIGHT_GRAY;
    public static Color                sColorServedRequest     = Color.DARK_GRAY;

    private final DynamicInstanceGraph mGraph;

    /**
     * Creates a new <code>DynamicInstanceViewer</code>
     * 
     * @param graph
     * @param preferredSize
     * @param preferredSize
     *            the preferred size
     * @param minX
     *            the min x coordinate
     * @param maxX
     *            the max x coordinate
     * @param minY
     *            the min y coordinate
     * @param maxY
     *            the max y coordinate
     */
    public DynamicInstanceViewer(DynamicInstanceGraph graph, Dimension preferredSize, double[] bounds) {
        super(graph, preferredSize, bounds);
        mGraph = graph;
        mGraph.addObserver(this);
    }

    /**
     * Instantiates a new dynamic instance viewer.
     * 
     * @param graph
     *            the graph
     */
    public DynamicInstanceViewer(DynamicInstanceGraph graph) {
        super(graph);
        mGraph = graph;
        mGraph.addObserver(this);
    }

    @Override
    protected void setDefaultVertexFillPaintTransformer() {
        getRenderContext().setVertexFillPaintTransformer(new VertexFillPaintTransformer());
    }

    // @Override
    // protected void setDefaultVertexLabelTransformer() {
    // getRenderContext().setVertexLabelTransformer(new VertexLabelTransformer());
    // }

    @Override
    public void update(IObservable source, Update update) {
        update();
        // if (update instanceof GraphUpdate) {
        // GraphUpdate up = (GraphUpdate) update;
        // if (up.isNodeUpdate()) {
        // for (INodeVisit n : up.getNodes()) {
        // getRenderer().renderVertex(getRenderContext(), getGraphLayout(), n);
        // getRenderer().renderVertexLabel(getRenderContext(), getGraphLayout(),
        // n);
        // }
        // }
        // if (up.isArcUpdate()) {
        // getRenderer().renderEdge(getRenderContext(), getGraphLayout(),
        // up.getArc());
        // getRenderer().renderEdgeLabel(getRenderContext(), getGraphLayout(),
        // up.getArc());
        // } else {
        // update();
        // }
        // } else {
        // update();
        // }
    }

    public class VertexFillPaintTransformer implements Transformer<INodeVisit, Paint> {
        @Override
        public Paint transform(INodeVisit node) {
            if (node.isDepot()) {
                return sColorDepot;
            } else if (node instanceof VRPActualRequest) {
                VRPActualRequest r = (VRPActualRequest) node;
                if (mGraph.getInstance().isRequestServed(r)) {
                    return sColorServedRequest;
                } else if (mGraph.getInstance().isRequestAssigned(r)) {
                    return sColorAssignedRequest;
                } else {
                    return sColorUnassignedRequest;
                }
            } else {
                return sColorUnassignedRequest;
            }
        }
    }

    public class VertexLabelTransformer implements Transformer<INodeVisit, String> {
        @Override
        public String transform(INodeVisit node) {
            if (node.isDepot()) {
                return "depot";
            }

            Object demand;
            if (node instanceof VRPSDActualRequest && ((VRPSDActualRequest) node).isDemandKnown(0)) {
                demand = ((VRPSDActualRequest) node).getDemand(0);
            } else {
                demand = node.getParentRequest().getAttribute(RequestAttributeKey.DEMAND);
            }

            return String.format("%s-%s", node.getID(), demand);
            // return "" + node.getID();
        }

    }

    public void detach() {
        mGraph.detach();
    }
}
