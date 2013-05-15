package vroom.optimization.online.jmsa.events;

/**
 * <code>DecisionEvent</code> is a preemptive event that triggers the generation of the current distinguished mSolution
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:06:57 a.m.
 */
public class DecisionEvent extends MSAEvent {

    /**
     * Creates a new <code>DecisionEvent</code>
     * 
     * @param simTime
     *            the time at which this event occurred in a simulation setting (optional)
     * @param source
     */
    public DecisionEvent(double simTime, IMSAEventFactory source) {
        super(IMSAEventFactory.PRIORITY_DECISION, simTime, source);
    }

    @Override
    public boolean isPreemptive() {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see vroom.common.utilities.IToShortString#toShortString()
     */
    @Override
    public String toShortString() {
        return "";
    }
}