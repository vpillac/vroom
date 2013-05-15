/*
 * jCW : a java library for the development of saving based heuristics
 */
package vroom.common.heuristics.cw;

import vroom.common.utilities.logging.LoggerHelper;
import vroom.common.utilities.logging.Logging;

/**
 * <code>CWLogging</code> is a utility class for the handling of log events from different sources
 * <p>
 * Creation date: Apr 16, 2010 - 4:00:10 PM.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class CWLogging extends Logging {

    /** The base logger for any CW logger. */
    public static final String BASE_LOGGER         = "jCW";

    /** The logger for CW procedure related logs. */
    public static final String CW_PROCEDURE_LOGGER = "jCW.Main";

    /** The logger for CW setup logs (definition of parameters). */
    public static final String CW_SETUP_LOGGER     = "jCW.Setup";

    /** The logger for CW algorithm logs (definition of parameters). */
    public static final String CW_ALGO_LOGGER      = "jCW.Algorithm";

    /**
     * CW base logger.
     * 
     * @return the base (root) logger for the CW framework
     */
    public final static LoggerHelper getBaseLogger() {
        return LoggerHelper.getLogger(BASE_LOGGER);
    }

    /**
     * CW procedure logger.
     * 
     * @return the logger used at the procedure level for the CW framework
     */
    public final static LoggerHelper getProcedureLogger() {
        return LoggerHelper.getLogger(CW_PROCEDURE_LOGGER);
    }

    /**
     * CW setup logger.
     * 
     * @return the logger used for setup logs in the CW framework
     */
    public final static LoggerHelper getSetupLogger() {
        return LoggerHelper.getLogger(CW_SETUP_LOGGER);
    }

    /**
     * CW algorithm logger.
     * 
     * @return the logger used at the algorithm level for the CW framework
     */
    public final static LoggerHelper getAlgoLogger() {
        return LoggerHelper.getLogger(CW_ALGO_LOGGER);
    }
}
