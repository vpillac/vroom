package vroom.common.modeling.dataModel.attributes;

import java.awt.geom.Point2D;

import vroom.common.utilities.GeoTools.CoordinateSytem;

/**
 * The Class PointLocation is an implementation of {@link ILocation} as a point with two double precision coordinates.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #created 15-Feb-2010 11:29:51 a.m.
 */
public class PointLocation extends Point2D implements ILocation {

    /** The coordinate system in which this point is defined */
    private final CoordinateSytem mCoordinateSystem;

    /** the abscissa of the point. */
    private final double          mXCoordinate;

    /** the ordinate of the point. */
    private final double          mYCoordinate;

    /**
     * Instantiates a new point location in a cartesian system.
     * 
     * @param x
     *            the x coordinate
     * @param y
     *            the y coordinate
     */
    public PointLocation(double x, double y) {
        this(CoordinateSytem.CARTESIAN, x, y);
    }

    /**
     * Creates a new <code>PointLocation</code>
     * 
     * @param coordinateSystem
     * @param firstCoordinate
     * @param secondCoordinate
     */
    public PointLocation(CoordinateSytem coordinateSystem, double firstCoordinate, double secondCoordinate) {
        mCoordinateSystem = coordinateSystem;
        mXCoordinate = firstCoordinate;
        mYCoordinate = secondCoordinate;
    }

    /*
     * (non-Javadoc)
     * @see edu.uniandes.copa.utils.IObjectWithName#getName()
     */
    @Override
    public String getName() {
        return "Point Location";
    }

    /**
     * Gets the x coordinate.
     * 
     * @return the x coordinate
     */
    @Override
    public double getX() {
        return mXCoordinate;
    }

    /**
     * Gets the y coordinate.
     * 
     * @return the y coordinate
     */
    @Override
    public double getY() {
        return mYCoordinate;
    }

    /*
     * (non-Javadoc)
     * @see vroom.common.modeling.dataModel.attributes.ILocation#getCoordinateSystem()
     */
    @Override
    public CoordinateSytem getCoordinateSystem() {
        return mCoordinateSystem;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("(%1$.2f,%2$.2f)", getX(), getY());
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = java.lang.Double.doubleToLongBits(mXCoordinate);
        result = prime * result + (int) (temp ^ temp >>> 32);
        temp = java.lang.Double.doubleToLongBits(mYCoordinate);
        result = prime * result + (int) (temp ^ temp >>> 32);
        return result;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PointLocation other = (PointLocation) obj;
        if (java.lang.Double.doubleToLongBits(mXCoordinate) != java.lang.Double.doubleToLongBits(other.mXCoordinate)) {
            return false;
        }
        if (java.lang.Double.doubleToLongBits(mYCoordinate) != java.lang.Double.doubleToLongBits(other.mYCoordinate)) {
            return false;
        }
        return true;
    }

    @Override
    public void setLocation(double x, double y) {
        throw new UnsupportedOperationException("This object is transcient");
    }

}// end PointLocation