package vroom.common.modeling.vrprep.translations;

import java.io.File;

/**
 * Main method to run translation classes
 * 
 * @author Maxim Hoskins <a href="https://plus.google.com/115909706630698463631/about">Profil Google+</a>
 */
public class RunTranslation {

    /**
     * @param args
     *            no args
     */
    public static void main(String[] args) {

        Translator t;
        File directory;
        File[] files;

        // TESTS
        // t = new CvrppdtwBreedam();
        // t.translateFile(".\\..\\InstancesMax\\CVRPPDTW\\Breedam\\10PP.DAT");
        // System.out.println("Done");

        t = new Augerat();
        directory = new File("./../Instances/cvrp/augerat/");
        files = directory.listFiles();

        for (File f : files) {
            if (f.getName().endsWith(".vrp")) {
                t.translateFile(f.toString());
                System.out.println("File " + f + " : DONE");
            }
        }

        // NOT USED ANYMORE
        // t = new CvrppdtwBreedam();
        // directory = new File("./../InstancesMax/CVRPPDTW/Breedam/");
        // files = directory.listFiles();
        //
        // for(File f : files){
        // if(f.getName().endsWith(".DAT")){
        // t.translateFile(f.toString());
        // System.out.println("File "+f+" : DONE");
        // }
        // }

        // t = new Homberger();
        // directory = new File("./../InstancesMax/CVRPTW/Homberger/");
        // files = directory.listFiles();
        //
        // for(File f : files){
        // t.translateFile(f.toString());
        // System.out.println("File "+f+" : DONE");
        // }

        // t = new FischettiTothAndVigo();
        // directory = new File("./../InstancesMax/ACVRP/FischettiTothAndVigo/");
        // files = directory.listFiles();
        //
        // for(File f : files){
        // t.translateFile(f.toString());
        // System.out.println("File "+f+" : DONE");
        // }

        // t = new Cordeau("./../InstancesMax/VRPRep/cvrptw/cordeau/");
        // directory = new File("./../InstancesMax/CVRPTW/Cordeau/");
        // files = directory.listFiles();
        //
        // for(File f : files){
        // t.translateFile(f.toString());
        // System.out.println("File "+f+" : DONE");
        // }

        // t = new Taillard();
        // directory = new File("./../InstancesMax/CVRP/Taillard/");
        // files = directory.listFiles();
        //
        // for(File f : files){
        // if(f.getName().endsWith(".dat")){
        // t.translateFile(f.toString());
        // System.out.println("File "+f+" : DONE");
        // }
        // }

        // t = new Kovac(false);
        // directory = new File("./../Instances/strsp/kovacs/tasks_noTeam/");
        // files = directory.listFiles();
        //
        // for(File f : files){
        // if(f.getName().endsWith(".txt")){
        // t.translateFile(f.toString());
        // System.out.println("File "+f+" : DONE");
        // }
        // }

        // t = new Golden();
        // directory = new File("./../InstancesMax/cvrp/Golden_benchmarks/");
        // files = directory.listFiles();
        //
        // for(File f : files){
        // if(f.getName().endsWith(".vrp")){
        // t.translateFile(f.toString());
        // System.out.println("File "+f+" : DONE");
        // }
        // }

    }

}
