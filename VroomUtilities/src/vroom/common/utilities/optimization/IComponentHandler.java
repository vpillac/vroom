package vroom.common.utilities.optimization;

import java.util.List;

import vroom.common.utilities.IDisposable;

/**
 * <code>IComponentHandler</code> is the interface for classes responsible for the handling of components in a generic
 * {@linkplain VariableComponentSearch variable component search}.
 * <p>
 * Creation date: 2 juil. 2010 - 20:11:22
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 * @param <S>
 */
public interface IComponentHandler<M> extends IDisposable {

    /**
     * <code>Outcome</code> is an enumeration of the possible outcomes resulting from the execution of a component.
     * <p>
     * Possible values are: {@link #NEW_BEST}, {@link #REJECTED}, {@link #ACCEPTED}
     * </p>
     * Creation date: 2 juil. 2010 - 20:11:22
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    public static enum Outcome {
        NEW_BEST, REJECTED, ACCEPTED
    }

    /**
     * Getter for the list of components
     * 
     * @return the list containing the components
     */
    public List<M> getComponents();

    /**
     * Selects the next component and return it
     * 
     * @return the next component to be used or <code>null</code> if all components have been used
     */
    public M nextComponent();

    /**
     * Return <code>true</code> if all components have been used
     * 
     * @return <code>true</code> if all components have been used
     */
    public boolean isCompletelyExplored();

    /**
     * Update the handler state at the end of an iteration
     * 
     * @param currentComponent
     *            the last used component
     * @param improvement
     *            the improvement in the objective function in the last iteration. Independently of optimization
     *            direction positive values are considered as improvement.
     * @param time
     *            the time spent on the last iteration (in ms)
     * @param iteration
     *            the last iteration number
     * @param outcome
     *            the result
     * @return <code>true</code> if the update changed the possible output of the next call to {@link #nextComponent()}
     */
    public boolean updateStats(M currentComponent, double improvement, double time, int iteration, Outcome outcome);

    /**
     * Returns the weight associated with a given component.
     * <p>
     * The weight of a component should reflect the likelihood of being selected at the next call of
     * {@link #nextComponent()}
     * </p>
     * 
     * @param component
     *            the component to be evaluated
     * @return the weight of <code>component</code>
     */
    public double getWeight(M component);

    /**
     * Initialize the handler
     * 
     * @param instance
     *            the instance that will be used
     */
    public void initialize(IInstance instance);

    /**
     * Reset the handler
     */
    public void reset();
}
