package vroom.common.utilities.dataModel;

/**
 * <code>ObjectWithNameAndId</code> is a base type for all object that have for a name and an id.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 */
public abstract class ObjectWithNameAndId extends ObjectWithID implements IObjectWithName {

    /** The name of this object. */
    private String mName;

    /**
     * Creates an object with a default name and the given id.
     * 
     * @param id
     *            the id for this object
     */
    public ObjectWithNameAndId(int id) {
        this(null, id);
    }

    /**
     * Creates a new object with the given name and id.
     * 
     * @param name
     *            the name of this object
     * @param id
     *            the id for this object
     */
    public ObjectWithNameAndId(String name, int id) {
        super(id);
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

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String
                .format("%s - %s (id:%s)", this.getClass().getSimpleName(), getName(), getID());
    }

}
