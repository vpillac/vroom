package vroom.common.modeling.io;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import vroom.common.modeling.dataModel.IVRPInstance;

/**
 * The Interface IPersistenceHelper define the operations that a pecistency helper has to implement to manipulate VRP
 * instances.
 * 
 * @param <F>
 *            the stream type that is used to I/O operations
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 */
public interface IPersistenceHelper<F> {

    /**
     * Loading of an instance.
     * 
     * @param input
     *            the input stream
     * @param params
     *            optional parameters
     * @return the loaded object
     * @throws ParserConfigurationException
     */
    public IVRPInstance readInstance(F input, Object... params) throws Exception;

    /**
     * Exporting of an instance.
     * 
     * @param instance
     *            the object to be exported
     * @param output
     *            the output stream
     * @param params
     *            optional parameters
     * @return if the export was successful, otherwise
     */
    public boolean writeInstance(IVRPInstance instance, F output, Object params) throws IOException;

    /**
     * Reset this instance to its initial state
     */
    public void reset();
}