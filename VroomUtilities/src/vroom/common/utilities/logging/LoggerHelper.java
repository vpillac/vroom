package vroom.common.utilities.logging;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.AppenderAttachable;

/**
 * This class wraps a <tt>org.apache.log4j.Logger</tt> and adds specific methods for the logging of formated strings
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 */
public class LoggerHelper implements AppenderAttachable {

    public static Layout                           DEFAULT_CONSOLE_LAYOUT = new PatternLayout(
                                                                                  "%d{HH:mm:ss} %-5p %-15c [%-10t] : %m%n");

    /**
     * Level for low debugging log messages. Useful for instance for detailed trace
     */
    public static final Level                      LEVEL_LOW_DEBUG        = Level.toLevel(Level.DEBUG_INT / 2);
    /** Level for common debugging log messages */
    public static final Level                      LEVEL_DEBUG            = Level.toLevel(Level.DEBUG_INT);
    /** Level for information messages */
    public static final Level                      LEVEL_INFO             = Level.toLevel(Level.INFO_INT);
    /** Level for warning messages */
    public static final Level                      LEVEL_WARN             = Level.toLevel(Level.WARN_INT);
    /**
     * Level for error messages, to be used for instance when catching an exception that interrupted a process
     */
    public static final Level                      LEVEL_ERROR            = Level.toLevel(Level.ERROR_INT);
    /** Level for fatal messages, to be used when a event stopped a process */
    public static final Level                      LEVEL_FATAL            = Level.toLevel(Level.FATAL_INT);

    /** The wrapped logger */
    private final Logger                           mLogger;

    private static final Map<String, LoggerHelper> LOGGERS                = new HashMap<String, LoggerHelper>();

    /** <code>true</code> if the stack trace should be logged for warn messages and higher */
    public static boolean                          LOG_STACK              = false;

    /**
     * Access to the underlying logger
     * 
     * @return the wrapped {@link Logger}
     */
    protected Logger getLogger() {
        return mLogger;
    }

    /**
     * Add the given <tt>appender</tt> to the wrapped logger
     * 
     * @param appender
     */
    @Override
    public void addAppender(Appender appender) {
        getLogger().addAppender(appender);
    }

    public Enumeration<?> getAllappenders() {
        return getLogger().getAllAppenders();
    }

    /**
     * Configure the logger with a default async appender
     * <p/>
     * Messages will be logged in the console using an {@link UnlimitedAsyncAppender} based on a {@link ConsoleAppender}
     * with the {@link #DEFAULT_CONSOLE_LAYOUT}.
     * 
     * @param loggerLevel
     *            the level of the root logger
     * @param appenderLevel
     *            the level of the async console appender that will be added to the logger. All log events with lower
     *            level will be ignored. If <code>null</code> default value of {@link #LEVEL_ERROR} will be used
     * @param async
     *            <code>true</code> if the default console appender should be wrapped in an
     *            {@link UnlimitedAsyncAppender}
     */
    public static void setupRootLogger(Level loggerLevel, Level appenderLevel, boolean async) {
        if (appenderLevel == null) {
            appenderLevel = LEVEL_ERROR;
        }

        ConsoleAppender appender = new ConsoleAppender(DEFAULT_CONSOLE_LAYOUT);
        appender.setThreshold(appenderLevel);

        Appender mainAppender;
        if (async) {
            // The main async appender for the logging system
            UnlimitedAsyncAppender asyncAppender = new UnlimitedAsyncAppender();

            asyncAppender.addAppender(appender);

            mainAppender = asyncAppender;
        } else {
            mainAppender = appender;
        }

        Logger.getRootLogger().addAppender(mainAppender);

        Logger.getRootLogger().setLevel(loggerLevel);
    }

    /**
     * Logging of a message
     * 
     * @param logger
     *            the logger to which the message will be sent
     * @param level
     *            the level of the message
     * @param message
     *            the message to be logged
     */
    private static void logMessage(Logger logger, Level level, Object message) {
        logMessage(logger, level, message, null);
    }

    /**
     * Logging of a message
     * 
     * @param logger
     *            the logger to which the message will be sent
     * @param level
     *            the level of the message
     * @param message
     *            the message to be logged
     */
    private static void logMessage(Logger logger, Level level, Object message, Throwable cause) {
        logger.log(level, message, cause);
    }

    /**
     * Logging of a formatted message
     * 
     * @param logger
     *            the logger to which the message will be sent
     * @param level
     *            the level of the message
     * @param format
     *            the format string
     * @param args
     *            the arguments of the format string
     */
    private static void logMessage(Logger logger, Level level, String format, Object... args) {
        logMessage(logger, level, format, null, args);
    }

