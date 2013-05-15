package vroom.optimization.online.jmsa;

import vroom.common.utilities.dataModel.IObjectWithID;

/**
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #created 11-Feb-2010 02:56:33 p.m.
 */
public interface IActualRequest extends IMSARequest, IObjectWithID {

    /**
     * Unique ID for this request.
     * <p>
     * Implementations should ensure that this method return the same value across all scenarios for a same request of
     * the underlying instance.
     * 
     * @return the ID of this request
     */
    @Override
    public int getID();

}