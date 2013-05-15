package vroom.common.modeling.dataModel;

import vroom.common.modeling.dataModel.attributes.IDemand;
import vroom.common.modeling.dataModel.attributes.IRequestAttribute;
import vroom.common.modeling.dataModel.attributes.RequestAttributeKey;
import vroom.common.utilities.dataModel.IObjectWithID;

public interface IVRPRequest extends
		IObjectWithAttributes<IRequestAttribute, RequestAttributeKey<?>>,
		IObjectWithID {
	/**
	 * Getter for the associated destination node.
	 * 
	 * @return the destination node associated with this request, or for single
	 *         node requests
	 */
	public Node getDestinationNode();

	/**
	 * Getter for the associated node.
	 * 
	 * @return the node associated with this request, or the origin node for
	 *         origin- destination requests
	 */
	public Node getNode();

	/**
	 * Checks if is origin destination.
	 * 
	 * @return if this request has an origin and a destination {@link Node}
	 */
	public boolean isOriginDestination();

	/*
	 * Convenience methods for the demand associated with this request
	 */
	/**
	 * Sets the demands for the different products
	 * <p>
	 * Convenience methods for the demand associated with this request
	 * </p>
	 * .
	 * 
	 * @param demands
	 *            a list or array of values containing the demands for each of
	 *            the products
	 */
	public void setDemands(double... demands);

	/**
	 * Getter for the demand
	 * <p>
	 * Convenience methods for the demand associated with this request
	 * </p>
	 * .
	 * 
	 * @return the demand for the assumed unique product
	 */
	public double getDemand();

	/**
	 * Getter for the demand for a specific product
	 * <p>
	 * Convenience methods for the demand associated with this request
	 * </p>
	 * .
	 * 
	 * @param product
	 *            the id of the considered product
	 * @return the demand for the given
	 */
	public double getDemand(int product);

	/**
	 * Convenience method to read the {@link RequestAttributeKey#DEMAND}
	 * attribute
	 * 
	 * @return the value associated with {@link RequestAttributeKey#DEMAND}
	 * @see #getAttribute(vroom.common.modeling.dataModel.attributes.AttributeKey)
	 * @see RequestAttributeKey#DEMAND
	 */
	public IDemand getDemandAttribute();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString();

}