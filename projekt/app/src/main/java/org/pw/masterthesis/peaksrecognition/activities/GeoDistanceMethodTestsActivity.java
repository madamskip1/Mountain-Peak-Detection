package org.pw.masterthesis.peaksrecognition.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.pw.masterthesis.peaksrecognition.R;
import org.pw.masterthesis.peaksrecognition.geodistance.EquirectangularApproximation;
import org.pw.masterthesis.peaksrecognition.geodistance.GeoDistance;
import org.pw.masterthesis.peaksrecognition.geodistance.Vincent;

public class GeoDistanceMethodTestsActivity extends AppCompatActivity {
    private final int ITERATIONS_TIME_TEST = 10000000;
    private final double VINCENT_CONTROL_ITERATION_LIMIT = 1000.0;
    private final double LATITUDE_1 = 52.237049;
    private final double LONGITUDE_1 = 21.017532;
    private final double LATITUDE_2 = 50.049683;
    private final double LONGITUDE_2 = 19.944544;

    private final GeoDistance controlGeoDistanceCalculator;
    private final GeoDistance testedGeoDistanceCalculator;

    public GeoDistanceMethodTestsActivity() {
        controlGeoDistanceCalculator = new Vincent(VINCENT_CONTROL_ITERATION_LIMIT);
        testedGeoDistanceCalculator = new EquirectangularApproximation();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_distance_method_tests);
        distanceTest();
        timeTest();
    }

    private void distanceTest() {
        TextView controlDistanceTextView = findViewById(R.id.control_test_distance);
        TextView testedDistanceTextView = findViewById(R.id.main_test_distance);
        TextView distanceDeltaTextView = findViewById(R.id.test_distance_delta);
        TextView distanceDeltaPercentTextView = findViewById(R.id.test_distance_delta_percent);

        double controlDistance = controlGeoDistanceCalculator.calcDistance(LATITUDE_1, LONGITUDE_1, LATITUDE_2, LONGITUDE_2);
        double testedDistance = testedGeoDistanceCalculator.calcDistance(LATITUDE_1, LONGITUDE_1, LATITUDE_2, LONGITUDE_2);

        double distanceDelta = Math.abs(controlDistance - testedDistance);
        double distanceDeltaPercent = distanceDelta / controlDistance;

        controlDistanceTextView.setText(String.valueOf(controlDistance));
        testedDistanceTextView.setText(String.valueOf(testedDistance));
        distanceDeltaTextView.setText(String.valueOf(distanceDelta));
        distanceDeltaPercentTextView.setText(String.valueOf(distanceDeltaPercent));
    }

    private void timeTest() {
        TextView controlTimeTextView = findViewById(R.id.control_test_time);
        TextView testedTimeTextView = findViewById(R.id.main_test_time);
        TextView deltaTimeTextView = findViewById(R.id.test_time_delta);
        TextView deltaPercentTimeTextView = findViewById(R.id.test_time_delta_percent);

        long startTime = System.nanoTime();
        for (int i = 0; i < ITERATIONS_TIME_TEST; i++) {
            double controlValue = controlGeoDistanceCalculator.calcDistance(LATITUDE_1, LONGITUDE_1, LATITUDE_2, LONGITUDE_2);
        }
        long endTime = System.nanoTime();
        long controlElapsedTime = endTime - startTime;

        startTime = System.nanoTime();
        for (int i = 0; i < ITERATIONS_TIME_TEST; i++) {
            double testedValue = testedGeoDistanceCalculator.calcDistance(LATITUDE_1, LONGITUDE_1, LATITUDE_2, LONGITUDE_2);
        }
        endTime = System.nanoTime();
        long testedElapsedTime = endTime - startTime;

        long deltaTime = controlElapsedTime - testedElapsedTime;
        double deltaPercentTime = (double) deltaTime / (double) controlElapsedTime;

        controlTimeTextView.setText(String.valueOf(controlElapsedTime));
        testedTimeTextView.setText(String.valueOf(testedElapsedTime));
        deltaTimeTextView.setText(String.valueOf(deltaTime));
        deltaPercentTimeTextView.setText(String.valueOf(deltaPercentTime));
    }
}