/**
 * 
 */
package vroom.common.modeling.dataModel.attributes;

/**
 * <code>ReleaseDate</code> is a simple implementation of {@link IReleaseDate}
 * 
 * <p>
 * Creation date: Nov 8, 2011 - 3:15:23 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los
 *         Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * 
 * @version 1.0
 * 
 */
public class ReleaseDate implements IReleaseDate, Comparable<IReleaseDate> {

	private final double mReleaseDate;

	public ReleaseDate(double releaseDate) {
		super();
		mReleaseDate = releaseDate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see vroom.common.utilities.dataModel.IObjectWithName#getName()
	 */
	@Override
	public String getName() {
		return "RD";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * vroom.common.modeling.dataModel.attributes.IReleaseDate#doubleValue()
	 */
	@Override
	public double doubleValue() {
		return mReleaseDate;
	}

	@Override
	public int compareTo(IReleaseDate o) {
		return o != null ? Double.compare(doubleValue(), o.doubleValue()) : 1;
	}

	@Override
	public String toString() {
		return Double.toString(doubleValue());
	}
}
