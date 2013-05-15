package vroom.trsp.datamodel;

import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.Node;
import vroom.common.modeling.dataModel.Vehicle;

/**
 * <code>TRSPDistTimeMatrix</code>
 * <p>
 * Creation date: Jun 13, 2012 - 11:40:31 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TRSPDistTimeMatrix extends TRSPDistanceMatrix {

    private final double[][] mTimeMatrix;

    /**
     * Creates a new <code>TRSPDistTimeMatrix</code>
     * 
     * @param instance
     * @param distTimeMatrix
     */
    public TRSPDistTimeMatrix(TRSPInstance instance, double[][][] distTimeMatrix) {
        super(instance, distTimeMatrix[0]);

        mTimeMatrix = new double[instance.getMaxId()][instance.getMaxId()];

        for (int i = 0; i < instance.getMaxId(); i++) {
            for (int j = 0; j < instance.getMaxId(); j++) {
                int idi = instance.getOriginalId(i);
                int idj = instance.getOriginalId(j);

                mTimeMatrix[i][j] = distTimeMatrix[1][i][j];
            }
        }
    }

    @Override
    public double getTravelTime(INodeVisit origin, INodeVisit destination, Vehicle vehicle) {
        return mTimeMatrix[origin.getID()][destination.getID()];
    }

    @Override
    public double getTravelTime(int o, int d, Technician technician) {
        return mTimeMatrix[o][d];
    }

    @Override
    public double getTravelTime(Node origin, Node destination, Vehicle vehicle) {
        return mTimeMatrix[origin.getID()][destination.getID()];
    }
}
