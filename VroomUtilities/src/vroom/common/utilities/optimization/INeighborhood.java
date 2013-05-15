package vroom.common.utilities.optimization;

/**
 * <code>INeighborhood</code> is an interface for classes that can explore the neighborhood of a mSolution
 * <p>
 * Creation date: Apr 27, 2010 - 10:55:44 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 * @param <S>
 *            the type of the mSolution object which neighborhood will be explored by instances of this class.
 * @param <P>
 *            the type of {@linkplain IParameters parameters} that will be passed to the neighborhood exploring method.
 */
public interface INeighborhood<S extends ISolution, M extends IMove> extends ILocalSearch<S>,
        IPerturbation<S> {

    /**
     * Neighborhood exploration
     * 
     * @param solution
     *            the solution which neighborhood has to be explored
     * @param params
     *            optional parameters for the neighborhood exploration
     * @return <code>true</code> if the solution was improved, <code>false</code> otherwise.
     */
    public boolean localSearch(S solution, IParameters params);

    /**
     * Neighborhood exploration
     * 
     * @param mSolution
     *            mSolution the mSolution which neighborhood has to be explored
     * @param params
     *            optional parameters for the neighborhood exploration
     * @return a IMove resulting from the exploration of the neighborhood of the given mSolution
     */
    public M exploreNeighborhood(S solution, IParameters params);

    /**
     * Execution of a IMove on a mSolution.
     * <p/>
     * Be aware that implementations should not check IMove feasibility
     * 
     * @param mSolution
     *            the mSolution to be modified
     * @param IMove
     *            the IMove that will be applied
     * @return <code>true</code> if the IMove was correctly executed
     */
    public boolean executeMove(S solution, IMove IMove);

    /**
     * Return a string containing a short name for this neighborhood
     * 
     * @return a string containing a short name for this neighborhood
     * @author vpillac
     */
    public String getShortName();
}
