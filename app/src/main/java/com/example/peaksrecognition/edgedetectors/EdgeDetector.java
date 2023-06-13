package com.example.peaksrecognition.edgedetectors;

import org.opencv.core.Mat;

public interface EdgeDetector {
    Mat detect(Mat image);
}
