package vroom.common.modeling.dataModel.attributes;

import java.util.Set;

/**
 * 
 * The Class <code>AttributeSet</code> contains a set of attributes.
 * 
 * <p>
 * Creation date: Feb 8, 2011 - 2:56:21 PM.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los
 *         Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 * @param <A>
 *            the type of attribute
 */
public class AttributeSet<A extends IAttribute> implements INodeAttribute,
		IRequestAttribute, IVehicleAttribute {

	/** the set of attributes *. */
	private final Set<A> mAttributes;

	/**
	 * Getter for the set of attributes.
	 * 
	 * @return the set of attributes
	 */
	public Set<A> getAttributes() {
		return this.mAttributes;
	}

	/**
	 * Instantiates a new attribute set.
	 * 
	 * @param attributes
	 *            the attributes
	 */
	public AttributeSet(Set<A> attributes) {
		super();
		mAttributes = attributes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see vroom.common.utilities.dataModel.IObjectWithName#getName()
	 */
	@Override
	public String getName() {
		return "AttributeSet";
	}

}
