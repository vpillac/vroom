/**
 * 
 */
package vroom.trsp.optimization;

import java.io.File;
import java.util.ArrayList;

import vroom.common.modeling.io.ChristofidesPersistenceHelper;
import vroom.common.modeling.io.SolomonPersistenceHelper;
import vroom.common.modeling.io.TSPLibPersistenceHelper;
import vroom.common.utilities.BestKnownSolutions;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.datamodel.TRSPTour;
import vroom.trsp.datamodel.costDelegates.TRSPCostDelegate;
import vroom.trsp.io.ConvertionPersistenceHelper;
import vroom.trsp.io.ITRSPPersistenceHelper;
import vroom.trsp.io.PillacSimplePersistenceHelper;

/**
 * <code>TRSPUtilities</code>
 * <p>
 * Creation date: May 13, 2011 - 4:23:40 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TRSPUtilities {

    /**
     * Returns the persistence helper to be used to read instances defined by the given path
     * 
     * @param path
     *            can be either the path of a file or a directory
     * @return the corresponding persistence helper
     */
    public static ITRSPPersistenceHelper getPersistenceHelper(String path) {
        if (path.contains("trsp")) {
            return new PillacSimplePersistenceHelper();
        } else if (path.contains("christofides-mingozzi-toth")) {
            return new ConvertionPersistenceHelper(new ChristofidesPersistenceHelper());
        } else if (path.contains("augerat")) {
            return new ConvertionPersistenceHelper(new TSPLibPersistenceHelper());
        } else if (path.contains("solomon")) {
            return new ConvertionPersistenceHelper(new SolomonPersistenceHelper(1));
        } else {
            throw new UnsupportedOperationException("Unsupported instance directory: " + path);
        }
    }

    /**
     * Returns the persistence helper to be used to read instances defined by the given path
     * 
     * @param path
     *            can be either the path of a file or a directory
     * @return the corresponding persistence helper
     */
    public static BestKnownSolutions getBKS(String path) {
        String bksPath = "";
        if (path.contains("/trsp/pillac")) {
            bksPath = "../Instances/trsp/pillac/pillac.sol";
        } else if (path.contains("/christofides-mingozzi-toth")) {
            bksPath = "../Instances/cvrp/christofides-mingozzi-toth.sol";
        } else if (path.contains("/augerat")) {
            bksPath = "../Instances/cvrp/augerat.sol";
        } else if (path.contains("/solomon")) {
            bksPath = "../Instances/cvrptw/solomon-100.sol";
        } else {
            throw new UnsupportedOperationException("Unsupported instance directory: " + path);
        }
        return new BestKnownSolutions(bksPath);
    }

    /**
     * Read an instance from a file
     * 
     * @param instanceFile
     *            the path of the instance definition file
     * @param cvrptw
     *            {@code true} if this is a cvrptw instance
     * @return the instance contained in {@code  instanceFile}
     */
    public static TRSPInstance readInstance(String instanceFile, boolean cvrptw) {
        ITRSPPersistenceHelper reader = TRSPUtilities.getPersistenceHelper(instanceFile);

        TRSPInstance instance = null;
        try {
            instance = reader.readInstance(new File(instanceFile));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return instance;
    }

    /**
     * Reads a solution from a string
     * 
     * @param instance
     * @param solString
     * @param costDelegate
     * @return the solution described in {@code  solString}
     * @see TRSPSolution#toShortString()
     */
    public static TRSPSolution readSolution(TRSPInstance instance, String solString,
            TRSPCostDelegate costDelegate) {
        ArrayList<Integer> sol = new ArrayList<Integer>(solString.length() / 3);

        // Remove delimiters
        solString = solString.substring(1, solString.length() - 2);

        // Split string into nodes
        String[] nodes = solString.split("\\p{Punct}");
        for (String node : nodes) {
            sol.add(Integer.valueOf(node));
        }

        TRSPSolution solution = new TRSPSolution(instance, costDelegate);

        int i = 0;
        int tech = 0;
        TRSPTour tour = solution.getTour(tech);
        while (i < sol.size()) {
            int node = sol.get(i);
            if (!instance.isMainDepot(node) && instance.isDepot(node)
                    && node < instance.getDepotCount()) {
                tour = solution.getTour(tech++);
            }
            tour.appendNode(node);
        }

        return solution;
    }
}
