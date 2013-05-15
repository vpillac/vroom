/**
 * 
 */
package vroom.common.utilities.params;

/**
 * The class <code>ParameterValueSet</code> contains a reference to a {@link ParameterKey} and an array of possible
 * values. A possible use is for experiment design to test various combinations of parameters
 * <p>
 * Creation date: Jun 21, 2012 - 11:20:38 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class ParameterValueSet<T> {

    private final ParameterKey<T> mParameter;
    private final T[]             mValues;

    /**
     * Creates a new <code>ParameterValueSet</code>
     * 
     * @param param
     * @param values
     */
    @SuppressWarnings("unchecked")
    public ParameterValueSet(ParameterKey<T> param, T... values) {
        mParameter = param;
        mValues = values;
    }

    /**
     * Return the parameter key
     * 
     * @return the parameter key
     */
    public ParameterKey<T> getParameter() {
        return mParameter;
    }

    /**
     * Return the possible values for the associated {@link #getParameter() key}
     * 
     * @return an array of values
     */
    public T[] getValues() {
        return mValues;
    }
}
