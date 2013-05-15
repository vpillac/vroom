/**
 *
 */
package vroom.trsp.datamodel;

import vroom.common.modeling.dataModel.Node;
import vroom.common.modeling.dataModel.Request;
import vroom.common.modeling.dataModel.attributes.AttributeWithIdSet;
import vroom.common.modeling.dataModel.attributes.IReleaseDate;
import vroom.common.modeling.dataModel.attributes.IRequestAttribute;
import vroom.common.modeling.dataModel.attributes.ITimeWindow;
import vroom.common.modeling.dataModel.attributes.RequestAttributeKey;
import vroom.common.modeling.dataModel.attributes.AttributeWithIdSet.AttributeSetType;
import vroom.common.utilities.IToShortString;
import vroom.common.utilities.Utilities;
import vroom.common.utilities.dataModel.IObjectWithID;

/**
 * The Class <code>TRSPRequest</code> is a definition of a request for the TRSP.
 * <p>
 * Creation date: Feb 11, 2011 - 1:32:16 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 2.0
 */
public class TRSPRequest extends Request implements IObjectWithID, ITRSPNode, IToShortString {

    private final int                mID;

    private final Node               mNode;

    private final AttributeWithIdSet mSkillSet;
    private final AttributeWithIdSet mToolSet;
    private final int[]              mSpareReq;
    private final ITimeWindow        mTW;
    private final double             mServiceTime;
    private double                   mReleaseDate = -1;
    private double                   mArrivalTime;

    /**
     * Creates a new <code>TRSPRequest</code>
     * 
     * @param id
     *            the request id
     * @param node
     *            the associated node
     * @param skills
     *            the set of skills required to service this request
     * @param tools
     *            the set of tools required to service this request
     * @param spare
     *            the demand for each of the spare part types
     * @param tw
     *            the time window for this request
     * @param serviceTime
     *            the service time for this request
     */
    public TRSPRequest(int id, Node node, int[] skills, int[] tools, int[] spare, ITimeWindow tw,
            double serviceTime) {
        super(id, node);

        mID = id;
        mNode = node;

        mSkillSet = new AttributeWithIdSet(AttributeSetType.Requirement, skills);
        mToolSet = new AttributeWithIdSet(AttributeSetType.Requirement, tools);
        mSpareReq = spare;

        mTW = tw;
        mServiceTime = serviceTime;

        mArrivalTime = Double.NaN;
    }

    /**
     * Getter for <code>iD</code>
     * 
     * @return the iD
     */
    @Override
    public int getID() {
        return mID;
    }

    /**
     * Getter for <code>node</code>
     * 
     * @return the node
     */
    @Override
    public Node getNode() {
        return mNode;
    }

    /**
     * Getter for the skill set.
     * 
     * @return the skill set associated with this request
     */
    public AttributeWithIdSet getSkillSet() {
        return mSkillSet;
    }

    /**
     * Getter for the tool set.
     * 
     * @return the tool set associated with this request
     */
    public AttributeWithIdSet getToolSet() {
        return mToolSet;
    }

    /**
     * Gets the time window.
     * 
     * @return the time window
     */
    @Override
    public ITimeWindow getTimeWindow() {
        return mTW;
    }

    /**
     * Getter for the service time
     * 
     * @return the time required to serve this request
     */
    @Override
    public double getServiceTime() {
        return mServiceTime;
    }

    /**
     * Return the {@linkplain RequestAttributeKey#RELEASE_DATE release date} of this request
     * 
     * @return the {@linkplain RequestAttributeKey#RELEASE_DATE release date} of this request
     */
    public double getReleaseDate() {
        return mReleaseDate;
    }

    /**
     * Getter for the number of required spare parts
     * 
     * @param type
     *            the considered type of spare parts
     * @return the number of required spare parts of type <code>type</code>
     * @see #getDemand(int)
     */
    public int getSparePartRequirement(int type) {
        return mSpareReq[type];
    }

    /**
     * Returns an array containing the spare parts requirements
     * <p>
     * Note that the returned array is independent from the internal representation of the requirements
     * </p>
     * 
     * @return an array containing the spare parts requirements
     * @see #getSparePartRequirements()
     */
    public int[] getSparePartRequirements() {
        return mSpareReq;
    }

    @Override
    public IRequestAttribute setAttribute(RequestAttributeKey<?> attributeKey,
            IRequestAttribute value) {
        if (attributeKey == RequestAttributeKey.RELEASE_DATE)
            mReleaseDate = value != null ? ((IReleaseDate) value).doubleValue() : -1;
        return super.setAttribute(attributeKey, value);
    }

    @Override
    public double getArrivalTime() {
        if (Double.isNaN(mArrivalTime))
            throw new IllegalStateException("ArrivalTime time is not defined for request " + mID);
        return mArrivalTime;
    }

    @Override
    public void setArrivalTime(double arrival) {
        mArrivalTime = arrival;
    }

    /*
     * (non-Javadoc)
     * @see vroom.common.modeling.dataModel.Vehicle#toString()
     */
    @Override
    public String toString() {
        return String.format("%s loc:%s tw:%s s:%s sk:%s tl:%s sp:%s", getID(), getNode()
                .getLocation(), getTimeWindow(), getServiceTime(), getSkillSet().toString(),
                getToolSet().toString(), Utilities.toShortString(getSparePartRequirements()));
    }

    @Override
    public String toShortString() {
        return "" + getID();
    }

    @Override
    public String getDescription() {
        return String.format("%s%-3s", getType().toShortString(), getID());
    }

    @Override
    public NodeType getType() {
        return NodeType.REQUEST;
    }

}
