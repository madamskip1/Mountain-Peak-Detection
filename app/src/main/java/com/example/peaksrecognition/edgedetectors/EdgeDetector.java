package com.example.peaksrecognition.edgedetectors;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

public interface EdgeDetector {
    Mat detect(Mat image);

    default Mat detectSkyline(Mat edgesMat) {
        Mat skyline = Mat.zeros(edgesMat.size(), CvType.CV_8UC1);
        int cols = edgesMat.cols();
        int rows = edgesMat.rows();
        boolean[] columnsDone = new boolean[cols];

        byte[] edgesMatRowValuesBuffer = new byte[cols];
        for (int row = 0; row < rows; row++) {
            edgesMat.row(row).get(0, 0, edgesMatRowValuesBuffer);
            boolean allColumnsDone = true;
            for (int col = 0; col < cols; col++) {
                if (!columnsDone[col]) {
                    allColumnsDone = false;
                    if (edgesMatRowValuesBuffer[col] == -1) {
                        columnsDone[col] = true;
                        allColumnsDone = true;
                        skyline.put(row, col, 255);
                    }
                }
            }
            if (allColumnsDone) {
                break;
            }
        }
        return skyline;
    }
}
