package vroom.trsp.legacy;

import java.util.List;

import vroom.common.modeling.dataModel.Depot;
import vroom.common.modeling.dataModel.DynamicInstance;
import vroom.common.modeling.dataModel.VehicleRoutingProblemDefinition;
import vroom.common.modeling.util.CostCalculationDelegate;
import vroom.trsp.datamodel.TechnicianFleet;

/**
 * The Class <code>TRSPLegacyInstance</code> is an extension of {@link DynamicInstance} for the TRSP.
 * <p>
 * It contains additional information such as number of skills or tools
 * </p>
 * <p>
 * Creation date: Feb 15, 2011 - 10:37:33 AM.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TRSPLegacyInstance extends DynamicInstance {

    public static final VehicleRoutingProblemDefinition TSRPDefinition = new VehicleRoutingProblemDefinition(
                                                                               "TSRP",
                                                                               "Technician Routing and Scheduling Problem",
                                                                               true,
                                                                               VehicleRoutingProblemDefinition.CVRPTW);

    /** the number of existing skills **/
    private final int                                   mSkillCount;

    /**
     * Getter for the number of existing skills
     * 
     * @return the number of existing skills
     */
    public int getSkillCount() {
        return this.mSkillCount;
    }

    /** the number of available tools **/
    private final int mToolCount;

    /**
     * Getter for the number of available tools
     * 
     * @return the number of available tools
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

    public TRSPLegacyInstance(String name, int id, TechnicianFleet fleet, List<Depot> depots, int skills, int tools,
            int spare, CostCalculationDelegate costHelper) {
        super(name, id, fleet, depots, TSRPDefinition, costHelper);
        mToolCount = tools;
        mSkillCount = skills;
        mSpareCount = spare;
    }

    /**
     * Getter for the main depot for this instance
     * 
     * @return the main depot
     */
    public Depot getMainDepot() {
        return getDepot(0);
    }

    @Override
    public TechnicianFleet getFleet() {
        return (TechnicianFleet) super.getFleet();
    }
}
