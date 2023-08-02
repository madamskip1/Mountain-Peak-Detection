package org.pw.masterthesis.peaksrecognition.peaksmatching;

import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

public class TemplateMatching extends PeaksMatching {
    private static final int METHOD = Imgproc.TM_CCORR_NORMED;
    private static final double THRESHOLD = 0.05;//0.472773462533951; //0.15;
    private static final int templateOffset = 40;
    private static final int subjectXOffset = 117;
    private static final int subjectYOffset = 97;

    public TemplateMatching() {
        super(true, true);
    }

    @Override
    protected double[] match(Mat template, Mat subject) {
        Mat matchResult = new Mat();
        Imgproc.matchTemplate(subject, template, matchResult, METHOD);

        MinMaxLocResult minMaxLocResult = Core.minMaxLoc(matchResult);
        Point matchLoc = minMaxLocResult.maxLoc;
        double value = minMaxLocResult.maxVal;

        if (value < THRESHOLD) {
            return null; // or [-1, -1] ?
        }
        Point center = new Point(matchLoc.x + ((int) ((double) template.cols() / 2.0)), matchLoc.y + ((int) ((double) template.rows() / 2.0)));

        return new double[]{center.x, center.y};
    }

    @Override
    Rect prepareTemplateROI(int x, int y, int matWidth, int matHeight) {
        int xRect = x - templateOffset;
        int yRect = xRect;
        int width = templateOffset * 2;
        int height = width;

        return new Rect(xRect, yRect, width, height);
    }

    @Override
    Rect prepareSubjectROI(int x, int y, int matWidth, int matHeight) {
        int x1 = x - subjectXOffset;
        int y1 = y - subjectYOffset;
        int x2 = x1 + subjectXOffset * 2;
        int y2 = y1 + subjectYOffset * 2;

        return prepareROI(x1, y1, x2, y2, matWidth, matHeight);
    }
}
