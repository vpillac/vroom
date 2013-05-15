/**
 * 
 */
package vroom.trsp.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import vroom.common.modeling.dataModel.Depot;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.dataModel.IVRPRequest;
import vroom.common.modeling.dataModel.attributes.ITimeWindow;
import vroom.common.modeling.io.FlatFilePersistenceHelper;
import vroom.common.utilities.Utilities;
import vroom.common.utilities.dataModel.ObjectWithIdComparator;
import vroom.trsp.datamodel.TRSPRequest;
import vroom.trsp.datamodel.Technician;
import vroom.trsp.legacy.TRSPLegacyInstance;

/**
 * The class <code>TRSPSolomonLegacyPersistenceHelper</code> is dedicated to the writing of TRSP instance file with a
 * format derived from the one of Solomon
 * <p>
 * Creation date: Feb 11, 2011 - 2:17:53 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 2.0
 */
public class TRSPSolomonLegacyPersistenceHelper extends FlatFilePersistenceHelper {

    /**
     * Creates a new <code>TRSPSolomonLegacyPersistenceHelper</code>
     */
    public TRSPSolomonLegacyPersistenceHelper() {
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
    public TRSPLegacyInstance readInstance(File input, Object... params) throws IOException {
        throw new UnsupportedOperationException("Read instance is not supported by this legacy implementation");
    }

    @Override
    public boolean writeInstance(IVRPInstance vrpinstance, File output, Object params) throws IOException {
        TRSPLegacyInstance instance = null;
        if (vrpinstance instanceof TRSPLegacyInstance) {
            instance = (TRSPLegacyInstance) vrpinstance;
        } else {
            return false;
        }
        BufferedWriter w = new BufferedWriter(new FileWriter(output));

        w.write(instance.getName());

        w.write("\n\nINFO\n");
        w.write("CREW COUNT  SKILLS  TOOLS   SPARE PARTS\n");
        w.write(String.format("%-8s%-8s%-8s%s\n", instance.getFleet().size(), instance.getSkillCount(),
                instance.getToolCount(), instance.getSpareCount()));

        // w.write("ID   SKILLS    TOOLS     SPARE PARTS\n");
        // for (Technician t : instance.getFleet()) {
        // w.write(String.format("%-5s%s \t%s \t%s\n", t.getID(), t.getSkillSet(), t.getToolSet(),
        // Arrays.toString(t.getAvailableSpareParts())));
        // }

        w.write("\nDEPOT TECHNICIANS AND REQUESTS\n");
        w.write("ID   X     Y     TWS    TWE    Serv   SKILLS   TOOLS    SPARE PARTS\n");
        // Depot
        Depot d = instance.getMainDepot();
        ITimeWindow tw = d.getTimeWindow();
        long tws = tw != null ? (long) tw.startAsDouble() : 0;
        long twe = tw != null ? (long) tw.endAsDouble() : 0;
        w.write(String.format("%-5s%-6s%-6s%-7s%-7s%-7s%s \t%s \t%s\n", d.getID(), (int) d.getLocation().getX(),
                (int) d.getLocation().getY(), tws, twe, 0, "[]", "[]", "[]"));

        // Technicians
        for (Technician t : instance.getFleet()) {
            d = t.getHome();
            tw = t.getHome().getTimeWindow();
            tws = tw != null ? (long) tw.startAsDouble() : 0;
            twe = tw != null ? (long) tw.endAsDouble() : 0;
            w.write(String.format("%-5s%-6s%-6s%-7s%-7s%-7s%s \t%s \t%s\n", d.getID(), (int) d.getLocation().getX(),
                    (int) d.getLocation().getY(), tws, twe, 0, t.getSkillSet(), t.getToolSet(),
                    Utilities.toShortString(t.getSpareParts())));
        }

        // Requests
        List<IVRPRequest> reqs = instance.getRequests();
        Collections.sort(reqs, new ObjectWithIdComparator());
        for (IVRPRequest r : reqs) {
            TRSPRequest s = (TRSPRequest) r;
            tw = s.getTimeWindow();
            tws = tw != null ? (long) tw.startAsDouble() : 0;
            twe = tw != null ? (long) tw.endAsDouble() : 0;
            w.write(String.format("%-5s%-6s%-6s%-7s%-7s%-7s%s \t%s \t%s\n", s.getID(), (int) s.getNode().getLocation()
                    .getX(), (int) s.getNode().getLocation().getY(), tws, twe, s.getServiceTime(), s.getSkillSet(),
                    s.getToolSet(), Utilities.toShortString(s.getSparePartRequirements())));
        }
        w.flush();
        w.close();
        return true;
    }

    /**
     * Converts a double array to a string
     * 
     * @param array
     *            an array of values (eg [1.0, 2.0, 3.0])
     * @return a string of the form [1,2,3]
     */
    public static String toShortStringDecimal(double[] array) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < array.length; i++) {
            sb.append((int) array[i]);
            if (i < array.length - 1)
                sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    protected void finalizeInstance(IVRPInstance instance, Object... params) {
        throw new UnsupportedOperationException("Read instance is not supported by this legacy implementation");
    }

    @Override
    protected void parseLine(IVRPInstance instance, String line, int lineNumber, Object... params) {
        throw new UnsupportedOperationException("Read instance is not supported by this legacy implementation");
    }

    @Override
    protected IVRPInstance initializeInstance(File input, BufferedReader reader, Object... params) throws IOException {
        throw new UnsupportedOperationException("Read instance is not supported by this legacy implementation");
    }
}
