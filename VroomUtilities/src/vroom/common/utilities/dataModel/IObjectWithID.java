package vroom.common.utilities.dataModel;

/**
 * <code>IObjectWithID</code> is an interface for all objects that have an ID.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #created 15-Feb-2010 11:29:50 a.m.
 */
public interface IObjectWithID {

    /**
     * Gets the iD.
     * 
     * @return the id of this object
     */
    public int getID();

}