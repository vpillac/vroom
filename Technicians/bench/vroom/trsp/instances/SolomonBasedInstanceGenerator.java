package vroom.trsp.instances;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import umontreal.iro.lecuyer.probdist.DiscreteDistributionInt;
import umontreal.iro.lecuyer.probdist.UniformIntDist;
import umontreal.iro.lecuyer.rng.MRG32k3a;
import umontreal.iro.lecuyer.rng.RandomStream;
import vroom.common.modeling.dataModel.Depot;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.dataModel.IVRPRequest;
import vroom.common.modeling.dataModel.Vehicle;
import vroom.common.modeling.dataModel.attributes.ITimeWindow;
import vroom.common.modeling.dataModel.attributes.NodeAttributeKey;
import vroom.common.modeling.dataModel.attributes.PointLocation;
import vroom.common.modeling.dataModel.attributes.RequestAttributeKey;
import vroom.common.modeling.dataModel.attributes.SimpleTimeWindow;
import vroom.common.modeling.io.SolomonPersistenceHelper;
import vroom.common.utilities.Utilities;
import vroom.common.utilities.ssj.RandomGeneratorManager;
import vroom.trsp.datamodel.TRSPRequest;
import vroom.trsp.datamodel.Technician;
import vroom.trsp.datamodel.TechnicianFleet;
import vroom.trsp.io.PillacSimplePersistenceHelper;
import vroom.trsp.io.TRSPSolomonLegacyPersistenceHelper;
import vroom.trsp.legacy.TRSPLegacyInstance;

