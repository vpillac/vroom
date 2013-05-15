/**
 * 
 */
package vroom.common.heuristics.cw.kernel;

import vroom.common.heuristics.Move;
import vroom.common.heuristics.cw.IJCWArc;
import vroom.common.modeling.dataModel.IRoute;

/**
 * <code>RouteMergingMove</code>
 * <p>
 * Creation date: Jun 28, 2010 - 5:22:19 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class RouteMergingMove extends Move {

    public final IJCWArc linkingArc;
    public final IRoute<?> tailRoute, headRoute;

    /**
     * Creates a new <code>RouteMergingMove</code>
     * 
     * @param linkingArc
     * @param tailRoute
     * @param headRoute
     */
    public RouteMergingMove(IJCWArc linkingArc, IRoute<?> tailRoute, IRoute<?> headRoute) {
        super(linkingArc.getSaving());
        this.linkingArc = linkingArc;
        this.tailRoute = tailRoute;
        this.headRoute = headRoute;
    }

    /* (non-Javadoc)
     * @see vroom.common.heuristics.Move#getMoveName()
     */
    @Override
    public String getMoveName() {
        return "routeMerge";
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("%s(arc:%s)", getMoveName(), linkingArc);
    }

}
