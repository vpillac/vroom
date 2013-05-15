package vroom.common.utilities.params;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import vroom.common.utilities.Utilities;
import vroom.common.utilities.logging.Logging;

/**
 * <code>GlobalParameters</code> contains the parameters for the MSA procedure.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:06:49 a.m.
 */
public abstract class GlobalParameters implements Cloneable {

    // -----------------------------------------------------------
    // Class fields
    /** A list of the parameters that are required */
    private final static Map<Class<?>, Set<ParameterKey<?>>> mRequiredParameters;

    /** A default persistence delegate (lazy instantiated) */
    private static ParametersFilePersistenceDelegate         sPersistenceDelegate;

    /**
     * Set the persistence delegate used to load and save parameters from/to a file
     * 
     * @param delegate
     */
    public static void setPersistenceDelegate(ParametersFilePersistenceDelegate delegate) {
        sPersistenceDelegate = delegate;
    }

    /**
     * Getter for the persistence delegate
     * 
     * @return the persistence delegate used to load and save parameters from/to a file
     */
    private static ParametersFilePersistenceDelegate getPersistenceDelegate() {
        if (sPersistenceDelegate == null) {
            sPersistenceDelegate = new ParametersFilePersistenceDelegate();
        }
        return sPersistenceDelegate;
    }

    /**
     * Initialization of the static fields
     */
    static {
        mRequiredParameters = new HashMap<Class<?>, Set<ParameterKey<?>>>();
    }

    /**
     * List of the required parameters
     * 
     * @return a copy of the required parameters
     */
    public static Collection<ParameterKey<?>> getRequiredParameters(Class<?> clazz) {
        if (!mRequiredParameters.containsKey(clazz)) {
            mRequiredParameters.put(clazz, new HashSet<ParameterKey<?>>());
        }

        return new LinkedList<ParameterKey<?>>(mRequiredParameters.get(clazz));
    }

    /**
     * Adds a new parameter to the list of required parameters
     * 
     * @param parameterKey
     */
    public static void addRequiredParameter(Class<?> clazz, ParameterKey<?> parameterKey) {
        if (!mRequiredParameters.containsKey(clazz)) {
            mRequiredParameters.put(clazz, new HashSet<ParameterKey<?>>());
        }
        mRequiredParameters.get(clazz).add(parameterKey);
    }

    // -----------------------------------------------------------

    /** A mapping between keys and values */
    private final Map<ParameterKey<?>, Object> mParameters;

    /** A set of the parameters that will possibly be defined in this instance */
    private final Map<String, ParameterKey<?>> mRegisteredKeys;

    /**
     * Creates a new <code>GlobalParameters</code>
     */
    public GlobalParameters() {
        mParameters = new HashMap<ParameterKey<?>, Object>();
        mRegisteredKeys = new HashMap<String, ParameterKey<?>>();

        for (ParameterKey<?> k : getRequiredParameters(this.getClass()))
            registerKey(k);

        registerKeysByReflection(this.getClass());

        resetDefaultValues();
    }

    /**
     * Parameter key reflective registering
     * <p/>
     * Lookup by reflection for static fields of type {@link ParameterKey} and register them in this instance.
     * 
     * @param clazz
     *            the class to be analyzed
     */
    public void registerKeysByReflection(Class<?> clazz) {
        for (Field f : clazz.getFields()) {
            if (Modifier.isStatic(f.getModifiers())
                    && ParameterKey.class.isAssignableFrom(f.getType())) {
                try {
                    ParameterKey<?> key = (ParameterKey<?>) f.get(null);
                    if (key != null) {
                        registerKey(key);
                    }
                } catch (IllegalArgumentException e) {
                    // Do nothing: ignore the field
                } catch (IllegalAccessException e) {
                    // Do nothing: ignore the field
                }

            }
        }
    }

    /**
     * Required parameter key reflective adding
     * <p/>
     * Lookup by reflection for static fields of type {@link ParameterKey} with the {@link RequiredParameter} annotation
     * and add them to the list of required parameters
     * 
     * @param clazz
     *            the class to be analyzed
     */
    public static void addRequiredKeysByReflection(Class<?> clazz) {
        for (Field f : clazz.getFields()) {
            if (Modifier.isStatic(f.getModifiers())
                    && f.getAnnotation(RequiredParameter.class) != null
                    && ParameterKey.class.isAssignableFrom(f.getType())) {
                try {
                    ParameterKey<?> key = (ParameterKey<?>) f.get(null);
                    if (key != null) {
                        addRequiredParameter(clazz, key);
                    }
                } catch (IllegalArgumentException e) {
                    // Do nothing: ignore the field
                } catch (IllegalAccessException e) {
                    // Do nothing: ignore the field
                }

            }
        }
    }

