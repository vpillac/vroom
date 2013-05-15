/**
 * 
 */
package vroom.common.heuristics.alns;

import vroom.common.utilities.callbacks.CallbackEventBase;
import vroom.common.utilities.optimization.ISolution;

/**
 * The Class <code>ALNSCallbackEvent</code> is a specialization of {@link CallbackEventBase} for the ALNS procedure.
 * <p>
 * Creation date: May 27, 2011 - 4:07:13 PM.
 * 
 * @param <S>
 *            the generic type
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class ALNSCallbackEvent<S extends ISolution> extends
        CallbackEventBase<AdaptiveLargeNeighborhoodSearch<S>, ALNSEventType> {

    /** the time elapsed since the beginning of the ALNS at the moment at which the event was raised **/
    private final double mElapsedTime;

    /**
     * Getter for the time elapsed since the beginning of the ALNS at the moment at which the event was raised
     * 
     * @return the time elapsed since the beginning of the ALNS at the moment at which the event was raised
     */
    public double getElapsedTime() {
        return this.mElapsedTime;
    }

    /** the current iteration **/
    private final int mCurrentIteration;

    /**
     * Getter for the current iteration
     * 
     * @return the current iteration
     */
    public int getCurrentIteration() {
        return this.mCurrentIteration;
    }

    /**
     * Creates a new <code>ALNSCallbackEvent</code>.
     * 
     * @param type
     *            the type of event from the {@link ALNSEventType} enumeration
     * @param source
     *            the {@link AdaptiveLargeNeighborhoodSearch} that generated this event
     * @param params
     *            optional parameters
     */
    public ALNSCallbackEvent(ALNSEventType type, AdaptiveLargeNeighborhoodSearch<S> source,
            double elapsedTime, int currentIteration, Object... params) {
        super(type, source, params);
        mElapsedTime = elapsedTime;
        mCurrentIteration = currentIteration;
    }
}
