package com.example.peaksrecognition.peaksmatching;

import com.example.peaksrecognition.Peaks;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.Vector;

public abstract class PeaksMatching {
    private final boolean dilationOnRender;
    private final boolean dilationOnImage;
    private final Mat dilationKernel;

    public PeaksMatching(boolean dilationOnRender, boolean dilationOnImage) {
        this.dilationOnRender = dilationOnRender;
        this.dilationOnImage = dilationOnImage;

        if (dilationOnRender || dilationOnImage) {
            dilationKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        } else {
            dilationKernel = null;
        }
    }

    public Vector<Peaks.Peak> matchAll(Vector<Peaks.Peak> peaks, Mat render, Mat image) {
        if (dilationOnRender)
            render = dilate(render);
        if (dilationOnImage)
            image = dilate(image);

        Vector<Peaks.Peak> matchedPeaks = new Vector<>();
        for (Peaks.Peak peak : peaks) {
            int renderedX = Math.round(peak.screenPosition[0]);
            int renderedY = Math.round(peak.screenPosition[1]);

            Rect templateROI = prepareTemplateROI(renderedX, renderedY);
            Rect subjectROI = prepareSubjectROI(renderedX, renderedY);

            Mat template = render.submat(templateROI);
            Mat subject = image.submat(subjectROI);

            double[] predicted = match(template, subject);
            if (predicted != null) {
                int realX = (int) Math.round(predicted[0] + subjectROI.x);
                int realY = (int) Math.round(predicted[1] + subjectROI.y);

                peak.realImagePosition = new int[]{realX, realY};
                matchedPeaks.add(peak);
            }
        }
        return matchedPeaks;
    }

    private Mat dilate(Mat img) {
        Mat dilatedImg = new Mat();
        Imgproc.dilate(img, dilatedImg, dilationKernel);
        return dilatedImg;
    }

    abstract double[] match(Mat template, Mat subject); // should return screen coordinates [x, y] if peaks matched, otherwise null

    abstract Rect prepareTemplateROI(int x, int y);

    abstract Rect prepareSubjectROI(int x, int y);
}
