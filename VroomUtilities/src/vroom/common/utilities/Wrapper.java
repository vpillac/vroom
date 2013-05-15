package vroom.common.utilities;

/**
 * Creation date: Apr 19, 2010 - 2:39:00 PM<br/>
 * <code>Wrapper</code> is an interface for classes that wrap an instance of another instance.
 * <p>
 * It provides a generic description that can be used to access an object with multiple wrappers
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 * @param <O>
 *            The type of the wrapped object
 */
public interface Wrapper<O> {

    /**
     * Getter for the wrapped object
     * 
     * @return the instance of O that is wrapped in this instance
     */
    public O getWrappedObject();

}
