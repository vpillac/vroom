/**
 * 
 */
package vroom.common.utilities.math;

/**
 * <code>Average</code> is a utility class to evaluate an average value
 * <p>
 * Creation date: Jun 20, 2012 - 11:41:20 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class Average {

    private int    mCount;
    private double mSum;

    /**
     * Creates a new <code>Average</code>
     */
    public Average() {
        mCount = 0;
        mSum = 0;
    }

    /**
     * Return the sum of all the values in this average
     * 
     * @return the sum of all the values in this average
     */
    public double getSum() {
        return mSum;
    }

    /**
     * The number of values in this average
     * 
     * @return the number of values in this average
     */
    public int getCount() {
        return mCount;
    }

    /**
     * Returns the average value
     * 
     * @return the average value
     */
    public double value() {
        return mSum / mCount;
    }

    /**
     * Add a value to this average
     * 
     * @param value
     */
    public void addValue(double value) {
        mSum += value;
        mCount++;
    }
}
