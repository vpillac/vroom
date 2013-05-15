/**
 * 
 */
package vroom.common.utilities;

import java.util.Observable;

/**
 * <code>ValueUpdate</code> is a simple description of a change in the value of a given {@link Observable}
 * <p>
 * Creation date: Jun 30, 2010 - 2:18:52 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class ValueUpdate implements Update {

    /** the name of the property which value has changed **/
    private final String mProperty;

    /**
     * Getter for the name of the changed property
     * 
     * @return the name of the property which value has changed
     */
    @Override
    public String getDescription() {
        return mProperty;
    }

    /** The previous value of the changed property **/
    private final Object mOldValue;

    /**
     * Getter for the previous value
     * 
     * @return The previous value of the changed property
     */
    public Object getOldValue() {
        return mOldValue;
    }

    /** the new value of the changed property **/
    private final Object mNewValue;

    /**
     * Getter for the new value
     * 
     * @return the new value of the changed property
     */
    public Object getNewValue() {
        return mNewValue;
    }

    /**
     * Creates a new <code>ValueUpdate</code>
     * 
     * @param property
     * @param oldValue
     * @param newValue
     */
    public ValueUpdate(String property, Object oldValue, Object newValue) {
        mProperty = property;
        mOldValue = oldValue;
        mNewValue = newValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("%s value changed: old:%s, new:%s", getDescription(), getOldValue(),
                getNewValue());
    }

}
