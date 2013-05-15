package vroom.common.heuristics.alns;

import vroom.common.utilities.callbacks.ICallbackEventTypes;

/**
 * <code>ALNSEventType</code> is an enumeration of the different types of events to which callbacks can be associated
 * within an ALNS
 * <p>
 * Creation date: May 27, 2011 - 3:51:41 PM.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public enum ALNSEventType implements ICallbackEventTypes {

    /** ALNS Started (args: instance,solution) */
    STARTED("ALNS Started"),
    /** ALNS Finished (args: instance,final solution) */
    FINISHED("ALNS Finished"),
    /** Start of an iteration (args: current). */
    IT_STARTED("ALNS Iteration Started"),
    /** End of an iteration (args: best,current, tmp,iteration timer) */
    IT_FINISHED("ALNS Iteration Finished"),
    /** New best solution found (args: new best sol). */
    SOL_NEW_BEST("ALNS New Best Solution"),
    /** New current solution (args: best,new current sol). */
    SOL_NEW_CURRENT("ALNS New Current Solution"),
    /** The solution was rejected (args: rejected sol). */
    SOL_REJECTED("ALNS Solution Rejected"),
    /** The current solution was destroyed (args: best,current, tmp, destroy, result). */
    DESTROYED("ALNS Sol Destroyed"),
    /** The current solution was repaired (args: best,current, tmp, repair, repaired). */
    REPAIRED("ALNS Sol Repaired"),
    /** The components stats were updated (args: best,current, tmp, repair, repaired). */
    COMP_UPDATED("ALNS Components Updated");

    /** The Description. */
    private final String mDescription;

    /**
     * Instantiates a new event type.
     * 
     * @param description
     *            the description
     */
    private ALNSEventType(String description) {
        mDescription = description;
    }

    /**
     * Gets the description.
     * 
     * @return a description of this event
     */
    @Override
    public String getDescription() {
        return mDescription;
    }

}