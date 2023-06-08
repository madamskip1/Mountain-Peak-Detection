package com.example.peaksrecognition.terrain;

import android.opengl.GLES30;

import com.example.peaksrecognition.mainopengl.ShaderProgram;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class TerrainModel {
    private final TerrainData terrainData;
    private final int positionAttribute;
    private final int viewMatrixUniform;
    private final int projectionMatrixUniform;
    private int vao;
    private int vbo;
    private int ibo;
    private int trianglesLength;

    public TerrainModel(TerrainData terrainData, ShaderProgram shaderProgram) {
        this.terrainData = terrainData;
        int shader = shaderProgram.getShaderProgram();
        positionAttribute = GLES30.glGetAttribLocation(shader, "vPosition");
        viewMatrixUniform = GLES30.glGetUniformLocation(shader, "viewMatrix");
        projectionMatrixUniform = GLES30.glGetUniformLocation(shader, "projectionMatrix");
        prepareBuffers();
    }

    public void draw(float[] viewMatrix, float[] projectionMatrix) {
        GLES30.glBindVertexArray(vao);
        GLES30.glUniformMatrix4fv(viewMatrixUniform, 1, false, viewMatrix, 0);
        GLES30.glUniformMatrix4fv(projectionMatrixUniform, 1, false, projectionMatrix, 0);
        GLES30.glDrawElements(
                GLES30.GL_TRIANGLES,
                trianglesLength,
                GLES30.GL_UNSIGNED_INT,
                0
        );
        GLES30.glBindVertexArray(0);

    }

    private void prepareBuffers() {
        generateBuffersIds();
        generateVboData();
        generateIboData();
        generateVaoData();
    }

    private void generateBuffersIds() {
        int[] buffersIds = new int[2];
        GLES30.glGenBuffers(2, buffersIds, 0);
        vbo = buffersIds[0];
        ibo = buffersIds[1];

        int[] vaoIds = new int[1];
        GLES30.glGenVertexArrays(1, vaoIds, 0);
        vao = vaoIds[0];
    }

    private void generateVboData() {
        float[] vertices = terrainData.getVertices();
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        FloatBuffer vertexBuffer = byteBuffer.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo);
        GLES30.glBufferData(
                GLES30.GL_ARRAY_BUFFER,
                vertexBuffer.capacity() * 4,
                vertexBuffer,
                GLES30.GL_STATIC_DRAW
        );
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
    }

    private void generateIboData() {
        int[] triangles = terrainData.getTriangles();
        trianglesLength = triangles.length;
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(triangles.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        IntBuffer trianglesBuffer = byteBuffer.asIntBuffer();
        trianglesBuffer.put(triangles);
        trianglesBuffer.position(0);

        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, ibo);
        GLES30.glBufferData(
                GLES30.GL_ELEMENT_ARRAY_BUFFER,
                trianglesBuffer.capacity() * 4,
                trianglesBuffer,
                GLES30.GL_STATIC_DRAW);
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    private void generateVaoData() {
        GLES30.glBindVertexArray(vao);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo);
        GLES30.glVertexAttribPointer(
                positionAttribute,
                3,
                GLES30.GL_FLOAT,
                false,
                0, 0
        );
        GLES30.glEnableVertexAttribArray(positionAttribute);
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, ibo);
        GLES30.glBindVertexArray(0);
    }

}
