/**
 *
 */
package vroom.trsp.util;

import java.util.logging.Logger;

import vroom.common.utilities.logging.LoggerHelper;
import vroom.common.utilities.logging.Logging;

/**
 * <code>TRSPLogging</code> is a helper class that is used to configure and manage the {@link Logger} instances used in
 * the TRSP procedures
 * <p>
 * Creation date: Mar 24, 2011 - 4:34:08 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TRSPLogging extends Logging {

    /** The base logger for any TRSP logger */
    public static final String        BASE_LOGGER               = "TRSP";
    private static final LoggerHelper sBase                     = LoggerHelper
                                                                        .getLogger(BASE_LOGGER);
    /** The logger for TRSP runs */
    public static final String        TRSP_RUN_LOGGER           = "TRSP.Run";
    private static final LoggerHelper sRun                      = LoggerHelper
                                                                        .getLogger(TRSP_RUN_LOGGER);
    /** The logger for TRSP simulation */
    public static final String        TRSP_SIM_LOGGER           = "TRSP.Sim";
    private static final LoggerHelper sSim                      = LoggerHelper
                                                                        .getLogger(TRSP_SIM_LOGGER);
    /** The logger for TRSP procedure related logs */
    public static final String        TRSP_PROCEDURE_LOGGER     = "TRSP.Main";
    private static final LoggerHelper sProc                     = LoggerHelper
                                                                        .getLogger(TRSP_PROCEDURE_LOGGER);
    /** The logger for TRSP setup logs (definition of parameters) */
    public static final String        TRSP_SETUP_LOGGER         = "TRSP.Setup";
    private static final LoggerHelper sSetup                    = LoggerHelper
                                                                        .getLogger(TRSP_SETUP_LOGGER);
    /** The logger for TRSP events logs */
    public static final String        TRSP_EVENTS_LOGGER        = "TRSP.Events";
    private static final LoggerHelper sEvents                   = LoggerHelper
                                                                        .getLogger(TRSP_EVENTS_LOGGER);
    /** The logger for TRSP components logs */
    public static final String        TRSP_COMPONENTS_LOGGER    = "TRSP.Comp";
    private static final LoggerHelper sComp                     = LoggerHelper
                                                                        .getLogger(TRSP_COMPONENTS_LOGGER);
    /** The logger for TRSP optimization logs */
    public static final String        TRSP_OPTIMIZATION_LOGGER  = "TRSP.Optim";
    private static final LoggerHelper sOpt                      = LoggerHelper
                                                                        .getLogger(TRSP_OPTIMIZATION_LOGGER);
    /** The logger for TRSP neighborhoods logs */
    public static final String        TRSP_NEIGHBORHOODS_LOGGER = "TRSP.Optim.Neigh";
    private static final LoggerHelper sNeigh                    = LoggerHelper
                                                                        .getLogger(TRSP_NEIGHBORHOODS_LOGGER);

    /**
     * TRSP base logger
     * 
     * @return the base (root) logger for the TRSP framework
     */
    public final static LoggerHelper getBaseLogger() {
        return sBase;
    }

    /**
     * TRSP Run logger
     * 
     * @return the logger for TRSP runs
     */
    public final static LoggerHelper getRunLogger() {
        return sRun;
    }

    /**
     * TRSP simulation logger
     * 
     * @return the logger for TRSP simulation
     */
    public final static LoggerHelper getSimulationLogger() {
        return sSim;
    }

    /**
     * TRSP Procedure Logger
     * 
     * @return the logger to be used in the TRSP procedure
     */
    public final static LoggerHelper getProcedureLogger() {
        return sProc;
    }

    /**
     * TRSP Setup logger
     * 
     * @return the logger to be used in the setup process of the TRSP procedure
     */
    public final static LoggerHelper getSetupLogger() {
        return sSetup;
    }

    /**
     * TRSP Components logger
     * 
     * @return the logger to be used by the components of the TRSP procedure
     */
    public final static LoggerHelper getComponentsLogger() {
        return sComp;
    }

    /**
     * TRSP Optimization logger
     * 
     * @return the logger to be used by the optimization of the TRSP procedure
     */
    public final static LoggerHelper getOptimizationLogger() {
        return sOpt;
    }

    /**
     * TRSP neighborhoods logger
     * 
     * @return the logger to be used by the optimization of the TRSP procedure
     */
    public final static LoggerHelper getNeighborhoodLogger() {
        return sNeigh;
    }

    /**
     * TRSP Events logger
     * 
     * @return the logger to be used to event related logs of the TRSP procedure
     */
    public final static LoggerHelper getEventsLogger() {
        return sEvents;
    }

}
