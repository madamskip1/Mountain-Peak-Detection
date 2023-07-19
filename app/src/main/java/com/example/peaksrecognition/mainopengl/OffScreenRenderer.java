package com.example.peaksrecognition.mainopengl;

import android.content.Context;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES30;

import com.example.peaksrecognition.Config;
import com.example.peaksrecognition.CoordsManager;
import com.example.peaksrecognition.Peaks;
import com.example.peaksrecognition.Peaks.Peak;
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
import java.util.Vector;

public class OffScreenRenderer {
    private final int width;
    private final int height;
    private final TerrainModel terrainModel;
    private final Camera camera;
    private final CoordsManager coordsManager;
    private final ScreenManager screenManager;
    private final Peaks peaks;
    private EGLContext eglContext;
    private EGLDisplay eglDisplay;
    private EGLConfig eglConfig;

    public OffScreenRenderer(Context context, Config config) {
        width = config.width;
        height = config.height;
        createEGLContext();
        createEGLSurface();

        ShaderProgram shaderProgram = new ShaderProgram();
        TerrainLoader terrainLoader = new TerrainLoader(context, config);
        LoadedTerrain loadedTerrain = terrainLoader.load();
        TerrainData terrainData = new TerrainData(loadedTerrain, config);
        terrainModel = new TerrainModel(terrainData, shaderProgram);
        coordsManager = new CoordsManager(config.initObserverLocation, terrainData.getCoordsRange(), terrainData.getGridSize());
        camera = new Camera(config.FovVertical, (float) width / (float) height, (float) config.minDistance, (float) config.maxDistance, config.deviceOrientation);
        screenManager = new ScreenManager();
        screenManager.setViewportDimensions(width, height);
        peaks = new Peaks(context, coordsManager, terrainData, screenManager, shaderProgram);

        double[] cameraPositionLocal = coordsManager.convertGeoToLocalCoords(config.initObserverLocation[0], config.initObserverLocation[1], config.initObserverLocation[2]);
        camera.setPosition(cameraPositionLocal[0], cameraPositionLocal[1], cameraPositionLocal[2]);
        camera.setAngles(config.initObserverRotation[0], config.initObserverRotation[1], config.initObserverRotation[2]);
    }

    public Mat getRenderedMat() {
        ByteBuffer buffer = getRenderBuffer();
        Mat image = new Mat(height, width, CvType.CV_8UC4);
        byte[] imageData = new byte[height * width * 4];
        buffer.get(imageData);
        image.put(0, 0, imageData);
        return image;
    }

    public void render() {
        GLES30.glViewport(0, 0, width, height);
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
        float[] viewMatrix = camera.getViewMatrix();
        float[] projectionMatrix = camera.getProjectionMatrix();
        terrainModel.draw(viewMatrix, projectionMatrix);
        screenManager.setMVPMatrices(viewMatrix, projectionMatrix);
    }

    public Peaks getPeaks() {
        return peaks;
    }

    public CoordsManager getCoordsManager() {
        return coordsManager;
    }

    public Camera getCamera() {
        return camera;
    }

    private ByteBuffer getRenderBuffer() {
        ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * 4);
        buffer.order(ByteOrder.nativeOrder());
        GLES30.glReadPixels(0, 0, width, height, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, buffer);
        return buffer;
    }

    private void createEGLContext() {
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

        int[] contextAttribs = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 3,
                EGL14.EGL_NONE
        };
        eglConfig = configs[0];
        eglContext = EGL14.eglCreateContext(eglDisplay, eglConfig, EGL14.EGL_NO_CONTEXT, contextAttribs, 0);
    }

    private void createEGLSurface() {
        int[] surfaceAttribs = {
                EGL14.EGL_WIDTH, width,
                EGL14.EGL_HEIGHT, height,
                EGL14.EGL_NONE
        };

        EGLSurface eglSurface = EGL14.eglCreatePbufferSurface(eglDisplay, eglConfig, surfaceAttribs, 0);
        EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext);
    }
}
