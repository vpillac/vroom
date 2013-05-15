/**
 * 
 */
package vroom.common.modeling.visualization;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import javax.swing.UIManager;

import org.apache.commons.collections15.Transformer;

import vroom.common.modeling.dataModel.IArc;
import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.IVRPInstance;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;

/**
 * <code>DefaultInstanceViewer</code> is a specialization of. {@link VisualizationViewer} that defines default colors
 * and shapes to represent an {@link IVRPInstance}.
 * <p>
 * Creation date: Sep 27, 2010 - 5:30:20 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class DefaultInstanceViewer extends VisualizationViewer<INodeVisit, IArc> implements
        ComponentListener {

    private static final long          serialVersionUID = 1L;

    /** The vertex size **/
    private int                        mVertexSize;

    private final NodeVisitTransformer mTransformer;

    /**
     * Getter for the node transformer
     * 
     * @return the node transformer
     */
    public NodeVisitTransformer getTransformer() {
        return mTransformer;
    }

    /** The underlying graph **/
    private final Graph<INodeVisit, IArc> mGraph;

    /**
     * Getter for the underlying graph
     * 
     * @return the value of name
     */
    public Graph<INodeVisit, IArc> getGraph() {
        return this.mGraph;
    }

    /**
     * Getter for the vertex size
     * 
     * @return the value of vertexSize
     */
    public int getVertexSize() {
        return mVertexSize;
    }

    /**
     * Setter for The vertex size
     * 
     * @param vertexSize
     *            the value to be set for The vertex size
     */
    public void setVertexSize(int vertexSize) {
        mVertexSize = vertexSize;
        mTransformer.setVertexSize(mVertexSize);
        update();
    }

    /**
     * Instantiates a new default instance viewer.
     * 
     * @param graph
     *            the graph
     * @param preferredSize
     *            the preferred size
     * @param bounds
     *            bounds for the nodes coordinates
     */
    public DefaultInstanceViewer(Graph<INodeVisit, IArc> graph, Dimension preferredSize,
            double[] bounds) {
        super(new StaticLayout<INodeVisit, IArc>(graph), preferredSize);

        mGraph = graph;

        mTransformer = new NodeVisitTransformer(Math.ceil(Math.max(bounds[1] - bounds[0], bounds[3]
                - bounds[2])), 14, -bounds[0], -bounds[2]);
        getGraphLayout().setInitializer(mTransformer);
        addComponentListener(mTransformer);

        mVertexSize = UIManager.getFont("Label.font").getSize() + 4;
        setDefaultVertexShapeTransformer();
        setDefaultVertexFillPaintTransformer();
        setDefaultVertexLabelTransformer();
        getRenderContext().setVertexFontTransformer(new Transformer<INodeVisit, Font>() {
            @Override
            public Font transform(INodeVisit arg0) {
                return UIManager.getFont("Label.font");
            }
        });
        addComponentListener(this);
    }

    /**
     * Instantiates a new default instance viewer of size 100x100
     * 
     * @param graph
     *            the graph
     */
    public DefaultInstanceViewer(Graph<INodeVisit, IArc> graph) {
        this(graph, new Dimension(100, 100), calculateBounds(graph));
    }

    /**
     * Calculate the min and max coordiates of the given graph.
     * 
     * @param graph
     * @return an array containing <code>[minX,maxX,minY,naxY]</code>
     */
    public static double[] calculateBounds(Graph<INodeVisit, IArc> graph) {
        double x, y, minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;

        for (INodeVisit v : graph.getVertices()) {
            x = v.getNode().getLocation().getX();
            y = v.getNode().getLocation().getX();

            if (x > maxX) {
                maxX = x;
            }
            if (x < minX) {
                minX = x;
            }
            if (y > maxY) {
                maxY = y;
            }
            if (x < minY) {
                minY = y;
            }
        }

        return new double[] { Math.floor(minX * 1.05), Math.ceil(maxX * 1.05),
                Math.floor(minY * 1.05), Math.ceil(maxY * 1.05) };
    }

    /**
     * Sets the default vertex shape transformer.
     */
    protected void setDefaultVertexShapeTransformer() {
        getRenderContext().setVertexShapeTransformer(new Transformer<INodeVisit, Shape>() {
            @Override
            public Shape transform(INodeVisit node) {
                if (node.isDepot()) {
                    return new Rectangle2D.Float(-mVertexSize / 2, -mVertexSize / 2, mVertexSize,
                            mVertexSize);
                } else {
                    return new Ellipse2D.Float(-mVertexSize / 2, -mVertexSize / 2, mVertexSize,
                            mVertexSize);
                }
            }
        });
    }

    /**
     * Sets the default vertex fill paint transformer.
     */
    protected void setDefaultVertexFillPaintTransformer() {
        getRenderContext().setVertexFillPaintTransformer(new Transformer<INodeVisit, Paint>() {
            @Override
            public Paint transform(INodeVisit node) {
                if (node.isDepot()) {
                    return Color.BLACK;
                } else {
                    return Color.DARK_GRAY;
                }
            }
        });
    }

    /**
     * Sets the default vertex label transformer.
     */
    protected void setDefaultVertexLabelTransformer() {
        getRenderContext().setVertexLabelTransformer(new Transformer<INodeVisit, String>() {
            @Override
            public String transform(INodeVisit node) {
                return "" + node.getID();
            }
        });
    }

    /**
     * Calls paint. Doesn't clear the background but see ComponentUI.update, which is called by paintComponent.
     */
    public void update() {
        update(getGraphics());
    }

    @Override
    public void componentResized(ComponentEvent e) {
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }

}
