package vroom.common.utilities.optimization;

import umontreal.iro.lecuyer.rng.RandomStream;

/**
 * <code>IParameter</code> is the root interface for all classes that represent a parameter that can be passed to an
 * optimization component
 * <p>
 * Creation date: Apr 27, 2010 - 11:07:51 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public interface IParameters {

    public enum LSStrategy {
        /** Random exploration accepting the first improving solution */
        RND_FIRST_IMPROVEMENT(true, true, false), // Mask: 6
        /** Random exploration accepting the first solution (improving or not) */
        RND_NON_IMPROVING(true, true, true), // Mask 7
        /** Deterministic exploration accepting the best improving solution */
        DET_BEST_IMPROVEMENT(false, false, false), // Mask 0
        /** Deterministic exploration accepting the first improving solution */
        DET_FIRST_IMPROVEMENT(false, true, false); // Mask 2

        private final boolean random;
        private final boolean firstImp;
        private final boolean acceptNonImp;

        /**
         * Creates a new <code>LSStrategy</code>
         * 
         * @param random
         * @param firstImp
         * @param acceptNonImp
         */
        private LSStrategy(boolean rnd, boolean fi, boolean nonImp) {
            this.random = rnd;
            this.firstImp = fi;
            this.acceptNonImp = nonImp;
        }

        /**
         * Randomized flag
         * 
         * @return <code>true</code> if the procedure should be randomized, <code>false</code> if a deterministic
         *         exploration is required.
         */
        public boolean randomized() {
            return random;
        }

        /**
         * First improvement flag
         * 
         * @return <code>true</code> if the first improving solution should be accepted, <code>false</code> if only the
         *         best improving solution should be accepted.
         */
        public boolean acceptFirstImprovement() {
            return firstImp;
        }

        /**
         * Non-improving flag
         * 
         * @return <code>true</code> if non-improving solutions should be accepted, <code>false</code> otherwise
         */
        public boolean acceptNonImproving() {
            return this.acceptNonImp;
        }

        /**
         * Get a strategy from a set of flag
         * 
         * @param rnd
         *            <code>true</code> if the procedure should be randomized, <code>false</code> if a deterministic
         *            exploration is required.
         * @param fi
         *            <code>true</code> if the first improving solution should be accepted, <code>false</code> if only
         *            the best improving solution should be accepted.
         * @param nonImp
         *            <code>true</code> if non-improving solutions should be accepted, <code>false</code> otherwise
         * @return the corresponding {@link LSStrategy}
         * @throws IllegalArgumentException
         *             if the flag combination is unknown
         */
        public static LSStrategy getStrategy(boolean rnd, boolean fi, boolean nonImp) {
            int val = (rnd ? 4 : 0) + (fi ? 2 : 0) + (nonImp ? 1 : 0);
            switch (val) {
            case 0:
                return DET_BEST_IMPROVEMENT;
            case 2:
                return DET_FIRST_IMPROVEMENT;
            case 6:
                return RND_FIRST_IMPROVEMENT;
            case 7:
                return RND_NON_IMPROVING;
            default:
                throw new IllegalArgumentException("Unknown combination");
            }
        }
    };

    public LSStrategy getStrategy();

    /**
     * Getter for the randomized flag
     * 
     * @return A flag defining whether the exploration should be randomized
     */
    public boolean randomize();

    /**
     * Getter for the first improvement flag
     * 
     * @return A flag stating whether the research should be thorough ( <code>false</code>) or accept the first
     *         improving move ( <code>true</code>)
     */
    public boolean acceptFirstImprovement();

    /**
     * Getter for the non improving flag
     * 
     * @return <code>true</code> if non improving moves should be accepted
     */
    public boolean acceptNonImproving();

    /**
     * Getter for the maximum number of iterations
     * 
     * @return A maximum number of iterations for the procedure
     */
    public int getMaxIterations();

    /**
     * Getter for the maximum time
     * 
     * @return A maximum time (in ms) for the procedure
     */
    public long getMaxTime();

    /**
     * Getter for the random stream
     * 
     * @return the random stream to be used
     */
    public RandomStream getRandomStream();

    /**
     * Getter for a custom acceptance criterion
     * 
     * @return the implementation of {@link IAcceptanceCriterion} to be used.
     */
    public IAcceptanceCriterion getAcceptanceCriterion();

    /**
     * Returns the stopping criterion associated with this parameter set.
     * <p>
     * Note that the returned stopping criterion should be coherent with the values returned by {@link #getMaxTime()}
     * and {@link #getMaxIterations()}
     * </p>
     * 
     * @return the stopping criterion associated with this parameter set
     */
    public IStoppingCriterion getStoppingCriterion();

}
