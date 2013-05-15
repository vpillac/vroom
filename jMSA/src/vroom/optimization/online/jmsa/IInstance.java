package vroom.optimization.online.jmsa;

import java.util.List;

import vroom.common.utilities.ILockable;

/**
 * <code>IInstance<code> is the interface for all classes that represent an
 * instance of a given problem.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:06:42 a.m.
 */
public interface IInstance extends ILockable {

    /**
     * Getter for the name of this instance
     * 
     * @return a short name for this instance
     */
    public String getName();

    /**
     * Add a request to this instance
     * 
     * @param request
     *            the request to be added
     * @return true if the <code>request</code> was successfully added
     */
    public boolean requestReleased(IActualRequest request);

    /**
     * Set a resource as stopped
     * 
     * @param resourceId
     *            the id of the resource that finished its service
     * @param param
     *            an optional additional parameter
     */
    public void setResourceStopped(int resourceId, Object param);

    /**
     * Set a resource as started
     * 
     * @param resourceId
     *            the id of the resource that started its service
     * @param param
     *            an optional additional parameter
     */
    public void setResourceStarted(int resourceId, Object param);

    /**
     * Started state of a resource
     * 
     * @param resourceId
     *            the id of the resource
     * @return <code>true</code> if the specified resource is started
     */
    public boolean isResourceStarted(int resourceId);

    /**
     * Stopped state of a resource
     * 
     * @param resourceId
     *            the id of the resource
     * @return <code>true</code> if the specified resource is stopped (has definitively finished its servicing)
     */
    public boolean isResourceStopped(int resourceId);

    /**
     * Getter for a request by its id.
     * 
     * @param requestId
     *            the id of the considered request
     * @return the request with id <code>requestId</code>, or <code>null</code> if there is no such request
     */
    public IActualRequest getNodeVisit(int requestId);

    /**
     * List of served or assigned requests, in the order in which they have been served
     * 
     * @return a list containing the {@link IActualRequest} that have already been served or are being served
     */
    public List<? extends IActualRequest> getServedRequests();

    /**
     * List of served or assigned requests for a particular resource, in the order in which they have been served
     * 
     * @param resourceId
     *            the id (index) of the considered request
     * @return a list containing the {@link IActualRequest} that have already been served or are being served
     */
    public List<? extends IActualRequest> getServedRequests(int resourceId);

    /**
     * List of pending accepted requests
     * 
     * @return a list containing the {@link IActualRequest} that have not been served yet
     */
    public List<? extends IActualRequest> getPendingRequests();

    /**
     * Update this instance to reflect the commitment of a resource to a request
     * 
     * @param request
     *            the request that will be served next by the specified resource
     * @param resourceId
     *            the id of the resource that will be assigned to the given <code>request</code>
     * @return <code>true</code> if the request can be assigned to the given resource
     */
    public boolean assignRequestToResource(IActualRequest request, int resourceId);

    /**
     * Mark a pending request as served
     * 
     * @param request
     *            the request that has to be marked as served
     * @param resourceId
     *            the id of the resource that served the given <code>request</code>
     * @return <code>true</code> if the state of the instance is coherent with the served request
     */
    public boolean markRequestAsServed(IActualRequest request, int resourceId);

    /**
     * Getter for the current mSolution.
     * <p/>
     * This method will return an object representing the current (or final is the MSA procedure is terminated) request
     * sequence that have been served by each resource.
     * 
     * @return the current mSolution
     */
    public Object getCurrentSolution();
}