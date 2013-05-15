/**
 * 
 */
package vroom.trsp.optimization;

import vroom.common.heuristics.Move;
import vroom.common.utilities.optimization.IMove;
import vroom.trsp.datamodel.ITRSPTour;

/**
 * <code>TRSPMove</code> is an extension of {@link IMove} for the TRSP
 * <p>
 * Creation date: May 18, 2011 - 1:46:27 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public abstract class TRSPMove extends Move {
    /** the tour affected by this move */
    private final ITRSPTour mTour;

    /**
     * Returns the tour affected by this move
     * 
     * @return the tour affected by this move
     */
    public ITRSPTour getTour() {
        return mTour;
    }

    /**
     * Creates a new <code>TRSPMove</code>
     * 
     * @param improvement
     *            the improvement associated with this move
     * @param tour
     *            the tour affected by this move
     */
    public TRSPMove(double improvement, ITRSPTour tour) {
        super(improvement);
        mTour = tour;
    }
}
