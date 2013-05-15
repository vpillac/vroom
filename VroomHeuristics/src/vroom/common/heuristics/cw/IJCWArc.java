/*
 * jCW : a java library for the development of saving based heuristics
 */
package vroom.common.heuristics.cw;

import vroom.common.modeling.dataModel.IArc;

/**
 * <code>IJCWArc</code> is an interface for representations of an arc in the jCW framework
 * 
 * @author Jorge E. Mendoza <br/>
 *         <a href="http://www.uco.fr">Universite Catholique de l'Ouest</a>
 * @author Victor Pillac <br/>
 *         <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <br/>
 *         <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 2.0
 */
public interface IJCWArc extends IArc, Comparable<IJCWArc> {

    /**
     * Gets the saving.
     * 
     * @return the value of the saving
     */
    public double getSaving();

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(IJCWArc anotherArc);

}