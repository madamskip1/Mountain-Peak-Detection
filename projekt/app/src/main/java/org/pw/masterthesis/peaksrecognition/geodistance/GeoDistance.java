package org.pw.masterthesis.peaksrecognition.geodistance;

public interface GeoDistance {
    double EARTH_RADIUS = 6371.0;

    double calcDistance(double latitude1, double longitude1, double latitude2, double longitude2);
}
