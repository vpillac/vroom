/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vroom.common.modeling.util;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Test;

import vroom.common.modeling.dataModel.Node;
import vroom.common.modeling.dataModel.attributes.PointLocation;
import vroom.common.modeling.util.BufferedDistance;
import vroom.common.modeling.util.EuclidianDistance;
import vroom.common.utilities.Stopwatch;

/**
 * @author victor
 */
public class BufferedCostHelperTest {

    protected int                         size = 1000;
    protected double[][]                  distances;
    protected Node[]                      nodes;

    protected EuclidianDistance ch;
    BufferedDistance            bch;

    @org.junit.Before
    public void setUp() {
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

        ch = new EuclidianDistance();
        bch = new BufferedDistance(ch);
    }

    @org.junit.After
    public void tearDown() {
        bch.clear();
        bch = null;
        ch = null;
        System.gc();
    }

    @Test
    public void testGetDistance() {
        System.out.println("Test getDistance");
        System.out.println("----------------------------------");

        loadBuffer();
        for (int i = 0; i < nodes.length; i++) {
            for (int j = 0; j < nodes.length; j++) {
                // System.out.printf("(%s,%s)%s",i,j,(i*nodes.length+j)%20==0?"\n":"");
                assertEquals(distances[i][j], bch.getDistance(nodes[i], nodes[j]), 10e-6);
            }
        }
        System.out.println();
        System.out.println("----------------------------------");
    }

    @Test
    public void testGetCost() {
        System.out.println("Test getCost");
        System.out.println("----------------------------------");

        loadBuffer();
        for (int i = 0; i < nodes.length; i++) {
            for (int j = 0; j < nodes.length; j++) {
                // System.out.printf("(%s,%s)%s",i,j,(i*nodes.length+j)%20==0?"\n":"");
                assertEquals(distances[i][j], bch.getCost(nodes[i], nodes[j]), 10e-6);
            }
        }
        System.out.println();
        System.out.println("----------------------------------");
    }

    public void loadBuffer() {
        for (Node node : nodes) {
            for (Node node2 : nodes) {
                bch.getCost(node, node2);
            }
        }
    }

    public void clearBuffer() {
        bch.clear();
    }

    public void performanceTest() {
        System.out.println("Performance Test");
        System.out.println("----------------------------------");
        Stopwatch t = new Stopwatch();
        Random r = new Random(1);

        System.out.println("Running tests for euclidian only");
        System.out.println("----------------------------------");
        t.start();
        for (int k = 0; k < 1000 * size; k++) {
            ch.getDistance(nodes[r.nextInt(size)], nodes[r.nextInt(size)]);
        }
        t.stop();
        System.out.println("Euclidian total time :" + t.readTimeMS());

        r = new Random(1);

        System.out.println("Running tests for buffered euclidian");
        System.out.println("----------------------------------");
        t.start();
        for (int k = 0; k < 2 * size; k++) {
            bch.getDistance(nodes[r.nextInt(size)], nodes[r.nextInt(size)]);
        }
        t.stop();
        System.out.println("Buffered total time :" + t.readTimeMS());
        System.out.println("----------------------------------");
    }

    public static void main(String[] args) {
        BufferedCostHelperTest test = new BufferedCostHelperTest();

        test.setUp();

        // test.testGetDistance();

        // test.testGetCost();

        test.performanceTest();
    }

}
