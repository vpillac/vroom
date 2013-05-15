package vroom.common.utilities.dataModel;

import java.util.HashMap;
import java.util.Map;

/**
 * <code>IDHelper</code> is a utility class used to assign ids to objects.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los
 *         Andes</a> - <a href="http://copa.uniandes.edu.co">Copa</a>, <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #created 15-Feb-2010 11:29:50 a.m.
 * @see IObjectWithID
 */
public class IDHelper {

	private static final Map<Class<?>, IDHelper> ID_HELPERS = new HashMap<Class<?>, IDHelper>();

	public static int getNextId(Class<?> clazz) {
		IDHelper helper;
		if (!ID_HELPERS.containsKey(clazz)) {
			helper = new IDHelper();
			ID_HELPERS.put(clazz, helper);
		} else {
			helper = ID_HELPERS.get(clazz);
		}
		return helper.nextId();
	}

	/** the last id that was attributed. */
	private int mLastId = -1;

	/**
	 * Instantiates a new iD helper.
	 */
	public IDHelper() {
		mLastId = -1;
	}

	/**
	 * Gets the next id.
	 * 
	 * @return the next id that should be attributed
	 */
	public int nextId() {
		mLastId++;
		return mLastId;
	}

}// end IDHelper