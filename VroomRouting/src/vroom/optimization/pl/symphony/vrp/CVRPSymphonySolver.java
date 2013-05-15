package vroom.optimization.pl.symphony.vrp;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import vroom.common.modeling.dataModel.ListRoute.ArrayListRoute;
import vroom.common.modeling.dataModel.IRoute;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.dataModel.IVRPRequest;
import vroom.common.modeling.dataModel.IVRPSolution;
import vroom.common.modeling.dataModel.Solution;
import vroom.common.modeling.io.TSPLibPersistenceHelper;
import vroom.common.modeling.util.SolutionChecker;
import vroom.common.utilities.ProcessDestroyer;
import vroom.common.utilities.Stopwatch;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.common.utilities.lp.SolverStatus;
import vroom.optimization.pl.IVRPSolver;
import vroom.optimization.vrph.VRPHSolver;

/**
 * The Class <code>CVRPSymphonySolver</code> implements {@link IVRPSolver} by using the Symphony VRP solver application.
 * Visit the <a href="https://projects.coin-or.org/SYMPHONY">symphony project home</a> to download and install the
 * latest version.
 * <p>
 * At this stage, this solver only supports instances with integral distances.
 * </p>
 * <p>
 * Creation date: Sep 15, 2010 - 10:30:38 AM.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class CVRPSymphonySolver implements IVRPSolver {

    /** A lock used to prevent different threads to use the same file name */
    protected static Lock sFileLock = new ReentrantLock();

    protected static class SolverOutput {
        double                   obj       = -1;
        Solution<ArrayListRoute> solution;
        SolverStatus             status    = SolverStatus.UNKNOWN_STATUS;
        int                      intobj;
        int                      numTrucks = -1;

        protected boolean isOptimal() {
            return status == SolverStatus.OPTIMAL;
        }
    }

    /**
     * Solver call command line format, arguments:
     * <p>
     * [solverCommand, instanceFile, numberOfTrucks, upperBound]
     * </p>
     */
    public final static String       COMMAND_FORMAT = "%s -F %s -N %s -u %s -v 0";

    public static final LoggerHelper LOGGER         = LoggerHelper.getLogger(CVRPSymphonySolver.class.getSimpleName());

    private static String            sSolverCommand = "/opt/coinor/symphony/SYMPHONY/Applications/VRP/vrp";

    /**
     * Getter for the solver command string
     * 
     * @return the solverCommand
     */
    public static String getSolverCommand() {
        return sSolverCommand;
    }

    /**
     * Setter for the solver command string
     * 
     * @param solverCommand
     *            the solverCommand to set
     */
    public static void setSolverCommand(String solverCommand) {
        sSolverCommand = solverCommand;
    }

    private double                        mPrecision   = 1e-4;

    // private final VersatileLocalSearch<Solution<ArrayListRoute>> mHeurSolver;

    private boolean                       mInitialized;

    private IVRPInstance                  mInstance;

    private final TSPLibPersistenceHelper mPersistenceHelper;

    private Solution<ArrayListRoute>      mSolution;

    private String                        mSolutionString;

    private SolverOutput                  mSolverOutput;

    /** a temporary folder for instance files **/
    private String                        mTempFolder;

    private final Stopwatch                   mTimer;

    private double                        mUpperBound;

    private final VRPHSolver              mVRPHSolver;

    public void setPrintOutput(boolean printOutput) {
    }

    /**
     * Creates a new <code>CVRPSymphonySolver</code>
     */
    public CVRPSymphonySolver() {
        mTimer = new Stopwatch();
        mPersistenceHelper = new TSPLibPersistenceHelper();
        setTempFolder("./tmp");

        mVRPHSolver = new VRPHSolver();

        // VLSGlobalParameters parameters = new VLSGlobalParameters();
        // parameters.set(VLSGlobalParameters.PARAM_LOCALSEARCH, new SimpleParameters(5000, 10000, false, true, false,
        // null));
        // parameters.set(VLSGlobalParameters.PARAM_INIT, new SimpleParameters(1000, 1000, false, false, false, null));
        // parameters.set(VLSGlobalParameters.ENABLE_CALLBACKS, false);
        // VLSParameters params = new VLSParameters(parameters, 1, 0, 0, 1000);
        //
        // ConstraintHandler<Solution<ArrayListRoute>> ctrHandler = new ConstraintHandler<Solution<ArrayListRoute>>();
        // ctrHandler.addConstraint(new FixedNodesConstraint<Solution<ArrayListRoute>>());
        // ctrHandler.addConstraint(new CapacityConstraint<Solution<ArrayListRoute>>());
        //
        // CWParameters cwParams = new CWParameters();
        // ClarkeAndWrightHeuristic<Solution<ArrayListRoute>> cw = new
        // ClarkeAndWrightHeuristic<Solution<ArrayListRoute>>(
        // cwParams, BasicSavingsHeuristic.class, ctrHandler);
        // CWInitialization<Solution<ArrayListRoute>> initialization = new
        // CWInitialization<Solution<ArrayListRoute>>(cw,
        // cwParams);
        //
        // @SuppressWarnings("unchecked")
        // VariableNeighborhoodSearch<Solution<ArrayListRoute>> ls = VariableNeighborhoodSearch.newVNS(VNSVariant.VND,
        // OptimizationSense.MINIMIZATION, null, new MRG32k3a(), new SwapNeighborhood<Solution<ArrayListRoute>>(
        // ctrHandler), new TwoOptNeighborhood<Solution<ArrayListRoute>>(ctrHandler),
        // new OrOptNeighborhood<Solution<ArrayListRoute>>(ctrHandler),
        // new StringExchangeNeighborhood<Solution<ArrayListRoute>>(ctrHandler));
        // ((GenericNeighborhoodHandler<?>) ls.getNeighHandler()).setStrategy(Strategy.FREQUENCY_BASED);
        // ((GenericNeighborhoodHandler<?>) ls.getNeighHandler()).setResetStrategy(10000, 0.01);
        //
        // mHeurSolver = new VersatileLocalSearch<Solution<ArrayListRoute>>(parameters, params, VLSStateBase.class,
        // new SimpleAcceptanceCriterion(parameters), initialization, ls,
        // new Identity<Solution<ArrayListRoute>>(), ctrHandler);
    }

    @Override
    protected void finalize() throws Throwable {
        // mHeurSolver.destroy();
        super.finalize();
    }

    @Override
    public IVRPInstance getInstance() {
        return mInstance;
    }

    @Override
    public double getObjectiveValue() {
        return mSolverOutput != null ? mSolverOutput.obj : Double.NaN;
    }

    @Override
    public IVRPSolution<? extends IRoute<?>> getSolution() {
        return mSolution;
    }

    @Override
    public double getSolveTime() {
        return mTimer.readTimeMS();
    }

    /**
     * Getter for a temporary folder for instance files
     * 
     * @return the value of tempFOlder
     */
    public String getTempFolder() {
        return mTempFolder;
    }

    @Override
    public boolean isInitialized() {
        return mInitialized;
    }

    @Override
    public boolean isSolutionFeasible() {
        return SolutionChecker.checkSolution(getSolution(), false, true, true) == null;
    }

    @Override
    public void printSolution(boolean printVariables) {
        System.out.println(mSolutionString);
    }

    @Override
    public synchronized void readInstance(IVRPInstance instance) {
        reset();

        mUpperBound = Double.POSITIVE_INFINITY;
        mInstance = instance;

        mInitialized = true;
    }

    @SuppressWarnings("unused")
    protected SolverOutput readOutput(Process pr) throws NumberFormatException, IOException {
        SolverOutput output = new SolverOutput();
        BufferedReader solverOutput = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        String line = null;
        String currentRoute = null;
        mSolutionString = null;
        while ((line = solverOutput.readLine()) != null) {
            LOGGER.lowDebug(line);

            if (line.contains("Optimal Solution Found ")) {
                output.status = SolverStatus.OPTIMAL;
                LOGGER.info(" Otimal solution found");
            } else if (line.contains("The problem is infeasible")) {
                output.obj = -2;
                output.status = SolverStatus.INFEASIBLE;
                LOGGER.info(" Problem is infeasible");
                break;
            } else if (output.isOptimal() && line.startsWith("Solution Cost: ")) {
                line = line.replaceFirst("Solution Cost: ", "");
                output.intobj = Double.valueOf(line).intValue();
                output.obj = ((double) output.intobj) / mPersistenceHelper.getCoordinatesScaleFactor();
                LOGGER.info(" Solution Cost: %s", output.obj);
                // break;
            } else if (output.isOptimal() && (line.startsWith("Route") || mSolutionString != null)) {
                // Read a route
                // TODO read and convert the solution
                mSolutionString = String.format("%s\n%s", mSolutionString, line);
            }
        }
        return output;
    }

    @Override
    public void reset() {
        mSolution = null;
        mSolutionString = null;
        mSolverOutput = null;
        mTimer.reset();
        mInitialized = false;
    }

    /**
     * Setter for a temporary folder for instance files
     * 
     * @param tempFOlder
     *            the value to be set for a temporary folder for instance files
     */
    public synchronized void setTempFolder(String tempFOlder) {
        mTempFolder = tempFOlder;
    }

    @Override
    public synchronized void setTimeLimit(int timeout) {
        mTimer.setTimout(timeout);
    }

    @Override
    public synchronized SolverStatus solve() throws InterruptedException, IOException {

        mTimer.reset();
        mTimer.start();

        mSolverOutput = null;

        sFileLock.lock();
        // write the instance in a temporary file
        int idx = 0;
        File instanceFile = new File(String.format("%s/%s-%s.vrp", getTempFolder(), getInstance().getName(), idx++));
        while (instanceFile.exists()) {
            instanceFile = new File(String.format("%s/%s-%s.vrp", getTempFolder(), getInstance().getName(), idx++));
        }
        // Set the persistence helper to autodetect the best scaling factor
        mPersistenceHelper.setCoordinatesScaleFactor(TSPLibPersistenceHelper.calculateCoordFracScale(getInstance(),
                mPrecision));
        mPersistenceHelper.setDemandsScaleFactor(TSPLibPersistenceHelper.calculateDemFracScale(getInstance(),
                mPrecision));
        LOGGER.info("Coordinates scaling factor: %s", mPersistenceHelper.getCoordinatesScaleFactor());
        LOGGER.info("Demands scaling factor: %s", mPersistenceHelper.getDemandsScaleFactor());
        // Write the instance in file
        mPersistenceHelper.writeInstance(getInstance(), instanceFile, "Temporary instance");
        LOGGER.info("Instance written to file: " + instanceFile.getAbsolutePath());

        sFileLock.unlock();

        // Estimate the minimum number of trucks required
        double load = 0;
        for (IVRPRequest r : getInstance().getRequests()) {
            load += r.getDemand();
        }
        int minTrucks = (int) Math.ceil(load / getInstance().getFleet().getVehicle().getCapacity());
        LOGGER.info("Minimum number of trucks: %s (%s/%s)", minTrucks, load, getInstance().getFleet().getVehicle()
                .getCapacity());

        // VLS Heuristic Solution
        // --------------------------------------------------------------
        // Run the heuristic to get an initial bound
        // mHeurSolver.setInstance(getInstance());
        // mHeurSolver.run();
        // mSolution = mHeurSolver.getBestSolution();
        // // Remove empty routes
        // SolutionChecker.removeEmptyRoutes(getSolution());
        // // Ensure that cost is coherent
        // SolutionChecker.checkSolution(getSolution(), true, true, true);
        //
        // // Number of trucks
        // int heurNumTrucks = getSolution().getRouteCount();
        // double heurObj = getSolution().getCost();
        // --------------------------------------------------------------

        // VRPH Heuristic Solution
        // --------------------------------------------------------------
        mVRPHSolver.solve(instanceFile, mPersistenceHelper);
        int heurNumTrucks = mVRPHSolver.getNumTrucks();
        double heurObj = mVRPHSolver.getObjectiveValue();

        // --------------------------------------------------------------

        // int maxTrucks = Math.min(2 * heurNumTrucks, getInstance().getRequestCount());
        int maxTrucks = getInstance().getRequestCount();
        double doubleUB = heurObj;

        // Upper bound on the solution cost
        int ub = (int) Math.ceil((doubleUB + mPrecision) * mPersistenceHelper.getCoordinatesScaleFactor());

        LOGGER.info("Heuristic solution found in %s ms: obj=%s trucks=%s", mTimer.readTimeMS(), heurObj,
                heurNumTrucks);
        int numTrucks = minTrucks;
        SolverOutput output = null;
        int exitVal = 0;

        boolean first = true;

        // while (!optimal && numTrucks <= maxTrucks) {
        while (numTrucks <= maxTrucks) {
            LOGGER.info(" Attempting to solve the problem with %s trucks, ub=%s", numTrucks, ub);
            // command
            String command = String.format(COMMAND_FORMAT, getSolverCommand(), instanceFile.getPath(), numTrucks, ub);
            // String command = String
            // .format("vrp -F %s -N %s", instanceFile.getName(), numTrucks);

            LOGGER.info(" Command line: " + command);

            Runtime rt = Runtime.getRuntime();
            // Run the command
            Process pr = rt.exec(command);

            // Parse the output
            output = readOutput(pr);

            // Timeout process killer
            ProcessDestroyer dest = ProcessDestroyer.monitorProcess(pr, mTimer.getRemainingTime());

            exitVal = pr.waitFor();
            dest.cancel();
            LOGGER.info(" Exited with error code " + exitVal);
            if (exitVal != 0) {
                break;
            }

            if (output.isOptimal()) {
                if (mSolverOutput == null || output.obj < mSolverOutput.obj) {
                    mSolverOutput = output;
                }

                if (output.intobj < ub) {
                    // Store upper bound
                    ub = output.intobj;
                    if (mSolverOutput == null || output.obj < mSolverOutput.obj) {
                        // Store output as best solution
                        mSolverOutput = output;
                        mSolverOutput.numTrucks = numTrucks;
                    }
                    LOGGER.info(" New upper bound: " + ub);
                }
                // Try with more trucks
                numTrucks++;
            } else {
                if (first && doubleUB < heurObj) {
                    // Revert upper bound but keep number of trucks
                    doubleUB = heurObj;
                    ub = (int) Math.ceil(doubleUB * 1.01 * mPersistenceHelper.getCoordinatesScaleFactor());
                    LOGGER.info(" Reverting upper bound");
                } else if (numTrucks == heurNumTrucks) {
                    // We know that a solution exist with this number of trucks
                    // Relax upper bound for rounding issues
                    ub = (int) Math.ceil(ub * 1.05);
                    LOGGER.info(" Heuristic solution known, relaxing upper bound");
                } else {
                    // Increase number of trucks
                    numTrucks++;
                    LOGGER.info(" Increasing number of trucks");
                }
            }
            first = false;

            pr.destroy();

        }

        instanceFile.delete();

        mTimer.stop();

        LOGGER.info("Optimization terminated in %sms", mTimer.readTimeMS());
        return mSolverOutput != null ? mSolverOutput.status : SolverStatus.UNKNOWN_STATUS;
    }

    /**
     * Define a user upper bound for the current instance
     * 
     * @param ub
     *            the upper bound value
     */
    public void setUpperBound(double ub) {
        if (ub > 0) {
            mUpperBound = ub;
        }
    }

    /**
     * Getter for the number of trucks
     * 
     * @return the number of trucks used in the solution
     */
    public int getNumTrucks() {
        return mSolverOutput.numTrucks;
    }
}
