package vroom.optimization.online.jmsa.events;

import vroom.common.utilities.Stopwatch;
import vroom.common.utilities.events.EventHandlingException;
import vroom.common.utilities.events.IEventHandler;
import vroom.optimization.online.jmsa.IInstance;
import vroom.optimization.online.jmsa.IScenario;
import vroom.optimization.online.jmsa.MSABase.MSAProxy;
import vroom.optimization.online.jmsa.utils.MSALogging;

/**
 * <code>GenerateHandler<code> is an implementation of  {@link
 * IEventHandler}responsible of the handling of {@link GenerateEvent} events.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:07:05 a.m.
 */
public class GenerateHandler<S extends IScenario, I extends IInstance> extends
        MSAEventHandler<GenerateEvent, S, I> {

    public GenerateHandler(MSAProxy<S, I> parentMSA) {
        super(parentMSA);
    }

    @Override
    public boolean canHandleEvent(GenerateEvent event) {
        return true;
    }

    @Override
    public boolean handleEvent(GenerateEvent event) throws EventHandlingException {

        MSALogging.getEventsLogger().lowDebug(
                "GenerateHandler.handleEvent: Generate new scenarios (%s)", event.getParameters());

        int newScen = getParentMSAProxy().getScenarioPool().size();

        // Generate new scenarios
        Stopwatch t = new Stopwatch();
        t.start();
        getParentMSAProxy().getComponentManager().generateScenarios(event.getParameters());
        newScen = getParentMSAProxy().getScenarioPool().size() - newScen;
        t.stop();

        // Prevent log flooding
        if (newScen > 0) {
            MSALogging.getEventsLogger().info(
                    "GenerateHandler.handleEvent: %s new scenarios generated in %sms (%s)",
                    newScen, t.readTimeMS(), event.getParameters());
        }

        // Optimize existing scenarios
        event.getSource().raiseOptimizeEvent();

        return true;
    }

}// end GenerateHandler