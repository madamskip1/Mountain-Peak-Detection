package com.example.peaksrecognition.terrain;

import com.example.peaksrecognition.Config;
import com.example.peaksrecognition.terrain.TerrainLoader.LoadedTerrain;

public class TerrainData {
    private final double[] scale;
    private final double[] gridSize;
    private final int[][] coordsRange;
    private float[] vertices;
    private int[] triangles;
    private int rows;
    private int cols;
    private double[] origin;
    private int offsetX;
    private int offsetZ;

    public TerrainData(LoadedTerrain loadedTerrain, Config config) {
        scale = loadedTerrain.scale;
        gridSize = loadedTerrain.gridSize;
        coordsRange = loadedTerrain.coordsRange;
        initTerrainData(loadedTerrain.heightMap, config.maxDistance, loadedTerrain.hgtSize);
        loadedTerrain.heightMap = null;
    }

    public float[] getVertices() {
        return vertices;
    }

    public int[] getTriangles() {
        return triangles;
    }

    public int[][] getCoordsRange() {
        return coordsRange;
    }

    public double[] getGridSize() {
        return gridSize;
    }

    public double[] getVertexCoords(int vertexNum)
    {
        int vertexCoordsStart = vertexNum * 3;
        return new double[] { vertices[vertexCoordsStart], vertices[vertexCoordsStart + 1], vertices[vertexCoordsStart + 2] };
    }

    private void initTerrainData(short[][] heightMap, double max_distance, int[] hgtSize) {
        short[][] newHeightMap = dropUnusedData(heightMap, max_distance, hgtSize);
        generateVertices(newHeightMap);
        heightMap = null;
        generateTriangles();
    }

    private short[][] dropUnusedData(short[][] heightMap, double max_distance, int[] loadedHgtSize) {
        final int observer_vertex_x = 793;
        final int observer_vertex_z = 1298;
        int range_x = (int) Math.ceil((max_distance + 1.0) / scale[0]);
        int range_z = (int) Math.ceil((max_distance + 1.0) / scale[2]);

        int x_start = observer_vertex_x - range_x;
        int x_end = observer_vertex_x + range_x;
        int z_start = observer_vertex_z - range_z;
        int z_end = observer_vertex_z + range_z;

        x_start = Math.max(0, x_start);
        z_start = Math.max(0, z_start);
        x_end = Math.min(x_end, loadedHgtSize[0]);
        z_end = Math.min(z_end, loadedHgtSize[1]);

        double origin_x = 0.0f + x_start * scale[0];
        double origin_z = 0.0f + z_start * scale[2];

        short[][] newHeightMap = new short[x_end - x_start + 1][z_end - z_start + 1];
        for (int i = x_start; i <= x_end; i++) {
            if (z_end + 1 - z_start >= 0)
                System.arraycopy(heightMap[i], z_start, newHeightMap[i - x_start], 0, z_end + 1 - z_start);
        }

        origin = new double[]{origin_x, 0.0, origin_z};
        rows = newHeightMap.length;
        cols = newHeightMap[0].length;
        offsetX = x_start;
        offsetZ = z_start;

        return newHeightMap;
    }

    private void generateVertices(short[][] heightMap) {
        float[] vertices = new float[rows * cols * 3];
        int verticesIndex = 0;

        for (int x = 0; x < rows; ++x) {
            for (int z = 0; z < cols; ++z) {
                short y = heightMap[x][z];
                float xCoord = (float) (origin[0] + scale[0] * x);
                float yCoord = (float) (origin[1] + scale[1] * y);
                float zCoord = (float) (origin[2] + scale[2] * z);
                vertices[verticesIndex] = xCoord;
                vertices[verticesIndex + 1] = yCoord;
                vertices[verticesIndex + 2] = zCoord;
                verticesIndex += 3;
            }
        }
        this.vertices = vertices;
    }

    private void generateTriangles() {
        int trianglesNum = (rows - 1) * (cols - 1) * 2;
        int[] triangles = new int[trianglesNum * 3];

        int trianglesIndex = 0;
        int index = 0;
        int xMax = rows - 1;
        int zMax = cols - 1;

        for (int x = 0; x < xMax; ++x) {
            for (int z = 0; z < zMax; ++z) {
                int a = index;
                int b = index + 1;
                int c = index + cols + 1;
                int d = index + cols;
                ++index;

                triangles[trianglesIndex] = a;
                triangles[trianglesIndex + 1] = b;
                triangles[trianglesIndex + 2] = c;

                triangles[trianglesIndex + 3] = a;
                triangles[trianglesIndex + 4] = c;
                triangles[trianglesIndex + 5] = d;

                trianglesIndex += 6;
            }
            ++index;
        }

        this.triangles = triangles;
    }
}
