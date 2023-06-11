package com.example.peaksrecognition.activities;

import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.peaksrecognition.Camera;
import com.example.peaksrecognition.Config;
import com.example.peaksrecognition.LocationManager;
import com.example.peaksrecognition.R;
import com.example.peaksrecognition.RotationManager;
import com.example.peaksrecognition.mainopengl.OffScreenRenderer;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.util.concurrent.atomic.AtomicBoolean;

public class DisplayRenderLiveActivity extends AppCompatActivity {
    private Location curLocation;
    private float[] curRotation;
    private OffScreenRenderer offScreenRenderer;
    private Camera camera;
    private ImageView imageView;


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
                camera.setPosition(curLocation.getLatitude(), curLocation.getLongitude(), curLocation.getAltitude());
                camera.setAngles(curRotation[0], curRotation[1], curRotation[2]);
                offScreenRenderer.render();

                Mat renderedImage = offScreenRenderer.getRenderedMat();
                Bitmap bitmap = Bitmap.createBitmap(renderedImage.cols(), renderedImage.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(renderedImage, bitmap);
                imageView.setImageBitmap(bitmap);

                elapsedTime[0] = System.currentTimeMillis() - startTime[0];
                long sleepTime = targetTime - elapsedTime[0];

                if (sleepTime > 0) {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                handler.post(this);
            }
        });
    }

    private void prepareLocationManager() {
        LocationManager locationManager = new LocationManager(this);
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
        camera = offScreenRenderer.getCamera();
        go();
    }


    private void prepareRotationManager() {
        RotationManager rotationManager = new RotationManager(this);
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
        config.initObserverRotation = curRotation;
        config.maxDistance = 30.0;
        config.minDistance = 0.001;
        config.FOVHorizontal = 66.0f;
        config.simplifyFactor = 3;
        config.initHgtSize = 3601;

        offScreenRenderer = new OffScreenRenderer(this, config);
    }

}