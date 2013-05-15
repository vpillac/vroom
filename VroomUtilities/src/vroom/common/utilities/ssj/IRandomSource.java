/**
 * 
 */
package vroom.common.utilities.ssj;

import umontreal.iro.lecuyer.rng.RandomStream;

/**
 * <code>IRandomSource</code> is a generic interface for classes that provides a
 * {@link RandomStream}
 * <p>
 * Creation date: Oct 11, 2010 - 3:40:42 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los
 *         Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public interface IRandomSource {

	/**
	 * Sets the random stream for this instance
	 * 
	 * @param stream
	 *            the random stream to be used in this instance
	 */
	public void setRandomStream(RandomStream stream);

	/**
	 * Access to the random stream
	 * 
	 * @return the {@link RandomStream} used in this instance
	 */
	public RandomStream getRandomStream();
}
