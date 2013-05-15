package vroom.common.heuristics.vls;

import vroom.common.heuristics.IInitialization;
import vroom.common.utilities.optimization.IInstance;
import vroom.common.utilities.optimization.IParameters;
import vroom.common.utilities.optimization.ISolution;

/**
 * <code>DummyInitialization</code> is an implementation of {@link IInitialization} that only generate a new initial
 * mSolution when certain conditions are met, and otherwise return the current best GRASP mSolution.
 * <p/>
 * In particular, it can be used to generate a new mSolution only if a given number of non-improving optimization has
 * occurred.
 * <p/>
 * This class can be useful to use the VLS framework as a pure ILS or ELS procedure, given that an initial mSolution is
 * provided
 * <p/>
 * Note that this implementation will <b>always</b> return a clone of the current best GRASP mSolution Creation date:
 * May 3, 2010 - 11:54:17 AM<br/>
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class DummyInitialization<S extends ISolution> implements IInitialization<S> {

    protected final IInitialization<S> mInitialization;

    /**
     * Creates a new <code>DummyInitialization</code>
     * 
     * @param initialization
     *            the actual {@link IInitialization} instance to which initialization should be delegated.
     */
    public DummyInitialization(IInitialization<S> initialization) {
        mInitialization = initialization;
    }

    /**
     * Creates a new <code>DummyInitialization</code> with no initialization delegate
     */
    public DummyInitialization() {
        this(null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public S newSolution(IVLSState<S> state, IInstance instance, IParameters params) {
        return (S) state.getBestSolution(VLSPhase.GRASP).clone();
    }

}
