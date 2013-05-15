/**
 *
 */
package vroom.common.utilities.gurobi;

import gurobi.GRB;
import gurobi.GRB.DoubleAttr;
import gurobi.GRB.StringAttr;
import gurobi.GRBException;
import gurobi.GRBModel;
import gurobi.GRBVar;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * <code>GRBMSTWriter</code> is a utility class that will retrieve the {@link DoubleAttr#Start} values of all the
 * {@link GRBVar} of a {@link GRBModel} and write them to a file in the <a
 * href="http://www.gurobi.com/doc/45/refman/node592.html">MST</a> format
 * <p>
 * Creation date: Aug 18, 2011 - 9:50:01 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class GRBMSTWriter {

    /**
     * Write the {@link DoubleAttr#Start} attribute of all the variables of a {@link GRBModel model}
     * 
     * @param model
     *            the model from which the start values will be extracted
     * @param file
     *            the path of the destination file
     * @returns the written {@link File}
     * @throws IOException
     * @throws GRBException
     */
    public static File writeMST(GRBModel model, String file) throws IOException, GRBException {
        if (!file.endsWith(".mst"))
            file = file + ".mst";

        File mst = new File(file);
        if (!mst.exists())
            mst.createNewFile();

        BufferedWriter out = new BufferedWriter(new FileWriter(mst, false));

        GRBVar[] vars = model.getVars();
        double[] start = model.get(DoubleAttr.Start, vars);
        String[] names = model.get(StringAttr.VarName, vars);

        String modelName = null;
        try {
            modelName = model.get(StringAttr.ModelName);
        } catch (GRBException e) {
            // Ignore
        } finally {
            if (modelName == null)
                modelName = file;
        }
        out.write("# MIP start for ");
        out.write(modelName);
        out.newLine();

        for (int i = 0; i < start.length; i++) {
            if (start[i] != GRB.UNDEFINED) {
                out.append(names[i]);
                out.write("  ");
                out.write(start[i] + "");
                out.newLine();
            }
        }

        out.flush();
        out.close();

        return mst;
    }
}
