package vroom.common.modeling.vrprep.sandbox;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.io.IPersistenceHelper;
import vroom.common.modeling.io.SolomonPersistenceHelper;
import vroom.common.modeling.io.VRPRepPersistenceHelper;
import vroom.common.utilities.Utilities;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.common.utilities.logging.Logging;

public class Converter {

    public static void main(String[] args) {
        String rootDes = "/Users/vpillac/Desktop/vrprep/";
        boolean compress = false;

        Logging.setupRootLogger(LoggerHelper.LEVEL_DEBUG, LoggerHelper.LEVEL_DEBUG, false);

        String srcDir = "../Instances/cvrptw/solomon";
        String filePattern = ".+txt";
        String dest = rootDes + "solomon/%s.xml";
        String ref = "Solomon, M. M. Algorithms for the Vehicle-Routing and Scheduling Problems with Time Window Constraints Operations Research, 1987, 35, 254-265";
        String problem = "CVRPTW";
        IPersistenceHelper<File> reader = new SolomonPersistenceHelper();

        // String srcDir = "../Instances/cvrp/christofides-mingozzi-toth";
        // String filePattern = "vrpnc.+txt";
        // String dest = rootDes + "christofides-mingozzi-toth/%s.xml";
        // String ref =
        // "Chapter 11 of N.Christofides, A.Mingozzi, P.Toth and C.Sandi (eds), Combinatorial optimization, John Wiley, Chichester 1979.";
        // String problem = "CVRP";
        // IPersistenceHelper<File> reader = new ChristofidesPersistenceHelper();

        // String srcDir = "../Instances/cvrp/augerat";
        // String ref =
        // "P. Augerat, J.M. Belenguer, E. Benavent, A. Corber‡n, D. Naddef, G. Rinaldi, Computational Results with a Branch and Cut Code for the Capacitated Vehicle Routing Problem, Research Report 949-M, Universite Joseph Fourier, Grenoble, France";
        // String dest = rootDes + "augerat-et-al/%s.xml";

        // String srcDir = "../Instances/cvrp/christofides-eilon";
        // String ref =
        // "Christofides N. and Eilon S. (1969) An Algorithm for the Vehicle Dispatching Problems. Operational Research Quarterly, 20(3), pp 309-318.";
        // String dest = rootDes + "christofides-eilon/%s.xml";

        // String problem = "CVRP";
        // String filePattern = ".+vrp";
        // IPersistenceHelper<File> reader = new TSPLibPersistenceHelper();

        try {
            List<File> files = Utilities.listFiles(srcDir, filePattern);

            VRPRepPersistenceHelper writer = new VRPRepPersistenceHelper();
            writer.getDefaultInfo().setReference(ref);
            writer.getDefaultInfo().setProblem(problem);
            writer.getDefaultInfo().getContributor().setName("Victor Pillac");
            writer.getDefaultInfo().getContributor().setEmail("vpillac@nicta.com.au");

            for (File f : files) {
                // if (f.getName().contains("E-n13-k4.vrp")) {
                try {
                    IVRPInstance instance = reader.readInstance(f);
                    String destFile = String.format(dest, instance.getName());
                    String name = f.getName();
                    name = name.substring(0, name.lastIndexOf("."));
                    writer.getDefaultInfo().setName(name);
                    System.out.printf("Converting file %s (%s) to %s\n", f.getName(), name,
                            destFile);
                    writer.writeInstance(instance, new File(destFile), compress);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // }
            }
        } catch (FileNotFoundException e) {
            Logging.getBaseLogger().exception("Converter.main", e);
        }

        System.out.println("TERMINATED");
    }

}
