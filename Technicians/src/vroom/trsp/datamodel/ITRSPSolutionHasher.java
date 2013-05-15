package vroom.trsp.datamodel;

import vroom.common.utilities.dataModel.ISolutionHasher;

/**
 * <code>ITRSPSolutionHasher</code> is an extension of {@link ISolutionHasher} that defines an additional method for the
 * hashing of individual tours
 * <p>
 * Creation date: Aug 16, 2011 - 11:50:51 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public interface ITRSPSolutionHasher extends ISolutionHasher<TRSPSolution> {

    /**
     * Hashing of a tour
     * 
     * @param tour
     *            the tour to be hashed
     * @return a hash for the given tour
     */
    public int hash(ITRSPTour tour);

}
