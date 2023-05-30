package com.example.peaksrecognition.terrain;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.example.peaksrecognition.CoordsManager;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Vector;

public class TerrainLoader {
    private final int[] newHgtSize = new int[]{2402, 2402};
    private final double max_distance = 30.0;
    private final int observer_vertex_x = 793;
    private final int observer_vertex_z = 1298;

    private final double[] worldSize;
    private final int[] initHgtSize = new int[]{3601, 3601};
    private int[] hgtSize;
    private final int simplifyFactor = 3;
    private final Context context;
    private double[] scale;

    private float[] vertices;
    private int[] triangles;
    private int heightMapRows;
    private int heightMapCols;
    private int heightMapOffsetX;
    private int heightMapOffsetZ;

    private double[] terrainOrigin;

    private int[][] coordsRange;
    private double[] gridSize;

    public TerrainLoader(Context context, double observerLatitude, double observerLongitude) {
        this.context = context;

        Vector<int[]> coords = prepareCoords(observerLatitude, observerLongitude, max_distance);
        coordsRange = getCoordsRange(coords);
        double[][] worldGridSize = calcWorldSize(coordsRange);
        worldSize = worldGridSize[0];
        gridSize = worldGridSize[1];
        String[][] filesNamesGrid = prepareFilesNamesGrid(coords, (int) observerLatitude, (int) observerLongitude);

        short[][] heightMap = loadHgtGrid(filesNamesGrid);

        calcScale();

        heightMap = dropUnusedData(heightMap);
        generateVertices(heightMap);
        generateTriangles();
    }

    public float[] getVertices() {
        return vertices;
    }

    public int[] getTriangles()
    {
        return triangles;
    }

