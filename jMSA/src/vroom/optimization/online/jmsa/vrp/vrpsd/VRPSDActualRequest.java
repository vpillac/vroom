package vroom.optimization.online.jmsa.vrp.vrpsd;

import java.util.Arrays;

import vroom.common.modeling.dataModel.NodeVisit;
import vroom.common.utilities.ValueUpdate;
import vroom.optimization.online.jmsa.vrp.VRPActualRequest;

/**
 * Creation date: Apr 29, 2010 - 10:49:08 AM<br/>
 * <code>VRPSDActualRequest</code>
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class VRPSDActualRequest extends VRPActualRequest {

    private static final Double UNKNOWN             = null;

    public static final String  PROP_ACTUAL_DEMANDS = "ActualDemands";

    /**
     * An array containing the actual demands of this request, typically made known after visiting the client
     */
    private final Double[]      mActualDemands;

    /**
     * Creates a new <code>VRPSDActualRequest</code>
     * 
     * @param nodeVisit
     */
    public VRPSDActualRequest(NodeVisit nodeVisit) {
        super(nodeVisit);

        mActualDemands = new Double[1];
        for (int p = 0; p < mActualDemands.length; p++) {
            mActualDemands[p] = UNKNOWN;
        }

    }

    /**
     * Getter for this request demand
     * 
     * @return the actual demand if it has been set, and the expected value otherwise
     */
    @Override
    public double getDemand() {
        return getDemand(0);
    }

    /**
     * Getter for this request demand
     * 
     * @return the actual demand if it has been set, and the expected value otherwise
     */
    @Override
    public double getDemand(int product) {
        if (isDemandKnown(product)) {
            return mActualDemands[product];
        } else {
            return super.getDemand(product);
        }
    }

    /**
     * Status of the request demand
     * 
     * @param product
     * @return <code>true</code> if the actual demand has been set for the specified product
     */
    public boolean isDemandKnown(int product) {
        return mActualDemands[product] != UNKNOWN;
    }

    /**
     * Sets the demand realizations
     * 
     * @param demands
     *            an array containing the actual demands in order
     */
    public void setActualDemands(double... demands) {
        if (demands.length != mActualDemands.length) {
            throw new IllegalArgumentException("The given demand array is of incorrect dimension");
        }

        boolean changed = false;

        double[] oldDemands = new double[mActualDemands.length];

        for (int p = 0; p < mActualDemands.length; p++) {
            oldDemands[p] = getDemand(p);
            if (mActualDemands[p] == null || mActualDemands[p] != demands[p]) {
                changed = true;
                mActualDemands[p] = demands[p];
            }
        }

        if (changed) {
            notifyObservers(new ValueUpdate(PROP_ACTUAL_DEMANDS, oldDemands, demands));
        }
    }

    /**
     * Getter for the demand realizations
     * 
     * @return a copy of the demand array
     */
    public Double[] getActualDemands() {
        return Arrays.copyOf(mActualDemands, mActualDemands.length);
    }

    /*
     * (non-Javadoc)
     * @see vroom.optimization.online.jmsa.vrp.VRPMSARequest#toString()
     */
    @Override
    public String toString() {
        return String.format("%s D=%s", super.toString().replaceFirst("AR", "SR"),
                Arrays.toString(mActualDemands));
    }
}
