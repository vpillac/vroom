/**
 * 
 */
package vroom.trsp.bench.mpa;

import java.io.File;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import vroom.common.heuristics.ProcedureStatus;
import vroom.common.heuristics.alns.DiversifiedPool;
import vroom.common.modeling.io.DynamicPersistenceHelper;
import vroom.common.utilities.BestKnownSolutions;
import vroom.common.utilities.StatCollector;
import vroom.common.utilities.StatCollector.Label;
import vroom.common.utilities.Utilities;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.common.utilities.logging.Logging;
import vroom.common.utilities.optimization.IConstraint;
import vroom.optimization.online.jmsa.MSASequential;
import vroom.optimization.online.jmsa.events.NewRequestEvent;
import vroom.optimization.online.jmsa.events.ResourceEvent;
import vroom.optimization.online.jmsa.utils.MSALogging;
import vroom.optimization.online.jmsa.utils.MSASimulator;
import vroom.trsp.ALNSSCSolver;
import vroom.trsp.MPASolver;
import vroom.trsp.bench.TRSPBench;
import vroom.trsp.bench.TRSPRunBase;
import vroom.trsp.datamodel.HashTourPool;
import vroom.trsp.datamodel.TRSPDetailedSolutionChecker;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPRequest;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.datamodel.Technician;
import vroom.trsp.datamodel.costDelegates.TRSPWorkingTime;
import vroom.trsp.io.DynamicTRSPPersistenceHelper;
import vroom.trsp.io.ITRSPPersistenceHelper;
import vroom.trsp.optimization.TRSPUtilities;
import vroom.trsp.optimization.constraints.ServicedRequestsConstraint;
import vroom.trsp.optimization.mpa.DTRSPSolution;
import vroom.trsp.util.BrokenPairsDistance;
import vroom.trsp.util.TRSPGlobalParameters;
import vroom.trsp.util.TRSPLogging;

