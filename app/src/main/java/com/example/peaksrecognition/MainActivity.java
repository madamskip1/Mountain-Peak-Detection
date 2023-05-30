package com.example.peaksrecognition;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.peaksrecognition.mainopengl.OffScreenRenderer;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

    private GLSurfaceView glView;
    private OffScreenRenderer renderer2;
    private ImageView imageView;
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
        renderer2 = new OffScreenRenderer(this);
        imageView = findViewById(R.id.renderedImageView);
        renderer2.render();
        ByteBuffer buffer = renderer2.getBuffer();
        Mat image = new Mat(1024, 768, CvType.CV_8UC4);
        byte[] imageData = new byte[1024 * 768 * 4];
        buffer.get(imageData);
        image.put(0, 0, imageData);
        Core.flip(image, image, 0);

        saveMatToDeviceMemory(image);
    }

    private void checkSaveToDeviceMemoryPermissions()
    {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    MainActivity.this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
            )
            {
                ActivityCompat.requestPermissions(
                        MainActivity.this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
            else
            {
                ActivityCompat.requestPermissions(
                        MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    private void saveMatToDeviceMemory(Mat imageMat)
    {
        byte[] imageData;
        MatOfByte matOfByte =  new MatOfByte();
        Imgcodecs.imencode(".jpg", imageMat, matOfByte);
        imageData = matOfByte.toArray();
        File dir = new File(getExternalFilesDir(null), "test");
        if (!dir.exists())
        {
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