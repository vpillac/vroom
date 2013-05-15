/**
 * 
 */
package vroom.common.heuristics.cw;

import vroom.common.modeling.dataModel.Arc;
import vroom.common.modeling.dataModel.IArc;
import vroom.common.modeling.dataModel.INodeVisit;

/**
 * <code>JCWRouteArc</code> is a specialization of {@link Arc} that includes a saving value
 * <p>
 * Creation date: Apr 28, 2010 - 4:36:43 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class JCWArc implements IJCWArc, IArc {

    /** the wrapped arc **/
    private final IArc   mArc;

    /** the saving associated with this arc **/
    private final double mSaving;

    /**
     * Creates a new <code>JCWRouteArc</code>
     * 
     * @param currentArc
     */
    public JCWArc(IArc arc, double saving) {
        mArc = arc;
        mSaving = saving;
    }

    /* (non-Javadoc)
     * @see vroom.common.heuristics.jcw.IJCWArc#compareTo(vroom.common.heuristics.jcw.IJCWArc)
     */
    @Override
    public int compareTo(IJCWArc anotherArc) {
        int comp = Double.compare(anotherArc.getSaving(), this.getSaving());
        if (comp == 0 && anotherArc instanceof JCWArc) {
            comp = ((JCWArc) anotherArc).mArc.getTailNode().getID() - mArc.getTailNode().getID();
            if (comp == 0)
                comp = ((JCWArc) anotherArc).mArc.getHeadNode().getID()
                        - mArc.getHeadNode().getID();
        }
        return comp;
    }

    /* (non-Javadoc)
     * @see vroom.common.heuristics.jcw.IJCWArc#getHeadNode()
     */
    @Override
    public INodeVisit getHeadNode() {
        return mArc.getHeadNode();
    }

    /* (non-Javadoc)
     * @see vroom.common.heuristics.jcw.IJCWArc#getSaving()
     */
    @Override
    public double getSaving() {
        return mSaving;
    }

    /* (non-Javadoc)
     * @see vroom.common.heuristics.jcw.IJCWArc#getTailNode()
     */
    @Override
    public INodeVisit getTailNode() {
        return mArc.getTailNode();
    }

    /* (non-Javadoc)
     * @see vroom.common.modeling.dataModel.IArc#getDistance()
     */
    @Override
    public double getDistance() {
        return mArc.getDistance();
    }

    /* (non-Javadoc)
     * @see vroom.common.modeling.dataModel.IArc#isDirected()
     */
    @Override
    public boolean isDirected() {
        return mArc.isDirected();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("(%s,%s)=%.2f", mArc.getTailNode().getID(),
                mArc.getHeadNode().getID(), getSaving());
    }

}
