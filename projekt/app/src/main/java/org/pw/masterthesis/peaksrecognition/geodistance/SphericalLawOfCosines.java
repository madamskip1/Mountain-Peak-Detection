package org.pw.masterthesis.peaksrecognition.geodistance;

public class SphericalLawOfCosines implements GeoDistance {
    @Override
    public double calcDistance(double latitude1, double longitude1, double latitude2, double longitude2) {
        double deltaLongitude = Math.toRadians(longitude2 - longitude1);

        double latitude1Radians = Math.toRadians(latitude1);
        double latitude2Radians = Math.toRadians(latitude2);

        double centralAngle = Math.acos(Math.sin(latitude1Radians) * Math.sin(latitude2Radians) + Math.cos(latitude1Radians) * Math.cos(latitude2Radians) * Math.cos(deltaLongitude));
        return EARTH_RADIUS * centralAngle;
    }
}
