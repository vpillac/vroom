package vroom.common.modeling.util;

import vroom.common.modeling.dataModel.Node;
import vroom.common.modeling.dataModel.attributes.PointLocation;
import vroom.common.utilities.Utilities;

/**
 * <code>EuclidianDistance</code> is an extension of {@link CostCalculationDelegate} that handle distances between
 * {@link Node}s as euclidian distances between the corresponding points of the space. <br/>
 * It requires that the {@link Node} contain a location of type {@link PointLocation}.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #created 15-Feb-2010 11:29:50 a.m.
 */
public class EuclidianDistance extends CostCalculationDelegate {

    /**
     * Instantiates a new euclidian distance cost helper.
     */
    public EuclidianDistance() {
        super();
    }

    /*
     * (non-Javadoc)
     * @see
     * vroom.common.modeling.dataModel.CostCalculationDelegate#getDistance(vroom
     * .modelling.VroomModelling.dataModel.Node, vroom.common.modeling.dataModel.Node)
     */
    @Override
    protected double getDistanceInternal(Node origin, Node destination) {
        if (origin == destination
                || Utilities.equal(origin.getLocation(), destination.getLocation())) {
            return 0;
        }

        if (origin.getLocation() instanceof PointLocation
                && destination.getLocation() instanceof PointLocation) {

            double x1 = (origin.getLocation()).getX();
            double x2 = (destination.getLocation()).getX();
            double y1 = (origin.getLocation()).getY();
            double y2 = (destination.getLocation()).getY();

            return Math.pow(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2), 0.5);

        } else {
            throw new IllegalArgumentException(
                    "Both origin and destination node must contain a PointLocation as location");
        }
    }

    @Override
    public String getDistanceType() {
        switch (getRoundingMethod()) {
        case HALF_EVEN:
            return "EUC_2D";
        case CEILING:
            return "CEIL_2D";
        default:
            return String.format("%s[%s]", getRoundingMethod(), getPrecision());
        }
    }

    @Override
    protected void precisionChanged() {
        // Nothing to do
    }

    @Override
    public String toString() {
        return String.format("%s[%s]", getClass().getSimpleName(), getDistanceType());
    }

}// end EuclidianDistance