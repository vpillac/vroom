package vroom.trsp.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import vroom.common.modeling.dataModel.Depot;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.dataModel.IVRPRequest;
import vroom.common.modeling.dataModel.Vehicle;
import vroom.common.modeling.dataModel.attributes.Duration;
import vroom.common.modeling.dataModel.attributes.ITimeWindow;
import vroom.common.modeling.dataModel.attributes.NodeAttributeKey;
import vroom.common.modeling.dataModel.attributes.OpenTimeWindow;
import vroom.common.modeling.dataModel.attributes.RequestAttributeKey;
import vroom.common.modeling.dataModel.attributes.SimpleTimeWindow;
import vroom.common.modeling.dataModel.attributes.VehicleAttributeKey;
import vroom.common.modeling.io.ChristofidesPersistenceHelper;
import vroom.common.modeling.io.IPersistenceHelper;
import vroom.common.utilities.dataModel.ObjectWithIdComparator;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPRequest;
import vroom.trsp.datamodel.Technician;
import vroom.trsp.datamodel.TechnicianFleet;
import vroom.trsp.util.TRSPLogging;

/**
 * <code>ConvertionPersistenceHelper</code>
 * <p>
 * Creation date: Nov 7, 2011 - 3:01:00 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class ConvertionPersistenceHelper implements ITRSPPersistenceHelper {

    private final IPersistenceHelper<File> mHelper;

    /**
     * Creates a new <code>ConvertionPersistenceHelper</code>
     * 
     * @param helper
     */
    public ConvertionPersistenceHelper(IPersistenceHelper<File> helper) {
        mHelper = helper;
    }

    /**
     * Reads an instance from a flat file
     * 
     * @param file
     *            the {@link File} containing the instance to be read
     * @return a {@link TRSPInstance} containing the instance defined in <code>file</code>
     * @throws IOException
     */
    @Override
    public TRSPInstance readInstance(File file, Object... params) throws Exception {
        IVRPInstance vrpIns = mHelper.readInstance(file);

        return convertInstance(vrpIns, params);
    }

    public static TRSPInstance convertInstance(IVRPInstance vrpIns, Object... params) {
        boolean cvrptw = params.length == 0 || (Boolean) params[0];

        int fleetSize = vrpIns.getFleet().isUnlimited() ? vrpIns.getRequestCount() : vrpIns.getFleet().size();

        List<Depot> depots = new ArrayList<Depot>(1 + fleetSize);
        int nodeId = 0;

        Depot mainDepot = vrpIns.getDepot(0);

        ITimeWindow tw = vrpIns.getDepot(0).getAttribute(NodeAttributeKey.TIME_WINDOW);
        if (tw == null) {
            tw = new OpenTimeWindow();
        } else {
            tw = new SimpleTimeWindow(tw.startAsDouble(), tw.endAsDouble());
        }

        mainDepot = new Depot(nodeId++, mainDepot.getLocation());
        mainDepot.setAttribute(NodeAttributeKey.TIME_WINDOW, tw);
        depots.add(mainDepot);

        boolean hasCap = vrpIns.getFleet().getVehicle().getCompartmentCount() > 0;

        // CREW
        List<Technician> crew = new ArrayList<Technician>(fleetSize);
        for (int i = 1; i <= fleetSize; i++) {
            Vehicle v = vrpIns.getFleet().getVehicle(i - 1);
            Depot home = v.getAttribute(VehicleAttributeKey.DEPOT);
            if (home == null) {
                home = new Depot(nodeId++, vrpIns.getDepot(0).getLocation());
                home.setAttribute(NodeAttributeKey.TIME_WINDOW, mainDepot.getTimeWindow());
            } else {
                tw = home.getTimeWindow();
                home = new Depot(nodeId++, home.getLocation());
                if (tw == null) {
                    tw = new OpenTimeWindow();
                } else {
                    tw = new SimpleTimeWindow(tw.startAsDouble(), tw.endAsDouble());
                }
                home.setAttribute(NodeAttributeKey.TIME_WINDOW, tw);
            }

            depots.add(home);

            int[] cap = hasCap ? new int[] { (int) vrpIns.getFleet().getVehicle(i - 1).getCapacity() } : new int[0];
            Technician t = new Technician(i - 1, i + "", v.getFixedCost(), v.getVariableCost(), v.getSpeed(),
                    new int[0], new int[0], cap, home);
            crew.add(t);
        }
        TechnicianFleet fleet = new TechnicianFleet(crew, true);

        // REQUESTS
        List<TRSPRequest> requests = new ArrayList<TRSPRequest>(vrpIns.getRequestCount());
        List<IVRPRequest> originalReq = vrpIns.getRequests();
        Collections.sort(originalReq, new ObjectWithIdComparator());
        if (originalReq.get(0).getID() != 0 && originalReq.get(0).getID() != 1)
            throw new IllegalStateException("The first request should have an id of 0 or 1");
        for (IVRPRequest r : originalReq) {
            tw = r.getAttribute(RequestAttributeKey.TIME_WINDOW);
            if (tw == null)
                tw = new OpenTimeWindow();
            else {
                tw = new SimpleTimeWindow(tw.startAsDouble(), tw.endAsDouble());
            }
            Duration st = r.getAttribute(RequestAttributeKey.SERVICE_TIME);
            int servTime = st != null ? (int) st.getDuration() : 0;

            int[] dem = hasCap ? new int[] { (int) r.getDemand() } : new int[0];
            TRSPRequest req = new TRSPRequest(nodeId++, r.getNode(), new int[0], new int[0], dem, tw, servTime);
            req.setAttribute(RequestAttributeKey.RELEASE_DATE, r.getAttribute(RequestAttributeKey.RELEASE_DATE));

            requests.add(req);
        }

        TRSPInstance instance = new TRSPInstance(vrpIns.getName(), fleet, 0, 0, hasCap ? 1 : 0, depots, requests,
                cvrptw);

        // Roundup distances
        instance.getCostDelegate().setPrecision(vrpIns.getCostDelegate().getPrecision(),
                vrpIns.getCostDelegate().getRoundingMethod());

        return instance;
    }

    public static void main(String[] args) {
        ConvertionPersistenceHelper h = new ConvertionPersistenceHelper(new ChristofidesPersistenceHelper());
        try {
            TRSPInstance ins = h.readInstance(new File("../Instances/cvrp/christodifes-mingozzi-toth/vrpnc1.txt"));
            System.out.println(ins);
        } catch (Exception e) {
            TRSPLogging.getBaseLogger().exception("ConvertionPersistenceHelper.main", e);
        }
    }

    @Override
    public boolean writeInstance(TRSPInstance instance, File file) throws IOException {
        throw new UnsupportedOperationException();
    }
}
