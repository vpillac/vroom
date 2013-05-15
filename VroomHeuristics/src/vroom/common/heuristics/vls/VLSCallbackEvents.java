package vroom.common.heuristics.vls;

import vroom.common.utilities.callbacks.ICallbackEventTypes;
import vroom.common.utilities.optimization.ISolution;

/**
 * <code>VLSCallbackEvents</code> is an implementation of
 * {@link ICallbackEventTypes} that is used within the
 * {@link VersatileLocalSearch} to describe callback events.
 * <p>
 * Creation date: Apr 26, 2010 - 2:17:21 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los
 *         Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public enum VLSCallbackEvents implements ICallbackEventTypes {
	/**
	 * Event raised whenever a mSolution has been accepted </p>The following
	 * parameters will be passed to the callback implementation:
	 * <ul>
	 * <li>{@link ISolution} The accepted mSolution</li>
	 * <li>{@link VLSPhase} The current phase</li>
	 * <li>{@link IVLSState} The current state</li>
	 * </ul>
	 */
	SOLUTION_ACCEPTED("Solution accepted"),
	/**
	 * Event raised whenever a mSolution has been rejected </p>The following
	 * parameters will be passed to the callback implementation:
	 * <ul>
	 * <li>{@link ISolution} The accepted mSolution</li>
	 * <li>{@link VLSPhase} The current phase</li>
	 * <li>{@link IVLSState} The current state</li>
	 * </ul>
	 */
	SOLUTION_REJECTED("Solution rejected");

	/** A description for this event **/
	private final String mDescription;

	private VLSCallbackEvents(String desc) {
		mDescription = desc;
	}

	/**
	 * Getter for this event description
	 * 
	 * @return A description for this event
	 */
	@Override
	public String getDescription() {
		return mDescription;
	}

}
