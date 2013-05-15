/**
 * 
 */
package vroom.common.modeling.vrprep;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * The class <code>VRPRepUtilities</code> contains utility methods to read/write instances in xml and compressed formats
 * <p>
 * Creation date: Jun 22, 2012 - 4:07:58 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class VRPRepJAXBUtilities {

    /**
     * Write an instance in a file
     * 
     * @param instance
     *            the instance to be written
     * @param destFile
     *            the destination file
     * @param compress
     *            {@code true} if the instance should be compressed, in which case the suffix {@code  .zip} will be added
     *            to the file name
     * @throws JAXBException
     * @throws IOException
     */
    public static void writeInstance(Instance instance, File destFile, boolean compress)
            throws JAXBException, IOException {
        JAXBContext context = JAXBContext.newInstance(Instance.class.getPackage().getName());

        Marshaller marshaller = context.createMarshaller();

        // Nicelly format the output XML
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, new Boolean(true));
        //marshaller.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, "http://vroom-project.net23.net/schemas/VRPRep.xsd");

        // Write the instance
        OutputStream os;
        if (compress) {
            String zipFile = destFile.getAbsolutePath().endsWith(".zip") ? destFile
                    .getAbsolutePath() : destFile.getAbsolutePath() + ".zip";
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
            ZipEntry ze = new ZipEntry(destFile.getName());
            zos.putNextEntry(ze);

            os = zos;
        } else {
            os = new FileOutputStream(destFile);
        }

        marshaller.marshal(instance, os);

        os.flush();
        os.close();
    }

    /**
     * Read an instance from a file.
     * <p>
     * Note that this method will detect automatically if the file is compressed
     * </p>
     * 
     * @param file
     * @return the instance contained in {@code  file}
     * @throws JAXBException
     * @throws IOException
     */
    public static Instance readInstance(File file) throws JAXBException, IOException {
        JAXBContext context = JAXBContext.newInstance(Instance.class.getPackage().getName());

        Unmarshaller unmarshaller = context.createUnmarshaller();

        InputStream is;
        if (file.getName().endsWith(".zip")) {
            ZipInputStream zis = new ZipInputStream(new FileInputStream(file));
            zis.getNextEntry();
            is = zis;
        } else {
            is = new FileInputStream(file);
        }

        Instance instance = (Instance) unmarshaller.unmarshal(is);

        is.close();

        return instance;
    }
}
