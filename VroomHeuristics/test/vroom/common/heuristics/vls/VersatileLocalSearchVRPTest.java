/**
 * 
 */
package vroom.common.heuristics.vls;

import static vroom.common.heuristics.vls.VLSGlobalParameters.INITIALIZATION_CLASS;
import static vroom.common.heuristics.vls.VLSGlobalParameters.LOCAL_SEARCH_CLASS;
import static vroom.common.heuristics.vls.VLSGlobalParameters.OPTIMIZATION_DIRECTION;
import static vroom.common.heuristics.vls.VLSGlobalParameters.PARAM_INIT;
import static vroom.common.heuristics.vls.VLSGlobalParameters.PARAM_LOCALSEARCH;
import static vroom.common.heuristics.vls.VLSGlobalParameters.PARAM_PERTUBATION;
import static vroom.common.heuristics.vls.VLSGlobalParameters.PERTUBATION_CLASS;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import vroom.common.heuristics.cw.algorithms.BasicSavingsHeuristic;
import vroom.common.heuristics.vls.vrp.CWInitialization;
import vroom.common.heuristics.vls.vrp.PSwapPerturbation;
import vroom.common.heuristics.vns.VariableNeighborhoodSearch;
import vroom.common.heuristics.vns.VariableNeighborhoodSearch.VNSVariant;
import vroom.common.heuristics.vrp.SwapNeighborhood;
import vroom.common.heuristics.vrp.TwoOptNeighborhood;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.dataModel.IVRPSolution;
import vroom.common.modeling.dataModel.Node;
import vroom.common.modeling.util.CircularInstanceGenerator;
import vroom.common.modeling.util.DefaultSolutionFactory;
import vroom.common.utilities.Stopwatch;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.common.utilities.logging.Logging;
import vroom.common.utilities.optimization.ILocalSearch;
import vroom.common.utilities.optimization.INeighborhood;
import vroom.common.utilities.optimization.ISolution;
import vroom.common.utilities.optimization.OptimizationSense;
import vroom.common.utilities.optimization.SimpleParameters;

/**
 * <code>VersatileLocalSearchTest</code> is a test class for {@link VersatileLocalSearch}
 * <p>
 * Creation date: Apr 28, 2010 - 3:44:38 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class VersatileLocalSearchVRPTest {

    private static final int                        NUM_NODES = 20;
    private ILocalSearch<IVRPSolution<?>>           mLS;
    private List<INeighborhood<IVRPSolution<?>, ?>> mNeighborhoods;

    private List<Node>                              mNodes;

    private IVRPInstance                            mInstance;
    private VLSGlobalParameters                     mParameters;
    protected VersatileLocalSearch<ISolution>       mVLS;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Before
    public void setUp() throws Exception {

        mNodes = new ArrayList<Node>(NUM_NODES + 2);
        mInstance = CircularInstanceGenerator.newCircularInstance(100, mNodes, 10);

        mParameters = new VLSGlobalParameters();

        mParameters.set(INITIALIZATION_CLASS, CWInitialization.class);
        mParameters.set(LOCAL_SEARCH_CLASS, VariableNeighborhoodSearch.class);
        mParameters.set(PERTUBATION_CLASS, PSwapPerturbation.class);

        // mParameters.setParameter(STATE_CLASS, TestState.class);

        mParameters.set(OPTIMIZATION_DIRECTION, -1);

        mParameters.set(PARAM_INIT, SimpleParameters.BEST_IMPROVEMENT);
        mParameters.set(PARAM_LOCALSEARCH, SimpleParameters.BEST_IMPROVEMENT);
        mParameters.set(PARAM_PERTUBATION, SimpleParameters.PERTURBATION);

        mNeighborhoods = new ArrayList<INeighborhood<IVRPSolution<?>, ?>>();

        mNeighborhoods.add(new TwoOptNeighborhood<IVRPSolution<?>>());
        mNeighborhoods.add(new SwapNeighborhood<IVRPSolution<?>>());

        mLS = VariableNeighborhoodSearch.newVNS(VNSVariant.VND, OptimizationSense.MINIMIZATION, null, null,
                mNeighborhoods);

        mVLS = new VersatileLocalSearch(mParameters, new VLSParameters(mParameters, 4, 10, 10, 60000),
                VLSStateBase.class, new SimpleAcceptanceCriterion(mParameters), new CWInitialization(
                        DefaultSolutionFactory.class, BasicSavingsHeuristic.class), mLS, new PSwapPerturbation(
                        mParameters, 5), null);

    }

    /**
     * Test method for {@link vroom.common.heuristics.vls.VersatileLocalSearch#run()}.
     */
    @Test
    public void testRun() {
        mVLS.setInstance(mInstance);

        Stopwatch timer = new Stopwatch();
        Logging.getBaseLogger().info("Starting the VLS procedure:");
        timer.start();
        mVLS.run();
        timer.stop();
        Logging.getBaseLogger().info("VLS procedure terminated after %sms", timer.readTimeMS());

        Logging.getBaseLogger().info("Solution:");
        Logging.getBaseLogger().info(mVLS.getBestSolution());

        mVLS.stop();

    }

    public static void main(String[] args) {
        Logging.setupRootLogger(LoggerHelper.LEVEL_DEBUG, LoggerHelper.LEVEL_DEBUG, true);

        VersatileLocalSearchVRPTest test = new VersatileLocalSearchVRPTest();
        try {
            test.setUp();
        } catch (Exception e) {
            e.printStackTrace();
        }
        test.testRun();
    }
}
