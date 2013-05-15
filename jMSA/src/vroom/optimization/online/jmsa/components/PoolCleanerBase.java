package vroom.optimization.online.jmsa.components;

import java.util.Collection;

import vroom.optimization.online.jmsa.IDistinguishedSolution;
import vroom.optimization.online.jmsa.IInstance;
import vroom.optimization.online.jmsa.IMSARequest;
import vroom.optimization.online.jmsa.IScenario;

/**
 * <code>PoolCleanerBase</code> is the base type for classes that are responsible for the removal of scenarios that are
 * incompatible with the current state of the system<br/>
 * 
 * @param the
 *            type of scenario that will be handled by instances of this class
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:06:51 a.m.
 */
public abstract class PoolCleanerBase extends MSAComponentBase {

    public PoolCleanerBase(ComponentManager<?, ?> componentManager) {
        super(componentManager);
    }

    /**
     * Remove the scenarios from the pool that are incompatible with the current state of the instance.
     * 
     * @return a collection containing the scenarios that were removed
     */
    public abstract Collection<IScenario> cleanPool();

    // /**
    // * Remove the scenarios from the pool that are incompatible with
    // * the given distinguished mSolution.
    // *
    // * @param distinguishedSolution the mSolution that is being enforced
    // * @return a collection containing the scenarios that were removed
    // *
    // * @see #isScenarioCompatible(IDistinguishedSolution, IScenario)
    // */
    // public abstract Collection<IScenario> cleanPool(IDistinguishedSolution
    // distinguishedSolution);

    /**
     * Test whether or not a scenario is compatible with the current state of the system represented by the
     * <code>instance</code>
     * 
     * @param instance
     *            the instance representing the current state of the system
     * @param scenario
     *            the scenario to test
     */
    public abstract boolean isScenarioCompatible(IInstance instance, IScenario scenario);

    /**
     * Test whether or not a scenario is compatible with the given <code>request</code>
     * 
     * @param request
     *            the considered request
     * @param scenario
     *            the scenario to test
     */
    public abstract boolean isScenarioCompatible(IMSARequest request, IScenario scenario);

    /**
     * Test whether or not a scenario is compatible with the given distinguished mSolution
     * 
     * @param mSolution
     *            the considered distinguished mSolution
     * @param scenario
     *            the scenario to test
     */
    public boolean isScenarioCompatible(IDistinguishedSolution solution, IScenario scenario) {
        if (solution == null) {
            return true;
        }

        boolean comp = true;

        int r = 0;

        scenario.acquireLock();

        while (r < scenario.getResourceCount() && comp) {
            comp &= scenario.getFirstActualRequest(r) == solution.getNextRequest(r)
                    || scenario.getFirstActualRequest(r) != null
                    && scenario.getFirstActualRequest(r).equals(solution.getNextRequest(r));
            r++;
        }

        scenario.releaseLock();

        return comp;
    }

}