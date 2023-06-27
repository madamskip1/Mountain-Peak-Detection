package com.example.peaksrecognition.devicecamera;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageProxy;

public abstract class FrameAnalyser {
    private final CameraFrameProvider cameraFramesProvider;

    protected FrameAnalyser(AppCompatActivity activity) {
        cameraFramesProvider = new CameraFrameProvider(activity, this);
    }

    protected abstract void analyse(ImageProxy image);

    public void start() {
        cameraFramesProvider.start();
    }
}
