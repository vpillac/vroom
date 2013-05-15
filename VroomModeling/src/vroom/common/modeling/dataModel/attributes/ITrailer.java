package vroom.common.modeling.dataModel.attributes;

import vroom.common.modeling.dataModel.Vehicle;

/**
 * <code>ITrailer</code> is an interface for classes that will describe the trailer(s) associated with a vehicle.
 * 
 * @param <C>
 *            is the type used to represent the capacities (dimensions) of a trailer. Common values would be
 *            {@link Integer} or {@link Double} for the CVRP.
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @see Vehicle
 */
public interface ITrailer<C> extends IVehicleAttribute {

    /**
     * Number of trailers.
     * 
     * @return the number of trailer(s) described by this instance
     */
    public int getTrailerCount();

    /**
     * Getter for the trailer capacity.
     * 
     * @return the capacity of this trailer
     * @see #getCapacity(int, int)
     * @see #getCapacity(int)
     * @see Vehicle#getCapacity()
     */
    public C getCapacity();

    /**
     * Getter for the trailer capacity.
     * 
     * @param trailer
     *            the index of the considered trailer
     * @return the capacity of the
     * @see #getCapacity(int, int)
     * @see #getCapacity()
     * @see Vehicle#getCapacity()
     */
    public C getCapacity(int trailer);

    /**
     * Getter for the trailer capacity for a given product.
     * 
     * @param trailer
     *            the index of the considered trailer
     * @param product
     *            the index of the considered product
     * @return the capacity of the for the
     * @see #getCapacity(int, int)
     * @see #getCapacity()
     * @see Vehicle#getCapacity(int)
     */
    public C getCapacity(int trailer, int product);

}
