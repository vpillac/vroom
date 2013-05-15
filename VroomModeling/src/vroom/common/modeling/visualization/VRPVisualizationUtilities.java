/**
 * 
 */
package vroom.common.modeling.visualization;

import java.awt.Dimension;

import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.dataModel.IVRPSolution;

/**
 * <code>VRPVisualizationUtilities</code> is a utility class to represent instances and solutions.
 * <p>
 * Creation date: Sep 29, 2010 - 10:34:45 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class VRPVisualizationUtilities {

    /**
     * Show a visualization frame for the given instance;
     * 
     * @param instance
     *            the instance to be displayed
     * @return the visualization frame
     */
    public static VisualizationFrame showVisualizationFrame(IVRPInstance instance) {
        DefaultInstanceGraph graph = new DefaultInstanceGraph(instance);
        DefaultInstanceViewer view = new DefaultInstanceViewer(graph);

        VisualizationFrame frame = new VisualizationFrame(instance.getName(), view);
        frame.setPreferredSize(new Dimension(400, 400));
        frame.pack();
        frame.setVisible(true);

        return frame;
    }

    /**
     * Show a visualization frame for the given solution;
     * 
     * @param solution
     *            the solution to be displayed
     * @return the visualization frame
     */
    public static VisualizationFrame showVisualizationFrame(IVRPSolution<?> solution) {
        DefaultInstanceGraph graph = new DefaultInstanceGraph(solution.getParentInstance());
        graph.addSolution(solution);
        DefaultInstanceViewer view = new DefaultInstanceViewer(graph);

        VisualizationFrame frame = new VisualizationFrame(solution.getParentInstance().getName(), view);
        frame.setPreferredSize(new Dimension(400, 400));
        frame.pack();
        frame.setVisible(true);

        return frame;
    }

}
