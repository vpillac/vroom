/*
 * 
 */
package vroom.common.utilities.callbacks;

/**
 * <code>ICallbackEvent</code> is the interface to events that are susceptible to have callback associated to.
 * 
 * @see ICallback
 * @see CallbackManagerDelegate
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:07:41 a.m.
 */
public interface ICallbackEvent<S, T extends ICallbackEventTypes> {

    /**
     * @return a description of this event
     */
    public String getDescription();

    /**
     * Getter for <code>type</code>
     * 
     * @return the type of event
     */
    public T getType();

    /**
     * Getter for <code>timeStamp</code>
     * 
     * @return the timeStamp associated with this event
     */
    public long getTimeStamp();

    /**
     * Getter for <code>source</code>
     * 
     * @return the object that originated this event
     */
    public S getSource();

    /**
     * Getter for <code>params</code>
     * 
     * @return the parameteres associated with this event
     */
    public Object[] getParams();

}