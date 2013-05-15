/**
 * 
 */
package vroom.common.heuristics.vns.benchmarking;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import umontreal.iro.lecuyer.rng.MRG32k3a;
import vroom.common.heuristics.ConstraintHandler;
import vroom.common.heuristics.GenericNeighborhood;
import vroom.common.heuristics.GenericNeighborhoodHandler;
import vroom.common.heuristics.GenericNeighborhoodHandler.Strategy;
import vroom.common.heuristics.cw.CWParameters;
import vroom.common.heuristics.cw.algorithms.BasicSavingsHeuristic;
import vroom.common.heuristics.cw.kernel.ClarkeAndWrightHeuristic;
import vroom.common.heuristics.vns.VariableNeighborhoodSearch;
import vroom.common.heuristics.vrp.OrOptNeighborhood;
import vroom.common.heuristics.vrp.StringExchangeNeighborhood;
import vroom.common.heuristics.vrp.SwapNeighborhood;
import vroom.common.heuristics.vrp.TwoOptNeighborhood;
import vroom.common.heuristics.vrp.VRPParameters;
import vroom.common.heuristics.vrp.constraints.CapacityConstraint;
import vroom.common.modeling.dataModel.ListRoute.ArrayListRoute;
import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.IRoute;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.dataModel.IVRPRequest;
import vroom.common.modeling.dataModel.Solution;
import vroom.common.modeling.util.DefaultSolutionFactory;
import vroom.common.modeling.util.SolutionChecker;
import vroom.common.utilities.Stopwatch;
import vroom.common.utilities.dataModel.ObjectWithIdComparator;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.common.utilities.logging.Logging;
import vroom.common.utilities.optimization.INeighborhood;
import vroom.common.utilities.optimization.OptimizationSense;

