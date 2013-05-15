/**
 *
 */
package vroom.common.modeling.checkers;

import java.io.IOException;

import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.dataModel.IVRPRequest;
import vroom.common.modeling.dataModel.Node;
import vroom.common.modeling.dataModel.attributes.ITimeWindow;
import vroom.common.modeling.dataModel.attributes.RequestAttributeKey;

/**
 * <code>SolomonSolutionChecker</code> is a class that will independently check a solution of a Solomon instance.
 * <p>
 * Creation date: Sep 6, 2011 - 4:38:04 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class SolomonSolutionChecker {

    public static String checkSolution(int[] solution, IVRPInstance instance) throws IOException {
        System.out.println("Cost delegate:");
        System.out.println(instance.getCostDelegate());

        StringBuilder err = new StringBuilder();

        double cap = instance.getFleet().getVehicle().getCapacity();
        double time = instance.getDepot(0).getTimeWindow().startAsDouble();
        double distance = 0;

        Node pred = null, node = null;
        for (int n : solution) {
            if (n == 0) {
                node = instance.getDepot(0);
                if (pred != null)
                    distance += instance.getCostDelegate().getDistance(pred, node);
                cap = instance.getFleet().getVehicle().getCapacity();
                time = instance.getDepot(0).getTimeWindow().startAsDouble();
            } else {
                IVRPRequest req = instance.getRequest(n);
                node = req.getNode();
                cap -= req.getDemand();
                time += instance.getCostDelegate().getDistance(pred, node);
                distance += instance.getCostDelegate().getDistance(pred, node);

                ITimeWindow tw = req.getAttribute(RequestAttributeKey.TIME_WINDOW);
                time = tw.getEarliestStartOfService(time);
                if (cap < 0) {
                    err.append(String.format(" Cap at node %s (%s) ", n, cap));
                }

                if (time > tw.endAsDouble()) {
                    err.append(String.format(" TW at node %s (%s-[%s,%s]) ", n, time, tw.startAsDouble(),
                            tw.endAsDouble()));
                }
                time += req.getAttribute(RequestAttributeKey.SERVICE_TIME).getDuration();
            }
            pred = node;

        }

        System.out.println("Total distance: " + distance);

        return "";
    }

}
