/**
 *
 */
package vroom.trsp.bench;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import vroom.common.modeling.dataModel.ListRoute.ArrayListRoute;
import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.dataModel.Solution;
import vroom.common.utilities.BestKnownSolutions;
import vroom.trsp.datamodel.CVRPTWSolutionChecker;
import vroom.trsp.datamodel.ITRSPNode.NodeType;
import vroom.trsp.datamodel.TRSPDetailedSolutionChecker;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.datamodel.TRSPSolutionCheckerBase;
import vroom.trsp.datamodel.TRSPTour;
import vroom.trsp.datamodel.Technician;
import vroom.trsp.datamodel.costDelegates.TRSPCostDelegate;
import vroom.trsp.datamodel.costDelegates.TRSPDistance;
import vroom.trsp.datamodel.costDelegates.TRSPWorkingTime;
import vroom.trsp.io.PillacSimplePersistenceHelper;
import vroom.trsp.optimization.TRSPUtilities;

/**
 * <code>CVRPSolutionChecker</code>
 * <p>
 * Creation date: Sep 6, 2011 - 4:13:28 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class CVRPUtilities {

    public static int sDistancePrecision = 1;

    public static int[] readString(String string, String instance) throws IOException {
        PillacSimplePersistenceHelper reader = new PillacSimplePersistenceHelper();
        TRSPInstance ins = reader.readInstance(new File(instance), true);

        return readString(string, ins);
    }

    public static int[] readString(String string, TRSPInstance ins) throws IOException {
        ArrayList<Integer> sol = new ArrayList<Integer>(string.length() / 3);

        // Remove delimiters
        string = string.substring(1, string.length() - 2);

        // Split string into nodes
        String[] nodes = string.split("\\p{Punct}");
        for (String node : nodes) {
            Integer n = Integer.valueOf(node);
            if (ins.isRequest(n)) {
                sol.add(n - ins.getDepotCount() + 1);
            } else if (sol.isEmpty() || sol.get(sol.size() - 1) != 0) {
                sol.add(0);
            }
        }

        // Copy solution to array
        int[] solarray = new int[sol.size()];
        for (int i = 0; i < sol.size(); i++) {
            solarray[i] = sol.get(i);
        }
        return solarray;
    }

    public static ArrayList<Integer> castLongString(String string) {
        string = string.substring(string.indexOf("Tours [") + 7, string.lastIndexOf(']'));
        String[] tours = string.split("t:");
        ArrayList<Integer> nodesInt = new ArrayList<Integer>(100);
        for (String t : tours) {
            if (t.length() == 0)
                continue;
            t = t.substring(t.indexOf('<') + 1, t.indexOf('>'));
            String[] nodes = t.split("\\p{Punct}");

            for (int i = 0; i < nodes.length; i++) {
                if (!nodes[i].isEmpty())
                    nodesInt.add(Integer.valueOf(nodes[i]));
            }
        }

        return nodesInt;
    }

    public static int[][] castLongStringToArray(String string) {
        string = string.substring(string.indexOf("Tours [") + 7, string.lastIndexOf(']'));
        String[] tours = string.split("t:");
        ArrayList<int[]> toursList = new ArrayList<int[]>(25);
        for (String t : tours) {
            if (t.length() == 0)
                continue;
            t = t.substring(t.indexOf('<') + 1, t.indexOf('>'));
            String[] nodes = t.split("\\p{Punct}");

            int[] tour = new int[nodes.length - 2];
            toursList.add(tour);
            for (int i = 1; i < nodes.length - 1; i++) {
                if (!nodes[i].isEmpty())
                    tour[i - 1] = Integer.valueOf(nodes[i]);
            }
        }

        int[][] solution = new int[toursList.size()][];
        for (int i = 0; i < solution.length; i++) {
            solution[i] = toursList.get(i);
        }

        return solution;
    }

    public static ArrayList<Integer> castString(String string) {
        String[] nodes = string.split("\\p{Punct}");

        ArrayList<Integer> nodesInt = new ArrayList<Integer>(nodes.length);
        for (int i = 0; i < nodes.length; i++) {
            if (!nodes[i].isEmpty())
                nodesInt.add(Integer.valueOf(nodes[i]));
        }

        return nodesInt;
    }

    public static List<TRSPSolution> readNewBks(String cvsFile, String instancePathFormat)
            throws IOException {

        LinkedList<TRSPSolution> newBKS = new LinkedList<>();

        BufferedReader r = new BufferedReader(new FileReader(cvsFile));

        String line = r.readLine();
        while (!line.startsWith("name"))
            line = r.readLine();

        int finalGap_col = -1, sol_col = -1;
        String data[] = line.split(";");
        for (int i = 0; i < data.length; i++) {
            if (data[i].equals("postop_gap"))
                finalGap_col = i;
            else if (data[i].equals("final_sol"))
                sol_col = i;
        }

        line = r.readLine();
        while (line != null) {

            data = line.split(";");
            String currentInstance = data[0];
            double bestGap = Double.valueOf(data[finalGap_col]);
            String bestSol = data[sol_col];
            while (line != null && line.startsWith(currentInstance)) {
                data = line.split(";");
                if (Double.valueOf(data[finalGap_col]) < bestGap) {
                    bestGap = Double.valueOf(data[finalGap_col]);
                    bestSol = data[sol_col];
                }
                line = r.readLine();
            }
            if (bestGap < 0) {
                // System.out.printf("%s : %.4f - %s\n", currentInstance, bestGap, bestSol);

                TRSPInstance instance = TRSPUtilities.readInstance(
                        String.format(instancePathFormat, currentInstance), true);

                // Read the instance
                newBKS.add(castSolution(instance, bestSol, new TRSPDistance()));
            }
            // line = r.readLine();
        }

        r.close();

        return newBKS;

    }

    public static void printNewBKS(String cvsFile, String instancePathFormat, String bksFile) {
        List<TRSPSolution> newBKS = null;
        try {
            newBKS = readNewBks(cvsFile, instancePathFormat);
        } catch (IOException e2) {
            e2.printStackTrace();
            return;
        }

        BestKnownSolutions bks = new BestKnownSolutions(bksFile);
        // Print a summary array
        System.out.println("\\subsection*{Summary of new BKS}");
        System.out.println("\\begin{tabular}{lrrr}");
        System.out.println("\\hline");
        System.out
                .println(" \\textbf{Instance} & \\textbf{Previous BKS} & \\textbf{New BKS} & \\textbf{Improvement} \\\\");
        System.out.println("\\hline");
        for (TRSPSolution s : newBKS) {
            s.getCostDelegate().evaluateSolution(s, true, true);
            double dist = TRSPSolutionCheckerBase.evaluateTotalEuclidianDistance(s, 1);
            double oldBKS = bks.getBKS(s.getInstance().getName());
            System.out.printf("%s & %.1f & %.1f & %.2f\\%% \\\\ \n", s.getInstance().getName(),
                    oldBKS, s.getObjectiveValue(), (oldBKS - s.getObjectiveValue()) / oldBKS * 100);
        }
        System.out.println("\\hline");
        System.out.println("\\end{tabular}");

        System.out.println();
        for (TRSPSolution s : newBKS) {
            printsol(s);
        }
    }

    public static void printBKS(String instancePathFormat, String bksFile,
            TRSPCostDelegate costDelegate, boolean cvrptw) {
        BestKnownSolutions bks = new BestKnownSolutions(bksFile);

        List<TRSPSolution> solutions = new ArrayList<>(100);
        for (String instance : bks.getInstanceNames()) {
            TRSPInstance trspIns = TRSPUtilities.readInstance(
                    String.format(instancePathFormat, instance), cvrptw);
            solutions.add(castSolution(trspIns, bks.getSolution(instance), costDelegate));
        }

        TRSPWorkingTime wtCD = new TRSPWorkingTime();
        TRSPDistance distCD = new TRSPDistance();
        // Print a summary array
        System.out.println("\\section{Summary of best known solutions}");
        System.out
                .println("Tables \\ref{tab:ap-bks-C}, \\ref{tab:ap-bks-R}, and \\ref{tab:ap-bks-RC} present a summary of the best known solutions for instances from group C, R, and RC respectively. Note that the objective function only considers the minimization of the duration, the distance is reported for reference only.");
        System.out.println();
        String[] prefixes = { "C", "R", "RC" };
        for (String pref : prefixes) {
            System.out.println("\\begin{table}[h!]");
            System.out.println("\\centering");
            System.out.println("\\footnotesize");
            System.out.println("\\begin{tabular}{lrr}");
            System.out.println("\\hline");
            System.out
                    .println(" \\textbf{Instance} & \\textbf{Duration} & \\textbf{Distance} \\\\");
            System.out.println("\\hline");
            for (TRSPSolution s : solutions) {
                if (!s.getInstance().getName().startsWith(pref + "1")
                        && !s.getInstance().getName().startsWith(pref + "2"))
                    continue;
                s.getCostDelegate().evaluateSolution(s, true, true);
                System.out.printf("%s & %12." + sDistancePrecision + "f & %12."
                        + sDistancePrecision + "f \\\\ \n",
                        s.getInstance().getName().replace("_", "\\_"),
                        wtCD.evaluateSolution(s, true, false),
                        distCD.evaluateSolution(s, true, false));
            }
            System.out.println("\\hline");
            System.out.println("\\end{tabular}");
            System.out.printf(
                    "\\caption{Best known solutions for group %s} \\label{tab:ap-bks-%s}\n", pref,
                    pref);
            System.out.println("\\end{table}");
            System.out.println();
        }

        System.out.println();
        System.out.println("\\section{Detailed solutions}");
        for (TRSPSolution s : solutions) {
            printsol(s);
        }
    }

    public static Solution<ArrayListRoute> castSolution(int[] solArray, IVRPInstance instance) {
        Solution<ArrayListRoute> sol = new Solution<ArrayListRoute>(instance);

        int i = 0;
        while (i < solArray.length) {
            ArrayListRoute route = new ArrayListRoute(sol, instance.getFleet().getVehicle());
            INodeVisit node = instance.getNodeVisit(solArray[i]);
            if (node.isDepot() && i == solArray.length - 1)
                break;
            route.appendNode(node);
            sol.addRoute(route);
            boolean first = true;
            i++;
            while (i < solArray.length) {
                node = instance.getNodeVisit(solArray[i]);
                route.appendNode(node);
                if (node.isDepot())
                    break;
                else
                    i++;
            }
        }

        return sol;
    }

    public static TRSPSolution castSolution(TRSPInstance instance, String solution,
            TRSPCostDelegate costDelegate) {
        ArrayList<Integer> nodes = castString(solution);

        TRSPSolution sol = new TRSPSolution(instance, costDelegate);
        TRSPTour currentTour = null;
        for (int i : nodes) {
            if (instance.getTRSPNode(i).getType() == NodeType.HOME) {
                for (Technician t : instance.getFleet()) {
                    if (t.getHome().getID() == i) {
                        // [12/08/14] bugfix for invalid solution formats
                        if (currentTour != null) {
                            int lastHome = instance.getHomeDuplicate(currentTour.getTechnician()
                                    .getHome().getID());
                            if (currentTour.getLastNode() != lastHome) {
                                currentTour.appendNode(lastHome);
                                sol.markAsServed(lastHome);
                            }
                        }

                        currentTour = sol.getTour(t.getID());
                        break;
                    }
                }
            }
            if (instance.isMainDepot(i))
                currentTour.appendNode(currentTour.getMainDepotId());
            else
                currentTour.appendNode(i);
            sol.markAsServed(i);
        }
        // [12/08/14] bugfix for invalid solution formats (last tour)
        if (currentTour != null) {
            int lastHome = instance.getHomeDuplicate(currentTour.getTechnician().getHome().getID());
            if (currentTour.getLastNode() != lastHome) {
                currentTour.appendNode(lastHome);
                sol.markAsServed(lastHome);
            }
        }

        return sol;
    }

    public static TRSPSolution readSolution(TRSPInstance instance, String statFile,
            TRSPCostDelegate costDelegate) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(statFile));

        String line = in.readLine();
        while (line != null && !line.startsWith("name;"))
            line = in.readLine();
        String[] stats = line.split(";");
        int solCol = 0;
        while (!stats[solCol].equals("final_sol"))
            solCol++;

        while (line != null && !line.startsWith(instance.getName()))
            line = in.readLine();

        TRSPSolution best = null;
        while (line != null && line.startsWith(instance.getName())) {
            TRSPSolution sol = castSolution(instance, line.split(";")[solCol], costDelegate);
            if (best == null || sol.getObjectiveValue() < best.getObjectiveValue())
                best = sol;
            line = in.readLine();
        }
        in.close();
        return best;
    }

    private static void printsol(Solution<ArrayListRoute> solution) {
        System.out.printf("\\subsection*{Instance Solomon %s}\n", solution.getParentInstance()
                .getName());
        System.out.println("\\begin{tabularx}{\\textwidth}{lrX}");
        System.out.println("\\hline");
        System.out
                .printf("\\textbf{Route}\t& \\textbf{Length}\t& \\textbf{Visit sequence}\\\\ \\hline\n");
        int rid = 1;
        for (ArrayListRoute r : solution) {
            StringBuilder sb = new StringBuilder(r.length() * 4);
            for (INodeVisit node : r) {
                if (!node.isDepot()) {
                    sb.append(node.getID());
                    sb.append(" ");
                }
            }

            System.out.printf("%s  \t& %." + sDistancePrecision + "f\t & %s\\\\ \n", rid,
                    r.getCost(), sb.toString());
            rid++;
        }
        System.out.println("\\hline");
        System.out.printf("\\textbf{Total} & \\textbf{%." + sDistancePrecision + "f} & ",
                solution.getCost());
        System.out.println("\\\\");
        System.out.println("\\end{tabularx}\n");
    }

    private static void printsol(TRSPSolution solution) {
        System.out.printf("\\subsection*{Instance %s}\n\n", solution.getInstance().getName()
                .replace("_", "\\_"));
        System.out.println("\\begin{footnotesize}");
        System.out.println("\\begin{singlespace}");
        System.out.println("\\begin{tabularx}{\\textwidth}{lrrX}");
        System.out.println("\\hline");
        System.out
                .printf("\\textbf{Tech.}\t& \\textbf{Duration}\t& \\textbf{Distance}\t& \\textbf{Tour}\\\\ \\hline\n");
        int rid = 1;

        TRSPWorkingTime wtCD = new TRSPWorkingTime();
        TRSPDistance distCD = new TRSPDistance();
        double totalWT = 0;
        double totalDist = 0;
        for (TRSPTour r : solution) {
            // if (r.length() > 0) {

            StringBuilder sb = new StringBuilder(r.length() * 4);
            for (int node : r) {
                if (!solution.getInstance().isDepot(node)) {
                    sb.append(node);
                    sb.append(" ");
                }
            }

            double wt = wtCD.evaluateTour(r, false);
            double dist = distCD.evaluateTour(r, false);

            System.out.printf("%4s & %12." + sDistancePrecision + "f\t& %12." + sDistancePrecision
                    + "f\t & %s\\\\ \n", r.getTechnicianId(), wt, dist, sb.toString());
            totalDist += dist;
            totalWT += wt;
            rid++;
            // }
        }
        System.out.println("\\hline");
        System.out.printf("\\textbf{Total} & \\textbf{%12." + sDistancePrecision
                + "f} & \\textbf{%12." + sDistancePrecision + "f}  ", totalWT, totalDist);
        System.out.println("\\\\");
        System.out.println("\\end{tabularx}");
        System.out.println("\\end{singlespace}");
        System.out.println("\\end{footnotesize}");
        System.out.println();
    }

    public static void main(String[] args) {
        String statFile = "./results/bks/Pillac_TRSP_results_110922_CVRP.csv";
        // String statFile = "./results/bks/Pillac_TRSP_results_Solomon_120303.csv";
        String insFolder = "../Instances/cvrptw/solomon/%s.txt";
        String bksFile = "../Instances/cvrptw/solomon-100.sol";
        // printNewBKS(
        // "./results/trsp-optl/trsp_bench_120303_15-26_CVRPTW_pALNSSC_divPool_noNoise.csv",
        // "../Instances/cvrptw/solomon/%s.txt", "../Instances/cvrptw/solomon-100.sol");
        // printNewBKS(statFile, insFolder, bksFile);
        testDistance();

    }

    public static void testDistance() {

        // int[][] sol = {
        // { 42, 92, 45, 46, 36, 64, 11, 62, 88, 30, 20, 65, 71, 9, 81, 34, 78, 79, 3, 76, 28,
        // 53, 40, 2, 87, 57, 41, 22, 73, 21, 72, 74, 75, 56, 4, 25, 55, 54, 80, 68,
        // 77, 12, 26, 58, 13, 97, 37, 100, 98, 93, 59, 95, 94 },
        // { 27, 1, 69, 50, 33, 29, 24, 39, 67, 23, 15, 43, 14, 44, 38, 86, 16, 61, 91, 85,
        // 99, 96, 6, 84, 8, 82, 7, 48, 47, 49, 19, 10, 63, 90, 32, 66, 35, 51, 70,
        // 31, 52, 18, 83, 17, 5, 60, 89 } };
        // TRSPInstance ins = TRSPUtilities.readInstance("../Instances/cvrptw/solomon/R207.txt", true);
        // String cost = "890.61";

        // int[][] sol =
        // castLongStringToArray("Cost:827.30, Unserved:[] Tours [t:0 c:97.00 d:907.00 l:11 <1,57,58,56,60,62,63,64,61,59,127>,t:1 c:0.00 d:0.00 l:2 <2,128>,t:2 c:0.00 d:0.00 l:2 <3,129>,t:3 c:0.00 d:0.00 l:2 <4,130>,t:4 c:75.90 d:975.90 l:12 <5,115,112,111,108,107,109,110,113,114,116,131>,t:5 c:0.00 d:0.00 l:2 <6,132>,t:6 c:0.00 d:0.00 l:2 <7,133>,t:7 c:101.70 d:821.70 l:10 <8,82,80,79,78,81,83,85,84,134>,t:8 c:0.00 d:0.00 l:2 <9,135>,t:9 c:0.00 d:0.00 l:2 <10,136>,t:10 c:50.70 d:1040.70 l:13 <11,45,49,50,52,54,55,53,51,48,47,46,137>,t:11 c:0.00 d:0.00 l:2 <12,138>,t:12 c:95.80 d:815.80 l:10 <13,38,42,43,44,40,41,39,37,139>,t:13 c:0.00 d:0.00 l:2 <14,140>,t:14 c:127.10 d:937.10 l:11 <15,106,103,101,96,95,98,102,104,105,141>,t:15 c:59.30 d:1049.30 l:13 <16,92,90,88,87,99,97,86,89,93,91,94,142>,t:16 c:0.00 d:0.00 l:2 <17,143>,t:17 c:59.40 d:1139.40 l:14 <18,30,28,32,33,35,36,34,31,29,27,26,100,144>,t:18 c:0.00 d:0.00 l:2 <19,145>,t:19 c:95.80 d:905.80 l:11 <20,123,121,120,119,117,118,122,125,124,146>,t:20 c:0.00 d:0.00 l:2 <21,147>,t:21 c:64.60 d:1234.60 l:15 <22,68,67,66,65,69,71,70,73,76,75,77,74,72,148>,t:22 c:0.00 d:0.00 l:2 <23,149>,t:23 c:0.00 d:0.00 l:2 <24,150>,t:24 c:0.00 d:0.00 l:2 <25,151>]");
        int[][] sol = castLongStringToArray("Cost:825.90, Unserved:[] Tours [t:0 c:0.00 d:0.00 l:2 <1,126>,t:1 c:0.00 d:0.00 l:2 <2,127>,t:2 c:0.00 d:0.00 l:2 <3,128>,t:3 c:0.00 d:0.00 l:2 <4,129>,t:4 c:0.00 d:0.00 l:2 <5,130>,t:5 c:75.90 d:975.90 l:12 <6,115,112,111,108,107,109,110,113,114,116,131>,t:6 c:0.00 d:0.00 l:2 <7,132>,t:7 c:0.00 d:0.00 l:2 <8,133>,t:8 c:64.60 d:1234.60 l:15 <9,68,67,66,65,69,71,70,73,76,75,77,74,72,134>,t:9 c:101.70 d:821.70 l:10 <10,82,80,79,78,81,83,85,84,135>,t:10 c:0.00 d:0.00 l:2 <11,136>,t:11 c:50.70 d:1040.70 l:13 <12,45,49,50,52,54,55,53,51,48,47,46,137>,t:12 c:56.80 d:1046.80 l:13 <13,30,28,32,33,35,36,34,31,29,26,100,138>,t:13 c:0.00 d:0.00 l:2 <14,139>,t:14 c:97.00 d:907.00 l:11 <15,57,58,56,60,62,63,64,61,59,140>,t:15 c:0.00 d:0.00 l:2 <16,141>,t:16 c:0.00 d:0.00 l:2 <17,142>,t:17 c:0.00 d:0.00 l:2 <18,143>,t:18 c:0.00 d:0.00 l:2 <19,144>,t:19 c:127.10 d:937.10 l:11 <20,106,103,101,96,95,98,102,104,105,145>,t:20 c:59.30 d:1049.30 l:13 <21,92,90,88,87,99,97,86,89,93,91,94,146>,t:21 c:95.80 d:815.80 l:10 <22,38,42,43,44,40,41,39,37,147>,t:22 c:97.00 d:957.70 l:12 <23,123,121,120,119,117,118,122,125,124,27,148>,t:23 c:0.00 d:0.00 l:2 <24,149>,t:24 c:0.00 d:0.00 l:2 <25,150>]");

        TRSPInstance ins = TRSPUtilities.readInstance("../Instances/cvrptw/solomon/C101.txt", true);
        String cost = "827.3";

        // System.out.println("===================================");
        // for (int n = 0; n < ins.getMaxId(); n++) {
        // ITRSPNode node = ins.getTRSPNode(n);
        // System.out.printf("%5s %5.0f %5.0f\n", node.getID(), node.getNode().getLocation()
        // .getX(), node.getNode().getLocation().getY());
        // }
        // System.out.println("===================================");
        // for (int n = 0; n < ins.getMaxId(); n++) {
        // if (n < 25 || n > 126)
        // continue;
        // ITRSPNode node = ins.getTRSPNode(n);
        // System.out.printf("%5s %5s %5.0f %5.0f\n", ins.getOriginalId(node.getID()),
        // node.getID() - 25, node.getNode().getLocation().getX(), node.getNode()
        // .getLocation().getY());
        // }
        // System.out.println("===================================");
        // for (int n = 0; n < ins.getMaxId(); n++) {
        // // if (n < 25 || n > 125)
        // // continue;
        // ILocation pred = ins.getTRSPNode(n).getNode().getLocation();
        // for (int m = 0; m < ins.getMaxId(); m++) {
        // ILocation node = ins.getTRSPNode(m).getNode().getLocation();
        // double dist = Math.sqrt(Math.pow(pred.getX() - node.getX(), 2)
        // + Math.pow(pred.getY() - node.getY(), 2));
        //
        // System.out.printf("%3s %3s %5.3f %5.3f\n", n, m, dist, ins.getCostDelegate()
        // .getDistance(n, m));
        // }
        // }

        TRSPSolution s = new TRSPSolution(ins, new TRSPDistance());
        for (int i = 0; i < sol.length; i++) {
            s.getTour(i).appendNode(i + 1);
            for (int j = 0; j < sol[i].length; j++) {
                int n = sol[i][j];// + ins.getDepotCount() - 1;
                s.getTour(i).appendNode(n);
                s.markAsServed(n);
            }
            s.getTour(i).appendNode(ins.getHomeDuplicate(i + 1));
        }

        System.out.println(s);
        System.out.println(TRSPSolutionCheckerBase.evaluateTotalEuclidianDistance(s, 1));
        System.out.println("Expected cost: " + cost);
        System.out.println("Checksol: " + TRSPDetailedSolutionChecker.INSTANCE.checkSolution(s));
        System.out.println("Checksol: " + CVRPTWSolutionChecker.INSTANCE.checkSolution(s));
        System.out.printf("27: %s - %s@%s\n", s.getVisitingTour(27).getTechnicianId(), s
                .getVisitingTour(27).getEarliestArrivalTime(27), s.getInstance().getTimeWindow(27));

        // System.out.print("    ");
        // for (int j = 0; j < s.getInstance().getMaxId(); j++) {
        // System.out.printf("%6s", j);
        // }
        for (int i = 0; i < s.getInstance().getMaxId(); i++) {
            // System.out.printf("%3s ", i);
            // System.out.printf("%3s %3.1f %s %s\n", i, s.getInstance().getServiceTime(i), s
            // .getInstance().getTimeWindow(i), s.getInstance().getNode(i));
            // for (int j = 0; j < s.getInstance().getMaxId(); j++) {
            // // System.out.printf("%6.1f", s.getInstance().getCostDelegate()
            // // .getTravelTime(i, j, s.getTour(0).getTechnician()));
            // System.out.printf("%3s %3s %6.1f\n", i, j, s.getInstance().getCostDelegate()
            // .getTravelTime(i, j, s.getTour(0).getTechnician()));
            // }
            System.out.println(s.getInstance().getTRSPNode(i));
        }

    }

}
