package com.example.peaksrecognition.edgedetectors;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class Canny implements EdgeDetector {
    private final int minThreshold;
    private final int maxThreshold;

    public Canny(int minThreshold, int maxThreshold) {
        this.minThreshold = minThreshold;
        this.maxThreshold = maxThreshold;
    }

    @Override
    public Mat detect(Mat image) {
        Mat edgeImage = new Mat();
        Imgproc.Canny(image, edgeImage, minThreshold, maxThreshold);
        return edgeImage;
    }
}
