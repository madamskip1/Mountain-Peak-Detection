package org.pw.masterthesis.peaksrecognition.activities;

import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.pw.masterthesis.peaksrecognition.Config;
import org.pw.masterthesis.peaksrecognition.Peaks;
import org.pw.masterthesis.peaksrecognition.R;
import org.pw.masterthesis.peaksrecognition.mainopengl.Camera;
import org.pw.masterthesis.peaksrecognition.managers.CoordsManager;
import org.pw.masterthesis.peaksrecognition.managers.LocationManager;
import org.pw.masterthesis.peaksrecognition.managers.RotationManager;
import org.pw.masterthesis.peaksrecognition.renderer.OffScreenRenderer;
import org.pw.masterthesis.peaksrecognition.renderer.Renderer;

import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

public class DisplayRenderLiveActivity extends AppCompatActivity {
    private Location curLocation;
    private float[] curRotation;
    private Renderer renderer;
    private Camera camera;
    private CoordsManager coordsManager;
    private ImageView imageView;
    private RotationManager rotationManager;
    private LocationManager locationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_render_live);
        imageView = findViewById(R.id.displayRenderLiveImageView);
        prepareLocationManager();
    }

    private void go() {
        long targetTime = 1000 / 30; // 30 fps, so 1000 milliseconds / 30 frames
        final long[] startTime = new long[1];
        final long[] elapsedTime = new long[1];

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                startTime[0] = System.currentTimeMillis();
                double[] cameraCoords = coordsManager.convertGeoToLocalCoords(curLocation.getLatitude(), curLocation.getLongitude(), curLocation.getAltitude());
                camera.setPosition(cameraCoords[0], cameraCoords[1], cameraCoords[2]);
                camera.setAngles(curRotation[0], curRotation[1], curRotation[2]);
                renderer.render();
                Vector<Peaks.Peak> visiblePeaks = renderer.getPeaks().getVisiblePeaks();
                Mat renderedImage = renderer.getRenderedMat();

                Bitmap bitmap = Bitmap.createBitmap(renderedImage.cols(), renderedImage.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(renderedImage, bitmap);
                imageView.setImageBitmap(bitmap);

                elapsedTime[0] = System.currentTimeMillis() - startTime[0];

                long sleepTime = targetTime - elapsedTime[0];

                handler.post(this);
            }
        });
    }

    private void prepareLocationManager() {
        locationManager = new LocationManager(this);
        locationManager.askForLocationPermissions(this, this);
        locationManager.setLocationUpdateCallback(new LocationManager.LocationListener() {
            @Override
            public void onLocationUpdate(Location location) {
                curLocation = location;
            }

            @Override
            public void onLocationAvailabilityChange(boolean isAvailable) {
                prepareRotationManager();
            }
        });
        locationManager.requestLocationUpdate(location -> {
            curLocation = location;
            afterLocationLoaded();
        });

        locationManager.start();
    }

    private void afterLocationLoaded() {
        prepareRotationManager();
    }

    private void afterRotationLoaded() {
        prepareOffScreenRendered();
        camera = renderer.getCamera();
        go();
    }


    private void prepareRotationManager() {
        rotationManager = new RotationManager(this);
        AtomicBoolean rotationLoaded = new AtomicBoolean(false);
        rotationManager.addRotationListener(rotationVector -> {
            curRotation = rotationVector;
            if (!rotationLoaded.get()) {
                rotationLoaded.set(true);
                afterRotationLoaded();
            }

        });
        rotationManager.start();
    }

    private void prepareOffScreenRendered() {

        Config config = new Config();
        config.initObserverLocation = new double[]{curLocation.getLatitude(), curLocation.getLongitude(), curLocation.getAltitude()};
        float[] observerRotation = new float[]{144.31152f, 2.3836904f, -2.0597333f};
        curRotation = observerRotation;
        config.initObserverRotation = curRotation;
        config.maxDistance = 30.0;
        config.minDistance = 0.001;
        config.FovVertical = 66.0f;
        config.simplifyFactor = 3;
        config.initHgtSize = 3601;

        renderer = new OffScreenRenderer(this, config);
        coordsManager = renderer.getCoordsManager();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null)
            locationManager.stop();
        if (rotationManager != null)
            rotationManager.stop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (locationManager != null)
            locationManager.stop();
        if (rotationManager != null)
            rotationManager.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (locationManager != null)
            locationManager.start();
        if (rotationManager != null)
            rotationManager.start();
    }
}