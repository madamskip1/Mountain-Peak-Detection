package com.example.peaksrecognition.mainopengl;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;

import com.example.peaksrecognition.Camera;
import com.example.peaksrecognition.Config;
import com.example.peaksrecognition.CoordsManager;
import com.example.peaksrecognition.Peaks;
import com.example.peaksrecognition.ScreenManager;
import com.example.peaksrecognition.terrain.TerrainData;
import com.example.peaksrecognition.terrain.TerrainLoader;
import com.example.peaksrecognition.terrain.TerrainLoader.LoadedTerrain;
import com.example.peaksrecognition.terrain.TerrainModel;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyGLRenderer implements GLSurfaceView.Renderer {
    private final Context context;
    private TerrainModel terrainModel;
    private Camera camera;
    private CoordsManager coordsManager;
    private ScreenManager screenManager;
    private Peaks peaks;

    public MyGLRenderer(Context context) {
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        Config config = new Config();

        double[] observerLocation = new double[]{49.339045, 20.081936, 991.1};
        double[] observerRotation = new double[]{144.31152, 2.3836904, -2.0597333};

        config.initObserverLocation = observerLocation;
        config.initObserverRotation = observerRotation;
        config.maxDistance = 30.0;
        config.minDistance = 0.01;
        config.FOVHorizontal = 66.0f;
        config.simplifyFactor = 3;
        config.initHgtSize = 3601;


        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        ShaderProgram shaderProgram = new ShaderProgram();
        TerrainLoader terrainLoader = new TerrainLoader(context, config);
        LoadedTerrain loadedTerrain = terrainLoader.load();
        TerrainData terrainData = new TerrainData(loadedTerrain, config);
        terrainModel = new TerrainModel(terrainData, shaderProgram);
        screenManager = new ScreenManager();
        coordsManager = new CoordsManager(observerLocation, terrainData.getCoordsRange(), terrainData.getGridSize());
        double[] cameraPositionLocal = coordsManager.convertGeoToLocalCoords(observerLocation[0], observerLocation[1], observerLocation[2]);
        camera = new Camera(66.0, 0.54573f, 0.01f, 31.0f);
        camera.setPosition(cameraPositionLocal[0], cameraPositionLocal[1], cameraPositionLocal[2]);
        camera.setAngles(observerRotation[0], observerRotation[1], observerRotation[2]);
         //peaks = new Peaks(context, coordsManager, terrainData, screenManager, shaderProgram);
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
        screenManager.setMVPMatrices(viewMatrix, projectionMatrix);
       // peaks.test();
    }

}
