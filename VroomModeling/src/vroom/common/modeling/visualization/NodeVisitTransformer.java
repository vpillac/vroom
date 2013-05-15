package vroom.common.modeling.visualization;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.Point2D;

import org.apache.commons.collections15.Transformer;

import vroom.common.modeling.dataModel.INodeVisit;

/**
 * The Class <code>NodeVisitTransformer</code>
 * <p>
 * Creation date: Sep 27, 2010 - 5:25:26 PM.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class NodeVisitTransformer implements Transformer<INodeVisit, Point2D>, ComponentListener {

    private int          mWidth;
    private int          mHeight;
    private int          mVertexSize;

    private final double mScale;
    private final double mXOffset;
    private final double mYOffset;

    /**
     * Gets the width.
     * 
     * @return the width
     */
    public int getWidth() {
        return mWidth;
    }

    /**
     * Sets the width.
     * 
     * @param width
     *            the new width
     */
    public void setWidth(int width) {
        mWidth = width;
    }

    /**
     * Gets the height.
     * 
     * @return the height
     */
    public int getHeight() {
        return mHeight;
    }

    /**
     * Sets the height.
     * 
     * @param height
     *            the new height
     */
    public void setHeight(int height) {
        mHeight = height;
    }

    /**
     * Gets the scale.
     * 
     * @return the scale
     */
    public double getScale() {
        return mScale;
    }

    public void setVertexSize(int vertexSize) {
        mVertexSize = vertexSize;
    }

    /**
     * Creates a new <code>NodeVisitTransformer</code> with the specified width and height.
     * 
     * @param scale
     *            the scale
     * @param xOffset
     *            the offset for the x coordinate
     * @param yOffset
     *            the offset for the y coordinate
     */
    public NodeVisitTransformer(double scale, int nodeSize, double xOffset, double yOffset) {
        mScale = scale;
        mWidth = 0;
        mHeight = 0;
        mVertexSize = nodeSize;
        mXOffset = xOffset;
        mYOffset = yOffset;
    }

    @Override
    public Point2D transform(INodeVisit arg0) {
        return new NodeVisitPoint(arg0);
    }

    class NodeVisitPoint extends Point2D {

        final INodeVisit mNode;

        public NodeVisitPoint(INodeVisit node) {
            super();
            mNode = node;
        }

        @Override
        public double getX() {
            return (int) Math.round((mWidth - mVertexSize)
                    * (mNode.getNode().getLocation().getX() + mXOffset) / mScale)
                    + mVertexSize / 2;
        }

        @Override
        public double getY() {
            return mHeight
                    - (int) Math.round((mHeight - mVertexSize)
                            * (mNode.getNode().getLocation().getY() + mYOffset) / mScale)
                    - mVertexSize / 2;
        }

        @Override
        public void setLocation(double x, double y) {
            throw new UnsupportedOperationException("This note is transcient");

        }

    }

    @Override
    public void componentResized(ComponentEvent e) {
        setWidth(e.getComponent().getWidth());
        setHeight(e.getComponent().getHeight());
    }

    @Override
    public void componentMoved(ComponentEvent e) {

    }

    @Override
    public void componentShown(ComponentEvent e) {
        setWidth(e.getComponent().getWidth());
        setHeight(e.getComponent().getHeight());
    }

    @Override
    public void componentHidden(ComponentEvent e) {

    }
}