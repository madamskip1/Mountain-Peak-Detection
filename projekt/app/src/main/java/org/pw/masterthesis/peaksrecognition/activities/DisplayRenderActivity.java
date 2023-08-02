package org.pw.masterthesis.peaksrecognition.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.pw.masterthesis.peaksrecognition.Config;
import org.pw.masterthesis.peaksrecognition.DeviceOrientation;
import org.pw.masterthesis.peaksrecognition.Peaks;
import org.pw.masterthesis.peaksrecognition.R;
import org.pw.masterthesis.peaksrecognition.edgedetectors.CannyEdgeDetector;
import org.pw.masterthesis.peaksrecognition.renderer.OffScreenRenderer;
import org.pw.masterthesis.peaksrecognition.renderer.Renderer;

import java.util.Vector;

public class DisplayRenderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_render);
        renderToLayout();
    }

    private void renderToLayout() {
        Config config = prepareConfig();
        Renderer renderer = new OffScreenRenderer(this, config);
        renderer.render();
        Mat renderedImage = renderer.getRenderedMat();

        if (getIntent().getBooleanExtra("edges", false)) {
            renderedImage = detectEdges(renderedImage);
        }

        Bitmap bitmap = Bitmap.createBitmap(renderedImage.cols(), renderedImage.rows(), Bitmap.Config.ARGB_8888);

        if (getIntent().getBooleanExtra("peaks", false)) {
            Peaks peaks = renderer.getPeaks();
            Vector<Peaks.Peak> visiblePeaks = peaks.getVisiblePeaks();
            bitmap = peaks.drawPeakNames(bitmap, visiblePeaks);
        }

        Utils.matToBitmap(renderedImage, bitmap);
        ImageView imageView = findViewById(R.id.displayRenderImageView);
        imageView.setImageBitmap(bitmap);
    }

    private Mat detectEdges(Mat image) {
        CannyEdgeDetector cannyEdgeDetector = new CannyEdgeDetector(20, 100);
        return cannyEdgeDetector.detect(image);
    }

    private Config prepareConfig() {
        Intent intent = getIntent();

        double latitude = intent.getDoubleExtra("latitude", 0.0);
        double longitude = intent.getDoubleExtra("longitude", 0.0);
        double altitude = intent.getDoubleExtra("altitude", 0.0);
        float yaw = intent.getFloatExtra("yaw", 0.0f);
        float pitch = intent.getFloatExtra("pitch", 0.0f);
        float roll = intent.getFloatExtra("roll", 0.0f);
        double minDistance = intent.getDoubleExtra("minDistance", 0.0);
        double maxDistance = intent.getDoubleExtra("maxDistance", 0.0);

        Config config = new Config();
        config.initObserverLocation = new double[]{latitude, longitude, altitude};
        config.initObserverRotation = new float[]{yaw, pitch, roll};
        config.maxDistance = maxDistance;
        config.minDistance = minDistance;
        config.FovVertical = 66.0f;
        config.simplifyFactor = 3;
        config.initHgtSize = 3601;
        config.deviceOrientation = DeviceOrientation.PORTRAIT;
        config.width = 768;
        config.height = 1024;

        return config;
    }
}