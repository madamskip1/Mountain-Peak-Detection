package org.pw.masterthesis.peaksrecognition.activities_frame_analysers;

import android.graphics.Bitmap;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageProxy;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.pw.masterthesis.peaksrecognition.devicecamera.FrameAnalyser;
import org.pw.masterthesis.peaksrecognition.devicecamera.ImageProxyToMatConverter;

public class CannyThresholdLive extends FrameAnalyser {
    static {
        OpenCVLoader.initDebug();
    }

    ImageView imageView;
    private int lowThreshold;
    private int highThreshold;


    public CannyThresholdLive(AppCompatActivity activity, ImageView imageView, int width, int height) {
        super(activity, width, height);
        this.imageView = imageView;
    }

    public void setThreshold(int low, int high) {
        lowThreshold = low;
        highThreshold = high;
    }


    @Override
    protected void analyse(ImageProxy image) {
        Mat rgba = ImageProxyToMatConverter.rgba(image);
        Mat gray = new Mat(rgba.rows(), rgba.cols(), CvType.CV_8UC1);
        Imgproc.cvtColor(rgba, gray, Imgproc.COLOR_RGBA2GRAY);
        Mat edge = new Mat();
        Imgproc.Canny(gray, edge, lowThreshold, highThreshold);
        Bitmap bitmap = Bitmap.createBitmap(edge.cols(), edge.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(edge, bitmap);
        imageView.setImageBitmap(bitmap);
    }

}
