/**
 * 
 */
package vroom.trsp.datamodel;

import vroom.common.modeling.dataModel.Depot;
import vroom.common.modeling.dataModel.Vehicle;
import vroom.common.modeling.dataModel.attributes.AttributeWithIdSet;
import vroom.common.modeling.dataModel.attributes.AttributeWithIdSet.AttributeSetType;
import vroom.common.utilities.Utilities;

/**
 * The Class <code>Technician</code> is an extension of {@link Vehicle} used to provide shortcuts to attributes
 * associated with technicians in the TRSP.
 * <p>
 * Creation date: Feb 11, 2011 - 12:56:33 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class Technician extends Vehicle {

    /** the set of skills of the technician */
    private final AttributeWithIdSet mSkills;

    /** the set of tools initially available to the technician */
    private final AttributeWithIdSet mTools;

    /** the home of this technician */
    private final Depot              mHome;

    /**
     * Instantiates a new technician.
     * 
     * @param id
     *            the id
     * @param description
     *            the description
     * @param fixedCost
     *            the fixed cost
     * @param variableCost
     *            the variable cost
     * @param speed
     *            the traveling speed of this technician
     * @param skills
     *            the skills
     * @param tools
     *            the tools available to this technician
     * @param spare
     *            the number of spare parts of each type available to this technician
     * @param home
     *            the technician home
     */
    public Technician(int id, String description, double fixedCost, double variableCost, double speed, int[] skills,
            int[] tools, int[] spare, Depot home) {
        super(id, description, fixedCost, variableCost, speed, Utilities.copyToDoubleArray(spare));

        mSkills = new AttributeWithIdSet(AttributeSetType.Offer, skills);
        mTools = new AttributeWithIdSet(AttributeSetType.Offer, tools);
        mHome = home;
    }

    /**
     * Getter for the skill set.
     * 
     * @return the skill set associated with this technician
     */
    public AttributeWithIdSet getSkillSet() {
        return mSkills;
    }

    /**
     * Getter for the tool set.
     * 
     * @return the tool set associated with this technician
     */
    public AttributeWithIdSet getToolSet() {
        return mTools;
    }

    /**
     * Getter for the number of available spare parts of a given type.
     * 
     * @param type
     *            the type of the spare part
     * @return the number of spare parts of the given type that the technician has available
     */
    public int getAvailableSpareParts(int type) {
        return (int) getCapacity(type);
    }

    /**
     * Get all the spare parts availability in form of an array
     * 
     * @return a copy of the spare part array
     */
    public int[] getSpareParts() {
        int[] s = new int[getCompartmentCount()];
        for (int i = 0; i < s.length; i++)
            s[i] = (int) getCapacity(i);
        return s;
    }

    /**
     * Gets the home of this technician.
     * 
     * @return the home
     */
    public Depot getHome() {
        return mHome;
    }

    /*
     * (non-Javadoc)
     * @see vroom.common.modeling.dataModel.Vehicle#toString()
     */
    @Override
    public String toString() {
        return String.format("%s h:%s s:%s t:%s p:%s", getID(), getHome().getLocation(), getSkillSet().toString(),
                getToolSet().toString(), Utilities.toShortString(getSpareParts()));
    }

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

        Technician other = (Technician) obj;

        if (this.getSpeed() != other.getSpeed())
            return false;
        if (!this.mSkills.equals(other.mSkills))
            return false;
        if (!this.mTools.equals(other.mTools))
            return false;
        if (!this.mHome.equals(other.mHome))
            return false;

        return true;
    }

}
