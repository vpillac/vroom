package vroom.optimization.online.jmsa.utils;

import vroom.common.utilities.Wrapper;

/**
 * Creation date: Apr 20, 2010 - 9:17:48 AM<br/>
 * <code>Utilities</code> provides a set of functions for he MSA library
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class Utilities {

    /**
     * Recursive unwrapping.
     * <p>
     * Will check if the wrapped object is an instance of {@link Wrapper}, in which case the method will be recursively
     * to called on the wrapped instance, otherwise the wrapped object is returned.
     * 
     * @param <T>
     *            the type of the wrapped object
     * @param wrapper
     *            the object wrapper
     * @return the wrapped object
     */
    @SuppressWarnings("unchecked")
    public static <T> T getWrappedObject(Wrapper<T> wrapper) {
        if (wrapper.getWrappedObject() instanceof Wrapper<?>) {
            return getWrappedObject((Wrapper<T>) wrapper.getWrappedObject());
        } else {
            return wrapper.getWrappedObject();
        }
    }

    /**
     * Object unwrapping.
     * <p>
     * Will check if the given <code>object</code> is a wrapper, in which case the wrapped instance is
     * {@linkplain #getWrappedObject(Wrapper) recursively unwrapped}, otherwise <code>object</code> is returned.
     * 
     * @param <T>
     *            the type of the wrapped object
     * @param object
     *            the object to be checked
     * @return the wrapped object
     * @see #getWrappedObject(Wrapper)
     */
    @SuppressWarnings("unchecked")
    public static <T> T getImplicitWrappedObject(T object) {
        if (object instanceof Wrapper<?>) {
            return getWrappedObject((Wrapper<T>) object);
        } else {
            return object;
        }
    }
}
