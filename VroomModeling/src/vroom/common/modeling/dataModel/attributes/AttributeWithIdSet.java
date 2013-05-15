package vroom.common.modeling.dataModel.attributes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import vroom.common.utilities.IntegerSet;
import vroom.common.utilities.Utilities;

/**
 * The Class <code>AttributeWithIdSet</code> represents a set of attributes that can be characterized with a unique ID
 * <p>
 * Creation date: Feb 11, 2011 - 11:01:23 AM.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class AttributeWithIdSet implements IVehicleAttribute, IRequestAttribute,
        ICompatibilityAttribute, Iterable<Integer> {

    public static enum AttributeSetType {
        Requirement, Offer
    };

    private final IntegerSet mAttributes;

    /** An hash code */
    private final int        mHashCode;

    private final AttributeSetType       mType;

    /**
     * Creates a new <code>AttributeWithIdSet</code>.
     * 
     * @param attributes
     *            an array containing the id of the attributes to be included in this set. Negative id will be ignored
     */
    public AttributeWithIdSet(AttributeSetType type, int... attributes) {
        mType = type;
        int max = Integer.MIN_VALUE;

        for (int s : attributes) {
            if (s > max)
                max = s;
        }

        mAttributes = new IntegerSet(max >= 0 ? max + 1 : 0);
        int hash = 0;

        for (int s : attributes) {
            if (s >= 0) {
                mAttributes.add(s);
                hash += Math.pow(2, s % 30);
            }
        }

        mHashCode = hash;
    }

    /**
     * Gets the type.
     * 
     * @return the type
     */
    public AttributeSetType getType() {
        return mType;
    }

    /**
     * Gets the number of attributes in this set.
     * 
     * @return the attribute count
     */
    public int size() {
        return mAttributes.size();
    }

    /**
     * Checks if the given attribute is a member of this set.
     * 
     * @param attribute
     *            the attribute to be checked
     * @return <code>true</code>, if this set contains the given <code>attribute</code>, <code>false</code> otherwise.
     *         Negative values will be ignored.
     */
    public boolean hasAttribute(int attribute) {
        return mAttributes.contains(attribute);
    }

    /**
     * Checks if this attribute set is included in the given set.
     * 
     * @param set
     *            the set in which inclusion will be tested
     * @return <code>true</code>, if <code>this</code> instance is included in the given <code>set</code>
     * @see #includes(AttributeWithIdSet)
     */
    public boolean isIncluded(AttributeWithIdSet set) {
        if (this.size() > set.size())
            return false;

        for (Integer i : mAttributes) {
            if (!set.hasAttribute(i))
                return false;
        }

        return true;
    }

    /**
     * Checks if this attribute set includes the given set.
     * 
     * @param set
     *            the set which inclusion will be tested.
     * @return <code>true</code>, if <code>this</code> instance includes the given <code>set</code>
     * @see #isIncluded(AttributeWithIdSet)
     */
    public boolean includes(AttributeWithIdSet set) {
        return set == null || set.isIncluded(this);
    }

    /**
     * Convert this set to a list containing the ID of each attribute.
     * 
     * @return a list containing the ID of each attribute.
     */
    public List<Integer> toList() {
        return new ArrayList<Integer>(mAttributes);
    }

    @Override
    public int hashCode() {
        return mHashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AttributeWithIdSet))
            return false;

        AttributeWithIdSet set = (AttributeWithIdSet) obj;
        if (this.size() != set.size() || size() != set.size())
            return false;

        for (Integer i : mAttributes) {
            if (!set.hasAttribute(i))
                return false;
        }

        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see vroom.common.utilities.dataModel.IObjectWithName#getName()
     */
    @Override
    public String getName() {
        return "Attribute Set";
    }

    /*
     * (non-Javadoc)
     *
     * @see vroom.common.modeling.dataModel.attributes.ICompatibilityAttribute#
     * isCompatibleWith
     * (vroom.common.modeling.dataModel.attributes.ICompatibilityAttribute)
     */
    @Override
    public boolean isCompatibleWith(ICompatibilityAttribute otherAttribute) {
        if (otherAttribute instanceof AttributeWithIdSet) {
            AttributeWithIdSet set = (AttributeWithIdSet) otherAttribute;

            if (set.getType() == getType() || (set.size() == 0 && this.size() == 0)) {
                return true;
            } else {
                switch (getType()) {
                case Requirement: // This is a required set of attributes
                    return isIncluded(set);
                case Offer: // This is a set of available attributes
                    return includes(set);
                default:
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return Utilities.toShortString(toList());
    }

    @Override
    public Iterator<Integer> iterator() {
        return mAttributes.iterator();
    }
}
