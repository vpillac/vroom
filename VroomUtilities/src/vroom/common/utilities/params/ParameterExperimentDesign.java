/**
 * 
 */
package vroom.common.utilities.params;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import vroom.common.utilities.SortedProperties;
import vroom.common.utilities.Utilities;
import vroom.common.utilities.logging.Logging;

/**
 * The class <code>ExperimentDesignParameter</code> provides base functionalities for experiment design. It defines a
 * base set of parameters and a set of {@link ParameterValueSet}, as well as methods to generate a list of all the
 * possibles parameter combinations
 * <p>
 * Creation date: Jun 21, 2012 - 11:24:45 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class ParameterExperimentDesign<G extends GlobalParameters> {

    private final G                                              mBaseParameters;

    private final HashMap<ParameterKey<?>, ParameterValueSet<?>> mValueSets;

    /**
     * Creates a new <code>ExperimentDesignParameter</code>
     * 
     * @param baseParameters
     */
    public ParameterExperimentDesign(G baseParameters) {
        mBaseParameters = baseParameters;
        mValueSets = new HashMap<>();
    }

    /**
     * Add a new set of values for a parameter. If a previous set was defined it will be replaced.
     * 
     * @param key
     * @param values
     */
    @SuppressWarnings("unchecked")
    public <T> void addParamValueSet(ParameterKey<T> key, T... values) {
        mValueSets.put(key, new ParameterValueSet<>(key, values));
    }

    public void addParamValueSet(String a) throws ClassNotFoundException {
        String[] p = a.split("=");
        @SuppressWarnings("unchecked")
        ParameterKey<Object> key = (ParameterKey<Object>) mBaseParameters.getRegisteredKey(p[0]);
        if (key == null)
            throw new IllegalArgumentException("Unknown parameter key:" + p[0]);
        String[] stringValues = p[1].substring(1, p[1].length() - 1).split(",");
        Object[] values = new Object[stringValues.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = ParametersFilePersistenceDelegate.castProperty(key, stringValues[i]);
        }
        mValueSets.put(key, new ParameterValueSet<Object>(key, values));
    }

    /**
     * Generate the list of experiments
     * 
     * @return a list containing all the experiments defined in this instance
     */
    public List<ExperimentParameterSetting<G>> getExperiments() {
        ArrayList<ParameterKey<?>> keys = new ArrayList<>(mValueSets.keySet());
        Collections.sort(keys);
        Object[][] matrix = new Object[keys.size()][];
        int combCount = 1;
        for (int k = 0; k < keys.size(); k++) {
            ParameterValueSet<?> s = mValueSets.get(keys.get(k));
            matrix[k] = new Object[s.getValues().length + 1];
            matrix[k][0] = keys.get(k);
            for (int i = 1; i < matrix[k].length; i++) {
                matrix[k][i] = s.getValues()[i - 1];
            }
            combCount *= s.getValues().length;
        }

        ArrayList<ExperimentParameterSetting<G>> experiments = new ArrayList<>(combCount);

        generateCombinations(matrix, experiments, 0, null, null);

        return experiments;
    }

    /**
     * Recursive method that does a deep first exploration of the tree of combinations
     * 
     * @param matrix
     * @param experiments
     * @param depth
     * @param params
     * @param changedValues
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void generateCombinations(Object[][] matrix,
            ArrayList<ExperimentParameterSetting<G>> experiments, int depth, G params,
            HashMap<ParameterKey, Object> changedValues) {
        if (depth == 0) {
            // Root child : initialize data structures
            changedValues = new HashMap<ParameterKey, Object>();
            params = (G) mBaseParameters.clone();
        } else if (depth == matrix.length) {
            // We reached a leaf, add the experiment to the list
            experiments.add(new ExperimentParameterSetting<G>(params, changedValues));
            return;
        }
        for (int i = 1; i < matrix[depth].length; i++) {
            HashMap<ParameterKey, Object> localChangedValues = (HashMap<ParameterKey, Object>) changedValues
                    .clone();
            G localParams = (G) params.clone();
            localChangedValues.put((ParameterKey) matrix[depth][0], matrix[depth][i]);
            localParams.setNoCheck((ParameterKey) matrix[depth][0], matrix[depth][i]);
            generateCombinations(matrix, experiments, depth + 1, localParams, localChangedValues);
        }

    }

    /**
     * Returns a string containing all the parameter keys in the order they will be displayed in each
     * {@link ExperimentParameterSetting}
     * 
     * @return string containing all the parameter keys
     */
    public String getParameterKeys() {
        ArrayList<ParameterKey<?>> keys = new ArrayList<>(mValueSets.keySet());
        Collections.sort(keys);
        return Utilities.toShortString(keys);
    }

    /**
     * Save all the settings contained in this instance into a file
     * 
     * @param file
     * @throws IOException
     */
    public void save(File file) throws IOException {
        SortedProperties prop = new SortedProperties();
        for (Entry<ParameterKey<?>, ParameterValueSet<?>> setting : mValueSets.entrySet()) {
            prop.setProperty(setting.getKey().getName(),
                    Utilities.toShortString(setting.getValue().getValues()));
        }
        FileOutputStream sf = new FileOutputStream(file);
        prop.store(sf, String.format("Created by %s \nDate: %s", this.getClass().getName(),
                new Date(System.currentTimeMillis())));
        sf.flush();
        sf.close();
    }

    /**
     * Load the settings contained in a file in this instance
     * 
     * @param file
     * @param params
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public void load(File file) throws IOException {
        // Loading the properties file
        Properties properties = new Properties();
        FileInputStream sf = new FileInputStream(file);
        properties.load(sf);

        for (String k : properties.stringPropertyNames()) {
            ParameterKey<Object> key = (ParameterKey<Object>) mBaseParameters.getRegisteredKey(k);
            if (key != null) {
                Object[] values = Utilities.toArray(properties.get(k).toString());
                addParamValueSet(key, values);
            } else {
                Logging.getSetupLogger().warn("Unknown key %s - ignoring", k);
            }
        }
    }

    /**
     * Remove all the configurations from this instance
     */
    public void clear() {
        mValueSets.clear();
    }

    /**
     * The class <code>ExperimentParameterSetting</code> defines a parameter setting for an experiment
     * <p>
     * Creation date: Jun 21, 2012 - 2:40:22 PM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     * @param <GG>
     */
    public static class ExperimentParameterSetting<GG extends GlobalParameters> {
        private final GG                            mParameters;
        @SuppressWarnings("rawtypes")
        private final HashMap<ParameterKey, Object> mChangedValues;

        @SuppressWarnings("rawtypes")
        private ExperimentParameterSetting(GG parameters,
                HashMap<ParameterKey, Object> changedValues) {
            super();
            mParameters = parameters;
            mChangedValues = changedValues;
        }

        /**
         * Return the parameter setting of this experiment
         * 
         * @return the parameter setting of this experiment
         */
        public GG getParameters() {
            return mParameters;
        }

        /**
         * Return an array containing all the changed values, sorted according to the alphabetical order of the
         * corresponding keys as in {@link ParameterExperimentDesign#getParameterKeys()}
         * 
         * @return an array containing all the changed values
         * @see ParameterExperimentDesign#getParameterKeys()
         */
        @SuppressWarnings({ "rawtypes", "unchecked" })
        public Object[] getChangedValues() {
            ArrayList<ParameterKey> keys = new ArrayList<>(mChangedValues.keySet());
            Collections.sort(keys);
            Object[] values = new Object[keys.size()];
            int i = 0;
            for (ParameterKey<?> k : keys) {
                values[i++] = mChangedValues.get(k);
            }
            return values;
        }

        /**
         * A comma-separated list of the changed values
         * 
         * @return comma-separated list of the changed values
         */
        public String getChangedValuesString() {
            return Utilities.toShortString(getChangedValues(), ',', false);
        }

        @Override
        public String toString() {
            return mChangedValues.toString();
        }
    }
}
