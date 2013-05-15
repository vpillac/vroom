package vroom.common.utilities.graphs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class CutTest {

    int           size = 20;

    CompleteGraph graph;
    Cut           cut;

    @Before
    public void setUp() {
        double[][] capacityMatrix = new double[size][size];
        double[][] costMatrix = new double[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                capacityMatrix[i][j] = 1;
            }
        }

        graph = new CompleteGraph(true, costMatrix, capacityMatrix);
        cut = new Cut(graph);
    }

    @Test
    public void testAddNode() {
        for (int i = 0; i < size; i++) {
            assertTrue("Cut should not contain node " + i, cut.add(i));
            assertTrue("Cut should contain node " + i, cut.contains(i));
        }

    }

    @Test
    public void testRemoveNode() {
        for (int i = 0; i < size; i++) {
            cut.add(i);
        }
        for (int i = 0; i < size; i++) {
            assertTrue("Cut should contain node " + i, cut.remove(i));
            assertTrue("Cut should not contain node " + i, !cut.contains(i));
        }
    }

    @Test
    public void testGetCut() {
        boolean[] contains = new boolean[size];

        for (int k = 1; k < size; k++) {
            int elem = 0;
            for (int i = 0; i <= k; i++) {
                cut.add(i);
                contains[i] = true;
                elem++;
            }

            for (int i : cut.getCut()) {
                assertTrue(contains[i]);
            }

            assertEquals(elem, cut.getCut().size());
        }
    }

    @Test
    public void testGetComplement() {
        boolean[] contains = new boolean[size];
        for (int k = 1; k < size; k++) {
            int elem = size;
            for (int i = 0; i <= k; i++) {
                cut.add(i);
                contains[i] = true;
                elem--;
            }

            for (int i : cut.getComplement()) {
                assertTrue(!contains[i]);
            }

            assertEquals(elem, cut.getComplement().size());
        }
    }

    @Test
    public void testGetCapacity() {
        boolean[] contains = new boolean[size];
        for (int k = 1; k < size; k++) {
            int elem = 0;
            for (int i = 0; i <= k; i++) {
                cut.add(i);
                contains[i] = true;
                elem++;
            }

            assertEquals(elem * (size - elem), cut.getCapacity(), 1e-12);
        }
    }

}
