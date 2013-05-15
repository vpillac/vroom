/**
 *
 */
package vroom.trsp.datamodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import vroom.common.modelling.dataModel.Depot;
import vroom.common.modelling.dataModel.attributes.ITimeWindow;
import vroom.common.utilities.ExtendedReentrantLock;
import vroom.common.utilities.Utilities;
import vroom.common.utilities.optimization.IInstance;
import vroom.optimization.online.jmsa.IActualRequest;
import vroom.trsp.sim.TRSPSimulator;

/**
 * <code>TRSPInstance</code> is a simplified representation of an instance, independent from the VroomModelling Library.
 * <p>
 * Creation date: Feb 23, 2011 - 11:19:53 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TRSPInstance implements IInstance, vroom.optimization.online.jmsa.IInstance {

    /** The id of the main depot */
    public static final int MAIN_DEPOT = 0;

    /** the name of this instance **/
    private final String    mName;

    /**
     * Getter for the name of this instance
     * 
     * @return the name of this instance
     */
    @Override
    public String getName() {
        return this.mName;
    }

    /**
     * Returns the group of instance (for TRSP instances)
     * 
     * @return the group of instance (for TRSP instances)
     */
    public String getGroup() {
        return getName().substring(0, (getName().contains("RC") ? 3 : 2));
    }

    /** the technician fleet *. */
    private final TechnicianFleet mFleet;

    /**
     * Getter for the technician fleet.
     * 
     * @return the value of fleet
     */
    public TechnicianFleet getFleet() {
        return this.mFleet;
    }

    /**
     * Returns the technician with the given id
     * 
     * @param techId
     *            the technician id
     * @return the technician with the given id
     */
    public Technician getTechnician(int techId) {
        return getFleet().getVehicle(techId);
    }

    /** the depots array in this instance (including technician homes) *. */
    private final Depot[] mDepots;

    /**
     * the number of depots in this instance, stored for performance
     */
    private final int     mDepotCount;
    /**
     * the number to be added to a depot id to obtain its duplicate id
     */
    private final int     mDepotsOffset;

    /**
     * Getter for the list of the depots in this instance (including technician homes).
     * 
     * @return the list of depots in this instance
     */
    public List<Depot> getDepots() {
        return Arrays.asList(Arrays.copyOf(mDepots, mDepots.length));
    }

    /**
     * Gets the depot count.
     * 
     * @return the depot count
     */
    public int getDepotCount() {
        return this.mDepotCount;
    }

    /**
     * Get a depot form its id.
     * <p>
     * Please note that a depot and its duplicate share a reference to the same {@link Depot} object
     * </p>
     * 
     * @param depotId
     *            the id of the depot
     * @return the depot with id <code>depotId</code>, or <code>null</code> if <code>depotId</code> is not a valid depot
     *         id
     */
    public Depot getDepot(int depotId) {
        // if (!isDepot(depotId))
        // return null;
        return mDepots[getOriginalId(depotId)];
    }

    /**
     * Get the depot duplicate id
     * 
     * @param depotId
     *            the original depot
     * @return the id used for the duplicate
     */
    public int getDepotDuplicate(int depotId) {
        if (!isDepot(depotId))
            throw new IllegalArgumentException("The depot id does not seem to be valid " + depotId);
        return depotId + mDepotsOffset;
    }

    /**
     * Get the original id of a node
     * 
     * @param nodeId
     *            the id of the considered node
     * @return the id of the original node: <code>nodeId-depotOffset</code> if <code>nodeId</code> is the ID of a depot
     *         duplicate, and <code>nodeId</code> otherwise
     */
    protected int getOriginalId(int nodeId) {
        if (nodeId >= mDepotsOffset)
            return nodeId - mDepotsOffset;
        else
            return nodeId;
    }

    /**
     * Get the main depot
     * 
     * @return the main depot in this instance
     */
    public Depot getMainDepot() {
        return getDepot(0);
    }

    /**
     * Check if the given id is the id of a depot.
     * 
     * @param id
     *            the id to be checked
     * @return <code>true</code> if <code>id</code> corresponds to a depot
     */
    public boolean isDepot(int id) {
        // return mDepots.containsK:ey(id);
        return id >= 0 && id < mDepotCount || id >= mDepotsOffset;
    }

    /**
     * Check if the given id is the id of a/the main depot
     * 
     * @param id
     *            the id to be checked
     * @return <code>true</code> if <code>id</code> corresponds to a depot
     */
    public boolean isMainDepot(int id) {
        return id == 0;
    }

    /** A list of the requests in this instance. */
    private final TRSPRequest[]     mRequests;
    /** A view of the list of requests in this instance */
    private final List<TRSPRequest> mRequestsView;

    /** A list of the requests that have already been released */
    private List<TRSPRequest>       mReleasedRequests;

    /** A list of the unserved requests that have already been released */
    private List<TRSPRequest>       mUnservedReleasedRequests;

    /** The number of requests in this instance, stored for performance */
    private final int               mRequestCount;

    /**
     * Returns all the requests in this instance (whether they are released or not).
     * <p>
     * Note that the returned list is an unmodifiable view of the internal request list.
     * </p>
     * 
     * @return a list containing all the requests in this instance
     */
    public List<TRSPRequest> getRequests() {
        return mRequestsView;
    }

    /**
     * Update the released requests list
     */
    public void updateReleasedRequests() {
        if (mSimulator == null) {
            mReleasedRequests = getRequests();
            mUnservedReleasedRequests = getRequests();
        } else {
            mReleasedRequests = new ArrayList<TRSPRequest>(getRequestCount());
            mUnservedReleasedRequests = new ArrayList<TRSPRequest>(getRequestCount());
            for (TRSPRequest r : mRequests) {
                if (mSimulator.isReleased(r)) {
                    mReleasedRequests.add(r);
                    if (!isServed(r.getID()))
                        mUnservedReleasedRequests.add(r);
                }
            }
            mReleasedRequests = Collections.unmodifiableList(mReleasedRequests);
            mUnservedReleasedRequests = Collections.unmodifiableList(mUnservedReleasedRequests);
        }
    }

    /**
     * Returns all the released requests in this instance (i.e. that have been made known to the system).
     * <p>
     * Note that the returned list is an unmodifiable view of the internal request list, and that method
     * {@link #updateReleasedRequests()} should be called before to ensure that the released request list is up to date.
     * </p>
     * 
     * @return a list containing all the released requests in this instance
     * @see TRSPInstance#updateReleasedRequests()
     */
    public List<TRSPRequest> getReleasedRequests() {
        return mReleasedRequests;
    }

    /**
     * Returns all the unserved released requests in this instance (i.e. that have been made known to the system).
     * <p>
     * Note that the returned list is an unmodifiable view of the internal request list, and that method
     * {@link #updateReleasedRequests()} should be called before to ensure that the released request list is up to date.
     * </p>
     * 
     * @return a list containing all the unserved released requests in this instance
     * @see TRSPInstance#updateReleasedRequests()
     */
    public List<TRSPRequest> getUnservedReleasedRequests() {
        return mUnservedReleasedRequests;
    }

    /**
     * Returns the total number of requests in this instance, ignoring their released state.
     * 
     * @return the request count
     */
    public int getRequestCount() {
        return mRequestCount;
    }

    /**
     * Gets a request from its id
     * 
     * @param id
     *            the id
     * @return the request with the given id
     */
    public TRSPRequest getRequest(int id) {
        // if (!isRequest(id))
        // throw new
        // IllegalArgumentException(String.format("%s is not a valid request id",
        // id));

        return mRequests[id - mDepotCount];
    }

    /**
     * Checks if is request.
     * 
     * @param id
     *            the id
     * @return true, if is request
     */
    public boolean isRequest(int id) {
        // return mRequests.containsKey(id);
        return !isDepot(id);
    }

    /**
     * Size of the instance: total number of nodes including both depots and requests
     * 
     * @return the total number of nodes in this instance
     */
    public int size() {
        return getDepotCount() + getRequestCount();
    }

    /** the cost delegate *. */
    private final TRSPDistanceMatrix mCostDelegate;

    /**
     * Getter for the cost delegate.
     * 
     * @return the cost delegate
     */
    public TRSPDistanceMatrix getCostDelegate() {
        return this.mCostDelegate;
    }

    /** the delegate class that will generate solutions hash codes **/
    private ITRSPSolutionHasher mSolutionHasher;

    /**
     * Getter for the delegate class that will generate solutions hash codes
     * 
     * @return the solution hasher associated with this instance
     */
    public ITRSPSolutionHasher getSolutionHasher() {
        return this.mSolutionHasher;
    }

    /**
     * Set for the delegate class that will generate solutions hash codes
     * 
     * @param solutionHasher
     *            the solution hasher associated with this instance
     */
    public void setSolutionHasher(ITRSPSolutionHasher solutionHasher) {
        mSolutionHasher = solutionHasher;
    }

    /** the number of skills *. */
    private final int mSkillCount;

    /**
     * Getter for the number of skills.
     * 
     * @return the number of skills
     */
    public int getSkillCount() {
        return this.mSkillCount;
    }

    /** the number of tools *. */
    private final int mToolCount;

    /**
     * Getter for the number of tools.
     * 
     * @return the number of tools
     */
    public int getToolCount() {
        return this.mToolCount;
    }

    /** the number of spare parts *. */
    private final int mSpareCount;

    /**
     * Getter for the number of spare parts.
     * 
     * @return the number of spare parts
     */
    public int getSpareCount() {
        return this.mSpareCount;
    }

    /** the highest id used for requests and depots **/
    private final int mMaxId;

    /**
     * Getter for the highest id used for requests and depots
     * 
     * @return the value of the highest id
     */
    public int getMaxId() {
        return this.mMaxId;
    }

    /**
     * A matrix representing the existing edges in the reduced graph: <code>mTWGraph[i][j]=true</code> iif node
     * <code>j</code> can be visited after node <code>i</code> without violating time window constraints
     */
    private final boolean[][] mTWGraph;

    /**
     * Feasibility of an arc regarding time windows
     * 
     * @param pred
     *            the first node
     * @param succ
     *            the second node
     * @return <code>true</code> iif node <code>succ</code> can be visited after node <code>pred</code> without
     *         violating time window constraints
     */
    public boolean isArcTWFeasible(int pred, int succ) {
        if (pred == ITRSPTour.UNDEFINED || succ == ITRSPTour.UNDEFINED)
            return true;
        return mTWGraph[getOriginalId(pred)][getOriginalId(succ)];
    }

    /**
     * A matrix representing the skill compatibility between technicians and requests for faster compatibility checks
     * <code>mTechSkillCompatibility[k][i]=true</code> if technician k has the required skills to serve request i
     */
    private final boolean[][]             mTechSkillCompatibility;

    /**
     * A matrix representing the tools compatibility between technicians and requests for faster compatibility checks
     * <code>mTechSkillCompatibility[k][i]=true</code> if technician k initially has the required tools to serve request
     * i
     */
    private final boolean[][]             mTechToolCompatibility;

    /**
     * A matrix representing the tools compatibility between technicians and requests for faster compatibility checks
     * <code>mTechSkillCompatibility[k][i]=true</code> if technician k initially has the required spare parts to serve
     * request i
     */
    private final boolean[][]             mTechSpareCompatibility;

    /**
     * A matrix representing the compatibilities between technicians and requests.
     * <code>mTechReqCompatibility[k][i]=true</code> if k has the required skills to serve i within its time window. In
     * other words, <code>mTechReqCompatibility[k][i]=true</code> iif there exist a feasible tour visiting i.
     */
    private final boolean[][]             mTechReqCompatibility;

    /**
     * A mapping between requests and technicians that have the required skills and can service the request with its
     * time window
     */
    private final ArrayList<Set<Integer>> mCompatibleTech;

    /**
     * Returns <code>true</code> iif there exist a feasible tour associated with technician <code>req</code> servicing
     * request <code>req</code> (considering skills, tools, spare parts, and time windows)
     * 
     * @param tech
     *            the id of the considered technician
     * @param req
     *            the id of the considered request
     * @return <code>true</code> iif there exist a feasible tour associated with technician <code>req</code> servicing
     *         request <code>req</code>
     */
    public boolean isCompatible(int tech, int req) {
        return mTechReqCompatibility[tech][req];
    }

    /**
     * @param tech
     *            the id of the considered technician
     * @param req
     *            the id of the considered request
     * @return <code>true</code> iif technician <code>tech</code> has the required skills to serve request
     *         <code>req</code>
     */
    public boolean hasRequiredSkills(int tech, int req) {
        return mTechSkillCompatibility[tech][req];
    }

    /**
     * @param tech
     *            the id of the considered technician
     * @param req
     *            the id of the considered request
     * @return <code>true</code> iif technician <code>tech</code> initially has the required tools to serve request
     *         <code>req</code>
     */
    public boolean hasRequiredTools(int tech, int req) {
        return mTechToolCompatibility[tech][req];
    }

    /**
     * @param tech
     *            the id of the considered technician
     * @param req
     *            the id of the considered request
     * @return <code>true</code> iif technician <code>tech</code> initially has the required spare parts to serve
     *         request <code>req</code>
     */
    public boolean hasRequiredSpareParts(int tech, int req) {
        return mTechSpareCompatibility[tech][req];
    }

    /**
     * Set of compatible technicians.
     * <p>
     * A technician is said to be compatible if it has the required skills
     * </p>
     * 
     * @param req
     *            the considered request
     * @return a set containing the technicians that can potentially service request <code>req</code>
     */
    public Set<Integer> getCompatibleTechnicians(int req) {
        return mCompatibleTech.get(req);
    }

    /** the allow main depot trip flag **/
    private boolean mMainDepotTripAllowed = true;

    /**
     * Getter for the allow main depot trip flag (default value is <code>true</code>)
     * 
     * @return <code>true</code> if trips to the main depot are allowed
     */
    public boolean isMainDepotTripAllowed() {
        return this.mMainDepotTripAllowed;
    }

    /**
     * Setter for the allow main depot trip flag (default value is <code>true</code>)
     * 
     * @param allowTrip
     *            <code>true</code> if trips to the main depot are allowed
     */
    public void setMainDepotTripAllowed(boolean allowTrip) {
        this.mMainDepotTripAllowed = allowTrip;
    }

    /** the allow unserved request flag **/
    private boolean mUnservedReqAllowed = false;

    /**
     * Getter for the allow unserved request flag (default value is <code>false</code>)
     * 
     * @return <code>true</code> if some requests can be left unserved, <code>false</code> otherwise
     */
    public boolean isUnservedReqAllowed() {
        return this.mUnservedReqAllowed;
    }

    /**
     * Setter for the allow unserved request flag (default value is <code>false</code>)
     * 
     * @param allowUnserved
     *            <code>true</code> if some requests can be left unserved, <code>false</code> otherwise
     */
    public void setUnservedReqAllowed(boolean allowUnserved) {
        this.mUnservedReqAllowed = allowUnserved;
    }

    /** the simulator being used with this instance **/
    private TRSPSimulator mSimulator;

    /**
     * Setter for the simulator being used with this instance
     * 
     * @param simulator
     *            the simulator being used with this instance
     * @throws IllegalStateException
     *             if the simulator was set previously
     */
    public void setSimulator(TRSPSimulator simulator) {
        // if (this.mSimulator != null)
        // throw new IllegalStateException("The simulator was set previously");
        this.mSimulator = simulator;
    }

    /**
     * Getter for the simulator being used with this instance
     * 
     * @return the simulator being used with this instance
     */
    public TRSPSimulator getSimulator() {
        return this.mSimulator;
    }

    /**
     * Returns {@code true} iif the request has already been served
     * 
     * @param reqId
     * @return {@code true} iif the request has already been served
     * @ßee {@link TRSPSimulator#isServedOrRejected(int)}
     */
    public boolean isServed(int reqId) {
        return getSimulator() != null && getSimulator().isServedOrRejected(reqId);
    }

    /** {@code true} if this is a dynamic instance */
    private boolean mDynamic;

    /**
     * Returns {@code true} if this is a dynamic instance
     * 
     * @return {@code true} if this is a dynamic instance
     */
    public boolean isDynamic() {
        return mDynamic;
    }

    /**
     * Set the dynamic flag
     * 
     * @param dynamic
     *            {@code true} if this is a dynamic instance
     */
    public void setDynamic(boolean dynamic) {
        mDynamic = dynamic;
    }

    /** The state (started/stoped) of each technician */
    private final boolean[] mTechState;

    /**
     * Creates a new <code>TRSPInstance</code>.
     * 
     * @param name
     *            the name of this instance
     * @param technicians
     *            the fleet of technician
     * @param skillCount
     *            the total number of skills
     * @param toolCount
     *            the total number of tool types
     * @param spareCount
     *            the total number of spare part types
     * @param depots
     *            a list of the depot, including possible technician homes. The main depot is assumed to have id 0, all
     *            other depots have ids 1 to <code>|depots|-1</code>.
     * @param requests
     *            a list of the requests, with ids ranging from <code>|depots|</code> to
     *            <code>|depots|+|requests|-1</code>
     */
    public TRSPInstance(String name, Collection<Technician> technicians, int skillCount, int toolCount, int spareCount,
            List<Depot> depots, List<TRSPRequest> requests) {
        this(name, TechnicianFleet.newTechnicianFleet(technicians), skillCount, toolCount, spareCount, depots, requests);
    }

    /**
     * Creates a new <code>TRSPInstance</code>.
     * 
     * @param name
     *            the name of this instance
     * @param technicians
     *            the fleet of technician
     * @param skillCount
     *            the total number of skills
     * @param toolCount
     *            the total number of tool types
     * @param spareCount
     *            the total number of spare part types
     * @param depots
     *            a list of the depot, including possible technician homes. The main depot is assumed to have id 0, all
     *            other depots have ids 1 to <code>|depots|-1</code>.
     * @param requests
     *            a list of the requests, with ids ranging from <code>|depots|</code> to
     *            <code>|depots|+|requests|-1</code>
     */
    public TRSPInstance(String name, TechnicianFleet technicians, int skillCount, int toolCount, int spareCount,
            List<Depot> depots, List<TRSPRequest> requests) {
        mDynamic = false;
        mSolutionHasher = new ITRSPSolutionHasher() {

            @Override
            public int hash(TRSPSolution solution) {
                return solution != null ? solution.hashCode() : 0;
            }

            @Override
            public int hash(ITRSPTour tour) {
                return tour != null ? tour.hashCode() : 0;
            }
        };

        mDepotCount = depots.size();
        mRequestCount = requests.size();

        // Find the maximum id
        int max = 0;
        for (Depot d : depots) {
            if (d.getID() >= max)
                max = d.getID() + 1;
            if (d.getID() >= mDepotCount)
                throw new IllegalArgumentException("Illegal id for depot " + d);
        }
        int depotMaxId = max;
        int reqMaxId = 0;
        for (TRSPRequest d : requests) {
            if (d.getID() >= max)
                max = d.getID() + 1;
            if (d.getID() >= reqMaxId)
                reqMaxId = d.getID();
        }
        mDepotsOffset = max;
        mMaxId = max + depotMaxId;

        mName = name;
        mFleet = technicians;
        mSkillCount = skillCount;
        mToolCount = toolCount;
        mSpareCount = spareCount;

        mTechState = new boolean[getFleet().size()];

        mTechSkillCompatibility = new boolean[getFleet().size()][mMaxId];
        mTechToolCompatibility = new boolean[getFleet().size()][mMaxId];
        mTechSpareCompatibility = new boolean[getFleet().size()][mMaxId];
        mTechReqCompatibility = new boolean[getFleet().size()][mMaxId];

        // A list of all ids that were used

        mDepots = new Depot[mDepotCount];
        for (Depot d : depots) {
            // Check id
            if (mDepots[d.getID()] != null)
                throw new IllegalArgumentException(String.format("Id %s is already used (%s)", d.getID(),
                        mDepots[d.getID()]));

            mDepots[d.getID()] = d;
        }

        mRequests = new TRSPRequest[mRequestCount];
        for (TRSPRequest r : requests) {
            // Check id
            if (mRequests[r.getID() - mDepotCount] != null)
                throw new IllegalArgumentException(String.format("Id %s is already used (%s)", r.getID(),
                        mRequests[r.getID() - mDepotCount]));
            mRequests[r.getID() - mDepotCount] = r;
        }
        mRequestsView = Collections.unmodifiableList(Arrays.asList(mRequests));
        updateReleasedRequests();

        // mCostDelegate = new TRSPDistanceMatrix(this);
        mCostDelegate = new TRSPDistanceMatrixUnitSpeed(this);

        // Preprocess the instance and create the TW compatible graph
        mTWGraph = new boolean[getMaxId()][getMaxId()];
        mCompatibleTech = new ArrayList<Set<Integer>>(getMaxId());
        for (int i = 0; i < getMaxId(); i++)
            mCompatibleTech.add(null);
        preprocess();
    }

    /**
     * Pre-process the instance for faster operations
     */
    public void preprocess() {
        for (int i = 0; i < mTWGraph.length; i++) {
            // Prune infeasible arcs
            for (int j = 0; j < mTWGraph.length; j++) {
                if (i != j)
                    mTWGraph[i][j] = getTimeWindow(j).isFeasible(
                            getTimeWindow(i).startAsDouble() + getServiceTime(i)
                                    + getCostDelegate().getTravelTime(i, j, getFleet().getVehicle()));
            }

            // Store request - technician compatibilities
            boolean atLeastOneTech = false;
            if (isRequest(i))
                mCompatibleTech.set(i, new HashSet<Integer>());

            for (Technician t : getFleet()) {
                mTechReqCompatibility[t.getID()][MAIN_DEPOT] = true;
                mTechReqCompatibility[t.getID()][t.getHome().getID()] = true;

                if (!isRequest(i)) {
                    // i is not a request, it is therefore compatible with the
                    // technician
                    mTechSkillCompatibility[t.getID()][i] = true;
                    mTechToolCompatibility[t.getID()][i] = true;
                    mTechSpareCompatibility[t.getID()][i] = true;
                    atLeastOneTech = true;
                } else {
                    // i is a request, store compatibilities
                    mTechSkillCompatibility[t.getID()][i] = getRequest(i).getSkillSet().isCompatibleWith(
                            t.getSkillSet());
                    mTechToolCompatibility[t.getID()][i] = getRequest(i).getToolSet().isCompatibleWith(t.getToolSet());
                    mTechSpareCompatibility[t.getID()][i] = Utilities.compare(getRequest(i).getSparePartRequirements(),
                            t.getSpareParts()) <= 0;

                    // Check compatibility
                    if (mTechSkillCompatibility[t.getID()][i]) {
                        double arrivalTime;
                        double returnTime;
                        if (mTechToolCompatibility[t.getID()][i] && mTechSpareCompatibility[t.getID()][i]) {
                            // The technician can service the request directly
                            arrivalTime = calculateArrivalTime(i, t.getHome().getID(), 0, t.getID());
                        } else {
                            // The technician needs to visit first the main
                            // depot
                            arrivalTime = calculateArrivalTime(getMainDepot().getID(), t.getHome().getID(), 0,
                                    t.getID());
                            arrivalTime = calculateArrivalTime(i, getMainDepot().getID(), arrivalTime, t.getID());
                        }
                        returnTime = calculateArrivalTime(t.getHome().getID(), i, arrivalTime, t.getID());
                        mTechReqCompatibility[t.getID()][i] = //
                        getTimeWindow(i).isFeasible(arrivalTime)
                                && getTimeWindow(t.getHome().getID()).isFeasible(returnTime);
                    } else {
                        mTechReqCompatibility[t.getID()][i] = false;
                    }

                    // Check if the request can be serviced by the technician
                    if (mTechReqCompatibility[t.getID()][i]) {
                        atLeastOneTech = true;
                        mCompatibleTech.get(i).add(t.getID());
                    }
                }
            }
            if (!atLeastOneTech)
                throw new IllegalArgumentException("Request " + i + " cannot be served by any technician");
        }
    }

    /**
     * Utility method to get the spare part demand associated with a node
     * 
     * @param node
     *            the considered node
     * @param type
     *            the type of spare part considered
     * @return the spare part requirement of <code>node</code>, or <code>0</code> if <code>node</code> is a depot
     * @see TRSPInstance#getDepot(int)
     * @see TRSPInstance#getRequest(int)
     * @see TRSPRequest#getSparePartRequirement(int)
     */
    public int getSparePartReq(int node, int type) {
        if (isDepot(node))
            return 0;
        else
            return getRequest(node).getSparePartRequirement(type);
    }

    /**
     * Utility method to get the time window associated with a node
     * 
     * @param node
     *            the considered node
     * @return the time window of <code>node</code>
     * @see TRSPInstance#getDepot(int)
     * @see TRSPInstance#getRequest(int)
     * @see TRSPRequest#getTimeWindow()
     * @see Depot#getTimeWindow()
     */
    public ITimeWindow getTimeWindow(int node) {
        if (isDepot(node))
            return getDepot(node).getTimeWindow();
        else
            return getRequest(node).getTimeWindow();
    }

    /**
     * Utility method to get the service time associated with a node.
     * <p>
     * In this implementation depots are assumed to have a service time of <code>0</code>.
     * </p>
     * 
     * @param node
     *            the considered node
     * @return the service time of <code>node</code>
     * @see TRSPInstance#getRequest(int)
     * @see TRSPRequest#getServiceTime()
     */
    public double getServiceTime(int node) {
        if (isDepot(node)) {
            // TODO add service time to depots?
            return 0;
        } else
            return getRequest(node).getServiceTime();
    }

    /**
     * Utility method to evaluate the arrival time at a node depending on the arrival time at its predecessor
     * 
     * @param node
     *            the considered node
     * @param pred
     *            the <code>node</code> predecessor
     * @param arrivalPred
     *            the arrival time at <code>pred</code>
     * @param techId
     *            the id of the considered technician
     * @return the arrival time at <code>node</code>
     */
    public double calculateArrivalTime(int node, int pred, double arrivalPred, int techId) {
        return getTimeWindow(pred).getEarliestStartOfService(arrivalPred) + getServiceTime(pred)
                + getCostDelegate().getTravelTime(pred, node, getTechnician(techId));
    }

    @Override
    public String toString() {
        return String.format("%s (crew:%s req:%s)", getName(), getFleet().size(), getRequestCount());
    }

    // --------------------------------------------------------------------------------------------
    // MSA IInstance implementation
    // --------------------------------------------------------------------------------------------

    @Override
    public boolean addRequest(IActualRequest request) {
        if (!isRequest(request.getID()))
            throw new IllegalArgumentException("Unknown request: " + request);
        if (mReleasedRequests.contains(request))
            throw new IllegalArgumentException("Request is already released: " + request);

        TRSPRequest r = (TRSPRequest) request;

        getSimulator().advanceTime(r.getReleaseDate());
        updateReleasedRequests();
        // mReleasedRequests.add(r);
        // if (!isServed(r.getID()))
        // mUnservedReleasedRequests.add(r);

        return true;
    }

    @Override
    public void setResourceStopped(int resourceId, Object param) {
        mTechState[resourceId] = false;
    }

    @Override
    public void setResourceStarted(int resourceId, Object param) {
        mTechState[resourceId] = true;
    }

    @Override
    public boolean isResourceStarted(int resourceId) {
        return mTechState[resourceId];
    }

    @Override
    public boolean isResourceStopped(int resourceId) {
        return !mTechState[resourceId];
    }

    @Override
    public IActualRequest getNodeVisit(int requestId) {
        return getRequest(requestId);
    }

    @Override
    public List<? extends IActualRequest> getServedRequests() {
        return toRequestList(getSimulator().getServedRequests());
    }

    @Override
    public List<? extends IActualRequest> getServedRequests(int resourceId) {
        ArrayList<TRSPRequest> servedReq = new ArrayList<TRSPRequest>(getSimulator().getCurrentSolution()
                .getTour(resourceId).length());
        for (Integer i : getSimulator().getCurrentSolution().getTour(resourceId)) {
            if (isRequest(i))
                servedReq.add(getRequest(i));
        }
        return servedReq;
    }

    @Override
    public List<? extends IActualRequest> getPendingRequests() {
        return toRequestList(getSimulator().getCurrentSolution().getUnservedRequests());
    }

    @Override
    public boolean assignRequestToResource(IActualRequest request, int resourceId) {
        // TODO check if the implementation of assignRequestToResource is required
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean markRequestAsServed(IActualRequest request, int resourceId) {
        getSimulator().markAsServed(request.getID());
        return true;
    }

    @Override
    public TRSPSolution getCurrentSolution() {
        return getSimulator().getCurrentSolution();
    }

    /**
     * Returns a list containing the requests which ids are contained in {@code  ids}
     * 
     * @param ids
     * @return a list containing the requests which ids are contained in {@code  ids}
     */
    public List<TRSPRequest> toRequestList(Collection<Integer> ids) {
        ArrayList<TRSPRequest> list = new ArrayList<TRSPRequest>(ids.size());
        for (Integer i : ids) {
            if (isRequest(i))
                list.add(getRequest(i));
        }
        return list;
    }

    // --------------------------------------------------------------------------------------------

    // --------------------------------------------------------------------------------------------
    // ILockable interface implementation
    // --------------------------------------------------------------------------------------------
    /** A lock to be used by this instance */
    private final ExtendedReentrantLock mLock = new ExtendedReentrantLock();

    @Override
    public boolean tryLock(long timeout) {
        try {
            return getLockInstance().tryLock(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    @Override
    public void acquireLock() {
        try {
            if (!getLockInstance().tryLock(TRY_LOCK_TIMOUT, TRY_LOCK_TIMOUT_UNIT)) {
                throw new IllegalStateException(String.format(
                        "Unable to acquire lock on this instance of %s (%s) after %s %s, owner: %s", this.getClass()
                                .getSimpleName(), hashCode(), TRY_LOCK_TIMOUT, TRY_LOCK_TIMOUT_UNIT, getLockInstance()
                                .getOwnerName()));
            }
        } catch (InterruptedException e) {
            throw new IllegalStateException(String.format("Unable to acquire lock on this instance of %s (%s)", this
                    .getClass().getSimpleName(), hashCode()), e);
        }
        ;
    }

    @Override
    public void releaseLock() {
        mLock.unlock();
    }

    @Override
    public boolean isLockOwnedByCurrentThread() {
        return mLock.isHeldByCurrentThread();
    }

    @Override
    public ExtendedReentrantLock getLockInstance() {
        return mLock;
    }
    // --------------------------------------------------------------------------------------------

}
