/**
 * 
 */
package vroom.trsp.optimization.rch;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import javax.swing.JFrame;

import vroom.common.modeling.visualization.DefaultInstanceViewer;
import vroom.common.modeling.visualization.VisualizationFrame;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.trsp.RCHSCSolver;
import vroom.trsp.datamodel.ITRSPTour;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPRequest;
import vroom.trsp.datamodel.TRSPSolutionChecker;
import vroom.trsp.instances.RndInstanceGenerator;
import vroom.trsp.optimization.split.SplitTourArcBuilder;
import vroom.trsp.optimization.split.TRSPSplit;
import vroom.trsp.util.TRSPGlobalParameters;
import vroom.trsp.util.TRSPLogging;
import vroom.trsp.visualization.TRSPInstanceGraph;

/**
 * <code>TRSPSplitVisualization</code>
 * <p>
 * Creation date: Oct 4, 2011 - 12:57:56 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TRSPSplitVisualization {

    /**
     * JAVADOC
     * 
     * @param args
     */
    public static void main(String[] args) {
        LoggerHelper.setupRootLogger(LoggerHelper.LEVEL_DEBUG, LoggerHelper.LEVEL_DEBUG, false);
        // TRSPTestRCHSC.setupGlobalParameters();
        TRSPGlobalParameters params = null;

        // TRSPInstance instance = TRSPTestRCHSC.readInstance();

        TRSPInstance instance = RndInstanceGenerator.generateCircularVRPInstance("circ-10", 10, 3);
        params.set(TRSPGlobalParameters.RUN_CVRPTW, true);

        RCHSCSolver solver = new RCHSCSolver(instance, params);
        // Iterator<IConstraint<ITRSPTour>> it = solver.getCtrHandler().iterator();
        // while (it.hasNext()) {
        // if (HomeConstraint.class.isAssignableFrom(it.next().getClass()))
        // it.remove();
        // }

        System.out.println(solver.getTourCtrHandler());

        testSplit(instance, params, solver);

        // testFeasibleTour(instance, params, solver);

    }

    public static void testSplit(TRSPInstance instance, TRSPGlobalParameters params,
            RCHSCSolver solver) {
        ArrayList<TRSPRndConstructiveHeuristic> heuristics = new ArrayList<TRSPRndConstructiveHeuristic>(
                3);

        int kMax = 10;

        // RNN
        // heuristics
        // .add(new RndNearestNeighbor(instance, params, solver.getCtrHandler(), solver.getCostDelegate(), kMax));
        //
        // // RFI
        // heuristics.add(new RndNearestFurthestIns(instance, params, solver.getCtrHandler(), solver.getCostDelegate(),
        // kMax, true));
        //
        // // RNI
        // heuristics.add(new RndNearestFurthestIns(instance, params, solver.getCtrHandler(), solver.getCostDelegate(),
        // kMax, false));

        // RBI
        // heuristics.add(new RndBestIns(instance, params, solver.getCtrHandler(), solver.getCostDelegate(), kMax));

        heuristics.add(new RndClarkeWright(instance, params, solver.getTourCtrHandler(), solver
                .getCostDelegate(), kMax));

        TRSPSplit split = new TRSPSplit(new SplitTourArcBuilder(solver.getTourCtrHandler(),
                solver.getCostDelegate(), params));

        for (TRSPRndConstructiveHeuristic heur : heuristics) {
            TRSPLogging.getBaseLogger().info(
                    "TRSPSplitVisualization.testSplit: First Call -----------------");
            heur.generateGiantTour(instance.getTechnician(0));
            TRSPLogging.getBaseLogger().info(
                    "TRSPSplitVisualization.testSplit: Second Call -----------------");
            heur.generateGiantTour(instance.getTechnician(0));
            TRSPLogging.getBaseLogger().info(
                    "TRSPSplitVisualization.testSplit: Third Call -----------------");
            heur.generateGiantTour(instance.getTechnician(0));
            TRSPLogging.getBaseLogger().info(
                    "TRSPSplitVisualization.testSplit: Fourth Call -----------------");
            heur.generateGiantTour(instance.getTechnician(0));
            TRSPLogging.getBaseLogger().info(
                    "TRSPSplitVisualization.testSplit: Fifth Call -----------------");
            Collection<ITRSPTour> giantTours = heur.generateGiantTour(instance.getTechnician(0));
            Collection<ITRSPTour> tours = new LinkedList<ITRSPTour>();
            for (ITRSPTour giantTour : giantTours) {
                tours.addAll(split.splitTour(giantTour));
            }
            display(instance, giantTours, tours, heur.toString(), false);
        }
    }

    public static void testFeasibleTour(TRSPInstance instance, TRSPGlobalParameters params,
            RCHSCSolver solver) {

        RndNearestNeighbor rnn = new RndNearestNeighbor(instance, params,
                solver.getTourCtrHandler(), solver.getCostDelegate(), 6);
        Collection<ITRSPTour> tours = new LinkedList<ITRSPTour>();
        tours.add(rnn.generateFeasibleTour(instance.getFleet().getVehicle()));
        tours.add(rnn.generateFeasibleTour(instance.getFleet().getVehicle()));
        tours.add(rnn.generateFeasibleTour(instance.getFleet().getVehicle()));
        tours.add(rnn.generateFeasibleTour(instance.getFleet().getVehicle()));
        display(instance, null, tours, "", true);
    }

    public static void display(TRSPInstance instance, Collection<ITRSPTour> giantTours,
            Collection<ITRSPTour> tours, String comment, boolean allTours) {
        HashSet<Integer> reqs = new HashSet<Integer>();
        for (TRSPRequest r : instance.getRequests()) {
            reqs.add(r.getID());
        }
        TRSPInstanceGraph graph = new TRSPInstanceGraph(instance);

        if (giantTours != null) {
            for (ITRSPTour giantTour : giantTours) {
                for (Integer r : giantTour)
                    reqs.remove(r);
                graph.addTour(giantTour);
                System.out.println("Giant tour: " + giantTour);
            }
        }

        double cost = 0;
        for (ITRSPTour tour : tours) {
            if (allTours)
                graph.addTour(tour);
            System.out.println(" " + tour + " - " + TRSPSolutionChecker.INSTANCE.checkTour(tour));
            cost += tour.getTotalCost();
        }
        System.out.println("Unserved requests: " + reqs);
        System.out.println("Total cost: " + cost);

        DefaultInstanceViewer view = new DefaultInstanceViewer(graph);

        VisualizationFrame frame = new VisualizationFrame(String.format("%s (%s) uns:%s cost:%.2f",
                comment, instance.getName(), reqs, cost), view);
        frame.setPreferredSize(new Dimension(400, 400));
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

}
