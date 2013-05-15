package vroom.optimization.online.jmsa.benchmarking;

import gurobi.GRBException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;

import vroom.common.heuristics.vns.VariableNeighborhoodSearch;
import vroom.common.modeling.dataModel.IRoute;
import vroom.common.modeling.dataModel.IVRPRequest;
import vroom.common.modeling.dataModel.IVRPSolution;
import vroom.common.modeling.dataModel.NodeVisit;
import vroom.common.modeling.io.NovoaPersistenceHelper.DemandDistribution;
import vroom.common.modeling.util.SolutionChecker;
import vroom.common.modeling.visualization.VRPVisualizationUtilities;
import vroom.common.utilities.Stopwatch;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.optimization.online.jmsa.MSABase;
import vroom.optimization.online.jmsa.MSAGlobalParameters;
import vroom.optimization.online.jmsa.MSASequential;
import vroom.optimization.online.jmsa.components.ScenarioOptimizerParam;
import vroom.optimization.online.jmsa.vrp.MSAVRPInstance;
import vroom.optimization.online.jmsa.vrp.VRPActualRequest;
import vroom.optimization.online.jmsa.vrp.VRPSampledRequest;
import vroom.optimization.online.jmsa.vrp.VRPScenario;
import vroom.optimization.pl.gurobi.CVRPCuttingPlaneSolver;

/**
 * 
 */

/**
 * <code>MSAOptTest</code>
 * <p>
 * Creation date: Sep 29, 2010 - 11:11:18 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class MSAOptTest {
    public final static LoggerHelper  LOGGER = LoggerHelper.getLogger("MSAOptTest");

    private static DemandDistribution sDist  = DemandDistribution.NORMAL;

    @SuppressWarnings("unused")
    public static void main(String[] args) {
        NovoaRun.setupLoggers(LoggerHelper.LEVEL_WARN, LoggerHelper.LEVEL_WARN, false, true, false);
        LOGGER.setLevel(LoggerHelper.LEVEL_INFO);
        VariableNeighborhoodSearch.LOGGER.setLevel(LoggerHelper.LEVEL_DEBUG);

        MSAGlobalParameters params = NovoaBenchmarking.getDefaultParameters();

        PerfectInformationSolver solver = new PerfectInformationSolver();
        CVRPCuttingPlaneSolver cpSolver = null;
        try {
            cpSolver = new CVRPCuttingPlaneSolver(false, true);
        } catch (GRBException e) {
            e.printStackTrace();
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        MSABase msa = new MSASequential(null, params);

        int run = 79;
        int size = 20;
        int num = 1;
        int cap = 0;
        int set = 1;
        NovoaRun novoa = null;
        try {
            novoa = new NovoaRun(set, size, num, cap, run, params, sDist);
        } catch (IOException e1) {
            LOGGER.exception("MSAOptTest.main", e1);
            return;
        }

        MSAVRPInstance instance = new MSAVRPInstance(novoa.getSimulationInstance(), params);

        Stopwatch timer = new Stopwatch();
        double initTime, optTime;
        double initObj, optObj;

        List<VRPActualRequest> act = new LinkedList<VRPActualRequest>();
        for (IVRPRequest r : novoa.getSimulationInstance().getRequests()) {
            VRPActualRequest req = new VRPActualRequest(NodeVisit.createNodeVisits(r)[0]);
            act.add(req);
        }

        List<VRPSampledRequest> samp = new LinkedList<VRPSampledRequest>();

        VRPScenario solution = new VRPScenario(instance, act, samp);

        timer.start();
        novoa.getMsa()
                .getComponentManager()
                .getScenarioOptimizer()
                .initialize(solution,
                        new ScenarioOptimizerParam(Integer.MAX_VALUE, Integer.MAX_VALUE, false));
        timer.stop();
        SolutionChecker.checkSolution(solution, true, true, true);
        initObj = solution.getCost();
        initTime = timer.readTimeMS();
        JFrame solInit = VRPVisualizationUtilities.showVisualizationFrame(solution);
        solInit.setTitle(String.format("%s Init (%.2f %sms)", instance.getName(), initObj, initTime));

        timer.restart();
        novoa.getMsa()
                .getComponentManager()
                .getScenarioOptimizer()
                .optimize(solution,
                        new ScenarioOptimizerParam(Integer.MAX_VALUE, Integer.MAX_VALUE, false));
        timer.stop();
        SolutionChecker.checkSolution(solution, true, true, true);
        optObj = solution.getCost();
        optTime = timer.readTimeMS();
        JFrame solOpt = VRPVisualizationUtilities.showVisualizationFrame(solution);
        solOpt.setTitle(String.format("%s Opt (%.2f %sms)", instance.getName(), optObj, optTime));
        solOpt.setLocation((int) solInit.getLocation().getX() + solInit.getWidth() + 10,
                (int) solInit.getLocation().getY());

        try {
            cpSolver.readInstance(instance);
            cpSolver.solve();

            IVRPSolution<? extends IRoute<?>> exactSol = cpSolver.getSolution();
            JFrame optimal = VRPVisualizationUtilities.showVisualizationFrame(exactSol);
            optimal.setTitle(String.format("%s Optimal (%.2f)", instance.getName(),
                    exactSol.getCost()));
            optimal.setLocation((int) solOpt.getLocation().getX() + solOpt.getWidth() + 10,
                    (int) solInit.getLocation().getY());
        } catch (GRBException e) {
            e.printStackTrace();
        }

        double optimal = solver.solvePerfectInformation(run, size, num, cap, set,
                Integer.MAX_VALUE, false, true, sDist);
    }

}
