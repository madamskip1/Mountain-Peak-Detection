package org.pw.masterthesis.peaksrecognition.geodistance;

public class EquirectangularApproximation implements GeoDistance {
    @Override
    public double calcDistance(double latitude1, double longitude1, double latitude2, double longitude2) {
        double deltaLongitude = Math.toRadians(longitude2 - longitude1);
        double deltaLatitude = Math.toRadians(latitude2 - latitude1);
        double sumLatitude = Math.toRadians(latitude1 + latitude2);

        double x = deltaLongitude * Math.cos(sumLatitude / 2.0);
        return EARTH_RADIUS * Math.sqrt(x * x + deltaLatitude * deltaLatitude);
    }
}
