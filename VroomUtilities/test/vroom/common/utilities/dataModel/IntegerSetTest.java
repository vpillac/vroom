package vroom.common.utilities.dataModel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import vroom.common.utilities.IntegerSet;

public class IntegerSetTest {

    Random           rnd;
    HashSet<Integer> ref;
    IntegerSet       set;
    int              testIt = 10000;
    int              maxVal = 1000;

    @Before
    public void setUp() {
        rnd = new Random();
        ref = new HashSet<Integer>();
        set = new IntegerSet(maxVal);
    }

    protected void fill() {
        for (int i = 0; i < maxVal / 2; i++) {
            int val = rnd.nextInt(maxVal);
            ref.add(val);
            set.add(val);
        }
    }

    @Test
    public void testAdd() {
        for (int i = 0; i < testIt; i++) {
            int val = rnd.nextInt(maxVal);
            ref.add(val);
            set.add(val);
            assertTrue(ref.equals(set));
        }
    }

    @Test
    public void testRemove() {
        fill();
        for (int i = 0; i < testIt; i++) {
            int val = rnd.nextInt(maxVal);
            ref.remove(val);
            set.remove(val);
            assertTrue(ref.equals(set));
        }
    }

    @Test
    public void testContains() {
        fill();
        for (int i = 0; i < testIt; i++) {
            int val = rnd.nextInt(maxVal);
            assertEquals(ref.contains(val), set.contains(val));
        }
    }

    public static void main(String[] args) {
        IntegerSetTest test = new IntegerSetTest();
        test.setUp();
        test.testAdd();
        test.setUp();
        test.testRemove();
        test.setUp();
        test.testContains();
    }
}
