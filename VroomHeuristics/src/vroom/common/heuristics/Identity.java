/**
 *
 */
package vroom.common.heuristics;

import vroom.common.heuristics.vls.IVLSPertubation;
import vroom.common.heuristics.vls.IVLSState;
import vroom.common.utilities.optimization.IInstance;
import vroom.common.utilities.optimization.ILocalSearch;
import vroom.common.utilities.optimization.IMove;
import vroom.common.utilities.optimization.INeighborhood;
import vroom.common.utilities.optimization.IParameters;
import vroom.common.utilities.optimization.IPerturbation;
import vroom.common.utilities.optimization.ISolution;

/**
 * <code>Identity</code> is a identity implementation of {@link ILocalSearch}, {@link IPerturbation} and
 * {@link INeighborhood}
 * <p>
 * Creation date: 11 juil. 2010 - 20:44:22
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class Identity<S extends ISolution> implements ILocalSearch<S>, IPerturbation<S>,
        INeighborhood<S, Identity.IdentityMove>, IVLSPertubation<S> {

    @SuppressWarnings("unchecked")
    @Override
    public S localSearch(IInstance instance, S solution, IParameters param) {
        // Do nothing
        return (S) solution.clone();
    }

    @Override
    public void pertub(IInstance instance, S solution, IParameters parameters) {
        // Do nothing
    }

    @Override
    public boolean executeMove(S solution, IMove move) {
        // Do nothing
        return true;
    }

    @Override
    public IdentityMove exploreNeighborhood(S solution, IParameters params) {
        // Do nothing
        return new IdentityMove();
    }

    @Override
    public boolean localSearch(S solution, IParameters params) {
        // Do nothing
        return true;
    }

    /**
     * <code>IdentityMove</code> is the move associated with the identity neighborhood.
     */
    public static class IdentityMove extends Move {

        public IdentityMove() {
            super(0);
        }

        @Override
        public String getMoveName() {
            return "identity";
        }

    }

    @Override
    public void pertub(IVLSState<S> state, IInstance instance, S solution, IParameters params) {
        // Do nothing
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void dispose() {
        // Do nothing
    }

    @Override
    public String getShortName() {
        return "identity";
    }
}
