package vroom.common.utilities.ssj;

import java.util.HashMap;
import java.util.Map;

import umontreal.iro.lecuyer.probdist.DiscreteDistributionInt;
import umontreal.iro.lecuyer.probdist.Distribution;
import umontreal.iro.lecuyer.randvar.RandomVariateGen;
import umontreal.iro.lecuyer.randvar.RandomVariateGenInt;
import umontreal.iro.lecuyer.rng.RandomStream;

/**
 * <code>RandomVariateManager</code> is a utility class that contains a mapping between {@linkplain Distribution
 * distributions} and {@linkplain RandomVariateGen random number generator}.
 * <p>
 * For type safety this class only handles 1-dimension distributions and associated generators.
 * <p>
 * Creation date: Apr 23, 2010 - 9:07:36 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class RandomGeneratorManager {

    /** Mapping between distributions and random number generators */
    private final Map<Distribution, RandomVariateGen> mRandomGeneratorsMap;

    /** The random stream that will be shared between all {@linkplain RandomVariateGen random generators} **/
    private RandomStream                              mRandomStream;

    /**
     * Getter for randomStream
     * 
     * @return The random stream that will be shared between all {@linkplain RandomVariateGen random generators}
     */
    public RandomStream getRandomStream() {
        return mRandomStream;
    }

    /**
     * Setter for the random stream that will be shared between all {@linkplain RandomVariateGen random generators}
     * 
     * @param randomStream
     *            the value to be set for randomStream
     * @throws IllegalStateException
     *             if the random stream was already defined
     */
    public void setRandomStream(RandomStream randomStream) {
        if (mRandomStream != null && mRandomStream != randomStream) {
            throw new IllegalStateException("The random stream for this manager has already been set: " + mRandomStream);
        }
        mRandomStream = randomStream;
    }

    /**
     * Getter for the random number generator associated with a distribution.
     * <p/>
     * If a random number generator is already associated with <code>dist</code> then it will be returned, otherwise a
     * generic number generator will be created, registered and returned.
     * <p/>
     * Note that a random stream has to be defined first
     * 
     * @param dist
     *            the distribution for which a random number generator is needed
     * @return the {@link RandomVariateGen} associated with the given distribution
     * @throws IllegalStateException
     *             if no random stream was defined
     * @see RandomVariateGen#RandomVariateGen(RandomStream, Distribution)
     * @see #setRandomStream(RandomStream)
     */
    public RandomVariateGen getRandomVariateGen(Distribution dist) {
        RandomVariateGen gen;
        if (!mRandomGeneratorsMap.containsKey(dist)) {
            if (mRandomStream == null) {
                throw new IllegalStateException("No random stream was defined for this instance");
            }
            gen = new RandomVariateGen(mRandomStream, dist);
            mRandomGeneratorsMap.put(dist, gen);
        } else {
            gen = mRandomGeneratorsMap.get(dist);
        }
        return gen;
    }

    /**
     * Getter for the random number generator associated with a distribution.
     * <p/>
     * If a random number generator is already associated with <code>dist</code> then it will be returned, otherwise a
     * generic number generator will be created, registered and returned.
     * <p/>
     * Note that a random stream has to be defined first
     * 
     * @param dist
     *            the distribution for which a random number generator is needed
     * @return the {@link RandomVariateGen} associated with the given distribution
     * @throws IllegalStateException
     *             if no random stream was defined
     * @see RandomVariateGen#RandomVariateGen(RandomStream, Distribution)
     * @see #setRandomStream(RandomStream)
     */
    public RandomVariateGenInt getRandomVariateGenInt(DiscreteDistributionInt dist) {
        RandomVariateGenInt gen;
        if (!mRandomGeneratorsMap.containsKey(dist)) {
            if (mRandomStream == null) {
                throw new IllegalStateException("No random stream was defined for this instance");
            }
            gen = new RandomVariateGenInt(mRandomStream, dist);
            mRandomGeneratorsMap.put(dist, gen);
        } else {
            gen = (RandomVariateGenInt) mRandomGeneratorsMap.get(dist);
        }
        return gen;
    }

    /**
     * Random number generation from a distribution
     * 
     * @param dist
     *            the distribution to be sampled
     * @return a random number generated from the given distribution
     * @see #getRandomVariateGen(Distribution)
     * @see RandomVariateGen#nextDouble()
     */
    public double nextDouble(Distribution dist) {
        return getRandomVariateGen(dist).nextDouble();
    }

    /**
     * Random number generation from a distribution
     * 
     * @param dist
     *            the distribution to be sampled
     * @return a random number generated from the given distribution
     * @see #getRandomVariateGenInt(DiscreteDistributionInt)
     * @see RandomVariateGenInt#nextInt()
     */
    public int nextInt(DiscreteDistributionInt dist) {
        return getRandomVariateGenInt(dist).nextInt();
    }

    /**
     * Creates a new <code>RandomVariateManager</code>
     * 
     * @param randomStream
     */
    public RandomGeneratorManager() {
        mRandomGeneratorsMap = new HashMap<Distribution, RandomVariateGen>();
    }

    /**
     * Creates a new <code>RandomGeneratorManager</code>
     * 
     * @param randomStream
     *            the {@link RandomStream} that will be used in the random generators.
     */
    public RandomGeneratorManager(RandomStream randomStream) {
        this();
        mRandomStream = randomStream;
    }

}
