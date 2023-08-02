package org.pw.masterthesis.peaksrecognition.peaksmatching;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.pw.masterthesis.peaksrecognition.Peaks;

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

        int width = image.cols();
        int height = image.rows();

        Vector<Peaks.Peak> matchedPeaks = new Vector<>();
        for (Peaks.Peak peak : peaks) {
            int renderedX = Math.round(peak.screenPosition[0]);
            int renderedY = Math.round(peak.screenPosition[1]);

            Rect templateROI = prepareTemplateROI(renderedX, renderedY, width, height);
            Rect subjectROI = prepareSubjectROI(renderedX, renderedY, width, height);

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

    abstract Rect prepareTemplateROI(int x, int y, int matWidth, int matHeight);

    abstract Rect prepareSubjectROI(int x, int y, int matWidth, int matHeight);

    protected Rect prepareROI(int x1, int y1, int x2, int y2, int matWidth, int matHeight) {
        if (x1 < 0)
            x1 = 0;
        if (y1 < 0)
            y1 = 0;

        if (x2 >= matWidth)
            x2 = matWidth - 1;
        if (y2 >= matHeight)
            y2 = matHeight - 1;

        int width = x2 - x1;
        int height = y2 - y1;

        return new Rect(x1, y1, width, height);
    }
}
