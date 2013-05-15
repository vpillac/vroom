/**
 *
 */
package vroom.common.utilities;

/**
 * <code>MutableInt</code> is a customer wrapper for a int field that supports incrementations and decrementations
 * <p>
 * Creation date: Aug 29, 2011 - 6:19:44 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class MutableInt extends Number {

    private static final long serialVersionUID = 1L;

    private int               mValue;

    /**
     * Creates a new <code>MutableInt</code>
     */
    public MutableInt() {
        this(0);
    }

    /**
     * Creates a new <code>MutableInt</code>
     * 
     * @param value
     */
    public MutableInt(int value) {
        mValue = value;
    }

    /**
     * Increment this value by 1
     */
    public void increment() {
        mValue++;
    }

    /**
     * Decrement this value by 1
     */
    public void decrement() {
        mValue--;
    }

    /**
     * Set the value of this int
     * 
     * @param value
     */
    public void setValue(int value) {
        mValue = value;
    }

    /* (non-Javadoc)
     * @see java.lang.Number#intValue()
     */
    @Override
    public int intValue() {
        return mValue;
    }

    /* (non-Javadoc)
     * @see java.lang.Number#longValue()
     */
    @Override
    public long longValue() {
        return mValue;
    }

    /* (non-Javadoc)
     * @see java.lang.Number#floatValue()
     */
    @Override
    public float floatValue() {
        return mValue;
    }

    /* (non-Javadoc)
     * @see java.lang.Number#doubleValue()
     */
    @Override
    public double doubleValue() {
        return mValue;
    }

    @Override
    public int hashCode() {
        return mValue;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MutableInt ? ((MutableInt) obj).mValue == mValue : false;
    }
}
