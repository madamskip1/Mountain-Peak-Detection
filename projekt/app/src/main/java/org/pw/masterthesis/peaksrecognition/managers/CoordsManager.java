package org.pw.masterthesis.peaksrecognition.managers;

public class CoordsManager {
    private static final double EARTH_RADIUS = 6371.0;
    private final int[] latitudeRange;
    private final int[] longitudeRange;
    private final double altitudeScale = 1.0 / 1000.0;
    private final double[] gridSize;
    private final double[] observerLocationGeo;

    public CoordsManager(double[] observerLocationGeo, int[][] coordsRange, double[] gridSize) {
        latitudeRange = coordsRange[0];
        longitudeRange = coordsRange[1];
        this.gridSize = gridSize;
        this.observerLocationGeo = observerLocationGeo;
    }

    public static double equirectangularApproximation(double latitude1, double longitude1, double latitude2, double longitude2) {
        double deltaLongitude = Math.toRadians(longitude2 - longitude1);
        double deltaLatitude = Math.toRadians(latitude2 - latitude1);
        double sumLatitude = Math.toRadians(latitude1 + latitude2);

        double x = deltaLongitude * Math.cos(sumLatitude / 2.0);
        return EARTH_RADIUS * Math.sqrt(x * x + deltaLatitude * deltaLatitude);
    }

    public double[] convertGeoToLocalCoords(double latitude, double longitude, double altitude) {
        double x = ((double) latitudeRange[1] - latitude) * gridSize[0];
        double z = (longitude - (double) longitudeRange[0]) * gridSize[1];
        double y = altitude * altitudeScale;

        return new double[]{x, y, z};
    }

    public double calcDistanceObserverToPoint(double latitude, double longitude) {
        return equirectangularApproximation(latitude, longitude,
                observerLocationGeo[0], observerLocationGeo[1]);
    }

    public int[] getLatitudeRange() {
        return latitudeRange;
    }

    public int[] getLongitudeRange() {
        return longitudeRange;
    }
}
