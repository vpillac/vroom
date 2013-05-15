package vroom.optimization.online.jmsa.utils;

import java.util.logging.Logger;

import vroom.common.utilities.logging.LoggerHelper;
import vroom.common.utilities.logging.Logging;

/**
 * <code>Logging</code> is an helper class that is used to configure and manage the {@link Logger} instances used in the
 * MSA procedure
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 */
public final class MSALogging extends Logging {

    /** The base logger for any MSA logger */
    public static final String BASE_LOGGER           = "jMSA";
    /** The logger for MSA procedure related logs */
    public static final String MSA_PROCEDURE_LOGGER  = "jMSA.Main";
    /** The logger for MSA setup logs (definition of parameters) */
    public static final String MSA_SETUP_LOGGER      = "jMSA.Setup";
    /** The logger for MSA events logs */
    public static final String MSA_EVENTS_LOGGER     = "jMSA.Events";
    /** The logger for MSA components logs */
    public static final String MSA_COMPONENTS_LOGGER = "jMSA.Components";
    /** The logger for MSA simulation related logs */
    public static final String MSA_SIMULATION_LOGGER = "jMSA.Sim";

    // static {
    // LoggerHelper.setupRootLogger(LoggerHelper.LEVEL_DEBUG);
    // }

    /**
     * MSA base logger
     * 
     * @return the base (root) logger for the MSA framework
     */
    public final static LoggerHelper getBaseLogger() {
        return LoggerHelper.getLogger(BASE_LOGGER);
    }

    /**
     * MSA Procedure Logger
     * 
     * @return the logger to be used in the MSA procedure
     */
    public final static LoggerHelper getProcedureLogger() {
        return LoggerHelper.getLogger(MSA_PROCEDURE_LOGGER);
    }

    /**
     * MSA Setup logger
     * 
     * @return the logger to be used in the setup process of the MSA procedure
     */
    public final static LoggerHelper getSetupLogger() {
        return LoggerHelper.getLogger(MSA_SETUP_LOGGER);
    }

    /**
     * MSA Components logger
     * 
     * @return the logger to be used by the components of the MSA procedure
     */
    public final static LoggerHelper getComponentsLogger() {
        return LoggerHelper.getLogger(MSA_COMPONENTS_LOGGER);
    }

    /**
     * MSA Events logger
     * 
     * @return the logger to be used to event related logs of the MSA procedure
     */
    public final static LoggerHelper getEventsLogger() {
        return LoggerHelper.getLogger(MSA_EVENTS_LOGGER);
    }

    /**
     * MSA Simulation logger
     * 
     * @return the logger to be used to event related logs of the MSA simulation
     */
    public final static LoggerHelper getSimulationLogger() {
        return LoggerHelper.getLogger(MSA_SIMULATION_LOGGER);
    }
}
