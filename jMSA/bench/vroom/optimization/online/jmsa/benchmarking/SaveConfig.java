package vroom.optimization.online.jmsa.benchmarking;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import vroom.optimization.online.jmsa.MSAGlobalParameters;

public class SaveConfig {

    public static void main(String[] args) {
        MSAGlobalParameters params = new MSAGlobalParameters();

        String file = "data/NovoaDefaults.conf";

        // Set the values to export
        NovoaRun.loadDefaultParameters(params);

        // Save to file omitting default values
        BufferedReader r;
        try {
            System.out.println("Savings params to file: " + file);

            params.saveParameters(new File(file), false);

            System.out.println("=================================");
            System.out.println(" File content:");
            System.out.println("=================================");
            r = new BufferedReader(new FileReader(new File(file)));
            String line = r.readLine();

            while (line != null) {
                System.out.println(line);
                line = r.readLine();
            }
            System.out.println("=================================");
            r.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
