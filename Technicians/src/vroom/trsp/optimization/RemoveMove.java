/**
 * 
 */
package vroom.trsp.optimization;

import vroom.trsp.datamodel.TRSPTour;

/**
 * <code>RemoveMove</code> is a representation of the removal of a request in a {@linkplain TRSPTour tour}.
 * <p>
 * Creation date: Jun 10, 2011 - 10:07:57 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class RemoveMove extends TRSPMove {

    private final int mRequest;

    public RemoveMove(int request, TRSPTour tour) {
        super(Double.NEGATIVE_INFINITY, tour);
        mRequest = request;
    }

    /* (non-Javadoc)
     * @see vroom.common.heuristics.Move#getMoveName()
     */
    @Override
    public String getMoveName() {
        return "rem";
    }

    @Override
    public String toString() {
        return String.format("rem(%s-%s,%.3f)", getNodeId(), getTour().getTechnicianId(), getImprovement());
    }

    public int getNodeId() {
        return mRequest;
    }

}
