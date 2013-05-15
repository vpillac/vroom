package vroom.optimization.online.jmsa.events;

import vroom.common.utilities.IToShortString;
import vroom.common.utilities.events.EventBase;

/**
 * <code>MSAEvent</code> is the base type for all events used in a MSA procedure
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:07:03 a.m.
 */
public abstract class MSAEvent extends EventBase<IMSAEventFactory> implements IToShortString {

    private final Object mAdditionalInformation;

    private final double mSimulationTimeStamp;

    /**
     * Creates a new <code>MSAEvent</code>
     * 
     * @param priority
     *            the priority of this event
     * @param simTime
     *            the time at which this event occurred in a simulation setting (optional)
     * @param source
     *            the instance of {@link IMSAEventFactory} that generated this event
     * @param additionalInformation
     *            an optional object that can be used to carry additional information on this event
     */
    protected MSAEvent(int priority, double simTime, IMSAEventFactory source, Object additionalInformation) {
        super(priority, source);
        mAdditionalInformation = additionalInformation;
        mSimulationTimeStamp = simTime;
    }

    /**
     * Creates a new <code>MSAEvent</code>
     * 
     * @param priority
     *            the priority of this event
     * @param simTime
     *            the time at which this event occurred in a simulation setting (optional)
     * @param source
     *            the instance of {@link IMSAEventFactory} that generated this event
     */
    protected MSAEvent(int priority, double simTime, IMSAEventFactory source) {
        this(priority, simTime, source, null);
    }

    /**
     * Optional additional information carried by this event
     * 
     * @return an object (possibly <code>null</code>) representing additional information that has been passed when
     *         generating the event
     */
    public Object getAdditionalInformation() {
        return mAdditionalInformation;
    }

    /**
     * Returns the time at which this event occurred in a simulation setting (optional)
     * 
     * @return the time at which this event occurred in a simulation setting (optional)
     */
    public double getSimulationTimeStamp() {
        return mSimulationTimeStamp;
    }

    /*
     * (non-Javadoc)
     * @see vroom.common.utilities.IToShortString#toShortString()
     */
    @Override
    public String toShortString() {
        return "";
    }

}// end MSAEvent