package com.example.peaksrecognition.peaksmatching;

import org.opencv.core.Mat;

public interface PeaksMatching {
    double[] match(Mat template, Mat image); // should return screen coordinates [x, y] if peaks matched, otherwise null
}
