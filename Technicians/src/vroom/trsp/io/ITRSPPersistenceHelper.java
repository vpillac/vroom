package vroom.trsp.io;

import java.io.File;

import vroom.trsp.datamodel.TRSPInstance;

public interface ITRSPPersistenceHelper {

    /**
     * Reads an instance from a flat file
     * 
     * @param file
     *            the {@link File} containing the instance to be read
     * @return a {@link TRSPInstance} containing the instance defined in <code>file</code>
     * @throws Exception
     */
    public abstract TRSPInstance readInstance(File file, Object... params) throws Exception;

    public abstract boolean writeInstance(TRSPInstance instance, File file) throws Exception;

}