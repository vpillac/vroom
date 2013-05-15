package vroom.trsp.bench.misc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import vroom.common.utilities.FileBufferedWriter;
import vroom.common.utilities.Utilities;

public class MergeCSV {

    // BiDVRP
    private final static String PATH    = "./tmp/";
    private final static String DEST    = "./tmp/Bench_BiDVRP_opt_params.csv";
    // private final static String PATH = "/media/data/Documents/Dropbox/Doctorat/ResultsBench/bidvrp";
    // private final static String DEST =
    // "/media/data/Documents/Dropbox/Doctorat/ResultsBench/bidvrp/Bench_BiDVRP_opt_params.csv";
    private final static String PATTERN = "trsp_bench_1208.+_pBiALNS.+csv";
    private final static String HEADER  = "ad;";

    // pALN
    // private final static String PATH = "/home/vpillac/Documents/Dropbox/Doctorat/ResultsBench/pALNS/params";
    // private final static String DEST =
    // "/home/vpillac/Documents/Dropbox/Doctorat/ResultsBench/pALNS/Bench_pALNS_120517_params.csv";
    // private final static String PATTERN = ".+.csv";
    // private final static String HEADER = "threads;pool_size;pIt;pool;";

    public static String getPrefix(File file) {
        // BiDVRP
        return file.getName().substring(file.getName().length() - 15, file.getName().length() - 13);

        // pALNS Benchmark
        // return file.getName().substring(37, 38);
        // String[] info = file.getName().substring(0, file.getName().length() - 4).split("_");
        // StringBuilder prefix = new StringBuilder();
        // for (int i = info.length - 4; i < info.length; i++) {
        // prefix.append(info[i]);
        // prefix.append(";");
        // }
        // return prefix.toString();
    }

    /**
     * JAVADOC
     * 
     * @param args
     */
    public static void main(String[] args) {

        try {
            List<File> files = Utilities.listFiles(PATH, PATTERN);

            FileBufferedWriter out = new FileBufferedWriter(DEST);

            boolean first = true;
            for (File file : files) {
                System.out.println("Parsing file: " + file);

                String prefix = getPrefix(file);

                BufferedReader r = new BufferedReader(new FileReader(file));

                String line = r.readLine();
                while (!line.contains("name;group"))
                    line = r.readLine();
                if (first) {
                    out.writeLine(HEADER + line);
                    first = false;
                }
                line = r.readLine();
                while (line != null) {
                    out.writeLine("%s;%s", prefix, line);
                    line = r.readLine();
                }
                r.close();
            }
            out.flush();
            out.close();
            System.out.println("All files merged in " + DEST);
            System.out.println("FINISHED");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