/**
 * The Class <code>SolomonBasedInstanceGenerator</code> is used to generate TRSP instances based on Solomon instances.
 * <p>
 * Creation date: Feb 11, 2011 - 10:42:39 AM.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class SolomonBasedInstanceGenerator {
    public enum Mode {
        NORMAL("../Instances/trsp/pillac/%s.txt"), //
        SIZE25("../Instances/trsp/pillac/25/%s.txt"), //
        TOY("../Instances/trsp/toy/%s.txt"), //
        CVRPTW("../Instances/trsp/cvrptw/%s.txt");

        public final String filePattern;

        /**
         * Creates a new <code>Mode</code>
         * 
         * @param file
         */
        private Mode(String file) {
            this.filePattern = file;
        }

    }

    // private static final String SRC_FOLDER = "../Instances/cvrptw/solomon_25";
    // private static final String SRC_FOLDER = "../Instances/cvrptw/solomon_50";
    private static final String                 SRC_FOLDER          = "../Instances/cvrptw/solomon";
    private static final String                 SOLOMON_BKS         = "../Instances/cvrptw/solomon.sol";

    private static final Mode                   MODE                = Mode.CVRPTW;

    // Expected number of request per technician
    public static final int                     sReqPerTech         = 8;

    // Total number of skills
    public static final int                     sSkillCount         = 5;
    // Total number of tools
    public static final int                     sToolCount          = 5;
    // Total number of spare parts
    public static final int                     sSpareCount         = 5;

    // Number of skills for each technician
    public static final DiscreteDistributionInt sTechSkillsDist     = new UniformIntDist(2, sSkillCount - 1);
    // Number of tools for each technician
    public static final DiscreteDistributionInt sTechToolsDist      = new UniformIntDist(0, sToolCount);
    // Number of spare parts for each technician
    public static final DiscreteDistributionInt sTechSpareCountDist = new UniformIntDist(2, sSpareCount);
    // Number of spare parts of each type for each technician
    public static final DiscreteDistributionInt sTechSpareDist      = new UniformIntDist(2, 5);

    // Number of skills for each request
    public static final DiscreteDistributionInt sReqSkillsDist      = new UniformIntDist(1, 1);
    // Number of tools for each request
    public static final DiscreteDistributionInt sReqToolsDist       = new UniformIntDist(0, 2);
    // Number of spare parts for each request
    public static final DiscreteDistributionInt sReqSpareCountDist  = new UniformIntDist(0, 2);
    // Number of spare parts of each type for each request
    public static final DiscreteDistributionInt sReqSpareDist       = new UniformIntDist(1, 1);

    private final int                           reqPerTech;

    private final Mode                          mode;

    private final RandomStream                  stream;
    private final int                           skillCount;
    private final int                           toolCount;
    private final int                           spareCount;
    private final DiscreteDistributionInt       techSkillsDist;
    private final DiscreteDistributionInt       techToolsDist;
    private final DiscreteDistributionInt       techSpareCountDist;
    private final DiscreteDistributionInt       techSpareDist;
    private final DiscreteDistributionInt       reqSkillsDist;
    private final DiscreteDistributionInt       reqToolsDist;
    private final DiscreteDistributionInt       reqSpareCountDist;
    private final DiscreteDistributionInt       reqSpareDist;

    private final RandomGeneratorManager        rndGen;

    private int[][]                             toolSets;
    private int[][]                             spareSets;

    /**
     * Creates a new <code>SolomonBasedInstanceGenerator</code>
     * 
     * @param stream
     * @param skillCount
     * @param toolCount
     * @param spareCount
     * @param techSkillsDist
     * @param techToolsDist
     * @param techSpareCountDist
     * @param techSpareDist
     * @param reqSkillsDist
     * @param reqToolsDist
     * @param reqSpareCountDist
     * @param reqSpareDist
     */
    public SolomonBasedInstanceGenerator(Mode mode, RandomStream stream, int reqPerTech, int skillCount, int toolCount,
            int spareCount, DiscreteDistributionInt techSkillsDist, DiscreteDistributionInt techToolsDist,
            DiscreteDistributionInt techSpareCountDist, DiscreteDistributionInt techSpareDist,
            DiscreteDistributionInt reqSkillsDist, DiscreteDistributionInt reqToolsDist,
            DiscreteDistributionInt reqSpareCountDist, DiscreteDistributionInt reqSpareDist) {
        this.mode = mode;
        this.stream = stream;
        this.skillCount = mode == Mode.CVRPTW ? 0 : skillCount;
        this.toolCount = mode == Mode.CVRPTW ? 0 : toolCount;
        this.spareCount = mode == Mode.CVRPTW ? 1 : spareCount;
        this.techSkillsDist = techSkillsDist;
        this.techToolsDist = techToolsDist;
        this.techSpareCountDist = techSpareCountDist;
        this.techSpareDist = techSpareDist;
        this.reqSkillsDist = reqSkillsDist;
        this.reqToolsDist = reqToolsDist;
        this.reqSpareCountDist = reqSpareCountDist;
        this.reqSpareDist = reqSpareDist;
        this.reqPerTech = reqPerTech;

        rndGen = new RandomGeneratorManager(stream);
    }

    /**
     * Generate a TRSP instance from an original VRPTW instance
     * 
     * @param instance
     * @return
     */
    public TRSPLegacyInstance generateInstance(IVRPInstance instance) {

        generateSubsets();
        int fleetSize = (int) Math.ceil(((double) instance.getRequestCount()) / reqPerTech);

        if (mode == Mode.CVRPTW) {
            fleetSize = instance.getFleet().size();
            // try {
            // BestKnownSolutions bks = new BestKnownSolutions(SOLOMON_BKS);
            // fleetSize = bks.getIntValue(instance.getName() + "." + instance.getRequestCount(), "K");
            // } catch (Exception e) {
            // // e.printStackTrace();
            // }
        }

        // Generate requests
        Collection<TRSPRequest> requests = generateRequests(instance, fleetSize);

        // Generate crew
        List<Technician> technicians = generateCrew(instance, requests, fleetSize);

        // Depots
        ArrayList<Depot> depots = new ArrayList<Depot>(technicians.size() + 1);
        if (mode == Mode.TOY)
            instance.getDepot(0).setAttribute(NodeAttributeKey.TIME_WINDOW, new SimpleTimeWindow(0, 9999));
        depots.add(instance.getDepot(0));
        for (Technician t : technicians) {
            depots.add(t.getHome());
        }

        // Define the new instance
        String name;
        switch (mode) {
        case CVRPTW:
            name = String.format("%s.%s", instance.getName(), instance.getRequestCount());
            break;
        default:
            name = String.format("%s.%s_%s-%s-%s-%s", instance.getName(), instance.getRequestCount(), reqPerTech,
                    skillCount, toolCount, spareCount);
            break;
        }
        TRSPLegacyInstance trspInstance = new TRSPLegacyInstance(
                //
                name, instance.getID(), TechnicianFleet.newTechnicianFleet(technicians), depots, skillCount, toolCount,
                spareCount, instance.getCostDelegate());

        trspInstance.addRequests(Utilities.<TRSPRequest, IVRPRequest> convertToList(requests));

        return trspInstance;
    }

    private List<Technician> generateCrew(IVRPInstance instance, Collection<TRSPRequest> requests, int fleetSize) {
        ArrayList<Technician> technicians = new ArrayList<Technician>(fleetSize);

        TechnicianComparator comp = new TechnicianComparator(instance);

        // Generate technician skills and tools
        // for (int id = 0; id < instance.getFleet().size(); id++) {
        for (int id = 0; id < fleetSize; id++) {
            Technician tec = generateTechnician(instance.getFleet().getVehicle(), id, instance);

            technicians.add(tec);
        }

        if (mode == Mode.CVRPTW)
            return technicians;

        comp.update(technicians, requests);

        int attempts = 0;
        if (!comp.mUnfeasibleRequests.isEmpty()) {
            System.out.printf(" ... fixing %s infeasible requests ... ", comp.mUnfeasibleRequests.size());
        }
        while (!comp.mUnfeasibleRequests.isEmpty()) {
            Collections.sort(technicians, comp);

            if (attempts > 100) {
                Set<TRSPRequest> old = new HashSet<TRSPRequest>(comp.mUnfeasibleRequests);
                int i = technicians.size() - 1;
                for (TRSPRequest r : comp.mUnfeasibleRequests) {
                    Technician t = technicians.get(i);
                    t = generateTechnician(t, t.getID(), instance, r);
                    technicians.set(i, t);
                    i--;
                }
                comp.update(technicians, requests);
                for (TRSPRequest r : old)
                    if (comp.mUnfeasibleRequests.contains(r))
                        throw new IllegalStateException("Cannot generate a technician able to serve request " + r);
                attempts = 0;
            } else {
                Technician t = technicians.remove(technicians.size() - 1);
                technicians.add(generateTechnician(t, t.getID(), instance));
                comp.update(technicians, requests);
            }
            attempts++;
        }

        if (!comp.mUnfeasibleRequests.isEmpty()) {
            System.out.print(" ... ok ... ");
        }

        return technicians;
    }

    /**
     * Generate a technician from the given original vehicle.
     * 
     * @param original
     *            the original
     * @param id
     *            the id
     * @param instance
     *            the instance
     * @return the technician
     */
    private Technician generateTechnician(Vehicle original, int id, IVRPInstance instance) {

        int[] tecSkills;
        int[] tecTools;
        int[] tecSpareId;
        int[] tecSpare;
        if (mode == Mode.CVRPTW) {
            tecSkills = new int[skillCount];
            for (int i = 0; i < tecSkills.length; i++)
                tecSkills[i] = i;
            tecTools = new int[toolCount];
            for (int i = 0; i < tecTools.length; i++)
                tecTools[i] = i;
            tecSpare = new int[] { (int) original.getCapacity() };
        } else {
            if (mode == Mode.TOY) {
                tecSkills = new int[skillCount];
                for (int i = 0; i < tecSkills.length; i++)
                    tecSkills[i] = i;
            }
            tecSkills = generateSubset(rndGen.nextInt(techSkillsDist), skillCount, stream);
            tecTools = generateSubset(toolSets, tecSkills, rndGen.nextInt(techToolsDist), stream);
            tecSpareId = generateSubset(spareSets, tecSkills, rndGen.nextInt(techSpareCountDist), stream);
            tecSpare = new int[spareCount];
            for (int s = 0; s < tecSpareId.length; s++) {
                tecSpare[tecSpareId[s]] = rndGen.nextInt(techSpareDist);
            }
        }
        // Generate the technician home
        int homeX = mode == Mode.CVRPTW ? (int) instance.getDepot(0).getLocation().getX() : stream.nextInt(0, 100);
        int homeY = mode == Mode.CVRPTW ? (int) instance.getDepot(0).getLocation().getY() : stream.nextInt(0, 100);
        Depot home = new Depot(id + 1, new PointLocation(homeX, homeY));
        if (mode == Mode.TOY)
            home.setAttribute(NodeAttributeKey.TIME_WINDOW, new SimpleTimeWindow(0, 9999));
        else
            home.setAttribute(NodeAttributeKey.TIME_WINDOW, instance.getDepot(0).getTimeWindow());

        // Create a new technician
        Technician tec = new Technician(id, "" + id, original.getFixedCost(), original.getVariableCost(), 1, tecSkills,
                tecTools, tecSpare, home);

        return tec;
    }

    /**
     * Generate a technician for a specific request.
     * 
     * @param original
     *            the original
     * @param id
     *            the id
     * @param instance
     *            the instance
     * @param request
     *            the request
     * @return the technician
     */
    private Technician generateTechnician(Vehicle original, int id, IVRPInstance instance, TRSPRequest request) {
        // The skill set is initialized from the request set
        int[] tecSkills = generateSubset(rndGen.nextInt(techSkillsDist), skillCount, stream);
        TreeSet<Integer> skills = new TreeSet<Integer>(request.getSkillSet().toList());
        int s = 0;
        // Add more skills
        while (s < tecSkills.length && skills.size() < tecSkills.length) {
            if (!skills.contains(tecSkills[s]))
                skills.add(tecSkills[s]);
            s++;
        }
        tecSkills = new int[skills.size()];
        s = 0;
        for (int i : skills) {
            tecSkills[s++] = i;
        }
        Arrays.sort(tecSkills);

        int[] tecTools = generateSubset(toolSets, tecSkills, rndGen.nextInt(techToolsDist), stream);
        TreeSet<Integer> tools = new TreeSet<Integer>(request.getToolSet().toList());
        s = 0;
        // Add more skills
        while (s < tecTools.length && tools.size() < tecTools.length) {
            if (!tools.contains(tecTools[s]))
                tools.add(tecTools[s]);
            s++;
        }
        tecTools = new int[tools.size()];
        s = 0;
        for (int i : tools) {
            tecTools[s++] = i;
        }
        Arrays.sort(tecTools);

        int[] tecSpareId = generateSubset(spareSets, tecSkills, rndGen.nextInt(techSpareCountDist), stream);
        int[] tecSpare = new int[spareCount];
        for (s = 0; s < tecSpareId.length; s++) {
            tecSpare[tecSpareId[s]] = rndGen.nextInt(techSpareDist);
        }
        // Ensure that the technician has the required spare parts
        for (int i = 0; i < tecSpare.length; i++) {
            tecSpare[i] = Math.max(tecSpare[i], request.getSparePartRequirement(i));
        }

        // Generate the technician home
        int homeX = (int) request.getNode().getLocation().getX();
        int homeY = (int) request.getNode().getLocation().getY();
        Depot home = new Depot(id + 1, new PointLocation(homeX, homeY));

        if (mode == Mode.TOY)
            home.setAttribute(NodeAttributeKey.TIME_WINDOW, new SimpleTimeWindow(0, 9999));
        else
            home.setAttribute(NodeAttributeKey.TIME_WINDOW, instance.getDepot(0).getTimeWindow());

        // Create a new technician
        Technician tec = new Technician(id, "" + id, original.getFixedCost(), original.getVariableCost(), 1, tecSkills,
                tecTools, tecSpare, home);

        return tec;
    }

    /**
     * Generate the tools and spare parts subsets that will be used to generate technicians and requests
     */
    private void generateSubsets() {
        // Generate the sets of tools associated with each skill
        toolSets = new int[skillCount][];
        int maxToolsPerSkill = (int) ((3f * toolCount) / skillCount);
        int minToolsPerSkill = (int) ((2f * toolCount) / skillCount);
        // int numTools = (int) Math.ceil(1f * toolCount / skillCount);
        for (int s = 0; s < skillCount; s++) {
            // Number of tools
            int numTools = stream.nextInt(minToolsPerSkill, maxToolsPerSkill);
            toolSets[s] = generateSubset(numTools, toolCount, stream);
            // toolSets[s] = new int[numTools];
            // for (int i = 0; i < numTools; i++) {
            // toolSets[s][i] = s * numTools + i;
            // }
        }

        // Generate the sets of spare parts associated with each skill
        spareSets = new int[skillCount][];
        int maxSparePerSkill = (int) ((2f * toolCount) / skillCount);
        int minSparePerSkill = (int) ((1f * toolCount) / skillCount);
        // int numSpare = (int) Math.ceil(1f * spareCount / skillCount);
        for (int s = 0; s < skillCount; s++) {
            // Number of spare parts
            int numSpare = stream.nextInt(minSparePerSkill, maxSparePerSkill);
            spareSets[s] = generateSubset(numSpare, spareCount, stream);
            // spareSets[s] = new int[numSpare];
            // for (int i = 0; i < numSpare; i++) {
            // spareSets[s][i] = s * numSpare + i;
            // }
        }
    }

    /**
     * Generate a list of TRSPRequest from a given instance
     * 
     * @param instance
     *            the base instance
     * @return
     */
    private Collection<TRSPRequest> generateRequests(IVRPInstance instance, int fleetSize) {
        Collection<TRSPRequest> requests = new LinkedList<TRSPRequest>();
        for (IVRPRequest request : instance.getRequests()) {
            int[] reqSkills = generateSubset(rndGen.nextInt(reqSkillsDist), skillCount, stream);
            int[] reqTools = generateSubset(toolSets, reqSkills, rndGen.nextInt(reqToolsDist), stream);
            int[] reqSpareId = generateSubset(spareSets, reqSkills, rndGen.nextInt(reqSpareCountDist), stream);

            int[] reqSpare;
            if (mode == Mode.CVRPTW) {
                reqSpare = new int[] { (int) request.getDemand() };
            } else {
                reqSpare = new int[spareCount];
                for (int s = 0; s < reqSpareId.length; s++) {
                    reqSpare[reqSpareId[s]] = rndGen.nextInt(reqSpareDist);
                }
            }

            ITimeWindow tw = mode == Mode.TOY ? new SimpleTimeWindow(0, 9999) : request
                    .getAttribute(RequestAttributeKey.TIME_WINDOW);

            TRSPRequest r = new TRSPRequest(request.getID() + fleetSize, request.getNode(), reqSkills, reqTools,
                    reqSpare, tw, (int) request.getAttribute(RequestAttributeKey.SERVICE_TIME).getDuration());
            requests.add(r);
        }
        return requests;
    }

    /**
     * @param numElements
     *            the number of elements for the subset
     * @param parentSetSize
     *            the size of the parent set
     * @param stream
     * @return <code>numElements</code> taken from the set <code>{0,..,parentSetSize-1}</code>
     */
    public static int[] generateSubset(int numElements, int parentSetSize, RandomStream stream) {
        if (parentSetSize == 0)
            return new int[0];

        if (numElements > parentSetSize) {
            int[] set = new int[parentSetSize];
            for (int i = 0; i < set.length; i++) {
                set[i] = i;
            }
        }

        LinkedList<Integer> pool = new LinkedList<Integer>();
        for (int i = 0; i < parentSetSize; i++) {
            pool.add(i);
        }

        int count = 0;
        int[] set = new int[numElements];
        while (count < numElements && !pool.isEmpty()) {
            int id = stream.nextInt(0, pool.size() - 1);
            set[count++] = pool.remove(id);
        }

        Arrays.sort(set);

        return set;
    }

    /**
     * @param sets
     *            an array containing the different available sets
     * @param indexSubset
     *            an array containing the indexes of the availables sets in <code>set</code>
     * @param numElements
     *            the number of elements to generate
     * @param stream
     * @return a subset of size <code>numElements</code> of the union of subsets defined by <code>sets</code> and
     *         <code>indexSubset</code>
     */
    public static int[] generateSubset(int[][] sets, int[] indexSubset, int numElements, RandomStream stream) {
        if (numElements == 0 || sets.length == 0)
            return new int[0];
        Set<Integer> union = new HashSet<Integer>();
        for (int i : indexSubset) {
            for (int e : sets[i]) {
                union.add(e);
            }
        }
        int[] set;
        if (union.size() < numElements) {
            set = new int[union.size()];
            int i = 0;
            for (int e : union) {
                set[i++] = e;
            }
        } else {

            LinkedList<Integer> pool = new LinkedList<Integer>(union);

            int count = 0;
            set = new int[numElements];
            while (count < numElements && !pool.isEmpty()) {
                int id = stream.nextInt(0, pool.size() - 1);
                set[count++] = pool.remove(id);
            }
        }

        Arrays.sort(set);

        return set;
    }

    public static void main(String[] args) {

        RandomStream stream = new MRG32k3a("rnd");

        File instanceDir = new File(SRC_FOLDER);
        String[] children = instanceDir.list();
        LinkedList<File> instances = new LinkedList<File>();
        for (String file : children) {
            if (!file.startsWith(".") && !file.startsWith("1-")) {
                try {
                    instances.add(new File(instanceDir.getAbsolutePath() + File.separator + file));
                } catch (Exception e) {
                    System.err.printf("Error when reading %s : %s", file, e.getMessage());
                }
            }
        }
        // instances.add(new File(SRC_FOLDER + "/C106.txt"));

        System.out.println("Mode : " + MODE);

        generateInstances(MODE, instances, sReqPerTech, sSkillCount, sToolCount, sSpareCount, sTechSkillsDist,
                sTechToolsDist, sTechSpareCountDist, sTechSpareDist, sReqSkillsDist, sReqToolsDist, sReqSpareCountDist,
                sReqSpareDist, stream);
    }

    public static void generateInstances(Mode mode, List<File> instanceFiles, int reqPerTech, int skillCount,
            int toolCount, int spareCount, DiscreteDistributionInt techSkillsDist,
            DiscreteDistributionInt techToolsDist, DiscreteDistributionInt techSpareCountDist,
            DiscreteDistributionInt techSpareDist, DiscreteDistributionInt reqSkillsDist,
            DiscreteDistributionInt reqToolsDist, DiscreteDistributionInt reqSpareCountDist,
            DiscreteDistributionInt reqSpareDist, RandomStream stream) {
        if (mode == Mode.CVRPTW) {
            spareCount = 1;
        }

        SolomonPersistenceHelper reader = new SolomonPersistenceHelper();
        TRSPSolomonLegacyPersistenceHelper writer = new TRSPSolomonLegacyPersistenceHelper();
        PillacSimplePersistenceHelper tester = new PillacSimplePersistenceHelper();

        SolomonBasedInstanceGenerator gen = new SolomonBasedInstanceGenerator(mode, stream, reqPerTech, skillCount,
                toolCount, spareCount, techSkillsDist, techToolsDist, techSpareCountDist, techSpareDist, reqSkillsDist,
                reqToolsDist, reqSpareCountDist, reqSpareDist);

        for (File i : instanceFiles) {
            System.out.printf("Converting instance %s ... ", i.getPath());
            IVRPInstance vrptwInstance;
            try {
                vrptwInstance = reader.readInstance(i);
                TRSPLegacyInstance trspInstance = gen.generateInstance(vrptwInstance);
                File dest = new File(String.format(mode.filePattern, trspInstance.getName()));
                writer.writeInstance(trspInstance, dest, null);
                System.out.printf("writen to file %s\n", dest.getName());
                tester.readInstance(dest, false);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            } catch (IllegalStateException e) {
                e.printStackTrace();
                continue;
            }
        }
        System.out.println("DONE");
    }

    private static class TechnicianComparator implements Comparator<Technician> {
        private final Map<Technician, Integer> mEvaluations;
        private final IVRPInstance             mInstance;
        private final Set<TRSPRequest>         mUnfeasibleRequests;

        private TechnicianComparator(IVRPInstance instance) {
            mEvaluations = new HashMap<Technician, Integer>();
            mUnfeasibleRequests = new HashSet<TRSPRequest>();
            mInstance = instance;
        }

        private void update(Collection<Technician> technicians, Collection<TRSPRequest> requests) {
            mEvaluations.clear();
            mUnfeasibleRequests.clear();
            mUnfeasibleRequests.addAll(requests);
            for (TRSPRequest r : requests) {
                for (Technician t : technicians) {
                    int eval = mEvaluations.containsKey(t) ? mEvaluations.get(t) : 0;
                    if (r.getSkillSet().isCompatibleWith(t.getSkillSet())) {
                        double arrivalTime = t.getHome().getTimeWindow().startAsDouble();
                        double returnTime;
                        // The technician can serve the request
                        if (r.getToolSet().isCompatibleWith(t.getToolSet())
                                && Utilities.compare(r.getSparePartRequirements(), t.getSpareParts()) <= 0) {
                            // The technician already has the tools to serve the request directly
                            // Check if time windows are compatible
                            arrivalTime += mInstance.getCostDelegate().getCost(t.getHome(), r.getNode(), t);
                            returnTime = r.getTimeWindow().getEarliestStartOfService(arrivalTime) + r.getServiceTime()
                                    + mInstance.getCostDelegate().getCost(r.getNode(), t.getHome(), t);
                        } else {
                            // The technician does not have the tools to serve the request directly, must go to central
                            // depot first
                            // Check if time windows are compatible
                            arrivalTime += mInstance.getCostDelegate().getCost(t.getHome(), mInstance.getDepot(0), t)
                                    + mInstance.getCostDelegate().getCost(mInstance.getDepot(0), r.getNode(), t);
                            returnTime = r.getTimeWindow().getEarliestStartOfService(arrivalTime) + r.getServiceTime()
                                    + mInstance.getCostDelegate().getCost(r.getNode(), t.getHome(), t);
                        }
                        if (r.getTimeWindow().isFeasible(arrivalTime)
                                && t.getHome().getTimeWindow().isFeasible(returnTime)) {
                            // The request can be served
                            eval++;
                            mUnfeasibleRequests.remove(r);
                        }
                    }
                    mEvaluations.put(t, eval);
                }
            }
        }

        @Override
        public int compare(Technician o1, Technician o2) {
            return mEvaluations.get(o2) - mEvaluations.get(o1);
        }
    }
}
