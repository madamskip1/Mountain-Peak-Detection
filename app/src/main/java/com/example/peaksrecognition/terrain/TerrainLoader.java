package com.example.peaksrecognition.terrain;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class TerrainLoader {
    private final String[][] filesNamesGrid = new String[][]
            {
                    { null, null, null },
                    { "N49E019.hgt", "N49E020.hgt", null },
                    { "N48E019.hgt", "N48E020.hgt", null }
            };
    private final float[] world_size = new float[] { 222.38985328911747f, 145.87864979682834f };
    private final float[] grid_size = new float[] { 111.19492664455873f, 72.93932489841417f };
    private final int[] newHgtSize = new int[] { 2402, 2402 };
    private final float max_distance = 50.0f;
    private final int observer_vertex_x = 793;
    private final int observer_vertex_z = 1298;

    private final float LATITUDE_APPROXIMATION = 111.0f;
    private final float LONGITUDE_APPROXIMATION = 73.0f;

    private final float[] worldSize;
    private final int[] hgtSize = new int[]{1201, 1201};
    private final Context context;
    private float[] scale;

    private float[] vertices;



    public TerrainLoader(Context context) {
        this.context = context;
        worldSize = new float[]{LATITUDE_APPROXIMATION, LONGITUDE_APPROXIMATION};
        calcScale();
        short[][] heightMap = loadHgtGrid();
        heightMap = dropUnusedData(heightMap);
    }

    private short[][] loadHgtFile(String path) {
        byte[] data;
        try {
            data = loadHgtByteData(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        short[][] heightMap = convertHgtByteToArray(data);
        return heightMap;
    }

    private short[][] loadHgtGrid()
    {
        int startRow = -1;
        int startCol = -1;
        int endRow = -1;
        int endCol = -1;

        for (int i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 3; ++j)
            {
                if (filesNamesGrid[i][j] != null)
                {
                    if (startRow == -1)
                    {
                        startRow = i;
                    }
                    if (startCol == -1)
                    {
                        startCol = j;
                    }
                    if (endRow < i)
                    {
                        endRow = i;
                    }
                    if (endCol < j)
                    {
                        endCol = j;
                    }
                }
            }
        }

        int rows = endRow - startRow + 1;
        int cols = endCol - startCol + 1;

        short[][] elevation = new short[rows * 1201][cols * 1201];
        for (int i = 0; i < rows; ++i)
        {
            for (int j = 0; j < cols; ++j)
            {
                short[][] fileData = loadHgtFile("srtm_data/" + filesNamesGrid[i + startRow][j + startCol]);
                int xOffset = i * 1201;
                int zOffset = j * 1201;

                for (int x = 0; x < 1201; ++x)
                {
                    int rowIndex = x + xOffset;
                    for (int z = 0; z < 1201; ++z)
                    {
                        int colIndex = z + zOffset;
                        short val = fileData[x][z];
                        elevation[rowIndex][colIndex] = val;
                    }
                }
                fileData = null;
            }
        }

        return elevation;
    }

    private short[][] dropUnusedData(short[][] heightMap)
    {
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

        float origin_x = 0.0f + (float)x_start * scale[0];
        float origin_z = 0.0f + (float)z_start * scale[2];

        short[][] newHeightMap = new short[x_end - x_start + 1][z_end - z_start + 1];
        for (int i = x_start; i <= x_end; i++) {
            for (int j = z_start; j <= z_end; j++) {
                newHeightMap[i - x_start][j - z_start] = heightMap[i][j];
            }
        }
        heightMap = null;
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
        int arrayRow = 0;
        int arrayCol = 0;

        for (int i = 0; i < initHgtSize; ++i) {
            arrayCol = 0;
            if (i % 3 == 0) {
                arrayRow = i / 3;
            } else {
                arrayRow = -1;
            }
            for (int j = 0; j < initHgtSize; ++j) {
                short value = byteBuffer.getShort();

                if (arrayRow != -1 && j % 3 == 0) {
                    heightMap[arrayRow][arrayCol] = value;
                    ++arrayCol;
                }
            }
        }
        return heightMap;
    }

    private void calcScale() {
        float xScale = worldSize[0] / (float) (hgtSize[0] - 1);
        float yScale = (float) (1.0 / 1000.0);
        float zScale = worldSize[1] / (float) (hgtSize[1] - 1);

        scale = new float[]{xScale, yScale, zScale};
    }

    private void generateVertices(short[][] heightMap) {
        float[] vertices = new float[1201 * 1201 * 3];
        int verticesIndex = 0;

        for (int x = 0; x < 1201; ++x) {
            for (int z = 0; z < 1201; ++z) {
                short y = heightMap[x][z];
                float xCoord = 0.0f + scale[0] * x;
                float yCoord = 0.0f + scale[1] * y;
                float zCoord = 0.0f + scale[2] * z;
                vertices[verticesIndex] = xCoord;
                vertices[verticesIndex + 1] = yCoord;
                vertices[verticesIndex + 2] = zCoord;
                verticesIndex += 3;
            }
        }
        this.vertices = vertices;
    }

    private void generateTriangles() {
        int trianglesNum = ((1201 - 1) * (1201 - 1) *2);
        int[] triangles = new int[trianglesNum * 3];

        int trianglesIndex = 0;
        int index = 0;
        int xMax = 1201 - 1;
        int zMax = 1201 - 1;

        for (int x = 0; x < xMax; ++x) {
            for (int z = 0; z < zMax; ++z) {
                int a = index;
                int b = index + 1;
                int c = index + 1201 + 1;
                int d = index + 1201;
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
    }

}
