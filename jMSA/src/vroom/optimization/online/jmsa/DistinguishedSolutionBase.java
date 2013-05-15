package vroom.optimization.online.jmsa;

import java.util.Arrays;

/**
 * Creation date: Mar 8, 2010 - 4:27:38 PM<br/>
 * <code>DistinguishedSolution</code> is a basic implementation of {@link IDistinguishedSolution} that contains a list
 * of {@link IActualRequest} to be served next for each of the resources.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class DistinguishedSolutionBase implements IDistinguishedSolution {

    /** The next requests to be served by each resource */
    private final IActualRequest[] mNextRequests;

    /**
     * Creates a new <code>DistinguishedSolutionBase</code> with the given next requests
     * 
     * @param nextRequests
     *            an array containing the next request to be served for each resource
     */
    public DistinguishedSolutionBase(IActualRequest... nextRequests) {
        super();
        mNextRequests = nextRequests;
    }

    @Override
    public IActualRequest getNextRequest() {
        return getNextRequest(0);
    }

    @Override
    public IActualRequest getNextRequest(int resource) {
        return mNextRequests[resource];
    }

    @Override
    public String toString() {
        return Arrays.toString(mNextRequests);
    }
}
