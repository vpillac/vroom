/**
 * 
 */
package vroom.common.utilities.math;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import vroom.common.utilities.Utilities;
import vroom.common.utilities.Utilities.Math.DeviationMeasure;

/**
 * The class <code>SimpleStats</code> provides simple statistic recollection functionalities to get the mean, median,
 * and variance of a set of values.
 * <p>
 * Creation date: Jun 20, 2012 - 2:16:09 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class SimpleStats {

    LinkedList<Double> mValues;

    /**
     * Creates a new <code>SimpleStats</code>
     */
    public SimpleStats() {
        mValues = new LinkedList<Double>();
    }

    /**
     * Returns a view of the values contained in this instance
     * 
     * @return a view of the values contained in this instance
     */
    public List<Double> getValues() {
        return Collections.unmodifiableList(mValues);
    }

    /**
     * Add a value to this instance
     * 
     * @param value
     */
    public void addValue(double value) {
        mValues.add(value);
    }

    /**
     * Returns the maximum value stored in this instance
     * 
     * @return the maximum value stored in this instance
     */
    public double max() {
        return Utilities.Math.max(mValues);
    }

    /**
     * Returns the minimum value stored in this instance
     * 
     * @return the minimum value stored in this instance
     */
    public double min() {
        return Utilities.Math.min(mValues);
    }

    /**
     * Returns the arithmetic mean of the values stored in this instance
     * 
     * @return the arithmetic mean of the values stored in this instance
     */
    public double mean() {
        return Utilities.Math.mean(mValues);
    }

    /**
     * Return the median of the values stored in this instance
     * 
     * @return the median of the values stored in this instance
     */
    public double median() {
        return Utilities.Math.median(mValues);
    }

    /**
     * Return the {@code  k}-th percentile of the values
     * 
     * @param k
     *            the desired percentile
     * @return the {@code  k}-th percentile of the values
     * @see Utilities.Math#percentile(java.util.Collection, int)
     */
    public double percentile(int k) {
        return Utilities.Math.percentile(mValues, k);
    }

    /**
     * Return the standard deviation of the values stored in this instance
     * 
     * @return the standard deviation of the values stored in this instance
     */
    public double stdDev() {
        return Utilities.Math.deviation(DeviationMeasure.StdDev, mValues);
    }

    /**
     * Return a measure of the deviation within the values stored in this instance
     * 
     * @param measure
     * @return a measure of the deviation within the values stored in this instance
     * @see Utilities.Math#deviation(DeviationMeasure, java.util.Collection)
     */
    public double deviation(DeviationMeasure measure) {
        return Utilities.Math.deviation(measure, mValues);
    }

    @Override
    public String toString() {
        return mValues.toString();
    }
}
