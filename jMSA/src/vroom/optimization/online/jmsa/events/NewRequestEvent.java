package vroom.optimization.online.jmsa.events;

import vroom.optimization.online.jmsa.IActualRequest;

/**
 * <code>NewRequestEvent</code> is a preemptive event that raised whenever a new <it>request</it> is become known. If
 * the request is accepted it triggers its insertion into the currently present scenarios, and the removal of
 * incompatible scenarios; if not it triggers an optimization of scenarios.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:06:59 a.m.
 */
public class NewRequestEvent extends MSAEvent {

    private final IActualRequest mNewRequest;

    /**
     * Creates a new <code>NewRequestEvent</code>
     * 
     * @param simTime
     *            the time at which this event occurred in a simulation setting (optional)
     * @param source
     * @param request
     *            the new request
     */
    public NewRequestEvent(double simTime, IMSAEventFactory source, IActualRequest request) {
        super(IMSAEventFactory.PRIORITY_NEW_REQUEST, simTime, source);
        mNewRequest = request;
    }

    @Override
    public boolean isPreemptive() {
        return true;
    }

    /**
     * @return the new request associated with this event
     */
    public IActualRequest getNewRequest() {
        return mNewRequest;
    }

    @Override
    public String toString() {
        return String.format("%s (req:%s)", super.toString(), getNewRequest());
    }

    /*
     * (non-Javadoc)
     * @see vroom.common.utilities.IToShortString#toShortString()
     */
    @Override
    public String toShortString() {
        return getNewRequest() != null ? getNewRequest().toString() : "null";
    }
}