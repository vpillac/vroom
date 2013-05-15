package vroom.common.heuristics.alns;

import vroom.common.utilities.IDisposable;
import vroom.common.utilities.optimization.IInstance;
import vroom.common.utilities.optimization.IParameters;
import vroom.common.utilities.optimization.ISolution;

public interface IALNSComponent<S extends ISolution> extends IDisposable, Cloneable {

    /**
     * Initialize this destroy procedure for a new instance.
     * <p>
     * This method is called once at the beginning of the
     * {@link AdaptiveLargeNeighborhoodSearch#localSearch(IInstance, ISolution, IParameters) ALNS}
     * </p>
     * 
     * @param instance
     *            the instance that will be considered until the next call to this method
     */
    public void initialize(IInstance instance);

    /**
     * Returns a short string describing this component
     * 
     * @return the name of this component
     */
    public String getName();
}