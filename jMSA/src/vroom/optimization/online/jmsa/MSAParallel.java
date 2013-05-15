package vroom.optimization.online.jmsa;

import vroom.common.utilities.Stopwatch;
import vroom.optimization.online.jmsa.components.ComponentManager;
import vroom.optimization.online.jmsa.components.RequestSamplerParam;
import vroom.optimization.online.jmsa.components.ScenarioGeneratorParam;
import vroom.optimization.online.jmsa.utils.MSALogging;

/**
 * <code>MSAParallel</code> is an event-driven and parallel implementation of a multiple scenario approach procedure.
 * 
 * @param <S>
 *            the type of scenario that will be handled in by the MSA algorithm
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:06:45 a.m.
 * @deprecated This implementation has not been kept up to date, use {@link MSASequential} instead which delegates
 *             parallelization to the {@link ComponentManager}
 */
@Deprecated
public class MSAParallel<S extends IScenario, I extends IInstance> extends MSABase<S, I> {

    public MSAParallel(I instance, MSAGlobalParameters parameters) {
        super(instance, parameters);
    }

    /**
     * The main method for the MSA procedure
     */
    @Override
    protected void msaProcedure() {
        Stopwatch procTimer = new Stopwatch();
        setInitialized(false);

        int poolSize = getParameter(MSAGlobalParameters.POOL_SIZE);
        double initialProp = getParameter(MSAGlobalParameters.POOL_INITIAL_PROPORTION);

        int sampledReqCount = getParameter(MSAGlobalParameters.SAMPLED_REQUEST_COUNT);

        ScenarioGeneratorParam params = new ScenarioGeneratorParam((int) (poolSize * initialProp),
                getParameter(MSAGlobalParameters.GEN_MAX_SCEN_OPT_TIME), new RequestSamplerParam(sampledReqCount));

        procTimer.start();
        MSALogging.getProcedureLogger().info("Scenario pool initialization started: %s", params);
        mComponentManager.generateScenarios(params);
        setInitialized(true);
        procTimer.stop();
        MSALogging.getProcedureLogger().info("Scenario pool initialization terminated in %ss", procTimer.readTimeS());
        MSALogging.getProcedureLogger().debug("Current state: %ss", this);

        mEventFactory.raiseOptimizeEvent();

        while (isRunning()) {
            synchronized (this) {
                try {
                    wait(500);
                } catch (InterruptedException e) {
                    MSALogging.getProcedureLogger().exception("MSAParallel.msaProcedure (awaiting termination)", e);
                }
            }
        }

        stop();
    }

}