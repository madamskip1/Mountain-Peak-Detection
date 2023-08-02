package org.pw.masterthesis.peaksrecognition.terrain;

public class LoadedTerrain {
    public short[][] heightMap;
    public int[][] coordsRange;
    public double[] gridSize;
    public int[] hgtSize;
    public double[] scale;

    public LoadedTerrain(short[][] heightMap, int[][] coordsRange, double[] gridSize, int[] hgtSize, double[] scale) {
        this.heightMap = heightMap;
        this.coordsRange = coordsRange;
        this.gridSize = gridSize;
        this.hgtSize = hgtSize;
        this.scale = scale;
    }
}