    /**
     * Return all the keys declared in the type {@code  clazz}
     * 
     * @param clazz
     * @returnall the keys declared in the type {@code  clazz}
     */
    public static List<ParameterKey<?>> getAllKeys(Class<?> clazz) {
        Field[] fields = clazz.getFields();
        ArrayList<ParameterKey<?>> keys = new ArrayList<ParameterKey<?>>(fields.length);
        for (Field f : fields) {
            if (Modifier.isStatic(f.getModifiers())
                    && ParameterKey.class.isAssignableFrom(f.getType())) {
                try {
                    ParameterKey<?> key = (ParameterKey<?>) f.get(null);
                    if (key != null) {
                        keys.add(key);
                    }
                } catch (IllegalArgumentException e) {
                    // Do nothing: ignore the field
                } catch (IllegalAccessException e) {
                    // Do nothing: ignore the field
                }
            }
        }
        Collections.sort(keys);
        return keys;
    }

    /**
     * Print all the keys declared in the type {@code  clazz}
     * 
     * @param clazz
     * @param halFormat
     *            {@code true} if keys should be printed in a HAL format
     */
    public static void printAllKeys(Class<?> clazz, boolean halFormat) {
        List<ParameterKey<?>> keys = getAllKeys(clazz);
        for (ParameterKey<?> k : keys) {
            if (halFormat)
                System.out.printf("%s=$%s$ ", k.getName(), k.getName().toLowerCase());
            else
                System.out.println(k);
        }
    }

    /**
     * Register the specified parameter in this instance.
     * <p>
     * This is in particular useful when loading parameters from a property file
     * 
     * @param key
     *            the parameter key to be registered
     * @return <code>true</code> if <code>key</code> was not already registered
     */
    public boolean registerKey(ParameterKey<?> key) {
        return mRegisteredKeys.put(key.getName(), key) != null;
    }

    /**
     * Set of registered parameters
     * 
     * @return a copy of the set containing the registered keys
     */
    public Set<ParameterKey<?>> getRegisteredKeys() {
        return new HashSet<ParameterKey<?>>(mRegisteredKeys.values());
    }

    /**
     * Returns the key named {@code  keyName} or <code>null</code> if it is not registered
     * 
     * @param keyName
     * @return the key named {@code  keyName}
     */
    public ParameterKey<?> getRegisteredKey(String keyName) {
        return mRegisteredKeys.get(keyName);
    }

    /**
     * Check if a value is associated with the given key
     * 
     * @param key
     *            the key to be tested
     * @return <code>true</code> if a value is associated with the given key, <code>false</code> otherwise
     */
    public boolean isSet(ParameterKey<?> key) {
        return mParameters.containsKey(key) && mParameters.get(key) != null;
    }

    /**
     * Set the all the unset values to their default.
     * <p>
     * This methods look up for static {@link ParameterKey} fields of the current type and set the corresponding values.
     * </p>
     */
    public void setDefaultValues() {
        for (Field f : this.getClass().getFields()) {
            if (Modifier.isStatic(f.getModifiers())
                    && ParameterKey.class.isAssignableFrom(f.getType())) {
                try {
                    ParameterKey<?> key = (ParameterKey<?>) f.get(null);
                    if (key != null && !isSet(key)) {
                        setNoCheck(key, key.getDefaultValue());
                    }
                } catch (IllegalArgumentException e) {
                    // Do nothing: ignore the field
                } catch (IllegalAccessException e) {
                    // Do nothing: ignore the field
                }

            }
        }
    }

    /**
     * Set a parameter value.
     * 
     * @param key
     *            the key for the parameter to set
     * @param value
     *            the value to be associated with <code>key</code>
     * @return the value previously associated with <code>key</code>
     * @throws IllegalArgumentException
     *             if the <code>key</code> is <code>null</code>
     * @see ParameterKey
     * @see #get(ParameterKey)
     */
    @SuppressWarnings("unchecked")
    public <T> T set(ParameterKey<? super T> key, T value) {
        return (T) setNoCheck(key, value);
    }

    /**
     * Set a parameter value from the name of the key
     * 
     * @param key
     *            the key for the parameter to set
     * @param value
     *            the value to be associated with <code>key</code>
     * @return the value previously associated with <code>key</code>
     * @throws IllegalArgumentException
     *             if the <code>key</code> is <code>null</code>
     * @see #set(ParameterKey, Object)
     */
    public Object set(String key, Object value) {
        if (mRegisteredKeys.containsKey(key))
            return setNoCheck(mRegisteredKeys.get(key), value);
        else
            return setNoCheck(new ParameterKey<Object>(key, Object.class), value);
    }

