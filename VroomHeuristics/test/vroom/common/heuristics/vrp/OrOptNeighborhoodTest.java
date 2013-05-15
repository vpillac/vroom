/**
 * 
 */
package vroom.common.heuristics.vrp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import vroom.common.heuristics.ConstraintHandler;
import vroom.common.heuristics.vrp.constraints.CapacityConstraint;
import vroom.common.modeling.dataModel.ListRoute.ArrayListRoute;
import vroom.common.modeling.dataModel.Fleet;
import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.dataModel.Node;
import vroom.common.modeling.dataModel.Solution;
import vroom.common.modeling.dataModel.Vehicle;
import vroom.common.modeling.util.CircularInstanceGenerator;
import vroom.common.modeling.util.SolutionChecker;
import vroom.common.utilities.dataModel.ObjectWithIdComparator;

/**
 * <code>OrOptNeighborhoodTest</code> is a test case for {@link OrOptNeighborhood}
 * <p>
 * Creation date: Jul 8, 2010 - 5:24:10 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class OrOptNeighborhoodTest {

    static int                                          sNumNodes = 10;

    private Solution<ArrayListRoute>                    solution;
    private IVRPInstance                                instance;

    private OrOptNeighborhood<Solution<ArrayListRoute>> neighborhood;

    @Before
    public void setUp() throws Exception {
        System.out.println("--------------------------------");
        System.out.println(" SETUP");
        List<Node> nodes = new LinkedList<Node>();
        instance = CircularInstanceGenerator.newCircularInstance(sNumNodes, nodes, 10);
        instance.setFleet(Fleet.newHomogenousFleet(2, new Vehicle(0, "V", sNumNodes / 2 + 1)));

        solution = new Solution<ArrayListRoute>(instance);
        ArrayListRoute route1 = new ArrayListRoute(solution, instance.getFleet().getVehicle());
        ArrayListRoute route2 = new ArrayListRoute(solution, instance.getFleet().getVehicle());

        solution.addRoute(route1);
        solution.addRoute(route2);

        route1.appendNode(instance.getDepotsVisits().iterator().next());
        route2.appendNode(instance.getDepotsVisits().iterator().next());

        ArrayList<INodeVisit> visits = new ArrayList<INodeVisit>(instance.getNodeVisits());
        Collections.sort(visits, new ObjectWithIdComparator());
        for (int i = 0; i < visits.size(); i++) {
            if (i < visits.size() / 2) {
                route1.appendNode(visits.get(i));
            } else {
                route2.appendNode(visits.get(i));
            }
        }

        route1.appendNode(instance.getDepotsVisits().iterator().next());
        route2.appendNode(instance.getDepotsVisits().iterator().next());

        System.out.println("Dummy solution initialized:");
        System.out.println(solution);

        neighborhood = new OrOptNeighborhood<Solution<ArrayListRoute>>(
                new ConstraintHandler<Solution<ArrayListRoute>>());
        neighborhood.getConstraintHandler().addConstraint(
                new CapacityConstraint<Solution<ArrayListRoute>>());

    }

    /**
     * Test method for
     * {@link vroom.common.heuristics.vrp.OrOptNeighborhood#executeMove(vroom.common.modeling.dataModel.IVRPSolution, vroom.common.heuristics.Move)}
     * .
     */
    @Test
    public void testExecuteMove() {
        System.out.println("--------------------------------");
        System.out.println("testExecuteMove");
        System.out.println("--------------------------------");

        Solution<ArrayListRoute> sol = solution.clone();

        System.out.println("Solution: " + sol);

        OrOptMove<Solution<ArrayListRoute>> move = new OrOptMove<Solution<ArrayListRoute>>(
                solution, 0, 1, 2);
        move.setInsertion(1, 3, false);
        int[] exp1 = new int[] { 0, 3, 4, 5, 0 };
        int[] exp2 = new int[] { 0, 6, 7, 1, 2, 8, 9, 10, 0 };
        System.out.println("Move    : " + move);
        System.out.printf("Expected: %s %s\n", Arrays.toString(exp1), Arrays.toString(exp2));
        assertTrue("Move should execute correctly " + move.toString(),
                neighborhood.executeMove(sol, move));
        System.out.println("Result  : " + sol);
        for (int i = 0; i < exp1.length; i++) {
            assertEquals(exp1[i], sol.getRoute(0).getNodeAt(i).getID());
        }
        for (int i = 0; i < exp2.length; i++) {
            assertEquals(exp2[i], sol.getRoute(1).getNodeAt(i).getID());
        }
        System.out.println();
        sol = solution.clone();
        System.out.println("Solution: " + sol);
        move = new OrOptMove<Solution<ArrayListRoute>>(solution, 0, 1, 2);
        move.setInsertion(1, 3, true);
        exp1 = new int[] { 0, 3, 4, 5, 0 };
        exp2 = new int[] { 0, 6, 7, 2, 1, 8, 9, 10, 0 };
        System.out.println("Move    : " + move);
        System.out.printf("Expected: %s %s\n", Arrays.toString(exp1), Arrays.toString(exp2));
        assertTrue("Move should execute correctly " + move.toString(),
                neighborhood.executeMove(sol, move));
        System.out.println("Result  : " + sol);
        for (int i = 0; i < exp1.length; i++) {
            assertEquals(exp1[i], sol.getRoute(0).getNodeAt(i).getID());
        }
        for (int i = 0; i < exp2.length; i++) {
            assertEquals(exp2[i], sol.getRoute(1).getNodeAt(i).getID());
        }

        System.out.println();
        sol = solution.clone();
        System.out.println("Solution: " + sol);
        move = new OrOptMove<Solution<ArrayListRoute>>(solution, 0, 0, 2);
        move.setInsertion(0, 4, true);
        exp1 = new int[] { 3, 2, 1, 0, 4, 5, 0 };
        exp2 = new int[] { 0, 6, 7, 8, 9, 10, 0 };
        System.out.println("Move    : " + move);
        System.out.printf("Expected: %s %s\n", Arrays.toString(exp1), Arrays.toString(exp2));
        assertTrue("Move should execute correctly " + move.toString(),
                neighborhood.executeMove(sol, move));
        System.out.println("Result  : " + sol);
        for (int i = 0; i < exp1.length; i++) {
            assertEquals(exp1[i], sol.getRoute(0).getNodeAt(i).getID());
        }
        for (int i = 0; i < exp2.length; i++) {
            assertEquals(exp2[i], sol.getRoute(1).getNodeAt(i).getID());
        }

        System.out.println();
        sol = solution.clone();
        System.out.println("Solution: " + sol);
        move = new OrOptMove<Solution<ArrayListRoute>>(solution, 0, 1, 1);
        move.setInsertion(1, 4, false);
        exp1 = new int[] { 0, 2, 3, 4, 5, 0 };
        exp2 = new int[] { 0, 6, 7, 8, 1, 9, 10, 0 };
        System.out.println("Move    : " + move);
        System.out.printf("Expected: %s %s\n", Arrays.toString(exp1), Arrays.toString(exp2));
        assertTrue("Move should execute correctly " + move.toString(),
                neighborhood.executeMove(sol, move));
        System.out.println("Result  : " + sol);
        for (int i = 0; i < exp1.length; i++) {
            assertEquals(exp1[i], sol.getRoute(0).getNodeAt(i).getID());
        }
        for (int i = 0; i < exp2.length; i++) {
            assertEquals(exp2[i], sol.getRoute(1).getNodeAt(i).getID());
        }

    }

    /**
     * Test method for
     * {@link vroom.common.heuristics.vrp.OrOptNeighborhood#evaluateCandidateMove(vroom.common.heuristics.vrp.OrOptMove)}
     * .
     */
    @Test
    public void testEvaluateCandidateMove() {
        System.out.println("--------------------------------");
        System.out.println("testEvaluateCandidateMove");
        System.out.println("--------------------------------");

        Solution<ArrayListRoute> sol;

        System.out.println("Solution: " + solution);

        LinkedList<OrOptMove<Solution<ArrayListRoute>>> moves = new LinkedList<OrOptMove<Solution<ArrayListRoute>>>();

        moves.add(new OrOptMove<Solution<ArrayListRoute>>(solution, 0, 1, 2));
        moves.add(new OrOptMove<Solution<ArrayListRoute>>(solution, 0, 1, 2));
        moves.add(new OrOptMove<Solution<ArrayListRoute>>(solution, 0, 1, 2));
        moves.add(new OrOptMove<Solution<ArrayListRoute>>(solution, 0, 0, 2));
        moves.add(new OrOptMove<Solution<ArrayListRoute>>(solution, 0, 3, 3));

        Random r = new Random(0);
        for (int m = 0; m < 1000; m++) {
            int r1 = r.nextInt(2);
            int r2 = r.nextInt(2);
            int min = r1 == r2 ? 1 : 0;
            int i = r.nextInt(solution.getRoute(r1).length() - min);
            int j = r.nextInt(solution.getRoute(r1).length() - i - min) + i;
            min = r1 == r2 ? j + 1 : 0;
            int k;
            k = r.nextInt(solution.getRoute(r2).length());

            moves.add(new OrOptMove<Solution<ArrayListRoute>>(solution, r1, i, j));
            moves.getLast().setInsertion(r2, k, r.nextBoolean());
        }

        for (OrOptMove<Solution<ArrayListRoute>> move : moves) {
            sol = solution.clone();

            move.setImprovement(neighborhood.evaluateCandidateMove(move));
            if (move.getInsertionRoute() != -1) {

                System.out.println("Move    : " + move);
                SolutionChecker.checkSolution(sol, true, true, true);
                System.out.println(sol);
                double delta = sol.getCost();
                neighborhood.executeMove(sol, move);
                SolutionChecker.checkSolution(sol, true, true, true);
                System.out.println(sol);
                delta -= sol.getCost();
                System.out.println("Delta    : " + delta);
                System.out.println("Improv   : " + move.getImprovement());

                assertEquals(String.format("Wrong improvement for move %s", move), delta,
                        move.getImprovement(), 1E-3);
            }
        }

    }

}
