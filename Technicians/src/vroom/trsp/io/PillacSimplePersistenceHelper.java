/**
 * 
 */
package vroom.trsp.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import vroom.common.modeling.dataModel.Depot;
import vroom.common.modeling.dataModel.Node;
import vroom.common.modeling.dataModel.attributes.ITimeWindow;
import vroom.common.modeling.dataModel.attributes.NodeAttributeKey;
import vroom.common.modeling.dataModel.attributes.PointLocation;
import vroom.common.modeling.dataModel.attributes.SimpleTimeWindow;
import vroom.common.utilities.Utilities;
import vroom.common.utilities.dataModel.ObjectWithIdComparator;
import vroom.common.utilities.gis.MapPointDist;
import vroom.trsp.datamodel.TRSPDistTimeMatrix;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPRequest;
import vroom.trsp.datamodel.Technician;

/**
 * The Class <code>PillacSimplePersistenceHelper</code> is responsible for the parsing of TRSP instances between a flat
 * file Solomon format and instances of {@link TRSPInstance}
 * <p>
 * Creation date: Feb 23, 2011 - 2:08:57 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class PillacSimplePersistenceHelper implements ITRSPPersistenceHelper {

    public static final PillacSimplePersistenceHelper INSTANCE             = new PillacSimplePersistenceHelper();

    /** The default file name pattern for instances */
    public static final String                        DEFAULT_FILE_PATTERN = "\\d+\\-R?C?\\d+_\\d+\\-\\d+\\-\\d+\\-\\d+.txt";

    /*
     * (non-Javadoc)
     * @see vroom.trsp.io.ITRSPPersistenceHelper#readInstance(java.io.File)
     */
    @Override
    public TRSPInstance readInstance(File file, Object... params) throws IOException {
        if (!file.exists())
            throw new FileNotFoundException("Instance file " + file.getPath() + " does not exist");

        boolean cvrptw = params.length > 0 ? (boolean) params[0] : false;

        BufferedReader r = new BufferedReader(new FileReader(file));

        // Instance name
        String name = r.readLine();

        r.readLine();// Empty line
        r.readLine();// TECHNICIANS
        r.readLine();// HEADER

        // Fleet info
        String line = r.readLine();
        if (line.startsWith(" "))
            line = line.replaceFirst("\\s+", "");
        String[] values = line.split("\\s+");
        int numTec = Integer.parseInt(values[0]);
        int numSkills = Integer.parseInt(values[1]);
        int numTools = Integer.parseInt(values[2]);
        int numSpareParts = Integer.parseInt(values[3]);

        r.readLine();// Empty line
        r.readLine();// REQUESTS
        r.readLine();// HEADER

        // Depots and technicians
        ArrayList<Depot> depots = new ArrayList<Depot>(numTec + 1);
        ArrayList<Technician> technicians = new ArrayList<Technician>(numTec);

        for (int d = 0; d <= numTec; d++) {
            values = r.readLine().split("\\s+");
            int id = Integer.valueOf(values[0]);
            double x = Double.valueOf(values[1]);
            double y = Double.valueOf(values[2]);
            double tws = Double.valueOf(values[3]);
            double twe = Double.valueOf(values[4]);
            int[] skills = Utilities.toIntArray(values[6]);
            int[] tools = Utilities.toIntArray(values[7]);
            int[] spare = Utilities.toIntArray(values[8]);

            Depot depot = new Depot(id, new PointLocation(x, y));
            depot.setAttribute(NodeAttributeKey.TIME_WINDOW, new SimpleTimeWindow(tws, twe));
            depots.add(id, depot);

            if (id > 0) {
                id--;
                technicians.add(id, new Technician(id, "tech-" + id, 0, 1, 1, skills, tools, spare,
                        depot));
            }
        }

        // Requests
        ArrayList<TRSPRequest> requests = new ArrayList<TRSPRequest>();
        line = r.readLine();
        while (line != null && !line.startsWith("DISTANCES")) {

            values = line.split("\\s+");
            int id = Integer.valueOf(values[0]);
            double x = Double.valueOf(values[1]);
            double y = Double.valueOf(values[2]);
            double tws = Double.valueOf(values[3]);
            double twe = Double.valueOf(values[4]);
            double serv = Double.valueOf(values[5]);
            int[] skills = Utilities.toIntArray(values[6]);
            int[] tools = Utilities.toIntArray(values[7]);
            int[] spare = Utilities.toIntArray(values[8]);

            TRSPRequest req = new TRSPRequest(id, new Node(id, new PointLocation(x, y)), skills,
                    tools, spare, new SimpleTimeWindow(tws, twe), serv);
            requests.add(id - depots.size(), req);

            line = r.readLine();
        }

        TRSPInstance instance = new TRSPInstance(name, technicians, numSkills, numTools,
                numSpareParts, depots, requests, cvrptw);

        if (line != null && line.startsWith("DISTANCES")) {
            // Read the distance/time matrix

            double[][][] distTimeMatrix = MapPointDist.loadDistanceTimeMatrix(
                    file.getAbsolutePath(), instance.getMaxId(), false);
            instance.setCostDelegate(new TRSPDistTimeMatrix(instance, distTimeMatrix));
        }

        r.close();
        return instance;

    }

    /*
     * (non-Javadoc)
     * @see vroom.trsp.io.ITRSPPersistenceHelper#writeInstance(vroom.trsp.datamodel.TRSPInstance, java.io.File)
     */
    @Override
    public boolean writeInstance(TRSPInstance instance, File file) throws IOException {
        Locale.setDefault(Locale.US);

        boolean distOnly = instance.getCostDelegate() instanceof TRSPDistTimeMatrix;

        BufferedWriter w = new BufferedWriter(new FileWriter(file));

        w.write(instance.getName());

        w.write("\n\nINFO\n");
        w.write("CREW COUNT  SKILLS    TOOLS   SPARE PARTS\n");
        w.write(String.format(" %8s %8s %8s %13s\n", instance.getFleet().size(),
                instance.getSkillCount(), instance.getToolCount(), instance.getSpareCount()));

        // w.write("ID   SKILLS    TOOLS     SPARE PARTS\n");
        // for (Technician t : instance.getFleet()) {
        // w.write(String.format(" %5s%s \t%s \t%s\n", t.getID(), t.getSkillSet(), t.getToolSet(),
        // Arrays.toString(t.getAvailableSpareParts())));
        // }

        w.write("\nDEPOT TECHNICIANS AND REQUESTS\n");
        w.write("ID           X        Y     TWS     TWE    Serv     SKILLS   TOOLS    SPARE PARTS\n");

        // Depot
        Depot d = instance.getDepot(0);
        ITimeWindow tw = d.getTimeWindow();
        double tws = tw != null ? tw.startAsDouble() : 0;
        double twe = tw != null ? tw.endAsDouble() : 0;
        w.write(String.format("%-5s %8.1f %8.1f %7.1f %7.1f %7.1f \t%s \t%s \t%s\n", d.getID(),
                distOnly ? 0 : d.getLocation().getX(), distOnly ? 0 : d.getLocation().getY(), tws,
                twe, 0d, "[]", "[]", "[]"));

        // Technicians
        for (Technician t : instance.getFleet()) {
            d = t.getHome();
            tw = t.getHome().getTimeWindow();
            tws = tw != null ? tw.startAsDouble() : 0;
            twe = tw != null ? tw.endAsDouble() : 0;
            w.write(String.format("%-5s %8.1f %8.1f %7.1f %7.1f %7.1f \t%s \t%s \t%s\n", d.getID(),
                    distOnly ? 0 : d.getLocation().getX(), distOnly ? 0 : d.getLocation().getY(),
                    tws, twe, 0d, t.getSkillSet(), t.getToolSet(),
                    Utilities.toShortString(t.getSpareParts())));
        }

        // Requests
        List<TRSPRequest> requests = new ArrayList<TRSPRequest>(instance.getRequests());
        Collections.sort(requests, new ObjectWithIdComparator());
        for (TRSPRequest r : requests) {
            tw = r.getTimeWindow();
            tws = tw != null ? tw.startAsDouble() : 0;
            twe = tw != null ? tw.endAsDouble() : 0;
            w.write(String.format("%-5s %8.1f %8.1f %7.1f %7.1f %7.1f \t%s \t%s \t%s\n", r.getID(),
                    distOnly ? 0 : r.getNode().getLocation().getX(), distOnly ? 0 : r.getNode()
                            .getLocation().getY(), tws, twe, r.getServiceTime(), r.getSkillSet(),
                    r.getToolSet(), Utilities.toShortString(r.getSparePartRequirements())));
        }

        // Distance and time matrices
        if (distOnly) {
            w.write("DISTANCES\n");
            for (int i = 0; i < instance.getMaxId(); i++) {
                w.write("" + i);
                for (int j = 0; j < instance.getMaxId(); j++) {
                    w.write(String.format(";%.4f", instance.getCostDelegate().getDistance(i, j)));
                }
                w.write("\n");
            }
            w.write("TRAVEL TIMES\n");
            for (int i = 0; i < instance.getMaxId(); i++) {
                w.write("" + i);
                for (int j = 0; j < instance.getMaxId(); j++) {
                    w.write(String.format(";%.4f",
                            instance.getCostDelegate().getTravelTime(i, j, null)));
                }
                w.write("\n");
            }
        }

        w.flush();
        w.close();
        return true;
    }
}
