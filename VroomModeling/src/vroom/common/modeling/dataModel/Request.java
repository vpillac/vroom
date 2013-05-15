package vroom.common.modeling.dataModel;

import vroom.common.modeling.dataModel.attributes.AttributeKey;
import vroom.common.modeling.dataModel.attributes.DeterministicDemand;
import vroom.common.modeling.dataModel.attributes.IAttribute;
import vroom.common.modeling.dataModel.attributes.IDemand;
import vroom.common.modeling.dataModel.attributes.IRequestAttribute;
import vroom.common.modeling.dataModel.attributes.RequestAttributeKey;

/**
 * The Class <code>Request</code> is used to represent a request in a VRP problem.
 * <p>
 * A request is associated with a set of attributes, for instance a {@link Node} (or possibly two in the case of pickup
 * and delivery problems) and a demand for the transported product.
 * </p>
 * <p>
 * In this implementation the only hard coded attributes are an origin node ({@link #getNode()}) and a destination node
 * ({@link #getDestinationNode()}) (Note that if a request is associated with a unique node both are equal). <br/>
 * Additional problem-dependent attributes of a <code>Request</code> can be defined as {@link IRequestAttribute}
 * associated to {@link RequestAttributeKey} with the method
 * {@link ObjectWithAttributes#setAttribute(AttributeKey, IAttribute)}
 * </p>
 * 
 * @see ObjectWithAttributes
 * @see IRequestAttribute
 * @see RequestAttributeKey
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #created 15-Feb-2010 11:29:51 a.m.
 */
public class Request extends ObjectWithAttributes<IRequestAttribute, RequestAttributeKey<?>> implements IVRPRequest {

    /** The destination node in the case of pickup and delivery problems. */
    private final Node mDestinationNode;

    /** The node associated with this request, or the source node in the case of pickup and delivery problems. */
    private final Node mNode;

    /** The id of this request */
    private final int  mID;

    /**
     * Constructor for requests associated with a unique node.
     * 
     * @param id
     *            the id of this request
     * @param node
     *            the request node
     */
    public Request(int id, Node node) {
        this(id, node, node);
    }

    /**
     * Constructor for requests with both an origin and a destination.
     * 
     * @param id
     *            the id of this request
     * @param source
     *            the source node
     * @param destination
     *            the destination node
     */
    public Request(int id, Node source, Node destination) {
        super();
        mNode = source;
        mDestinationNode = destination;
        mID = id;
    }

    /* (non-Javadoc)
     * @see vroom.common.modeling.dataModel.IRequest#getDestinationNode()
     */
    @Override
    public Node getDestinationNode() {
        return mDestinationNode;
    }

    /* (non-Javadoc)
     * @see vroom.common.modeling.dataModel.IRequest#getNode()
     */
    @Override
    public Node getNode() {
        return mNode;
    }

    /* (non-Javadoc)
     * @see vroom.common.modeling.dataModel.IRequest#isOriginDestination()
     */
    @Override
    public boolean isOriginDestination() {
        return getNode() != getDestinationNode();
    }

    /*
     * Convenience methods for the demand associated with this request
     */
    /* (non-Javadoc)
     * @see vroom.common.modeling.dataModel.IRequest#setDemands(double)
     */
    @Override
    public void setDemands(double... demands) {
        setAttribute(RequestAttributeKey.DEMAND, new DeterministicDemand(demands));
    }

    /* (non-Javadoc)
     * @see vroom.common.modeling.dataModel.IRequest#getDemand()
     */
    @Override
    public double getDemand() {
        IDemand d = getAttribute(RequestAttributeKey.DEMAND);
        return d == null || d.getProductCount() == 0 ? 0 : d.getDemand(0);
    }

    /* (non-Javadoc)
     * @see vroom.common.modeling.dataModel.IRequest#getDemand(int)
     */
    @Override
    public double getDemand(int product) {
        IDemand d = getAttribute(RequestAttributeKey.DEMAND);
        return d == null ? 0 : d.getDemand(product);
    }

    @Override
    public IDemand getDemandAttribute() {
        return getAttribute(RequestAttributeKey.DEMAND);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    /* (non-Javadoc)
     * @see vroom.common.modeling.dataModel.IRequest#toString()
     */
    @Override
    public String toString() {
        if (isOriginDestination()) {
            return String.format("[%s->%s %s]", getNode().getID(), getDestinationNode().getID(),
                    getAttributesAsString());
        } else {
            return String.format("[%s %s]", getDestinationNode().getID(), getAttributesAsString());
        }
    }

    @Override
    public int getID() {
        return mID;
    }

}// end VRPRequest