package vroom.trsp.datamodel;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import vroom.common.modeling.dataModel.Fleet;

/**
 * The Class <code>TechnicianFleet</code> is an extension of {@link Fleet} for
 * the TRSP.
 * <p>
 * Creation date: Feb 15, 2011 - 10:22:33 AM.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los
 *         Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TechnicianFleet extends Fleet<Technician> {

	/**
	 * Creates a new <code>TechnicianFleet</code>.
	 * 
	 * @param technicians
	 *            a collection of the technicians in this fleet
	 * @param homogeneous
	 *            <code>true</code> is the flee is homogeneous
	 * @param tools
	 *            the number of existing tools
	 * @param skills
	 *            the number of existing skills
	 */
	public TechnicianFleet(Collection<Technician> technicians,
			boolean homogeneous) {
		super(technicians, homogeneous);
	}

	/**
	 * Creates a new technician fleet
	 * 
	 * @param technicians
	 *            a collection of the technicians in this fleet
	 * @return a {@link TechnicianFleet} assumed to be heterogeneous
	 */
	public static TechnicianFleet newTechnicianFleet(
			Collection<Technician> technicians) {
		boolean homogeneous = true;
		Iterator<Technician> it = technicians.iterator();
		Technician tech = it.next();
		while (homogeneous && it.hasNext()) {
			Technician tech2 = it.next();
			homogeneous = tech.equals(tech2);
			tech = tech2;
		}

		return new TechnicianFleet(technicians, homogeneous);
	}

	/**
	 * Creates a new technician fleet with a single technician
	 * 
	 * @param technicians
	 *            a collection of the technicians in this fleet
	 * @return a {@link TechnicianFleet} assumed to be heterogeneous
	 */
	public static TechnicianFleet newTechnicianFleet(Technician technician) {
		return newTechnicianFleet(Collections.singleton(technician));
	}

	/**
	 * Limit the size of this fleet.
	 * <p>
	 * Note that this will remove the exceeding {@link Technician} permanently
	 * </p>
	 * 
	 * @param fleetSize
	 *            the size of the new fleet
	 */
	public void limitSize(int fleetSize) {
		if (fleetSize > size())
			throw new IllegalArgumentException(
					"The new fleet size is larger that the current size");
		while (mVehicles.size() > fleetSize)
			mVehicles.remove(mVehicles.size() - 1);

	}

}
