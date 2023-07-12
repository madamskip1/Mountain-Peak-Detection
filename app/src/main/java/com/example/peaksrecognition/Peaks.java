package com.example.peaksrecognition;

import android.content.Context;
import android.content.res.AssetManager;
import android.opengl.GLES30;

import com.example.peaksrecognition.mainopengl.ShaderProgram;
import com.example.peaksrecognition.terrain.TerrainData;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

public class Peaks {
    private final TerrainData terrainData;
    private final ScreenManager screenManager;
    private final int positionAttribute;
    private final double[] scale;
    private final int offsetX;
    private final int offsetZ;
    private final int rows;
    private final int cols;
    private final Vector<Peak> peaks;

    public Peaks(Context context, CoordsManager coordsManager, TerrainData terrainData, ScreenManager screenManager, ShaderProgram shaderProgram) {
        this.terrainData = terrainData;
        this.screenManager = screenManager;
        offsetX = terrainData.getOffset()[0];
        offsetZ = terrainData.getOffset()[2];
        rows = terrainData.getRows();
        cols = terrainData.getCols();
        scale = terrainData.getScale();

        int shader = shaderProgram.getShaderProgram();
        positionAttribute = GLES30.glGetAttribLocation(shader, "vPosition");

        peaks = new Vector<>();
        preparePeaks(context, coordsManager);
    }

    public Vector<Peak> getVisiblePeaks() {
        Vector<Peak> passedFrutsum = frustumTest(peaks);
        return occlusionTest(passedFrutsum);
    }

    private Vector<Peak> frustumTest(Vector<Peak> peaks) {
        Vector<Peak> peaksPassedFrustumTest = new Vector<>();
        for (Peak peak : peaks) {
            float[] screenPoint = screenManager.getScreenPosition(peak.vertexCoords[0], peak.vertexCoords[1], peak.vertexCoords[2]);

            if (screenManager.checkIfPointOnScreen(screenPoint)) {
                peaksPassedFrustumTest.add(peak);
                peak.screenPosition = screenPoint;
            }
        }

        return peaksPassedFrustumTest;
    }

    private Vector<Peak> occlusionTest(Vector<Peak> peaks) {
        int peaksLength = peaks.size();
        int[] queryIds = new int[peaksLength];
        int[] queryResults = new int[peaksLength];
        GLES30.glGenQueries(peaksLength, queryIds, 0);
        Vector<Peak> peaksPassedOcclusionTest = new Vector<>();
        for (int i = 0; i < peaksLength; ++i) {
            Peak peak = peaks.get(i);
            GLES30.glBeginQuery(GLES30.GL_ANY_SAMPLES_PASSED, queryIds[i]);
            GLES30.glVertexAttrib3f(positionAttribute, peak.vertexCoords[0], peak.vertexCoords[1], peak.vertexCoords[2]);
            GLES30.glDrawArrays(GLES30.GL_POINTS, 0, 1);
            GLES30.glEndQuery(GLES30.GL_ANY_SAMPLES_PASSED);
            GLES30.glGetQueryObjectuiv(queryIds[i], GLES30.GL_QUERY_RESULT, queryResults, i);
            if (queryResults[i] != 0) {
                peaksPassedOcclusionTest.add(peak);
            }
        }

        return peaksPassedOcclusionTest;
    }

    private void preparePeaks(Context context, CoordsManager coordsManager) {
        CSVReader csvReader = getPeaksReader(context);

        String[] nextLine;
        try {
            while ((nextLine = csvReader.readNext()) != null) {
                String name = nextLine[0];
                double latitude = Double.parseDouble(nextLine[1]);
                double longitude = Double.parseDouble(nextLine[2]);
                int dem = (int) Double.parseDouble(nextLine[3]);
                int elevation = Integer.parseInt(nextLine[4]);
                float[] vertexCoords = getPeakVertexCoords(coordsManager, latitude, longitude);
                if (vertexCoords[0] != -1) {
                    vertexCoords[1] -= 0.000001;
                    peaks.add(new Peak(name, latitude, longitude, dem, elevation, vertexCoords));
                }
            }
        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException(e);
        }
    }

    private CSVReader getPeaksReader(Context context) {
        AssetManager assetManager = context.getAssets();
        InputStreamReader inputStreamReader;
        try {
            inputStreamReader = new InputStreamReader(assetManager.open("peaks_data/havran.csv"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        CSVParser parser = new CSVParserBuilder().withSeparator(';').build();
        return new CSVReaderBuilder(inputStreamReader).withCSVParser(parser).build();
    }

    private float[] getPeakVertexCoords(CoordsManager coordsManager, double latitude, double longitude) {
        double[] localCoords = coordsManager.convertGeoToLocalCoords(latitude, longitude, 0.0);
        double xLocal = localCoords[0] / scale[0];
        double zLocal = localCoords[2] / scale[2];

        int xLocalInt = (int) Math.round(xLocal);
        int zLocalInt = (int) Math.round(zLocal);
        xLocalInt -= offsetX;
        zLocalInt -= offsetZ;

        if (xLocalInt < 0 || xLocalInt >= rows || zLocalInt < 0 || zLocalInt >= cols) {
            return new float[]{-1.0f, -1.0f, -1.0f};
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
                double[] vertexCoords = terrainData.getVertexCoords(vertex_num);

                if (vertexCoords[1] > max_y) {
                    max_x = vertexCoords[0];
                    max_y = vertexCoords[1];
                    max_z = vertexCoords[2];
                }
            }
        }

        return new float[]{(float) max_x, (float) max_y, (float) max_z};
    }

    public static class Peak {
        public String name;
        public double latitude;
        public double longitude;
        public int dem;
        public int elevation;
        public float[] vertexCoords;
        public float[] screenPosition;

        Peak(String name, double latitude, double longitude, int dem, int elevation, float[] vertexCoords) {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
            this.dem = dem;
            this.elevation = elevation;
            this.vertexCoords = vertexCoords;
        }

        public void writeNameOnImage(Mat image, int screenHeight, int offsetBottom, int offsetTop)
        {
            final Scalar lineColor = new Scalar(0, 0, 255);
            final Scalar textColor = new Scalar(0, 0, 255);
            final int lineThickness = 5;
            final int textThickness = 1;

            int x = (int) screenPosition[0];
            int y = screenHeight - (int) screenPosition[1];
            int yName = (int) (Math.random() * (y - offsetBottom)) + offsetTop;
            Imgproc.line(image, new Point(screenPosition[0], screenPosition[1]), new Point(x, yName), lineColor, lineThickness);
            Imgproc.putText(image, name, new Point(x + 5, yName), Imgproc.FONT_HERSHEY_SIMPLEX, 1.0, textColor, textThickness);
        }
    }
}

