package vroom.optimization.online.jmsa.events;

import vroom.common.utilities.events.EventHandlingException;
import vroom.common.utilities.events.IEventHandler;
import vroom.optimization.online.jmsa.IInstance;
import vroom.optimization.online.jmsa.IScenario;
import vroom.optimization.online.jmsa.MSABase.MSAProxy;
import vroom.optimization.online.jmsa.utils.MSALogging;

/**
 * <code>NewRequestHandler<code> is an implementation of  {@link
 * IEventHandler}responsible of the handling of {@link NewRequestEvent} events.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:07:00 a.m.
 */
public class NewRequestHandler<S extends IScenario, I extends IInstance> extends MSAEventHandler<NewRequestEvent, S, I> {

    public NewRequestHandler(MSAProxy<S, I> parentMSA) {
        super(parentMSA);
    }

    @Override
    public boolean canHandleEvent(NewRequestEvent event) {
        return true;
    }

    @Override
    public boolean handleEvent(NewRequestEvent event) throws EventHandlingException {

        // Check if the request can be serviced
        if (getParentMSAProxy().getComponentManager().canBeServiced(event.getNewRequest())) {
            MSALogging.getEventsLogger().info("NewRequestHandler.handleEvent: Request accepted (%s)",
                    event.getNewRequest());

            // Add request to the instance
            getParentMSAProxy().getInstance().acquireLock();
            getParentMSAProxy().getInstance().requestReleased(event.getNewRequest());
            getParentMSAProxy().getInstance().releaseLock();

            // Insert request in compatible scenarios and remove incompatible
            // scenarios
            getParentMSAProxy().getComponentManager().insertRequest(event.getNewRequest());

            getParentMSAProxy().getComponentManager().getRequestValidator().requestAccepted(event.getNewRequest());

            // Generate new scenarios
            event.getSource().raiseGenerateScenarioEvent();
        } else {
            MSALogging.getEventsLogger().info("NewRequestHandler.handleEvent: Request rejected (%s)",
                    event.getNewRequest());
            getParentMSAProxy().getComponentManager().getRequestValidator().requestRejected(event.getNewRequest());
            // Optimize existing scenarios
            event.getSource().raiseOptimizeEvent();
        }

        return true;
    }

}