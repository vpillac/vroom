package vroom.common.heuristics.vls;

import vroom.common.utilities.logging.LoggerHelper;
import vroom.common.utilities.logging.Logging;

/**
 * <code>VLSLogging</code> is a utility class for logging in the VLS procedure.
 * <p>
 * Creation date: 1 mai 2010 - 15:30:50.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class VLSLogging extends Logging {

    /** The base logger for any VLS logger. */
    public static final String BASE_LOGGER          = "VLS";

    /** The logger for VLS procedure related logs. */
    public static final String VLS_PROCEDURE_LOGGER = "VLS.Main";

    /** The logger for VLS setup logs (definition of parameters). */
    public static final String VLS_SETUP_LOGGER     = "VLS.Setup";

    /** The logger for VLS algorithm logs (definition of parameters). */
    public static final String VLS_ALGO_LOGGER      = "VLS.Algorithm";

    /**
     * VLS base logger.
     * 
     * @return the base (root) logger for the VLS framework
     */
    public final static LoggerHelper getBaseLogger() {
        return LoggerHelper.getLogger(BASE_LOGGER);
    }

    /**
     * VLS procedure logger.
     * 
     * @return the logger used at the procedure level for the VLS framework
     */
    public final static LoggerHelper getProcedureLogger() {
        return LoggerHelper.getLogger(VLS_PROCEDURE_LOGGER);
    }

    /**
     * VLS setup logger.
     * 
     * @return the logger used for setup logs in the VLS framework
     */
    public final static LoggerHelper getSetupLogger() {
        return LoggerHelper.getLogger(VLS_SETUP_LOGGER);
    }

    /**
     * VLS algorithm logger.
     * 
     * @return the logger used at the algorithm level for the VLS framework
     */
    public final static LoggerHelper getAlgoLogger() {
        return LoggerHelper.getLogger(VLS_ALGO_LOGGER);
    }
}
