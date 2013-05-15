/**
 * 
 */
package vroom.common.utilities;

/**
 * The interface <code>IDistance</code> defines a distance metric that can be used to measure distances between objects
 * <p>
 * Creation date: Feb 23, 2012 - 11:33:08 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public interface IDistance<T> {

    /**
     * Evaluate the distance between two objects
     * 
     * @param obj1
     *            a first object
     * @param obj2
     *            a second object
     * @return a measure of the distance between {@code  obj1} and {@code  obj2}
     */
    public double evaluateDistance(T obj1, T obj2);

    /**
     * Returns {@code true} if this metric is symmetric, {@code false} otherwise
     * 
     * @return {@code true} if this metric is symmetric, {@code false} otherwise
     */
    public boolean isSymmetric();

}
