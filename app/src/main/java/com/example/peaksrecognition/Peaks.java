package com.example.peaksrecognition;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Shader;
import android.opengl.GLES30;
import android.util.Log;

import com.example.peaksrecognition.mainopengl.ShaderProgram;
import com.example.peaksrecognition.terrain.TerrainModel;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Vector;

public class Peaks {
    private final CoordsManager coordsManager;
    private final TerrainModel terrainModel;
    private final ScreenManager screenManager;
    private final int positionAttribute;
    private final double[] scale = new double[] { 0.09266243887046562, 0.001, 0.06015208526040229 };

    private int xOffset = 469;
    private int zOffset = 799;
    private int rows = 649;
    private int cols = 999;

    private int vertexArrayId;
    private int vertexBufferId;
    private int bufferId;
    private Vector<Peak> peaks;
    public Peaks(Context context, CoordsManager coordsManager, TerrainModel terrainModel, ScreenManager screenManager, ShaderProgram shaderProgram) {
        this.coordsManager = coordsManager;
        this.terrainModel = terrainModel;
        this.screenManager = screenManager;
        int shader = shaderProgram.getShaderProgram();
        positionAttribute = GLES30.glGetAttribLocation(shader, "vPosition");

        AssetManager assetManager = context.getAssets();
        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(assetManager.open("peaks_data/test.csv"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        CSVParser parser = new CSVParserBuilder().withSeparator(';').build();
        CSVReader csvReader = new CSVReaderBuilder(inputStreamReader).withCSVParser(parser).build();
        peaks = new Vector<>();

        String[] nextLine;
        try {
            while ((nextLine = csvReader.readNext()) != null) {
                String name = nextLine[0];
                double latitude = Double.parseDouble(nextLine[1]);
                double longitude = Double.parseDouble(nextLine[2]);
                int dem = (int) Double.parseDouble(nextLine[3]);
                int elevation = Integer.parseInt(nextLine[4]);
                float[] vertexCoords = getPeakVertexCoords(latitude, longitude);
                vertexCoords[1] += 0.000001;
                peaks.add(new Peak(name, latitude, longitude, dem, elevation, vertexCoords));
            }
        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException(e);
        }

        int[] bufferIds = new int[1];
        GLES30.glGenBuffers(1, bufferIds, 0);
        bufferId = bufferIds[0];


    }

    public void test()
    {
        Log.d("moje", "test");
        Vector<Peak> peaksPassedFrustumTest = frustumTest(peaks);
            Log.d("moje", "name " + peaksPassedFrustumTest.size());
        //occlusionTest(peaksPassedFrustumTest);
        dumbOcclusion(peaksPassedFrustumTest);
    }

    private float[] getPeakVertexCoords(double latitude, double longitude)
    {
        double[] localCoords = coordsManager.convertGeoToLocalCoords(latitude, longitude, 0.0);
        double xLocal = localCoords[0] / scale[0];
        double zLocal = localCoords[2] / scale[2];

        int xLocalInt = (int) Math.round(xLocal);
        int zLocalInt = (int) Math.round(zLocal);
        xLocalInt -= xOffset;
        zLocalInt -= zOffset;

        if (xLocalInt < 0 || xLocalInt >= rows || zLocalInt < 0 || zLocalInt >= cols)
        {
            return new float[] { -1.0f, -1.0f, -1.0f };
        }

        double max_x = 0.0;
        double max_y = 0.0;
        double max_z = 0.0;
        int start_x = Math.max(0, xLocalInt - 1);
        int start_z = Math.max(0, zLocalInt - 1);
        int end_x = Math.min(xLocalInt + 1, rows - 1);
        int end_z = Math.min(zLocalInt + 1, cols - 1);

        for (int x_loop = start_x; x_loop <= end_x; x_loop++) {
            for (int z_loop = start_z; z_loop <= end_z; z_loop++) {
                int vertex_num = x_loop * cols + z_loop;
                double[] vertexCoords = terrainModel.getVertexCoords(vertex_num);

                if (vertexCoords[1] > max_y)
                {
                    max_x = vertexCoords[0];
                    max_y = vertexCoords[1];
                    max_z = vertexCoords[2];
                }
            }
        }

        return new float[] { (float) max_x, (float) max_y, (float) max_z };
    }

    private Vector<Peak> frustumTest(Vector<Peak> peaks)
    {
        Log.d("moje", "Before " + peaks.size());
        Vector<Peak> peaksPassedFrustumTest = new Vector<>();
        for (Peak peak : peaks)
        {
            float[] screenPoint = screenManager.getScreenPosition((float) peak.vertexCoords[0], (float) peak.vertexCoords[1], (float) peak.vertexCoords[2]);
            if (screenManager.checkIfPointOnScreen(screenPoint))
            {
                peaksPassedFrustumTest.add(peak);
                peak.screenPosition = screenPoint;
            }
        }
        Log.d("moje", "frustum " + peaksPassedFrustumTest.size());
        return peaksPassedFrustumTest;
    }

    private void dumbOcclusion(Vector<Peak> peaks)
    {
        Log.d("moje", "name " + peaks.get(0).name);
        float[] vertex = peaks.get(0).vertexCoords;
        vertex[1] += 1.0;
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, bufferId);

        // Transfer data to the buffer
        FloatBuffer floatBuffer = FloatBuffer.allocate(3);
        floatBuffer.put(vertex).position(0);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, floatBuffer.capacity() * Float.BYTES, floatBuffer, GLES30.GL_STATIC_DRAW);

        // Unbind the buffer
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);

       // GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

