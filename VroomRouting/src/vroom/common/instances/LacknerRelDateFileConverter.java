/**
 * 
 */
package vroom.common.instances;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import vroom.common.utilities.FileBufferedWriter;
import vroom.common.utilities.Utilities;

/**
 * <code>LacknerRelDateFileConverter</code> contains methods to convert Lackner release date files in a more readable
 * format
 * <p>
 * Creation date: Nov 22, 2011 - 11:05:10 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class LacknerRelDateFileConverter {

    public static String sSourceDir = "../Instances/dvrptw/lackner";
    public static String sDestDir   = "../Instances/dvrptw/lackner_rd";

    public static void convertFile(File source, String destDir) throws IOException {
        String[] nameArgs = source.getName().split("_");
        String instanceName = nameArgs[0];
        int propDyn = Integer.valueOf(nameArgs[3]);

        FileBufferedWriter out = new FileBufferedWriter(String.format(DVRPReleaseDateGenerator.sNameFormat, destDir,
                instanceName, propDyn));
        BufferedReader in = new BufferedReader(new FileReader(source));

        // Write a comment line
        out.writeLine("# Instance %s - %s dynamic requests - %s static requests", instanceName, propDyn, 100 - propDyn);

        // #Dyn/Static Propotions
        out.writeLine("%-5s %s", "Dyn", "Stat");
        out.writeLine("%-5s %s", propDyn, 100 - propDyn);

        // Headers
        out.writeLine("%-5s %s", "ID", "RD");
        String line;
        ArrayList<Integer[]> rd = new ArrayList<Integer[]>(100);
        boolean flushed = false;
        while ((line = in.readLine()) != null && line.length() > 1) {
            // out.writeLine(line);
            if (!flushed && !line.contains("-1")) {
                // Flush the static rd
                Collections.sort(rd, new Comparator<Integer[]>() {
                    @Override
                    public int compare(Integer[] o1, Integer[] o2) {
                        return o1[0] - o2[0];
                    }
                });
                for (Integer[] l : rd)
                    out.writeLine("%-5s %s", l[0], l[1]);
                flushed = true;
                rd.clear();
            }
            if (line.startsWith(" "))
                line = line.replaceFirst("\\s+", "");
            String[] l = line.split("\\s+");
            rd.add(new Integer[] { Integer.valueOf(l[0]), Integer.valueOf(l[1]) });
        }
        Collections.sort(rd, new Comparator<Integer[]>() {
            @Override
            public int compare(Integer[] o1, Integer[] o2) {
                return o1[0] - o2[0];
            }
        });
        for (Integer[] l : rd)
            out.writeLine("%-5s %s", l[0], l[1]);

        in.close();
        out.flush();
        out.close();
    }

    public static void convertFiles(String sourceDir, String destDir) throws IOException {
        List<File> files = Utilities.listFiles(sourceDir, ".+_in\\.txt");
        for (File file : files) {
            System.out.printf("Converting file %s (dest dir:%s)\n", file.getPath(), destDir);
            convertFile(file, destDir);
        }
    }

    /**
     * JAVADOC
     * 
     * @param args
     */
    public static void main(String[] args) {
        try {
            convertFiles(sSourceDir, sDestDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("FINISHED");
    }

}
