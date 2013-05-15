package vroom.common.modeling.io;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import vroom.common.modeling.dataModel.StaticInstance;
import vroom.common.modeling.io.SolomonPersistenceHelper;

public class SolomonPersistenceHelperTest {

    private final static File        INSTANCES_DIRECTORY = new File("../Instances/vrptw/solomon");

    private final static List<File>  INSTANCES;

    static {
        String[] children = INSTANCES_DIRECTORY.list();
        if (children == null) {
            // Either dir does not exist or is not a directory
            INSTANCES = null;
        } else {
            INSTANCES = new LinkedList<File>();
            for (int i = 0; i < children.length; i++) {
                if (!children[i].contains("info") && !children[i].startsWith(".")) {
                    INSTANCES.add(new File(INSTANCES_DIRECTORY.getAbsolutePath() + File.separator
                            + children[i]));
                }
            }
        }
    }

    private SolomonPersistenceHelper parser;

    @Before
    public void setUp() {
        parser = new SolomonPersistenceHelper();
    }

    @Test
    public void testReadInstance() {

        for (File f : INSTANCES) {
            try {
                System.out.println("-------------------------------------");
                System.out.println("Reading instance " + f.getName());

                StaticInstance instance = (StaticInstance) parser.readInstance(f);
                System.out.println("Instance         : " + instance);
                System.out.println("Vehicle capacity : "
                        + instance.getFleet().getVehicle(0).getCapacity());
                System.out.println("Requests         : " + instance.getRequests());

            } catch (IOException e) {
                fail(e.getMessage());
                // e.printStackTrace();
            }
        }
    }

}
