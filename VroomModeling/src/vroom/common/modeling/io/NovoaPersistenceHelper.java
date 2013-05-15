package vroom.common.modeling.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import umontreal.iro.lecuyer.probdist.Distribution;
import umontreal.iro.lecuyer.probdist.NormalDist;
import umontreal.iro.lecuyer.probdist.UniformIntDist;
import vroom.common.modeling.dataModel.Depot;
import vroom.common.modeling.dataModel.DynamicInstance;
import vroom.common.modeling.dataModel.Fleet;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.dataModel.Node;
import vroom.common.modeling.dataModel.Request;
import vroom.common.modeling.dataModel.Vehicle;
import vroom.common.modeling.dataModel.VehicleRoutingProblemDefinition;
import vroom.common.modeling.dataModel.attributes.DeterministicDemand;
import vroom.common.modeling.dataModel.attributes.PointLocation;
import vroom.common.modeling.dataModel.attributes.RequestAttributeKey;
import vroom.common.modeling.dataModel.attributes.StochasticDemand;
import vroom.common.modeling.util.EuclidianDistance;
import vroom.common.utilities.dataModel.IDHelper;

/**
 * <code>NovoaPersistenceHelper</code> is a extension of the
 * {@link FlatFilePersistenceHelper} dedicated to the parsing and writing of
 * instances in the format used by Novoa (2005)
 * <p>
 * Creation date: Apr 13, 2010 - 4:11:27 PM
 * <p>
 * <b>References</b>
 * <p>
 * Novoa, Clara M. (2005), <br/>
 * Static and dynamic approaches for solving the vehicle routing problem with
 * stochastic demands, <br/>
 * Ph.D. dissertation, Lehigh University, United States - Pennsylvania,
 * Publication No. AAT 3188502, <br/>
 * Available online on the <a href=
 * "http://proquest.umi.com/pqdweb?did=994239101&sid=2&Fmt=2&clientId=80016&RQT=309&VName=PQD"
 * >Umi ProQuest website</a>
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los
 *         Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class NovoaPersistenceHelper extends FlatFilePersistenceHelper {

	public static enum DemandDistribution {
		UNIFORM, NORMAL
	};

	/** The relative path to the Novoa instances files */
	public static String RELATIVE_INSTANCE_PATH = "../Instances/vrpsd/novoa";

	private final static IDHelper ID_HELPER = new IDHelper();

	/**
	 * Vehicle capacities for Novoa instances Format:
	 * [n,Set1:Q1,Set1:Q2,Set2:Q1,Set2:Q2]
	 */
	public static final int[][] CAPACITIES = { { 5, 9, 5, 9, 9 },
			{ 8, 14, 9, 13, 10 }, { 20, 91, 58, 60, 45 },
			{ 30, 137, 87, 90, 68 }, { 40, 183, 116, 120, 90 },
			{ 60, 274, 175, 180, 135 }, { 100, 457, 291, 0, 0 },
			{ 150, 686, 436, 0, 0 } };

	/**
	 * Getter for the vehicle capacity
	 * 
	 * @param size
	 *            the instance size
	 * @param set
	 *            the set (1 or 2)
	 * @param cap
	 *            the capacity variant (0 for higher, 1 for lower)
	 * @return the capacity to set for the specified instance
	 */
	public static int getCapacity(int size, int set, int cap) {
		return CAPACITIES[getInstanceId(size)][1 + 2 * (set - 1) + cap];
	}

	/**
	 * Conversion of an actual capacity to a 0-1 number
	 * 
	 * @param size
	 * @param set
	 * @param realCap
	 * @return
	 */
	public static int getCapacityIdx(int size, int set, int realCap) {
		int[] caps = CAPACITIES[getInstanceId(size)];
		for (int id = 1; id < caps.length; id++) {
			if (caps[id] == realCap) {
				return id - 1 - 2 * (set - 1);
			}
		}
		return -1;
	}

	/** Mapping between instance sizes and indexes */
	public static final int[] SIZE_MAPPING = { 5, 8, 20, 30, 40, 60, 100, 150 };

	/**
	 * Instance id lookup
	 * 
	 * @param size
	 *            the instance size
	 * @return an id for this instance, e.g. size 8 instances have id 0
	 * @see #SIZE_MAPPING
	 */
	public static int getInstanceId(int size) {
		return Arrays.binarySearch(SIZE_MAPPING, size);
	}

	/**
	 * Instance name builder
	 * 
	 * @param size
	 *            the instance size
	 * @param set
	 *            the set (1 or 2)
	 * @param rep
	 *            the instance replica (from 1 to 5)
	 * @return the corresponding instance name
	 */
	public static String getInstanceName(int size, int set, int rep) {
		String sset = "";
		switch (set) {
		case 1:
			sset = "i";
			break;
		case 2:
			sset = "newi";
			break;
		default:
			assert false;
		}

		return String.format("%s_%sr%s.dat", sset, size, rep);
	}

	private double mExpectedDemandSum;
	private int mMaxDemand;

	private int mNumRequests;

	private Random mRND = null;

	/** The expected filling factor used to calculate the vehicle capacity **/
	private double mExpectedFillingFactor = 1;

	/**
	 * Getter for the expected filling factor
	 * 
	 * @return The expected filling factor used to calculate the vehicle
	 *         capacity
	 */
	public double getExpectedFillingFactor() {
		return mExpectedFillingFactor;
	}

	/**
	 * Setter for the expected filling factor used to calculate the vehicle
	 * capacity
	 * 
	 * @param fillingFactor
	 *            the value to be set
	 */
	public void setExpectedFillingFactor(double fillingFactor) {
		mExpectedFillingFactor = fillingFactor;
	}

	/** the conversion to be applied to customer demands **/
	private DemandDistribution mDemandDistribution = DemandDistribution.UNIFORM;

	/**
	 * Getter for the conversion to be applied to customer demands
	 * 
	 * @return the value of name
	 */
	public DemandDistribution getDemandDistribution() {
		return this.mDemandDistribution;
	}

	/**
	 * Setter for the conversion to be applied to customer demands
	 * 
	 * @param name
	 *            the value to be set for the conversion to be applied to
	 *            customer demands
	 */
	public void setDemandDistribution(DemandDistribution name) {
		this.mDemandDistribution = name;
	}

	/**
	 * Read the instance defined by the size, set, replica and capacity from
	 * folder {@link #RELATIVE_INSTANCE_PATH}
	 * 
	 * @param size
	 *            the instance size (5, 8, 20, 30, 40, 60 or 100)
	 * @param set
	 *            the set (1 or 2)
	 * @param rep
	 *            the replica (from 1 to 5)
	 * @param cap
	 *            the capacity variant (0 for higher, 1 for lower)
	 * @param seed
	 *            an optional seed to generate sampled demands. If set to
	 *            <code>null</code> then the instance will contain demand
	 *            distribution information.
	 * @return the corresponding instance.
	 * @throws IOException
	 */
	public DynamicInstance readInstance(int size, int set, int rep, int cap,
			Long seed) throws IOException {
		// Check parameters
		// ----------------------------------------------------------------------
		if (Arrays.binarySearch(SIZE_MAPPING, size) < 0) {
			throw new IllegalArgumentException(String.format(
					"Unkonwn size: %s", size));
		}
		if (set < 1 || set > 2) {
			throw new IllegalArgumentException(String.format(
					"Unsupported set argument: %s", set));
		}
		if (rep < 1 || rep > 5) {
			throw new IllegalArgumentException(String.format(
					"Unsupported instance number: %s", rep));
		}
		if (cap < 0 || cap > 1) {
			throw new IllegalArgumentException(String.format(
					"Unsupported vehicle capacity: %s", cap));
		}
		// ----------------------------------------------------------------------
		return readInstance(new File(RELATIVE_INSTANCE_PATH + "/"
				+ getInstanceName(size, set, rep)),
				getCapacity(size, set, cap), seed);
	}

	/**
	 * Read the specified set of instances and return it as a list
	 * 
	 * @param sizes
	 *            the sizes to be read (5, 8, 20, 30, 40, 60 or 100)
	 * @param capacities
	 *            the capacities to be read (0 for higher, 1 for lower)
	 * @param sets
	 *            the sets to be read (1 or 2)
	 * @param seed
	 *            an optional seed to generate sampled demands. If set to
	 *            <code>null</code> then the instance will contain demand
	 *            distribution information.
	 * @return a list containing the specified instances
	 * @throws IOException
	 */
	public List<DynamicInstance> readInstances(int[] sizes, int[] capacities,
			int[] sets, Long seed) throws IOException {
		ArrayList<DynamicInstance> instances = new ArrayList<DynamicInstance>(
				sizes.length * capacities.length * sets.length * 5);
		for (int set : sets) {
			for (int s : sizes) {
				for (int c : capacities) {
					for (int r = 1; r <= 5; r++) {
						instances.add(readInstance(s, set, r, c,
								seed != null ? seed++ : null));
					}
				}
			}
		}

		return instances;
	}

	/**
	 * Read an instance in the Novoa file format.
	 * 
	 * @param input
	 *            the file conaining the instance
	 * @param params
	 *            the first element should be the instance capacity, the second
	 *            is an optional random seed to generate an instance with
	 *            sampled demands.
	 */
	@Override
	public DynamicInstance readInstance(File input, Object... params)
			throws IOException {
		return (DynamicInstance) super.readInstance(input, params);
	}

	@Override
	protected void parseLine(IVRPInstance instance, String line,
			int lineNumber, Object... params) {
		if (lineNumber == 0) {
			mNumRequests = Integer.parseInt(line);
		} else if (lineNumber <= mNumRequests) {
			String[] values = line.split("\\s+");

			int id = Integer.parseInt(values[0]);
			double x = Double.parseDouble(values[1]);
			double y = Double.parseDouble(values[2]);
			int lD = Integer.parseInt(values[3]);
			int uD = Integer.parseInt(values[4]);

			Request request = new Request(id, new Node(id, new PointLocation(x,
					y)));

			if (mRND == null) {
				request.setAttribute(RequestAttributeKey.DEMAND,
						new StochasticDemand(getDemandDistribution(lD, uD)));
			} else {
				request.setAttribute(RequestAttributeKey.DEMAND,
						new DeterministicDemand(lD + mRND.nextInt(uD - lD + 1)));
			}

			instance.addRequest(request);

			mExpectedDemandSum += (uD + lD) / 2d;

			mMaxDemand = Math.max(mMaxDemand, uD);
		}
	}

	/**
	 * Returns a {@link Distribution} representing the demand
	 * 
	 * @param low
	 *            the demand lower value
	 * @param high
	 *            the demand upper value
	 * @return a {@link Distribution} representing the demand
	 */
	protected Distribution getDemandDistribution(int low, int high) {
		switch (getDemandDistribution()) {
		case UNIFORM:
			return new UniformIntDist(low, high);
		case NORMAL:
			// mu is the original expected demand
			double mu = (low + high) / 2d;
			// sigma such that demand is between low-1 and high+1 with 99.7%
			// evaluation
			double sigma = (high - low + 2) / 6d;

			return new NormalDist(mu, sigma);
		default:
			return new UniformIntDist(low, high);
		}
	}

	@Override
	protected DynamicInstance initializeInstance(File input,
			BufferedReader reader, Object... params) {

		DynamicInstance instance = new DynamicInstance(String.format("%sc%s",
				input.getName().replaceFirst(".dat", ""),
				((Number) params[0]).intValue()), ID_HELPER.nextId(),
				VehicleRoutingProblemDefinition.VRPSD);

		// instance.setCostHelper(new BufferedDistance(new
		// EuclidianDistance()));
		instance.setCostHelper(new EuclidianDistance());

		instance.setSymmetric(true);

		List<Depot> depots = new LinkedList<Depot>();
		depots.add(new Depot(0, "CentralDepot", new PointLocation(0, 0)));

		instance.setDepots(depots);

		mMaxDemand = 0;
		mExpectedDemandSum = 0;

		if (params.length > 1 && params[1] instanceof Number) {
			mRND = new Random(((Number) params[1]).longValue());
		}

		return instance;
	}

	@Override
	protected void finalizeInstance(IVRPInstance instance, Object... params) {
		double cap = ((Number) params[0]).doubleValue();
		instance.setFleet(Fleet.newHomogenousFleet(1, new Vehicle(0, "Vehicle",
				cap)));

	}

}
