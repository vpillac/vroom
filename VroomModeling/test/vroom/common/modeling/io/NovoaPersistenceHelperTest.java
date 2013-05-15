package vroom.common.modeling.io;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import vroom.common.modeling.dataModel.StaticInstance;
import vroom.common.modeling.io.NovoaPersistenceHelper;

public class NovoaPersistenceHelperTest {

    private final static File       INSTANCES_DIRECTORY = new File("../Instances/vrpsd/novoa");

    private final static List<File> INSTANCES;

    static {
        String[] children = INSTANCES_DIRECTORY.list();
        if (children == null) {
            // Either dir does not exist or is not a directory
            INSTANCES = null;
        } else {
            INSTANCES = new LinkedList<File>();
            for (String element : children) {
                if (element.startsWith("i_") && !element.startsWith("i_100")
                        && !element.startsWith("i_150")) {
                    INSTANCES.add(new File(INSTANCES_DIRECTORY.getAbsolutePath() + File.separator
                            + element));
                }
            }
        }
    }

    private NovoaPersistenceHelper  parser;

    @Before
    public void setUp() {
        parser = new NovoaPersistenceHelper();
    }

    @Test
    public void testReadInstance() {
        parser.setExpectedFillingFactor(1.75);

        for (File f : INSTANCES) {
            try {
                System.out.println("-------------------------------------");
                System.out.println("Reading instance " + f.getName());

                StaticInstance instance = parser.readInstance(f);

                System.out.println("Filling factor   : " + parser.getExpectedFillingFactor());
                System.out.println("Instance         : " + instance);
                System.out.println("Vehicle capacity : "
                        + instance.getFleet().getVehicle(0).getCapacity());
                System.out.println("Requests         : " + instance.getRequests());

            } catch (Exception e) {
                fail(f.getName() + ": " + e.toString());
                // e.printStackTrace();
            }
        }
    }

}