    /**
     * Logging of a formatted message
     * 
     * @param logger
     *            the logger to which the message will be sent
     * @param level
     *            the level of the message
     * @param cause
     *            the {@link Throwable} that caused this logging
     * @param NUMBER_FORMAT
     *            the format string
     * @param args
     *            the arguments of the format string
     */
    private static void logMessage(Logger logger, Level level, String f, Throwable cause,
            Object... args) {
        if (logger.isEnabledFor(level)) {
            if (LOG_STACK && level.isGreaterOrEqual(LEVEL_WARN)) {
                f += "\nStack: %s";
                args = Arrays.copyOf(args, args.length + 1);
                args[args.length - 1] = Thread.currentThread().getStackTrace();
            }

            if (args == null || args.length == 0) {
                logger.log(level, f, cause);
            } else {
                logger.log(level, new FormattedLogMessage(f, args), cause);
            }
        }
    }

    /**
     * Private constructor for a logger wrapper
     * 
     * @param logger
     *            the logger to be wrapped
     */
    private LoggerHelper(Logger logger) {
        mLogger = logger;
    }

    /**
     * Shorthand for <code>getLogger(clazz.getName())</code>.
     * 
     * @param clazz
     *            The name of <code>clazz</code> will be used as the name of the logger to retrieve. See
     *            {@link #getLogger(String)} for more detailed information.
     */
    public static LoggerHelper getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    /**
     * Retrieve a logger named according to the value of the <code>name</code> parameter. If the named logger already
     * exists, then the existing instance will be returned. Otherwise, a new instance is created.
     * <p>
     * By default, loggers do not have a set level but inherit it from their neareast ancestor with a set level. This is
     * one of the central features of log4j.
     * 
     * @param name
     *            The name of the logger to retrieve.
     * @see Logger#getLogger(String)
     */
    public static LoggerHelper getLogger(String name) {
        LoggerHelper log = LOGGERS.get(name);
        if (log == null) {
            log = new LoggerHelper(Logger.getLogger(name));
            LOGGERS.put(name, log);
        }

        return log;
    }

    // ------------------------
    // DEBUG LOW
    // ------------------------

    /**
     * Static utility method used to log low level debug messages
     * 
     * @param object
     *            the object logging the message
     * @param format
     * @param args
     * @see #debug(String,Object[])
     */
    public static void lowDebugMessage(Object object, String format, Object... args) {
        getLogger(object.getClass()).debug(format, args);
    }

    /**
     * Append <tt>message</tt> with level {@link #LEVEL_LOW_DEBUG}
     * 
     * @param message
     *            the message to be logged
     */
    public void lowDebug(Object message) {
        logMessage(mLogger, LEVEL_LOW_DEBUG, message);
    }

    /**
     * Append a message defined by <tt>format</tt> and <tt>args</tt>, with level {@link #LEVEL_LOW_DEBUG}
     * 
     * @param format
     *            a formatting string to produce the message
     * @param args
     *            the arguments to be used with the formatted string
     * @see String#format(String, Object[])
     */
    public void lowDebug(String format, Object... args) {
        logMessage(mLogger, LEVEL_LOW_DEBUG, format, args);
    }

    // ------------------------
    // DEBUG
    // ------------------------

    /**
     * Static utility method used to log debug messages
     * 
     * @param object
     *            the object logging the message
     * @param format
     * @param args
     * @see #debug(String,Object[])
     */
    public static void debugMessage(Object object, String format, Object... args) {
        getLogger(object.getClass()).debug(format, args);
    }

    /**
     * Append <tt>message</tt> with level {@link #LEVEL_DEBUG}
     * 
     * @param message
     *            the message to be logged
     */
    public void debug(Object message) {
        logMessage(mLogger, LEVEL_DEBUG, message);
    }

    /**
     * Append a message defined by <tt>format</tt> and <tt>args</tt>, with level {@link #LEVEL_DEBUG}
     * 
     * @param format
     *            a formatting string to produce the message
     * @param args
     *            the arguments to be used with the formatted string
     * @see String#format(String, Object[])
     */
    public void debug(String format, Object... args) {
        logMessage(mLogger, LEVEL_DEBUG, format, args);
    }

    // ------------------------
    // INFO
    // ------------------------

    /**
     * Append <tt>message</tt> with level {@link #LEVEL_INFO}
     * 
     * @param message
     *            the message to be logged
     */
    public void info(Object message) {
        logMessage(mLogger, LEVEL_INFO, message);
    }

    /**
     * Append a message defined by <tt>format</tt> and <tt>args</tt>, with level {@link #LEVEL_INFO}
     * 
     * @param format
     *            a formatting string to produce the message
     * @param args
     *            the arguments to be used with the formatted string
     * @see String#format(String, Object[])
     */
    public void info(String format, Object... args) {
        logMessage(mLogger, LEVEL_INFO, format, args);
    }

    /**
     * Append <tt>message</tt> with level {@link #LEVEL_WARN}
     * 
     * @param message
     *            the message to be logged
     */
    public void warn(Object message) {
        logMessage(mLogger, LEVEL_WARN, message);
    }

    /**
     * Append a message defined by <tt>format</tt> and <tt>args</tt>, with level {@link #LEVEL_WARN}
     * 
     * @param format
     *            a formatting string to produce the message
     * @param args
     *            the arguments to be used with the formatted string
     * @see String#format(String, Object[])
     */
    public void warn(String format, Object... args) {
        logMessage(mLogger, LEVEL_WARN, format, args);
    }

