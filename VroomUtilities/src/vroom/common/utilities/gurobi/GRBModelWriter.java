/**
 *
 */
package vroom.common.utilities.gurobi;

import gurobi.GRBException;
import gurobi.GRBModel;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import vroom.common.utilities.Utilities;
import vroom.common.utilities.logging.LoggerHelper;

/**
 * <code>GRBModelWriter</code> is a utility class that provides methods to write instances of {@link GRBModel} into a
 * compresses file
 * <p>
 * Creation date: Aug 17, 2011 - 10:43:51 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class GRBModelWriter {
    public static boolean    sWriteToZip = false;

    private static final int BUFFER      = 1048576;

    private final String     mDate;
    private final String     mPath;

    private final File       mZipFile;
    private final File       mZipTmpFile;

    /**
     * Creates a new <code>GRBModelWriter</code>
     * 
     * @param path
     *            the path of the destination directory
     * @param zipName
     *            the name of the compressed archive (without extension)
     * @throws IOException
     */
    public GRBModelWriter(String path, String zipName) throws IOException {
        mDate = Utilities.Time.getDateString();
        mPath = path;
        if (sWriteToZip) {
            mZipFile = new File(String.format("%s/%s_%s.zip", path, zipName, mDate));
            mZipTmpFile = new File(String.format("%s/%s_%s_tmp.zip", path, zipName, mDate));
        } else {
            mZipFile = null;
            mZipTmpFile = null;
        }
    }

    /**
     * Write a model to a .mps file and add it to the compressed archive
     * 
     * @param model
     *            the model to be written
     * @param name
     *            the name of the model
     * @throws IOException
     */
    public void write(GRBModel model, String name) throws IOException {
        if (sWriteToZip)
            writeToZIP(model, name);
        else
            writePlain(model, name);
    }

    /**
     * Write a model to two files (.mst and .mps.bz2)
     * 
     * @param model
     *            the model to be written
     * @param name
     *            the name of the files in which the model will be written (date tag and extensions are added
     *            automatically)
     * @throws IOException
     */
    private void writePlain(GRBModel model, String name) throws IOException {
        File lockFile = new File(String.format("%s/writing.lock", mPath));
        lockFile.createNewFile();

        try {
            String fileName = String.format("%s/%s_%s", mPath, name, mDate);

            File mstFile = null;
            try {
                mstFile = GRBMSTWriter.writeMST(model, fileName);
            } catch (GRBException e) {
                LoggerHelper.getLogger(getClass()).exception("GRBModelWriter.writePlain (file: %s)", e,
                        fileName + ".mst");
            }

            if (mstFile != null) {
                try {
                    model.write(fileName + ".mps.bz2");
                } catch (GRBException e) {
                    LoggerHelper.getLogger(getClass()).exception("GRBModelWriter.writePlain (file: %s)", e,
                            fileName + ".mps.bz2");
                    mstFile.delete();
                }
            }
        } catch (IOException e) {
            lockFile.delete();
            throw e;
        } finally {
            lockFile.delete();
        }
    }

    /**
     * Write a model to two files (.mst and .mps.bz2) and add them to the compressed archive
     * 
     * @param model
     *            the model to be written
     * @param name
     *            the name of the files in which the model will be written (date tag and extensions are added
     *            automatically)
     * @throws IOException
     */
    private void writeToZIP(GRBModel model, String name) throws IOException {
        byte data[] = new byte[BUFFER];
        if (mZipFile.exists())
            mZipFile.renameTo(mZipTmpFile);

        mZipFile.createNewFile();

        ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(mZipFile)));
        zos.setMethod(ZipOutputStream.DEFLATED);

        if (mZipTmpFile.exists()) {
            // Copy the existing file
            synchronized (zos) {
                ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(mZipTmpFile)));
                ZipEntry entry = zis.getNextEntry();
                while (entry != null) {
                    zos.putNextEntry(entry);
                    int count = zis.read(data, 0, BUFFER);
                    while (count != -1) {
                        zos.write(data, 0, count);
                        count = zis.read(data, 0, BUFFER);
                    }
                    zos.closeEntry();
                    entry = zis.getNextEntry();
                }
            }
            mZipTmpFile.delete();
        }

        // Write the model in two files
        String fileName = String.format("%s_%s", name, mDate);
        File mpsFile = new File(fileName + ".mps.bz2");

        File mstFile = null;
        try {
            mstFile = GRBMSTWriter.writeMST(model, fileName);
        } catch (GRBException e) {
            LoggerHelper.getLogger(getClass()).exception("GRBModelWriter.writeToZOP (file: %s)", e, mstFile);
            mstFile = null;
        }
        try {
            model.write(mpsFile.getAbsolutePath());
        } catch (GRBException e) {
            LoggerHelper.getLogger(getClass()).exception("GRBModelWriter.writeToZOP (file: %s)", e, mpsFile);
            mpsFile = null;
        }

        File[] files = mpsFile != null && mstFile != null ? new File[] { mstFile, mpsFile } : new File[] {};

        for (File file : files) {
            synchronized (zos) {
                // Compress files
                FileInputStream fi = new FileInputStream(file);
                BufferedInputStream origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(file.getName());
                zos.putNextEntry(entry);
                int count = origin.read(data, 0, BUFFER);
                while (count != -1) {
                    zos.write(data, 0, count);
                    count = origin.read(data, 0, BUFFER);
                }
                origin.close();
                zos.closeEntry();
            }
            file.delete();
        }

        // Write
        zos.flush();
        zos.close();
    }

    /**
     * Returns the path to the zip file
     * 
     * @return the path to the zip file
     */
    public String getZipPath() {
        return mZipFile.getPath();
    }

}
