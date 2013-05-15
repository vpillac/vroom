package vroom.optimization.online.jmsa;

import vroom.optimization.online.jmsa.components.ComponentManager;
import vroom.optimization.online.jmsa.events.DecisionHandler;
import vroom.optimization.online.jmsa.events.MSACallbackEvent;

public interface MSAProxy<SS extends IScenario, II extends IInstance> {

	/**
	 * Getter for the component manager
	 * 
	 * @return the component manager of the associated MSA instance
	 */
	public ComponentManager<SS, II> getComponentManager();

	/**
	 * Setter for the distinguished mSolution, should only be called by
	 * instances of {@link DecisionHandler}
	 * 
	 * @param distinguishedSolution
	 *            The new distinguished mSolution
	 */
	public void setDistinguishedSolution(
			IDistinguishedSolution distinguishedSolution);

	/**
	 * Getter for the global parameters
	 * @return the global parameters associated with this 
	 * msa procedure
	 */
	public MSAGlobalParameters getParameters();

	/**
	 * Getter for the instance
	 * 
	 * @return the instance associated with this msa procedure
	 */
	public II getInstance();

	/**
	 * Getter for the scenario pool
	 * 
	 * @return the scenario pool of this msa procedure
	 */
	public ScenarioPool<SS> getScenarioPool();

	/**
	 * Getter proy for the distinguished mSolution
	 * 
	 * @return The current distinguished mSolution
	 * 
	 * @see MultipleScenarioApproachMT#getDistinguishedSolution()
	 */
	public IDistinguishedSolution getDistinguishedSolution();

	/**
	 * Execute the callbacks associated with <code>event</code>
	 * 
	 * @param eventType
	 *            the event type that has occurred and for which the
	 *            associated callbacks will be run
	 * @param params
	 *            an optional parameter that will be transmitted to the
	 *            callback
	 */
	public void callbacks(MSACallbackEvent.EventTypes eventType,
			Object... params);

}