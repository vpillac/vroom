package vroom.common.modeling.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Collections;

import vroom.common.modeling.dataModel.Depot;
import vroom.common.modeling.dataModel.Fleet;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.dataModel.Node;
import vroom.common.modeling.dataModel.Request;
import vroom.common.modeling.dataModel.StaticInstance;
import vroom.common.modeling.dataModel.Vehicle;
import vroom.common.modeling.dataModel.VehicleRoutingProblemDefinition;
import vroom.common.modeling.dataModel.attributes.Duration;
import vroom.common.modeling.dataModel.attributes.NodeAttributeKey;
import vroom.common.modeling.dataModel.attributes.PointLocation;
import vroom.common.modeling.dataModel.attributes.RequestAttributeKey;
import vroom.common.modeling.dataModel.attributes.SimpleTimeWindow;
import vroom.common.modeling.util.BufferedDistance;
import vroom.common.modeling.util.EuclidianDistance;
import vroom.common.utilities.dataModel.IDHelper;

/**
 * The Class <code>ChristofidesPersistenceHelper</code>
 * <p>
 * Creation date: Aug 23, 2010 - 2:20:49 PM.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class ChristofidesPersistenceHelper extends FlatFilePersistenceHelper {

    private static final IDHelper sIdHelper = new IDHelper();
    private IDHelper              mNodeIdHelper;

    private int                   mDropTime;

    @Override
    protected void finalizeInstance(IVRPInstance instance, Object... params) {

    }

    @Override
    protected void parseLine(IVRPInstance instance, String line, int lineNumber, Object... params) {
        String[] node = line.split("\\s");

        int x = Integer.parseInt(node[0]);
        int y = Integer.parseInt(node[1]);
        int d = Integer.parseInt(node[2]);
        int id = mNodeIdHelper.nextId();

        Request req = new Request(id, new Node(id, new PointLocation(x, y)));
        req.setDemands(d);
        req.setAttribute(RequestAttributeKey.SERVICE_TIME, new Duration(mDropTime));

        instance.addRequest(req);

    }

    @Override
    public StaticInstance readInstance(File input, Object... params) throws IOException {
        return (StaticInstance) super.readInstance(input, params);
    }

    @Override
    protected StaticInstance initializeInstance(File input, BufferedReader reader, Object... params)
            throws IOException {
        mNodeIdHelper = new IDHelper();

        String[] firstLine = reader.readLine().replaceFirst("\\s", "").split("\\s");
        String[] secondLine = reader.readLine().replaceFirst("\\s", "").split("\\s");

        // int size = Integer.parseInt(firstLine[0]);
        int cap = Integer.parseInt(firstLine[1]);
        int maxRouteTime = Integer.parseInt(firstLine[2]);
        mDropTime = Integer.parseInt(firstLine[3]);

        int dx = Integer.parseInt(secondLine[0]);
        int dy = Integer.parseInt(secondLine[1]);

        StaticInstance instance = new StaticInstance(input.getName().replace(".txt", ""),
                sIdHelper.nextId(), VehicleRoutingProblemDefinition.VRP);

        instance.setFleet(Fleet.newUnlimitedFleet(new Vehicle(0, "v", cap)));

        Depot depot = new Depot(mNodeIdHelper.nextId(), "depot", new PointLocation(dx, dy));
        if (maxRouteTime < 999999)
            depot.setAttribute(NodeAttributeKey.TIME_WINDOW, new SimpleTimeWindow(0, maxRouteTime));

        instance.setDepots(Collections.singletonList(depot));

        instance.setCostHelper(new BufferedDistance(new EuclidianDistance()));

        return instance;
    }
}
