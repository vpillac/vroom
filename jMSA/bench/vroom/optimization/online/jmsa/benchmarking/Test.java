package vroom.optimization.online.jmsa.benchmarking;

public class Test {

    public static void main(String[] args) {
        String test = "This     is  a regex       test";
        String[] t = test.split("\\s+");
        for (String s : t) {
            System.out.println("|" + s + "|");
        }
    }
}
