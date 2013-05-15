/**
 * 
 */
package vroom.trsp.visualization;

import java.awt.Dimension;

import vroom.common.modeling.visualization.DefaultInstanceViewer;
import vroom.common.modeling.visualization.VisualizationFrame;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPTour;

/**
 * <code>TRSPVisualization</code> is a utility class to create visualization for the TRSP.
 * <p>
 * Creation date: Mar 23, 2011 - 1:51:29 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TRSPVisualization {

    /**
     * Show a visualization frame for the given instance;
     * 
     * @param instance
     *            the instance to be displayed
     * @return the visualization frame
     */
    public static VisualizationFrame showVisualizationFrame(TRSPInstance instance) {
        TRSPInstanceGraph graph = new TRSPInstanceGraph(instance);
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
    public static VisualizationFrame showVisualizationFrame(TRSPTour tour) {
        TRSPInstanceGraph graph = new TRSPInstanceGraph(tour.getInstance());
        graph.addTour(tour);
        DefaultInstanceViewer view = new DefaultInstanceViewer(graph);

        VisualizationFrame frame = new VisualizationFrame(tour.getInstance().getName(), view);
        frame.setPreferredSize(new Dimension(400, 400));
        frame.pack();
        frame.setVisible(true);

        return frame;
    }

}
