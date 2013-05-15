package vroom.common.utilities.logging;

import java.util.Enumeration;
import java.util.logging.Logger;

import org.apache.log4j.Level;

/**
 * <code>Logging</code> is an helper class that is used to configure and manage the {@link Logger} instances used in all
 * vroom packages.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 */
public class Logging {

    /** The base logger for any vroom logger */
    public static final String BASE_LOGGER  = "Main";
    /** The logger for vroom setup logs (definition of parameters) */
    public static final String SETUP_LOGGER = "Setup";

    /**
     * Setup the root logger with the given level
     * 
     * @param loggerLevel
     *            the level to be set for the root logger
     * @param appenderThreshold
     *            the threshold for the default asyn console appender
     * @param async
     *            <code>true</code> if the default console appender should be wrapped in an
     *            {@link UnlimitedAsyncAppender}
     * @see LoggerHelper#setupRootLogger(Level, Level, boolean)
     */
    public static void setupRootLogger(Level loggerLevel, Level appenderThreshold, boolean async) {
        LoggerHelper.setupRootLogger(loggerLevel, appenderThreshold, async);
    }

    /**
     * vroom base logger
     * 
     * @return the base (root) logger for the vroom framework
     */
    public static LoggerHelper getBaseLogger() {
        return LoggerHelper.getLogger(BASE_LOGGER);
    }

    /**
     * vroom Setup logger
     * 
     * @return the logger to be used in the setup process of the vroom procedure
     */
    public static LoggerHelper getSetupLogger() {
        return LoggerHelper.getLogger(SETUP_LOGGER);
    }

    /**
     * Sets the level for the {@link org.apache.log4j.Logger} with the given <code>loggerName</code>
     * 
     * @param loggerName
     *            the name of the logger for which the <code>level</code> will be set. <br/>
     *            Loggers names are defined in the {@link Logging} class.
     * @param level
     *            the desired level of logging for logger <code>loggerName</code> <br/>
     *            Logging levels are defined in the {@link LoggerHelper} class.
     * @see LoggerHelper#LEVEL_LOW_DEBUG
     * @see LoggerHelper#LEVEL_DEBUG
     * @see LoggerHelper#LEVEL_INFO
     * @see LoggerHelper#LEVEL_WARN
     * @see LoggerHelper#LEVEL_ERROR
     * @see LoggerHelper#LEVEL_FATAL
     */
    public final static void setLoggerLevel(String loggerName, Level level) {
        LoggerHelper.getLogger(loggerName).setLevel(level);
    }

    /**
     * Wait for any {@link UnlimitedAsyncAppender} attached to the {@link #getBaseLogger() base logger} to log their
     * messages
     * 
     * @param timeout
     *            the maximum time to wait (in milliseconds)
     * @throws InterruptedException
     */
    public static void awaitLogging(int timeout) {
        Enumeration<?> e = org.apache.log4j.Logger.getRootLogger().getAllAppenders();
        while (e.hasMoreElements()) {
            Object a = e.nextElement();
            if (a instanceof UnlimitedAsyncAppender)
                try {
                    ((UnlimitedAsyncAppender) a).awaitTermination(timeout);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
        }

    }
}
