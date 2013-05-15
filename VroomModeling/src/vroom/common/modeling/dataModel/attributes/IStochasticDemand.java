package vroom.common.modeling.dataModel.attributes;

import umontreal.iro.lecuyer.probdist.Distribution;

/**
 * The Interface IStochasticDemand represent a stochastic demand for various products that can be associated with a
 * request
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #created 15-Feb-2010 11:29:50 a.m.
 */
public interface IStochasticDemand extends IDemand {

    /**
     * Gets the distribution.
     * 
     * @param productId
     *            the id of the product for which the demand distribution has to be retreived
     * @return the demand distribution for the product with id
     */
    public Distribution getDistribution(int productId);

    /**
     * Gets the demand.
     * 
     * @param productId
     *            the id of the product for which the demand value is required
     * @return the mean of the demand associated with the product with id
     */
    @Override
    public double getDemand(int productId);

}