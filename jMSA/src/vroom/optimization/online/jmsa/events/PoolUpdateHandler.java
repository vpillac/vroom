package vroom.optimization.online.jmsa.events;

import vroom.common.utilities.events.EventHandlingException;
import vroom.common.utilities.events.IEventHandler;
import vroom.optimization.online.jmsa.IInstance;
import vroom.optimization.online.jmsa.IScenario;
import vroom.optimization.online.jmsa.MSABase.MSAProxy;
import vroom.optimization.online.jmsa.utils.MSALogging;

/**
 * <code>PoolUpdateHandler<code> is an implementation of  {@link
 * IEventHandler}responsible of the handling of {@link PoolUpdateEvent} events.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:07:02 a.m.
 */
public class PoolUpdateHandler<S extends IScenario, I extends IInstance> extends
        MSAEventHandler<PoolUpdateEvent, S, I> {

    public PoolUpdateHandler(MSAProxy<S, I> parentMSA) {
        super(parentMSA);
    }

    @Override
    public boolean canHandleEvent(PoolUpdateEvent event) {
        return true;
    }

    @Override
    public boolean handleEvent(PoolUpdateEvent event) throws EventHandlingException {

        MSALogging.getEventsLogger().info("PoolUpdateHandler.handleEvent: Cleaning the pool (%s)",
                event);

        getParentMSAProxy().getComponentManager().cleanPool();

        event.getSource().raiseGenerateScenarioEvent();

        return true;

    }

}