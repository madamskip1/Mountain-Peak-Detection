package com.example.peaksrecognition.mainopengl;

import android.graphics.Shader;
import android.opengl.GLES30;

public class ShaderProgram {
    private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "uniform mat4 viewMatrix;" +
                    "uniform mat4 projectionMatrix;" +
                    "void main() {" +
                    //"  vPosition = vec4(vPosition.x * -1.0, vPosition.y, vPosition.z, vPosition.w);" +
                    "  gl_Position = projectionMatrix * viewMatrix * vPosition;" +
                    "}";
    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "void main() {" +
                    "  gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);" +
                    "}";
    private final int shaderProgram;

    public ShaderProgram()
    {
        int vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderCode);
        shaderProgram = GLES30.glCreateProgram();
        GLES30.glAttachShader(shaderProgram, vertexShader);
        GLES30.glAttachShader(shaderProgram, fragmentShader);
        GLES30.glLinkProgram(shaderProgram);
        GLES30.glUseProgram(shaderProgram);
    }

    public int getShaderProgram()
    {
        return shaderProgram;
    }

    private int loadShader(int type, String shaderCode)
    {
        int shader = GLES30.glCreateShader(type);
        GLES30.glShaderSource(shader, shaderCode);
        GLES30.glCompileShader(shader);
        return shader;
    }
}
