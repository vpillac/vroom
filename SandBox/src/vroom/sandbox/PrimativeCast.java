package vroom.sandbox;

import java.text.DecimalFormat;

@SuppressWarnings("unused")
public class PrimativeCast {

    private static final int LOOP_COUNT = Integer.MAX_VALUE;

    public PrimativeCast() {

    }

    public void run() {
        for (int i = 0; i < 10; i++) {
            System.out.println("--------------------------------");
            System.out.println("Test Run: " + i);
            System.out.println("--------------------------------");

            // intControl();
            // shortControl();
            // castIntToShort();
            // castShortToInt();
            long mb = multiplyBytes();
            long ms = multiplyShorts();
            long mi = multiplyInts();
            long ml = multiplyLongs();
            long mf = multiplyFloats();
            long md = multiplyDoubles();

            DecimalFormat format = new DecimalFormat("0.00");
            System.out.println("--- the following figures use int as their reference ---");
            System.out.println("multiply byte time - multiply int time = " + (mb - mi) + "ms");
            System.out.println("byte execution speed compared to int: " + format.format((100.0 / mi) * mb) + "%");
            System.out.println("multiply short time - multiply int time = " + (ms - mi) + "ms");
            System.out.println("short execution speed compared to int: " + format.format((100.0 / mi) * ms) + "%");
            System.out.println("multiply long time - multiply int time = " + (ml - mi) + "ms");
            System.out.println("long execution speed compared to int: " + format.format((100.0 / mi) * ml) + "%");

            System.out.println("--- the following figures use float as their reference ---");
            System.out.println("multiply float time - multiply double time = " + (md - mf) + "ms");
            System.out.println("double execution speed compared to float: " + format.format((100.0 / mf) * md) + "%");
        }
    }

    private int intControl() {
        int testInt = 0;

        long start = System.currentTimeMillis();
        for (int i = 0; i < LOOP_COUNT; i++) {
            int result = testInt;
            testInt++;
            result++;
        }
        long end = System.currentTimeMillis();
        int duration = (int) (end - start);
        System.out.println("int to int (control): " + duration);

        return duration;
    }

    private int shortControl() {
        short testShort = 0;

        long start = System.currentTimeMillis();
        for (int i = 0; i < LOOP_COUNT; i++) {
            short result = testShort;
            testShort++;
            result++;
        }
        long end = System.currentTimeMillis();
        int duration = (int) (end - start);
        System.out.println("short to short (control): " + duration);

        return duration;
    }

    private int castIntToShort() {
        int testInt = 0;

        long start = System.currentTimeMillis();
        for (int i = 0; i < LOOP_COUNT; i++) {
            short result = (short) testInt;
            testInt++;
            result++;
        }
        long end = System.currentTimeMillis();
        int duration = (int) (end - start);
        System.out.println("int to short (simple cast): " + duration);

        return duration;
    }

    private int castShortToInt() {
        short testShort = 0;

        long start = System.currentTimeMillis();
        for (int i = 0; i < LOOP_COUNT; i++) {
            int result = testShort;
            testShort++;
            result++;
        }
        long end = System.currentTimeMillis();
        int duration = (int) (end - start);
        System.out.println("short to int (simple automatic cast): " + duration);

        return duration;
    }

    private int multiplyBytes() {
        byte test = 0;

        long start = System.currentTimeMillis();
        for (int i = 0; i < LOOP_COUNT; i++) {
            byte result = (byte) (test * test);
            test++;
            result++;
        }
        long end = System.currentTimeMillis();
        int duration = (int) (end - start);
        System.out.println("multiplying bytes: " + duration);

        return duration;
    }

    private int multiplyShorts() {
        short test = 0;

        long start = System.currentTimeMillis();
        for (int i = 0; i < LOOP_COUNT; i++) {
            short result = (short) (test * test);
            test++;
            result++;
        }
        long end = System.currentTimeMillis();
        int duration = (int) (end - start);
        System.out.println("multiplying shorts: " + duration);

        return duration;
    }

    private int multiplyInts() {
        int testInt = 0;

        long start = System.currentTimeMillis();
        for (int i = 0; i < LOOP_COUNT; i++) {
            int result = testInt * testInt;
            testInt++;
            result++;
        }
        long end = System.currentTimeMillis();
        int duration = (int) (end - start);
        System.out.println("multiplying ints: " + duration);

        return duration;
    }

    private int multiplyLongs() {
        long test = 0;

        long start = System.currentTimeMillis();
        for (int i = 0; i < LOOP_COUNT; i++) {
            long result = test * test;
            test++;
            result++;
        }
        long end = System.currentTimeMillis();
        int duration = (int) (end - start);
        System.out.println("multiplying longs: " + duration);

        return duration;
    }

    private int multiplyFloats() {
        float test = 0.0f;

        long start = System.currentTimeMillis();
        for (int i = 0; i < LOOP_COUNT; i++) {
            float result = test * test;
            test++;
            result++;
        }
        long end = System.currentTimeMillis();
        int duration = (int) (end - start);
        System.out.println("multiplying floats: " + duration);

        return duration;
    }

    private int multiplyDoubles() {
        double test = 0.0;

        long start = System.currentTimeMillis();
        for (int i = 0; i < LOOP_COUNT; i++) {
            double result = test * test;
            test++;
            result++;
        }
        long end = System.currentTimeMillis();
        int duration = (int) (end - start);
        System.out.println("multiplying doubles: " + duration);

        return duration;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        PrimativeCast test = new PrimativeCast();
        test.run();
    }

}