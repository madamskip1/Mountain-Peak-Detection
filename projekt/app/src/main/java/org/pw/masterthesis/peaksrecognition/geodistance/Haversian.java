package org.pw.masterthesis.peaksrecognition.geodistance;

public class Haversian implements GeoDistance {
    @Override
    public double calcDistance(double latitude1, double longitude1, double latitude2, double longitude2) {
        double deltaLatitude = Math.toRadians(latitude2 - latitude1);
        double deltaLongitude = Math.toRadians(longitude2 - longitude1);

        double latitude1Radians = Math.toRadians(latitude1);
        double latitude2Radians = Math.toRadians(latitude2);

        double sinDeltaLatitude = Math.sin(deltaLatitude / 2.0);
        double sinDeltaLongitude = Math.sin(deltaLongitude / 2.0);

        double a = sinDeltaLatitude * sinDeltaLatitude + Math.cos(latitude1Radians) * Math.cos(latitude2Radians) * sinDeltaLatitude * sinDeltaLongitude;
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
}
