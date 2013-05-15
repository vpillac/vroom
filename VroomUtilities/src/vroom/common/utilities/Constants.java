/**
 * 
 */
package vroom.common.utilities;

/**
 * <code>Constants</code> is a collection of constants used across a variety of applications.
 * <p>
 * Creation date: Jun 24, 2010 - 4:44:11 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class Constants {

    /**
     * A tolerance for zero values: values that are in absolute lower than this value are considered as zero
     */
    public static double sZeroTolerance = 1E-9;

    /**
     * Getter for <code>zeroTolerance</code>, default value is {@value #sZeroTolerance}
     * 
     * @return the zeroTolerance
     */
    public static double getZeroTolerance() {
        return sZeroTolerance;
    }

    /**
     * Setter for <code>zeroTolerance</code>, default value is {@value #sZeroTolerance}
     * 
     * @param zeroTolerance
     *            the zeroTolerance to set
     */
    public static void setZeroTolerance(double zeroTolerance) {
        sZeroTolerance = zeroTolerance;
    }

    /** Maximum time to wait for lock in an implementation of the toString() method */
    public static int    TOSTRING_LOCK_TIMOUT = 200;

    /** Replacement string for locked objects */
    public static String TOSTRING_LOCKED      = "[Locked]";

/**
     * Returns true if {@code  val1} is equal to {@code  val2} allowing the specified {@linkplain #getZeroTolerance()
     * 
     * @param val1
     * @param val2
     * @return {@code true} if <code>|val2-val1| &leq; &epsilon;</code>
     * @see #getZeroTolerance()
     */
    public static boolean equals(double val1, double val2) {
        return Double.doubleToLongBits(val1) == Double.doubleToLongBits(val2)
                || Math.abs(val2 - val1) <= getZeroTolerance()
                || (Double.isNaN(val1) && Double.isNaN(val2));
    }

    /**
     * Returns true if {@code  val1} is lower than {@code  val2} allowing the specified {@linkplain #getZeroTolerance()
     * zero tolerance}
     * 
     * @param val1
     * @param val2
     * @return {@code true} if <code>val1 &leq; val2 + &epsilon;</code>
     * @see #getZeroTolerance()
     */
    public static boolean isLowerThan(double val1, double val2) {
        return val1 <= val2 + getZeroTolerance();
    }

    /**
     * Returns {@code true} if {@code  val} is strictly greater than the specified {@linkplain #getZeroTolerance() zero
     * tolerance}
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
     * tolerance}
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
     * tolerance}
     * 
     * @param val
     *            the value to be tested
     * @return {@code true} if {@code  val} is equal to zero
     */
    public static boolean isZero(double val) {
        return Math.abs(val) <= getZeroTolerance();
    }
}
