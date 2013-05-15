/**
 *
 */
package vroom.common.sandbox;

import vroom.common.utilities.Constants;

/**
 * <p>
 * Creation date: Jun 3, 2010 - 1:23:39 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class SandBox {

    /**
     * Main method that run a test
     * 
     * @param args
     */
    public static void main(String[] args) {
        // double trois = 3.000000000000001d;
        // System.out.println(trois);
        // System.out.println(trois == 3);
        // System.out.println(trois <= 3);
        // System.out.println(Double.compare(trois, 3));
        // Double bks = getBKS();
        // System.out.println(bks);
        System.out.println(Constants.equals(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
        System.out.println(Constants.equals(Double.NaN, Double.NaN));
        System.out.println(Constants.equals(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY));
        System.out.println(Constants.equals(0.0000000001, 0d));
    }

    public static Double getBKS() {
        return null;
    }

}
