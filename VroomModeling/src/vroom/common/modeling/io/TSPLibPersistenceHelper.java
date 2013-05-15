/**
 * 
 */
package vroom.common.modeling.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import vroom.common.modeling.dataModel.Depot;
import vroom.common.modeling.dataModel.DistanceMatrix;
import vroom.common.modeling.dataModel.Fleet;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.dataModel.IVRPRequest;
import vroom.common.modeling.dataModel.Node;
import vroom.common.modeling.dataModel.Request;
import vroom.common.modeling.dataModel.StaticInstance;
import vroom.common.modeling.dataModel.Vehicle;
import vroom.common.modeling.dataModel.VehicleRoutingProblemDefinition;
import vroom.common.modeling.dataModel.attributes.PointLocation;
import vroom.common.modeling.util.BufferedDistance;
import vroom.common.modeling.util.CostCalculationDelegate;
import vroom.common.modeling.util.EuclidianDistance;
import vroom.common.utilities.dataModel.IDHelper;
import vroom.common.utilities.dataModel.ObjectWithIdComparator;

/**
 * <code>TSPLibPersistenceHelper</code> is a specialization of {@link FlatFilePersistenceHelper} for instances written
 * in the extended TSPLib format (<a href="http://neo.lcc.uma.es/radi-aeb/WebVRP/data/Doc.ps">details</a>).
 * <p>
 * Creation date: Jul 6, 2010 - 6:16:13 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TSPLibPersistenceHelper extends FlatFilePersistenceHelper {

    static {
        Locale.setDefault(Locale.US);
    }

    /** a scale factor to convert fractional values to integer **/
    private int mFracScale = 1;

    /**
     * Getter for fracScale : a scale factor to convert fractional values to integer
     * 
     * @return the value of fracScale
     */
    public int getCoordinatesScaleFactor() {
        return mFracScale;
    }

    /**
     * Setter for fracScale : a scale factor to convert fractional values to integer
     * 
     * @param fracScale
     *            the value to be set for fracScale
     */
    public void setCoordinatesScaleFactor(int fracScale) {
        mFracScale = fracScale;
    }

    /** a scale factor to convert fractional demands to integers **/
    private int mDemandsScaleFactor = 1;

    /**
     * Getter for a scale factor to convert fractional demands to integers
     * 
     * @return the value of demScale
     */
    public int getDemandsScaleFactor() {
        return this.mDemandsScaleFactor;
    }

    /**
     * Setter for a scale factor to convert fractional demands to integers
     * 
     * @param demScale
     *            the value to be set for a scale factor to convert fractional demands to integers
     */
    public void setDemandsScaleFactor(int demScale) {
        this.mDemandsScaleFactor = demScale;
    }

    private static final IDHelper sIdHelper = new IDHelper();

    public static enum EDGE_WEIGHT_FORMAT {
        LOWER_ROW
    }

    public static enum Specifications {
        NAME, TYPE, COMMENT, DIMENSION, CAPACITY, EDGE_WEIGHT_TYPE, EDGE_WEIGHT_FORMAT, EDGE_DATA_FORMAT, NODE_COORD_TYPE, DISPLAY_DATA_TYPE
    };

    public static enum DataSection {
        NODE_COORD_SECTION, DEPOT_SECTION, DEMAND_SECTION, EDGE_DATA_SECTION, FIXED_EDGE_SECTION, DISPLAY_DATA_SECTION, TOUR_SECTION, EDGE_WEIGHT_SECTION, EOF
    };

    private List<Depot>               mDepots;
    private Map<Integer, IVRPRequest> mRequests;

    private DataSection               mCurrentSection;

    /*
     * (non-Javadoc)
     * 
     * @see
     * vroom.common.modeling.io.FlatFilePersistenceHelper#finalizeInstance(vroom
     * .modelling.VroomModelling.dataModel.IVRPInstance, java.lang.Object)
     */
    @Override
    protected void finalizeInstance(IVRPInstance instance, Object... params) {
        instance.setDepots(mDepots);
        instance.addRequests(mRequests.values());

        instance.setCostHelper(new BufferedDistance(instance.getCostDelegate()));
    }

    private int                mEdgeI = 0;
    private int                mEdgeJ = 0;
    private EDGE_WEIGHT_FORMAT mEdgeWeightFormat;

    /*
     * (non-Javadoc)
     * 
     * @see
     * vroom.common.modeling.io.FlatFilePersistenceHelper#processLine(vroom.modelling
     * .VroomModelling.dataModel.IVRPInstance, java.lang.String, int, java.lang.Object)
     */
    @Override
    protected void parseLine(IVRPInstance instance, String line, int lineNumber, Object... params) {
        boolean ignore = false;
        try {
            mCurrentSection = DataSection.valueOf(line.replace(" ", ""));
            ignore = true;
        } catch (IllegalArgumentException e) {
            // DO NOTHING
            ignore = false;
        }

        if (!ignore) {
            if (line.startsWith(" ")) {
                line = line.replaceFirst("\\s", "");
            }
            String[] values = line.split("\\s+");

            switch (mCurrentSection) {
            case EDGE_WEIGHT_SECTION:
                double[] edgesWeights = new double[values.length];
                for (int i = 0; i < values.length; i++) {
                    edgesWeights[i] = Double.valueOf(values[i]);
                }
                setEdgeWeights(instance, edgesWeights);
                break;
            case NODE_COORD_SECTION:
                int id = Integer.valueOf(values[0]) - 1;
                double x = Double.valueOf(values[1]) / getCoordinatesScaleFactor();
                double y = Double.valueOf(values[2]) / getCoordinatesScaleFactor();

                IVRPRequest req = new Request(id, new Node(id, new PointLocation(x, y)));
                mRequests.put(id, req);

                break;
            case DEMAND_SECTION:
                id = Integer.valueOf(values[0]) - 1;
                double dem = Double.valueOf(values[1]);

                req = mRequests.get(id);
                if (req == null) {
                    req = new Request(id, new Node(id, new PointLocation(Double.NaN, Double.NaN)));
                    mRequests.put(id, req);
                }
                req.setDemands(dem);

                break;
            case DEPOT_SECTION:
                id = Integer.valueOf(values[0]) - 1;

                if (id >= 0) {
                    req = mRequests.get(id);
                    mRequests.remove(id);

                    Depot depot = new Depot(id, req.getNode().getLocation());
                    mDepots.add(depot);
                }

                break;
            case EOF:
                break;
            default:
                throw new IllegalArgumentException("Unsuported section: " + mCurrentSection);
            }
        }
    }

    private void setEdgeWeights(IVRPInstance instance, double[] edgesWeights) {
        boolean symmetrical = false;
        for (int i = 0; i < edgesWeights.length; i++) {
            switch (mEdgeWeightFormat) {
            case LOWER_ROW:
                if (mEdgeI == mEdgeJ) {
                    mEdgeI++;
                    mEdgeJ = 0;
                }
                symmetrical = true;
                break;
            default:
                throw new UnsupportedOperationException("Unsupported Edge Weght Format "
                        + mEdgeWeightFormat);
            }

            ((DistanceMatrix) instance.getCostDelegate()).setDistance(mEdgeI, mEdgeJ,
                    edgesWeights[i]);
            if (symmetrical)
                ((DistanceMatrix) instance.getCostDelegate()).setDistance(mEdgeJ, mEdgeI,
                        edgesWeights[i]);
            switch (mEdgeWeightFormat) {
            case LOWER_ROW:
                mEdgeJ++;
                break;
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * vroom.common.modeling.io.FlatFilePersistenceHelper#initializeInstance(
     * java.io.File, java.io.BufferedReader, java.lang.Object)
     */
    @Override
    protected IVRPInstance initializeInstance(File input, BufferedReader reader, Object... params)
            throws IOException {

        String name = "NA";
        double capacity = 0;
        VehicleRoutingProblemDefinition vrpDef = null;
        CostCalculationDelegate cd = null;

        String[] line;
        String key = null, value = null;

        // Guess the fleet size from the file name
        int fleetSize = Integer.valueOf(input.getName().substring(
                input.getName().lastIndexOf("k") + 1, input.getName().lastIndexOf(".")));
        boolean endSpec = false;
        int dimension = -1;
        while (!endSpec) {
            line = reader.readLine().split(":");
            key = line[0].replaceAll(" ", "");

            if (line.length > 1) {
                value = line[1].replaceFirst("\\s", "");
            }

            Specifications spec = null;
            try {
                spec = Specifications.valueOf(key);
            } catch (IllegalArgumentException e) {
                endSpec = true;
                continue;
            }

            switch (spec) {
            case NAME:
                name = value;
                break;
            case DIMENSION:
                dimension = Integer.valueOf(value);
                break;
            case CAPACITY:
                capacity = Double.valueOf(value);
                break;
            case TYPE:
                // TODO select appropriate def
                vrpDef = VehicleRoutingProblemDefinition.VRP;
                break;
            case EDGE_WEIGHT_TYPE:
                value = value.replaceAll(" ", "");
                if (value.contains("EUC_2D") || value.contains("EUC_3D")) {
                    cd = new EuclidianDistance();
                    cd.setPrecision(0, RoundingMode.HALF_EVEN);
                } else if (value.contains("CEIL_2D")) {
                    cd = new EuclidianDistance();
                    cd.setPrecision(0, RoundingMode.CEILING);
                } else if (value.contains("EXPLICIT")) {
                    cd = new DistanceMatrix(dimension);
                    cd.setPrecision(Integer.MAX_VALUE, RoundingMode.UNNECESSARY);
                } else {
                    // TODO add specific cost delegate for other distances
                    throw new IllegalArgumentException("Unsupported edge weight type: " + value);
                }
                break;
            case EDGE_WEIGHT_FORMAT:
                value = value.replaceAll("\\s+", "");
                mEdgeWeightFormat = EDGE_WEIGHT_FORMAT.valueOf(value);
            default:
                break;
            }
        }

        StaticInstance instance = new StaticInstance(name, sIdHelper.nextId(), vrpDef);

        instance.setFleet(Fleet.newHomogenousFleet(fleetSize, new Vehicle(0, "Vehicle", capacity)));
        instance.setCostHelper(cd);

        mDepots = new LinkedList<Depot>();
        mRequests = new HashMap<Integer, IVRPRequest>();

        mEdgeI = 0;
        mEdgeJ = 0;

        try {
            mCurrentSection = DataSection.valueOf(key);
        } catch (IllegalArgumentException e) {
            endSpec = true;
        }

        return instance;
    }

    /**
     * Calculate a scaling factor to convert double coordinates into integer coordinates
     * 
     * @param instance
     * @param precision
     * @return a scaling factor that can convert all coordinates into integer values with the given precision
     */
    public static int calculateCoordFracScale(IVRPInstance instance, double precision) {
        int frac = 1;
        for (int d = 0; d < instance.getDepotCount(); d++) {
            Depot dep = instance.getDepot(d);

            double x = dep.getLocation().getX();
            double y = dep.getLocation().getY();

            while (Math.abs(x * frac - Math.round(x * frac)) > precision
                    || Math.abs(y * frac - Math.round(y * frac)) > precision) {
                frac *= 10;
            }
        }

        for (IVRPRequest r : instance.getRequests()) {
            double x = r.getNode().getLocation().getX();
            double y = r.getNode().getLocation().getY();

            while (Math.abs(x * frac - Math.round(x * frac)) > precision * frac
                    || Math.abs(y * frac - Math.round(y * frac)) > precision * frac) {
                frac *= 10;
            }
        }
        return frac;
    }

    /**
     * Calculate a scaling factor to convert double demands into integer demands
     * 
     * @param instance
     * @param precision
     * @return a scaling factor that can convert all demands into integer values with the given precision
     */
    public static int calculateDemFracScale(IVRPInstance instance, double precision) {
        int frac = 1;

        for (IVRPRequest r : instance.getRequests()) {
            double d = r.getDemand();

            while (Math.abs(d * frac - Math.round(d * frac)) > precision * frac) {
                frac *= 10;
            }
        }
        return frac;
    }

    @Override
    public boolean writeInstance(IVRPInstance instance, File output, Object params)
            throws IOException {
        BufferedWriter w = new BufferedWriter(new FileWriter(output));

        String comment = params instanceof String ? (String) params : "none";

        // Name
        w.write(String.format("%s : %s\n", Specifications.NAME, instance.getName()));

        // Comment
        w.write(String.format("%s : %s\n", Specifications.COMMENT, comment));

        // Type
        w.write(String.format("%s : %s\n", Specifications.TYPE, "CVRP"));

        // Dimension
        w.write(String.format("%s : %s\n", Specifications.DIMENSION, instance.getRequestCount()
                + instance.getDepotCount()));

        // Edge Weight Type
        w.write(String.format("%s : %s\n", Specifications.EDGE_WEIGHT_TYPE, instance
                .getCostDelegate().getDistanceType()));

        // Capacity
        w.write(String.format("%s : %s\n", Specifications.CAPACITY, (int) instance.getFleet()
                .getVehicle().getCapacity()
                * getDemandsScaleFactor()));

        // Nodes coordinates
        w.write(DataSection.NODE_COORD_SECTION.toString() + "\n");

        List<IVRPRequest> requests = instance.getRequests();
        Collections.sort(requests, new ObjectWithIdComparator());

        // Autodetect the scaling factor for nodes coordinates
        if (getCoordinatesScaleFactor() <= 0) {
            setCoordinatesScaleFactor(calculateCoordFracScale(instance, 1e-3));
        }

        // Coordinates and demands
        StringBuilder demands = new StringBuilder();
        for (int d = 0; d < instance.getDepotCount(); d++) {
            Depot dep = instance.getDepot(d);
            int x = (int) Math.round(dep.getLocation().getX() * getCoordinatesScaleFactor());
            int y = (int) Math.round(dep.getLocation().getY() * getCoordinatesScaleFactor());
            w.write(String.format("%s %s %s\n", dep.getID() + 1, x, y));
            demands.append(String.format("%s %s\n", dep.getID() + 1, 0));
            // id++;
        }

        for (IVRPRequest r : requests) {
            int x = (int) Math
                    .round(r.getNode().getLocation().getX() * getCoordinatesScaleFactor());
            int y = (int) Math
                    .round(r.getNode().getLocation().getY() * getCoordinatesScaleFactor());
            w.write(String.format("%s %s %s\n", r.getID() + 1, x, y));
            demands.append(String.format("%s %s\n", r.getID() + 1,
                    (int) Math.round(r.getDemand() * getDemandsScaleFactor())));
            // id++;
        }

        // Node demands
        w.write(DataSection.DEMAND_SECTION.toString() + "\n");
        w.write(demands.toString());

        // Depots
        w.write(DataSection.DEPOT_SECTION.toString() + "\n");
        for (int d = 1; d <= instance.getDepotCount(); d++) {
            w.write(d + "\n");
        }
        w.write("-1\n");

        // End of file
        w.write(DataSection.EOF.toString());

        w.flush();

        w.close();

        return true;
    }

}
