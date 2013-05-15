/**
 * 
 */
package vroom.trsp.datamodel;

/**
 * <code>TRSPDistanceMatrixUnitSpeed</code> is a specialization of {@link TRSPDistanceMatrix} that assume
 * unit travel speeds.
 * <p>
 * Creation date: Jun 22, 2011 - 3:40:27 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TRSPDistanceMatrixUnitSpeed extends TRSPDistanceMatrix {

    /**
     * Creates a new <code>TRSPDistanceMatrixUnitSpeed</code>
     * 
     * @param instance
     */
    public TRSPDistanceMatrixUnitSpeed(TRSPInstance instance) {
        super(instance);
    }

    @Override
    public double getTravelTime(int o, int d, Technician technician) {
        return getDistance(o, d);
    }

}
