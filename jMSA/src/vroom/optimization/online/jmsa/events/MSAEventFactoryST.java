package vroom.optimization.online.jmsa.events;

import vroom.optimization.online.jmsa.IActualRequest;
import vroom.optimization.online.jmsa.MSABase;
import vroom.optimization.online.jmsa.utils.MSALogging;

/**
 * <code>IMSAEventFactory</code> is the class responsible for the creation and raising of events in the MSA procedure
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:07:02 a.m.
 */
public class MSAEventFactoryST implements IMSAEventFactory {

    private final MSAEventQueue mEventQueue;

    private final MSABase<?, ?>   mParentMSA;

    /*
     * (non-Javadoc)
     * @see vroom.optimization.online.jmsa.events.IMSAEventFactory#getParentMSA()
     */
    @Override
    public MSABase<?, ?> getParentMSA() {
        return mParentMSA;
    }

    /**
     * @param eventQueue
     */
    public MSAEventFactoryST(MSABase<?, ?> parentMSA, MSAEventQueue eventQueue) {
        super();
        mEventQueue = eventQueue;
        mParentMSA = parentMSA;
    }

    /*
     * (non-Javadoc)
     * @see
     * vroom.optimization.online.jmsa.events.IMSAEventFactory#raiseDecisionEvent
     * ()
     */
    @Override
    public boolean raiseDecisionEvent() {
        return raiseEvent(new DecisionEvent(0, this));
    }

    /*
     * (non-Javadoc)
     * @see vroom.optimization.online.jmsa.events.IMSAEventFactory#
     * raiseRequestAssignedEvent(int,
     * vroom.optimization.online.jmsa.IActualRequest)
     */
    @Override
    public boolean raiseRequestAssignedEvent(int resourceId, IActualRequest assignedRequest) {
        return raiseEvent(ResourceEvent.newRequestAssignedEvent(0, this, resourceId,
                assignedRequest, null));
    }

    /*
     * (non-Javadoc)
     * @see
     * vroom.optimization.online.jmsa.events.IMSAEventFactory#raiseNewRequestEvent
     * (vroom.optimization.online.jmsa.IActualRequest)
     */
    @Override
    public boolean raiseNewRequestEvent(IActualRequest request) {
        return raiseEvent(new NewRequestEvent(0, this, request));
    }

    /*
     * (non-Javadoc)
     * @see vroom.optimization.online.jmsa.events.IMSAEventFactory#
     * raiseStartOfServiceEvent(int,
     * vroom.optimization.online.jmsa.IActualRequest, java.lang.Object)
     */
    @Override
    public boolean raiseStartOfServiceEvent(int resourceId, IActualRequest servedRequest,
            Object additionalInfo) {
        return servedRequest != null
                && raiseEvent(ResourceEvent.newStartOfServiceEvent(0, this, resourceId,
                        servedRequest, additionalInfo));
    }

    /*
     * (non-Javadoc)
     * @see
     * vroom.optimization.online.jmsa.events.IMSAEventFactory#raiseEndOfServiceEvent
     * (int, vroom.optimization.online.jmsa.IActualRequest)
     */
    @Override
    public boolean raiseEndOfServiceEvent(int resourceId, IActualRequest servedRequest) {
        return servedRequest != null
                && raiseEvent(ResourceEvent.newEndOfServiceEvent(0, this, resourceId,
                        servedRequest, null));
    }

    /*
     * (non-Javadoc)
     * @see
     * vroom.optimization.online.jmsa.events.IMSAEventFactory#raiseResourceStart
     * (int, java.lang.Object)
     */
    @Override
    public boolean raiseResourceStart(int resourceId, Object param) {
        return raiseEvent(ResourceEvent.newStartServiceEvent(0, this, resourceId, param));
    }

    /*
     * (non-Javadoc)
     * @see
     * vroom.optimization.online.jmsa.events.IMSAEventFactory#raiseResourceStop
     * (int, java.lang.Object)
     */
    @Override
    public boolean raiseResourceStop(int resourceId, Object param) {
        return raiseEvent(ResourceEvent.newStopServiceEvent(0, this, resourceId, param));

    }

    /*
     * (non-Javadoc)
     * @see
     * vroom.optimization.online.jmsa.events.IMSAEventFactory#raisePoolUpdateEvent
     * ()
     */
    @Override
    public boolean raisePoolUpdateEvent() {
        return raisePoolUpdateEvent(false);
    }

    /*
     * (non-Javadoc)
     * @see
     * vroom.optimization.online.jmsa.events.IMSAEventFactory#raisePoolUpdateEvent
     * (boolean)
     */
    @Override
    public boolean raisePoolUpdateEvent(boolean preemptive) {
        return raiseEvent(new PoolUpdateEvent(this, preemptive));
    }

    /*
     * (non-Javadoc)
     * @see vroom.optimization.online.jmsa.events.IMSAEventFactory#
     * raiseGenerateScenarioEvent()
     */
    @Override
    public boolean raiseGenerateScenarioEvent() {
        return raiseEvent(new GenerateEvent(this));
    }

    /*
     * (non-Javadoc)
     * @see
     * vroom.optimization.online.jmsa.events.IMSAEventFactory#raiseOptimizeEvent
     * ()
     */
    @Override
    public boolean raiseOptimizeEvent() {
        return raiseEvent(new OptimizeEvent(this));
    }

    /*
     * (non-Javadoc)
     * @see
     * vroom.optimization.online.jmsa.events.IMSAEventFactory#raiseEvent(vroom
     * .optimization.online.jmsa.events.MSAEvent)
     */
    @Override
    public boolean raiseEvent(MSAEvent event) {
        try {
            mEventQueue.pushEvent(event);
            return true;
        } catch (InterruptedException e) {
            MSALogging.getBaseLogger().exception("MSAEventFactoryST.raiseEvent", e);
            return false;
        }
    }

}// end IMSAEventFactory