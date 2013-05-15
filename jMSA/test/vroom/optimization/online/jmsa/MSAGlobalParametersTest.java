package vroom.optimization.online.jmsa;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import vroom.common.utilities.params.ParameterKey;
import vroom.optimization.online.jmsa.benchmarking.NovoaRun;
import vroom.optimization.online.jmsa.components.ScenarioOptimizerBase;

public class MSAGlobalParametersTest {

    @Test
    public void testLoadParameters() {
        System.out.println("Testing MSAGlobalParameters.loadParameters");

        // Save and load all default parameters
        MSAGlobalParameters dafaults = new MSAGlobalParameters();
        NovoaRun.loadDefaultParameters(dafaults);

        String file = "data/test/MSAGlobalParametersTest.loadParams";

        try {
            dafaults.saveParameters(new File(file), true);

            MSAGlobalParameters loadedDefaults = new MSAGlobalParameters();
            loadedDefaults.loadParameters(new File(file));
            for (ParameterKey<?> key : loadedDefaults.getRegisteredKeys()) {
                assertEquals(String.format("Different values for key %s: original: %s, loaded: %s\n", key,
                        dafaults.get(key), loadedDefaults.get(key)), dafaults.get(key), loadedDefaults.get(key));
            }
            for (ParameterKey<?> key : dafaults.getRegisteredKeys()) {
                assertEquals(String.format("Different values for key %s: original: %s, loaded: %s\n", key,
                        dafaults.get(key), loadedDefaults.get(key)), dafaults.get(key), loadedDefaults.get(key));
            }
        } catch (Exception e) {
            fail(String.format("%s (%s)", e.getClass().getSimpleName(), e.getMessage()));
        }

        file = "data/test/MSAGlobalParametersTest.loadParamsSingle";
        // Save only one parameter
        MSAGlobalParameters subset = new MSAGlobalParameters();
        subset.set(MSAGlobalParameters.SCENARIO_OPTIMIZER_CLASS, ScenarioOptimizerBase.class);

        try {
            subset.saveParameters(new File(file), true);

            MSAGlobalParameters loadedSubset = new MSAGlobalParameters();
            NovoaRun.loadDefaultParameters(loadedSubset);
            loadedSubset.loadParameters(new File(file));
            for (ParameterKey<?> key : loadedSubset.getRegisteredKeys()) {
                if (key != MSAGlobalParameters.SCENARIO_OPTIMIZER_CLASS) {
                    assertEquals(
                            String.format("Different values for key %s: original: %s, loaded: %s\n", key,
                                    dafaults.get(key), loadedSubset.get(key)), dafaults.get(key), loadedSubset.get(key));
                } else {
                    assertEquals("Value of key SCENARIO_OPTIMIZER_CLASS should be ScenarioOptimizerBase",
                            ScenarioOptimizerBase.class, loadedSubset.get(key));
                }
            }
        } catch (Exception e) {
            fail(String.format("%s (%s)", e.getClass().getSimpleName(), e.getMessage()));
            System.exit(1);
        }
    }

    @Test
    public void testSaveParameters() {
        System.out.println("Testing MSAGlobalParameters.saveParameters");
        MSAGlobalParameters params = new MSAGlobalParameters();
        NovoaRun.loadDefaultParameters(params);

        String file = "data/test/MSAGlobalParametersTest..saveParams";

        try {
            System.out.println("Savings params to file: " + file);
            params.saveParameters(new File(file), true);

        } catch (IOException e) {
            fail(String.format("%s (%s)", e.getClass().getSimpleName(), e.getMessage()));
        }

    }

}
