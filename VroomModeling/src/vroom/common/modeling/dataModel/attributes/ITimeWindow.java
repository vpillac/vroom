package vroom.common.modeling.dataModel.attributes;

import vroom.common.modeling.dataModel.Node;

/**
 * The Interface ITimeWindow represent a time window that can be associated either with a node or a node.
 * <p>
 * By convention the time windows defines bounds for the arrival time at a node, without considering the service time.
 * In other words the vehicle has to arrive at the corresponding {@link node} or {@link Node} after
 * {@link #startAsDouble()} and before {@link #endAsDouble()}
 * </p>
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #created 15-Feb-2010 11:29:50 a.m.
 */
public interface ITimeWindow extends IRequestAttribute, INodeAttribute {

    /**
     * End as double.
     * 
     * @return the end date of this time window as a
     */
    public double endAsDouble();

    /**
     * <code>true</code> if the end of the time windows is soft, <code>false</code> otherwise.
     * 
     * @return true, if is soft end
     */
    public boolean isSoftEnd();

    /**
     * <code>true</code> if the start of the time windows is soft, <code>false</code> otherwise.
     * 
     * @return true, if is soft start
     */
    public boolean isSoftStart();

    /**
     * Start as double.
     * 
     * @return the start date of this time window as a
     */
    public double startAsDouble();

    /**
     * Returns <code>true</code> if the <code>arrivalTime</code> is within this time window
     * 
     * @param arrivalTime
     *            the arrival time at the associated node
     * @return <code>{@link #startAsDouble()}<=arrivalTime && arrivalTime<={@link #endAsDouble()}</code>
     */
    public boolean isWithinTW(double arrivalTime);

    /**
     * Returns <code>true</code> if the <code>arrivalTime</code> is feasible regarding this time window
     * 
     * @param arrivalTime
     *            the arrival time at the associated node
     * @return <code>arrivalTime<={@link #endAsDouble()}</code>
     */
    public boolean isFeasible(double arrivalTime);

    /**
     * Returns the violation of this time window when the associated node is visited at <code>arrivalTime</code>
     * 
     * @param arrivalTime
     *            the arrival time at the associated node
     * @return <code>max(0,{@link #endAsDouble()}-arrivalTime)</code>
     */
    public double getViolation(double arrivalTime);

    /**
     * Returns the waiting time when the associated node is visited at <code>arrivalTime</code>
     * 
     * @param arrivalTime
     *            the arrival time at the associated node
     * @return <code>max(0,arrivalTime-{@link #startAsDouble()})</code>
     */
    public double getWaiting(double arrivalTime);

    /**
     * Returns the earliest time of the start of service at the associated node
     * 
     * @param arrivalTime
     *            the arrival time at the associated node
     * @return <code>max({@link #startAsDouble()},arrivalTime)</code>
     */
    public double getEarliestStartOfService(double arrivalTime);

    /**
     * Return the width of this time window ({@code  end-start})
     * 
     * @return the width of this time window
     */
    public double width();

}