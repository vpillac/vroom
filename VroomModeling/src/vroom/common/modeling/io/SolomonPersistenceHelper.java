/**
 *
 */
package vroom.common.modeling.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.List;

import vroom.common.modeling.dataModel.Depot;
import vroom.common.modeling.dataModel.DistanceMatrix;
import vroom.common.modeling.dataModel.Fleet;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.dataModel.Node;
import vroom.common.modeling.dataModel.Request;
import vroom.common.modeling.dataModel.StaticInstance;
import vroom.common.modeling.dataModel.Vehicle;
import vroom.common.modeling.dataModel.VehicleRoutingProblemDefinition;
import vroom.common.modeling.dataModel.attributes.DeterministicDemand;
import vroom.common.modeling.dataModel.attributes.Duration;
import vroom.common.modeling.dataModel.attributes.NodeAttributeKey;
import vroom.common.modeling.dataModel.attributes.PointLocation;
import vroom.common.modeling.dataModel.attributes.RequestAttributeKey;
import vroom.common.modeling.dataModel.attributes.SimpleTimeWindow;
import vroom.common.modeling.util.EuclidianDistance;
import vroom.common.utilities.dataModel.IDHelper;

/**
 * <code>SolomonPersistenceHelper</code> is an extension of {@link FlatFilePersistenceHelper} for the parsing of the
 * Solomon instances in the format used in <a href="http://www2.imm.dtu.dk/~jla/solomon.html">http://www2
 * .imm.dtu.dk/~jla/solomon.html</a>
 * <p>
 * Creation date: Jun 28, 2010 - 4:13:26 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class SolomonPersistenceHelper extends FlatFilePersistenceHelper {

    private int                   mSize;

    private final static IDHelper ID_HELPER = new IDHelper();

    private final int             mPrecision;

    /**
     * Creates a new <code>SolomonPersistenceHelper</code>
     * 
     * @param precision
     *            the precision used for distance truncation, default is 1, a negative value means no truncating
     */
    public SolomonPersistenceHelper(int precision) {
        mPrecision = precision;
    }

    /**
     * Creates a new <code>SolomonPersistenceHelper</code>
     */
    public SolomonPersistenceHelper() {
        this(-1);
    }

    /**
     * Loading of an instance.
     * 
     * @param input
     *            the input file
     * @param params
     *            (Integer) the size of the desired instance (will truncate the instance to the <code>size</code> first
     *            customers)
     * @return the loaded instance
     */
    @Override
    public IVRPInstance readInstance(File input, Object... params) throws IOException {

        if (params.length > 0 && params[0] instanceof Integer) {
            mSize = (Integer) params[0];

            if (mSize < 1 || mSize > 100) {
                throw new IllegalArgumentException("Size must be between 1 and 100");
            }
        } else if (mSize == 0) {
            mSize = 100;
        }

        return super.readInstance(input, params);
    }

    /*
     * (non-Javadoc)
     * @see vroom.common.modeling.io.FlatFilePersistenceHelper#finalizeInstance(
     * vroom.common.modeling.dataModel.IVRPInstance, java.lang.Object)
     */
    @Override
    protected void finalizeInstance(IVRPInstance instance, Object... params) {
        DistanceMatrix ch = new DistanceMatrix(instance);
        // Truncate distances
        if (mPrecision >= 0)
            ch.setPrecision(mPrecision, RoundingMode.FLOOR);
        instance.setCostHelper(ch);
    }

    /*
     * (non-Javadoc)
     * @see vroom.common.modeling.io.FlatFilePersistenceHelper#initializeInstance (java.io.File,
     * java.io.BufferedReader, java.lang.Object)
     */
    @Override
    protected IVRPInstance initializeInstance(File input, BufferedReader reader, Object... params)
            throws IOException {

        // Name in first line
        String name = reader.readLine();
        // Blank line
        reader.readLine();
        // VEHICLE line
        reader.readLine();
        // Header line
        reader.readLine();
        // Number Capacity
        String[] fleetDef = reader.readLine().split("\\s+");
        int fleetSize = Integer.valueOf(fleetDef[1]);
        double capacity = Double.valueOf(fleetDef[2]);
        // Blank line
        reader.readLine();
        // CUSTOMER line
        reader.readLine();
        // Header line
        reader.readLine();
        // Blank line
        reader.readLine();
        // Depot
        String[] depotDef = reader.readLine().split("\\s+");
        Depot depot = new Depot(0, new PointLocation(Double.valueOf(depotDef[2]),
                Double.valueOf(depotDef[3])));
        depot.setAttribute(NodeAttributeKey.TIME_WINDOW,
                new SimpleTimeWindow(Long.valueOf(depotDef[5]), Long.valueOf(depotDef[6]), false,
                        false));
        // DataSection

        IVRPInstance instance = new StaticInstance(name, ID_HELPER.nextId(),
                VehicleRoutingProblemDefinition.CVRPTW);

        instance.setFleet(Fleet.newHomogenousFleet(fleetSize, new Vehicle(0, "Vehicle", capacity)));

        EuclidianDistance dh = new EuclidianDistance();
        dh.setPrecision(mPrecision, RoundingMode.HALF_EVEN);
        instance.setCostHelper(dh);

        instance.setSymmetric(true);

        List<Depot> depots = new LinkedList<Depot>();
        depots.add(depot);
        instance.setDepots(depots);

        return instance;
    }

    /*
     * (non-Javadoc)
     * @see vroom.common.modeling.io.FlatFilePersistenceHelper#processLine(vroom
     * .common.modelling.dataModel.IVRPInstance, java.lang.String, int, java.lang.Object)
     */
    @Override
    protected void parseLine(IVRPInstance instance, String line, int lineNumber, Object... params) {
        if (line == null || line.length() == 0)
            return;
        String[] custDef = line.split("\\s+");

        int id = Integer.valueOf(custDef[0]);
        double x = Double.valueOf(custDef[1]);
        double y = Double.valueOf(custDef[2]);
        int d = Integer.valueOf(custDef[3]);
        int ltw = Integer.valueOf(custDef[4]);
        int utw = Integer.valueOf(custDef[5]);
        int st = Integer.valueOf(custDef[6]);

        Request r = new Request(id, new Node(id, new PointLocation(x, y)));

        r.setAttribute(RequestAttributeKey.DEMAND, new DeterministicDemand(d));

        r.setAttribute(RequestAttributeKey.TIME_WINDOW, new SimpleTimeWindow(ltw, utw));

        r.setAttribute(RequestAttributeKey.SERVICE_TIME, new Duration(st));

        instance.addRequest(r);

    }

}
