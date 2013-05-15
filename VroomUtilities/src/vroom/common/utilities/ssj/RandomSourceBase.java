package vroom.common.utilities.ssj;

import umontreal.iro.lecuyer.rng.MRG32k3a;
import umontreal.iro.lecuyer.rng.RandomStream;

/**
 * The Class <code>RandomSourceBase</code> is an implementation of {@link IRandomSource} providing the base for generic
 * random sources.
 * <p>
 * Creation date: Oct 11, 2010 - 3:42:36 PM.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class RandomSourceBase implements IRandomSource {

    private RandomStream mRandomStream;

    /**
     * Creates a new <code>RandomSourceBase</code> with a default {@link MRG32k3a} random stream based on the current
     * time.
     */
    public RandomSourceBase(RandomStream stream) {
        super();
        setRandomStream(stream);
    }

    /**
     * Creates a new <code>RandomSourceBase</code> with a default {@link MRG32k3a} random stream based on the current
     * time.
     */
    public RandomSourceBase() {
        super();
        long[] seeds = new long[6];
        for (int i = 0; i < seeds.length; i++) {
            seeds[i] = System.currentTimeMillis() % 4294967087l + i;
        }
        MRG32k3a stream = new MRG32k3a(this.getClass().getName());
        stream.setSeed(seeds);
        setRandomStream(stream);
    }

    /* (non-Javadoc)
     * @see vroom.common.utilities.ssj.IRandomSource#setRandomStream(umontreal.iro.lecuyer.rng.RandomStream)
     */
    @Override
    public void setRandomStream(RandomStream stream) {
        mRandomStream = stream;
    }

    /**
     * Set up the {@link #getRandomStream() random stream} with a new {@link MRG32k3a} and the given <code>seeds</code>
     * 
     * @param seeds
     * @param name
     */
    public void setMRG32k3aRndStream(long seed, String name) {
        long[] seeds = new long[6];
        for (int i = 0; i < seeds.length; i++) {
            seeds[i] = Double.doubleToLongBits(Math.pow(seed + 1, i + 1)) % 4294967087l;
        }
        setMRG32k3aRndStream(seeds, name);
    }

    /**
     * Set up the {@link #getRandomStream() random stream} with a new {@link MRG32k3a} and the given <code>seeds</code>
     * 
     * @param seeds
     * @param name
     */
    public void setMRG32k3aRndStream(long[] seeds, String name) {
        MRG32k3a stream = new MRG32k3a(name);
        stream.setSeed(seeds);
        setRandomStream(stream);

    }

    /* (non-Javadoc)
     * @see vroom.common.utilities.ssj.IRandomSource#getRandomStream()
     */
    @Override
    public RandomStream getRandomStream() {
        return mRandomStream;
    }

}
