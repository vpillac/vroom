package vroom.common.utilities;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

public class LimitedSortedListTest {

    int                        mSize = 10;
    Random                     mRnd;
    ArrayList<Integer>         mRef;
    LimitedSortedList<Integer> mList;

    @Before
    public void setUp() {
        mRnd = new Random(0);
        mRef = new ArrayList<Integer>();
        mList = new LimitedSortedList<Integer>(mSize);
    }

    @Test
    public void testAdd() {
        for (int i = 0; i < 1000; i++) {
            int e = mRnd.nextInt(10000);
            mRef.add(e);
            mList.add(e);
            assertEquals("Unexpected list length", Math.min(mRef.size(), mSize), mList.size());
            // System.out.println(mList);
        }
        Collections.sort(mRef);
        assertArrayEquals(mRef.subList(0, Math.min(mRef.size(), mSize)).toArray(), mList.toArray());
    }

    @Test
    public void testRemove() {
        for (int i = 0; i < 1000; i++) {
            int e = mRnd.nextInt(10000);
            mRef.add(e);
            mList.add(e);
        }
        Collections.sort(mRef);
        List<Integer> ref = mRef.subList(0, Math.min(mRef.size(), mSize));

        while (!mList.isEmpty()) {
            int i = mRnd.nextInt(mList.size());
            mList.remove(i);
            ref.remove(i);
            // System.out.println(mList);
            // System.out.println(ref);
            assertArrayEquals(ref.toArray(), mList.toArray());
        }
    }

    public static void main(String[] args) {
        LimitedSortedListTest test = new LimitedSortedListTest();
        test.setUp();
        test.testAdd();
    }

}