    /**
     * Set a parameter value without build-in type checking.
     * <p>
     * <b>Use {@link #set(ParameterKey, Object)} when possible</b>
     * 
     * @param key
     *            the key for the parameter to set
     * @param value
     *            the value to be associated with <code>key</code>
     * @return the value previously associated with <code>key</code>
     * @throws IllegalArgumentException
     *             if the <code>key</code> is <code>null</code>
     * @see ParameterKey
     * @see #get(ParameterKey)
     * @see #set(ParameterKey, Object)
     */
    public Object setNoCheck(ParameterKey<?> key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("Argument key cannot be null");
        }
        if (!key.isValueValid(value) && String.class.isAssignableFrom(value.getClass())
                && key.getParser() != null) {
            // Try to cast value
            value = key.getParser().parse((String) value);
        }
        if (!key.isValueValid(value)) {
            throw new IllegalArgumentException(String.format(
                    "The value %s is not valid for parameter %s", value, key));
        }

        Object prev = mParameters.put(key, value);

        // The key wasnt previously registered
        if (registerKey(key)) {
            Logging.getSetupLogger().lowDebug(
                    "Setting parameter asociated with the unregistered key %s", key);
        }

        Logging.getSetupLogger().lowDebug(
                "Setting parameter - key:%s, value:%s, previous value:%s", key, value, prev);

