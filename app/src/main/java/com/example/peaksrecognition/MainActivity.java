package com.example.peaksrecognition;

import android.Manifest;
import android.content.pm.PackageManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.peaksrecognition.mainopengl.MyGLSurfaceView;
import com.example.peaksrecognition.mainopengl.OffScreenRenderer;
import com.example.peaksrecognition.mainopengl.ShaderProgram;
import com.example.peaksrecognition.terrain.TerrainLoader;
import com.example.peaksrecognition.terrain.TerrainModel;
import com.opencsv.exceptions.CsvValidationException;

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
        Log.d("moje", "main");
        super.onCreate(savedInstanceState);
        checkSaveToDeviceMemoryPermissions();
        OpenCVLoader.initDebug();
        /*double[] observerLocation = new double[]{49.339045, 20.081936, 991.1};

        ShaderProgram shaderProgram = new ShaderProgram();
        TerrainLoader terrainLoader = new TerrainLoader(this, observerLocation[0], observerLocation[1]);
        TerrainModel terrainModel = new TerrainModel(terrainLoader, shaderProgram);
        CoordsManager coordsManager = new CoordsManager(observerLocation, new int[][] {{49, 50}, {19, 21}}, new double[] { 111.19492664455873, 72.21257835511295 });
        ScreenManager screenManager = new ScreenManager(768, 1024);
        Camera camera = new Camera(66.0, 0.75f, 0.01f, 31.0f);
        double[] cameraPositionLocal = coordsManager.convertGeoToLocalCoords(observerLocation[0], observerLocation[1], observerLocation[2]);
        double[] observerRotation = new double[]{144.31152, 2.3836904, -2.0597333};
        camera.setPosition(cameraPositionLocal[0], cameraPositionLocal[1], cameraPositionLocal[2]);
        camera.setAngles(observerRotation[0], observerRotation[1], observerRotation[2]);
        screenManager.setMVPMatrices(camera.getViewMatrix(), camera.getProjectionMatrix());

        OffScreenRenderer renderer = new OffScreenRenderer(this);
        renderer.render();
        System.out.print("qweqwe");
        Peaks peaks = new Peaks(this, coordsManager, terrainModel, screenManager, shaderProgram);

*/
        // Surface Rendering

        glView = new MyGLSurfaceView(this);
        setContentView(glView);


        // Off-screen rendering
/*
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
*/
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