/**
 * 
 */
package vroom.trsp.instances;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JFrame;

import vroom.common.modeling.dataModel.Depot;
import vroom.common.modeling.dataModel.Node;
import vroom.common.modeling.dataModel.attributes.NodeAttributeKey;
import vroom.common.modeling.dataModel.attributes.PointLocation;
import vroom.common.modeling.dataModel.attributes.SimpleTimeWindow;
import vroom.common.modeling.visualization.DefaultInstanceViewer;
import vroom.common.modeling.visualization.VisualizationFrame;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPRequest;
import vroom.trsp.datamodel.Technician;
import vroom.trsp.visualization.TRSPInstanceGraph;

/**
 * <code>RndInstanceGenerator</code> is class with factory methods to generate random instances
 * <p>
 * Creation date: Oct 19, 2011 - 2:20:46 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class RndInstanceGenerator {

    private static final double mAreaSize = 100;

    /**
     * JAVADOC
     * 
     * @param size
     *            the number of requests to generate
     * @param cap
     *            the capacity of the vehicle
     * @return
     */
    public static TRSPInstance generateCircularVRPInstance(String name, int size, int cap) {
        double inc = 2 * Math.PI / size;
        SimpleTimeWindow tw = new SimpleTimeWindow(0, 1800);

        // Main depot
        Depot mainDepot = new Depot(TRSPInstance.MAIN_DEPOT, new PointLocation(mAreaSize / 2, mAreaSize / 2));
        mainDepot.setAttribute(NodeAttributeKey.TIME_WINDOW, tw);

        // Crew
        Depot home = new Depot(1, new PointLocation(mAreaSize / 2 + 5, mAreaSize / 2 + 5));
        home.setAttribute(NodeAttributeKey.TIME_WINDOW, tw);
        Technician tech = new Technician(0, "tech", 0, 1, 1, new int[0], new int[0], new int[] { cap }, home);

        ArrayList<Depot> depots = new ArrayList<Depot>(2);
        depots.add(mainDepot);
        depots.add(home);

        // Requests
        List<TRSPRequest> requests = new ArrayList<TRSPRequest>(size);
        double theta = 0;
        for (int id = 2; id <= size + 1; id++) {
            double x = mAreaSize / 2 * (1 + Math.cos(theta));
            double y = mAreaSize / 2 * (1 + Math.sin(theta));
            Node node = new Node(id, new PointLocation(x, y));
            requests.add(new TRSPRequest(id, node, new int[0], new int[0], new int[] { 1 }, tw, 1));
            theta += inc;
        }

        TRSPInstance instance = new TRSPInstance(name, Collections.singletonList(tech), 0, 0, 1, depots, requests, true);

        return instance;
    }

    public static void main(String[] args) {

        TRSPInstanceGraph graph = new TRSPInstanceGraph(generateCircularVRPInstance("circ-10", 10, 2));

        DefaultInstanceViewer view = new DefaultInstanceViewer(graph);
        VisualizationFrame frame = new VisualizationFrame("test", view);
        frame.setPreferredSize(new Dimension(400, 400));
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
