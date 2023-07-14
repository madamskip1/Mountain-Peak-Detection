package com.example.peaksrecognition;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageProxy;

import com.example.peaksrecognition.devicecamera.FrameAnalyser;
import com.example.peaksrecognition.devicecamera.ImageProxyToMatConverter;
import com.example.peaksrecognition.edgedetectors.CannyEdgeDetector;
import com.example.peaksrecognition.mainopengl.Camera;
import com.example.peaksrecognition.mainopengl.OffScreenRenderer;
import com.example.peaksrecognition.peaksmatching.TemplateMatching;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

public class PeaksRecognizer extends FrameAnalyser {
    static {
        OpenCVLoader.initDebug();
    }

    private final Context parentContext;
    private final AppCompatActivity parentActivity;
    private final CannyEdgeDetector cannyRender;
    private final CannyEdgeDetector cannyLive;
    private final int width;
    private final int height;
    private final TemplateMatching templateMatching;
    ImageView imageView;
    private Location curLocation;
    private float[] curRotation;
    private OffScreenRenderer offScreenRenderer;
    private Camera camera;
    private Peaks peaks;
    private CoordsManager coordsManager;
    private RotationManager rotationManager;
    private LocationManager locationManager;

    public PeaksRecognizer(AppCompatActivity activity, Context context, ImageView imageView, int width, int height) {
        super(activity, width, height);
        parentContext = context;
        parentActivity = activity;
        this.imageView = imageView;
        this.width = width;
        this.height = height;
        cannyRender = new CannyEdgeDetector(50, 100);
        cannyLive = new CannyEdgeDetector(50, 130);
        templateMatching = new TemplateMatching();
    }

    public void prepareAndStart() {
        prepare();
    }

    @Override
    protected void analyse(ImageProxy image) {
        Mat rgba = ImageProxyToMatConverter.rgba(image);
        Mat liveEdges = cannyLive.detect(rgba);
        Mat liveSkyline = cannyLive.detectSkyline(liveEdges);
        if (templateMatching.shouldImageBeDilated()) {
            liveSkyline = templateMatching.dilate(liveSkyline);
        }

        double[] cameraCoords = coordsManager.convertGeoToLocalCoords(curLocation.getLatitude(), curLocation.getLongitude(), curLocation.getAltitude());
        camera.setPosition(cameraCoords[0], cameraCoords[1], cameraCoords[2]);
        camera.setAngles(curRotation[0], curRotation[1], curRotation[2]);
        offScreenRenderer.render();

        Mat renderedScene = offScreenRenderer.getRenderedMat();
        Mat renderedEdges = cannyRender.detect(renderedScene);
        Mat renderedSkyline = cannyRender.detectSkyline(renderedEdges);
        if (templateMatching.shouldRenderBeDilated()) {
            renderedSkyline = templateMatching.dilate(renderedSkyline);
        }

        Vector<Peaks.Peak> visiblePeaks = peaks.getVisiblePeaks();
        Vector<Peaks.Peak> visiblePeaksAfterMatching = new Vector<>();
        for (Peaks.Peak peak : visiblePeaks) {
            int templateOffset = 40;
            int subjectXOffset = 117;
            int subjectYOffset = 97;
            int peakX = Math.round(peak.screenPosition[0]);
            int peakY = Math.round(peak.screenPosition[1]);

            Rect templateROI = new Rect(peakX - templateOffset, peakY - templateOffset, templateOffset * 2, templateOffset * 2);
            Rect subjectROI = new Rect(peakX - subjectXOffset, peakY - subjectYOffset, subjectXOffset * 2, subjectYOffset * 2);
            Mat template = renderedSkyline.submat(templateROI);
            Mat subject = liveSkyline.submat(subjectROI);
            Imgproc.rectangle(rgba, templateROI, new Scalar(255, 0, 0));
            Imgproc.rectangle(rgba, subjectROI, new Scalar(255, 0, 0));
            double[] predicted = templateMatching.match(template, subject);
            if (predicted != null) {
                predicted[0] += subjectROI.x;
                predicted[1] += subjectROI.y;
                peak.realImagePosition = new int[]{(int) Math.round(predicted[0]), (int) Math.round(predicted[1])};
                visiblePeaksAfterMatching.add(peak);
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(rgba.cols(), rgba.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(rgba, bitmap);
        bitmap = peaks.drawPeakNames(bitmap, visiblePeaksAfterMatching);
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
        config.width = width;
        config.height = height;
        offScreenRenderer = new OffScreenRenderer(parentContext, config);
        coordsManager = offScreenRenderer.getCoordsManager();
        camera = offScreenRenderer.getCamera();
        peaks = offScreenRenderer.getPeaks();
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
