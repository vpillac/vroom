package vroom.common.modeling.dataModel.attributes;

/**
 * The Class ClientLocation represent the location of a client.
 * <p>
 * It extends {@link PointLocation} by adding an optional description of the location, that could for instance be a name
 * and an address
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #created 15-Feb-2010 11:29:49 a.m.
 */
public class ClientLocation extends PointLocation {

    /** a description for this client. */
    private String mDescription;

    /**
     * Creates a new client location with a default description.
     * 
     * @param x
     *            the x coordinate of this location
     * @param y
     *            the y coordinate of this location
     * @see PointLocation#PointLocation(double, double)
     * @see ClientLocation#ClientLocation(double, double, String)
     */
    public ClientLocation(double x, double y) {
        this(x, y, "Unknown");

    }

    /**
     * Creates a new client location with the given <code>description</code>.
     * 
     * @param x
     *            the x coordinate of this location
     * @param y
     *            the y coordinate of this location
     * @param description
     *            a description for this client location
     */
    public ClientLocation(double x, double y, String description) {
        super(x, y);
        mDescription = description;
    }

    /**
     * Gets the description.
     * 
     * @return the description
     */
    public String getDescription() {
        return mDescription;
    }

    /* (non-Javadoc)
     * @see vroom.common.modeling.dataModel.attributes.PointLocation#toString()
     */
    @Override
    public String toString() {
        return String.format("(%s,%s)-[%s]", getX(), getY(), getDescription());
    }

    /* (non-Javadoc)
     * @see vroom.common.modeling.dataModel.attributes.PointLocation#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof ClientLocation && super.equals(obj);
    }

}// end ClientLocation