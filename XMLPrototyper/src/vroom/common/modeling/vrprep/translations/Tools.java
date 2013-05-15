package vroom.common.modeling.vrprep.translations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import vroom.common.modeling.vrprep.Instance;
import vroom.common.modeling.vrprep.Instance.Fleet;
import vroom.common.modeling.vrprep.Instance.Info;
import vroom.common.modeling.vrprep.Instance.Info.Contributor;
import vroom.common.modeling.vrprep.Instance.Network;
import vroom.common.modeling.vrprep.Instance.Requests;
import vroom.common.modeling.vrprep.ObjectFactory;
import vroom.common.modeling.vrprep.VRPRepJAXBUtilities;

/**
 * Parent class of all the XML translator classes<br />
 * Contains all the default variables and tools required by several translaotr classes
 * 
 * @author Maxim Hoskins
 */
public class Tools extends ObjectFactory {

    protected FileReader     fr = null;
    protected BufferedReader br = null;

    /**
     * A {@link Document} object that will be used to create {@link Element elements}
     */
    protected final Document document;
    /**
     * object factory instance element
     * 
     * @see vroom.common.modeling.vrprep.Instance
     */
    protected Instance       instance;
    /**
     * object factory instance.info element
     * 
     * @see vroom.common.modeling.vrprep.Instance.Info
     */
    protected Info           info;
    /**
     * object factory instance.requests element
     * 
     * @see vroom.common.modeling.vrprep.Instance.Requests
     */
    protected Requests       requests;
    /**
     * object factory instance.network element
     * 
     * @see vroom.common.modeling.vrprep.Instance.Network
     */
    protected Network        network;
    /**
     * object factory instance.fleet element
     * 
     * @see vroom.common.modeling.vrprep.Instance.Fleet
     */
    protected Fleet          fleet;

    /**
     * Constructor for the class Tools.java <br />
     * Initializes the document builder
     */
    public Tools() {
        DocumentBuilder builder = null;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            // No exception should be thrown, just in case:
            throw new IllegalStateException("Could not create a DocumentBuilder", e);
        }
        document = builder.newDocument();
    }

    /**
     * Initiate file reader objects<br />
     * 
     * @exception FileNotFoundException
     * @exception IOException
     * @param fileName
     *            name of file to load
     */
    protected void openFileReader(String fileName) {
        try {

            // close fileReader if open before opening a new file
            if (this.fr != null || this.br != null) {
                closeFileReader();
            }

            this.fr = new FileReader(fileName);
            this.br = new BufferedReader(fr);

        } catch (FileNotFoundException e) {
            System.out.println("File " + fileName + " could not be opened.");
        }

        instance = createInstance();
        info = createInstanceInfo();
        network = createInstanceNetwork();
        fleet = createInstanceFleet();
        requests = createInstanceRequests();
    }

    /**
     * Close file reader<br />
     * 
     * @exception IOException
     */
    protected void closeFileReader() {
        try {
            this.br.close();
            this.fr.close();
            this.br = null;
            this.fr = null;
        } catch (IOException e) {
            System.out.println("Could not close filewriter");
        }
    }

    /**
     * Finalize the XML JAXB objects and call the fill marshaller
     * 
     * @param file
     *            file to which the XML JAXB objects should be marshalled
     */
    protected void marshalFile(String file) {
        // add contributor
        Contributor c = createInstanceInfoContributor();
        c.setName("Maxim Hoskins");
        c.setEmail("maxim.hoskins@gmail.com");
        info.setContributor(c);

        instance.setInfo(info);
        instance.setNetwork(network);
        instance.setFleet(fleet);
        instance.setRequests(requests);

        File f = new File(file);

        try {
            f.getParentFile().mkdirs();
            VRPRepJAXBUtilities.writeInstance(instance, f, true);
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
