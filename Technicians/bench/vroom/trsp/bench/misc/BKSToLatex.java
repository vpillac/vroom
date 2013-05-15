/**
 * 
 */
package vroom.trsp.bench.misc;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

import vroom.common.utilities.BestKnownSolutions;
import vroom.trsp.bench.CVRPUtilities;
import vroom.trsp.datamodel.costDelegates.TRSPCostDelegate;
import vroom.trsp.datamodel.costDelegates.TRSPWorkingTime;

/**
 * JAVADOC <code>BKSToLatex</code>
 * <p>
 * Creation date: 5 juil. 2012 - 12:25:07
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class BKSToLatex {

    public static void bksToLatex(String bksFile, String latexFile) throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(latexFile));
        BestKnownSolutions bks = new BestKnownSolutions(bksFile);
        for (String ins : bks.getInstanceNames()) {
            double obj = bks.getBKS(ins);
            String sol = bks.getSolution(ins);

        }
    }

    public static String convertInstance(String insName, double obj, String sol) {
        StringBuilder sb = new StringBuilder(sol.length() * 2);
        //
        // sb.append(String.format("\\subsection*{Instance Solomon %s}\n", insName);
        // sb.append("\\begin{tabularx}{\\textwidth}{lrX}\n");
        // sb.append("\\hline\n");
        // sb.append("\\textbf{Route}\t& \\textbf{Length}\t& \\textbf{Visit sequence}\\\\ \\hline\n");
        // int rid = 1;
        // sol = sol.substring(1, sol.length()-1);
        // TRSPSolution sol = CVRPUtilities.castSolution(instance, sol, )
        // // String[] solution = sol.split("|");
        // for (TRSPTour r : solution) {
        // if (r.length() > 0) {
        //
        // StringBuilder sb = new StringBuilder(r.length() * 4);
        // for (int node : r) {
        // if (!solution.getInstance().isDepot(node)) {
        // sb.append(node);
        // sb.append(" ");
        // }
        // }
        //
        // sb.append(String.format(("%s  \t& %." + DISTANCE_PRECISION + "f\t & %s\\\\ \n", rid,
        // r.getTotalCost(), sb.toString());
        // rid++;
        // }
        // }
        // sb.append(("\\hline");
        // sb.append(String.format(("\\textbf{Total} & \\textbf{%." + DISTANCE_PRECISION + "f} & ",
        // solution.getObjectiveValue());
        // sb.append(("\\\\");
        // sb.append(("\\end{tabularx}\n");
        return null;
    }

    public static String convertTour(String tour) {
        // String[] seq = tour.split(",");
        // StringBuffer s = new StringBuffer(seq.length*3);
        return tour.replaceAll("", " ");
    }

    /**
     * JAVADOC
     * 
     * @param args
     */
    public static void main(String[] args) {
        String bksFile = "../Instances/trsp/pillac/pillac-minwt.sol";
        TRSPCostDelegate cd = new TRSPWorkingTime();
        String instancePathFormat = "../Instances/trsp/pillac/crew25/%s.txt";
        // String outFile = "Pillac_Dissertation_ap_bks_trsp.tex";
        String outFile = "/home/vpillac/Documents/Dropbox/Doctorat/Documents/Dissertation/report/Pillac_Dissertation_ap_bks_trsp.tex";

        try {

            PrintStream out = new PrintStream(outFile);
            System.setOut(out);

            out.println("\\chapter{Best known solutions for the TRSP}\\label{ap:bks}");
            System.out.println("\\chaptermark{BKS for the TRSP}");

            System.out
                    .println("This appendix presents the value of the best known solutions for the TRSP instances introduced in this thesis.");
            // System.out.println("\\newpage");

            CVRPUtilities.sDistancePrecision = 3;
            CVRPUtilities.printBKS(instancePathFormat, bksFile, cd, false);

            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
