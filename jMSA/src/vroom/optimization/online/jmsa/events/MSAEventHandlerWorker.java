/**
 * 
 */
package vroom.optimization.online.jmsa.events;

import vroom.common.utilities.Stopwatch;
import vroom.common.utilities.events.EventHandlerWorker;
import vroom.common.utilities.events.IEvent;
import vroom.common.utilities.events.IEventHandler;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.optimization.online.jmsa.MSABase;
import vroom.optimization.online.jmsa.events.MSACallbackEvent.EventTypes;
import vroom.optimization.online.jmsa.utils.MSALogging;

/**
 * <code>MSAEventHandlerCall</code> is an extension of {@link EventHandlerWorker} providing additional logging functionalities for the MSA context.
 * <p>
 * Creation date: 31/08/2010 - 13:48:04
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class MSAEventHandlerWorker<E extends IEvent<?>> extends EventHandlerWorker<E> {

    private final MSABase<?, ?> mMSA;

    protected MSAEventHandlerWorker(E event, IEventHandler<E> handler, MSABase<?, ?> msa) {
        super(event, handler);
        mMSA = msa;
    }

    @Override
    public Boolean call() throws Exception {
        if (getEvent() instanceof OptimizeEvent) {
            ((OptimizeEvent) getEvent()).setParameters(mMSA.getOptimizeParameters((MSAEvent) getEvent()));
        } else if (getEvent() instanceof GenerateEvent) {
            ((GenerateEvent) getEvent()).setParameters(mMSA.getGenerateParameters());
        }

        getLogger().debug("Handling event %s (%s)", getEvent(), getHandler());

        this.mMSA.callbacks(EventTypes.MSA_EVENT_HANDLING_START, getEvent(), getHandler());
        Stopwatch t = new Stopwatch();
        t.start();
        Boolean b = super.call();
        t.stop();
        this.mMSA.callbacks(EventTypes.MSA_EVENT_HANDLING_END, getEvent(), getHandler());

        getLogger().debug("Event %s processed in %sms (%s)", getEvent(), t.readTimeMS(), getHandler());

        return b;
    }

    @Override
    protected LoggerHelper getLogger() {
        return MSALogging.getEventsLogger();
    }
}
