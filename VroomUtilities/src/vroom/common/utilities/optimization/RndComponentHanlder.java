/**
 *
 */
package vroom.common.utilities.optimization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import umontreal.iro.lecuyer.rng.RandomStream;
import vroom.common.utilities.Utilities;

/**
 * <code>RndComponentHanlder</code> is an implementation of {@link IComponentHandler} that selects components randomly
 * <p>
 * Creation date: Aug 31, 2011 - 4:09:26 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class RndComponentHanlder<M> implements IComponentHandler<M> {

    /** A list containing all the registered components */
    private final List<M>      mComponents;
    /** A list containing the components that were not selected so far */
    private final List<M>      mUnexploredComponents;
    /** A random stream used for component selection */
    private final RandomStream mRndStream;
    /** <code>true</code> if components should be discarded once they were selected, <code>false</code> otherwise */
    private final boolean      mDiscardAfterSelection;

    /**
     * Creates a new <code>RndComponentHanlder</code>
     * 
     * @param rndStream
     *            the random stream that will be used to select components
     * @param components
     *            the components that will be handled
     * @param discardAfterSelection
     *            <code>true</code> if a component should be discarded after being selected, in which case it could not
     *            be selected until the next call to {@link #reset()}, <code>false</code> if components can always be
     *            selected
     */
    public RndComponentHanlder(RandomStream rndStream, Collection<M> components,
            boolean discardAfterSelection) {
        this.mRndStream = rndStream;
        mDiscardAfterSelection = discardAfterSelection;

        mComponents = new ArrayList<M>(components);
        if (mDiscardAfterSelection)
            mUnexploredComponents = new ArrayList<M>(components);
        else
            mUnexploredComponents = mComponents;
    }

    @Override
    public List<M> getComponents() {
        return new ArrayList<M>(mComponents);
    }

    @Override
    public M nextComponent() {
        if (isCompletelyExplored())
            return null;

        int idx = this.mRndStream.nextInt(0, mUnexploredComponents.size() - 1);

        if (mDiscardAfterSelection)
            return mUnexploredComponents.remove(idx);
        else
            return mUnexploredComponents.get(idx);
    }

    @Override
    public boolean isCompletelyExplored() {
        return mUnexploredComponents.isEmpty();
    }

    @Override
    public boolean updateStats(M currentComponent, double improvement, double time, int iteration,
            vroom.common.utilities.optimization.IComponentHandler.Outcome outcome) {
        return false;
    }

    @Override
    public double getWeight(M component) {
        if (mDiscardAfterSelection) {
            if (mUnexploredComponents.contains(component))
                return 1d / mUnexploredComponents.size();
            else
                return 0;
        } else
            return 1d / mComponents.size();
    };

    @Override
    public void initialize(IInstance instance) {
        reset();
    }

    @Override
    public void reset() {
        if (mDiscardAfterSelection) {
            mUnexploredComponents.clear();
            mUnexploredComponents.addAll(mComponents);
        }
    }

    @Override
    public void dispose() {
        mComponents.clear();
        mUnexploredComponents.clear();
    }

    @Override
    public String toString() {
        if (mDiscardAfterSelection)
            return String.format("comp:%s unexpl:%s", Utilities.toShortString(mComponents),
                    Utilities.toShortString(mUnexploredComponents));
        else
            return String.format("comp:%s", Utilities.toShortString(mComponents));

    }

}
