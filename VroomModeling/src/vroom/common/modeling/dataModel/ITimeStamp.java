package vroom.common.modeling.dataModel;

/**
 * <code>ITimeStamp</code> is the interface for classes that represent a specific instant in time.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:07:26 a.m.
 */
public interface ITimeStamp extends Comparable<ITimeStamp> {

    /**
     * Long value.
     * 
     * @return a representing the instant in time of this time stamp
     */
    public double doubleValue();

}