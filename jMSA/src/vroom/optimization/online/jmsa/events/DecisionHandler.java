package vroom.optimization.online.jmsa.events;

import vroom.common.utilities.events.EventHandlingException;
import vroom.common.utilities.events.IEventHandler;
import vroom.optimization.online.jmsa.IDistinguishedSolution;
import vroom.optimization.online.jmsa.IInstance;
import vroom.optimization.online.jmsa.IScenario;
import vroom.optimization.online.jmsa.MSABase.MSAProxy;
import vroom.optimization.online.jmsa.utils.MSALogging;

/**
 * <code>DecisionHandler<code> is an implementation of  {@link
 * IEventHandler}responsible of the handling of {@link DecisionEvent} events.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:06:58 a.m.
 */
public class DecisionHandler<S extends IScenario, I extends IInstance> extends
        MSAEventHandler<DecisionEvent, S, I> {

    public DecisionHandler(MSAProxy<S, I> parentMSA) {
        super(parentMSA);
    }

    @Override
    public boolean canHandleEvent(DecisionEvent event) {
        return true;
    }

    @Override
    public boolean handleEvent(DecisionEvent event) throws EventHandlingException {

        IDistinguishedSolution sol = getParentMSAProxy().getComponentManager()
                .buildDistinguishedPlan(null);

        MSALogging.getEventsLogger().info(
                "DecisionHandler.handleEvent: Distinguished solution build (%s)", sol);

        getParentMSAProxy().setDistinguishedSolution(sol);

        return true;
    }
}