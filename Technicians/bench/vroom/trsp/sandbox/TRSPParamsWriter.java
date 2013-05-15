/**
 * 
 */
package vroom.trsp.sandbox;

import static vroom.trsp.util.TRSPGlobalParameters.RUN_SEEDS;

import java.io.File;
import java.io.IOException;

import vroom.trsp.util.TRSPGlobalParameters;

/**
 * <code>TRSPParamsWriter</code> is used to write a set of parameters to a file
 * <p>
 * Creation date: Jun 28, 2011 - 1:16:34 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TRSPParamsWriter {

    /**
     * @param args
     */
    public static void main(String[] args) {
        String file = "defaults_10k.ini";
        TRSPGlobalParameters params = new TRSPGlobalParameters();
        params.set(TRSPGlobalParameters.ALNS_MAX_IT, 10000);
        params.set(TRSPGlobalParameters.ALNS_MAX_TIME, Integer.MAX_VALUE);
        params.set(RUN_SEEDS, new long[] { 1, 2, 3, 4, 5, 6 });

        params.setDefaultValues();

        System.out.println("Writing params to file " + file);
        System.out.println(params);
        try {
            params.saveParameters(new File(file), false);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
