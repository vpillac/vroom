/**
 * 
 */
package vroom.optimization.online.jmsa.events;

import vroom.common.utilities.callbacks.ICallback;
import vroom.common.utilities.callbacks.ICallbackEvent;
import vroom.optimization.online.jmsa.MSABase;
import vroom.optimization.online.jmsa.events.MSACallbackEvent.EventTypes;

/**
 * Creation date: Mar 9, 2010 - 10:15:15 AM<br/>
 * <code>MSACallback</code> is a convenience base class that implements {@link ICallback} for callbacks that will be
 * asociate with a MSA procedure
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public abstract class MSACallbackBase implements ICallback<MSABase<?, ?>, EventTypes> {

    @Override
    public final void execute(ICallbackEvent<MSABase<?, ?>, EventTypes> event) {

        if (event == null) {
            throw new IllegalArgumentException("Argument event cannot be null");
        } else if (event instanceof MSACallbackEvent) {
            this.execute((MSACallbackEvent) event);
        } else {
            throw new IllegalArgumentException(
                    "This methods expects an event of type MSACallbackEvent, was "
                            + event.getClass());
        }
    }

    /**
     * Execute this callback
     * 
     * @param event
     *            the event that caused the execution of this method
     */
    public abstract void execute(MSACallbackEvent event);

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int compareTo(ICallback<?, ?> o) {
        return o.getPriority() - getPriority();
    }
}
