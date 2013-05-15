/**
 * 
 */
package vroom.common.modeling.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import vroom.common.modeling.dataModel.DynamicInstance;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.dataModel.attributes.ReleaseDate;
import vroom.common.modeling.dataModel.attributes.RequestAttributeKey;

/**
 * <code>DynamicPersistenceHelper</code>
 * <p>
 * Creation date: Nov 8, 2011 - 3:07:06 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class DynamicPersistenceHelper implements IPersistenceHelper<File> {

    private final IPersistenceHelper<File> mInstanceReader;

    /**
     * Creates a new <code>DynamicPersistenceHelper</code>
     * 
     * @param instanceReader
     *            the persistence helper used to read the base instance
     */
    public DynamicPersistenceHelper(IPersistenceHelper<File> instanceReader) {
        super();
        mInstanceReader = instanceReader;
    }

    @Override
    public DynamicInstance readInstance(File input, Object... params) throws Exception {
        File dynFile = (File) params[0];
        DynamicInstance instance = new DynamicInstance(mInstanceReader.readInstance(input));

        BufferedReader reader = new BufferedReader(new FileReader(dynFile));

        // Skip the comment line and other info
        reader.readLine();
        reader.readLine();
        reader.readLine();
        reader.readLine();

        String line = reader.readLine();
        while (line != null) {
            String[] info = line.split("\\s+");
            int id = Integer.valueOf(info[0]);
            int rd = Integer.valueOf(info[1]);
            instance.getRequest(id).setAttribute(RequestAttributeKey.RELEASE_DATE,
                    new ReleaseDate(rd));
            line = reader.readLine();
        }

        reader.close();
        return instance;
    }

    /**
     * @param path
     * @param dynProp
     * @return a list of the files contains in the directory specified by <code>path</code> that match the given pattern
     * @throws FileNotFoundException
     */
    public static Map<String, List<File>> getRelDateFiles(String path, int... dynProp)
            throws FileNotFoundException {
        Map<String, List<File>> files = new HashMap<String, List<File>>();
        File folder = new File(path);
        if (!folder.exists())
            throw new FileNotFoundException("Folder not found, path:" + path);
        for (String f : folder.list()) {
            for (int dp : dynProp) {
                if (f.contains("_rd_" + dp)) {
                    String name = f.substring(0, f.indexOf("_rd_"));
                    File rdFile = new File(String.format("%s%s%s", path, File.separatorChar, f));
                    if (!files.containsKey(name))
                        files.put(name, new LinkedList<File>());
                    files.get(name).add(rdFile);
                    Collections.sort(files.get(name));
                    break;
                }
            }
        }

        return files;
    }

    @Override
    public boolean writeInstance(IVRPInstance instance, File output, Object params)
            throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void reset() {
        mInstanceReader.reset();
    }

}
