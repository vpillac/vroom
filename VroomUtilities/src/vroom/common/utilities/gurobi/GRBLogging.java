/**
 *
 */
package vroom.common.utilities.gurobi;

import vroom.common.utilities.logging.LoggerHelper;

/**
 * The class <code>GRBLogging</code> provides a set of loggers to be used by gurobi components
 * <p>
 * Creation date: Aug 25, 2011 - 10:03:23 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class GRBLogging {

    /** The Base logger for Gurobi components */
    public static final LoggerHelper MAIN     = LoggerHelper.getLogger("GRB");
    /** The logger for callbacks */
    public static final LoggerHelper CALLBACK = LoggerHelper.getLogger("GRB.CB");

}
