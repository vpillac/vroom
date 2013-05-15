package vroom.common.utilities.params;

import vroom.common.utilities.StringParser;
import vroom.common.utilities.StringParser.BooleanParser;
import vroom.common.utilities.StringParser.DoubleParser;
import vroom.common.utilities.StringParser.DummyStringParser;
import vroom.common.utilities.StringParser.EnumStringParser;
import vroom.common.utilities.StringParser.IntegerParser;

/**
 * <code>ParameterKey</code> used to describe a parameter. It is composed by a string giving the name of the parameter
 * and a reference to the supertype class for parameters values.<br/>
 * Note that the hash code used to associate a key to a value is calculated based on the name of a key. Names should
 * therefore be unique
 * 
 * @param T
 *            is a supertype of the parameters values
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:06:46 a.m.
 */
public class ParameterKey<T extends Object> implements Comparable<ParameterKey<?>> {

    /**
     * The name of this parameter
     */
    private final String          mName;
    /**
     * The type of this parameter
     */
    private final Class<T>        mType;

    /**
     * An optional default value for this key
     */
    private final T               mDefaultValue;

    /**
     * An optional parser for this key
     */
    private final StringParser<T> mParser;

    /**
     * Creates a new key
     * 
     * @param name
     *            the name for this key, should be unique
     * @param type
     *            the type of the corresponding parameter value
     */
    public ParameterKey(String name, Class<T> type) {
        this(name, type, null, null);
    }

    /**
     * Creates a new key
     * 
     * @param name
     *            the name for this key, should be unique
     * @param type
     *            the type of the corresponding parameter value
     * @param defaultValue
     *            an optional default value for this key
     */
    public ParameterKey(String name, Class<T> type, T defaultValue) {
        this(name, type, null, defaultValue);
    }

    /**
     * Creates a new <code>ParameterKey</code>
     * 
     * @param name
     *            the name for this key, should be unique
     * @param type
     *            the type of the corresponding parameter value
     * @param parser
     *            an optional parser that can be used to convert values from/to string
     */
    public ParameterKey(String name, Class<T> type, StringParser<T> parser) {
        this(name, type, parser, null);
    }

    /**
     * Creates a new <code>ParameterKey</code>
     * 
     * @param name
     *            the name for this key, should be unique
     * @param type
     *            the type of the corresponding parameter value
     * @param parser
     *            an optional parser that can be used to convert values from/to string
     * @param defaultValue
     *            an optional default value for this key
     */
    public ParameterKey(String name, Class<T> type, StringParser<T> parser, T defaultValue) {
        super();
        mName = name;
        mType = type;
        mDefaultValue = defaultValue;
        mParser = parser;
    }

    /**
     * @return the name for this key
     */
    public String getName() {
        return this.mName;
    }

    /**
     * @return the type of the corresponding parameter value
     */
    public Class<T> getType() {
        return this.mType;
    }

    /**
     * Return the optional parser associated with this key
     * 
     * @return the optional parser associated with this key
     */
    public StringParser<T> getParser() {
        return mParser;
    }

    /**
     * @return a default value for this key
     */
    public T getDefaultValue() {
        return mDefaultValue;
    }

    /**
     * Check if the given <code>value</code> is valid for this parameter.
     * <p>
     * By default <code>null</code> is considered as a valid value for any parameter
     * 
     * @param value
     *            the value to be checked
     * @return <code>true</code> if the <code>value</code> is valid for the associated parameter
     */
    public boolean isValueValid(Object value) {
        return getType() == null || value == null || getType().isAssignableFrom(value.getClass());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ParameterKey<?>
                && getName().equals(((ParameterKey<?>) obj).getName());
    }

    @Override
    public String toString() {
        if (getType() != null) {
            return String.format("%s (%s)", getName(), getType().getSimpleName());
        } else {
            return String.format("%s (%s)", getName(), null);
        }
    }

    @Override
    public int compareTo(ParameterKey<?> o) {
        return o != null ? getName().compareTo(o.getName()) : 1;
    }

