package vroom.common.modeling.visualization;

import java.util.Arrays;

import vroom.common.modeling.dataModel.IArc;
import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.utilities.Update;

/**
 * The Class <code>GraphUpdate</code> is used to notify a visualization when the underlying graph has been changed
 * <p>
 * Creation date: Sep 29, 2010 - 10:17:43 AM.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp" >SLP</a>
 * @version 1.0
 */
public class GraphUpdate implements Update {

    /** The arc. */
    private final IArc         arc;

    /** The nodes. */
    private final INodeVisit[] nodes;

    /**
     * Instantiates a new graph update.
     * 
     * @param arc
     *            the arc
     * @param nodes
     *            the nodes
     */
    public GraphUpdate(IArc arc, INodeVisit... nodes) {
        super();
        this.arc = arc;
        this.nodes = nodes;
    }

    /**
     * Checks if is arc update.
     * 
     * @return true, if is arc update
     */
    public boolean isArcUpdate() {
        return arc != null;
    }

    /**
     * Checks if is node update.
     * 
     * @return true, if is node update
     */
    public boolean isNodeUpdate() {
        return nodes.length > 0;
    }

    /**
     * Gets the arc.
     * 
     * @return the arc
     */
    public IArc getArc() {
        return arc;
    }

    /**
     * Gets the nodes.
     * 
     * @return the nodes
     */
    public INodeVisit[] getNodes() {
        return nodes;
    }

    /*
     * (non-Javadoc)
     * @see vroom.common.utilities.Update#getDescription()
     */
    @Override
    public String getDescription() {
        return String.format("%s %s", arc, Arrays.toString(nodes));
    }

}