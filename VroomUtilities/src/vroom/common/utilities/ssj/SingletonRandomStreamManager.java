/**
 * 
 */
package vroom.common.utilities.ssj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import umontreal.iro.lecuyer.rng.MRG32k3a;
import umontreal.iro.lecuyer.rng.RandomStream;
import umontreal.iro.lecuyer.rng.RandomStreamManager;

/**
 * The class <code>RuntimeBaseRandomStreamManager</code> is a replacement of {@link RandomStreamManager} that is based
 * on a mapping between streams and current {@link ThreadGroup}. The idea is to maintain in a central place all the
 * random streams of the JVM and ensure that each {@link ThreadGroup} has only access to its own
 * {@linkplain RandomStream random streams}.
 * <p>
 * Creation date: Oct 8, 2010 - 5:03:28 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class SingletonRandomStreamManager {

    private static final SingletonRandomStreamManager         INSTANCE = new SingletonRandomStreamManager();

    private final Map<ThreadGroup, Map<String, RandomStream>> mStreams;

    /**
     * @return the singleton instance
     */
    public static SingletonRandomStreamManager getInstance() {
        return INSTANCE;
    }

    private SingletonRandomStreamManager() {
        mStreams = new HashMap<ThreadGroup, Map<String, RandomStream>>();
    }

    /**
     * @return the streams defined for the current thread group
     */
    private Map<String, RandomStream> getCurrentThreadGroupStreams() {
        ThreadGroup cur = Thread.currentThread().getThreadGroup();

        Map<String, RandomStream> streams;
        if (mStreams.containsKey(cur)) {
            streams = mStreams.get(cur);
        } else {
            streams = new HashMap<String, RandomStream>();
            mStreams.put(cur, streams);
        }

        return streams;
    }

    /**
     * Adds the given stream to the internal list of this random stream manager and returns the added stream.
     * <p>
     * Any previous stream associated with the same key will be discarded
     * </p>
     * 
     * @param key
     *            a unique key to identify this stream within this {@link ThreadGroup}
     * @param stream
     *            the {@link RandomStream} to be added
     * @return the newly added stream.
     */
    public RandomStream add(String key, RandomStream stream) {
        getCurrentThreadGroupStreams().put(key, stream);
        return stream;
    }

    /**
     * Adds a {@link MRG32k3a} random stream for the current {@link ThreadGroup} and the given <code>key</code>.
     * 
     * @param key
     *            a unique key to identify this stream within this {@link ThreadGroup}
     * @param name
     *            a name for the {@link RandomStream}
     * @param seeds
     *            seeds for the {@link MRG32k3a} random stream, if <code>null</code> the default value will be used (see
     *            {@link MRG32k3a#setPackageSeed(long[])})
     * @return the stream that was added
     */
    public MRG32k3a addMRG32k3a(String key, String name, long[] seeds) {
        MRG32k3a stream = new MRG32k3a(name);
        if (seeds != null) {
            stream.setSeed(seeds);
        }
        add(key, stream);
        return stream;
    }

    /**
     * Removes all the streams from the internal list of this random stream manager.
     */
    public void clear() {
        getCurrentThreadGroupStreams().clear();
    }

    /**
     * Returns an unmodifiable list containing all the random streams in this random stream manager.
     * 
     * @return
     */
    public List<RandomStream> getStreams() {
        return new ArrayList<RandomStream>(getCurrentThreadGroupStreams().values());
    }

    /**
     * Access a specific random stream
     * 
     * @param key
     *            a unique key that identify the desired stream
     * @return
     */
    public RandomStream getStream(String key) {
        return getCurrentThreadGroupStreams().get(key);
    }

    /**
     * Removes the given stream from the internal list of this random stream manager.
     * 
     * @param key
     *            the key for which the random stream has to be removed
     * @return
     */
    public boolean remove(String key) {
        return getCurrentThreadGroupStreams().remove(key) != null;
    }

    /**
     * Forwards to the resetNextSubstream methods of all streams in the list.
     */
    public void resetNextSubstream() {
        for (RandomStream stream : getCurrentThreadGroupStreams().values()) {
            stream.resetNextSubstream();
        }
    }

    /**
     * Forwards to the resetStartStream methods of all streams in the list.
     */
    public void resetStartStream() {
        for (RandomStream stream : getCurrentThreadGroupStreams().values()) {
            stream.resetStartStream();
        }
    }

    /**
     * Forwards to the resetStartSubstream methods of all streams in the list.
     */
    public void resetStartSubstream() {
        for (RandomStream stream : getCurrentThreadGroupStreams().values()) {
            stream.resetStartSubstream();
        }
    }
}
