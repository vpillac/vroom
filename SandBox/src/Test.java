/*
 * National ICT Australia - http://www.nicta.com.au - All Rights Reserved
 */

public class Test {

    public Test() {
        // TODO Auto-generated constructor stub
    }

    public static void main(String[] args) {
        int n = (int) 1e3;

        double a = 132135654125.0651656, b = 45414251468.156546423, c = 0;
        for (int i = 0; i < n; i++) {
            c = a + b;
        }

        long startTime = System.nanoTime();
        double nmax = Math.pow(n, 3);
        for (int i = 0; i < nmax; i++) {
            c = a + b + c % a;
        }
        long endTime = System.nanoTime();
        System.out.println(c);
        System.out.println((endTime - startTime) / 1000000);
    }

}
