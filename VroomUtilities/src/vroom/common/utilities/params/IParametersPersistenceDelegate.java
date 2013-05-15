package vroom.common.utilities.params;

import java.io.IOException;

/**
 * <code>IParametersPersistenceDelegate</code> is an interface for classes responsible for the loading and saving of
 * {@linkplain GlobalParameters global parameters}.
 * <p>
 * Creation date: Apr 15, 2010 - 11:29:59 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 * @param <S>
 */
public interface IParametersPersistenceDelegate<S> {

    /**
     * Loading of global parameters
     * 
     * @param params
     *            the global parameters to be completed
     * @param source
     *            the source stream from which parameters will be imported
     * @throws Exception
     */
    public void loadParameters(GlobalParameters params, S source) throws Exception;

    /**
     * Saving of global parameters
     * 
     * @param params
     *            the global parameters to be saved
     * @param output
     *            the output stream where the parameters will be saved
     * @param omitDefaults
     *            <code>true</code> if default values should be omitted
     * @throws IOException
     */
    public void saveParameters(GlobalParameters params, S output, boolean omitDefaults) throws IOException;

}