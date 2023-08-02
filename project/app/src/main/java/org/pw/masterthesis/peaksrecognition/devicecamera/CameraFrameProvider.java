package org.pw.masterthesis.peaksrecognition.devicecamera;

import android.util.Size;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

public class CameraFrameProvider {
    private final ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private final AppCompatActivity activity;
    private final FrameAnalyser frameAnalyser;
    private final int width;
    private final int height;

    public CameraFrameProvider(AppCompatActivity activity, FrameAnalyser frameAnalyser, int width, int height) {
        this.activity = activity;
        this.frameAnalyser = frameAnalyser;
        this.width = width;
        this.height = height;


        cameraProviderFuture = ProcessCameraProvider.getInstance(this.activity);
    }


    public void start() {
        cameraProviderFuture.addListener(prepareCameraProviderRunnable(), ContextCompat.getMainExecutor(activity));
    }

    private Runnable prepareCameraProviderRunnable() {
        return () -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindImageAnalysis(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        };
    }

    private void bindImageAnalysis(ProcessCameraProvider cameraProvider) {
        ImageAnalysis imageAnalysis = prepareImageAnalysis();
        CameraSelector cameraSelector = prepareCameraSelector();
        cameraProvider.bindToLifecycle(activity, cameraSelector, imageAnalysis);
    }

    private CameraSelector prepareCameraSelector() {
        return new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
    }

    private ImageAnalysis prepareImageAnalysis() {
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(width, height))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(activity), image -> {
            frameAnalyser.analyse(image);
            image.close();
        });

        return imageAnalysis;
    }
}
