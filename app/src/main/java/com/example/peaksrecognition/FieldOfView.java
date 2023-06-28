package com.example.peaksrecognition;

import static java.lang.Math.atan;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.util.SizeF;

public class FieldOfView {
    private final float focalLength;
    private final float sensorWidth;
    private final float sensorHeight;
    private DeviceOrientation deviceOrientation = DeviceOrientation.PORTRAIT;
    public FieldOfView(Context context) {
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        CameraCharacteristics cameraCharacteristics;
        try {
            cameraCharacteristics = cameraManager.getCameraCharacteristics("0");
        } catch (CameraAccessException e) {
            throw new RuntimeException(e);
        }
        float[] focalLengths = cameraCharacteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
        focalLength = focalLengths[0];

        SizeF sensorSize = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
        sensorWidth = sensorSize.getWidth();
        sensorHeight = sensorSize.getHeight();
    }

    public void setDeviceOrientation(DeviceOrientation deviceOrientation) {
        this.deviceOrientation = deviceOrientation;
    }

    public double getVerticalFOV() {
        return deviceOrientation == DeviceOrientation.LANDSCAPE ? getVerticalDeviceFOV() : getHorizontalDeviceFOV();
    }

    public double getHorizontalFOV() {
        return deviceOrientation == DeviceOrientation.LANDSCAPE ? getHorizontalDeviceFOV() : getVerticalDeviceFOV();
    }

    private double getHorizontalDeviceFOV() {
        return (2.0f * atan(sensorWidth / (focalLength * 2.0f)) * 180.0f / Math.PI);
    }

    private double getVerticalDeviceFOV() {
        return (2.0f * atan(sensorHeight / (focalLength * 2.0f)) * 180.0 / Math.PI);
    }

    public enum DeviceOrientation {
        PORTRAIT,
        LANDSCAPE
    }

}
