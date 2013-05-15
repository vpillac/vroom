package vroom.optimization.online.jmsa;

/**
 * Creation date: Mar 8, 2010 - 4:27:54 PM<br/>
 * <code>IDistinguishedSolution</code> is an interface for a distinguished mSolution of the MSA
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public interface IDistinguishedSolution {

    /**
     * Getter for the next request to be served
     * 
     * @return the next request to be served
     */
    public IActualRequest getNextRequest();

    /**
     * Getter for the next to be served for a given resource
     * 
     * @param resource
     *            the identifier of the considered resource
     * @return the next request to be served by the given <code>resource</code>
     */
    public IActualRequest getNextRequest(int resource);

}
