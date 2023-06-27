package com.example.peaksrecognition.devicecamera;

import android.annotation.SuppressLint;
import android.media.Image;

import androidx.camera.core.ImageProxy;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;

public class ImageProxyToMatConverter {
    @SuppressLint("UnsafeOptInUsageError")
    public static Mat rgb(ImageProxy frame) {
        Image image = frame.getImage();
        byte[] nv21;
        assert image != null;
        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer();
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        nv21 = new byte[ySize + uSize + vSize];

        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        Mat mYuv = new Mat(image.getHeight() + image.getHeight() / 2, image.getWidth(), CvType.CV_8UC1);
        mYuv.put(0, 0, nv21);
        Mat mRGB = new Mat();
        Imgproc.cvtColor(mYuv, mRGB, Imgproc.COLOR_YUV2RGB_NV21, 3);

        rotateAndFlip(mRGB);
        return mRGB;
    }

    @SuppressLint("UnsafeOptInUsageError")
    public static Mat rgba(ImageProxy frame) {
        Mat rgbaMat = new Mat();
        Image image = frame.getImage();
        assert image != null;
        Image.Plane[] planes = image.getPlanes();
        int w = frame.getWidth();
        int h = frame.getHeight();

        ByteBuffer y_plane = planes[0].getBuffer();
        int y_plane_step = planes[0].getRowStride();
        ByteBuffer uv_plane1 = planes[1].getBuffer();
        int uv_plane1_step = planes[1].getRowStride();
        ByteBuffer uv_plane2 = planes[2].getBuffer();
        int uv_plane2_step = planes[2].getRowStride();
        Mat y_mat = new Mat(h, w, CvType.CV_8UC1, y_plane, y_plane_step);
        Mat uv_mat1 = new Mat(h / 2, w / 2, CvType.CV_8UC2, uv_plane1, uv_plane1_step);
        Mat uv_mat2 = new Mat(h / 2, w / 2, CvType.CV_8UC2, uv_plane2, uv_plane2_step);
        long addr_diff = uv_mat2.dataAddr() - uv_mat1.dataAddr();
        if (addr_diff > 0) {
            Imgproc.cvtColorTwoPlane(y_mat, uv_mat1, rgbaMat, Imgproc.COLOR_YUV2RGBA_NV12);
        } else {
            Imgproc.cvtColorTwoPlane(y_mat, uv_mat2, rgbaMat, Imgproc.COLOR_YUV2RGBA_NV21);
        }

        rotateAndFlip(rgbaMat);

        return rgbaMat;
    }


    private static void rotateAndFlip(Mat matToRotate) {
        Core.rotate(matToRotate, matToRotate, Core.ROTATE_90_COUNTERCLOCKWISE);
        Core.flip(matToRotate, matToRotate, 1);
    }
}
