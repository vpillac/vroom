/*
 * 
 */
package vroom.common.utilities;

/**
 * <code>ProxyObserver</code> is a proxy class that allows reference separation between an {@link IObserver observer}
 * and {@link IObservable observable}.
 * <p>
 * It is in particular useful when {@link IObserver observers} cannot keep track of all the {@link IObservable} they
 * monitor, leading to memory leaks
 * </p>
 * <p>
 * Creation date: Sep 6, 2010 - 6:42:55 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class ObserverProxy implements IObserver {

    private IObserver mParentObserver;

    /**
     * Creates a new <code>ProxyObserver</code>
     * 
     * @param parentObserver
     */
    public ObserverProxy(IObserver parentObserver) {
        if (parentObserver == null) {
            throw new IllegalArgumentException("Argument parentObserver cannot be null");
        }

        mParentObserver = parentObserver;
    }

    /* (non-Javadoc)
     * @see vroom.common.utilities.IObserver#update(vroom.common.utilities.IObservable, vroom.common.utilities.Update)
     */
    @Override
    public void update(IObservable source, Update update) {
        if (mParentObserver != null) {
            mParentObserver.update(source, update);
        }
    }

    /**
     * Detach the parent observer from all the {@link IObservable observable} objects in which it was referenced
     */
    public void detach() {
        mParentObserver = null;
    }

    /**
     * Test if this proxy is still attached
     * 
     * @return <code>true</code> if this proxy has been detached from its parent
     */
    public boolean isDetached() {
        return mParentObserver == null;
    }
}
