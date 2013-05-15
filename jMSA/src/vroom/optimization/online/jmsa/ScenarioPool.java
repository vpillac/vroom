package vroom.optimization.online.jmsa;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import vroom.optimization.online.jmsa.utils.MSALogging;

public class ScenarioPool<S extends IScenario> implements Iterable<S> {

    /** The set of scenarios contained in this pool */
    private final Set<S> mScenarios;

    /** The maximum number of scenarios in this pool **/
    private int          mCapacity;

    /**
     * Getter for capacity : The maximum number of scenarios in this pool
     * 
     * @return the value of maxSize
     */
    public int getCapacity() {
        return this.mCapacity;
    }

    /**
     * Returns the remaining capacity : the number of additional scenarios that can be added to this pool
     * 
     * @return the remaining capacity
     */
    public int getRemainingCapacity() {
        return getCapacity() - size();
    }

    /**
     * Setter for capacity : The maximum number of scenarios in this pool
     * 
     * @param capacity
     *            the value to be set for maxSize
     */
    public final void setCapacity(int capacity) {
        this.mCapacity = capacity;
    }

    /**
     * Creates a new <code>ScenarioPool</code>, with te given maximum size
     * 
     * @param maxSize
     */
    public ScenarioPool(int maxSize) {
        super();
        this.mScenarios = Collections.synchronizedSet(new HashSet<S>(maxSize / 2));
        setCapacity(maxSize);
    }

    /**
     * Current size of the pool
     * 
     * @return the number of scenarios in this pool
     */
    public int size() {
        return this.mScenarios.size();
    }

    /**
     * Remaining capacity
     * 
     * @return <code>true</code> is the pool cannot include more scenarios, <code>false</code> otherwise.
     */
    public boolean isFull() {
        return size() == getCapacity();
    }

    /**
     * Adds a scenario to this pool
     * 
     * @param scenario
     *            the scenario to be added in the pool
     * @return <code>true</code> if the scenario was successfully added
     */
    public synchronized boolean addScenario(S scenario) {
        return this.mScenarios.add(scenario);
    }

    @Override
    public Iterator<S> iterator() {
        return this.mScenarios.iterator();
    }

    /**
     * Return a cpoy of the scenario pool
     * 
     * @return a set containing the scenarios of this pool
     */
    public Set<S> getScenarios() {
        return new HashSet<S>(this.mScenarios);
    }

    /**
     * Removal of all the given scenarios from the pool
     * 
     * @param scenarios
     *            a collection containing the scenarios to be removed
     * @return <code>true</code> if the pool has been changed by this operation
     */
    public boolean removeScenarios(Collection<? extends IScenario> scenarios) {
        MSALogging.getComponentsLogger().info(
                "ScenarioPool.removeScenarios: Removing the following %s scenarios out of %s: %s",
                scenarios.size(), this.size(), scenarios);

        boolean b = this.mScenarios.removeAll(scenarios);

        for (IScenario s : scenarios) {
            s.dereference();
        }

        MSALogging.getComponentsLogger().lowDebug("ScenarioPool.removeScenarios:  >new pool %s",
                this);

        if (b) {
            System.gc();
        }

        return b;
    }

    @Override
    public synchronized String toString() {
        StringBuilder b = new StringBuilder(size() * 200);

        b.append(String.format("Pool (size:%s, capacity:%s) - Scenarios:\n", size(), getCapacity()));

        // Copying the scenario set to prevent concurrent modification exception
        Set<IScenario> scens = new HashSet<IScenario>(this.mScenarios);
        for (IScenario s : scens) {
            b.append(String.format("[%s]:%s\n", s.hashCode(), s));
        }

        return b.toString();
    }

    /**
     * Remove all scenarios
     */
    public void clear() {
        mScenarios.clear();
    }
}