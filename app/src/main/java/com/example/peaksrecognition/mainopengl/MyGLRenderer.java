package com.example.peaksrecognition.mainopengl;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.example.peaksrecognition.Camera;
import com.example.peaksrecognition.CoordsManager;
import com.example.peaksrecognition.ScreenManager;
import com.example.peaksrecognition.terrain.TerrainLoader;
import com.example.peaksrecognition.terrain.TerrainModel;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyGLRenderer implements GLSurfaceView.Renderer {
    private TerrainModel terrainModel;
    private ShaderProgram shaderProgram;
    private Camera camera;
    private CoordsManager coordsManager;
    private ScreenManager screenManager;
    private final Context context;


    public MyGLRenderer(Context context) {
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        double[] observerLocation = new double[] { 49.339045, 20.081936, 991.1 };
        double[] observerRotation = new double[] { 144.31152, 2.3836904, -2.0597333 };


        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        shaderProgram = new ShaderProgram();
        TerrainLoader terrainLoader = new TerrainLoader(context, observerLocation[0], observerLocation[1]);
        terrainModel = new TerrainModel(terrainLoader, shaderProgram);
        screenManager = new ScreenManager();
        coordsManager = new CoordsManager(observerLocation, terrainLoader.getCoordsRange(), terrainLoader.getGridSize());
        double[] cameraPositionLocal = coordsManager.convertGeoToLocalCoords(observerLocation[0], observerLocation[1], observerLocation[2]);
        camera = new Camera(66.0, 0.54573f, 0.01f, 31.0f);
        camera.setPosition(cameraPositionLocal[0], cameraPositionLocal[1], cameraPositionLocal[2]);
        camera.setAngles(observerRotation[0], observerRotation[1], observerRotation[2]);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES30.glViewport(0, 0, width, height);
        screenManager.setViewportDimensions(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
        float[] viewMatrix = camera.getViewMatrix();
        float[] projectionMatrix = camera.getProjectionMatrix();
        terrainModel.draw(viewMatrix, projectionMatrix);
    }

}
