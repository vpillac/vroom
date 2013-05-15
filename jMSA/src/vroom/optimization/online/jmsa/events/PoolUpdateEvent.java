package vroom.optimization.online.jmsa.events;

/**
 * <code>PoolUpdateEvent</code> is a preemptive event that triggers the removal of incompatible from the pool.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:07:01 a.m.
 */
public class PoolUpdateEvent extends MSAEvent {

    private final boolean mPreemptive;

    /**
     * Creates a new non-preemptive <code>PoolUpdateEvent</code>
     * 
     * @param source
     *            the {@link IMSAEventFactory} that generated this event
     * @see #PoolUpdateEvent(IMSAEventFactory, boolean)
     */
    protected PoolUpdateEvent(IMSAEventFactory source) {
        this(source, false);
    }

    /**
     * Creates a new <code>PoolUpdateEvent</code>
     * 
     * @param source
     *            the {@link IMSAEventFactory} that generated this event
     * @param preemptive
     *            the prehemptivity of this event
     */
    protected PoolUpdateEvent(IMSAEventFactory source, boolean preemptive) {
        super(preemptive ? IMSAEventFactory.PRIORITY_POOL_UPDATE_PRE
                : IMSAEventFactory.PRIORITY_POOL_UPDATE, 0, source);
        mPreemptive = preemptive;
    }

    @Override
    public boolean isPreemptive() {
        return mPreemptive;
    }

}