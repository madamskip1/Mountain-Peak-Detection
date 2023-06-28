package com.example.peaksrecognition;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageProxy;

import com.example.peaksrecognition.devicecamera.FrameAnalyser;
import com.example.peaksrecognition.devicecamera.ImageProxyToMatConverter;
import com.example.peaksrecognition.mainopengl.OffScreenRenderer;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.util.concurrent.atomic.AtomicBoolean;

public class BlendRenderAndLive extends FrameAnalyser {
    static {
        OpenCVLoader.initDebug();
    }

    ImageView imageView;
    private Location curLocation;
    private float[] curRotation;
    private OffScreenRenderer offScreenRenderer;
    private Camera camera;
    private CoordsManager coordsManager;
    private RotationManager rotationManager;
    private LocationManager locationManager;
    private final AppCompatActivity parentActivity;
    private final Context parentContext;

    public BlendRenderAndLive(AppCompatActivity activity, Context context, ImageView imageView) {
        super(activity);
        parentActivity = activity;
        parentContext = context;
        this.imageView = imageView;
    }

    public void prepareAndStart() {
        prepare();
    }

    @Override
    protected void analyse(ImageProxy image) {
        Mat rgbaMat = ImageProxyToMatConverter.rgba(image);
        double[] cameraCoords = coordsManager.convertGeoToLocalCoords(curLocation.getLatitude(), curLocation.getLongitude(), curLocation.getAltitude());
        camera.setPosition(cameraCoords[0], cameraCoords[1], cameraCoords[2]);
        camera.setAngles(curRotation[0], curRotation[1], curRotation[2]);
        offScreenRenderer.render();
        Mat renderMat = offScreenRenderer.getRenderedMat();

        Mat blended = new Mat();
        blended = rgbaMat;
        Core.addWeighted(rgbaMat, 0.5, renderMat, 0.5, 0.0, blended);
        Bitmap bitmap = Bitmap.createBitmap(blended.cols(), blended.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(blended, bitmap);
        imageView.setImageBitmap(bitmap);
    }

    private void prepare() {
        prepareLocationManager();
    }

    private void prepareLocationManager() {
        locationManager = new LocationManager(parentContext);
        locationManager.askForLocationPermissions(parentActivity, parentContext);
        locationManager.setLocationUpdateCallback(new LocationManager.LocationListener() {
            @Override
            public void onLocationUpdate(Location location) {
                curLocation = location;
            }

            @Override
            public void onLocationAvailabilityChange(boolean isAvailable) {
            }
        });
        locationManager.requestLocationUpdate(location -> {
            curLocation = location;
            prepareRotationManager();
        });

        locationManager.start();
    }

    private void prepareRotationManager() {
        rotationManager = new RotationManager(parentContext);
        AtomicBoolean rotationLoaded = new AtomicBoolean(false);
        rotationManager.addRotationListener(rotationVector -> {
            curRotation = rotationVector;
            if (!rotationLoaded.get()) {
                rotationLoaded.set(true);
                prepareRenderer();
            }

        });
        rotationManager.start();
    }

    private void prepareRenderer() {
        FieldOfView fieldOfView = new FieldOfView(parentContext);
        fieldOfView.setDeviceOrientation(FieldOfView.DeviceOrientation.LANDSCAPE);

        Config config = new Config();
        config.initObserverLocation = new double[]{curLocation.getLatitude(), curLocation.getLongitude(), curLocation.getAltitude()};
        config.initObserverRotation = curRotation;
        config.maxDistance = 30.0;
        config.minDistance = 0.001;
        config.FovVertical = (float) fieldOfView.getVerticalFOV();
        config.simplifyFactor = 3;
        config.initHgtSize = 3601;
        config.deviceOrientation = Config.DeviceOrientation.LANDSCAPE;

        offScreenRenderer = new OffScreenRenderer(parentContext, config);
        coordsManager = offScreenRenderer.getCoordsManager();
        camera = offScreenRenderer.getCamera();
        start();
    }


    public void pause() {
        if (locationManager != null)
            locationManager.stop();
        if (rotationManager != null)
            rotationManager.stop();
    }

    public void resume() {
        if (locationManager != null)
            locationManager.start();
        if (rotationManager != null)
            rotationManager.start();
    }

    public void destroy() {
        if (locationManager != null)
            locationManager.stop();
        if (rotationManager != null)
            rotationManager.stop();
    }
}
