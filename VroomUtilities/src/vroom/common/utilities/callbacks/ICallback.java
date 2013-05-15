/*
 * 
 */
package vroom.common.utilities.callbacks;

/**
 * <code>ICallBack</code> is the interface for callbacks that can be associated to a MSA procedure
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:07:40 a.m.
 */
public interface ICallback<S, T extends ICallbackEventTypes> extends Comparable<ICallback<?, ?>> {

    /**
     * Execute this callback
     * 
     * @param event
     *            the event that caused the execution of this method
     */
    public void execute(ICallbackEvent<S, T> event);

    /**
     * Getter for the priority of this callback. <br/>
     * Note that callbacks with lower priority values will be called first
     * 
     * @return the priority of this callback
     */
    public int getPriority();

    /**
     * Synchronous/Asynchronous execution of this callback
     * 
     * @return <code>true</code> if this callback has to be executed synchronously (in the same thread that called its
     *         execution), <code>false</code> if it can be executed asynchronously (in a thread different from the
     *         caller)
     */
    public boolean isExecutedSynchronously();

}