package vroom.optimization.pl.symphony.vrp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import vroom.common.modeling.dataModel.DynamicInstance;
import vroom.common.modeling.io.NovoaPersistenceHelper;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.common.utilities.lp.SolverStatus;
import vroom.optimization.vrph.VRPHSolver;

public class CVRPSymphonySolverTest {

    static int[]          sizes = new int[] { 5, 8, 20 };
    static int[]          cap   = new int[] { 0, 1 };

    CVRPSymphonySolver    mSolver;

    List<DynamicInstance> mInstances;

    @BeforeClass
    public void globalSetup() throws IOException {
        NovoaPersistenceHelper nh = new NovoaPersistenceHelper();
        mInstances = nh.readInstances(sizes, cap, new int[] { 1 }, null);
    }

    @Before
    public void setUp() {
        mSolver = new CVRPSymphonySolver();

    }

    @Test
    public void testSolve() {
        System.out.println("==========================================");
        System.out.println("Test Solve");
        System.out.println("==========================================");

        VRPHSolver test = new VRPHSolver();

        // IVRPSolver test = null;
        // try {
        // test = new CVRPCuttingPlaneSolver(false, true);
        // test = new CVRPCapBasedSolver(false);
        // } catch (GRBException e1) {
        // fail(e1.getMessage());
        // }
        // test.setTimeLimit(60);

        for (DynamicInstance instance : mInstances) {
            try {
                System.out.println("Instance " + instance.getName());
                mSolver.readInstance(instance);
                SolverStatus status = mSolver.solve();
                double obj = mSolver.getObjectiveValue();
                System.out.println(" Symphony solution: " + obj);

                test.readInstance(instance);
                test.solve();
                double heur = test.getObjectiveValue();
                System.out.println(" VRPH solution    : " + heur);

                // mSolver.printSolution(false);

                assertEquals("Symphony did not find the optimal solution: " + status, SolverStatus.OPTIMAL, status);

                test.readInstance(instance);
                status = test.solve();
                // if (status == SolverStatus.OPTIMAL) {
                // double testObj = test.getObjectiveValue();
                // System.out.println(" Cutting Planes solution: " +
                // test.getSolution());
                // // test.printSolution(true);
                // assertEquals(
                // String.format(
                // "Symphony and CuttingPlane did not return the same value (symph:%s cp:%s)",
                // obj, testObj), testObj, obj, 1e-4);
                // } else {
                // System.out.println(" Cutting Planes failed");
                // }
            } catch (Exception e) {
                fail(e.getMessage());
            }
        }
        System.out.println("==========================================");
        System.out.println("Test Solve PASSED");
        System.out.println("==========================================");
    }

    public static void main(String[] args) {
        LoggerHelper.setupRootLogger(LoggerHelper.LEVEL_INFO, LoggerHelper.LEVEL_INFO, false);
        CVRPSymphonySolver.LOGGER.setLevel(LoggerHelper.LEVEL_WARN);
        VRPHSolver.LOGGER.setLevel(LoggerHelper.LEVEL_WARN);
        CVRPSymphonySolverTest test = new CVRPSymphonySolverTest();
        sizes = new int[] { 5 };
        cap = new int[] { 1 };
        try {
            test.globalSetup();
            test.setUp();
            // test.mSolver.setPrintOutput(true);
            test.testSolve();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
