package vroom.optimization.online.jmsa.components;

import vroom.optimization.online.jmsa.MSABase.MSAProxy;

/**
 * <code>MSAComponentBase</code> is the parent base type for all components of a MSA algorithm Please note that all
 * subclasses will be instantiated in the MSA procedure by calling
 * {@link MSAComponentBase#MSAComponentBase(ComponentManager)}
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:06:50 a.m.
 */
public abstract class MSAComponentBase {
    /**
     * The parent component manager
     */
    private final ComponentManager<?, ?> mComponentManager;

    /**
     * @return the parent component manager for this component
     */
    protected ComponentManager<?, ?> getComponentManager() {
        return mComponentManager;
    }

    /**
     * Creates a new component
     * 
     * @param componentManager
     *            the parent component manager
     */
    public MSAComponentBase(ComponentManager<?, ?> componentManager) {
        mComponentManager = componentManager;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    /**
     * Getter for the parent msa proxy
     * 
     * @return a proxy to the parent msa
     */
    public MSAProxy<?, ?> getMSAProxy() {
        return getComponentManager().getParentMSAProxy();
    }
}