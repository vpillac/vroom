/**
 *
 */
package vroom.common.utilities.gurobi;

import gurobi.GRB.IntParam;
import gurobi.GRBEnv;
import gurobi.GRBException;

import java.io.File;

import vroom.common.utilities.logging.Logging;

/**
 * <code>GRBEnvProvider</code> is a utility class that is used to create a unique {@link GRBEnv} using the ./gurobi
 * working folder.
 * <p>
 * Creation date: Aug 11, 2011 - 3:35:09 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class GRBEnvProvider {

    /** The default working directory */
    public static final String GRB_WORKING_DIR = "./gurobi";

    private static GRBEnv      sEnvironment    = null;

    /**
     * Getter for the unique instance of {@link GRBEnv}
     * 
     * @return the unique instance of {@link GRBEnv}
     */
    public static GRBEnv getEnvironment() {
        if (sEnvironment == null) {
            File dir = new File(GRB_WORKING_DIR);
            if (dir.isFile())
                dir.delete();
            if (!dir.exists())
                dir.mkdir();
            try {
                sEnvironment = new GRBEnv(String.format("%s/%s", GRB_WORKING_DIR, "gurobi.log"));
            } catch (GRBException e) {
                Logging.getBaseLogger().exception("GRBEnvProvider.getEnvironment", e);
            }
        }

        return sEnvironment;
    }

    /**
     * Enable or disable the logging to file
     * 
     * @param log
     *            <code>true</code> to enable logging, <code>false</code> to disable it
     */
    public static void setLogging(boolean log) {
        try {
            getEnvironment().set(IntParam.OutputFlag, log ? 1 : 0);
        } catch (GRBException e) {
            Logging.getBaseLogger().exception("GRBEnvProvider.setLogging", e);
        }
    }
}
