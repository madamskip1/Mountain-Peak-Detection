package com.example.peaksrecognition;

public class Config {
    public double[] initObserverLocation;
    public float[] initObserverRotation;
    public double maxDistance;
    public double minDistance;
    public float FovVertical;
    public int simplifyFactor;
    public int initHgtSize;
    public DeviceOrientation deviceOrientation = DeviceOrientation.PORTRAIT;

    public enum DeviceOrientation {
        PORTRAIT,
        LANDSCAPE
    }
}