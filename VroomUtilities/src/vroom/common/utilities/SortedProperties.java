package vroom.common.utilities;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

/**
 * The class <code>SortedProperties</code> extends {@link Properties} by overriding the {@link #keys()} method which
 * returns a sorted enumeration of keys
 * <p>
 * Creation date: Dec 15, 2011 - 1:49:49 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class SortedProperties extends Properties {

    private static final long serialVersionUID = 8811159451646269896L;

    /**
     * Overrides, called by the store method.
     */
    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public synchronized Enumeration keys() {
        Enumeration keysEnum = super.keys();
        Vector keyList = new Vector();
        while (keysEnum.hasMoreElements()) {
            keyList.add(keysEnum.nextElement());
        }
        Collections.sort(keyList);
        return keyList.elements();
    }
}