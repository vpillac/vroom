/**
 * 
 */
package longsVsDoubles;

import java.util.Random;

/**
 * The class <code>DoubleVsLongsGeektor</code> JAVADOC
 * <p>
 * Creation date: Dec 12, 2011 - 11:17:32 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
@SuppressWarnings("unused")
public class DoubleVsLongsGeektor {

    private final static int SCALE = (int) 10E6;

    private final int        mSize;
    private final double[]   mDoubles;
    private final long[]     mLongs;

    private long             mDoubleTimer;
    private long             mLongTimer;

    /**
     * Getter for <code>doubleTimer</code>
     * 
     * @return the doubleTimer
     */
    public long getDoubleTimer() {
        return mDoubleTimer;
    }

    /**
     * Getter for <code>longTimer</code>
     * 
     * @return the longTimer
     */
    public long getLongTimer() {
        return mLongTimer;
    }

    /**
     * Creates a new <code>DoubleVsLongsGeektor</code>
     * 
     * @param size
     */
    public DoubleVsLongsGeektor(int size) {
        mSize = size;

        mDoubles = new double[size];
        mLongs = new long[size];

        Random r = new Random(0);
        for (int i = 0; i < size; i++) {
            mDoubles[i] = r.nextDouble();
            mLongs[i] = (long) (mDoubles[i] * SCALE);
        }

    }

    public void warmup() {
        testAdd(10);
        testMult(10);
        testComp(10);

        // Test
        reset();
    }

    public void testMult(int rep) {
        long time = System.nanoTime();
        for (int i = 0; i < rep; i++)
            testMultDouble();
        mDoubleTimer += System.nanoTime() - time;

        time = System.nanoTime();
        for (int i = 0; i < rep; i++)
            testMultLong();
        mLongTimer = System.nanoTime() - time;
    }

    public void testAdd(int rep) {
        long time = System.nanoTime();
        for (int i = 0; i < rep; i++)
            testAddDouble();
        mDoubleTimer += System.nanoTime() - time;

        time = System.nanoTime();
        for (int i = 0; i < rep; i++)
            testAddLong();
        mLongTimer = System.nanoTime() - time;
    }

    public void testComp(int rep) {
        long time = System.nanoTime();
        for (int i = 0; i < rep; i++)
            testCompDouble();
        mDoubleTimer += System.nanoTime() - time;

        time = System.nanoTime();
        for (int i = 0; i < rep; i++)
            testCompLong();
        mLongTimer = System.nanoTime() - time;
    }

    public void reset() {
        mDoubleTimer = 0;
        mLongTimer = 0;
    }

    private void testMultDouble() {
        double prod = 0;
        for (int i = 0; i < mSize - 1; i++)
            prod = mDoubles[i] * mDoubles[i + 1];
    }

    private void testMultLong() {
        long prod = 0;
        for (int i = 0; i < mSize - 1; i++)
            prod = mLongs[i] * mLongs[i + 1];
    }

    private void testAddDouble() {
        double prod = 0;
        for (int i = 0; i < mSize - 1; i++)
            prod = mDoubles[i] + mDoubles[i + 1];
    }

    private void testAddLong() {
        long prod = 0;
        for (int i = 0; i < mSize - 1; i++)
            prod = mLongs[i] + mLongs[i + 1];
    }

    private void testCompDouble() {
        boolean prod;
        for (int i = 0; i < mSize - 1; i++)
            prod = mDoubles[i] != mDoubles[i + 1];
    }

    private void testCompLong() {
        boolean prod;
        for (int i = 0; i < mSize - 1; i++)
            prod = mLongs[i] != mLongs[i + 1];
    }

    public static void main(String[] args) {
        int size = (int) 10E6;
        int rep = 10000;

        DoubleVsLongsGeektor test = new DoubleVsLongsGeektor(size);

        System.out.printf("Performance benchamrks for %s operations (size=%s,rep=%s)\n", ((double) size) * rep,
                ((double) size), ((double) rep));

        System.out.println("Scaling factor: " + SCALE);

        test.warmup();

        test.reset();
        System.out.println("Multiplication");
        test.testMult(rep);
        System.out.printf(" Doubles: %9s ns\n", test.getDoubleTimer());
        System.out.printf(" Longs  : %9s ns\n", test.getLongTimer());

        test.reset();
        System.out.println("Addition");
        test.testAdd(rep);
        System.out.printf(" Doubles: %9s ns\n", test.getDoubleTimer());
        System.out.printf(" Longs  : %9s ns\n", test.getLongTimer());

        test.reset();
        System.out.println("Comparison");
        test.testComp(rep);
        System.out.printf(" Doubles: %9s ns\n", test.getDoubleTimer());
        System.out.printf(" Longs  : %9s ns\n", test.getLongTimer());
    }
}
