/**
 * 
 */
package vroom.trsp.optimization.alns;

import java.util.Set;

import vroom.common.heuristics.alns.IDestroy;
import vroom.common.heuristics.alns.IDestroy.IDestroyResult;
import vroom.trsp.datamodel.TRSPSolution;

/**
 * <code>TRSPDestroyResult</code> is an implementation of {@link IDestroyResult} that stores the results of a
 * {@link IDestroy#destroy(vroom.common.utilities.optimization.ISolution, vroom.common.utilities.optimization.IParameters, int)
 * destroy} operation in the form of a collection of removed requests
 * <p>
 * Creation date: May 13, 2011 - 1:35:47 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TRSPDestroyResult implements IDestroyResult<TRSPSolution> {

    private final Set<Integer> mRemovedRequests;

    /**
     * Creates a new <code>TRSPDestroyResult</code>
     * 
     * @param remReq
     *            the requests that were removed during the destroy operation
     */
    public TRSPDestroyResult(Set<Integer> remReq) {
        mRemovedRequests = remReq;
    }

    @Override
    public boolean isNull() {
        return mRemovedRequests != null && !mRemovedRequests.isEmpty();
    }

    /**
     * Returns the list of removed requests
     * 
     * @return the list of removed requests
     */
    public Set<Integer> getRemovedRequests() {
        return mRemovedRequests;
    }

    @Override
    public String toString() {
        return mRemovedRequests != null ? vroom.common.utilities.Utilities.toShortString(mRemovedRequests) : "[]";
    }
}
