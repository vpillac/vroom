/**
 *
 */
package vroom.common.heuristics.vls;

import static vroom.common.heuristics.vls.VLSGlobalParameters.INITIALIZATION_CLASS;
import static vroom.common.heuristics.vls.VLSGlobalParameters.LOCAL_SEARCH_CLASS;
import static vroom.common.heuristics.vls.VLSGlobalParameters.NC;
import static vroom.common.heuristics.vls.VLSGlobalParameters.NI;
import static vroom.common.heuristics.vls.VLSGlobalParameters.NS;
import static vroom.common.heuristics.vls.VLSGlobalParameters.OPTIMIZATION_DIRECTION;
import static vroom.common.heuristics.vls.VLSGlobalParameters.PERTUBATION_CLASS;
import static vroom.common.heuristics.vls.VLSPhase.ELS;
import static vroom.common.heuristics.vls.VLSPhase.GRASP;
import static vroom.common.heuristics.vls.VLSPhase.ILS;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.junit.Before;
import org.junit.Test;

import vroom.common.heuristics.IInitialization;
import vroom.common.utilities.callbacks.CallbackEventBase;
import vroom.common.utilities.callbacks.ICallback;
import vroom.common.utilities.callbacks.ICallbackEvent;
import vroom.common.utilities.optimization.IInstance;
import vroom.common.utilities.optimization.ILocalSearch;
import vroom.common.utilities.optimization.IParameters;
import vroom.common.utilities.optimization.ISolution;

