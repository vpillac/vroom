package vroom.common.modeling.dataModel.attributes;

import vroom.common.modeling.dataModel.ObjectWithAttributes;

/**
 * The Class AttributeKey represent a key to which a value can be assigned in an instance of
 * {@link ObjectWithAttributes}.
 * 
 * @param <A>
 *            the type of attribute that will be associated with this key
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #created 24-Feb-2010 01:48:52 p.m.
 */
public abstract class AttributeKey<A extends IAttribute> {

    private final String mName;

    /**
     * @return the name of this key
     */
    public String getName() {
        return mName;
    }

    /** The <code>Class</code> of the Java object representing the value of this attribute. */
    final Class<A> mAttributeClass;

    /**
     * Instantiates a new attribute key.
     * 
     * @param name
     *            a name for this attribute
     * @param attributeClass
     *            the class of the attribute
     */
    public AttributeKey(String name, Class<A> attributeClass) {
        mName = name;
        if (attributeClass == null) {
            throw new IllegalArgumentException("Argument attributeClass cannot be null");
        }
        this.mAttributeClass = attributeClass;
    }

    /**
     * Gets the attribute class.
     * 
     * @return the attribute class that is used for the values of this attribute
     */
    public Class<A> getAttributeClass() {
        return this.mAttributeClass;
    }

    /**
     * Type validation for a value.
     * 
     * @param value
     *            the value which type has to be tested
     * @return or of a subtype of the value type required for this key.
     */
    public boolean isValidValue(Object value) {
        return value == null || this.getAttributeClass().isAssignableFrom(value.getClass());
    }

    @Override
    public String toString() {
        return getName();
    }
}// end AttributeKey