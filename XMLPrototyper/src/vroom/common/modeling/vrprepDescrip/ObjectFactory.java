//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.2.5-2 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2012.07.13 à 02:34:53 PM CEST 
//


package vroom.common.modeling.vrprepDescrip;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the vroom.common.modeling.vrprepDescrip package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: vroom.common.modeling.vrprepDescrip
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Description }
     * 
     */
    public Description createDescription() {
        return new Description();
    }

    /**
     * Create an instance of {@link Description.TypeDefinitions }
     * 
     */
    public Description.TypeDefinitions createDescriptionTypeDefinitions() {
        return new Description.TypeDefinitions();
    }

    /**
     * Create an instance of {@link Type }
     * 
     */
    public Type createType() {
        return new Type();
    }

    /**
     * Create an instance of {@link Description.TypeDefinitions.Node }
     * 
     */
    public Description.TypeDefinitions.Node createDescriptionTypeDefinitionsNode() {
        return new Description.TypeDefinitions.Node();
    }

    /**
     * Create an instance of {@link Description.TypeDefinitions.Link }
     * 
     */
    public Description.TypeDefinitions.Link createDescriptionTypeDefinitionsLink() {
        return new Description.TypeDefinitions.Link();
    }

    /**
     * Create an instance of {@link Description.TypeDefinitions.Vehicle }
     * 
     */
    public Description.TypeDefinitions.Vehicle createDescriptionTypeDefinitionsVehicle() {
        return new Description.TypeDefinitions.Vehicle();
    }

    /**
     * Create an instance of {@link Description.TypeDefinitions.Request }
     * 
     */
    public Description.TypeDefinitions.Request createDescriptionTypeDefinitionsRequest() {
        return new Description.TypeDefinitions.Request();
    }

    /**
     * Create an instance of {@link Description.TypeDefinitions.Other }
     * 
     */
    public Description.TypeDefinitions.Other createDescriptionTypeDefinitionsOther() {
        return new Description.TypeDefinitions.Other();
    }

}