        // Bind the vertex buffer
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vertexBufferId);

        // Specify the vertex position attribute
        GLES30.glEnableVertexAttribArray(positionAttribute);
        GLES30.glVertexAttribPointer(positionAttribute, 3, GLES30.GL_FLOAT, false, 0, 0);

        // Perform the occlusion test
        int[] occlusionResult = new int[1];
        GLES30.glGenQueries(1, occlusionResult, 0);
        GLES30.glBeginQuery(GLES30.GL_ANY_SAMPLES_PASSED, occlusionResult[0]);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_FAN, 0, 1);
        GLES30.glEndQuery(GLES30.GL_ANY_SAMPLES_PASSED);

        // Check if the occlusion test passed
        IntBuffer queryResult = IntBuffer.allocate(1);
        GLES30.glGetQueryObjectuiv(occlusionResult[0], GLES30.GL_QUERY_RESULT, queryResult);
        int occlusionPassed = queryResult.get(0);
        Log.d("moje", "dupa1");
        if (occlusionPassed != 0) {
            // Draw the vertex only if it passes the occlusion test
            Log.d("moje", "dupa2");
            GLES30.glDrawArrays(GLES30.GL_TRIANGLE_FAN, 0, 1);
        }

        // Clean up
        GLES30.glDisableVertexAttribArray(positionAttribute);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
        GLES30.glDeleteQueries(1, occlusionResult, 0);
    }

    private Vector<Peak> occlusionTest(Vector<Peak> peaks)
    {
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        GLES30.glEnable(GLES30.GL_SAMPLE_ALPHA_TO_COVERAGE);
        int peaksLength = peaks.size();
        Vector<Peak> peaksPassedOcclusionTest = new Vector<>();
        int[] queryIds = new int[peaksLength];
        int[] queryResults = new int[peaksLength];
        GLES30.glGenQueries(peaksLength, queryIds, 0);
        int queryId = 0;
        GLES30.glBindVertexArray(vertexArrayId);
        GLES30.glEnableVertexAttribArray(positionAttribute);
        // Set vertex attribute pointer
        GLES30.glVertexAttribPointer(positionAttribute, 3, GLES30.GL_FLOAT, false, 0, 0);
        for (Peak peak : peaks)
        {
           GLES30.glBeginQuery(GLES30.GL_ANY_SAMPLES_PASSED, queryIds[queryId]);
            GLES30.glVertexAttrib3f(positionAttribute, peak.vertexCoords[0], peak.vertexCoords[1], peak.vertexCoords[2]);
            GLES30.glDrawArrays(GLES30.GL_POINTS, 0, 1);
            GLES30.glEndQuery(GLES30.GL_ANY_SAMPLES_PASSED);
            //GLES30.glGetQueryObjectuiv(queryIds[queryId], GLES30.GL_QUERY_RESULT_AVAILABLE, queryResults, queryId);

            /*
            GLES30.glBeginQuery(GLES30.GL_ANY_SAMPLES_PASSED, queryIds[queryId]);

            // Bind vertex array


            // Bind vertex buffer
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vertexBufferId);

            // Specify vertex data
            peak.vertexCoords[1] += 1.0;
            FloatBuffer vertexBuffer = FloatBuffer.wrap(peak.vertexCoords);
            GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vertexBuffer.capacity() * Float.BYTES, vertexBuffer, GLES30.GL_STATIC_DRAW);

            // Enable vertex attribute




            // Draw the points
            GLES30.glDrawArrays(GLES30.GL_POINTS, 0, 1);



            // End query
            GLES30.glEndQuery(GLES30.GL_ANY_SAMPLES_PASSED);
            //GLES30.glGetQueryObjectuiv(queryIds[0], GLES30.GL_QUERY_RESULT, queryResults, 0);

             */
            ++queryId;
        }
        Log.d("moje", "dupa1");
        // Disable vertex attribute
        GLES30.glDisableVertexAttribArray(positionAttribute);

        // Unbind vertex buffer
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);

        // Unbind vertex array
        GLES30.glBindVertexArray(0);
        int[] queryAv = new int[peaksLength];
        boolean allResultsAvailable = false;
        while (!allResultsAvailable) {

            allResultsAvailable = true;
            for (int i = 0; i < peaksLength; i++) {
                GLES30.glGetQueryObjectuiv(queryIds[i], GLES30.GL_QUERY_RESULT_AVAILABLE, queryAv, i);

                if (queryAv[i] == GLES30.GL_FALSE) {
                    allResultsAvailable = false;
                    break;
                }
            }

        }
        Log.d("moje", "dupa2");
        for (int i = 0; i < peaksLength; ++i)
        {
           // Log.d("moje", "loop " + i + " " + peaksLength);
            GLES30.glGetQueryObjectuiv(queryIds[i], GLES30.GL_QUERY_RESULT, queryResults, i);
           // Log.d("moje", "is " + queryResults[i]);
            if (queryResults[i] != 0) {
                peaksPassedOcclusionTest.add(peaks.get(i));
            }
        }
        Log.d("moje", "dupa3");
        GLES30.glDeleteQueries(peaksLength, queryIds, 0);
        Log.d("moje", "peaksPassedOcclusionTest " + peaksPassedOcclusionTest.size());
        return peaksPassedOcclusionTest;
    }

    private class Peak
    {
        public String name;
        public double latitude;
        public double longitude;
        public int dem;
        public int elevation;
        public float[] vertexCoords;
        public float[] screenPosition;

        Peak(String name, double latitude, double longitude, int dem, int elevation, float[] vertexCoords)
        {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
            this.dem = dem;
            this.elevation = elevation;
            this.vertexCoords = vertexCoords;
        }
    }
}

