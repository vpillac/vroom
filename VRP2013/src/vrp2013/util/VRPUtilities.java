/**
 * 
 */
package vrp2013.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

import vroom.common.modeling.dataModel.StaticInstance;
import vroom.common.modeling.io.VRPRepPersistenceHelper;
import vroom.common.utilities.BestKnownSolutions;
import vroom.common.utilities.Utilities;
import vroom.common.utilities.logging.Logging;

/**
 * <code>VRPUtilities</code> contains utility methods used in the VRP 2013 examples.
 * <p>
 * Creation date: 07/05/2013 - 5:37:36 PM
 * 
 * @author Victor Pillac, <a href="http://www.nicta.com.au">National ICT Australia</a>, <a
 *         href="http://www.victorpillac.com">www.victorpillac.com</a>
 * @version 1.0
 */
public class VRPUtilities {

    static {
        setup("./instances/examples");
    }

    private static StaticInstance[]   INSTANCES;
    private static File[]             INSTANCE_FILES;
    private static BestKnownSolutions BKS;

    /**
     * Setup the instance dir
     * 
     * @param instanceDir
     */
    public static void setup(String instanceDir) {
        System.out.println("Using instances in folder " + instanceDir);
        String bksFile = String.format("%s.sol", instanceDir);

        BKS = new BestKnownSolutions(bksFile);

        List<File> instanceFiles = Collections.emptyList();
        try {
            instanceFiles = Utilities.listFiles(instanceDir, ".+xml.zip");
        } catch (FileNotFoundException e) {
            Logging.getBaseLogger().exception("VRPUtilities.", e);
        }
        INSTANCE_FILES = instanceFiles.toArray(new File[instanceFiles.size()]);
        INSTANCES = new StaticInstance[instanceFiles.size()];
    }

    /**
     * Returns the number of available instances
     * 
     * @return
     */
    public static int getInstanceCount() {
        return INSTANCES.length;
    }

    /**
     * Ask the user to select an instance
     * 
     * @return the instance selected by the user
     */
    public static StaticInstance pickInstance() {
        System.out.printf("VRPUtilities: %s instances are available\n", getInstanceCount());
        for (int i = 0; i < INSTANCE_FILES.length; i++) {
            System.out.printf("%3s: %s\n", i, INSTANCE_FILES[i].getName());
        }

        InputStreamReader istream = new InputStreamReader(System.in);
        BufferedReader bufRead = new BufferedReader(istream);

        StaticInstance instance = null;
        while (instance == null) {
            try {
                System.out.printf("Select an instance number (between 0 and %s): ",
                        getInstanceCount() - 1);
                String num = bufRead.readLine();
                instance = pickInstance(Integer.valueOf(num));
            } catch (Exception e) {
                System.out.printf("An error occured (%s)\n", e.getClass().getSimpleName());
            }
        }

        return instance;

    }

    /**
     * Load and return an instance from its id
     * 
     * @param instance
     * @return the instance number {@code  instance}
     * @see #getInstanceCount()
     */
    public static StaticInstance pickInstance(int instance) {
        if (INSTANCES[instance] == null)
            INSTANCES[instance] = loadInstance(INSTANCE_FILES[instance]);
        return INSTANCES[instance];
    }

    public static BestKnownSolutions getBKS() {
        return BKS;
    }

    /**
     * Load and return the instance contained in file {@code  instanceFile}
     * 
     * @param instanceFile
     *            the instance file
     * @return the instance contained in {@code  instanceFile}
     */
    public static StaticInstance loadInstance(String instanceFile) {
        return loadInstance(new File(instanceFile));
    }

    /**
     * Load and return the instance contained in file {@code  instanceFile}
     * 
     * @param instanceFile
     *            the path of the instance file
     * @return the instance contained in {@code  instanceFile}
     */
    public static StaticInstance loadInstance(File instanceFile) {
        VRPRepPersistenceHelper in = new VRPRepPersistenceHelper();

        StaticInstance instance = null;
        try {
            instance = (StaticInstance) in.readInstance(instanceFile);
        } catch (Exception e) {
            Logging.getBaseLogger().exception("VRPUtilities.loadInstance", e);
        }

        VRPLogging.getBenchLogger().info("Loaded instance %s - %s reqs. fleet:%s", instance,
                instance.getRequestCount(), instance.getFleet());

        return instance;
    }

}
