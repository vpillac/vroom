package vroom.optimization.online.jmsa.components;

/**
 * <code>RequestSamplerParam</code> is a class used to encapsulate parameters that will be passed to a
 * {@link RequestSamplerBase} instance.
 * 
 * @see RequestSamplerBase
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:06:44 a.m.
 */
public class RequestSamplerParam implements IMSAComponentParameter {

    /** The number of requests to generate **/
    private final int mNumSampledRequests;

    /**
     * Getter for numSampledRequests : The number of requests to generate
     * 
     * @return the value of numSampledRequests
     */
    public int getNumSampledRequests() {
        return mNumSampledRequests;
    }

    /**
     * Creates a new <code>RequestSamplerParam</code>
     * 
     * @param numSampledRequests
     *            the number of requests to be generated
     */
    public RequestSamplerParam(int numSampledRequests) {
        super();
        mNumSampledRequests = numSampledRequests;
    }

    @Override
    public String toString() {
        return String.format("numSampledRequests=%s", getNumSampledRequests());
    }

}