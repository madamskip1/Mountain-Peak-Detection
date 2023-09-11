package org.pw.masterthesis.peaksrecognition.geodistance;

public class Vincent implements GeoDistance {
    private final double ITERATION_LIMIT;

    public Vincent(double iteration_limit) {
        ITERATION_LIMIT = iteration_limit;
    }

    @Override
    public double calcDistance(double latitude1, double longitude1, double latitude2, double longitude2) {
        double a = 6378137.0; // length of semi-major axis of the ellipsoid (radius at equator), in meters
        double f = 1.0 / 298.257223563; // 	flattening of the ellipsoid;
        double b = 6356752.314245; // 	length of semi-minor axis of the ellipsoid (radius at the poles), in meters
        double deltaLongitude = Math.toRadians(longitude2 - longitude1);
        double latitude1Radians = Math.toRadians(latitude1);
        double latitude2Radians = Math.toRadians(latitude2);

        double U1 = Math.atan((1.0 - f) * Math.tan(latitude1Radians));
        double U2 = Math.atan((1.0 - f) * Math.tan(latitude2Radians));
        double sinU1 = Math.sin(U1);
        double sinU2 = Math.sin(U2);
        double cosU1 = Math.cos(U1);
        double cosU2 = Math.cos(U2);

        double cosU1sinU2 = cosU1 * sinU2;
        double sinU1cosU2 = sinU1 * cosU2;
        int iteration = 0;

        double lambda = deltaLongitude;
        double lambdaPrime = 0.0;
        double sigma = 0.0;
        double sinSigma = 0.0;
        double cosSigma = 0.0;
        double cos2SigmaM = 0.0;
        double cosSqAlpha = 0.0;

        do {
            double sinLambda = Math.sin(lambda);
            double cosLambda = Math.cos(lambda);
            sinSigma = Math.sqrt((cosU2 * sinLambda) * cosU2 * sinLambda
                    + (cosU1sinU2 - sinU1cosU2 * cosLambda)
                    * (cosU1sinU2 - sinU1cosU2 * cosLambda));
            if (sinSigma == 0.0) {
                return 0.0;
            }
            cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda;
            sigma = Math.atan2(sinSigma, cosSigma);
            double sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
            cosSqAlpha = 1.0 - sinAlpha * sinAlpha;
            cos2SigmaM = cosSigma - 2.0 * sinU1 * sinU2 / cosSqAlpha;
            double C = f / 16.0 * cosSqAlpha * (4.0 + f * (4.0 - 3.0 * cosSqAlpha));
            lambdaPrime = lambda;
            lambda = deltaLongitude + (1.0 - C) * f * sinAlpha * (sigma + C * sinSigma
                    * (cos2SigmaM + C * cosSigma * (-1.0 + 2.0 * cos2SigmaM * cos2SigmaM)));
        } while (Math.abs(lambda - lambdaPrime) > 1.0e-12 && ++iteration < ITERATION_LIMIT);

        if (iteration == ITERATION_LIMIT) {
            return 0.0;
        }
        double uSq = cosSqAlpha * (a * a - b * b) / (b * b);
        double A = 1.0 + uSq / 16384.0 * (4096.0 + uSq * (-768.0 + uSq * (320.0 - 175.0 * uSq)));
        double B = uSq / 1024.0 * (256.0 + uSq * (-128.0 + uSq * (74.0 - 47.0 * uSq)));
        double deltaSigma = B * sinSigma * (cos2SigmaM + B / 4.0 * (cosSigma *
                (-1.0 + 2.0 * cos2SigmaM * cos2SigmaM) - B / 6.0 * cos2SigmaM *
                (-3.0 + 4.0 * sinSigma * sinSigma) * (-3.0 + 4.0 * cos2SigmaM * cos2SigmaM)));
        double s = b * A * (sigma - deltaSigma);
        s /= 1000;
        return s;
    }
}
