package com.example.peaksrecognition;

import android.Manifest;
import android.content.pm.PackageManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.peaksrecognition.mainopengl.OffScreenRenderer;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private GLSurfaceView glView;
    private OffScreenRenderer renderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkSaveToDeviceMemoryPermissions();
        OpenCVLoader.initDebug();

        // Surface Rendering
/*
        glView = new MyGLSurfaceView(this);
        setContentView(glView);
*/

        // Off-screen rendering

        setContentView(R.layout.activity_main);
        renderer = new OffScreenRenderer(this);
        renderer.render();
        Mat image = renderer.getRenderedMat();

        saveMatToDeviceMemory(image);
    }

    private void checkSaveToDeviceMemoryPermissions() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    MainActivity.this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
            ) {
                ActivityCompat.requestPermissions(
                        MainActivity.this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                ActivityCompat.requestPermissions(
                        MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    private void saveMatToDeviceMemory(Mat imageMat) {
        byte[] imageData;
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg", imageMat, matOfByte);
        imageData = matOfByte.toArray();
        File dir = new File(getExternalFilesDir(null), "test");
        if (!dir.exists()) {
            dir.mkdir();
        }
        File file = new File(dir, "off_screen_render.jpg");
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(imageData);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}