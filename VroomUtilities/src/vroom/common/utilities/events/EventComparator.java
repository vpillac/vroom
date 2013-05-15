/**
 * 
 */
package vroom.common.utilities.events;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Creation date: Apr 12, 2010 - 2:38:21 PM<br/>
 * <code>EventComparator</code>
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class EventComparator implements Comparator<IEvent<?>>, Serializable {

    /** The default priority factor (one week) */
    public static final int   DEFAULT_PRIORITY_FACTOR = 1000 * 3600 * 12;

    private static final long serialVersionUID        = 1L;
    private final int         mPriorityFactor;

    /**
     * Creates a new <code>EventComparator</code>
     * 
     * @param priorityFactor
     *            a factor used during comparison to prevent collisions between between events with different priorities
     *            (in ms)
     * @see #compare(IEvent, IEvent)
     */
    public EventComparator(int priorityFactor) {
        super();
        mPriorityFactor = priorityFactor;
    }

    /**
     * Creates a new <code>EventComparator</code> with a <code>priorityFactor</code> of <code>604800000</code> (one
     * week)
     */
    public EventComparator() {
        this(DEFAULT_PRIORITY_FACTOR);
    }

    /**
     * Compare two events depending on their priority and time stamp.
     * <p>
     * If <code>e1</code> and <code>e2</code> have different priorities, then the returned result is equal to
     * <code>(e1.getPriority()-e2.getPriority())*priorityFactor</code> where <code>priorityFactor</code> is the number
     * defined in the constructor {@link #EventComparator(int)}
     * <p>
     * If <code>e1</code> and <code>e2</code> have the same priority then the returned result is equal to
     * <code>e1.getTimeStamp()-e2.getTimeStamp()</code>
     * 
     * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater
     *         than the second.
     * @see Comparator#compare(Object, Object)
     * @see IEvent#getPriority()
     * @see IEvent#getTimeStamp()
     */
    @Override
    public int compare(IEvent<?> e1, IEvent<?> e2) {
        return compareEvents(e1, e2, mPriorityFactor);
    }

    /**
     * Compare two events depending on their priority and time stamp.
     * 
     * @param e1
     *            the first event to compare
     * @param e2
     *            the second event to compare
     * @param priorityFactor
     *            a factor used during comparison to prevent collisions between between events with different priorities
     *            (in ms)
     *            <p>
     *            If <code>e1</code> and <code>e2</code> have different priorities, then the returned result is equal to
     *            <code>(e1.getPriority()-e2.getPriority())*priorityFactor</code> where <code>priorityFactor</code> is
     *            the number defined in the constructor {@link #EventComparator(int)}
     *            <p>
     *            If <code>e1</code> and <code>e2</code> have the same priority then the returned result is equal to
     *            <code>e1.getTimeStamp()-e2.getTimeStamp()</code>
     * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater
     *         than the second.
     * @see Comparator#compare(Object, Object)
     * @see IEvent#getPriority()
     * @see IEvent#getTimeStamp()
     */
    public static int compareEvents(IEvent<?> e1, IEvent<?> e2, int priorityFactor) {
        if (e1 != null) {
            if (e2 == null) {
                return Integer.MAX_VALUE;
            } else {
                if (e1.getPriority() != e2.getPriority()) {
                    return (e1.getPriority() - e2.getPriority()) * priorityFactor;
                } else {
                    return (int) (e1.getTimeStamp() - e2.getTimeStamp());
                }
            }
        } else if (e2 != null) {
            return Integer.MIN_VALUE;
        }
        return 0;
    }

    /**
     * Compare two events depending on their priority and time stamp.
     * 
     * @param e1
     *            the first event to compare
     * @param e2
     *            the second event to compare
     *            <p>
     *            If <code>e1</code> and <code>e2</code> have different priorities, then the returned result is equal to
     *            <code>(e1.getPriority()-e2.getPriority())*priorityFactor</code> where <code>priorityFactor</code> is
     *            the number defined in the constructor {@link #EventComparator(int)}
     *            <p>
     *            If <code>e1</code> and <code>e2</code> have the same priority then the returned result is equal to
     *            <code>e1.getTimeStamp()-e2.getTimeStamp()</code>
     * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater
     *         than the second.
     * @see Comparator#compare(Object, Object)
     * @see IEvent#getPriority()
     * @see IEvent#getTimeStamp()
     */
    public static int compareEvents(IEvent<?> e1, IEvent<?> e2) {
        return compareEvents(e1, e2, DEFAULT_PRIORITY_FACTOR);
    }
}
