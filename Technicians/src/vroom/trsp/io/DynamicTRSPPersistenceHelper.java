/**
 * 
 */
package vroom.trsp.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import vroom.common.modeling.dataModel.attributes.ReleaseDate;
import vroom.common.modeling.dataModel.attributes.RequestAttributeKey;
import vroom.trsp.datamodel.TRSPInstance;

/**
 * <code>DynamicTRSPPersistenceHelper</code>
 * <p>
 * Creation date: Nov 8, 2011 - 3:07:06 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class DynamicTRSPPersistenceHelper implements ITRSPPersistenceHelper {

    private final ITRSPPersistenceHelper mInstanceReader;

    /**
     * Creates a new <code>DynamicTRSPPersistenceHelper</code>
     * 
     * @param instanceReader
     *            the persistence helper used to read the base instance
     */
    public DynamicTRSPPersistenceHelper(ITRSPPersistenceHelper instanceReader) {
        super();
        mInstanceReader = instanceReader;
    }

    @Override
    public TRSPInstance readInstance(File input, Object... params) throws Exception {
        File dynFile = (File) params[0];
        boolean cvrptw = (boolean) params[1];
        TRSPInstance instance = mInstanceReader.readInstance(input, cvrptw);

        readRelDates(instance, dynFile, false);

        return instance;
    }

    /**
     * Read the release dates from a file
     * 
     * @param instance
     * @param rdFile
     * @param cvrpInstance
     * @throws IOException
     */
    public static void readRelDates(TRSPInstance instance, File rdFile, boolean cvrpInstance)
            throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(rdFile));

        // Skip the comment line and other info
        reader.readLine();
        reader.readLine();
        reader.readLine();
        reader.readLine();

        String line = reader.readLine();
        double dynReq = 0;
        while (line != null) {
            String[] info = line.split("\\s+");
            int id = Integer.valueOf(info[0]);
            int rd = Integer.valueOf(info[1]);
            instance.getRequest(cvrpInstance ? instance.getDepotCount() + id - 1 : id)
                    .setAttribute(RequestAttributeKey.RELEASE_DATE, new ReleaseDate(rd));
            if (rd >= 0)
                dynReq++;
            line = reader.readLine();
        }
        double dod = dynReq / instance.getRequestCount();
        instance.setDod(dod);
        reader.close();
    }

    @Override
    public boolean writeInstance(TRSPInstance instance, File output) throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

}
