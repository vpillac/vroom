/**
 * 
 */
package vroom.trsp;

import java.io.File;

import vroom.common.heuristics.ProcedureStatus;
import vroom.common.utilities.BestKnownSolutions;
import vroom.common.utilities.StatCollector.Label;
import vroom.common.utilities.params.ParameterKey;
import vroom.optimization.online.jmsa.MSAGlobalParameters;
import vroom.optimization.online.jmsa.MSASequential;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.optimization.mpa.DTRSPSolution;
import vroom.trsp.util.TRSPGlobalParameters;
import vroom.trsp.util.TRSPLogging;

/**
 * The class <code>MPASolver</code> implements a solver for the D-TRSP based on a Multiple Scenario Approach.
 * <p>
 * Creation date: Feb 7, 2012 - 10:37:37 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class MPASolver extends TRSPSolver {

    /** A key for the MPA solver */
    public static final ParameterKey<MPASolver>              TRSP_MPA_SOLVER = new ParameterKey<MPASolver>(
                                                                                     "TRSP_MPA_SOLVER",
                                                                                     MPASolver.class);

    /** The MPA procedure used in this solver */
    private final MSASequential<DTRSPSolution, TRSPInstance> mMPA;

    /**
     * Returns the MPA used in this solver
     * 
     * @return the MPA used in this solver
     */
    public MSASequential<DTRSPSolution, TRSPInstance> getMPA() {
        return mMPA;
    }

    /** The thread in which the MPA will be run */
    private MPAThread mMPAThread;

    /**
     * Returns the thread used to run the MPA procedure
     * 
     * @return the thread used to run the MPA procedure
     */
    public MPAThread getMPAThread() {
        return mMPAThread;
    }

    /**
     * Sets the final solution found by this solver
     * 
     * @param postOpSol
     *            the final solution found by this solver
     */
    @Override
    public void setFinalSolution(TRSPSolution postOpSol) {
        super.setFinalSolution(postOpSol);
    }

    /**
     * Creates a new <code>MPASolver</code>
     * 
     * @param instance
     * @param params
     */
    public MPASolver(TRSPInstance instance, TRSPGlobalParameters params) {
        super(instance, params);

        MSAGlobalParameters msaParams = new MSAGlobalParameters();

        try {
            msaParams.loadParameters(new File(params.get(TRSPGlobalParameters.MPA_CONFIG_FILE)));
        } catch (Exception e) {
            TRSPLogging.getBaseLogger().exception("MPASolver.init", e);
        }

        msaParams.set(TRSP_MPA_SOLVER, this);

        long[] seeds = params.get(TRSPGlobalParameters.RUN_SEEDS);
        msaParams.set(MSAGlobalParameters.RANDOM_SEEDS, seeds);
        msaParams.set(MSAGlobalParameters.RANDOM_SEED, seeds[0]);

        mMPA = new MSASequential<DTRSPSolution, TRSPInstance>(instance, msaParams);

    }

    @Override
    public TRSPSolution call() {
        setStatus(ProcedureStatus.INITIALIZATION);
        // Start the MPA procedure
        mMPAThread = new MPAThread();
        mMPAThread.start();

        if (mMPAThread.getException() != null) {
            TRSPLogging.getRunLogger().exception("MPASolver.call", mMPAThread.getException());
            // TRSPLogging.getRunLogger().info("MPASolver.call: %s", getMPA());

            setStatus(ProcedureStatus.EXCEPTION);
            return null;
        }

        while (!getMPA().isInitialized())
            try {
                getMPA().acquireLock();
                getMPA().getInitializedCondition().await();
                getMPA().releaseLock();
                // Thread.sleep(500);
            } catch (InterruptedException e) {
                TRSPLogging.getRunLogger().exception("DTRSPRunMPA.call", e);
            }

        setStatus(ProcedureStatus.RUNNING);
        return (TRSPSolution) getMPA().getCurrentSolution();
    }

    @Override
    public void dispose() {
    }

    @Override
    public Label<?>[] getLabels() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object[] getStats(BestKnownSolutions bks, int runId, int runNum) {
        throw new UnsupportedOperationException();
    }

    /**
     * <code>MPAThread</code> is an implementation of Thread that will run the MPA procedure
     * <p>
     * Creation date: May 3, 2012 - 11:23:24 AM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    public class MPAThread extends Thread {
        private Exception mException;

        public Exception getException() {
            return mException;
        }

        private MPAThread() {
            super(getMPA(), "mpa");
        }

        @Override
        public void run() {
            try {
                super.run();
                setStatus(ProcedureStatus.TERMINATED);
            } catch (Exception e) {
                TRSPLogging.getRunLogger().exception("MPAThread.run (%s)", e,
                        MPASolver.this.getInstance());
                mException = e;
                getMPA().stop();
                setStatus(ProcedureStatus.EXCEPTION);
                // TRSPLogging.getRunLogger().info("MPAThread.run: %s", getMPA());
            }
        }
    }
}
