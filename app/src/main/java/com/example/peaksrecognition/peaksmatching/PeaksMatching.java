package com.example.peaksrecognition.peaksmatching;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public abstract class PeaksMatching {
    private final boolean dilationOnRender;
    private final boolean dilationOnImage;
    private final Mat dilationKernel;

    public PeaksMatching(boolean dilationOnRender, boolean dilationOnImage)
    {
        this.dilationOnRender = dilationOnRender;
        this.dilationOnImage = dilationOnImage;

        if (dilationOnRender || dilationOnImage)
        {
            dilationKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        }
        else {
            dilationKernel = null;
        }
    }

    public boolean shouldRenderBeDilated()
    {
        return dilationOnRender;
    }

    public boolean shouldImageBeDilated()
    {
        return dilationOnImage;
    }

    public Mat dilate(Mat img)
    {
        Mat dilatedImg = new Mat();
        Imgproc.dilate(img, dilatedImg, dilationKernel);
        return dilatedImg;
    }

    abstract double[] match(Mat template, Mat image); // should return screen coordinates [x, y] if peaks matched, otherwise null
}