    /**
     * Append a message defined by <tt>format</tt> and <tt>args</tt>, with level {@link #LEVEL_WARN}
     * 
     * @param format
     *            a formatting string to produce the message
     * @param cause
     *            the <code>Throwable</code> object that produced this message
     * @param args
     *            the arguments to be used with the formatted string
     * @see String#format(String, Object[])
     */
    public void warn(String format, Throwable cause, Object... args) {
        logMessage(mLogger, LEVEL_WARN, format, cause, args);
    }

    // ------------------------
    // ERROR
    // ------------------------

    /**
     * Append <tt>message</tt> with level {@link #LEVEL_ERROR}
     * 
     * @param message
     *            the message to be logged
     */
    public void error(Object message) {
        logMessage(mLogger, LEVEL_ERROR, message);
    }

    /**
     * Append a message defined by <tt>format</tt> and <tt>args</tt>, with level {@link #LEVEL_ERROR}
     * 
     * @param format
     *            a formatting string to produce the message
     * @param args
     *            the arguments to be used with the formatted string
     * @see String#format(String, Object[])
     */
    public void error(String format, Object... args) {
        logMessage(mLogger, LEVEL_ERROR, format, args);
    }

    /**
     * Append a message defined by <tt>format</tt> and <tt>args</tt>, with level {@link #LEVEL_ERROR}
     * 
     * @param format
     *            a formatting string to produce the message
     * @param cause
     *            the <code>Throwable</code> object that produced this message
     * @param args
     *            the arguments to be used with the formatted string
     * @see String#format(String, Object[])
     */
    public void error(String format, Throwable cause, Object... args) {
        logMessage(mLogger, LEVEL_ERROR, format, cause, args);
    }

    /**
     * Append an error message when an exception is caught of the form
     * <code>"Exception caught in [context] [cause.getMessage]"</code>
     * 
     * @param context
     *            a context string, can contain formating
     * @param cause
     *            the {@link Throwable} that was caught
     * @param args
     *            optional fomrating arguments for the context
     */
    public void exception(String context, Throwable cause, Object... args) {
        Object[] argst = new Object[args.length + 2];
        argst[argst.length - 2] = cause != null ? cause.getClass().getSimpleName() : "na";
        argst[argst.length - 1] = cause != null ? cause.getMessage() : "na";
        for (int i = 0; i < args.length; i++) {
            argst[i] = args[i];
        }
        logMessage(mLogger, LEVEL_ERROR, "Exception caught in " + context + " %s: %s", cause, argst);
    }

    /**
     * Append an error message when an exception is caught of the form
     * <code>"Exception caught in [context] [cause.getMessage]"</code>
     * 
     * @param context
     *            a context string, can contain formating
     * @param cause
     *            the {@link Throwable} that was caught
     * @param args
     *            optional fomrating arguments for the context
     */
    public void fatalException(String context, Throwable cause, Object... args) {
        Object[] argst = new Object[args.length + 2];
        argst[argst.length - 2] = cause != null ? cause.getClass().getSimpleName() : "na";
        argst[argst.length - 1] = cause != null ? cause.getMessage() : "na";
        for (int i = 0; i < args.length; i++) {
            argst[i] = args[i];
        }
        logMessage(mLogger, LEVEL_FATAL, "Exception caught in " + context + " %s: %s", cause, argst);
    }

    // ------------------------
    // FATAL
    // ------------------------

    /**
     * Append <tt>message</tt> with level {@link #LEVEL_FATAL}
     * 
     * @param message
     *            the message to be logged
     */
    public void fatal(Object message) {
        logMessage(mLogger, LEVEL_FATAL, message);
    }

    /**
     * Append a message defined by <tt>format</tt> and <tt>args</tt>, with level {@link #LEVEL_FATAL}
     * 
     * @param format
     *            a formatting string to produce the message
     * @param args
     *            the arguments to be used with the formatted string
     * @see String#format(String, Object[])
     */
    public void fatal(String format, Object... args) {
        logMessage(mLogger, LEVEL_FATAL, format, args);
    }

    /**
     * Set the level of the wrapped logger
     * 
     * @param level
     */
    public void setLevel(Level level) {
        getLogger().setLevel(level);
    }

    public boolean isEnabledFor(Level level) {
        return getLogger().isEnabledFor(level);
    }

    @Override
    public Enumeration<?> getAllAppenders() {
        return getLogger().getAllAppenders();
    }

    @Override
    public Appender getAppender(String name) {
        return getLogger().getAppender(name);
    }

    @Override
    public boolean isAttached(Appender appender) {
        return getLogger().isAttached(appender);
    }

    @Override
    public void removeAllAppenders() {
        getLogger().removeAllAppenders();

    }

    @Override
    public void removeAppender(Appender appender) {
        getLogger().removeAppender(appender);
    }

    @Override
    public void removeAppender(String name) {
        getLogger().removeAppender(name);
    }
}