        return prev;
    }

    /**
     * Reading of a parameter value.
     * 
     * @param key
     *            the key for the desired parameter
     * @return the value associated with the given <code>key</code>, <code>null</code> if there is no value associated
     *         with <code>key</code>
     * @throws IllegalArgumentException
     *             if no value is associated with <code>key</code>
     */
    public Object get(String keyName) throws IllegalArgumentException {
        return get(new ParameterKey<>(keyName, Object.class));
    }

    /**
     * Reading of a parameter value.
     * 
     * @param key
     *            the key for the desired parameter
     * @return the value associated with the given <code>key</code>, <code>null</code> if there is no value associated
     *         with <code>key</code>
     * @throws IllegalArgumentException
     *             if no value is associated with <code>key</code>
     */
    @SuppressWarnings("unchecked")
    public <T> T get(ParameterKey<T> key) throws IllegalArgumentException {
        if (key == null) {
            throw new IllegalArgumentException("Argument key cannot be null");
        }
        if (!isSet(key)) {
            Logging.getSetupLogger()
                    .warn("%s.getParameter: no value associated with key %s, returning default value %s",
                            this.getClass().getSimpleName(), key,
                            Utilities.toString(key.getDefaultValue()));
            return key.getDefaultValue();
        } else {
            return (T) mParameters.get(key);
        }
    }

    /**
     * Factory method for parameters which values are types
     * 
     * @param <O>
     *            the type of object to be returned
     * @param key
     *            the key of the considered parameter
     * @param argsTypes
     *            the types of the arguments to be passed to the class constructor
     * @param args
     *            the arguments to be passed to the class constructor (can include <code>null</code> elements)
     * @return a new instance of the class defined as value of the parameter <code>key</code>
     * @throws IllegalArgumentException
     *             if the new object could not be instantiated because of a error in the arguments
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <O> O newInstanceSafe(ParameterKey<Class> key, Class<?>[] argsTypes, Object... args)
            throws IllegalArgumentException {
        Class<O> clazz = get(key);

        if (clazz == null) {
            throw new IllegalArgumentException(String.format(
                    "The value associated with key %s is null", key));
        }

        return Utilities.newInstance(clazz, argsTypes, args);
    }

    /**
     * Factory method for parameters which values are types. <br/>
     * Will deduce the constructor by using the <code>class</code> of the given arguments <code>args</code>
     * 
     * @param <O>
     *            the type of object to be returned
     * @param key
     *            the key of the considered parameter
     * @param args
     *            the arguments to be passed to the class constructor (all elements should be <b>not <code>null</code>
     *            </b>)
     * @return a new instance of the class defined as value of the parameter <code>key</code>
     * @throws IllegalArgumentException
     *             if the new object could not be instantiated because of a error in the arguments
     * @see GlobalParameters#newInstanceSafe(ParameterKey, Class[], Object[])
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <O> O newInstance(ParameterKey<Class> key, Object... args)
            throws IllegalArgumentException {
        Class<O> clazz = get(key);

        if (clazz == null) {
            throw new IllegalArgumentException(String.format(
                    "The value associated with key %s is null", key));
        }

        return Utilities.newInstance(clazz, args);
    }

    /**
     * Reset to their default values all the required parameters
     */
    public void resetDefaultValues() {
        for (ParameterKey<?> key : getRequiredParameters(this.getClass())) {
            resetDefaultValue(key);
        }
    }

    /**
     * Reset the given parameter to its default value
     * 
     * @param the
     *            {@link ParameterKey} describing the considered parameter
     */
    public <T> void resetDefaultValue(ParameterKey<T> key) {
        set(key, key.getDefaultValue());
    }

    /**
     * Remove all mapping between parameters and values
     */
    public void clear() {
        mParameters.clear();
    }

    /**
     * Loading of global parameters from a file, overwriting previous values.
     * 
     * @param file
     *            The file from which global parameters will be loaded
     * @throws Exception
     * @see ParametersFilePersistenceDelegate#loadParameters(GlobalParameters, java.io.File)
     */
    public void loadParameters(java.io.File file) throws Exception {
        getPersistenceDelegate().loadParameters(this, file);
    }

    /**
     * Saving of global parameters to a file
     * 
     * @param file
     *            The file in which global parameters will be saved
     * @param omitDefaults
     *            <code>true</code> if default values should be omitted
     * @throws IOException
     * @see ParametersFilePersistenceDelegate#saveParameters(GlobalParameters, java.io.File, boolean)
     */
    public void saveParameters(java.io.File file, boolean omitDefaults) throws IOException {
        getPersistenceDelegate().saveParameters(this, file, omitDefaults);
    }

    /**
     * Checking of the required parameters
     * 
     * @return a collection containing the missing parameters from the one returned by
     *         {@link GlobalParameters#getRequiredParameters()}
     */
    public final Collection<ParameterKey<?>> checkRequiredParameters() {
        Collection<ParameterKey<?>> missing = new LinkedList<ParameterKey<?>>();

        for (ParameterKey<?> param : getRequiredParameters(this.getClass())) {
            if (!checkParameter(param)) {
                missing.add(param);
            }
        }

        return missing;
    }

    /**
     * Checking of a single parameter
     * 
     * @param key
     * @return <code>true</code> if the parameter associated with <code>key</code> is defined and not <code>null</code>,
     *         <code>false</code> otherwise
     */
    public boolean checkParameter(ParameterKey<?> key) {
        return mParameters.containsKey(key) && mParameters.get(key) != null;
    }

    /**
     * Mapping of parameters to values
     * 
     * @return a copy of the {@link Map} containing the mapping between keys and values
     */
    public Set<Entry<ParameterKey<?>, Object>> getParametersMapping() {
        return mParameters.entrySet();
    }

    /**
     * Mapping of parameters to values as a string
     * 
     * @return a string containing the mapping in the form key=value
     */
    public String getParametersMappingAsString() {
        StringBuilder s = new StringBuilder();
        for (Entry<ParameterKey<?>, Object> e : getParametersMapping()) {
            if (e.getKey() instanceof ClassParameterKey<?> && e.getValue() != null) {
                s.append(e.getKey().getName() + "=" + ((Class<?>) e.getValue()).getCanonicalName());
            } else {
                s.append(e.getKey().getName() + "=" + e.getValue());
            }
            s.append("\n");
        }

        return s.toString();
    }

    @Override
    public String toString() {
        StringBuffer b = new StringBuffer();
        ArrayList<ParameterKey<?>> keys = new ArrayList<ParameterKey<?>>(mParameters.keySet());
        Collections.sort(keys);
        for (ParameterKey<?> key : keys) {
            b.append(key.getName());
            b.append("=");
            b.append(Utilities.toString(get(key)));
            b.append("\n");
        }

        return b.toString();
    }

    /**
     * Clone this instance. Parameter values will have shared references
     */
    @Override
    public GlobalParameters clone() {
        try {
            GlobalParameters clone = getClass().newInstance();
            for (Entry<ParameterKey<?>, Object> e : this.mParameters.entrySet()) {
                clone.setNoCheck(e.getKey(), e.getValue());
            }
            return clone;
        } catch (InstantiationException e) {
            Logging.getBaseLogger().exception("GlobalParameters.clone", e);
            return null;
        } catch (IllegalAccessException e) {
            Logging.getBaseLogger().exception("GlobalParameters.clone", e);
            return null;
        }
    }
}// end GlobalParameters
