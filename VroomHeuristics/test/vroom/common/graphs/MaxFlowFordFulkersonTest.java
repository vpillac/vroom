package vroom.common.graphs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;

import org.junit.Test;

import vroom.common.utilities.graphs.CompleteGraph;
import vroom.common.utilities.graphs.Cut;

public class MaxFlowFordFulkersonTest {

    int seed        = 0;
    int repetitions = 10;

    @Test
    public void testMinCut() {
        System.out.println("---------------------------------");
        System.out.println("  testMinCut");
        System.out.println("---------------------------------");

        Random rnd = new Random(0);

        for (int r = 0; r < repetitions; r++) {
            int size = (rnd.nextInt(5) + 1) * 10;
            boolean integer = rnd.nextBoolean();
            CompleteGraph graph = new CompleteGraph(true, size);
            System.out.println("Graph: " + graph);
            System.out.println("Integer: " + integer);
            for (int i = 0; i < size; i++) {
                for (int j = i + 1; j < size; j++) {
                    if (integer) {
                        graph.setArcCapacity(i, j, rnd.nextInt(10) + 1);
                    } else {
                        graph.setArcCapacity(i, j, rnd.nextDouble() * 10);
                    }
                }
            }

            MaxFlowFordFulkerson mf = new MaxFlowFordFulkerson(graph);

            boolean[][] tested = new boolean[size][size];
            for (int i = 0; i < size; i++) {
                for (int j = i + 1; j < size; j++) {
                    if (!tested[i][j]) {
                        mf.reset();
                        Cut c = mf.minCut(i, j);

                        double minCut = c.getCapacity();
                        double maxFlow = mf.getCurrentMaxFlow();

                        // System.out.printf("Min cut (%s,%s): %s\n",i,j,c);
                        // System.out.println(String.format("Max Flow : %s", maxFlow));

                        for (int s : c) {
                            for (int t : c.getComplement()) {
                                tested[s][t] = true;
                                assertEquals(
                                        String.format("Cut Arc (%s,%s) is not saturated", s, t), 0,
                                        graph.getArcCapacity(s, t) - mf.getCurrentFlow(s, t), 1e-10);
                            }
                        }

                        boolean marked[] = new boolean[size];
                        LinkedList<Integer> stack = new LinkedList<Integer>();
                        stack.add(i);
                        marked[i] = true;
                        int count = 0;
                        Set<Integer> myCut = new HashSet<Integer>();
                        myCut.add(i);
                        while (!stack.isEmpty()) {
                            int node = stack.pop();
                            count++;
                            for (int suc = 0; suc < size; suc++) {
                                // if(graph.getArcCapacity(node, suc)-mf.getCurrentFlow(node, suc)>1e-6)
                                // System.out.printf("(%s,%s)=%s (%s)\n",node,suc,graph.getArcCapacity(node,
                                // suc)-mf.getCurrentFlow(node, suc),mf.getCurrentResidual(node, suc));
                                if (!marked[suc]
                                        && graph.getArcCapacity(node, suc)
                                                - mf.getCurrentFlow(node, suc) > 1e-6) {
                                    if (suc == j) {
                                        fail("Improving path found");
                                    }

                                    marked[suc] = true;
                                    myCut.add(suc);
                                    stack.add(suc);
                                    assertTrue("Cut should contain " + suc, c.contains(suc));
                                }
                            }
                        }

                        assertEquals("Cut should be of size " + count, count, c.size());
                        assertEquals("Min Cut and Max Flow should have the same value", maxFlow,
                                minCut, 1e-10);

                    }
                }
            }
        }

    }

    @Test
    public void testMaxFlow() {
        fail("Not yet implemented");
    }

    @Test
    public void testImprovingPath() {
        fail("Not yet implemented");
    }

    public static void main(String[] args) {
        MaxFlowFordFulkersonTest test = new MaxFlowFordFulkersonTest();

        test.testMinCut();
    }
}
