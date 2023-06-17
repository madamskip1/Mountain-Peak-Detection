package com.example.peaksrecognition.peaksmatching;

import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

public class TemplateMatching implements PeaksMatching {
    private static final int METHOD = Imgproc.TM_CCOEFF_NORMED;
    private static final double THRESHOLD = 0.15;

    @Override
    public double[] match(Mat template, Mat image) {
        Mat matchResult = new Mat();
        Imgproc.matchTemplate(image, template, matchResult, METHOD);
        Point matchLoc;
        double value;
        MinMaxLocResult minMaxLocResult = Core.minMaxLoc(matchResult);
        if (METHOD == Imgproc.TM_SQDIFF_NORMED) {
            matchLoc = minMaxLocResult.minLoc;
            value = minMaxLocResult.minVal;
        } else {
            matchLoc = minMaxLocResult.maxLoc;
            value = minMaxLocResult.maxVal;
        }

        if (value < THRESHOLD) {
            return null; // or [-1, -1] ?
        }
        Point center = new Point(matchLoc.x + ((int) ((double) template.cols() / 2.0)), matchLoc.y + ((int) ((double) template.rows() / 2.0)));

        return new double[]{center.x, center.y};
    }
}
