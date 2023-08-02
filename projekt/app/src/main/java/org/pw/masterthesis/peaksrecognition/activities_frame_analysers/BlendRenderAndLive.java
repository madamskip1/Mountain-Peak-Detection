package org.pw.masterthesis.peaksrecognition.activities_frame_analysers;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageProxy;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.pw.masterthesis.peaksrecognition.Config;
import org.pw.masterthesis.peaksrecognition.DeviceOrientation;
import org.pw.masterthesis.peaksrecognition.FieldOfView;
import org.pw.masterthesis.peaksrecognition.devicecamera.FrameAnalyser;
import org.pw.masterthesis.peaksrecognition.devicecamera.ImageProxyToMatConverter;
import org.pw.masterthesis.peaksrecognition.mainopengl.Camera;
import org.pw.masterthesis.peaksrecognition.managers.CoordsManager;
import org.pw.masterthesis.peaksrecognition.managers.LocationManager;
import org.pw.masterthesis.peaksrecognition.managers.RotationManager;
import org.pw.masterthesis.peaksrecognition.renderer.OffScreenRenderer;
import org.pw.masterthesis.peaksrecognition.renderer.Renderer;

import java.util.concurrent.atomic.AtomicBoolean;

public class BlendRenderAndLive extends FrameAnalyser {
    static {
        OpenCVLoader.initDebug();
    }

    private final int width;
    private final int height;
    private final AppCompatActivity parentActivity;
    private final Context parentContext;
    ImageView imageView;
    private Location curLocation;
    private float[] curRotation;
    private Renderer renderer;
    private Camera camera;
    private CoordsManager coordsManager;
    private RotationManager rotationManager;
    private LocationManager locationManager;

    public BlendRenderAndLive(AppCompatActivity activity, Context context, ImageView imageView) {
        super(activity, 640, 480);
        width = 640;
        height = 480;
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
        renderer.render();
        Mat renderMat = renderer.getRenderedMat();

        Core.addWeighted(rgbaMat, 0.5, renderMat, 0.5, 0.0, rgbaMat);
        Bitmap bitmap = Bitmap.createBitmap(rgbaMat.cols(), rgbaMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(rgbaMat, bitmap);
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
        fieldOfView.setDeviceOrientation(DeviceOrientation.LANDSCAPE);

        Config config = new Config();
        config.initObserverLocation = new double[]{curLocation.getLatitude(), curLocation.getLongitude(), curLocation.getAltitude()};
        config.initObserverRotation = curRotation;
        config.maxDistance = 30.0;
        config.minDistance = 0.001;
        config.FovVertical = (float) fieldOfView.getVerticalFOV();
        config.simplifyFactor = 3;
        config.initHgtSize = 3601;
        config.deviceOrientation = DeviceOrientation.LANDSCAPE;
        config.width = width;
        config.height = height;

        renderer = new OffScreenRenderer(parentContext, config);
        coordsManager = renderer.getCoordsManager();
        camera = renderer.getCamera();
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