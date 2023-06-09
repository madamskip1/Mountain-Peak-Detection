package com.example.peaksrecognition.mainopengl;

import android.content.Context;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES30;

import com.example.peaksrecognition.Camera;
import com.example.peaksrecognition.Config;
import com.example.peaksrecognition.CoordsManager;
import com.example.peaksrecognition.Peaks;
import com.example.peaksrecognition.ScreenManager;
import com.example.peaksrecognition.terrain.TerrainData;
import com.example.peaksrecognition.terrain.TerrainLoader;
import com.example.peaksrecognition.terrain.TerrainLoader.LoadedTerrain;
import com.example.peaksrecognition.terrain.TerrainModel;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class OffScreenRenderer {
    private final int width = 768;
    private final int height = 1024;
    private final TerrainModel terrainModel;
    private final Camera camera;
    private final CoordsManager coordsManager;
    private final ScreenManager screenManager;
    private final Peaks peaks;
    private EGLContext eglContext;
    private EGLDisplay eglDisplay;
    private EGLConfig eglConfig;

    public OffScreenRenderer(Context context) {
        createContext();
        createSurface();

        double[] observerLocation = new double[]{49.339045, 20.081936, 991.1};
        double[] observerRotation = new double[]{144.31152, 2.3836904, -2.0597333};

        Config config = new Config();
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

        coordsManager = new CoordsManager(observerLocation, terrainData.getCoordsRange(), terrainData.getGridSize());
        double[] cameraPositionLocal = coordsManager.convertGeoToLocalCoords(observerLocation[0], observerLocation[1], observerLocation[2]);
        camera = new Camera(config.FOVHorizontal, (float) width / (float) height, (float) config.minDistance, (float) config.maxDistance);
        camera.setPosition(cameraPositionLocal[0], cameraPositionLocal[1], cameraPositionLocal[2]);
        camera.setAngles(observerRotation[0], observerRotation[1], observerRotation[2]);
        screenManager = new ScreenManager();
        screenManager.setViewportDimensions(width, height);
        peaks = new Peaks(context, coordsManager, terrainData, screenManager, shaderProgram);
    }

    public ByteBuffer getBuffer() {
        ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * 4);
        buffer.order(ByteOrder.nativeOrder());
        GLES30.glReadPixels(0, 0, width, height, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, buffer);
        return buffer;
    }

    public Mat getRenderedMat() {
        ByteBuffer buffer = getBuffer();
        Mat image = new Mat(height, width, CvType.CV_8UC4);
        byte[] imageData = new byte[height * width * 4];
        buffer.get(imageData);
        image.put(0, 0, imageData);
        Core.flip(image, image, 0);

        return image;
    }

    public void render() {
        GLES30.glViewport(0, 0, width, height);
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
        float[] viewMatrix = camera.getViewMatrix();
        float[] projectionMatrix = camera.getProjectionMatrix();
        terrainModel.draw(viewMatrix, projectionMatrix);
        screenManager.setMVPMatrices(viewMatrix, projectionMatrix);
        peaks.test();
    }

    private void createContext() {
        eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        int[] version = new int[2];
        EGL14.eglInitialize(eglDisplay, version, 0, version, 1);

        int[] attribList = {
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_NONE
        };

        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        EGL14.eglChooseConfig(eglDisplay, attribList, 0, configs, 0, 1, numConfigs, 0);

        eglConfig = configs[0];

        int[] contextAttribs = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 3,
                EGL14.EGL_NONE
        };

        eglContext = EGL14.eglCreateContext(eglDisplay, eglConfig, EGL14.EGL_NO_CONTEXT, contextAttribs, 0);
    }

    private void createSurface() {
        int[] surfaceAttribs = {
                EGL14.EGL_WIDTH, width,
                EGL14.EGL_HEIGHT, height,
                EGL14.EGL_NONE
        };

        EGLSurface eglSurface = EGL14.eglCreatePbufferSurface(eglDisplay, eglConfig, surfaceAttribs, 0);
        EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext);
    }
}
