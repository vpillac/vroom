package vroom.common.modeling.dataModel;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import vroom.common.modeling.dataModel.DynamicInstance;
import vroom.common.modeling.dataModel.Node;
import vroom.common.modeling.dataModel.Request;
import vroom.common.modeling.dataModel.VehicleRoutingProblemDefinition;
import vroom.common.modeling.dataModel.attributes.PointLocation;
import vroom.common.modeling.util.VRPInstanceBuilder;

/**
 * The Class DynamicInstanceTest is a unit test class for {@link DynamicInstance}
 */
public class DynamicInstanceTest {

    /** The instance. */
    DynamicInstance instance;

    /**
     * Sets the up.
     */
    @Before
    public void setUp() {
        instance = VRPInstanceBuilder.newSimpleDynamicInstance(20, 20, 10, 36, 0, true);
    }

    /**
     * Test add request.
     */
    @Test
    public void testAddRequest() {
        System.out.println("addRequest");
        System.out.println("--------------------------");
        int c = instance.getRequestCount();
        System.out.println("Request count:" + c);
        instance.addRequest(new Request(0, new Node(99, "TestNode", new PointLocation(10, 10))));
        assertEquals("Instance should have " + (c + 1) + " requests", c + 1,
                instance.getRequestCount());
        System.out.println("New request count:" + c);
    }

    /**
     * Test get request count.
     */
    @Test
    public void testGetRequestCount() {
        System.out.println("getRequestCount");
        System.out.println("--------------------------");
        assertEquals("Instance should contain 20 requests", 20, instance.getRequestCount());
    }

    /**
     * Test get request.
     */
    @Test
    public void testGetRequest() {
    }

    /**
     * Test get requests.
     */
    @Test
    public void testGetRequests() {
        System.out.println("getRequests");
        System.out.println("--------------------------");
        System.out.println(instance.getRequests());
    }

    /**
     * Test get cost node node.
     */
    @Test
    public void testGetCostNodeNode() {
        System.out.println("testGetCostNodeNode");
        System.out.println("--------------------------");
    }

    /**
     * Test get cost node node vehicle of q.
     */
    @Test
    public void testGetCostNodeNodeVehicleOfQ() {
        System.out.println("testGetCostNodeNodeVehicleOfQ");
        System.out.println("--------------------------");
    }

    /**
     * Test get depot.
     */
    @Test
    public void testGetDepot() {
        System.out.println("getDepot");
        System.out.println("--------------------------");
        System.out.println(instance.getDepot(0));
    }

    /**
     * Test get fleet.
     */
    @Test
    public void testGetFleet() {
        System.out.println("getFleet");
        System.out.println("--------------------------");
        System.out.println(instance.getFleet());
    }

    /**
     * Test get depot count.
     */
    @Test
    public void testGetDepotCount() {
        System.out.println("getDepotCount");
        System.out.println("--------------------------");
        assertEquals("Instance has 1 depot", 1, instance.getDepotCount());
    }

    /**
     * Test get routing problem.
     */
    @Test
    public void testGetRoutingProblem() {
        System.out.println("getRoutingProblem");
        System.out.println("--------------------------");
        assertEquals("The Routing Problem should be VehicleRoutingProblemDefinition.DynVRP",
                VehicleRoutingProblemDefinition.DynVRP, instance.getRoutingProblem());
        System.out.println(instance.getRoutingProblem());
    }

}
