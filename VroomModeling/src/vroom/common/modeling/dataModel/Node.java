package vroom.common.modeling.dataModel;

import vroom.common.modeling.dataModel.attributes.ILocation;
import vroom.common.modeling.dataModel.attributes.INodeAttribute;
import vroom.common.modeling.dataModel.attributes.NodeAttributeKey;
import vroom.common.utilities.dataModel.IObjectWithID;
import vroom.common.utilities.dataModel.IObjectWithName;

/**
 * The class <code>Node</code> represents a physical location in a VRP problem. It is associated with a
 * {@link ILocation} and as both a name and a id.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #created 15-Feb-2010 11:29:51 a.m.
 */
public class Node extends ObjectWithAttributes<INodeAttribute, NodeAttributeKey<?>> implements IObjectWithName,
        IObjectWithID {

    /** The location of this node. */
    private final ILocation mLocation;

    /**
     * Creates a new node with the given <code>id</code> and <code>location</code>.
     * 
     * @param id
     *            the id of this node
     * @param location
     *            the location of this node
     * @see Node#Node(int, String, ILocation)
     */
    public Node(int id, ILocation location) {
        this(id, null, location);
    }

    /**
     * Creates a new node with the given <code>id</code>, <code>name</code> and <code>location</code>.
     * 
     * @param id
     *            the id of this node
     * @param name
     *            the name of this node
     * @param location
     *            the location of this node
     */
    public Node(int id, String name, ILocation location) {
        super();

        mID = id;
        setName(name);

        mLocation = location;
    }

    /**
     * Gets the location.
     * 
     * @return the location of this node
     */
    public ILocation getLocation() {
        return mLocation;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("%s-%s", getID(), getLocation());
    }

    /* IObjectWithName interface implementation */
    /** The name of this object. */
    private String mName;

    /*
     * (non-Javadoc)
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

    /* */

    /* IObjectWithId interface implementation */
    /** The m id. */
    private final int mID;

    /*
     * (non-Javadoc)
     * @see edu.uniandes.copa.utils.IObjectWithID#getID()
     */
    @Override
    public int getID() {
        return mID;
    }

    /* */

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + mID;
        return result;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Node other = (Node) obj;
        if (mID != other.mID) {
            return false;
        }
        return true;
    }

}// end VRPNode