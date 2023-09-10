package org.pw.masterthesis.peaksrecognition.managers;

import org.pw.masterthesis.peaksrecognition.geodistance.EquirectangularApproximation;
import org.pw.masterthesis.peaksrecognition.geodistance.GeoDistance;

public class CoordsManager {
    private final int[] latitudeRange;
    private final int[] longitudeRange;
    private final double altitudeScale = 1.0 / 1000.0;
    private final double[] gridSize;
    private final double[] observerLocationGeo;
    private final GeoDistance geoDistanceCalculator;

    public CoordsManager(double[] observerLocationGeo, int[][] coordsRange, double[] gridSize) {
        latitudeRange = coordsRange[0];
        longitudeRange = coordsRange[1];
        this.gridSize = gridSize;
        this.observerLocationGeo = observerLocationGeo;
        this.geoDistanceCalculator = new EquirectangularApproximation();
    }

    public double[] convertGeoToLocalCoords(double latitude, double longitude, double altitude) {
        double x = ((double) latitudeRange[1] - latitude) * gridSize[0];
        double z = (longitude - (double) longitudeRange[0]) * gridSize[1];
        double y = altitude * altitudeScale;

        return new double[]{x, y, z};
    }

    public double calcDistanceObserverToPoint(double latitude, double longitude) {
        return geoDistanceCalculator.calcDistance(latitude, longitude,
                observerLocationGeo[0], observerLocationGeo[1]);
    }

    public int[] getLatitudeRange() {
        return latitudeRange;
    }

    public int[] getLongitudeRange() {
        return longitudeRange;
    }
}
