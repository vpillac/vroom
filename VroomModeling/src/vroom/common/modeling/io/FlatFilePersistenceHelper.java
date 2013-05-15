package vroom.common.modeling.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import vroom.common.modeling.dataModel.IVRPInstance;

/**
 * <code>FlatFilePersistenceHelper</code> is an implementation of {@link IPersistenceHelper} that manages plain text
 * files.
 * <p>
 * Creation date: Jan 27, 2011 - 2:56:19 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public abstract class FlatFilePersistenceHelper implements IPersistenceHelper<java.io.File> {

    private boolean mWorking = false;

    @Override
    public IVRPInstance readInstance(File input, Object... params) throws IOException {
        if (mWorking) {
            throw new IllegalStateException("The parser is already being used");
        } else {
            mWorking = true;
        }

        BufferedReader reader = new BufferedReader(new FileReader(input));

        IVRPInstance instance = initializeInstance(input, reader, params);

        int lineNumber = 0;
        String line = reader.readLine();
        while (line != null) {
            if (line.startsWith(" "))
                line = line.replaceFirst(" +", "");
            parseLine(instance, line, lineNumber, params);
            line = reader.readLine();
            lineNumber++;
        }

        mWorking = false;
        finalizeInstance(instance, params);

        reader.close();

        return instance;
    }

    /**
     * Method called to finalize the instance
     * 
     * @param instance
     * @param params
     */
    protected abstract void finalizeInstance(IVRPInstance instance, Object... params);

    /**
     * Method called when a new line has been read
     * 
     * @param instance
     * @param line
     * @param lineNumber
     * @param params
     */
    protected abstract void parseLine(IVRPInstance instance, String line, int lineNumber, Object... params);

    /**
     * Method call to initialize the instance object
     * 
     * @param input
     * @param reader
     * @param params
     * @return
     * @throws IOException
     */
    protected abstract IVRPInstance initializeInstance(File input, BufferedReader reader, Object... params)
            throws IOException;

    @Override
    public boolean writeInstance(IVRPInstance instance, File output, Object params) throws IOException {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.io.IPersistenceHelper#reset()
     */
    @Override
    public void reset() {
        mWorking = false;
    }
}