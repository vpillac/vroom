/**
 * 
 */
package vroom.trsp.bench.misc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JOptionPane;

import vroom.common.utilities.BestKnownSolutions;

/**
 * The class <code>BKSExtractor</code> is used to extract BKS information from a cvs result file
 * <p>
 * Creation date: Oct 17, 2011 - 11:23:05 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class BKSExtractor {

    public static String             sAuthors = "Pillac_etal.";
    // public static final String BKS_FILE = "../Instances/cvrptw/solomon-100.sol";

    private final BestKnownSolutions mBKS;

    public BKSExtractor(String bksFile) {
        mBKS = new BestKnownSolutions(bksFile);
    }

    public int readStatFile(String statFile, String authors, boolean fixOldFormat)
            throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(statFile));

        String line = reader.readLine();
        String[] labels = line.split(";");
        // Skip the comments and header
        while (line != null && (labels.length < 2 || labels[1].isEmpty())) {
            line = reader.readLine();
            labels = line != null ? line.split(";") : null;
        }

        if (line == null)
            throw new IllegalStateException("Unable to find data in file " + statFile);

        // Find the checksol and solution col index
        labels = line.split(";");
        int nameColIdx = -1;
        int checkColIdx = -1;
        int valColIdx = -1;
        int solColIdx = -1;
        for (int i = 0; i < labels.length; i++) {
            if (labels[i].equals("name"))
                nameColIdx = i;
            else if (labels[i].equals("postop_checksol"))
                checkColIdx = i;
            else if (labels[i].equals("postop_wt") || labels[i].equals("postop_dist"))
                valColIdx = i;
            else if (labels[i].equals("final_sol"))
                solColIdx = i;
        }

        if (checkColIdx < 0)
            System.out.println("Warning: checksol column detected");
        if (valColIdx < 0) {
            System.out.println("Error  : could not find solution column");
            return 0;
        }

        // Skip the header line
        line = reader.readLine();
        int newBKS = 0;
        while (line != null) {
            String[] stats = line.split(";");
            if (checkColIdx < 0 || "".equals(stats[checkColIdx])) {
                double sol = Double.valueOf(stats[valColIdx]);
                String insName = stats[nameColIdx].replaceAll("\"", "");
                Double bks = mBKS.getBKS(insName);
                if (bks == null || sol < bks) {
                    newBKS++;
                    System.out.printf("New BKS for instance %s (old:%.3f new:%.3f) %s\n", insName,
                            bks != null ? bks : Double.NaN, sol, stats[solColIdx]);
                    String solString = stats[solColIdx];
                    if (fixOldFormat)
                        solString = fixOldSolution(solString);
                    mBKS.setBestKnownSolution(insName, sol, false,
                            findFleetSize(stats[solColIdx], 2), authors, solString);
                }
            }
            line = reader.readLine();
        }
        return newBKS;
    }

    public String fixOldSolution(String sol) {
        for (int i = 127; i <= 176; i++)
            sol = sol.replaceAll("" + i, "" + (i - 1));
        return sol;
    }

    /**
     * Returns the number of non empty routes in {@code  sol}
     * 
     * @param sol
     * @param minRouteLength
     *            the minimum length of a route, shorter routes will be ignored
     * @return the number of non empty routes in {@code  sol}
     */
    public static int findFleetSize(String sol, int minRouteLength) {
        sol = sol.substring(1, sol.length() - 1);
        String[] routes = sol.split("\\|+");
        int size = 0;
        for (String r : routes)
            if (r.split(",").length > minRouteLength)
                size++;
        return size;
    }

    /**
     * Save changes to the BKS file
     * 
     * @param comment
     * @throws IOException
     */
    public void saveBKS(String comment) throws IOException {
        mBKS.save(comment);
    }

    /**
     * JAVADOC
     * 
     * @param args
     */
    public static void main(String[] args) {

        // String statFile = "./tmp/trsp_bench_120314_09-56_TRSP_pALNSSC_25crew_divPool_noNoise.csv";
        // String statFile = "./results/bks/Pillac_TRSP_results_110922_CVRP.csv";
        String statFile = "/media/data/Documents/Dropbox/Doctorat/ResultsBench/TRSP OPTL/new/trsp_bench_120729_00-40_TRSP_pALNSSC_optParam.csv";
        boolean fix = false;
        String bksPath = "../Instances/trsp/pillac/pillac-minwt.sol";

        BKSExtractor bks = new BKSExtractor(bksPath);
        try {
            int newBKS = bks.readStatFile(statFile, sAuthors, fix);

            if (JOptionPane.showConfirmDialog(null, "Save " + newBKS + " new BKS to file "
                    + bksPath, "Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                File bksFile = new File(bksPath);
                bksFile.renameTo(new File(bksPath + ".bck"));
                bks.saveBKS(statFile);
            } else {
                System.out.println("Changes not saved");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // System.out.println(bks.mBKS.toCSVString(1));

    }

}