/**
 * <code>VNSRun</code>
 * <p>
 * Creation date: Jul 8, 2010 - 4:54:08 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class VNSRun implements Runnable {

    protected final static LoggerHelper                                LOGGER = LoggerHelper.getLogger(VNSRun.class
                                                                                      .getSimpleName());

    private final IVRPInstance                                         mInstance;

    private final VariableNeighborhoodSearch<Solution<ArrayListRoute>> mVNS;

    private Solution<ArrayListRoute>                                   mSolution;

    private final ConstraintHandler<Solution<ArrayListRoute>>          mCtrHandler;

    /**
     * Setter for <code>mSolution</code>
     * 
     * @param mSolution
     *            the mSolution to set
     */
    public void setSolution(Solution<ArrayListRoute> solution) {
        mSolution = solution;
    }

    /**
     * Getter for <code>mSolution</code>
     * 
     * @return the mSolution
     */
    public Solution<ArrayListRoute> getSolution() {
        return mSolution;
    }

    /** the global timer **/
    private final Stopwatch mGlobalTimer;

    /**
     * Getter for the global timer
     * 
     * @return the global timer
     */
    public Stopwatch getGlobalTimer() {
        return mGlobalTimer;
    }

    /** the vns timer **/
    private final Stopwatch mVNSTimer;

    /**
     * Getter for the vns timer
     * 
     * @return the vns timer
     */
    public Stopwatch getVNSTimer() {
        return mVNSTimer;
    }

    /** vns parameters **/
    private final VRPParameters mVNSParameters;

    /**
     * Getter for the vns parameters
     * 
     * @return vns parameters
     */
    public VRPParameters getVNSParameters() {
        return mVNSParameters;
    }

    /**
     * Creates a new <code>VNSBenchmark</code>
     * 
     * @param instance
     * @param vns
     */
    public VNSRun(IVRPInstance instance, VariableNeighborhoodSearch<Solution<ArrayListRoute>> vns,
            ConstraintHandler<Solution<ArrayListRoute>> ctrHandlr, VRPParameters params) {
        mInstance = instance;
        mVNS = vns;
        mCtrHandler = ctrHandlr;
        mVNSParameters = params;

        mGlobalTimer = new Stopwatch();
        mVNSTimer = new Stopwatch();
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        LOGGER.info("#########################");
        LOGGER.info("Instance: %s", mInstance.getName());
        LOGGER.debug(" Detail");
        LOGGER.debug("  Fleet     \t : %s", mInstance.getFleet());
        LOGGER.debug("  %s Depots \t : %s", mInstance.getDepotCount(), mInstance.getDepotsVisits());
        List<IVRPRequest> reqs = mInstance.getRequests();
        Collections.sort(reqs, new ObjectWithIdComparator());
        LOGGER.debug("  %s Requests \t : %s", mInstance.getRequestCount(), reqs);

        mGlobalTimer.start();

        if (getSolution() == null) {
            throw new IllegalStateException("Set the initial solution first");
        }

        // mCW.initialize(mInstance);
        // mCW.run();
        // Solution<ArrayListRoute> solution = mCW.getSolution();
        //
        // setSolution(solution);

        LOGGER.info("Initial solution:");
        LOGGER.info(getSolution());

        mVNSTimer.start();
        setSolution(mVNS.localSearch(mInstance, mSolution, mVNSParameters));

        mGlobalTimer.stop();
        mVNSTimer.stop();

        LOGGER.info("VNS finished in %sms", mVNSTimer.readTimeMS());
        LOGGER.info("Solution");
        LOGGER.info(getSolution());

        Set<IVRPRequest> unservedReq = new HashSet<IVRPRequest>(mInstance.getRequests());
        for (ArrayListRoute r : getSolution()) {
            for (INodeVisit n : r) {
                if (!n.isDepot()) {
                    if (!unservedReq.remove(n.getParentRequest())) {
                        LOGGER.warn("%s was not in the set of requests", n);
                    }
                }
            }
        }
        if (!unservedReq.isEmpty()) {
            LOGGER.warn("There are unserved requests: %s", unservedReq);
        }

        String err = SolutionChecker.checkSolution(mSolution, true, true, true);
        if (err != null) {
            LOGGER.warn("Inconsistent costs/loads:");
            LOGGER.warn(err);
            LOGGER.warn("Corrected Solution");
            LOGGER.warn(getSolution());
        }

        err = mCtrHandler.getInfeasibilityExplanation(getSolution());
        if (err != null) {
            LOGGER.error("Solution is infeasible:");
            LOGGER.error(err);
        } else {
            LOGGER.info("Solution is feasible");
        }
        for (IRoute<?> route : getSolution()) {
            if (!route.getFirstNode().isDepot() || !route.getLastNode().isDepot()) {
                LOGGER.error("Route %s does not start/end with a depot (%s)", route.hashCode(), route);
            }
        }

        LOGGER.info("#########################");
    }

    @SuppressWarnings("rawtypes")
    public static void main(String[] args) {
        Logging.setupRootLogger(LoggerHelper.LEVEL_DEBUG, LoggerHelper.LEVEL_DEBUG, false);
        GenericNeighborhood.setCheckSolutionAfterMove(false);

        File instanceFile = VNSBenchmark.INSTANCES_FILES.iterator().next();

        try {
            IVRPInstance instance = VNSBenchmark.INSTANCE_READER.readInstance(instanceFile);

            // Constraint Handler
            ConstraintHandler<Solution<ArrayListRoute>> ctrHandler = new ConstraintHandler<Solution<ArrayListRoute>>();
            ctrHandler.addConstraint(new CapacityConstraint<Solution<ArrayListRoute>>());

            // Neighborhoods
            LinkedList<INeighborhood<Solution<ArrayListRoute>, ?>> neighborhoods = new LinkedList<INeighborhood<Solution<ArrayListRoute>, ?>>();
            // swap
            neighborhoods.add(new SwapNeighborhood<Solution<ArrayListRoute>>(ctrHandler));
            // 2-opt
            neighborhoods.add(new TwoOptNeighborhood<Solution<ArrayListRoute>>(ctrHandler));
            // Or-opt
            neighborhoods.add(new OrOptNeighborhood<Solution<ArrayListRoute>>(ctrHandler));

            // string-exchange
            neighborhoods.add(new StringExchangeNeighborhood<Solution<ArrayListRoute>>(ctrHandler));
            ((StringExchangeNeighborhood<?>) neighborhoods.getLast()).setMaxLength(3);

            // Collections.reverse(neighborhoods);

            // VNS
            VariableNeighborhoodSearch<Solution<ArrayListRoute>> vns = new VariableNeighborhoodSearch<Solution<ArrayListRoute>>(
                    OptimizationSense.MINIMIZATION, null, neighborhoods, false, new MRG32k3a());
            // VNS Params
            VRPParameters params = new VRPParameters(Long.MAX_VALUE, Integer.MAX_VALUE, false, false, null);

            ((GenericNeighborhoodHandler) vns.getNeighHandler()).setStrategy(Strategy.SEQUENTIAL);

            VNSRun run = new VNSRun(instance, vns, ctrHandler, params);

            CWParameters cwParams = new CWParameters();
            cwParams.set(CWParameters.SOLUTION_FACTORY_CLASS, DefaultSolutionFactory.class);
            cwParams.set(CWParameters.RANDOM_SEED, 0l);
            ClarkeAndWrightHeuristic<Solution<ArrayListRoute>> cw = new ClarkeAndWrightHeuristic<Solution<ArrayListRoute>>(
                    cwParams, BasicSavingsHeuristic.class, ctrHandler);

            cw.initialize(instance);
            Stopwatch t = new Stopwatch();
            t.start();
            cw.run();
            t.stop();

            LOGGER.info("CW run in        : %sms", t.readTimeMS());
            LOGGER.info("Initial solution : %s", cw.getSolution());

            run.setSolution(cw.getSolution());

            run.run();

            LOGGER.info("VNS terminated in: %sms", run.getVNSTimer().readTimeMS());
            LOGGER.info("Solution         : %s", run.getSolution());
            LOGGER.info("Neighborhoods    : %s", vns.getNeighHandler());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
