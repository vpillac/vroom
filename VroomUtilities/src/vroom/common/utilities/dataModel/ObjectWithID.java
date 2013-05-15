package vroom.common.utilities.dataModel;

/**
 * The Class ObjectWithID is a utility base type for objects that have a id.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #created 15-Feb-2010 11:29:50 a.m.
 */
public abstract class ObjectWithID implements IObjectWithID {

    /** The m id. */
    private final int mID;

    /**
     * Creates a new object with the given <code>id</code>.
     * 
     * @param id
     *            the id
     */
    public ObjectWithID(int id) {
        mID = id;
    }

    /* (non-Javadoc)
     * @see edu.uniandes.copa.utils.IObjectWithID#getID()
     */
    @Override
    public int getID() {
        return mID;
    }

}// end ObjectWithID