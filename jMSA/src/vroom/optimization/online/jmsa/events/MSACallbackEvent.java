package vroom.optimization.online.jmsa.events;

import vroom.common.utilities.callbacks.CallbackEventBase;
import vroom.common.utilities.callbacks.CallbackManagerDelegate;
import vroom.common.utilities.callbacks.ICallback;
import vroom.common.utilities.callbacks.ICallbackEvent;
import vroom.common.utilities.callbacks.ICallbackEventTypes;
import vroom.common.utilities.events.IEventHandler;
import vroom.optimization.online.jmsa.IDistinguishedSolution;
import vroom.optimization.online.jmsa.MSABase;

/**
 * <code>MSACallBackEvents</code> is an implementation of {@link ICallbackEvent} for the multiple plan approach.
 * <p>
 * It provides a description for events that will be generated during the MSA procedure
 * </p>
 * 
 * @see ICallback
 * @see MultipleScenarioApproach
 * @see CallbackManagerDelegate
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:07:04 a.m.
 */
public class MSACallbackEvent extends CallbackEventBase<MSABase<?, ?>, MSACallbackEvent.EventTypes> {

    /**
     * Creates a new <code>MSACallbackEvents</code>
     * 
     * @param type
     *            the type of event from the {@link EventTypes} enumeration
     * @param source
     *            the {@link MultipleScenarioApproach} that generated this event
     * @param params
     *            optional parameters
     */
    public MSACallbackEvent(EventTypes type, MSABase<?, ?> source, Object... params) {
        super(type, source, params);
    }

    /**
     * Creation date: Mar 9, 2010 - 9:57:04 AM<br/>
     * <code>EventTypes</code> is an enumeration of the possible types of events that will be be generated by the MSA
     * procedure and to which objects implementing {@link ICallback} can be associated.
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp" >SLP</a>
     * @version 1.0
     */
    public static enum EventTypes implements ICallbackEventTypes {
        /**
         * Event raised each time the MSA procedure retrieves a new {@link MSAEvent} . <br/>
         * The following parameters will be passed to the callback implementation of
         * {@link ICallback#execute(ICallbackEvent, Object...)} (in order):<br/>
         * <ul>
         * <li>{@link MSAEvent} The retrieved event</li>
         * <li>{@link IEventHandler} associatedHandler</li>
         * </ul>
         */
        MSA_NEW_EVENT("New event retreived"),
        /**
         * A generic event raised before handling a {@link MSAEvent} <br/>
         * The following parameters will be passed to the callback implementation of
         * {@link ICallback#execute(ICallbackEvent, Object...)} (in order):<br/>
         * <ul>
         * <li>{@link MSAEvent} The retrieved event</li>
         * <li>{@link IEventHandler} The handler associated with the retrieved event</li>
         * </ul>
         */
        MSA_EVENT_HANDLING_START("Event handling start"),
        /**
         * A generic event raised after handling a {@link MSAEvent} <br/>
         * The following parameters will be passed to the callback implementation of
         * {@link ICallback#execute(ICallbackEvent, Object...)} (in order):<br/>
         * <ul>
         * <li>{@link MSAEvent} The retrieved event</li>
         * <li>{@link IEventHandler} The handler associated with the retrieved event</li>
         * </ul>
         */
        MSA_EVENT_HANDLING_END("Event handling end"),
        /**
         * Event raised any time an optimization procedure is started <br/>
         * The following parameters will be passed to the callback implementation of
         * {@link ICallback#execute(ICallbackEvent, Object...)} (in order):<br/>
         * <ul>
         * <li>JAVADOC Define parameters</li>
         * </ul>
         */
        MSA_OPTIMIZATION_START("Optimization started"),
        /**
         * Event raised when an optimization procedure ends <br/>
         * The following parameters will be passed to the callback implementation of
         * {@link ICallback#execute(ICallbackEvent, Object...)} (in order):<br/>
         * <ul>
         * <li>JAVADOC Define parameters</li>
         * </ul>
         */
        MSA_OPTIMIZATION_END("Optimization finised"),
        /**
         * Event raised when a new distinguished mSolution is gnerated <br/>
         * The following parameters will be passed to the callback implementation of
         * {@link ICallback#execute(ICallbackEvent, Object...)} (in order):<br/>
         * <ul>
         * <li> {@link IDistinguishedSolution} The previous distinguished mSolution</li>
         * <li> {@link IDistinguishedSolution} The new distinguished mSolution</li>
         * </ul>
         */
        MSA_NEW_DISTINGUISHED_SOLUTION("New distinguished mSolution"),
        /**
         * Event raised when the main MSA procedure is started
         */
        MSA_START("MSA procedure started"),
        /**
         * Event raised when the main MSA procedure terminates
         */
        MSA_END("MSA procedure terminated"),

        /**
         * Event raised when a {@link ResourceHandler} finished the handling of a {@link ResourceEvent} <br/>
         * The following parameters will be passed to the callback implementation:
         * <ul>
         * <li>{@link ResourceEvent} The corresponding event</li>
         * <li><code>boolean</code> <code>true</code> if the event has been successfully handled, <code>false</code> if
         * an error occurred</li>
         * </ul>
         */
        EVENTS_RESOURCE("Resource event handled");

        private final String mDescription;

        private EventTypes(String description) {
            mDescription = description;
        }

        /**
         * @return a description of this event
         */
        @Override
        public String getDescription() {
            return mDescription;
        }
    }
}