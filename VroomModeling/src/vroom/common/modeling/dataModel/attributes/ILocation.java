package vroom.common.modeling.dataModel.attributes;

import vroom.common.utilities.GeoTools.CoordinateSytem;

/**
 * The Interface ILocation.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #created 15-Feb-2010 11:29:50 a.m.
 */
public interface ILocation extends IRequestAttribute, INodeAttribute {
    /**
     * Returns the first coordinate of this location
     * 
     * @return the first coordinate of this location
     */
    public abstract double getX();

    /**
     * Returns the second coordinate of this location
     * 
     * @return the second coordinate of this location
     */
    public abstract double getY();

    /**
     * Returns the coordinate system in which the coordinates are expressed
     * 
     * @return the coordinate system in which the coordinates are expressed
     */
    public CoordinateSytem getCoordinateSystem();

}