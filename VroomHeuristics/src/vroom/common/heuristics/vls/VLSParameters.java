/**
 * 
 */
package vroom.common.heuristics.vls;

import vroom.common.heuristics.IInitialization;
import vroom.common.utilities.optimization.ILocalSearch;
import vroom.common.utilities.optimization.IParameters;
import vroom.common.utilities.optimization.SimpleParameters;
import vroom.common.utilities.optimization.SimpleStoppingCriterion;

/**
 * <code>VLSParameters</code> is the class holding the different parameters used inside the VLS procedure.
 * <p/>
 * It can be used to replace the some of the {@link VLSGlobalParameters} for convenience
 * <p>
 * Creation date: 1 mai 2010 - 16:12:18
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class VLSParameters extends SimpleParameters {

    /** The global parameters used in the VLS procedure **/
    private final VLSGlobalParameters mGlobalParameters;

    /**
     * Getter for globalParameters : The global parameters used in the VLS procedure
     * 
     * @return the value of globalParameters
     */
    public VLSGlobalParameters getGlobalParameters() {
        return mGlobalParameters;
    }

    /** The number of iterations in the GRASP loop **/
    private int mNS;

    /**
     * Getter for ns : The number of iterations in the GRASP loop
     * 
     * @return the value of ns
     */
    public int getNS() {
        return mNS;
    }

    /**
     * Setter for ns : The number of iterations in the GRASP loop
     * 
     * @param ns
     *            the value to be set for ns
     */
    public void setNS(int ns) {
        mNS = ns;
    }

    /** The number of iterations in the ILS loop **/
    private int mNI;

    /**
     * Getter for ni : The number of iterations in the ILS loop
     * 
     * @return the value of ni
     */
    public int getNI() {
        return mNI;
    }

    /**
     * Setter for ni : The number of iterations in the ILS loop
     * 
     * @param ni
     *            the value to be set for ni
     */
    public void setNI(int ni) {
        mNI = ni;
    }

    /** The number of iterations in the ELS loop **/
    private int mNC;

    /**
     * Getter for nc : The number of iterations in the ELS loop
     * 
     * @return the value of nc
     */
    public int getNC() {
        return mNC;
    }

    /**
     * Setter for nc : The number of iterations in the ELS loop
     * 
     * @param nc
     *            the value to be set for nc
     */
    public void setNC(int nc) {
        mNC = nc;
    }

    /** The maximum overall time for the VLS procedure (in ms) **/
    private long mMaxTime;

    /**
     * Getter for maxTime : The maximum overall time for the VLS procedure (in ms)
     * 
     * @return the value of maxTime
     */
    @Override
    public long getMaxTime() {
        return mMaxTime;
    }

    /**
     * Setter for maxTime : The maximum overall time for the VLS procedure (in ms)
     * 
     * @param maxTime
     *            the value to be set for maxTime
     */
    public void setMaxTime(long maxTime) {
        mMaxTime = maxTime;
        if (getStoppingCriterion() instanceof SimpleStoppingCriterion) {
            ((SimpleStoppingCriterion) getStoppingCriterion()).setMaxTime(maxTime);
        }
    }

    /**
     * Creates a new <code>VLSParameters</code> instance
     * 
     * @param globalParameters
     *            the global parameters used in the vls procedure
     */
    public VLSParameters(VLSGlobalParameters globalParameters) {
        this(globalParameters, globalParameters.get(VLSGlobalParameters.NS), globalParameters
                .get(VLSGlobalParameters.NI), globalParameters.get(VLSGlobalParameters.NC),
                globalParameters.get(VLSGlobalParameters.VLS_MAX_TIME));
    }

    /**
     * Creates a new <code>VLSParameters</code> instance
     * 
     * @param globalParameters
     *            the global parameters used in the vls procedure
     * @param nS
     *            the number of grasp iterations
     * @param nI
     *            the number of ils iterations
     * @param nC
     *            the number of els iterations
     * @param maxTime
     *            the maximum time for the vls procedure
     */
    public VLSParameters(VLSGlobalParameters globalParameters, int nS, int nI, int nC, long maxTime) {
        super(LSStrategy.DET_BEST_IMPROVEMENT, maxTime, Integer.MAX_VALUE);
        mGlobalParameters = globalParameters;
        mNS = nS;
        mNI = nI;
        mNC = nC;
        mMaxTime = maxTime;
        setStoppingCriterion(new SimpleStoppingCriterion(maxTime, Integer.MAX_VALUE));
    }

    /**
     * Getter for the initialization parameters
     * 
     * @return the parameters to be passed to the {@link IInitialization} component
     */
    public IParameters getInitParams() {
        return getGlobalParameters().get(VLSGlobalParameters.PARAM_INIT);
    }

    /**
     * Getter for the local search parameters
     * 
     * @return the parameters to be passed to the {@link ILocalSearch} component
     */
    public IParameters getLSParameters() {
        return getGlobalParameters().get(VLSGlobalParameters.PARAM_LOCALSEARCH);
    }

    /**
     * Getter for the pertubation parameters
     * 
     * @return the parameters to be passed to the {@link IVLSPertubation} component
     */
    public IParameters getPertubParameters() {
        return getGlobalParameters().get(VLSGlobalParameters.PARAM_PERTUBATION);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("ns=%s,ni=%s,nc=%s,maxTime=%s,stop=%s", getNS(), getNI(), getNC(),
                getMaxTime(), getStoppingCriterion());
    }
}
