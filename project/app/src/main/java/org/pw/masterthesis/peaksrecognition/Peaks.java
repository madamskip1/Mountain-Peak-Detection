package org.pw.masterthesis.peaksrecognition;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.opengl.GLES30;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import org.pw.masterthesis.peaksrecognition.mainopengl.ShaderProgram;
import org.pw.masterthesis.peaksrecognition.terrain.TerrainData;

import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
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
    private Paint paint;

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
        preparePaintForDrawingText();
    }

    public Vector<Peak> getVisiblePeaks() {
        Vector<Peak> passedFrutsum = frustumTest(peaks);
        //return occlusionTest(passedFrutsum);
        return passedFrutsum;
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

        String[] listOfPeaksFiles = getListOfPeaksFiles(coordsManager);
        String[] nextLine;
        try {
            for (String peaksFilePath : listOfPeaksFiles) {
                CSVReader csvReader = getPeaksReader(context, peaksFilePath);
                while ((nextLine = csvReader.readNext()) != null) {
                    double latitude = Double.parseDouble(nextLine[1]);
                    double longitude = Double.parseDouble(nextLine[2]);

                    float[] vertexCoords = getPeakVertexCoords(coordsManager, latitude, longitude);
                    if (vertexCoords[0] != -1) {
                        String name = nextLine[0];
                        int dem = (int) Double.parseDouble(nextLine[3]);
                        int elevation = Integer.parseInt(nextLine[4]);
                        double distance = coordsManager.calcDistanceObserverToPoint(latitude, longitude);
                        vertexCoords[1] -= 0.000001;
                        peaks.add(new Peak(name, latitude, longitude, dem, elevation, vertexCoords, distance));
                    }
                }
            }
        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException(e);
        }
    }

    private CSVReader getPeaksReader(Context context, String peaksFilePath) {
        AssetManager assetManager = context.getAssets();
        InputStreamReader inputStreamReader;
        try {
            inputStreamReader = new InputStreamReader(assetManager.open(peaksFilePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        CSVParser parser = new CSVParserBuilder().withSeparator(';').build();
        return new CSVReaderBuilder(inputStreamReader).withCSVParser(parser).build();
    }

    private String[] getListOfPeaksFiles(CoordsManager coordsManager) {
        final String peaksDir = "peaks_data/";
        int[] latitudeRange = coordsManager.getLatitudeRange();
        int[] longitudeRange = coordsManager.getLongitudeRange();
        int numOfPeaksFiles = (latitudeRange[1] - latitudeRange[0]) * (longitudeRange[1] - longitudeRange[0]);
        String[] listOfPeaksFiles = new String[numOfPeaksFiles];
        int counter = 0;

        for (int latitude = latitudeRange[0]; latitude < latitudeRange[1]; ++latitude) {
            for (int longitude = longitudeRange[0]; longitude < longitudeRange[1]; ++longitude) {
                String filePath = peaksDir + latitude + "_" + longitude + ".csv";
                listOfPeaksFiles[counter] = filePath;
                ++counter;
            }
        }

        return listOfPeaksFiles;
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

    private void preparePaintForDrawingText() {
        int textSize = 30;
        int textColor = Color.MAGENTA;
        int fontStyle = Typeface.NORMAL;
        String fontName = Typeface.DEFAULT_BOLD.toString();

        paint = new Paint();
        paint.setTextSize(textSize);
        paint.setColor(textColor);
        paint.setTypeface(Typeface.create(fontName, fontStyle));
        paint.setStrokeWidth(2);
    }

    public Bitmap drawPeakNames(Bitmap bitmap, Vector<Peak> peaks) {
        final float rotationAngle = -45.0f;
        DecimalFormat decimalFormat = new DecimalFormat("#0.0");

        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true); // mutable copy
        Canvas canvas = new Canvas(bitmap);

        for (Peak peak : peaks) {
            int x = peak.realImagePosition[0];
            int y = peak.realImagePosition[1];
            int yText = y - 30;
            String text = peak.name + " (" + decimalFormat.format(peak.distance) + " km)";
            canvas.save();
            canvas.drawLine(x, y, x, yText, paint);
            canvas.rotate(rotationAngle, x, yText);
            canvas.drawText(text, x, yText, paint);
            canvas.restore();
        }
        return bitmap;
    }

    public static class Peak {
        public String name;
        public double latitude;
        public double longitude;
        public int dem;
        public int elevation;
        public float[] vertexCoords;
        public float[] screenPosition;
        public int[] realImagePosition;
        public double distance;

        Peak(String name, double latitude, double longitude, int dem, int elevation, float[] vertexCoords, double distance) {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
            this.dem = dem;
            this.elevation = elevation;
            this.vertexCoords = vertexCoords;
            this.distance = distance;
        }
    }
}

