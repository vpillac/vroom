/**
 * 
 */
package vroom.common.utilities;

/**
 * The class <code>GeoTools</code> provides utility methods to calculate distances using different coordinate systems.
 * <p>
 * Creation date: Mar 22, 2012 - 1:05:13 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class GeoTools {

    public static enum CoordinateSytem {
        CARTESIAN, POLAR, LAT_LON_DEC_DEG
    };

    /** The mean earth radius in kilometers */
    public static final double EARTH_RADIUS_KM = 6371.009;
    /** The mean earth radius in meters */
    public static final double EARTH_RADIUS_M  = 6371009;

    /**
     * Returns the euclidean distance between two points
     * 
     * @param x1
     *            the x coordinate of the first point
     * @param y1
     *            the y coordinate of the first point
     * @param x2
     *            the x coordinate of the second point
     * @param y2
     *            the y coordinate of the second point
     * @return the distance between points {@code  (x1,y1)} and {@code  (x2,y2)}
     */
    public static double distEuclidean(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    /**
     * Calculates geodetic distance between two points specified by latitude/longitude using Haversine formula
     * 
     * @param lat1
     *            first point latitude in decimal degrees
     * @param lon1
     *            first point longitude in decimal degrees
     * @param lat2
     *            second point latitude in decimal degrees
     * @param lon2
     *            second point longitude in decimal degrees
     * @returns distance in meters between points with 0.3% precision
     */
    public static double distHaversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        double sindLat = Math.sin(dLat / 2);
        double sindLon = Math.sin(dLon / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLon, 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_M * c;
    }

    /**
     * Calculates geodetic distance between two points specified by latitude/longitude using Vincenty inverse formula
     * for ellipsoids
     * 
     * @param lat1
     *            first point latitude in decimal degrees
     * @param lon1
     *            first point longitude in decimal degrees
     * @param lat2
     *            second point latitude in decimal degrees
     * @param lon2
     *            second point longitude in decimal degrees
     * @returns distance in meters between points with 5.10<sup>-4</sup> precision
     * @see <a href="http://www.movable-type.co.uk/scripts/latlong-vincenty.html">Originally posted here</a>
     */
    public static double distVincenty(double lat1, double lon1, double lat2, double lon2) {
        double a = 6378137, b = 6356752.314245, f = 1 / 298.257223563; // WGS-84 ellipsoid params
        double L = Math.toRadians(lon2 - lon1);
        double U1 = Math.atan((1 - f) * Math.tan(Math.toRadians(lat1)));
        double U2 = Math.atan((1 - f) * Math.tan(Math.toRadians(lat2)));
        double sinU1 = Math.sin(U1), cosU1 = Math.cos(U1);
        double sinU2 = Math.sin(U2), cosU2 = Math.cos(U2);

        double sinLambda, cosLambda, sinSigma, cosSigma, sigma, sinAlpha, cosSqAlpha, cos2SigmaM;
        double lambda = L, lambdaP, iterLimit = 100;
        do {
            sinLambda = Math.sin(lambda);
            cosLambda = Math.cos(lambda);
            sinSigma = Math.sqrt((cosU2 * sinLambda) * (cosU2 * sinLambda)
                    + (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda) * (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda));
            if (sinSigma == 0)
                return 0; // co-incident points
            cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda;
            sigma = Math.atan2(sinSigma, cosSigma);
            sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
            cosSqAlpha = 1 - sinAlpha * sinAlpha;
            cos2SigmaM = cosSigma - 2 * sinU1 * sinU2 / cosSqAlpha;
            if (Double.isNaN(cos2SigmaM))
                cos2SigmaM = 0; // equatorial line: cosSqAlpha=0 (§6)
            double C = f / 16 * cosSqAlpha * (4 + f * (4 - 3 * cosSqAlpha));
            lambdaP = lambda;
            lambda = L + (1 - C) * f * sinAlpha
                    * (sigma + C * sinSigma * (cos2SigmaM + C * cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)));
        } while (Math.abs(lambda - lambdaP) > 1e-12 && --iterLimit > 0);

        if (iterLimit == 0)
            return Double.NaN; // formula failed to converge

        double uSq = cosSqAlpha * (a * a - b * b) / (b * b);
        double A = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
        double B = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));
        double deltaSigma = B
                * sinSigma
                * (cos2SigmaM + B
                        / 4
                        * (cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM) - B / 6 * cos2SigmaM
                                * (-3 + 4 * sinSigma * sinSigma) * (-3 + 4 * cos2SigmaM * cos2SigmaM)));
        double dist = b * A * (sigma - deltaSigma);

        return dist;
    }

    public static void main(String[] args) {
        double lat1 = 39.021911;
        double lon1 = -104.397891;

        double lat2 = 39.021385;
        double lon2 = -104.396826;

        System.out.println(distHaversine(lat1, lon1, lat2, lon2));
        System.out.println(distVincenty(lat1, lon1, lat2, lon2));
    }
}
