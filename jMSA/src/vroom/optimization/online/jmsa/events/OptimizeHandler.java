package vroom.optimization.online.jmsa.events;

import vroom.common.utilities.Stopwatch;
import vroom.common.utilities.events.EventHandlingException;
import vroom.common.utilities.events.IEventHandler;
import vroom.optimization.online.jmsa.IInstance;
import vroom.optimization.online.jmsa.IScenario;
import vroom.optimization.online.jmsa.MSABase.MSAProxy;

/**
 * <code>OptimizeHandler<code> is an implementation of  {@link
 * IEventHandler}responsible of the handling of {@link OptimizeEvent} events.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a href="http://copa.uniandes.edu.co">Copa</a>, <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:07:01 a.m.
 */
public class OptimizeHandler<S extends IScenario, I extends IInstance> extends
        MSAEventHandler<OptimizeEvent, S, I> {

    public OptimizeHandler(MSAProxy<S, I> parentMSA) {
        super(parentMSA);
    }

    @Override
    public boolean canHandleEvent(OptimizeEvent event) {
        return true;
    }

    @Override
    public boolean handleEvent(OptimizeEvent event) throws EventHandlingException {

        // MSALogging.getEventsLogger().lowDebug(
        // "OptimizeHandler.handleEvent: Optimize the pool (%s)", event.getParameters());

        // Optimize all scenarios in the pool
        Stopwatch t = new Stopwatch();
        t.start();
        getParentMSAProxy().getComponentManager().optimizePool(event.getParameters());
        t.stop();

        // Prevent log flooding
        // if (t.readTimeS() > 1) {
        // MSALogging.getEventsLogger().info(
        // "OptimizeHandler.handleEvent: Pool optimized in %sms (%s)", t.getTime(),
        // event.getParameters());
        // }

        // Generate new scenarios
        event.getSource().raiseGenerateScenarioEvent();

        return true;
    }

}