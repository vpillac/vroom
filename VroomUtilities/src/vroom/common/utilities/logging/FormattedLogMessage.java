package vroom.common.utilities.logging;

import java.util.Collection;
import java.util.Collections;

import vroom.common.utilities.Constants;
import vroom.common.utilities.ILockable;
import vroom.common.utilities.Utilities;

/**
 * A convenience class to pass formatted string to loggers and defer the actual formatting to the appending time
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp" >SLP</a>, CTI
 */
public class FormattedLogMessage {
    private final String   mFormatString;
    private final Object[] mArgs;
    private String         mFormattedString;

    /**
     * Creates a new formated message based on the given <tt>formatString</tt> and <tt>args</tt>
     * 
     * @param formatString
     * @param args
     */
    public FormattedLogMessage(String formatString, Object[] args) {
        mFormatString = formatString;
        mArgs = args;
    }

    /**
     * @return the result of the formatting of the given format string with the given args
     */
    @Override
    public String toString() {
        try {
            if (mFormattedString == null) {
                for (int i = 0; i < mArgs.length; i++) {
                    if (mArgs[i] instanceof ILockable) {
                        if (!((ILockable) mArgs[i]).tryLock(1000)) {
                            mArgs[i] = Constants.TOSTRING_LOCKED;
                        }
                    }

                    if (mArgs[i] instanceof StackTraceElement[]) {
                        StackTraceElement[] stack = (StackTraceElement[]) mArgs[i];
                        StringBuilder b = new StringBuilder(stack.length * 20);

                        boolean first = true;
                        for (int j = 1; j < stack.length; j++) {
                            // Filter out logging stack
                            if (!first
                                    || !stack[j].getClassName().startsWith(LoggerHelper.class.getPackage().getName())) {
                                if (!first) {
                                    b.append("    ");
                                }
                                b.append(String.format("%s.%s (%s %s)", stack[j].getClassName(),
                                        stack[j].getMethodName(), stack[j].getFileName(), stack[j].getLineNumber()));
                                b.append("\n");
                                first = false;
                            }
                        }

                        if (mArgs[i] instanceof Collections) {
                            mArgs[i] = Utilities.toShortString((Collection<?>) mArgs[i]);
                        } else {
                            mArgs[i] = b.toString();
                        }
                    }
                }

                mFormattedString = String.format(mFormatString, mArgs);

                for (Object mArg : mArgs) {
                    if (mArg instanceof ILockable) {
                        ((ILockable) mArg).releaseLock();
                    }
                }
            }

            return mFormattedString;
        } catch (Exception e) {
            return "Exception caught while formating " + mFormatString + " (" + e.toString() + ")";
        }
    }

}
