package com.example.peaksrecognition;

import android.opengl.GLU;

public class ScreenManager {
    private int[] viewport;
    private float[] viewMatrix;
    private float[] projectionMatrix;

    public ScreenManager() {
        viewport = new int[]{0, 0, 0, 0};
    }

    ScreenManager(int screenWidth, int screenHeight) {
        viewport = new int[]{0, 0, screenWidth, screenHeight};
    }

    public void setViewportDimensions(int screenWidth, int screenHeight) {
        viewport[2] = screenHeight;
        viewport[3] = screenHeight;
    }

    public void setMVPMatrices(float[] viewMatrix, float[] projectionMatrix) {
        this.viewMatrix = viewMatrix;
        this.projectionMatrix = projectionMatrix;
    }

    public float[] getScreenPosition(float xVertex, float yVertex, float zVertex) {
        float[] screenPosition = new float[3];
        GLU.gluProject(xVertex, yVertex, zVertex, viewMatrix, 0, projectionMatrix, 0, viewport, 0, screenPosition, 0);

        return screenPosition;
    }

    public boolean checkIfPointOnScreen(float[] screenPoint) {
        return screenPoint[0] >= 0 && screenPoint[0] <= viewport[2]
                && screenPoint[1] >= 0 && screenPoint[0] <= viewport[3]
                && screenPoint[2] >= 0 && screenPoint[0] <= 1;
    }

}
