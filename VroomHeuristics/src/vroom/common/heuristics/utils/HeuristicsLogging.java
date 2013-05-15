/**
 * 
 */
package vroom.common.heuristics.utils;

import java.util.logging.Logger;

import vroom.common.utilities.logging.LoggerHelper;

/**
 * <code>HeuristicsLogging</code> is an helper class that is used to configure and manage the {@link Logger} instances
 * used in the heuristics
 * <p>
 * Creation date: Oct 4, 2010 - 5:30:39 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class HeuristicsLogging {
    /** The base logger for any Heuristics logger */
    public static final String BASE_LOGGER                     = "heur";
    /** The logger for Heuristics procedure related logs */
    public static final String HEURISTICS_PROCEDURE_LOGGER     = BASE_LOGGER + ".Main";
    /** The logger for Heuristics setup logs (definition of parameters) */
    public static final String HEURISTICS_SETUP_LOGGER         = BASE_LOGGER + ".Setup";
    /** The logger for Heuristics Neighborhoods logs */
    public static final String HEURISTICS_NEIGHBORHOODS_LOGGER = BASE_LOGGER + ".Neigh";

    // static {
    // LoggerHelper.setupRootLogger(LoggerHelper.LEVEL_DEBUG);
    // }

    /**
     * Heuristics base logger
     * 
     * @return the base (root) logger for the Heuristics framework
     */
    public final static LoggerHelper getBaseLogger() {
        return LoggerHelper.getLogger(BASE_LOGGER);
    }

    /**
     * Heuristics Procedure Logger
     * 
     * @return the logger to be used in the Heuristics procedure
     */
    public final static LoggerHelper getProcedureLogger() {
        return LoggerHelper.getLogger(HEURISTICS_PROCEDURE_LOGGER);
    }

    /**
     * Heuristics Setup logger
     * 
     * @return the logger to be used in the setup process of the Heuristics procedure
     */
    public final static LoggerHelper getSetupLogger() {
        return LoggerHelper.getLogger(HEURISTICS_SETUP_LOGGER);
    }

    /**
     * Heuristics Neighborhoods logger
     * 
     * @return the logger to be used by the Neighborhoods of the Heuristics procedure
     */
    public final static LoggerHelper getNeighborhoodsLogger() {
        return LoggerHelper.getLogger(HEURISTICS_NEIGHBORHOODS_LOGGER);
    }

    /**
     * Get a custom logger in the heuristic namespace
     * 
     * @param name
     * @return a logger with the given name
     */
    public static LoggerHelper getLogger(String name) {
        return LoggerHelper.getLogger(String.format("%s.%s", BASE_LOGGER, name));
    }
}
