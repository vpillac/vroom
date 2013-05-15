/**
 * 
 */
package vroom.common.utilities;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import vroom.common.utilities.DoublyLinkedIntSet;
import vroom.common.utilities.DoublyLinkedIntSet.DoublyLinkedIterator;

/**
 * <code>DoublyLinkedIntegerListTest</code>
 * <p>
 * Creation date: Oct 5, 2011 - 10:31:24 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class DoublyLinkedIntegerListTest {

    private static final int   TEST_SIZE = 1000;

    private DoublyLinkedIntSet mList;

    private ArrayList<Integer> mReference;
    private int                mCurrentIdx;

    /**
     * JAVADOC
     * 
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        // List<Integer> values = new ArrayList<Integer>(MAX_VALUE);
        // for (int i = 0; i < values.size() + 1; i++) {
        // values.add(i);
        // }

        Random rnd = new Random(0);
        mReference = new ArrayList<Integer>(TEST_SIZE);
        for (int i = 0; i < TEST_SIZE; i++)
            mReference.add(i % TEST_SIZE);
        Collections.shuffle(mReference, rnd);
        mCurrentIdx = 0;

        mList = new DoublyLinkedIntSet(TEST_SIZE);
    }

    /**
     * Test method for {@link vroom.common.utilities.DoublyLinkedIntSet#listIterator()}.
     */
    @Test
    public void testListIterator() {
        mList.addAll(mReference);

        DoublyLinkedIterator it = mList.listIterator();
        ListIterator<Integer> refIt = mReference.listIterator();

        while (it.hasNext() || refIt.hasNext()) {
            assertEquals("The iterator hasNext should return " + refIt.hasNext(), refIt.hasNext(), it.hasNext());

            Integer e = it.next();
            Integer refE = refIt.next();
            assertEquals(String.format("The iterator next should return %s but returned %s instead", refE, e), refE, e);
        }

    }

    /**
     * Test method for {@link vroom.common.utilities.DoublyLinkedIntSet#listIterator(int)}.
     */
    @Test
    public void testListIteratorInt() {
        mList.addAll(mReference);

        for (int i = 0; i < TEST_SIZE; i++) {

            ListIterator<Integer> refIt = mReference.listIterator(i);
            DoublyLinkedIterator it = mList.listIterator(i);

            while (it.hasNext() || refIt.hasNext()) {
                assertEquals("The iterator hasNext method did not returned the expected value", refIt.hasNext(),
                        it.hasNext());

                Integer e = it.next();
                Integer refE = refIt.next();
                assertEquals(String.format(
                        "The iterator (started at pos:%s e:%s) next did not returned the expected element", i,
                        mReference.get(i)), refE, e);
            }
        }
    }

    /**
     * Test method for {@link vroom.common.utilities.DoublyLinkedIntSet#insert(java.lang.Integer, java.lang.Integer)} .
     */
    @Test
    public void testInsert() {
        ArrayList<Integer> ref = new ArrayList<Integer>(mReference.size());

        Random rnd = new Random(0);
        Iterator<Integer> it = mReference.iterator();

        Integer e = it.next();
        ref.add(e);
        mList.add(e);

        while (it.hasNext()) {
            e = it.next();
            int insIdx = rnd.nextInt(ref.size());
            Integer succ = ref.get(insIdx);
            ref.add(insIdx, e);
            mList.insert(e, succ);
            assertArrayEquals(ref.toArray(new Integer[ref.size()]), mList.toArray());
        }
    }

    /**
     * Test method for {@link vroom.common.utilities.DoublyLinkedIntSet#add(java.lang.Integer)}.
     */
    @Test
    public void testAddInteger() {
        for (int i = 0; i < mReference.size(); i++) {
            mList.add(mReference.get(i));

            assertArrayEquals(toArray(mReference, i + 1), toArray(mList, mList.size()));
        }
    }

    /**
     * Test method for {@link vroom.common.utilities.DoublyLinkedIntSet#remove(java.lang.Object)}.
     */
    @Test
    public void testRemoveObject() {
        mList.addAll(mReference);
        Random rnd = new Random(0);
        while (!mReference.isEmpty()) {
            // Remove elements in a random order
            int idx = rnd.nextInt(mReference.size());
            Integer e = mReference.remove(idx);
            mList.remove(e);
            assertArrayEquals(mReference.toArray(new Integer[mReference.size()]), mList.toArray());
        }
    }

    /**
     * Test method for {@link vroom.common.utilities.DoublyLinkedIntSet#contains(java.lang.Object)}.
     */
    @Test
    public void testContainsObject() {
        mList.addAll(mReference);
        for (Integer e : mReference) {
            assertTrue("List should contain element " + e, mList.contains(e));
        }
    }

    public static Integer[] toArray(List<Integer> list, int newSize) {
        Integer[] array = new Integer[newSize];
        Iterator<Integer> it = list.iterator();
        int idx = 0;
        while (it.hasNext() && idx < newSize) {
            array[idx++] = it.next();
        }
        return array;
    }

}
