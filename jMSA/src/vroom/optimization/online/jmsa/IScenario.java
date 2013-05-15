package vroom.optimization.online.jmsa;

import java.util.List;

import vroom.common.utilities.IDerefenceable;
import vroom.common.utilities.ILockable;
import vroom.common.utilities.optimization.ISolution;

/**
 * Creation date: Feb 25, 2010 - 3:29:49 pm<br/>
 * <code>IScenario</code> is a general interface for classes that represent a scenario in the context of a multiple
 * scenario approach. Scenarios contains both actual and sampled requests that are used to determine a mSolution to the
 * initial problem.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 * @see MultipleScenarioApproach
 */
public interface IScenario extends ILockable, IDerefenceable, ISolution {

    /**
     * @return a list of the actual requests contained in this scenario
     */
    public List<? extends IActualRequest> getActualRequests();

    /**
     * @return a list of the sampled requests contained in this scenario
     */
    public List<? extends ISampledRequest> getSampledRequests();

    /**
     * Getter for the first request
     * 
     * @param resource
     *            the considered resource
     * @return the first actual request of this scenario for the given resource
     */
    public IActualRequest getFirstActualRequest(int resource);

    /**
     * Fix the first actual request for a given resource and remove all the preceding sampled requests
     * 
     * @param resource
     *            the considered resource
     * @return the actual request that has been fixed
     */
    public IActualRequest fixFirstActualRequest(int resource);

    /**
     * Mark the last visit of the given resource as served
     * 
     * @param resource
     *            the considered resource
     * @return <code>true</code> if the last visit was not already marked as served
     */
    boolean markLastVisitAsServed(int resource);

    /**
     * Sequence of requests
     * 
     * @param resource
     *            the considered resource
     * @return a list of the requests associated with <code>resource</code> in the order in which they appear in the
     *         scenario
     */
    public List<? extends IActualRequest> getOrderedActualRequests(int resource);

    /**
     * Sequence of sampled requests
     * 
     * @param resource
     *            the considered resource
     * @return a list of the sampled requests associated with <code>resource</code> in the order in which they appear in
     *         the scenario
     */
    public List<? extends ISampledRequest> getOrderedSampledRequests(int resource);

    /**
     * Number of resources used in this scenario
     * 
     * @return the number of resources used in this scenario
     */
    public int getResourceCount();

    /**
     * Increment the number of non-improving optimizations
     */
    public void incrementNonImprovingCount();

    /**
     * Reset the number of non-improving optimizations
     */
    public void resetNonImprovingCount();

    /**
     * Return the number of non-improving optimizations
     * 
     * @return the number of non-improving optimizations
     */
    public int getNonImprovingCount();
}