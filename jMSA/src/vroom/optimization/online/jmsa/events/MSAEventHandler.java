package vroom.optimization.online.jmsa.events;

import vroom.common.utilities.events.IEventHandler;
import vroom.optimization.online.jmsa.IInstance;
import vroom.optimization.online.jmsa.IScenario;
import vroom.optimization.online.jmsa.MSABase.MSAProxy;

/**
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #created 22-Feb-2010 02:28:00 p.m.
 */
public abstract class MSAEventHandler<E extends MSAEvent, S extends IScenario, I extends IInstance>
        implements IEventHandler<E> {

    /** The parent MSA instance */
    private final MSAProxy<S, I> mParentMSA;

    /**
     * Getter for the parent MSA instance proxy
     * 
     * @return the MSA instance proxy that is associated with this handler
     */
    protected MSAProxy<S, I> getParentMSAProxy() {
        return this.mParentMSA;
    }

    public MSAEventHandler(MSAProxy<S, I> parentMSA) {
        this.mParentMSA = parentMSA;
    }

    @Override
    public String toString() {
        return String.format("%s@%s", this.getClass().getSimpleName(), hashCode());
    }

}// end MSAEventHandler