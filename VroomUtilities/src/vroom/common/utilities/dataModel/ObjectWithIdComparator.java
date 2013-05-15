package vroom.common.utilities.dataModel;

import java.io.Serializable;
import java.util.Comparator;

/**
 * <code>ObjectWithIdComparator</code> is an implementation of
 * {@link Comparator} to compare instances of {@link IObjectWithID}
 * <p>
 * Creation date: Apr 16, 2010 - 4:57:52 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los
 *         Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class ObjectWithIdComparator implements Comparator<IObjectWithID>,
		Serializable {

	public static final ObjectWithIdComparator INSTANCE = new ObjectWithIdComparator();

	private static final long serialVersionUID = 1L;

	@Override
	public int compare(IObjectWithID o1, IObjectWithID o2) {
		return o1 == null ? (o2 == null ? 0 : -1) : (o2 == null ? 1 : o1
				.getID() - o2.getID());
	}

}
