/**
 *
 */
package vroom.trsp.optimization.matheuristic;

import vroom.common.heuristics.alns.ALNSEventType;
import vroom.common.heuristics.alns.AdaptiveLargeNeighborhoodSearch;
import vroom.common.utilities.callbacks.CallbackBase;
import vroom.common.utilities.callbacks.ICallbackEvent;
import vroom.trsp.datamodel.HashTourPool;
import vroom.trsp.datamodel.ITRSPSolutionHasher;
import vroom.trsp.datamodel.ITRSPTourPool;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPSolution;

/**
 * JAVADOC <code>SolutionPoolCallBack</code>
 * <p>
 * Creation date: Aug 12, 2011 - 11:51:53 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TourPoolCallBack extends
        CallbackBase<AdaptiveLargeNeighborhoodSearch<TRSPSolution>, ALNSEventType> {

    private final ITRSPSolutionHasher mHasher;

    private final ITRSPTourPool       mTourPool;

    /**
     * Gets the tour pool maintained by this callback
     * 
     * @return the tour pool maintained by this callback
     */
    public ITRSPTourPool getTourPool() {
        return mTourPool;
    }

    /**
     * Getter for the hasher used to hash tours
     * 
     * @return the hasher used to hash tours
     */
    public ITRSPSolutionHasher getHasher() {
        return mHasher;
    }

    /**
     * Creates a new <code>SolutionPoolCallBack</code>
     * 
     * @param instance
     * @param numIterations
     */
    public TourPoolCallBack(TRSPInstance instance, int numIterations, ITRSPSolutionHasher hasher) {
        super();
        mTourPool = new HashTourPool(instance.getFleet().size(), numIterations, hasher);

        mHasher = hasher;
    }

    @Override
    public void execute(
            ICallbackEvent<AdaptiveLargeNeighborhoodSearch<TRSPSolution>, ALNSEventType> event) {
        if (event.getType() == ALNSEventType.REPAIRED) {
            getTourPool().add((TRSPSolution) event.getParams()[2]);
        }
    }

}
