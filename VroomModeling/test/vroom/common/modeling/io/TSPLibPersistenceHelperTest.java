/**
 * 
 */
package vroom.common.modeling.io;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.dataModel.StaticInstance;
import vroom.common.modeling.io.NovoaPersistenceHelper;
import vroom.common.modeling.io.TSPLibPersistenceHelper;

/**
 * <code>TSPLibPersistenceHelperTest</code> is a simple test case for the {@link TSPLibPersistenceHelper} class
 * <p>
 * Creation date: Jul 6, 2010 - 6:57:20 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TSPLibPersistenceHelperTest {

    private final static File       INSTANCES_DIRECTORY = new File("../Instances/cvrp/augerat");

    private final static List<File> INSTANCES;

    static {
        String[] children = INSTANCES_DIRECTORY.list();
        if (children == null) {
            // Either dir does not exist or is not a directory
            INSTANCES = null;
        } else {
            INSTANCES = new LinkedList<File>();
            for (String file : children) {
                if (!file.contains("info") && !file.equals(".svn")) {
                    try {
                        INSTANCES.add(new File(INSTANCES_DIRECTORY.getAbsolutePath()
                                + File.separator + file));
                    } catch (Exception e) {
                        System.err.printf("Error when reading %s : %s", file, e.getMessage());
                    }
                }
            }
        }
    }

    private TSPLibPersistenceHelper parser;

    @Before
    public void setUp() {
        parser = new TSPLibPersistenceHelper();
    }

    @Test
    public void testReadInstance() {

        for (File f : INSTANCES) {
            try {
                System.out.println("-------------------------------------");
                System.out.println("Reading instance " + f.getName());

                StaticInstance instance = (StaticInstance) parser.readInstance(f);
                System.out.println("Instance         : " + instance);
                System.out.println("Size             : " + instance.getRequestCount());
                System.out.println("Vehicle capacity : "
                        + instance.getFleet().getVehicle(0).getCapacity());
                System.out.println("Requests         : " + instance.getRequests());

            } catch (IOException e) {
                fail(e.getMessage());
                // e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        // TSPLibPersistenceHelperTest test = new TSPLibPersistenceHelperTest();
        // test.setUp();
        // test.testReadInstance();

        NovoaPersistenceHelper r = new NovoaPersistenceHelper();
        try {
            writeInstance(r.readInstance(60, 1, 3, 1, 10l), "test.vrp");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void writeInstance(IVRPInstance instance, String file) {
        TSPLibPersistenceHelper w = new TSPLibPersistenceHelper();
        try {
            w.writeInstance(instance, new File(file), "Test");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
