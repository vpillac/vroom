package vroom.optimization.online.jmsa.components;

/**
 * <code>ScenarioGeneratorParam</code> is a class used to encapsulate parameters that will be passed to a {@link ScenarioGeneratorBase} instance.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a href="http://copa.uniandes.edu.co">Copa</a>, <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:06:44 a.m.
 * @see ScenarioGeneratorBase
 */
public class ScenarioGeneratorParam implements IMSAComponentParameter {

    /** The maximum number of scenarios to be generated **/
    private final int mMaxScen;

    /**
     * Getter for maxScen : The maximum number of scenarios to be generated
     * 
     * @return the value of maxScen
     */
    public int getMaxScen() {
        return mMaxScen;
    }

    /**
     * The maximum time spent on initial optimization of each scenario (in milliseconds)
     **/
    private final int mMaxInitTime;

    /**
     * Getter for maxInitTime : The maximum time spent on initial optimization of each scenario (in milliseconds)
     * 
     * @return the value of maxInitTime
     */
    public int getMaxInitTime() {
        return mMaxInitTime;
    }

    /** The parameters for the request sampling **/
    private final RequestSamplerParam mSamplerParams;

    /**
     * Getter for samplerParams : The parameters for the request sampling
     * 
     * @return the value of samplerParams
     */
    public RequestSamplerParam getSamplerParams() {
        return mSamplerParams;
    }

    /**
     * Creates a new <code>ScenarioGeneratorParam</code>
     * 
     * @param maxScen
     *            the maximum number of scenarios to be generated
     * @param maxInitTime
     *            the maximum time (in milliseconds) to be spent on scenario initial optimization
     * @param samplerParams
     *            the maximum number of sampled requests to be included in the scenario
     */
    public ScenarioGeneratorParam(int maxScen, int maxInitTime, RequestSamplerParam samplerParams) {
        super();

        if (maxScen <= 0) {
            throw new IllegalArgumentException("Argument maxScen cannot be lower than 0");
        }

        if (maxInitTime <= 0) {
            throw new IllegalArgumentException("Argument maxInitTime cannot be lower than 0");
        }

        if (samplerParams == null) {
            throw new IllegalArgumentException("Argument samplerParams cannot be null");
        }

        mMaxScen = maxScen;
        mMaxInitTime = maxInitTime;
        mSamplerParams = samplerParams;
    }

    @Override
    public String toString() {
        return String.format("maxScen=%s, maxInitTime=%s, samplerParams: (%s)", getMaxScen(),
            getMaxInitTime(), getSamplerParams());
    }
}