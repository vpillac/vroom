package vroom.common.modeling.dataModel;

import vroom.common.modeling.dataModel.attributes.AttributeKey;
import vroom.common.modeling.dataModel.attributes.IAttribute;

public interface IObjectWithAttributes<A extends IAttribute, K extends AttributeKey<? extends A>> {

    /**
     * Getter for the value of an attribute.
     * 
     * @param <AE>
     *            the generic type
     * @param <KE>
     *            the generic type
     * @param attributeKey
     *            the key for the desired attribute
     * @return the value associated with
     */
    public <AE extends A, KE extends AttributeKey<AE>> AE getAttribute(KE attributeKey);

    /**
     * Access to an attribute value.
     * 
     * @param attributeKey
     *            the key for the desired attribute
     * @param value
     *            the value to be set for <code>attributeKey</code>
     * @return the value previously associated with the key
     * @throws IllegalArgumentException
     *             if the type of <code>value</code> is not compatible with the type expected for
     *             <code>attributeKey</code>
     */
    public A setAttribute(K attributeKey, A value);

    /**
     * Gets the attributes as string.
     * 
     * @return the attributes as string
     */
    public String getAttributesAsString();

}