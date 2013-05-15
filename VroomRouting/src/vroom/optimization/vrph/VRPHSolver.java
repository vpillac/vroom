package vroom.optimization.vrph;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import vroom.common.modeling.dataModel.ListRoute.ArrayListRoute;
import vroom.common.modeling.dataModel.IRoute;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.dataModel.IVRPSolution;
import vroom.common.modeling.dataModel.Solution;
import vroom.common.modeling.io.TSPLibPersistenceHelper;
import vroom.common.modeling.util.SolutionChecker;
import vroom.common.utilities.ProcessDestroyer;
import vroom.common.utilities.Stopwatch;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.common.utilities.lp.SolverStatus;
import vroom.optimization.pl.IVRPSolver;

/**
 * The Class <code>VRPHSolver</code> implements {@link IVRPSolver} by using the VRPH library. Visit the <a
 * href="http://www.coin-or.org/projects/VRPH.xml">VRPH project home</a> to download and install the latest version.
 * <p>
 * Creation date: Feb 21, 2011 - 1:30:50 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class VRPHSolver implements IVRPSolver {

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
    public final static String       COMMAND_FORMAT = "%s -f %s";

    public static final LoggerHelper LOGGER         = LoggerHelper.getLogger(VRPHSolver.class.getSimpleName());

    private static String            sSolverCommand = "/opt/coinor/vrph/bin/vrp_rtr";

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

    private double                   mPrecision   = 1e-4;

    private boolean                  mInitialized;

    private IVRPInstance             mInstance;

    private TSPLibPersistenceHelper  mPersistenceHelper;

    private Solution<ArrayListRoute> mSolution;

    private String                   mSolutionString;

    private SolverOutput             mSolverOutput;

    /** a temporary folder for instance files **/
    private String                   mTempFolder;

    private final Stopwatch              mTimer;

    public void setPrintOutput(boolean printOutput) {
    }

    /**
     * Creates a new <code>VRPHSolver</code>
     */
    public VRPHSolver() {
        mTimer = new Stopwatch();
        mPersistenceHelper = new TSPLibPersistenceHelper();
        setTempFolder("./tmp");
    }

    @Override
    protected void finalize() throws Throwable {
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

        mInstance = instance;

        mInitialized = true;
    }

    @SuppressWarnings("unused")
    protected SolverOutput readOutput(Process pr) throws NumberFormatException, IOException {
        SolverOutput output = new SolverOutput();
        BufferedReader solverOutput = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        String currentRoute = null;
        mSolutionString = null;

        // Comments:
        // --------------------------------------------
        // VRPH, version 1.0
        // Copyright 2010 Chris Groer
        // Distributed under Common Public License 1.0
        // --------------------------------------------
        //
        String line;
        for (int c = 0; c < 6; c++) {
            line = solverOutput.readLine();
            LOGGER.lowDebug(line);
        }

        // Solution summary
        // 5 524.611 0.43 1.000
        // numtrucks obj ? ?
        line = solverOutput.readLine();
        LOGGER.lowDebug(line);
        String[] values = line.split("\\s+");
        // Num trucks
        output.numTrucks = Integer.valueOf(values[0]);
        // Objective
        output.intobj = Double.valueOf(values[1]).intValue();
        output.obj = ((double) output.intobj) / mPersistenceHelper.getCoordinatesScaleFactor();

        output.status = SolverStatus.HEURISTIC;

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

    public synchronized SolverStatus solve(File instanceFile, TSPLibPersistenceHelper persistenceHelper)
            throws InterruptedException, IOException {
        if (!mTimer.isStarted()) {
            mTimer.reset();
            mTimer.start();
        }

        mSolverOutput = null;

        mPersistenceHelper = persistenceHelper;

        // write the instance in a temporary file

        LOGGER.info(" Running the heuristic");
        // command
        String command = String.format(COMMAND_FORMAT, getSolverCommand(), instanceFile.getPath());

        LOGGER.info(" Command line: " + command);

        Runtime rt = Runtime.getRuntime();
        // Run the command
        Process pr = rt.exec(command);

        // Parse the output
        mSolverOutput = readOutput(pr);

        // Timeout process killer
        ProcessDestroyer dest = ProcessDestroyer.monitorProcess(pr, mTimer.getRemainingTime());

        int exitVal = pr.waitFor();
        dest.cancel();
        LOGGER.info(" Exited with error code " + exitVal);

        mTimer.stop();

        LOGGER.info("Optimization terminated in %sms, solution: %s", mTimer.readTimeMS(), getObjectiveValue());
        return mSolverOutput != null ? mSolverOutput.status : SolverStatus.UNKNOWN_STATUS;
    }

    @Override
    public synchronized SolverStatus solve() throws InterruptedException, IOException {
        mTimer.reset();
        mTimer.start();

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
        LOGGER.info("Instance written to file: " + instanceFile);

        sFileLock.unlock();

        SolverStatus r = solve(instanceFile, mPersistenceHelper);

        instanceFile.delete();
        return r;
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
