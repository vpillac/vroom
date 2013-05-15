package vroom.common.utilities.dataModel;

/**
 * The Class ObjectWithName is a utility base type for objects that have a name.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #created 15-Feb-2010 11:29:50 a.m.
 */
public abstract class ObjectWithName implements IObjectWithName {

    /** The name of this object. */
    private String mName;

    /**
     * Creates an object with a default name.
     */
    public ObjectWithName() {
        this(null);
    }

    /**
     * Creates a new object with the given name.
     * 
     * @param name
     *            the name of this object
     */
    public ObjectWithName(String name) {
        mName = name != null ? name : "Unknown";
    }

    /* (non-Javadoc)
     * @see edu.uniandes.copa.utils.IObjectWithName#getName()
     */
    @Override
    public String getName() {
        return mName;
    }

    /**
     * Setter for this object name.
     * 
     * @param name
     *            the name to be set
     */
    public void setName(String name) {
        mName = name;

    }

}// end ObjectWithName