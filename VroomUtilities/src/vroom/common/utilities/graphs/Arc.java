package vroom.common.utilities.graphs;

/**
 * The Class<code>Arc</code> is a simple representation of an arc as a pair of integers
 * <p>
 * Creation date: 14 août 2010 - 18:26:46.
 * 
 * @author Victor Pillac <br/>
 *         <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <br/>
 *         <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class Arc {

    /** An optional id for this arc */
    public final int    id;

    /** The tail of this arc */
    public final int    tail;

    /** The head of this arc */
    public final int    head;

    /** The cost associated with this arc */
    public final double cost;

    /** The capacity of this arc */
    public final double capacity;

    /**
     * Creates a new <code>Arc</code>
     * 
     * @param id
     *            an optional id for this arc
     * @param tail
     *            this arc tail node
     * @param head
     *            this arc head node
     * @param cost
     *            a cost associated with this arc
     * @param capacity
     *            the capacity of this arc
     */
    public Arc(int id, int tail, int head, double cost, double capacity) {
        super();
        this.id = id;
        this.tail = tail;
        this.head = head;
        this.cost = cost;
        this.capacity = capacity;
    }

}