/**
 * <code>DTRSPRunMPA</code> is a benchmarking class for the D-TRSP using MPA
 * <p>
 * Creation date: Feb 7, 2012 - 10:34:34 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class DTRSPRunMPA extends TRSPRunBase {

    /** The labels used in the stat collector */
    public static final Label<?>[] LABELS = new Label<?>[] { new Label<>("run_id", Integer.class), // run id
            new Label<>("name", String.class), // name
            new Label<>("group", String.class), // group
            new Label<>("size", Integer.class), // size
            new Label<>("crew", Integer.class), // crew
            new Label<>("dod", String.class), // degree of dynamism
            new Label<>("run", Integer.class), // run #
            new Label<>("comment", String.class), // comment
            new Label<>("status", String.class), // status
            new Label<>("final_wall_time", Double.class), // final wall clock time
            new Label<>("final_sim_time", Double.class), // final simulation time
            new Label<>("final_dur", Double.class), // final cost
            new Label<>("final_dis", Double.class), // final cost
            new Label<>("final_bal", Double.class), // final cost
            new Label<>("rejected_count", Integer.class), // number of rejected requests
            new Label<>("rejected", String.class), // detail of rejected requests
            new Label<>("static_dur", Double.class), // a-posteriori solution cost
            new Label<>("static_dis", Double.class), // a-posteriori solution cost
            new Label<>("static_bal", Double.class), // a-posteriori solution cost
            new Label<>("static_num_req", Integer.class), // number of request in the a-posteriori solution
            new Label<>("vi", Double.class), // value of information
            new Label<>("bks", Double.class), // bks value
            new Label<>("seeds", String.class), // seeds
            new Label<>("final_sol", String.class) // detailed final solution
                                          };

    private MSASimulator           mMSASimulator;

    /**
     * Returns the MSA simulator used in this run
     * 
     * @return the MSA simulator used in this run
     */
    public MSASimulator getMSASimulator() {
        return mMSASimulator;
    }

    private DTRSPSimulationCallback mSimCallback;

    public DTRSPSimulationCallback getSimCallback() {
        return mSimCallback;
    }

    public DTRSPRunMPA(Integer runId, TRSPInstance instance, TRSPGlobalParameters params,
            BestKnownSolutions bks, Integer run, String com) {
        super(runId, instance, params, bks, run, com);
        mMSASimulator = new MSASimulator(getMPA(), getInstance().size()
                + getInstance().getFleet().size(), getParameters().getSimSpeed(getInstance()));
        mSimCallback = new DTRSPSimulationCallback(this);
    }

    public DTRSPRunMPA(Integer runId, File instanceFile, File rdFile, TRSPGlobalParameters params,
            BestKnownSolutions bks, Integer run, String comment) {
        super(runId, instanceFile, rdFile, params, bks, run, comment);
    }

    @Override
    public void initialize() throws Exception {
        super.initialize();
        mMSASimulator = new MSASimulator(getMPA(), getInstance().size()
                + getInstance().getFleet().size(), getParameters().getSimSpeed(getInstance()));
        mSimCallback = new DTRSPSimulationCallback(this);
    }

    @Override
    public MPASolver getSolver() {
        return (MPASolver) super.getSolver();
    }

    public MSASequential<DTRSPSolution, TRSPInstance> getMPA() {
        return getSolver().getMPA();
    }

    public static void setupVerboseLoggers() {
        TRSPLogging.getRunLogger().setLevel(LoggerHelper.LEVEL_DEBUG);
        Logging.getSetupLogger().setLevel(LoggerHelper.LEVEL_INFO);
        MSALogging.getBaseLogger().setLevel(LoggerHelper.LEVEL_WARN);
        MSALogging.getProcedureLogger().setLevel(LoggerHelper.LEVEL_DEBUG);
        MSALogging.getSimulationLogger().setLevel(LoggerHelper.LEVEL_INFO);
        TRSPLogging.getSimulationLogger().setLevel(LoggerHelper.LEVEL_INFO);
        TRSPLogging.getOptimizationLogger().setLevel(LoggerHelper.LEVEL_WARN);
    }

    @Override
    public TRSPSolution call() throws Exception {
        if (!isInitialized())
            initialize();
        if (!getInstance().isDynamic())
            throw new IllegalStateException("Instance must be dynamic");
        return super.call();
    }

    @Override
    protected DTRSPSolution dynamicRun(boolean abortAfterInit) {
        // Add all the request released events
        for (TRSPRequest r : getInstance().getSimulator().getUnreleasedRequests()) {
            getMSASimulator().scheduleEvent(
                    new NewRequestEvent(r.getReleaseDate(), getMPA().getEventFactory(), r));
        }
        // Start and initialize MPA
        getSimCallback().registerAsCallback(getMPA());
        getSolver().call();

        // Schedule a start of service event for all vehicles
        for (Technician t : getInstance().getFleet()) {
            getMSASimulator().scheduleEvent(
                    ResourceEvent.newStartServiceEvent(t.getHome().getTimeWindow().startAsDouble(),
                            getMPA().getEventFactory(), t.getID(), null));
        }

        // Start the simulation
        getMSASimulator().runInNewThread();

        while (getMPA().getStatus() != ProcedureStatus.TERMINATED
                && getMPA().getStatus() != ProcedureStatus.EXCEPTION)
            try {
                getMPA().acquireLock();
                getMPA().getStatusCond().await();
                getMPA().releaseLock();
                // Thread.sleep(500);
            } catch (InterruptedException e) {
                TRSPLogging.getRunLogger().exception("DTRSPRunMPA.call", e);
                getMPA().stop();
            }

        getMSASimulator().stop();
        // for (IScenario s : getMPA().getProxy().getScenarioPool())
        // TRSPLogging.getRunLogger().debug(s);

        if (getSolver().getMPA().getStatus() == ProcedureStatus.TERMINATED) {
            setFinalSolution(getSimulator().getCurrentSolution().clone());
            getSolver().setFinalSolution(getFinalSolution());

            aPosterioriRun();
            // getSimulator().terminate(getFinalSolution());

            return (DTRSPSolution) getMPA().getCurrentSolution();
        } else {
            setFinalSolution(null);
            getSolver().setFinalSolution(null);
            setStaticSolution(null);
            return null;
        }
    }

    @Override
    public TRSPSolution aPosterioriRun() {
        // Setup an ALNS+SC solver
        ALNSSCSolver solver = new ALNSSCSolver(getInstance(), getParameters());

        // Remove serviced constraints
        ListIterator<IConstraint<TRSPSolution>> it = getSolver().getSolCtrHandler().iterator();
        while (it.hasNext()) {
            IConstraint<TRSPSolution> c = it.next();
            if (c.getClass() == ServicedRequestsConstraint.class)
                it.remove();
        }

        getInstance().getSimulator().staticSetup();

        TRSPLogging
                .getRunLogger()
                .info("TRSPRun.call %s:  Solving the static problem (%s accepted requests) - Rejected requests:%s Served Requests:%s",
                        TRSPBench.getInstance().getProgress(),
                        getInstance().getReleasedRequests().size(),
                        getSimulator().getRejectedRequests(), getSimulator().getServedRequests());

        TRSPSolution staticSol = solver.call();
        if (staticSol == null) {
            staticSol = solver.getALNSSol();
        }

        setStaticSolution(staticSol);

        return getStaticSolution();
    }

    @Override
    public Label<?>[] getLabels() {
        return LABELS;
    }

    @Override
    public void collectStats(StatCollector col, boolean exception) {
        double[] evalFinal = evaluateSolution(getFinalSolution());
        double[] evalStatic = evaluateSolution(getStaticSolution());

        String group = getInstance().getName().substring(0,
                (getInstance().getName().contains("RC") ? 3 : 2));
        int idx = getComment().indexOf("rd_");
        String dod = getComment().substring(idx + 3, idx + 5);

        int obj = getParameters().get(TRSPGlobalParameters.ALNS_COST_DELEGATE) == TRSPWorkingTime.class ? 0
                : 1;
        double vi = (evalFinal[obj] - evalStatic[obj]) / evalStatic[obj];

        Object[] stats = new Object[] {
                getRunId(),
                getInstance().getName(), // name
                group, // group
                getInstance().getRequestCount(), // size
                getInstance().getFleet().size(), // crew
                dod,// degree of dynamism
                getRun(), // run
                getComment(), // comment
                getSolver().getMPAThread().getException() == null ? getSolver().getStatus()
                        .toString() : String.format("%s (%s)", getSolver().getStatus(), getSolver()
                        .getMPAThread().getException()),
                getMPA().getTimer().readTimeS(), // final wall clock time
                getMSASimulator().simulationTime(), // final simulation time
                evalFinal[0], //
                evalFinal[1], //
                evalFinal[2], //
                getInstance().getSimulator().getRejectedRequests().size(),// number of rejected requests
                Utilities.toShortString(getInstance().getSimulator().getRejectedRequests()),// detail of rejected
                                                                                            // requests
                evalStatic[0], //
                evalStatic[1], //
                evalStatic[2], //
                // cost
                getStaticSolution() == null ? 0 : getStaticSolution().getServedCount(),// number of request in the
                                                                                       // a-posteriori solution
                vi,// value of information
                getBKS().getBKS(getInstance().getName()),// bks value
                Utilities.toShortString(getParameters().get(TRSPGlobalParameters.RUN_SEEDS)),// seeds
                getSolver().getFinalSolution() == null ? "na" : getSolver().getFinalSolution()
                        .toShortString() // detailed final solution
        };

        col.collect(stats);
    }

    public static DTRSPRunMPA newDTRSPRunMPATest(boolean trsp, String instanceFile, int dod) {

        String configFile = "./config/bench/" + //
                (trsp ? "bench_dtrsp_mpa_25crew.cfg" : //
                        "bench_dvrptw_???.cfg");

        LoggerHelper.setupRootLogger(LoggerHelper.LEVEL_WARN, LoggerHelper.LEVEL_DEBUG, true);
        TRSPLogging.getRunLogger().setLevel(LoggerHelper.LEVEL_DEBUG);
        TRSPLogging.getProcedureLogger().setLevel(LoggerHelper.LEVEL_INFO);
        Logging.getSetupLogger().setLevel(LoggerHelper.LEVEL_INFO);

        TRSPGlobalParameters params = new TRSPGlobalParameters();
        try {
            params.loadParameters(new File(configFile));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        HashTourPool.sCountCollisions = true;

        // Disable SC
        params.set(TRSPGlobalParameters.SC_ENABLED, false);
        // params.set(TRSPGlobalParameters.SC_MAX_TIME, 60d);
        // Set ALNS iterations
        // params.set(TRSPGlobalParameters.ALNS_PARALLEL, true);
        // params.set(TRSPGlobalParameters.ALNS_MAX_IT, 25000);
        // params.set(TRSPGlobalParameters.ALNS_PALNS_IT_P, 25000);
        // Set pool
        params.set(TRSPGlobalParameters.ALNS_PALNS_POOL, DiversifiedPool.class);
        params.set(TRSPGlobalParameters.ALNS_PALNS_DIV_METRIC, BrokenPairsDistance.class);

        // Enable logging
        // params.set(TRSPGlobalParameters.ALNS_ENABLE_LOGGING, true);

        // Fix the number of threads
        params.set(TRSPGlobalParameters.THREAD_COUNT,
                Math.min(params.getThreadCount(), Runtime.getRuntime().availableProcessors()));

        // Setup the TRSPBench
        TRSPBench.setup(params, true, "TRSPRunBase_test");

        ITRSPPersistenceHelper reader = TRSPUtilities.getPersistenceHelper(params
                .get(TRSPGlobalParameters.RUN_INSTANCE_FOLDER));
        TRSPInstance instance = null;
        String com = "";
        try {
            instance = reader.readInstance(
                    new File(String.format("%s/%s",
                            params.get(TRSPGlobalParameters.RUN_INSTANCE_FOLDER), instanceFile)),
                    params.isCVRPTW());
            if (params.isDynamic()) {
                Map<String, List<File>> rdFileMapping = DynamicPersistenceHelper.getRelDateFiles(
                        params.get(TRSPGlobalParameters.RUN_REL_DATE_FOLDER), new int[] { dod });
                List<File> rdFiles = rdFileMapping.get(instance.getName());
                if (rdFiles != null && !rdFiles.isEmpty()) {
                    DynamicTRSPPersistenceHelper.readRelDates(instance, rdFiles.get(0),
                            params.isCVRPTW());
                    com = rdFiles.get(0).getName();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
        }

        DTRSPRunMPA run = new DTRSPRunMPA(0, instance, params, TRSPBench.getInstance().getBKS(), 0,
                com);

        return run;
    }

    public static void main(String[] args) {
        TRSPRunBase run = newDTRSPRunMPATest(true, "C104.100_25-5-5-5.txt", 10);

        try {
            run.call();
        } catch (Exception e) {
            TRSPLogging.getBaseLogger().exception("DTRSPRunMPA.main", e);
        }
        // run.testDynRun();

        System.out.println("Checksol : "
                + TRSPDetailedSolutionChecker.INSTANCE.checkSolution(run.getSolver()
                        .getFinalSolution()));
        Double bks = run.getBKS().getBKS(run.getInstance().getName());
        if (bks != null) {
            double cost = run.getSolver().getFinalSolution().getObjectiveValue();
            System.out.println("BKS      : " + bks);
            System.out.printf("GAP      : %.2f\n", (cost - bks) / bks * 100);
        }
    }
}
