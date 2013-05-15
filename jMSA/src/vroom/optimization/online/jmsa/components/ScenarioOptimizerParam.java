package vroom.optimization.online.jmsa.components;

/**
 * <code>ScenarioOptimizerParam</code> is a class used to encapsulate parameters that will be passed to a {@link ScenarioOptimizerBase} instance.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a href="http://copa.uniandes.edu.co">Copa</a>, <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:06:43 a.m.
 * @see ScenarioOptimizerBase
 */
public class ScenarioOptimizerParam implements IMSAComponentParameter {

    /** The maximum amount of time to be spent on optimization (in seconds) **/
    private final int mMaxTime;

    /**
     * Getter for maxTime : The maximum amount of time to be spent on optimization (in milliseconds)
     * 
     * @return the value of maxTime
     */
    public int getMaxTime() {
        return mMaxTime;
    }

    /** The maximum amount of time to be spent for each scenario (in milliseconds) **/
    private final int mMaxTimePerScen;

    /**
     * Getter for maxTimePerScen : The maximum amount of time to be spent for each scenario (in seconds)
     * 
     * @return the value of maxTimePerScen
     */
    public int getMaxTimePerScen() {
        return mMaxTimePerScen;
    }

    /** <code>true</code> is the optimization is interruptible **/
    private final boolean mInterruptible;

    /**
     * Getter for the interruptible flag
     * 
     * @return <code>true</code> is the optimization is interruptible
     */
    public boolean isInterruptible() {
        return this.mInterruptible;
    }

    /**
     * Creates a new <code>ScenarioOptimizerParam</code>
     * 
     * @param maxTime
     *            the maximum amount of time to be spent on optimization (in milliseconds)
     * @param maxTimePerScen
     *            the maximum amount of time to be spent for each scenario (in milliseconds)
     * @param interruptible
     *            <code>true</code> is the optimization is interruptible
     */
    public ScenarioOptimizerParam(int maxTime, int maxTimePerScen, boolean interruptible) {
        super();
        mMaxTime = maxTime;
        mMaxTimePerScen = maxTimePerScen;
        mInterruptible = interruptible;
    }

    @Override
    public String toString() {
        return String.format("maxTime=%s, maxTimePerScen=%s", getMaxTime(), getMaxTimePerScen());
    }
}