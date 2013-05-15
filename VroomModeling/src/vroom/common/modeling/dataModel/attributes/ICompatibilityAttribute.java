package vroom.common.modeling.dataModel.attributes;

/**
 * 
 * The Interface <code>ICompatibilityAttribute</code> defines a class of
 * attributes that model compatibilities between objects. It is in particular
 * used to factor code of compatibility constraints.
 * 
 * <p>
 * Creation date: Feb 8, 2011 - 2:27:07 PM.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los
 *         Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public interface ICompatibilityAttribute {

	/**
	 * Checks if this instance is compatible with <code>otherAttribute</code>.
	 * 
	 * @param otherAttribute
	 *            the other attribute
	 * @return <code>true</code> if this instance is compatible with
	 *         <code>otherAttribute</code>
	 */
	public boolean isCompatibleWith(ICompatibilityAttribute otherAttribute);

}