    /**
     * <code>DoubleParameterKey</code> is a convenience extension of {@link ParameterKey} that provides a default parser
     * <p>
     * Creation date: Jun 26, 2012 - 10:15:04 AM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    public static class DoubleParameterKey extends ParameterKey<Double> {

        /**
         * Creates a new <code>DoubleParameterKey</code>
         * 
         * @param name
         *            the name for this key, should be unique
         */

        public DoubleParameterKey(String name) {
            super(name, Double.class, new DoubleParser());
        }

        /**
         * Creates a new <code>DoubleParameterKey</code>
         * 
         * @param name
         *            the name for this key, should be unique
         * @param defaultValue
         *            an optional default value for this key
         */
        public DoubleParameterKey(String name, Double defaultValue) {
            super(name, Double.class, new DoubleParser(), defaultValue);
        }
    }

    /**
     * <code>IntegerParameterKey</code> is a convenience extension of {@link ParameterKey} that provides a default
     * parser
     * <p>
     * Creation date: Jun 26, 2012 - 10:15:04 AM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    public static class IntegerParameterKey extends ParameterKey<Integer> {

        /**
         * Creates a new <code>DoubleParameterKey</code>
         * 
         * @param name
         *            the name for this key, should be unique
         */

        public IntegerParameterKey(String name) {
            super(name, Integer.class, new IntegerParser());
        }

        /**
         * Creates a new <code>DoubleParameterKey</code>
         * 
         * @param name
         *            the name for this key, should be unique
         * @param defaultValue
         *            an optional default value for this key
         */
        public IntegerParameterKey(String name, Integer defaultValue) {
            super(name, Integer.class, new IntegerParser(), defaultValue);
        }
    }

    /**
     * <code>BooleanParameterKey</code> is a convenience extension of {@link ParameterKey} that provides a default
     * parser
     * <p>
     * Creation date: Jun 26, 2012 - 10:15:04 AM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    public static class BooleanParameterKey extends ParameterKey<Boolean> {

        /**
         * Creates a new <code>DoubleParameterKey</code>
         * 
         * @param name
         *            the name for this key, should be unique
         */

        public BooleanParameterKey(String name) {
            super(name, Boolean.class, new BooleanParser());
        }

        /**
         * Creates a new <code>DoubleParameterKey</code>
         * 
         * @param name
         *            the name for this key, should be unique
         * @param defaultValue
         *            an optional default value for this key
         */
        public BooleanParameterKey(String name, Boolean defaultValue) {
            super(name, Boolean.class, new BooleanParser(), defaultValue);
        }
    }

    /**
     * <code>StringParameterKey</code> is a convenience extension of {@link ParameterKey} that provides a default parser
     * <p>
     * Creation date: Jun 26, 2012 - 10:15:04 AM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    public static class StringParameterKey extends ParameterKey<String> {

        /**
         * Creates a new <code>DoubleParameterKey</code>
         * 
         * @param name
         *            the name for this key, should be unique
         */

        public StringParameterKey(String name) {
            super(name, String.class, new DummyStringParser());
        }

        /**
         * Creates a new <code>DoubleParameterKey</code>
         * 
         * @param name
         *            the name for this key, should be unique
         * @param defaultValue
         *            an optional default value for this key
         */
        public StringParameterKey(String name, String defaultValue) {
            super(name, String.class, new DummyStringParser(), defaultValue);
        }
    }

    /**
     * The class <code>EnumParameterKey</code> is a convenience extension of {@link ParameterKey} that provides a
     * default parser for enumerations
     * <p>
     * Creation date: 18/02/2013 - 5:03:02 PM
     * 
     * @author Victor Pillac, <a href="http://www.nicta.com.au">National ICT Australia</a>
     * @version 1.0
     * @param <E>
     */
    public static class EnumParameterKey<E extends Enum<E>> extends ParameterKey<E> {

        @SuppressWarnings("unchecked")
        public EnumParameterKey(String name, E defaultValue) {
            super(name, (Class<E>) defaultValue.getClass(), new EnumStringParser<E>(
                    (Class<E>) defaultValue.getClass()), defaultValue);
        }

    }
}// end ParameterKey
