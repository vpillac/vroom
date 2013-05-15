/**
 * 
 */
package vroom.common.modeling.dataModel.attributes;

import vroom.common.modeling.dataModel.Request;
import vroom.common.modeling.dataModel.Vehicle;

/**
 * The class <code>Skill</code> represents a skill that can be associated to either a {@link Request} or a
 * {@link Vehicle}. This implementation only considers the skill id to check for compatibility (the skill proficiency is
 * ignored).
 * <p>
 * Creation date: Feb 8, 2011 - 2:30:47 PM.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class Skill implements IRequestAttribute, IVehicleAttribute, ICompatibilityAttribute {

    /** the name of this skill *. */
    private final String mSkillName;

    /**
     * Getter for the name of this skill.
     * 
     * @return the name of this skill
     */
    public String getSkillName() {
        return this.mSkillName;
    }

    /** a unique id for this skill *. */
    private final int mSkillId;

    /**
     * Getter for a unique id for this skill.
     * 
     * @return the value of this skill unique ID
     */
    public int getId() {
        return this.mSkillId;
    }

    /** the skill proficiency *. */
    private final int mProficiency;

    /**
     * Getter for the skill proficiency.
     * 
     * @return the value of the skill proficiency
     */
    public int getProficiency() {
        return this.mProficiency;
    }

    /**
     * Instantiates a new skill attribute.
     * 
     * @param skillName
     *            the skill name
     */
    public Skill(String skillName) {
        this(skillName, 0);
    }

    /**
     * Instantiates a new skill attribute, the name {@link String#hashCode() hashCode} will be used as unique ID.
     * 
     * @param skillName
     *            the skill name
     * @param proficiency
     *            the skill proficiency
     */
    public Skill(String skillName, int proficiency) {
        this(skillName, skillName.hashCode(), proficiency);
    }

    /**
     * Instantiates a new skill attribute, the skill id will be used as name.
     * 
     * @param skillId
     *            the skill id
     * @param proficiency
     *            the proficiency
     */
    public Skill(int skillId, int proficiency) {
        this("" + skillId, skillId, proficiency);
    }

    /**
     * Instantiates a new skill attribute.
     * 
     * @param skillName
     *            the skill name
     * @param skillId
     *            the skill id
     * @param proficiency
     *            the proficiency
     */
    public Skill(String skillName, int skillId, int proficiency) {
        mSkillName = skillName;
        mSkillId = skillId;
        mProficiency = proficiency;
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.utilities.dataModel.IObjectWithName#getName()
     */
    @Override
    public String getName() {
        return "Skill";
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
        return otherAttribute instanceof Skill
                && getId() == ((Skill) otherAttribute).getId();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return mSkillName + "[" + mProficiency + "]";
    }

}
