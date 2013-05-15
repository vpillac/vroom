package vroom.common.modeling.util;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Test;

import vroom.common.modeling.dataModel.Node;
import vroom.common.modeling.dataModel.Vehicle;
import vroom.common.modeling.dataModel.attributes.PointLocation;
import vroom.common.modeling.util.EuclidianDistance;

/**
 * The Class EuclidianDistanceCostHelperTest is a unit test case for {@link EuclidianDistance}
 */
public class EuclidianDistanceCostHelperTest {

    protected int                         size = 10000;
    protected double[][]                  distances;
    protected Node[]                      nodes;

    protected EuclidianDistance ch;

    /** The nm1m1. */
    private Node                          n00, n11, nm1m1;

    /**
     * Sets the up.
     */
    @org.junit.Before
    public void setUp() {
        ch = new EuclidianDistance();
        n00 = new Node(0, "Node(0,0)", new PointLocation(0, 0));
        n11 = new Node(1, "Node(1,1)", new PointLocation(1, 1));
        nm1m1 = new Node(1, "Node(-1,-1)", new PointLocation(-1, -1));

        Random r = new Random(0);
        nodes = new Node[size];
        distances = new double[size][size];

        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = new Node(i, new PointLocation(r.nextDouble() * 100, r.nextDouble() * 100));
        }

        for (int i = 0; i < nodes.length; i++) {
            for (int j = 0; j < nodes.length; j++) {
                double x = nodes[i].getLocation().getX();
                double y = nodes[i].getLocation().getY();
                double a = nodes[j].getLocation().getX();
                double b = nodes[j].getLocation().getY();
                distances[i][j] = Math.sqrt((x - a) * (x - a) + (y - b) * (y - b));
            }
        }
    }

    /**
     * Test get distance.
     */
    @Test
    public void testGetDistance() {
        assertEquals("Distance between (0,0) and (1,1) is root(2)", Math.pow(2, 0.5),
                ch.getDistance(n00, n11), 1E-10);
        assertEquals("Distance between (0,0) and (-1,-1) is root(2)", Math.pow(2, 0.5),
                ch.getDistance(n00, nm1m1), 1E-10);
        assertEquals("Distance between (1,1) and (-1,-1) is 2", Math.pow(8, 0.5),
                ch.getDistance(n11, nm1m1), 1E-10);

        for (int i = 0; i < nodes.length; i++) {
            for (int j = 0; j < nodes.length; j++) {
                assertEquals(distances[i][j], ch.getCost(nodes[i], nodes[j]), 10e-6);
            }
        }
    }

    /**
     * Test get cost node node.
     */
    @Test
    public void testGetCostNodeNode() {
        assertEquals("Cost between (0,0) and (1,1) is root(2)", Math.pow(2, 0.5),
                ch.getCost(n00, n11), 1E-10);
        assertEquals("Cost between (0,0) and (-1,-1) is root(2)", Math.pow(2, 0.5),
                ch.getCost(n00, nm1m1), 1E-10);
        assertEquals("Cost between (1,1) and (-1,-1) is 2", Math.pow(8, 0.5),
                ch.getCost(n11, nm1m1), 1E-10);
    }

    /**
     * Test get cost node node double.
     */
    @Test
    public void testGetCostNodeNodeDouble() {
        assertEquals("Cost between (0,0) and (1,1) is 21.5*root(2)", 21.5 * Math.pow(2, 0.5),
                ch.getCost(n00, n11, 21.5), 1E-10);
        assertEquals("Cost between (0,0) and (-1,-1) is 21.5*root(2)", 21.5 * Math.pow(2, 0.5),
                ch.getCost(n00, nm1m1, 21.5), 1E-10);
        assertEquals("Cost between (1,1) and (-1,-1) is 21.5*2", 21.5 * Math.pow(8, 0.5),
                ch.getCost(n11, nm1m1, 21.5), 1E-10);
    }

    /**
     * Test get cost node node vehicle of q.
     */
    @Test
    public void testGetCostNodeNodeVehicleOfQ() {
        Vehicle v = new Vehicle(0, "V", 12.0, 68.45, new double[] { 1.0 });
        assertEquals("Cost between (0,0) and (1,1) is 68.45*root(2)", 68.45 * Math.pow(2, 0.5),
                ch.getCost(n00, n11, v), 1E-10);
        assertEquals("Cost between (0,0) and (-1,-1) is 68.45*root(2)", 68.45 * Math.pow(2, 0.5),
                ch.getCost(n00, nm1m1, v), 1E-10);
        assertEquals("Cost between (1,1) and (-1,-1) is 68.45*2", 68.45 * Math.pow(8, 0.5),
                ch.getCost(n11, nm1m1, v), 1E-10);
    }

}
