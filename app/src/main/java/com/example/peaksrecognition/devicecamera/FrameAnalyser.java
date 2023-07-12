package com.example.peaksrecognition.devicecamera;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageProxy;

public abstract class FrameAnalyser {
    private final CameraFrameProvider cameraFramesProvider;

    protected FrameAnalyser(AppCompatActivity activity, int width, int height) {
        cameraFramesProvider = new CameraFrameProvider(activity, this, width, height);
    }

    protected abstract void analyse(ImageProxy image);

    public void start() {
        cameraFramesProvider.start();
    }
}
