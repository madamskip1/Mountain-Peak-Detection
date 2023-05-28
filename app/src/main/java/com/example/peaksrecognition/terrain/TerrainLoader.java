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
        short[][] heightMap = loadHgtFile("srtm_data/N49E020.hgt");
        generateVertices(heightMap);
        heightMap = null;
        generateTriangles();
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
        Log.d("Moje", "verictes length= " + vertices.length);
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
        Log.d("Moje", "dUpa");
        int trianglesNum = ((1201 - 1) * (1201 - 1) *2);
        Log.d("Moje", "dUpa");
        int[] triangles = new int[trianglesNum * 3];

        Log.d("Moje", "dUpa");
        Log.d("Moje", "dUpatutaj");
        int trianglesIndex = 0;
        int index = 0;
        int xMax = 1201 - 1;
        int zMax = 1201 - 1;
        Log.d("Moje", "dUpa");
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
