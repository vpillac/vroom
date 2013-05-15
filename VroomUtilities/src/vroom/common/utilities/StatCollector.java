/**
 *
 */
package vroom.common.utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Format;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import vroom.common.utilities.logging.LoggerHelper;

/**
 * <code>StatCollector</code> is a class that allows the collection of statistic information and their output in a CSV
 * text file.
 * <p>
 * Creation date: Jul 8, 2010 - 10:14:16 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class StatCollector {

    /**
     * Getter for this class logger
     * 
     * @return the logger associated with this class
     */
    public static LoggerHelper getLogger() {
        return LoggerHelper.getLogger(StatCollector.class);
    }

    private static String sNumberFormat = "###0.0000";

    /**
     * Set the default format number to be used
     * 
     * @param format
     *            a string of the form <code>###0.0000</code> defining the formating of decimal numbers
     */
    public static void setDefaultNumberFormat(String format) {
        sNumberFormat = format;
    }

    /**
     * Returns the default number format to be used
     * 
     * @return the default number format to be used
     * @see #setDefaultNumberFormat(String)
     */
    public static String getDefaultNumberFormat() {
        return sNumberFormat;
    }

    public static String         sCVSSeparator      = ";";
    public static char           sDecimalSeparator  = '.';
    public static char           sGroupingSeparator = ',';
    public static String         sCommentsPrefix    = "#==================== COMMENTS ====================\n";
    public static String         sCommentsSuffix    = "\n#==================================================\n\n";

    private BufferedWriter       mWriter;

    private boolean              mAutoFlush;

    private final Label<?>[]     mLabels;

    private final List<Object[]> mValues;

    private final DecimalFormat  mFormat;
    private final String         mComment;

    /**
     * Creates a new <code>StatCollector</code> with no file attached
     * 
     * @param comment
     *            an optional comment inserted at the beginning of the ouput file
     * @param labels
     *            the labels for collected data
     * @see #setFile(File, boolean)
     */
    public StatCollector(String comment, Label<?>... labels) {
        this(null, false, false, comment, labels);
    }

    /**
     * Creates a new <code>StatCollector</code>
     * 
     * @param output
     *            the file in which stats will be recorded
     * @param autoFlush
     *            <code>true</code> if stats should be written immediatly in the file
     * @param append
     *            <code>true</code> if statistics should be appended to the file, <code>false</code> to erase previous
     *            content
     * @param comment
     *            an optional comment inserted at the beginning of the ouput file
     * @param labels
     *            the labels for collected data
     */
    public StatCollector(File output, boolean autoFlush, boolean append, String comment,
            Label<?>... labels) {
        if (labels == null) {
            throw new IllegalArgumentException("Argument labels cannot be null");
        }

        mLabels = labels;
        for (int i = 0; i < labels.length; i++) {
            mLabels[i].id = i;
        }

        mValues = new LinkedList<Object[]>();

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setDecimalSeparator(sDecimalSeparator);
        symbols.setGroupingSeparator(sGroupingSeparator);

        mFormat = new DecimalFormat(sNumberFormat, symbols);

        mValues.clear();

        mComment = comment;

        setFile(output, autoFlush, append);

    }

    /**
     * Statistic collection.
     * <p>
     * The <code>stats</code> should be given in the same order as the labels were defined
     * </p>
     * 
     * @param stats
     *            the statistics to be collected
     */
    public synchronized void collect(Object... stats) {
        if (stats.length != mLabels.length) {
            throw new IllegalArgumentException(
                    "The array stats should have the same length as the labels");
        }

        for (int i = 0; i < stats.length; i++) {
            if (stats[i] != null && !mLabels[i].mValueClass.isAssignableFrom(stats[i].getClass())) {
                throw new IllegalArgumentException(String.format(
                        "Unexpected data type for label #%s - %s: expected %s but was %s", i,
                        mLabels[i].mName, mLabels[i].mValueClass.getSimpleName(), stats[i]
                                .getClass().getSimpleName()));
            }
        }

        mValues.add(stats);

        write(false, stats);
    }

    /**
     * Return the labels defined in this instance
     * 
     * @return an array containing the defined labels
     */
    public Label<?>[] getLabels() {
        return mLabels;
    }

    /**
     * Return the values collected for a given label
     * 
     * @param <V>
     *            the type of data
     * @param label
     *            the label to lookup
     * @return an array containing the values collected so far
     */
    @SuppressWarnings("unchecked")
    public <V> V[] getValues(Label<V> label) {
        V[] values = (V[]) new Object[mValues.size()];

        int i = 0;
        for (Object[] val : mValues) {
            values[i++] = (V) val[label.id];
        }

        return values;
    }

    public String getSatString(Object... stats) {
        if (stats.length != mLabels.length) {
            throw new IllegalArgumentException(
                    "The array stats should have the same length as the labels");
        }

        StringBuilder b = new StringBuilder(stats.length * 10);
        for (int i = 0; i < stats.length; i++) {
            b.append(mLabels[i].getName());
            b.append("=");
            b.append(toString(mLabels[i], stats[i]));
            if (i < stats.length - 1)
                b.append(", ");
        }

        return b.toString();
    }

    /**
     * Set the output file.
     * 
     * @param output
     *            the file in which stats will be recorded
     * @param autoflush
     *            <code>true</code> if stats should be written immediatly in the file
     */
    public synchronized void setFile(File output, boolean autoflush, boolean append) {
        boolean writeheader = (output != null) && (!output.exists() || !append);
        BufferedWriter writer = null;
        if (output != null) {
            try {
                writer = new BufferedWriter(new FileWriter(output, append));
            } catch (IOException e) {
                getLogger().exception("StatCollector.setFile", e);
                writer = null;
            }
        }
        mWriter = writer;
        mAutoFlush = writer != null && autoflush;

        if (writeheader) {
            // Write comments
            write("%s%s%s", sCommentsPrefix, mComment, sCommentsSuffix);
            // Write column labels (headers)
            write(true, (Object[]) mLabels);
        }
    }

    /**
     * Write a formated string, used for comments
     * 
     * @param format
     * @param args
     */
    protected synchronized void write(String format, Object... args) {
        if (mWriter == null) {
            return;
        }

        try {
            mWriter.write(String.format(format, args));
            mWriter.newLine();
            if (mAutoFlush) {
                mWriter.flush();
            }
        } catch (IOException e) {
            getLogger().exception("StatCollector.write", e);
        }
    }

    /**
     * Write an array of values separated by {@link #sCVSSeparator}
     * 
     * @param header
     *            {@code true} if the objects are headers of the columns
     * @param values
     *            the values to be writen
     */
    protected synchronized void write(boolean header, Object... values) {
        if (mWriter == null) {
            return;
        }

        StringBuilder b = new StringBuilder();
        for (int v = 0; v < values.length; v++) {
            if (header)
                b.append(mLabels[v].getName());
            else
                b.append(toString(mLabels[v], values[v]));

            if (v < values.length - 1)
                b.append(sCVSSeparator);

        }

        try {
            mWriter.write(b.toString());
            mWriter.newLine();
            if (mAutoFlush) {
                mWriter.flush();
            }
        } catch (IOException e) {
            getLogger().exception("StatCollector.write", e);
        }
    }

    public String toString(Label<?> label, Object val) {
        if (val == null)
            return "null";
        if ((val instanceof Double && Double.isNaN((double) val))
                || (val instanceof Float && Float.isNaN((float) val)))
            return "-";
        else if (label.mFormat != null)
            return label.mFormat.format(val);
        else if (val instanceof Double || val instanceof Float)
            return Constants.isZero(((Number) val).doubleValue()) ? mFormat.format(0) : mFormat
                    .format(val);
        else
            return val.toString();
    }

    /**
     * Write the collected stats to the file
     */
    public synchronized void flush() {
        try {
            mWriter.flush();
        } catch (IOException e) {
            getLogger().exception("StatCollector.write", e);
        }
    }

    /**
     * Close the underlying file writer
     */
    public synchronized void close() {
        try {
            mWriter.close();
        } catch (IOException e) {
            getLogger().exception("StatCollector.write", e);
        }
    }

    public static class Label<V extends Object> {

        private final String   mName;
        private final Class<V> mValueClass;
        private int            id;
        private final Format   mFormat;

        /**
         * Getter for <code>name</code>
         * 
         * @return the name
         */
        public String getName() {
            return mName;
        }

        /**
         * Getter for <code>valueClass</code>
         * 
         * @return the valueClass
         */
        public Class<V> getValueClass() {
            return mValueClass;
        }

        /**
         * Creates a new <code>Label</code> using default formatting
         * 
         * @param label
         *            the name of the label
         * @param valueClass
         *            the type of data that will be collected
         */
        public Label(String label, Class<V> valueClass) {
            this(label, valueClass, null);
        }

        /**
         * Creates a new <code>Label</code> using a specific formatting
         * 
         * @param label
         *            the name of the label
         * @param valueClass
         *            the type of data that will be collected
         * @param format
         *            the format used to format the values (can be <code>null</code>)
         */
        public Label(String label, Class<V> valueClass, Format format) {
            mName = label;
            mValueClass = valueClass;
            mFormat = format;
            id = -1;
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return mName;
        }
    }

}
