package vroom.common.utilities.params;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import vroom.common.utilities.SortedProperties;
import vroom.common.utilities.Utilities;
import vroom.common.utilities.logging.Logging;

/**
 * Creation date: Feb 25, 2010 - 3:13:09 PM<br/>
 * <code>GlobalParametersPersistenceDelegate</code> is a delegate class used to write and read {@link GlobalParameters}
 * instance to and from a {@link Properties property file}.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class ParametersFilePersistenceDelegate implements IParametersPersistenceDelegate<File> {

    public static final String CFG_PARENT = "CFG_PARENT";

    @Override
    public void loadParameters(GlobalParameters params, File file) throws Exception {
        // Loading the properties file
        Properties properties = new Properties();
        FileInputStream sf = new FileInputStream(file);
        properties.load(sf);

        // Check for a parent configuration file
        Object parent = properties.get(CFG_PARENT);
        if (parent != null && parent instanceof String) {
            File parentCfg = new File((String) parent);
            if (!parentCfg.equals(file))
                params.loadParameters(parentCfg);
        }

        Logging.getSetupLogger().info(
                "ParametersFilePersistenceDelegate.loadParameters: loading parameters from file %s", file);

        // Get the keys from the property file
        Set<String> propKeys = properties.stringPropertyNames();
        // Get the registered keys from the parameters
        Set<ParameterKey<?>> registeredKeys = params.getRegisteredKeys();

        // Registered keys map
        HashMap<String, ParameterKey<?>> regKeysMap = new HashMap<String, ParameterKey<?>>();
        for (ParameterKey<?> regKey : registeredKeys) {
            regKeysMap.put(regKey.getName(), regKey);
        }

        // Add the properties content
        for (String k : propKeys) {
            if (regKeysMap.containsKey(k)) {
                ParameterKey<?> pK = regKeysMap.get(k);

                setRegisteredParameter(params, pK, properties.getProperty(k));
            } else {
                setUnregisteredParameter(params, k, properties.getProperty(k));
            }
        }

        sf.close();
    }

    /**
     * Handling of registered keys
     * 
     * @param params
     * @param key
     * @param value
     * @throws ClassNotFoundException
     */
    protected void setRegisteredParameter(GlobalParameters params, ParameterKey<?> key, String value)
            throws ClassNotFoundException {
        params.setNoCheck(key, castProperty(key, value));
    }

    /**
     * Special handling of keys that have not been found as registered
     * 
     * @param params
     *            the parameters being loaded
     * @param key
     *            the unregistered key
     * @param value
     *            the associated value in the property file
     * @throws Exception
     */
    protected void setUnregisteredParameter(GlobalParameters params, String key, String value) throws Exception {
        params.setNoCheck(new ParameterKey<Object>(key, Object.class), value);
    }

    /**
     * Try to cast <code>value</code> to the correct type
     * 
     * @param <T>
     * @param key
     * @param value
     * @return
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    public static Object castProperty(ParameterKey<?> key, String value) throws ClassNotFoundException {
        if (key == null || key.getType() == null)
            return value;
        else if (String.class.isAssignableFrom(key.getType())) {
            return value;
        } else if (Boolean.class.isAssignableFrom(key.getType())) {
            return Boolean.valueOf(value);
        } else if (Integer.class.isAssignableFrom(key.getType())) {
            return Integer.valueOf(value);
        } else if (Long.class.isAssignableFrom(key.getType())) {
            return Long.valueOf(value);
        } else if (Double.class.isAssignableFrom(key.getType())) {
            return Double.valueOf(value);
        } else if (Float.class.isAssignableFrom(key.getType())) {
            return Float.valueOf(value);
        } else if (File.class.isAssignableFrom(key.getType())) {
            return new File(value);
        } else if (key instanceof ClassParameterKey<?>) {
            return Class.forName(value);
        } else if (key.getType().isArray()) {
            Class<?> compType = key.getType().getComponentType();
            if (int.class.isAssignableFrom(compType)) {
                return Utilities.toIntArray(value);
            } else if (double.class.isAssignableFrom(compType)) {
                return Utilities.toDoubleArray(value);
            } else if (long.class.isAssignableFrom(compType)) {
                return Utilities.toLongArray(value);
            } else {
                throw new UnsupportedOperationException(String.format("Value for key %s could not be cast (%s)", key,
                        value));
            }
        } else if (key.getType().isEnum()) {
            @SuppressWarnings("rawtypes")
            Class<? extends Enum> enumType = (Class<? extends Enum>) key.getType();
            return Enum.valueOf(enumType, value);
        }
        // else {
        // throw new UnsupportedOperationException(
        // String.format("Value for key %s could not be cast (%s)", key, value));
        // }
        return value;
    }

    @Override
    public void saveParameters(GlobalParameters params, File file, boolean omitDefaults) throws IOException {
        SortedProperties settings = new SortedProperties();

        for (Entry<ParameterKey<?>, Object> e : params.getParametersMapping()) {
            if (e.getValue() != null && (!omitDefaults || !Utilities.equal(e.getKey().getDefaultValue(), e.getValue()))) {
                if (e.getKey() instanceof ClassParameterKey<?>) {
                    settings.put(e.getKey().getName(), ((Class<?>) e.getValue()).getCanonicalName());
                } else if (e.getValue().getClass().isArray()) {
                    settings.put(e.getKey().getName(), Utilities.toShortString(e.getValue()));
                } else {
                    settings.put(e.getKey().getName(), e.getValue().toString());
                }
            }
        }

        FileOutputStream sf = new FileOutputStream(file);
        settings.store(
                sf,
                String.format("Created by %s \nDate: %s", this.getClass().getName(),
                        new Date(System.currentTimeMillis())));
        sf.flush();
        sf.close();

        Logging.getSetupLogger().info("ParametersFilePersistenceDelegate.loadParameters: parameters saved to file %s",
                file);
    }

}
