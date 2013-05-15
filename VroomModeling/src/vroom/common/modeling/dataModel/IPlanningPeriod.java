package vroom.common.modeling.dataModel;

/**
 * <code>IPlanningPeriod</code> is the interface for classes representing a planning period. A planning period is
 * defined by a start date, an end date, and a time division, all convertible to a <code>long</code> value.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:07:25 a.m.
 */
public interface IPlanningPeriod {

    /**
     * End as long.
     * 
     * @return a long value representing the end date of this planning period
     */
    public long endAsLong();

    /**
     * Interval as long.
     * 
     * @return a long value representing the interval length of this planning period
     */
    public long intervalAsLong();

    /**
     * Size.
     * 
     * @return the number of intervals in this planning period
     */
    public int size();

    /**
     * Start as long.
     * 
     * @return a long value representing the start date of this planning period
     */
    public long startAsLong();

}