/**
 * <code>VersatileLocalSearchTest</code> is a JUnit4 test case for {@link VersatileLocalSearch}
 * <p>
 * Creation date: Apr 26, 2010 - 1:33:59 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class VersatileLocalSearchSimpleTest {

    protected VLSGlobalParameters             mParameters;

    protected VersatileLocalSearch<ISolution> mVLS;

    @Before
    public void setUp() throws Exception {
        mParameters = new VLSGlobalParameters();

        mParameters.set(INITIALIZATION_CLASS, TestInitialization.class);
        mParameters.set(LOCAL_SEARCH_CLASS, TestLocalSearch.class);
        mParameters.set(PERTUBATION_CLASS, TestPertubation.class);

        // mParameters.setParameter(STATE_CLASS, TestState.class);

        mParameters.set(OPTIMIZATION_DIRECTION, 1);

        mParameters.set(NS, 10);
        mParameters.set(NI, 10);
        mParameters.set(NC, 10);

        mVLS = new VersatileLocalSearch<ISolution>(mParameters);

        TestCallback callback = new TestCallback();
        mVLS.registerCallback(VLSCallbackEvents.SOLUTION_ACCEPTED, callback);
        mVLS.registerCallback(VLSCallbackEvents.SOLUTION_REJECTED, callback);
    }

    /**
     * Test method for {@link vroom.common.heuristics.vls.VersatileLocalSearch#run()}.
     */
    @Test
    public void testRun() {
        mVLS.run();
    }

    public static class TestSolution implements ISolution {

        private double fitness = 0;

        /**
         * Creates a new <code>TestSolution</code>
         */
        public TestSolution() {
            this(0);
        }

        /**
         * Creates a new <code>TestSolution</code>
         * 
         * @param fitness
         */
        public TestSolution(double fitness) {
            this.fitness = fitness;
        }

        @Override
        public Comparable<?> getObjective() {
            return fitness;
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * vroom.common.utilities.optimization.ISolution#getObjectiveValue()
         */
        @Override
        public double getObjectiveValue() {
            return fitness;
        }

        @Override
        public String toString() {
            return "[" + getObjective() + "]";
        }

        @Override
        public ISolution clone() {
            return new TestSolution(fitness);
        }

        /*
         * (non-Javadoc)
         *
         * @see vroom.common.utilities.ILockable#acquireLock()
         */
        @Override
        public void acquireLock() {

        }

        /*
         * (non-Javadoc)
         *
         * @see vroom.common.utilities.ILockable#getLockInstance()
         */
        @Override
        public Lock getLockInstance() {
            return null;
        }

        /*
         * (non-Javadoc)
         *
         * @see vroom.common.utilities.ILockable#isLockOwnedByCurrentThread()
         */
        @Override
        public boolean isLockOwnedByCurrentThread() {
            return false;
        }

        @Override
        public boolean tryLock(long timeout) {
            try {
                return getLockInstance().tryLock(timeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                return false;
            }
        }

        /*
         * (non-Javadoc)
         *
         * @see vroom.common.utilities.ILockable#releaseLock()
         */
        @Override
        public void releaseLock() {
        }

        @Override
        public int hashSolution() {
            return hashCode();
        }
    }

    public static class TestInitialization implements IInitialization<ISolution> {

        public TestInitialization(VLSGlobalParameters params) {
        }

        @Override
        public ISolution newSolution(IVLSState<ISolution> state, IInstance instance,
                IParameters params) {
            return new TestSolution();
        }
    }

    public static class TestLocalSearch implements ILocalSearch<ISolution> {
        private static Random r = new Random(1);

        public TestLocalSearch(VLSGlobalParameters params) {
        }

        @Override
        public ISolution localSearch(IInstance instance, ISolution solution, IParameters param) {
            ((TestSolution) solution).fitness = ((TestSolution) solution).fitness + r.nextInt(5);

            return solution;
        }

        @Override
        public void dispose() {
            // Do nothing
        }

    }

    public static class TestPertubation implements IVLSPertubation<ISolution> {
        private static Random r = new Random(0);

        public TestPertubation(VLSGlobalParameters params) {
        }

        @Override
        public void pertub(IVLSState<ISolution> state, IInstance instance, ISolution solution,
                IParameters params) {
            ((TestSolution) solution).fitness += r.nextInt(10) - 5;
        }
    }

    public static class TestState extends VLSStateBase<ISolution> {

        /**
         * Creates a new <code>TestState</code>
         * 
         * @param parentVLS
         */
        public TestState(VersatileLocalSearch<ISolution> parentVLS) {
            super(parentVLS);
        }

        @Override
        public void solutionAccepted(ISolution solution, VLSPhase phase) {
            super.solutionAccepted(solution, phase);
            System.out.println(String.format("State %s> mSolution accepted: %s - %s", phase,
                    solution, toString()));
        }

        @Override
        public void solutionRejected(ISolution solution, VLSPhase phase) {
            super.solutionRejected(solution, phase);
            System.out.println(String.format("State %s> mSolution rejected: %s - %s", phase,
                    solution, toString()));
        }

        @Override
        public String toString() {
            return String.format("(bELS %s,bILS %s,bGRASP %s)", getBestSolution(ELS),
                    getBestSolution(ILS), getBestSolution(GRASP));
        }
    }

    public static class TestCallback implements
            ICallback<VersatileLocalSearch<ISolution>, VLSCallbackEvents> {

        @Override
        public void execute(ICallbackEvent<VersatileLocalSearch<ISolution>, VLSCallbackEvents> event) {
            System.out.println(String.format("CALLBACK %s %s> %s: %s   \t- %s",
                    CallbackEventBase.getTimeStampString(event.getTimeStamp()),
                    event.getParams()[1], event.getDescription(), event.getParams()[0],
                    event.getParams()[2]));
        }

        @Override
        public int getPriority() {
            return 0;
        }

        @Override
        public boolean isExecutedSynchronously() {
            return true;
        }

        @Override
        public int compareTo(ICallback<?, ?> o) {
            return o.getPriority() - getPriority();
        }

    }

    public static void main(String[] args) {
        VersatileLocalSearchSimpleTest test = new VersatileLocalSearchSimpleTest();
        try {
            test.setUp();
        } catch (Exception e) {
            e.printStackTrace();
        }
        test.testRun();
    }

}
