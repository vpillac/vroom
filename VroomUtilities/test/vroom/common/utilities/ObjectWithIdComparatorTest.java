/**
 * 
 */
package vroom.common.utilities;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import vroom.common.utilities.dataModel.IObjectWithID;
import vroom.common.utilities.dataModel.ObjectWithID;
import vroom.common.utilities.dataModel.ObjectWithIdComparator;

/**
 * <code>ObjectWithIdComparatorTest</code> is a test case for
 * {@link ObjectWithIdComparator}
 * <p>
 * Creation date: 18 avr. 2010 - 09:20:21
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los
 *         Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class ObjectWithIdComparatorTest {

	private List<IObjectWithID> mObjects;
	private List<IObjectWithID> mTestList;

	@Before
	public void setUp() throws Exception {
		mObjects = new ArrayList<IObjectWithID>();

		for (int id = -10; id < 30; id++) {
			IObjectWithID object = new ObjectWithID(id) {
				@Override
				public String toString() {
					return "[" + getID() + "]";
				}
			};
			mObjects.add(object);
		}

		mTestList = new ArrayList<IObjectWithID>(mObjects);
		Collections.shuffle(mTestList);
	}

	/**
	 * Test method for
	 * {@link vroom.common.utilities.dataModel.ObjectWithIdComparator#compare(vroom.common.utilities.dataModel.IObjectWithID, vroom.common.utilities.dataModel.IObjectWithID)}
	 * .
	 */
	@Test
	public void testCompare() {
		ObjectWithIdComparator comp = new ObjectWithIdComparator();

		Collections.sort(mTestList, comp);

		for (int i = 0; i < mTestList.size(); i++) {
			assertEquals(mObjects.get(i), mTestList.get(i));
		}
	}

}
