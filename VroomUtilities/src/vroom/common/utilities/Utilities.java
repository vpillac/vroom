package vroom.common.utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.AbstractList;
import java.util.AbstractSequentialList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import umontreal.iro.lecuyer.rng.RandomPermutation;
import umontreal.iro.lecuyer.rng.RandomStream;
import vroom.common.utilities.dataModel.IObjectWithID;
import vroom.common.utilities.math.QuickSelect;

/**
 * Creation date: Apr 5, 2010 - 5:58:47 PM<br/>
 * <code>Utilities</code> is a utility class containing miscellaneous static methods
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public abstract class Utilities {

    /**
     * The class <code>Time</code> is a collection of utility method to deal with time
     * <p>
     * Creation date: Mar 27, 2012 - 4:51:30 PM.
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    public abstract static class Time {

        /**
         * Returns the current date time in form of a string <code>yymmdd_hh-mm</code>.
         * 
         * @return the current date time in form of a string <code>yymmdd_hh-mm</code>
         */
        public static String getDateString() {
            return String.format("%1$ty%1$tm%1$td_%1$tH-%1$tM",
                    new Date(System.currentTimeMillis()));
        }

        /**
         * Converts a duration in s in a human friendly string.
         * 
         * @param time
         *            the duration to be converted
         * @param maxDigits
         *            the number of digits to show
         * @param showMS
         *            <code>true</code> if ms should be displayed, <code>false</code> otherwise
         * @param showAll
         *            <code>false</code> to show only non-null values
         * @return a string of format <code>"d h m s ms"</code>, or <code>"na"</code> if <code>time</code> is negative
         */
        public static String secondsToString(double time, int maxDigits, boolean showMS,
                boolean showAll) {
            return millisecondsToString((long) (time * 1000), maxDigits, showMS, showAll);
        }

        /**
         * Converts a duration in ms in a human friendly string.
         * 
         * @param time
         *            the duration to be converted
         * @param maxDigits
         *            the number of digits to show
         * @param showMS
         *            <code>true</code> if ms should be displayed, <code>false</code> otherwise
         * @param showAll
         *            <code>false</code> to show only non-null values
         * @return a string of format <code>"d h m s ms"</code>, or <code>"na"</code> if <code>time</code> is negative
         */
        public static String millisecondsToString(long time, int maxDigits, boolean showMS,
                boolean showAll) {
            if (time < 0) {
                return "na";
            }

            int[] duration = Time.decomposeMillis(time);
            String[] labels = { "d", "h", "m", "s", "ms" };

            boolean force = showAll;

            StringBuilder sb = new StringBuilder(20);
            int digits = 0;
            for (int i = 0; i < duration.length; i++) {
                if (force || (i < duration.length - 1 || showMS || digits == 0)
                        && digits < maxDigits && duration[i] > 0) {
                    if (i != 0) {
                        if (i == 4 && duration[i] < 10)
                            sb.append("  ");
                        else if (duration[i] < 10
                                || (i == 4 && duration[i] < 100 && duration[i] >= 10))
                            sb.append(" ");
                    }
                    sb.append(duration[i]);
                    sb.append(labels[i]);
                    digits++;
                }
            }
            if (sb.length() == 0)
                return "0ms";
            else
                return sb.toString();
        }

        /** The Constant sVMStartDateString. */
        private static final String sVMStartDateString = getDateString();

        /**
         * Returns the date time at the start of the jVM in form of a string <code>yymmdd_hh-mm</code>.
         * 
         * @return the date time at the start of the jVM in form of a string <code>yymmdd_hh-mm</code>
         */
        public static String getVMStartDateString() {
            return sVMStartDateString;
        }

        /** The Constant TIME_STAMP_FORMAT. */
        public static final SimpleDateFormat TIME_STAMP_FORMAT = new SimpleDateFormat("HH:mm:ss");

        /**
         * Convert a duration in ns in an array containing the corresponding number of days, hours, minutes, seconds,
         * milliseconds, and nanoseconds.
         * 
         * @param time
         *            the duration to be converted
         * @return <code>[days,hours,minutes,seconds,milliseconds]</code>
         */
        public static int[] decomposeNanos(long time) {

            int[] duration = new int[6];

            if (time < 0) {
                return duration;
            }

            // Days
            duration[0] = (int) (time / Stopwatch.NS_IN_DAY);
            time -= duration[0] * Stopwatch.NS_IN_DAY;

            // Hours
            duration[1] = (int) (time / Stopwatch.NS_IN_HOUR);
            time -= duration[1] * Stopwatch.NS_IN_HOUR;

            // Minutes
            duration[2] = (int) (time / Stopwatch.NS_IN_MIN);
            time -= duration[2] * Stopwatch.NS_IN_MIN;

            // Seconds
            duration[3] = (int) (time / Stopwatch.NS_IN_S);
            time -= duration[3] * Stopwatch.NS_IN_S;

            // Milliseconds
            duration[4] = (int) (time / Stopwatch.NS_IN_MS);
            time -= duration[4] * Stopwatch.NS_IN_S;

            // Nanoseconds
            duration[5] = (int) time;

            return duration;
        }

        /**
         * Convert a duration in milliseconds in an array containing the corresponding number of days, hours, minutes,
         * seconds and milliseconds.
         * 
         * @param time
         *            the duration to be converted
         * @return <code>[days,hours,minutes,seconds,milliseconds]</code>
         */
        public static int[] decomposeMillis(long time) {

            int[] duration = new int[5];

            if (time < 0) {
                return duration;
            }

            // Days
            duration[0] = (int) time / Stopwatch.MS_IN_DAY;
            time -= duration[0] * Stopwatch.MS_IN_DAY;

            // Hours
            duration[1] = (int) time / Stopwatch.MS_IN_HOUR;
            time -= duration[1] * Stopwatch.MS_IN_HOUR;

            // Minutes
            duration[2] = (int) time / Stopwatch.MS_IN_MIN;
            time -= duration[2] * Stopwatch.MS_IN_MIN;

            // Seconds
            duration[3] = (int) time / 1000;
            time -= duration[3] * 1000;

            // Milliseconds
            duration[4] = (int) time;

            return duration;
        }

    }

    /**
     * <code>Random</code> is a collection of utility methods to deal with randomization
     * <p>
     * Creation date: Nov 8, 2011 - 11:41:26 AM.
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp" >SLP</a>
     * @version 1.0
     */
    public abstract static class Random {

        /**
         * Draw a {@code  sampleSize} indexes in the set <code>{0,...,setSize-1}</code>
         * 
         * @param setSize
         *            the original set size
         * @param sampleSize
         *            the number of indexes to draw
         * @param stream
         *            the random stream to be used
         * @return an array containing {@code  sampleSize} indexes in the set <code>{0,...,setSize-1}</code>
         */
        public static int[] randomIndexes(int setSize, int sampleSize, RandomStream stream) {
            if (sampleSize < 1)
                return new int[0];

            int[] indexes = new int[setSize];
            for (int i = 0; i < indexes.length; i++) {
                indexes[i] = i;
            }

            if (sampleSize >= setSize)
                return indexes;

            RandomPermutation.shuffle(indexes, stream);
            indexes = Arrays.copyOf(indexes, sampleSize);
            Arrays.sort(indexes);
            return indexes;
        }

        /**
         * Draw a sample from a collection.
         * 
         * @param <E>
         *            the element type
         * @param <C>
         *            the generic type
         * @param collection
         *            the collection
         * @param sampleSize
         *            the sample size
         * @param stream
         *            the stream
         * @return a sample of size {@code  sampleSize} drawn from {@code  collection}
         */
        public static <E, C extends Collection<E>> ArrayList<E> sample(C collection,
                int sampleSize, RandomStream stream) {
            if (collection == null || stream == null)
                throw new NullPointerException();

            int[] indexes = randomIndexes(collection.size(), sampleSize, stream);

            if (sampleSize >= collection.size())
                return new ArrayList<E>(collection);

            ArrayList<E> sample = new ArrayList<E>();
            if (!AbstractSequentialList.class.isAssignableFrom(collection.getClass())
                    && AbstractList.class.isAssignableFrom(collection.getClass())) {
                // Faster implementation for random access lists
                AbstractList<E> list = (AbstractList<E>) collection;
                for (int i : indexes)
                    sample.add(list.get(i));
            } else {
                Iterator<E> it = collection.iterator();
                int i = 0, itIndex = 0;
                while (it.hasNext() && i < indexes.length) {
                    E e = it.next();
                    if (itIndex == indexes[i]) {
                        sample.add(e);
                        i++;
                    }
                    itIndex++;
                }
            }

            return sample;
        }
    }

    /**
     * The class <code>SignificantPrecision</code> provides comparison methods to take into account precision issues
     * using the notion of significant numbers
     * <p>
     * Creation date: 20/04/2013 - 12:38:09 PM
     * 
     * @author vpillac, <a href="http://www.nicta.com.au">National ICT Australia</a>
     * @version 1.0
     */
    public static final class SignificantPrecision {
        /** the number of significant digits to consider **/
        private static int sSignificantDigits;

        /**
         * Getter for the number of significant digits to consider
         * 
         * @return the number of significant digits
         */
        public static int getSignificantDigits() {
            return sSignificantDigits;
        }

        /**
         * Setter for the number of significant digits to consider
         * 
         * @param digits
         *            the number of significant digits
         */
        public static void setSignificantDigits(int digits) {
            if (digits < 0)
                throw new IllegalArgumentException("The number of digits must be positive");
            sSignificantDigits = digits;
        }

        /**
         * Round a decimal value according to the {@linkplain #getSignificantDigits() number of significant digits}
         * using {@link RoundingMode#HALF_EVEN}.
         * 
         * @param d
         *            the decimal to be rounded
         * @return {@code  d} rounded with the correct number of digits
         * @author vpillac
         * @see Math#round(double, double, RoundingMode)
         */
        public static double round(double d) {
            return Math.roundSignificant(d, getSignificantDigits());
        }

        /**
         * Returns {@code true} iif {@code  round(val1)} is equal to {@code  round(val2)}
         * 
         * @param val1
         *            the val1
         * @param val2
         *            the val2
         * @return {@code true} if <code>round(val1)==round(val2)</code>
         * @see #getZeroTolerance()
         */
        public static boolean equals(double val1, double val2) {
            return round(val1) == round(val2) || (Double.isNaN(val1) && Double.isNaN(val2));
        }

        /**
         * Returns true if {@code  round(val1)} is lower or equal to {@code  round(val2)}
         * 
         * @param val1
         *            the val1
         * @param val2
         *            the val2
         * @return {@code true} if <code>round(val1) &leq; round(val2)</code>
         * @see #getZeroTolerance()
         */
        public static boolean isLowerEqual(double val1, double val2) {
            return round(val1) <= round(val2);
        }

        /**
         * Returns true if {@code  round(val1)} is greater or equal to {@code  round(val2)}
         * 
         * @param val1
         *            the val1
         * @param val2
         *            the val2
         * @return {@code true} if <code>round(val1) &geq; round(val2)</code>
         * @see #getZeroTolerance()
         */
        public static boolean isGreaterEqual(double val1, double val2) {
            return round(val1) >= round(val2);
        }

        /**
         * Returns true if {@code  round(val1)} is strictly greater than {@code  round(val2)}
         * 
         * @param val1
         *            the val1
         * @param val2
         *            the val2
         * @return {@code true} if <code>round(val1) > round(val2)</code>
         * @see #getZeroTolerance()
         */
        public static boolean isStrictlyGreaterThan(double val1, double val2) {
            return round(val1) > round(val2);
        }

        /**
         * Returns true if {@code  round(val1)} is strictly lower than {@code  round(val2)}
         * 
         * @param val1
         *            the val1
         * @param val2
         *            the val2
         * @return {@code true} if <code>round(val1) < round(val2)</code>
         * @see #getZeroTolerance()
         */
        public static boolean isStrictlyLowerThan(double val1, double val2) {
            return round(val1) < round(val2);
        }

        /**
         * Returns {@code true} if {@code  round(val)} is strictly positive
         * 
         * @param val
         *            the value to be tested
         * @return {@code true} if {@code  round(val)} is strictly positive
         */
        public static boolean isStrictlyPositive(double val) {
            return round(val) > 0;
        }

        /**
         * Returns {@code true} if {@code  round(val)} is greater or equal to zero
         * 
         * @param val
         *            the value to be tested
         * @return {@code true} if {@code  round(val)} is positive
         */
        public static boolean isPositive(double val) {
            return round(val) >= 0;
        }

        /**
         * Returns {@code true} if {@code  round(val)} is equal to zero
         * 
         * @param val
         *            the value to be tested
         * @return {@code true} if {@code  round(val)} is equal to zero
         */
        public static boolean isZero(double val) {
            return round(val) == 0;
        }

    }

    /**
     * The class <code>AbsolutePrecision</code> provides comparison methods to take into account precision issues using
     * an absolute precision measure
     * <p>
     * Creation date: 06/03/2013 - 5:53:25 PM
     * 
     * @author vpillac, <a href="http://www.nicta.com.au">National ICT Australia</a>
     * @version 1.0
     */
    public static final class AbsolutePrecision {
        /** A tolerance for zero values: values that are in absolute lower than this value are considered as zero. */
        private static double sZeroTolerance = 1E-9;

        /**
         * Getter for <code>zeroTolerance</code>, default value is {@code 1E-9}.
         * 
         * @return the zeroTolerance
         */
        public static double getZeroTolerance() {
            return sZeroTolerance;
        }

        /**
         * Setter for <code>zeroTolerance</code>, default value is {@code 1E-9}.
         * 
         * @param zeroTolerance
         *            the zeroTolerance to set
         */
        public static void setZeroTolerance(double zeroTolerance) {
            sZeroTolerance = zeroTolerance;
        }

/**
         * Returns true if {@code  val1} is equal to {@code  val2} allowing the specified {@linkplain #getZeroTolerance().
         *
         * @param val1 the val1
         * @param val2 the val2
         * @return {@code true} if <code>|val2-val1| &leq; &epsilon;</code>
         * @see #getZeroTolerance()
         */
        public static boolean equals(double val1, double val2) {
            return Double.doubleToLongBits(val1) == Double.doubleToLongBits(val2)
                    || java.lang.Math.abs(val2 - val1) <= getZeroTolerance()
                    || (Double.isNaN(val1) && Double.isNaN(val2));
        }

        /**
         * Returns true if {@code  val1} is lower or equal to {@code  val2} allowing the specified
         * {@linkplain #getZeroTolerance() zero tolerance}.
         * 
         * @param val1
         *            the val1
         * @param val2
         *            the val2
         * @return {@code true} if <code>val1 &leq; val2 + &epsilon;</code>
         * @see #getZeroTolerance()
         */
        public static boolean isLowerEqual(double val1, double val2) {
            return val1 <= val2 + getZeroTolerance();
        }

        /**
         * Returns true if {@code  val1} is greater or equal to {@code  val2} allowing the specified
         * {@linkplain #getZeroTolerance() zero tolerance}.
         * 
         * @param val1
         *            the val1
         * @param val2
         *            the val2
         * @return {@code true} if <code>val1 &geq; val2 - &epsilon;</code>
         * @see #getZeroTolerance()
         */
        public static boolean isGreaterEqual(double val1, double val2) {
            return val1 >= val2 - getZeroTolerance();
        }

        /**
         * Returns true if {@code  val1} is strictly greater than {@code  val2} considering the specified
         * {@linkplain #getZeroTolerance() zero tolerance}.
         * 
         * @param val1
         *            the val1
         * @param val2
         *            the val2
         * @return {@code true} if <code>val1 > val2 + &epsilon;</code>
         * @see #getZeroTolerance()
         */
        public static boolean isStrictlyGreaterThan(double val1, double val2) {
            return val1 > val2 + getZeroTolerance();
        }

        /**
         * Returns true if {@code  val1} is strictly lower than {@code  val2} considering the specified
         * {@linkplain #getZeroTolerance() zero tolerance}.
         * 
         * @param val1
         *            the val1
         * @param val2
         *            the val2
         * @return {@code true} if <code>val1 < val2 - &epsilon;</code>
         * @see #getZeroTolerance()
         */
        public static boolean isStrictlyLowerThan(double val1, double val2) {
            return val1 < val2 - getZeroTolerance();
        }

        /**
         * Returns {@code true} if {@code  val} is strictly greater than the specified {@linkplain #getZeroTolerance()
         * zero tolerance}.
         * 
         * @param val
         *            the value to be tested
         * @return {@code true} if {@code  val} is strictly positive
         */
        public static boolean isStrictlyPositive(double val) {
            return val > getZeroTolerance();
        }

        /**
         * Returns {@code true} if {@code  val} is greater than minus the specified {@linkplain #getZeroTolerance() zero
         * tolerance}.
         * 
         * @param val
         *            the value to be tested
         * @return {@code true} if {@code  val} is positive
         */
        public static boolean isPositive(double val) {
            return val >= -getZeroTolerance();
        }

        /**
         * Returns {@code true} if {@code  |val|} is equal to minus the specified {@linkplain #getZeroTolerance() zero
         * tolerance}.
         * 
         * @param val
         *            the value to be tested
         * @return {@code true} if {@code  val} is equal to zero
         */
        public static boolean isZero(double val) {
            return java.lang.Math.abs(val) <= getZeroTolerance();
        }

        /**
         * Round a decimal value according to the {@linkplain #getZeroTolerance() zero tolerance} using
         * {@link RoundingMode#HALF_EVEN}.
         * 
         * @param d
         *            the decimal to be rounded
         * @return {@code  d} rounded according to the {@linkplain #getZeroTolerance() zero tolerance}
         * @author vpillac
         * @see Math#round(double, double, RoundingMode)
         */
        public static double round(double d) {
            return Math.round(d, getZeroTolerance(), RoundingMode.HALF_EVEN);
        }

    }

    /**
     * <code>Math</code> is a collection of utility methods
     * <p>
     * Creation date: May 18, 2011 - 2:47:11 PM.
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp" >SLP</a>
     * @version 1.0
     */
    public final static class Math {

        /**
         * <code>DeviationMeasure</code> is an enumeration of the different types of deviation measures that can be used
         * to measure the difference between values and their mean
         * <p>
         * Creation date: May 18, 2011 - 2:58:09 PM.
         * 
         * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
         *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de
         *         Nantes</a>-<a href ="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp"> SLP</a>
         * @version 1.0
         */
        public static enum DeviationMeasure {

            /** Maximum absolute deviation. */
            MaxAbsDev,

            /** Average absolute deviation. */
            AvgAbsDev,

            /** Median absolute deviation. */
            MedAbsDev,

            /** Variance. */
            Var,

            /** Standard deviation. */
            StdDev,

            /** Maximum value, a proxy for the deviation. */
            Max,

            /** Minimum value, a proxy for the deviation. */
            Min,

            /** Difference between max value and min value. */
            MaxMinGap
        }

        /**
         * Returns the maximum value of an array.
         * 
         * @param values
         *            an array of value
         * @return the maximum of all elements of <code>values</code>
         */
        public static int maxInt(int... values) {
            if (values.length == 0)
                throw new IllegalArgumentException("Must have at least one value");
            int max = Integer.MIN_VALUE;
            for (int i : values) {
                if (i > max)
                    max = i;
            }
            return max;
        }

        /**
         * Returns the maximum value of an array.
         * 
         * @param values
         *            an array of value
         * @return the maximum of all elements of <code>values</code>
         */
        public static long maxLong(long... values) {
            if (values.length == 0)
                throw new IllegalArgumentException("Must have at least one value");
            long max = Long.MIN_VALUE;
            for (long i : values) {
                if (i > max)
                    max = i;
            }
            return max;
        }

        /**
         * Returns the maximum value of an array.
         * 
         * @param values
         *            an array of value
         * @return the maximum of all elements of <code>values</code>
         */
        public static double maxDouble(double... values) {
            if (values.length == 0)
                throw new IllegalArgumentException("Must have at least one value");
            double max = Double.NEGATIVE_INFINITY;
            for (double i : values) {
                if (i > max)
                    max = i;
            }
            return max;
        }

        /**
         * Returns the index and minimum value of an array.
         * 
         * @param values
         *            an array of values
         * @return an array [argmin, min value]
         */
        public static double[] argMin(double... values) {
            if (values.length == 0)
                throw new IllegalArgumentException("Must have at least one value");
            double min = Integer.MAX_VALUE;
            int argMin = 0;
            for (int i = 0; i < values.length; i++) {
                if (values[i] < min) {
                    min = values[i];
                    argMin = i;
                }
            }
            return new double[] { argMin, min };
        }

        /**
         * Returns the maximum value of an array.
         * 
         * @param <T>
         *            the generic type
         * @param values
         *            an array of value
         * @return the maximum of all elements of <code>values</code>
         */
        public static <T extends Comparable<? super T>> T max(T[] values) {
            if (values.length == 0)
                throw new IllegalArgumentException("Must have at least one value");
            T max = null;
            for (T i : values) {
                if (i != null && (max == null || i.compareTo(max) > 0))
                    max = i;
            }
            return max;
        }

        /**
         * Returns the maximum value of a collection.
         * 
         * @param <T>
         *            the generic type
         * @param values
         *            an array of value
         * @return the maximum of all elements of <code>values</code>
         */
        public static <T extends Comparable<? super T>> T max(Collection<T> values) {
            if (values.isEmpty())
                throw new IllegalArgumentException("Must have at least one value");
            T max = null;
            for (T i : values) {
                if (i != null && (max == null || i.compareTo(max) > 0))
                    max = i;
            }
            return max;
        }

        /**
         * Returns the <code>k</code> biggest values of an array.
         * 
         * @param <T>
         *            the generic type
         * @param values
         *            an array of value
         * @param k
         *            the number of elements to extract
         * @return the <code>n</code> biggest elements of <code>values</code> sorted in ascending order
         * @see QuickSelect#max(Comparable[], int, boolean, boolean)
         */
        public static <T extends Comparable<? super T>> T[] max(T[] values, int k) {
            if (values.length == 0 || k == 0)
                return Arrays.copyOf(values, 0);

            if (k == 1) {
                // More efficient implementation
                T[] r = Arrays.copyOf(values, 1);
                r[0] = max(values);
                return r[0] != null ? r : Arrays.copyOf(values, 0);
            }

            if (k >= values.length) {
                int nonNull = 0;
                for (T v : values)
                    if (v != null)
                        nonNull++;

                T[] r = Arrays.copyOf(values, nonNull);
                int i = 0;
                for (T v : values)
                    if (v != null)
                        r[i++] = v;
                Arrays.sort(r);
                return r;
            }

            return QuickSelect.max(values, k, true, true);
        }

        /**
         * Returns the <code>k</code> biggest values of an array.
         * 
         * @param values
         *            an array of double
         * @param k
         *            the number of elements to extract
         * @return the <code>n</code> biggest elements of <code>values</code> sorted in ascending order
         * @see QuickSelect#max(Comparable[], int, boolean, boolean)
         */
        public static double[] max(double[] values, int k) {
            if (values.length == 0)
                return new double[0];
            if (k == 1) {
                // More efficient implementation
                double[] r = Arrays.copyOf(values, 1);
                r[0] = maxDouble(values);
                return r[0] != Double.NEGATIVE_INFINITY ? r : new double[0];
            }

            if (k >= values.length) {
                double[] r = Arrays.copyOf(values, values.length);
                Arrays.sort(r);
                return r;
            }

            Double[] clone = new Double[values.length];
            for (int i = 0; i < clone.length; i++) {
                clone[i] = values[i];
            }
            Double[] rD = QuickSelect.max(clone, k, false, true);
            double[] r = new double[rD.length];
            for (int i = 0; i < r.length; i++) {
                r[i] = rD[i];
            }
            return r;
        }

        /**
         * Returns the minimum value of an array.
         * 
         * @param values
         *            an array of values
         * @return the minimum of all elements of <code>values</code>
         */
        public static int minInt(int... values) {
            if (values.length == 0)
                throw new IllegalArgumentException("Must have at least one value");
            int min = Integer.MAX_VALUE;
            for (int i : values) {
                if (i < min)
                    min = i;
            }
            return min;
        }

        /**
         * Returns the index and minimum value of an array.
         * 
         * @param values
         *            an array of values
         * @return an array [argmin, min value]
         */
        public static int[] argMin(int... values) {
            if (values.length == 0)
                throw new IllegalArgumentException("Must have at least one value");
            int min = Integer.MAX_VALUE;
            int argMin = 0;
            for (int i = 0; i < values.length; i++) {
                if (values[i] < min) {
                    min = values[i];
                    argMin = i;
                }
            }
            return new int[] { argMin, min };
        }

        /**
         * Returns the maximum value of an array.
         * 
         * @param values
         *            an array of value
         * @return the maximum of all elements of <code>values</code>
         */
        public static long minLong(long... values) {
            if (values.length == 0)
                throw new IllegalArgumentException("Must have at least one value");
            long min = Long.MAX_VALUE;
            for (long i : values) {
                if (i < min)
                    min = i;
            }
            return min;
        }

        /**
         * Returns the maximum value of an array.
         * 
         * @param values
         *            an array of value
         * @return the maximum of all elements of <code>values</code>
         */
        public static double minDouble(double... values) {
            if (values.length == 0)
                throw new IllegalArgumentException("Must have at least one value");
            double min = Double.POSITIVE_INFINITY;
            for (double i : values) {
                if (i < min)
                    min = i;
            }
            return min;
        }

        /**
         * Returns the minimum value of an array.
         * 
         * @param <T>
         *            the generic type
         * @param values
         *            an array of value
         * @return the maximum of all elements of <code>values</code>
         */
        public static <T extends Comparable<? super T>> T min(T[] values) {
            if (values.length == 0)
                throw new IllegalArgumentException("Must have at least one value");
            T min = null;
            for (T i : values) {
                if (i != null && (min == null || i.compareTo(min) < 0))
                    min = i;
            }
            return min;
        }

        /**
         * Returns the minimum value of a collection.
         * 
         * @param <T>
         *            the generic type
         * @param values
         *            an array of value
         * @return the maximum of all elements of <code>values</code>
         */
        public static <T extends Comparable<? super T>> T min(Collection<T> values) {
            if (values.isEmpty())
                throw new IllegalArgumentException("Must have at least one value");
            T min = null;
            for (T i : values) {
                if (i != null && (min == null || i.compareTo(min) < 0))
                    min = i;
            }
            return min;
        }

        /**
         * Returns the <code>n</code> smallest values of an array.
         * 
         * @param <T>
         *            the generic type
         * @param values
         *            an array of value
         * @param k
         *            the number of elements to extract
         * @return the <code>n</code> smallest elements of <code>values</code> sorted in ascending order
         * @see QuickSelect#max(Comparable[], int, boolean, boolean)
         */
        public static <T extends Comparable<? super T>> T[] min(T[] values, int k) {
            if (values.length == 0 || k == 0)
                return Arrays.copyOf(values, 0);

            if (k == 1) {
                // More efficient implementation
                T[] r = Arrays.copyOf(values, 1);
                r[0] = min(values);
                return r[0] != null ? r : Arrays.copyOf(values, 0);
            }

            return QuickSelect.min(values, k, true, true);
        }

        /**
         * Returns the <code>k</code> smallest values of an array.
         * 
         * @param values
         *            an array of double
         * @param k
         *            the number of elements to extract
         * @return the <code>n</code> biggest elements of <code>values</code> sorted in ascending order
         * @see QuickSelect#max(Comparable[], int, boolean, boolean)
         */
        public static double[] min(double[] values, int k) {
            if (values.length == 0)
                return new double[0];
            if (k == 1) {
                // More efficient implementation

                double[] r = Arrays.copyOf(values, 1);
                r[0] = minDouble(values);
                return r[0] != Double.POSITIVE_INFINITY ? r : new double[0];
            }

            Double[] clone = new Double[values.length];
            for (int i = 0; i < clone.length; i++) {
                clone[i] = values[i];
            }
            Double[] rD = QuickSelect.min(clone, k, false, true);
            double[] r = new double[rD.length];
            for (int i = 0; i < r.length; i++) {
                r[i] = rD[i];
            }
            return r;
        }

        /**
         * Returns the sum of an array of values.
         * 
         * @param values
         *            an array of values to be averaged
         * @return the sum of an array of values
         */
        public static double sum(double... values) {
            double sum = 0;
            for (double d : values) {
                sum += d;
            }
            return sum;
        }

        /**
         * Sum the second dimension of a matrix
         * 
         * @param values
         *            a matrix
         * @return an array {@code  sum} containing the sum of the <em>rows</em>:
         *         <code>sum[i]=values[i][0]+...+values[i][values[i].length-1]</code>
         * @author vpillac
         */
        public static double[] sum(double[][] values) {
            double[] sum = new double[values.length];
            for (int i = 0; i < values.length; i++) {
                sum[i] = Utilities.Math.sum(values[i]);
            }
            return sum;
        }

        /**
         * Returns the arithmetic mean of an array of values.
         * 
         * @param values
         *            the values
         * @return the arithmetic mean of of {@code  values}
         */
        public static double mean(double... values) {
            if (values.length == 0)
                return 0;
            double avg = sum(values);
            return avg / values.length;
        }

        /**
         * Returns the arithmetic mean of a collection of values.
         * 
         * @param values
         *            the values
         * @return the arithmetic mean of of {@code  values}
         */
        public static double mean(Collection<Double> values) {
            if (values.isEmpty())
                return 0;
            double sum = 0;
            for (Double d : values)
                sum += d;
            return sum / values.size();
        }

        /**
         * Returns the median of a collection of values.
         * 
         * @param values
         *            the values
         * @return the median of {@code  values}
         */
        public static double median(Collection<Double> values) {
            return percentile(values, 50);
        }

        /**
         * Return the {@code  k}-th percentile of a collection values.
         * 
         * @param values
         *            the values
         * @param k
         *            the desired percentile
         * @return the {@code  k}-th percentile of {@code  values}
         */
        public static double percentile(Collection<Double> values, int k) {
            if (values.isEmpty())
                throw new IllegalArgumentException("Collection of values is empty");
            if (k < 0 || k > 100)
                throw new IllegalArgumentException("Illegal value for k: " + k);
            Double[] val = values.toArray(new Double[values.size()]);
            Arrays.sort(val);
            return val[(int) (val.length / 100d * k)];
        }

        /**
         * Return the frequency histogram data for a serie of values
         * 
         * @param values
         * @param buckectCount
         * @return a 2-dimension {@code  hist} array containing for each bucket {@code  i} the bucket upper bound
         *         {@code  hist[i][0]} and the nummber of values in that bucket {@code  hist[i][1]}
         * @author vpillac
         */
        public static double[][] freqHistogram(double[] values, int buckectCount) {
            double min = java.lang.Math.floor(minDouble(values)) - 1;
            double max = java.lang.Math.ceil(maxDouble(values)) + 1;
            double bucketWidth = (max - min) / buckectCount;
            double[][] hist = new double[2][buckectCount];

            for (int i = 0; i < buckectCount; i++) {
                hist[0][i] = min + (i + 1) * bucketWidth;
                hist[1][i] = 0;
            }

            for (double value : values) {
                boolean counted = false;
                double lb = min;
                double ub = hist[0][0];
                for (int i = 0; i < buckectCount & !counted; i++) {
                    if (lb <= value && value < ub) {
                        hist[1][i]++;
                    }
                    lb = ub;
                    ub = ub += bucketWidth;
                }
            }

            return hist;
        }

        /**
         * Returns the sum of an array of values.
         * 
         * @param values
         *            an array of values to be averaged
         * @return the sum of an array of values
         */
        public static int sum(int... values) {
            int sum = 0;
            for (int d : values) {
                sum += d;
            }
            return sum;
        }

        /**
         * Returns the average of an array of values.
         * 
         * @param values
         *            an array of values to be averaged
         * @return the average of an array of values
         */
        public static double average(int... values) {
            double avg = sum(values);
            return avg / values.length;
        }

        /**
         * Evaluate the deviation of the <code>values</code> with respect to the given deviation metric.
         * 
         * @param devMetric
         *            the deviation metric to be used
         * @param values
         *            the values which variance/deviation will be evaluated
         * @return the deviation of <code>values</code> with respect to <code>devMetric</code>
         */
        public static double deviation(DeviationMeasure devMetric, Collection<Double> values) {
            if (values.size() == 0)
                return 0;

            if (devMetric == DeviationMeasure.Min || devMetric == DeviationMeasure.Max) {
                return devMetric == DeviationMeasure.Min ? Utilities.Math.min(values)
                        : Utilities.Math.max(values);
            }

            double avg = Utilities.Math.mean(values);
            double eval = Double.NaN;
            switch (devMetric) {
            case MaxAbsDev:
                eval = 0;
                for (double v : values) {
                    eval = java.lang.Math.max(eval, java.lang.Math.abs(avg - v));
                }
                break;
            case AvgAbsDev:
                eval = 0;
                for (double v : values) {
                    eval += java.lang.Math.abs(avg - v);
                }
                eval /= values.size();
                break;
            case StdDev:
                eval = 0;
                for (double v : values) {
                    eval += java.lang.Math.pow(java.lang.Math.abs(avg - v), 2);
                }
                eval = java.lang.Math.sqrt(eval);
                break;
            case MaxMinGap:
                double min = Double.POSITIVE_INFINITY;
                double max = Double.NEGATIVE_INFINITY;
                for (double v : values) {
                    if (v < min)
                        min = v;
                    if (v > max)
                        max = v;
                }
                return max - min;
            default:
                throw new UnsupportedOperationException(devMetric + " is not supported yet");
            }

            return eval;
        }

        /**
         * Evaluate the deviation of the <code>values</code> with respect to the given deviation metric.
         * 
         * @param devMetric
         *            the deviation metric to be used
         * @param values
         *            the values which variance/deviation will be evaluated
         * @return the deviation of <code>values</code> with respect to <code>devMetric</code>
         */
        public static double deviation(DeviationMeasure devMetric, double... values) {
            if (values.length == 0)
                return 0;

            if (devMetric == DeviationMeasure.Min || devMetric == DeviationMeasure.Max) {
                return devMetric == DeviationMeasure.Min ? Utilities.Math.minDouble(values)
                        : Utilities.Math.maxDouble(values);
            }

            double avg = Utilities.Math.mean(values);
            double eval = Double.NaN;
            switch (devMetric) {
            case MaxAbsDev:
                eval = 0;
                for (double v : values) {
                    eval = java.lang.Math.max(eval, java.lang.Math.abs(avg - v));
                }
                break;
            case AvgAbsDev:
                eval = 0;
                for (double v : values) {
                    eval += java.lang.Math.abs(avg - v);
                }
                eval /= values.length;
                break;
            case StdDev:
                eval = 0;
                for (double v : values) {
                    eval += java.lang.Math.pow(java.lang.Math.abs(avg - v), 2);
                }
                eval /= values.length;
                eval = java.lang.Math.sqrt(eval);
                break;
            case Var:
                eval = 0;
                for (double v : values) {
                    eval += java.lang.Math.pow(java.lang.Math.abs(avg - v), 2);
                }
                eval /= values.length;
                break;
            case MaxMinGap:
                double min = Double.POSITIVE_INFINITY;
                double max = Double.NEGATIVE_INFINITY;
                for (double v : values) {
                    if (v < min)
                        min = v;
                    if (v > max)
                        max = v;
                }
                eval = max - min;
                break;
            default:
                throw new UnsupportedOperationException(devMetric + " is not supported yet");
            }

            return eval;
        }

        /**
         * Round a decimal with the given number of significant digits using {@linkplain RoundingMethod rounding method}
         * .
         * 
         * @param decimal
         *            the decimal to be rounded
         * @param digits
         *            the number of significant digits
         * @param mode
         *            the rounding mode
         * @return {@code  decimal} rounded to the given number of significant digits
         */
        public static double roundSignificant(double decimal, int digits) {
            BigDecimal bd = new BigDecimal(decimal);
            bd = bd.round(new MathContext(digits));
            return bd.doubleValue();
        }

        /**
         * Round a decimal with the given precision using the specified {@linkplain RoundingMethod rounding method}.
         * 
         * @param decimal
         *            the decimal to be rounded
         * @param precision
         *            the precision (number of digits)
         * @param mode
         *            the rounding mode
         * @return {@code  decimal} rounded to the given {@code  precision} according to {@code  method}
         */
        public static double round(double decimal, int precision, RoundingMode mode) {
            if (mode == RoundingMode.UNNECESSARY)
                return decimal;
            BigDecimal bd = new BigDecimal(decimal).setScale(precision, mode);
            return bd.doubleValue();
        }

        /**
         * Round a decimal with the given precision using the specified {@linkplain RoundingMethod rounding method}.
         * 
         * @param decimal
         *            the decimal to be rounded
         * @param precision
         *            the precision (for instance 1E-6)
         * @param mode
         *            the rounding mode
         * @return {@code  decimal} rounded to the given {@code  precision} according to {@code  method}
         */
        public static double round(double decimal, double precision, RoundingMode mode) {
            return round(decimal, -(int) java.lang.Math.log10(precision), mode);
        }

        /**
         * Returns {@code true} if {@code  small} is included in {@code  big}.
         * 
         * @param big
         *            the big
         * @param small
         *            the small
         * @return {@code true} if {@code  small} is included in {@code  big}
         */
        public static boolean isIncluded(Set<?> big, Set<?> small) {
            if (small.size() > big.size())
                return false;
            for (Object e : small)
                if (!big.contains(e))
                    return false;
            return true;
        }

        /**
         * Returns {@code true} if {@code  set1} is equal to {@code  set2}, i.e. if they contain the same elements
         * 
         * @param set1
         *            the set1
         * @param set2
         *            the set2
         * @return {@code true} if {@code  set1} is equal to {@code  set2}
         */
        public static boolean equals(Set<?> set1, Set<?> set2) {
            return set1.size() == set2.size() && isIncluded(set1, set2);
        }
    }

    /**
     * Creates a iterator of the desired type based on the given iterator
     * <p/>
     * <b>Warning</b> This operation is not type safe.
     * 
     * @param <S>
     *            the generic type
     * @param <D>
     *            the generic type
     * @param iterator
     *            the iterator
     * @return an iterator of type <code>Iterator<</>D></code> based on the given iterator
     */
    public static <S, D> Iterator<D> castIterator(final Iterator<S> iterator) {
        return new Iterator<D>() {
            private final Iterator<S> it = iterator;

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @SuppressWarnings("unchecked")
            @Override
            public D next() {
                return (D) it.next();
            }

            @Override
            public void remove() {
                it.remove();
            }
        };
    }

    /**
     * Creates a iterator of the desired type based on the given iterator
     * <p/>
     * <b>Warning</b> This operation is not type safe.
     * 
     * @param <S>
     *            the generic type
     * @param <D>
     *            the generic type
     * @param iterator
     *            the iterator
     * @return an iterator of type <code>Iterator<</>D></code> based on the given iterator
     */
    public static <S, D> ListIterator<D> castIterator(final ListIterator<S> iterator) {
        return new ListIterator<D>() {
            private final ListIterator<S> it = iterator;

            @SuppressWarnings("unchecked")
            @Override
            public void add(D e) {
                it.add((S) e);
            }

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public boolean hasPrevious() {
                return it.hasPrevious();
            }

            @SuppressWarnings("unchecked")
            @Override
            public D next() {
                return (D) it.next();
            }

            @Override
            public int nextIndex() {
                return it.nextIndex();
            }

            @SuppressWarnings("unchecked")
            @Override
            public D previous() {
                return (D) it.previous();
            }

            @Override
            public int previousIndex() {
                return it.previousIndex();
            }

            @Override
            public void remove() {
                it.remove();
            }

            @SuppressWarnings("unchecked")
            @Override
            public void set(D e) {
                it.set((S) e);

            }
        };
    }

    /**
     * Collection pseudo-casting.
     * <p/>
     * Will copy all elements of the given collection to a list of the given generic type
     * <p/>
     * Supports {@link LinkedList} and {@link ArrayList}, all other implementations will be casted in a
     * 
     * @param <S>
     *            the source type
     * @param <D>
     *            the destination type
     * @param source
     *            the collection to be casted
     * @return a List with generic type <code><D></code> containing the casted elements of the <code>source</code> list
     *         {@link ArrayList}.
     *         <p/>
     *         <b>Warning</b> This operation is not type safe.
     */
    @SuppressWarnings("unchecked")
    public static <S, D> List<D> convertToList(Collection<S> source) {
        if (source == null) {
            return null;
        }

        List<D> casted = null;

        if (source instanceof LinkedList<?>) {
            casted = new LinkedList<D>();
        } else {
            casted = new ArrayList<D>();
        }

        for (S s : source) {
            casted.add((D) s);
        }

        return casted;
    }

    /**
     * Collection pseudo-casting.
     * <p/>
     * Will copy all elements of the given collection to a {@link HashSet} of the given generic type
     * <p/>
     * <b>Warning</b> This operation is not type safe.
     * 
     * @param <S>
     *            the source type
     * @param <D>
     *            the destination type
     * @param source
     *            the collection to be casted
     * @return a {@link HashSet} with generic type <code><D></code> containing the casted elements of the
     *         <code>source</code> list
     */
    @SuppressWarnings("unchecked")
    public static <S, D> Set<D> convertToSet(Collection<S> source) {
        if (source == null) {
            return null;
        }

        Set<D> casted = null;

        casted = new HashSet<D>();

        for (S s : source) {
            casted.add((D) s);
        }

        return casted;
    }

    /**
     * Copy an <code>int</code> array into a <code>double</code> array.
     * 
     * @param array
     *            the array
     * @return a copy of <code>array</code> in a array of <code>double</code>
     */
    public static double[] copyToDoubleArray(int[] array) {
        double[] copy = new double[array.length];
        for (int s = 0; s < array.length; s++) {
            copy[s] = array[s];
        }
        return copy;
    }

    /**
     * Copy a <code>double</code> array into a <code>int</code> array.
     * 
     * @param array
     *            the array
     * @return a copy of <code>array</code> in a array of <code>int</code>
     */
    public static int[] copyToIntArray(double[] array) {
        int[] copy = new int[array.length];
        for (int s = 0; s < array.length; s++) {
            copy[s] = (int) array[s];
        }
        return copy;
    }

    /**
     * Equality check.
     * 
     * @param a
     *            the a
     * @param b
     *            the b
     * @return <code>a==b || (a!=null && a.equals(b))</code>
     */
    public static boolean equal(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }

    /**
     * Recursively unwrap a wrapper to get the underlying object.
     * 
     * @param <O>
     *            the generic type
     * @param wrapper
     *            the wrapper
     * @param wrappedClass
     *            the wrapped class
     * @return the wrapped object
     */
    @SuppressWarnings("unchecked")
    public static <O> O getWrappedObjectRec(Wrapper<O> wrapper, Class<O> wrappedClass) {
        O object = wrapper.getWrappedObject();
        Object tmp = object;
        while (tmp instanceof Wrapper<?>) {
            tmp = ((Wrapper<?>) tmp).getWrappedObject();
            if (wrappedClass.isAssignableFrom(tmp.getClass())) {
                object = (O) tmp;
            }
        }
        return object;
    }

    /**
     * Factory method for compatible constructors.
     * 
     * @param <O>
     *            the type of object to be returned
     * @param clazz
     *            the class to be instantiated
     * @param argsTypes
     *            the types of the arguments to be passed to the class constructor
     * @param args
     *            the arguments to be passed to the class constructor (can include <code>null</code> elements)
     * @return a new instance of the class defined as value of the parameter <code>clazz</code>
     * @throws IllegalArgumentException
     *             if the new object could not be instantiated because of a error in the arguments
     * @see Reflection#getMatchingConstructor(Class, Class...)
     */
    @SuppressWarnings("unchecked")
    public static <O> O newInstance(Class<O> clazz, Class<?>[] argsTypes, Object[] args)
            throws IllegalArgumentException {

        if (clazz == null) {
            throw new IllegalArgumentException("The class argument cannot be null");
        }

        O object = null;

        Constructor<?> cons;
        try {
            cons = Reflection.getMatchingConstructor(clazz, argsTypes);

            if (cons != null) {
                object = (O) cons.newInstance(args);
            } else {
                throw new IllegalArgumentException(String.format(
                        "No matching constructor found for class:%s : args:%s",
                        clazz.getSimpleName(), Arrays.toString(argsTypes)));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format(
                    "Exception caught when trying to instanciate class:%s : args:%s, exception:%s",
                    clazz.getSimpleName(), Arrays.toString(argsTypes), e), e);
        }

        return object;
    }

    /**
     * Factory method for parameters which values are types. <br/>
     * Will deduce the constructor by using the <code>class</code> of the given arguments <code>args</code>
     * 
     * @param <O>
     *            the type of object to be returned
     * @param clazz
     *            the class to be instantiated
     * @param args
     *            the arguments to be passed to the class constructor (all elements should be <b>not <code>null</code>
     *            </b>)
     * @return a new instance of the class defined as value of the parameter <code>clazz</code>
     * @throws IllegalArgumentException
     *             if the new object could not be instantiated because of a error in the arguments
     * @see #newInstance(Class, Class[], Object[])
     */
    public static <O> O newInstance(Class<O> clazz, Object... args) throws IllegalArgumentException {
        Class<?>[] argsClasses = new Class<?>[args.length];

        // Deducing the argument types
        for (int i = 0; i < argsClasses.length; i++) {
            if (args[i] != null) {
                argsClasses[i] = args[i].getClass();
            } else {
                argsClasses[i] = Object.class;
            }
        }

        return newInstance(clazz, argsClasses, args);
    }

    /**
     * Cast a string to an array of integers
     * <p>
     * String should have the format <code>"[1,2,3]"</code>.
     * 
     * @param array
     *            the string to be cats
     * @return an array containing the parsed integers
     */
    public static int[] toIntArray(String array) {
        // Empty array []
        if (array.length() == 2)
            return new int[0];

        String[] cast = array.substring(1, array.length() - 1).split(",");

        int[] result = new int[cast.length];

        for (int i = 0; i < cast.length; i++) {
            result[i] = Integer.parseInt(cast[i].replaceAll("\\s+", ""));
        }

        return result;
    }

    /**
     * Cast a list to an array of integers.
     * 
     * @param list
     *            the list to be cast
     * @return an array containing the original list
     */
    public static int[] toIntArray(List<Integer> list) {
        // Empty array []
        int[] result = new int[list.size()];

        int i = 0;
        for (int v : list) {
            result[i++] = v;
        }

        return result;
    }

    /**
     * Cast a string to an array of longs
     * <p>
     * String should have the format <code>"[1,2,3]"</code>.
     * 
     * @param array
     *            the string to be cast
     * @return an array containing the parsed longs
     */
    public static long[] toLongArray(String array) {
        // Empty array []
        if (array.length() == 2)
            return new long[0];

        String[] cast = array.substring(1, array.length() - 1).split(",");

        long[] result = new long[cast.length];

        for (int i = 0; i < cast.length; i++) {
            result[i] = Long.valueOf(cast[i]);
        }

        return result;
    }

    /**
     * Cast a string to an array of double
     * <p>
     * String should have the format <code>"[1.2,2.6,3]"</code>
     * 
     * @param array
     *            the string to be casted
     * @return an array containing the parsed double
     */
    public static double[] toDoubleArray(String array) {
        // Empty array []
        if (array.length() == 2)
            return new double[0];

        String[] cast = array.substring(1, array.length() - 1).split(",");

        double[] result = new double[cast.length];

        for (int i = 0; i < cast.length; i++) {
            result[i] = Double.valueOf(cast[i]);
        }

        return result;
    }

    /**
     * Cast a string to an array of objects
     * <p>
     * String should have the format <code>"[a,b,c]"</code>.
     * 
     * @param array
     *            the string to be parsed
     * @return the parsed array
     */
    public static String[] toArray(String array) {
        // Empty array []
        if (array.length() == 2)
            return new String[0];

        return array.substring(1, array.length() - 1).split(",");
    }

    /**
     * Converts a collection to a short string of the form <code>[a,b,c]</code> with comma separated values and no
     * spaces.
     * 
     * @param collection
     *            the collection
     * @return the corresponding string
     */
    public static String toShortString(Iterable<?> collection) {
        return toShortString(collection, ',', true);
    }

    /**
     * Converts a collection to a short string of the form <code>[a,b,c]</code>.
     * 
     * @param collection
     *            the collection
     * @param separator
     *            the character used between value
     * @param brackets
     *            the brackets
     * @return the corresponding string {@code true} to include opening/closing brackets
     */
    public static String toShortString(Iterable<?> collection, char separator, boolean brackets) {
        if (collection == null)
            return "null";
        StringBuilder s = new StringBuilder(255);
        if (brackets)
            s.append("[");
        Iterator<?> it = collection.iterator();
        while (it.hasNext()) {
            s.append(toShortString(it.next()));
            if (it.hasNext())
                s.append(separator);
        }
        if (brackets)
            s.append("]");

        return s.toString();
    }

    /**
     * Converts an object to a (short) string.
     * 
     * @param object
     *            the object to be converted
     * @return a short string describing the array
     * @see Class#isArray()
     */
    public static String toShortString(Object object) {
        if (object == null)
            return "null";
        if (IToShortString.class.isAssignableFrom(object.getClass()))
            return ((IToShortString) object).toShortString();
        else if (object.getClass().isArray()) {

            final int l = Array.getLength(object);
            StringBuilder s = new StringBuilder(l * 3);
            s.append("[");

            for (int i = 0; i < l; i++) {
                Object v = Array.get(object, i);
                s.append(toShortString(v));
                if (i < l - 1)
                    s.append(",");
            }

            s.append("]");
            return s.toString();
        } else if (Iterable.class.isAssignableFrom(object.getClass())) {
            return toShortString((Iterable<?>) object);
        } else {
            return object.toString();
        }

    }

    /**
     * Converts an object to a {@link String}, automatically handling arrays and collections.
     * 
     * @param o
     *            the object to be converted to a {@link String}
     * @return a {@link String} describing the object
     */
    public static String toString(Object o) {
        if (o == null)
            return null;
        else if (o instanceof Collection<?>)
            return toShortString((Collection<?>) o);
        else if (o.getClass().isArray())
            return toShortString(o);
        else
            return o.toString();
    }

    /**
     * Converts an array to a short string of the form <code>[a,b,c]</code> with comma separated values and no spaces.
     * 
     * @param <T>
     *            the generic type
     * @param array
     *            the array
     * @return the corresponding string
     */
    public static <T> String toShortString(T[] array) {
        return toShortString(array, ',', true);
    }

    /**
     * Converts an array to a short string of the form <code>[a,b,c]</code>.
     * 
     * @param <T>
     *            the generic type
     * @param array
     *            the array
     * @param separator
     *            the character used between value
     * @param brackets
     *            the brackets
     * @return the corresponding string {@code true} to include opening/closing brackets
     */
    public static <T> String toShortString(T[] array, char separator, boolean brackets) {
        if (array == null)
            return "null";
        StringBuilder s = new StringBuilder(array.length * 3);
        if (brackets)
            s.append("[");

        for (int i = 0; i < array.length; i++) {
            s.append(array[i]);
            if (i < array.length - 1)
                s.append(separator);
        }
        if (brackets)
            s.append("]");

        return s.toString();
    }

    /**
     * Compare two arrays of same size element by element.
     * 
     * @param a
     *            the a
     * @param b
     *            the b
     * @return <code>0 if a==b</code>, <code>1 if a>b</code>, <code>-1 if a&lt;b</code> and {@link Integer#MAX_VALUE}
     *         otherwise
     */
    public static int compare(int[] a, int[] b) {
        if (a.length != b.length)
            throw new IllegalArgumentException("Both arrays must be of same size");
        boolean greater = true;
        boolean lower = true;
        boolean equal = true;

        for (int i = 0; i < a.length; i++) {
            if (a[i] < b[i]) {
                greater = false;
                equal = false;
            } else if (a[i] > b[i]) {
                lower = false;
                equal = false;
            }
        }

        return equal ? 0 : greater ? 1 : lower ? -1 : Integer.MAX_VALUE;
    }

    /**
     * Merge two array in a single one.
     * 
     * @param <T>
     *            the generic type
     * @param array1
     *            the first array
     * @param array2
     *            the second array
     * @return an array containing all elements from {@code  array1} and {@code  array2} (the first element is the first
     *         element of {@code  array1})
     */
    public static <T> T[] merge(T[] array1, T[] array2) {
        T[] merge = Arrays.copyOf(array1, array1.length + array2.length);
        for (int i = 0; i < array2.length; i++) {
            merge[i + array1.length] = array2[i];
        }
        return merge;
    }

    /**
     * Search for the maximum {@link IObjectWithID#getID() id} in a collection of objects
     * 
     * @param objects
     * @return the maximium id found in {@code  objects}
     * @author vpillac
     */
    public static int getMaxId(Collection<? extends IObjectWithID> objects) {
        if (objects.isEmpty())
            return 0;
        int maxId = 0;
        for (IObjectWithID o : objects) {
            if (o.getID() > maxId)
                maxId = o.getID();
        }
        return maxId;
    }

    /**
     * Unified path for output file, dated to the start of the jVM.
     * 
     * @param directory
     *            the destination directory
     * @param runName
     *            the name of the run
     * @param comment
     *            a comment for this file
     * @param extension
     *            the extension of the file (without dots)
     * @return a unified path for the desired output file
     */
    public static String getUnifiedOutputFilePath(String directory, String runName, String comment,
            String extension) {
        File f = new File(String.format("%s/%s_%s_%s.%s", directory, Time.getVMStartDateString(),
                runName, comment, extension));
        int i = 1;
        while (f.exists()) {
            f = new File(String.format("%s/%s_%s_%s-%s.%s", directory, Time.getVMStartDateString(),
                    runName, comment, i++, extension));
        }
        return f.getPath();
    }

    /**
     * List files.
     * 
     * @param path
     *            the path
     * @param filePattern
     *            the file pattern
     * @return a list of the files contains in the directory specified by <code>path</code> that match the given pattern
     * @throws FileNotFoundException
     *             the file not found exception
     */
    public static List<File> listFiles(String path, String filePattern)
            throws FileNotFoundException {
        LinkedList<File> files = new LinkedList<File>();
        File folder = new File(path);
        if (!folder.exists())
            throw new FileNotFoundException("Folder not found, path:" + path);
        for (String f : folder.list()) {
            if (f.matches(filePattern)) {
                files.add(new File(String.format("%s%s%s", path, File.separatorChar, f)));
            }
        }

        Collections.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        return files;
    }

    /** The Number format. */
    private static String sNumberFormat = "0.###";

    /**
     * Set the default format number to be used.
     * 
     * @param format
     *            a string of the form <code>###0.000</code> defining the formating of decimal numbers
     * @see #format(Number)
     */
    public static void setDefaultNumberFormat(String format) {
        sNumberFormat = format;
        sFormat = new DecimalFormat(sNumberFormat);
    }

    /** The Format. */
    private static DecimalFormat sFormat = new DecimalFormat(sNumberFormat);

    /**
     * Format a number.
     * 
     * @param val
     *            the val
     * @return a string describing the given number with the default precision
     * @see #setDefaultNumberFormat(String)
     */
    public static String format(Object val) {

        if (val == null)
            return "null";
        else if (Number.class.isAssignableFrom(val.getClass()))
            // return NumberFormat.getNumberInstance().format(val);
            return sFormat.format(val);
        // else if (val instanceof Double || val instanceof Float)
        else
            return val.toString();
    }

}