    public int[][] getCoordsRange() {
        return coordsRange;
    }
    public double[] getGridSize()
    {
        return gridSize;
    }
    private short[][] loadHgtFile(String path) {
        byte[] data;
        try {
            data = loadHgtByteData(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return convertHgtByteToArray(data);
    }

    private short[][] loadHgtGrid(String[][] filesNamesGrid) {
        int startRow = -1;
        int startCol = -1;
        int endRow = -1;
        int endCol = -1;

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                if (filesNamesGrid[i][j] != null) {
                    if (startRow == -1) {
                        startRow = i;
                    }
                    if (startCol == -1) {
                        startCol = j;
                    }
                    if (endRow < i) {
                        endRow = i;
                    }
                    if (endCol < j) {
                        endCol = j;
                    }
                }
            }
        }

        int rows = endRow - startRow + 1;
        int cols = endCol - startCol + 1;

        short[][] elevation = new short[rows * 1201][cols * 1201];
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                short[][] fileData = loadHgtFile("srtm_data/" + filesNamesGrid[i + startRow][j + startCol]);
                int xOffset = i * fileData.length;
                int zOffset = j * fileData[0].length;

                for (int x = 0; x < fileData.length; ++x) {
                    int rowIndex = x + xOffset;
                    for (int z = 0; z < fileData[0].length; ++z) {
                        int colIndex = z + zOffset;
                        short val = fileData[x][z];
                        elevation[rowIndex][colIndex] = val;
                    }
                }
                fileData = null;
            }
        }

        hgtSize = new int[] { elevation.length, elevation[0].length };
        return elevation;
    }

    private short[][] dropUnusedData(short[][] heightMap) {
        int range_x = (int) Math.ceil(max_distance / scale[0]);
        int range_z = (int) Math.ceil(max_distance / scale[2]);

        int x_start = observer_vertex_x - range_x;
        int x_end = observer_vertex_x + range_x;
        int z_start = observer_vertex_z - range_z;
        int z_end = observer_vertex_z + range_z;
        x_start = Math.max(0, x_start);
        z_start = Math.max(0, z_start);
        x_end = Math.min(x_end, newHgtSize[0]);
        z_end = Math.min(z_end, newHgtSize[1]);

        double origin_x = 0.0f + x_start * scale[0];
        double origin_z = 0.0f + z_start * scale[2];

        short[][] newHeightMap = new short[x_end - x_start + 1][z_end - z_start + 1];
        for (int i = x_start; i <= x_end; i++) {
            if (z_end + 1 - z_start >= 0)
                System.arraycopy(heightMap[i], z_start, newHeightMap[i - x_start], 0, z_end + 1 - z_start);
        }
        heightMap = null;

        terrainOrigin = new double[] { origin_x, 0.0, origin_z };
        heightMapRows = x_end - x_start + 1;
        heightMapCols = z_end - z_start + 1;
        heightMapOffsetX = x_start;
        heightMapOffsetZ = z_start;

        return newHeightMap;
    }

    private byte[] loadHgtByteData(String path) throws IOException {
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = assetManager.open(path);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        byte[] data = new byte[bufferedInputStream.available()];
        bufferedInputStream.read(data);
        bufferedInputStream.close();

        return data;
    }

    private short[][] convertHgtByteToArray(byte[] data) {
        int initHgtSize = 3601;
        int numRows = 1201;
        int numColumns = 1201;
        short[][] heightMap = new short[numRows][numColumns];
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        byteBuffer.order(ByteOrder.BIG_ENDIAN);  // The byte order may vary, adjust accordingly
        int arrayRow;
        int arrayCol;

        for (int i = 0; i < initHgtSize; ++i) {
            arrayCol = 0;
            if (i % simplifyFactor == 0) {
                arrayRow = i / simplifyFactor;
            } else {
                arrayRow = -1;
            }
            for (int j = 0; j < initHgtSize; ++j) {
                short value = byteBuffer.getShort();

                if (arrayRow != -1 && j % simplifyFactor == 0) {
                    heightMap[arrayRow][arrayCol] = value;
                    ++arrayCol;
                }
            }
        }

        return heightMap;
    }

    private void calcScale() {
        double xScale = worldSize[0] / (hgtSize[0] - 1);
        double yScale = (1.0 / 1000.0);
        double zScale = worldSize[1] / (hgtSize[1] - 1);

        scale = new double[]{xScale, yScale, zScale};
    }

    private void generateVertices(short[][] heightMap) {
        float[] vertices = new float[heightMapRows * heightMapCols * 3];
        int verticesIndex = 0;

        for (int x = 0; x < heightMapRows; ++x) {
            for (int z = 0; z < heightMapCols; ++z) {
                short y = heightMap[x][z];
                float xCoord = (float) (terrainOrigin[0] + scale[0] * x);
                float yCoord = (float) (terrainOrigin[1] + scale[1] * y);
                float zCoord = (float) (terrainOrigin[2] + scale[2] * z);
                vertices[verticesIndex] = xCoord;
                vertices[verticesIndex + 1] = yCoord;
                vertices[verticesIndex + 2] = zCoord;
                verticesIndex += 3;
            }
        }
        this.vertices = vertices;
    }

    private void generateTriangles() {
        int trianglesNum = (heightMapRows - 1) * (heightMapCols - 1) * 2;
        int[] triangles = new int[trianglesNum * 3];

        int trianglesIndex = 0;
        int index = 0;
        int xMax = heightMapRows - 1;
        int zMax = heightMapCols - 1;

        for (int x = 0; x < xMax; ++x) {
            for (int z = 0; z < zMax; ++z) {
                int a = index;
                int b = index + 1;
                int c = index + heightMapCols + 1;
                int d = index + heightMapCols;
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

    private String convertCoordsToFileName(int latitude, int longitude) {
        String latitudeStr = String.valueOf(Math.abs(latitude));
        String longitudeStr = String.valueOf(Math.abs(longitude));

        if (latitude > -10 && latitude < 10) {
            latitudeStr = "0" + latitudeStr;
        }

        if (longitude > -100 && longitude < 100) {
            if (longitude > -10 && longitude < 10) {
                longitudeStr = "00" + longitudeStr;
            } else {
                longitudeStr = "0" + longitudeStr;
            }
        }
        String fileName;
        if (latitude >= 0) {
            fileName = "N" + latitudeStr;
        } else {
            fileName = "S" + latitudeStr;
        }

        if (longitude >= 0) {
            fileName += "E" + longitudeStr;
        } else {
            fileName += "W" + longitudeStr;
        }

        return fileName;
    }

    private Vector<int[]> prepareCoords(double observerLatitude, double observerLongitude, double maxDistance) {
        int latitudeInt = (int) observerLatitude;
        int longitudeInt = (int) observerLongitude;
        Vector<double[]> coordsToCheck = new Vector<>();
        Vector<int[]> coordsForFilesNames = new Vector<>();
        double[] y;
        double[] x;

        if (observerLatitude > 0.0) {
            y = new double[]{latitudeInt, observerLatitude, latitudeInt + 1.0};
        } else {
            y = new double[]{latitudeInt - 1, observerLatitude, latitudeInt};
        }

        if (observerLongitude > 0.0) {
            x = new double[]{longitudeInt, observerLongitude, longitudeInt + 1.0};
        } else {
            x = new double[]{longitudeInt - 1, observerLongitude, longitudeInt};
        }

        if (observerLatitude == latitudeInt && observerLongitude == longitudeInt) {
            for (int i = 0; i < 2; ++i) {
                for (int j = 0; j < 2; ++j) {
                    coordsForFilesNames.add(new int[]{latitudeInt - i, longitudeInt - j});
                }
            }
        } else if (observerLatitude == latitudeInt) {
            coordsForFilesNames.add(new int[]{latitudeInt - 1, (int) x[0]});
            coordsForFilesNames.add(new int[]{latitudeInt, (int) x[0]});

            for (int i = 0; i < 2; ++i) {
                coordsToCheck.add(new double[]{latitudeInt, x[0], latitudeInt - i, x[0] - 1});
                coordsToCheck.add(new double[]{latitudeInt, x[2], latitudeInt - i, x[0] + 1});
            }
        } else if (observerLongitude == longitudeInt) {
            coordsForFilesNames.add(new int[]{(int) y[0], longitudeInt - 1});
            coordsForFilesNames.add(new int[]{(int) y[0], longitudeInt});

            for (int i = 0; i < 2; ++i) {
                coordsToCheck.add(new double[]{y[0], longitudeInt, y[0] - 1, longitudeInt - i});
                coordsToCheck.add(new double[]{y[1], longitudeInt, y[0] + 1, longitudeInt - i});
            }
        } else {
            coordsForFilesNames.add(new int[]{latitudeInt, longitudeInt});

            for (int i = 0; i < 3; ++i) { // latitude

                double yCoordToCheck = y[i];
                int yCoordFile = (int) y[0] + (i - 1);

                for (int j = 0; j < 3; ++j) { // longitude
                    if (!(i == 1 && j == 1)) {
                        double xCoordToCheck = x[j];
                        int xCoordFile = (int) x[0] + (j - 1);
                        coordsToCheck.add(new double[]{yCoordToCheck, xCoordToCheck, yCoordFile, xCoordFile});
                    }
                }
            }
        }


        for (double[] coordsData : coordsToCheck) {
            double distance = CoordsManager.equirectangularApproximation(coordsData[0], coordsData[1],
                    observerLatitude, observerLongitude);
            if (distance <= maxDistance) {
                coordsForFilesNames.add(new int[]{(int) coordsData[2], (int) coordsData[3]});
            }
        }

        return coordsForFilesNames;
    }

    private int[][] getCoordsRange(Vector<int[]> coords) {
        int minLatitude = 1000;
        int maxLatitude = -1000;
        int minLongitude = 1000;
        int maxLongitude = -1000;

        for (int[] coord : coords) {
            minLatitude = Math.min(minLatitude, coord[0]);
            maxLatitude = Math.max(maxLatitude, coord[0]);
            minLongitude = Math.min(minLongitude, coord[1]);
            maxLongitude = Math.max(maxLongitude, coord[1]);
        }
        ++maxLatitude;
        ++maxLongitude;

        return new int[][]{{minLatitude, maxLatitude}, {minLongitude, maxLongitude}};
    }

    private double[][] calcWorldSize(int[][] coordsRange) {
        int[] latitudeRange = coordsRange[0];
        int[] longitudeRange = coordsRange[1];
        double latitudeDistance = 0.0;
        double longitudeDistance = 0.0;

        for (int i = 0; i < 2; ++i) {
            latitudeDistance += CoordsManager.equirectangularApproximation(latitudeRange[0], longitudeRange[i],
                    latitudeRange[1], longitudeRange[i]);
            longitudeDistance += CoordsManager.equirectangularApproximation(latitudeRange[i], longitudeRange[0],
                    latitudeRange[i], longitudeRange[1]);
        }

        latitudeDistance /= 2.0;
        longitudeDistance /= 2.0;
        double latitudeGridSize = latitudeDistance / (double) Math.abs(latitudeRange[1] - latitudeRange[0]);
        double longitudeGridSize = longitudeDistance / (double) Math.abs(longitudeRange[1] - longitudeRange[0]);
        Log.d("moje", "range " + latitudeDistance + " " + longitudeDistance + " " + latitudeGridSize + " " + longitudeGridSize + " ");
        return new double[][]{{latitudeDistance, longitudeDistance}, {latitudeGridSize, longitudeGridSize}};
    }

    private String[][] prepareFilesNamesGrid(Vector<int[]> coords, int centralLatitude, int centralLongitude) {
        String[][] filesNamesGrid = new String[][]{
                {null, null, null},
                {null, null, null},
                {null, null, null},
        };
        for (int[] coord : coords) {
            int latitudeDiff = coord[0] - centralLatitude;
            int longitudeDiff = coord[1] - centralLongitude;
            filesNamesGrid[1 - latitudeDiff][1 + longitudeDiff] = convertCoordsToFileName(coord[0], coord[1]) + ".hgt";
        }
        return filesNamesGrid;
    }

}
