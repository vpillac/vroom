/**
 * 
 */
package vroom.trsp.bench;

import java.io.File;
import java.io.IOException;

import vroom.common.heuristics.vls.VLSLogging;
import vroom.common.heuristics.vns.VariableNeighborhoodSearch;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.common.utilities.logging.Logging;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.io.PillacSimplePersistenceHelper;
import vroom.trsp.optimization.mTSPHeur.TRSPmTSPHeuristic;

/**
 * <code>MTSPHeuristicTest</code> is a class used to test the {@link TRSPmTSPHeuristic}
 * <p>
 * Creation date: Feb 17, 2011 - 4:36:54 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class MTSPHeuristicTest {

    public static String INSTANCE = "../Instances/trsp/pillac/C202-S5_3_1-T10_8_3.txt";

    public static void main(String[] args) {

        Logging.setupRootLogger(LoggerHelper.LEVEL_INFO, LoggerHelper.LEVEL_INFO, false);
        VLSLogging.getBaseLogger().setLevel(LoggerHelper.LEVEL_WARN);
        VariableNeighborhoodSearch.LOGGER.setLevel(LoggerHelper.LEVEL_WARN);

        PillacSimplePersistenceHelper reader = new PillacSimplePersistenceHelper();
        try {
            TRSPInstance instance = reader.readInstance(new File(INSTANCE));

            TRSPmTSPHeuristic heuristic = new TRSPmTSPHeuristic();
            heuristic.setMRG32k3aRndStream(new long[] { 1, 2, 3, 4, 5, 6 }, "default");
            heuristic.setInstance(instance);
            heuristic.run();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
