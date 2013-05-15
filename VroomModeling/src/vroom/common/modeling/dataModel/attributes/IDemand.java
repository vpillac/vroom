package vroom.common.modeling.dataModel.attributes;

/**
 * The Interface IDemand represents a demand for various products that can be associated with a request
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #created 15-Feb-2010 11:29:50 a.m.
 */
public interface IDemand extends IRequestAttribute {

    /**
     * Gets the demand.
     * 
     * @param productId
     *            the id of the product for which the demand value is required
     * @return the demand associated with the product with id
     */
    public double getDemand(int productId);

    /**
     * Number of products
     * 
     * @return the number of products for which a demand is defined
     */
    public int getProductCount();

    /**
     * Return the demand for each product in form of an array.
     * <p>
     * Implementations should ensure that the returned array is independent form internal representation
     * </p>
     * 
     * @return the demand for each product in form of an array
     */
    public double[] asArray();

}