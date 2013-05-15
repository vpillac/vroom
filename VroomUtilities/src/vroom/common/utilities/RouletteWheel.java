/**
 * 
 */
package vroom.common.utilities;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import umontreal.iro.lecuyer.rng.RandomStream;

/**
 * <code>RouletteWheel</code> is a simple implementation of a roulette wheel.
 * <p>
 * Creation date: May 12, 2011 - 2:03:33 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class RouletteWheel<O> {

    private final List<WheelElement<O>> mWheel;

    private boolean                     mChanged;

    /**
     * Creates a new empty <code>RouletteWheel</code>
     */
    public RouletteWheel() {
        mWheel = new LinkedList<WheelElement<O>>();
    }

    /**
     * Creates a new <code>RouletteWheel</code>
     * 
     * @param evaluations
     *            a mapping between objects and their evaluation
     */
    public RouletteWheel(Map<O, Double> evaluations) {
        this();
        addAll(evaluations);
    }

    /**
     * Add an object to this wheel.
     * <p>
     * If this wheel already contains the object, then it will be added a second time.
     * </p>
     * 
     * @param object
     *            the object to be added
     * @param evaluation
     *            the evaluation of the added <code>object</code>
     */
    public void add(O object, double evaluation) {
        if (evaluation < 0)
            throw new IllegalArgumentException("Evaluation must be positive");
        mWheel.add(new WheelElement<O>(object, evaluation));
        mChanged = true;
    }

    /**
     * Add all the object from a map
     * 
     * @param evaluations
     *            a mapping between objects and their evaluation
     * @see #add(Object, double)
     */
    public void addAll(Map<O, Double> evaluations) {
        for (Entry<O, Double> e : evaluations.entrySet()) {
            add(e.getKey(), e.getValue());
        }
    }

    /**
     * Returns <code>true</code> if this wheel is empty
     * 
     * @return <code>true</code> if this wheel is empty
     */
    public boolean isEmpty() {
        return mWheel.isEmpty();
    }

    /**
     * Returns the number of objects in this wheel
     * 
     * @return the number of objects in this wheel
     */
    public int size() {
        return mWheel.size();
    }

    /**
     * Draw an object from this wheel
     * 
     * @param stream
     *            the {@link RandomStream} that will be used
     * @param removeAfterDraw
     *            <code>true</code> if the object should be removed after being drawn
     * @return the drawn object
     */
    public O drawObject(RandomStream stream, boolean removeAfterDraw) {
        if (size() == 0)
            return null;
        else if (size() == 1)
            return mWheel.get(0).object;

        updateWheel();

        double rnd = stream.nextDouble();
        Iterator<WheelElement<O>> it = mWheel.iterator();
        double acc = 0;
        WheelElement<O> e = null;
        while (it.hasNext() && acc < rnd) {
            e = it.next();
            acc += e.evaluation;
        }
        O object = e != null ? e.object : null;

        if (removeAfterDraw && object != null) {
            it.remove();
            mChanged = true;
        }

        return object;
    }

    /**
     * Calculate the probability of each wheel element and sort the wheel
     */
    private void updateWheel() {
        if (!mChanged)
            return;

        double cumulated = 0;
        for (WheelElement<O> e : mWheel) {
            cumulated += e.evaluation;
        }
        for (WheelElement<O> e : mWheel) {
            e.evaluation = e.evaluation / cumulated;
        }
        Collections.sort(mWheel);

        mChanged = false;
    }

    /**
     * Reset this selection wheel
     */
    public void clear() {
        mWheel.clear();
        mChanged = false;
    }

    @Override
    public String toString() {
        updateWheel();

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (WheelElement<O> e : mWheel) {
            if (sb.length() > 1)
                sb.append(",");
            sb.append(e.toString());
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * <code>WheelElement</code> representation of a wheel element for the random selection of an object
     * <p>
     * Creation date: 2 juil. 2010 - 21:00:56
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    class WheelElement<OO> implements Comparable<WheelElement<O>> {
        final O object;
        double  evaluation;

        /**
         * Creates a new <code>WheelElement</code>
         * 
         * @param object
         * @param evaluation
         */
        public WheelElement(O object, double probability) {
            this.object = object;
            this.evaluation = probability;
        }

        @Override
        public int compareTo(WheelElement<O> o) {
            return Double.compare(evaluation, o.evaluation);
        }

        @Override
        public String toString() {
            return String.format("[%s,%.4f]", object, evaluation);
        }
    }

}
