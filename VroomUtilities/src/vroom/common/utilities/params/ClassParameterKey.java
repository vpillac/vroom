package vroom.common.utilities.params;

/**
 * <code>ClassParameterKey</code> is an extension of {@link ParameterKey} dedicated to the parameters which values are
 * of type {@link Class}
 * <p>
 * Creation date: Apr 15, 2010 - 2:14:38 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 * @param <T>
 *            the type of a supertype against which parameter values will be checked
 */
@SuppressWarnings("rawtypes")
public class ClassParameterKey<T> extends ParameterKey<Class> {

    private final Class<T> mSupertype;

    /**
     * Creates a new <code>ClassParameterKey</code>
     * 
     * @param name
     *            a name for this key
     * @param supertype
     *            a supertype of the classes that can be set as values for this parameter
     * @param defaultValue
     *            a default value for this parameter
     * @see ParameterKey#ParameterKey(String, Class, Object)
     */
    public ClassParameterKey(String name, Class<T> supertype, Class defaultValue) {
        super(name, Class.class, defaultValue);

        if (supertype == null) {
            throw new IllegalArgumentException("Argument supertype cannot be null");
        }

        mSupertype = supertype;
    }

    /**
     * Creates a new <code>ClassParameterKey</code>
     * 
     * @param name
     * @param supertype
     * @see #ClassParameterKey(String, Class, Class)
     */
    public ClassParameterKey(String name, Class<T> supertype) {
        this(name, supertype, null);
    }

    @Override
    public boolean isValueValid(Object value) {
        return super.isValueValid(value)
                && (value == null || mSupertype.isAssignableFrom((Class) value));
    }

    /* (non-Javadoc)
     * @see vroom.common.utilities.params.ParameterKey#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof ClassParameterKey
                && mSupertype == ((ClassParameterKey) obj).mSupertype && super.equals(obj);
    }
}
