package com.example.peaksrecognition.terrain;

import android.opengl.GLES20;
import android.opengl.GLES30;

import com.example.peaksrecognition.mainopengl.ShaderProgram;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class DumbTriangle {
    static final int COORDS_PER_VERTEX = 3;
    static float[] triangleCoords = {   // in counterclockwise order:
            0.0f, 0.622008459f, 0.0f, // top
            -0.5f, -0.311004243f, 0.0f, // bottom left
            0.5f, -0.311004243f, 0.0f  // bottom right
    };
    static short[] drawOrder = {0, 1, 2};

    private final FloatBuffer vertexBuffer;
    private final ShortBuffer drawListBuffer;

    private final ShaderProgram shaderClass;

    private int[] buffersIds;
    private int[] vaoIds;

    private int vbo;
    private int ibo;
    private int vao;

    private int positionAttrib;

    public DumbTriangle() {
        shaderClass = new ShaderProgram();
        getAttributesLocations();

        ByteBuffer bb = ByteBuffer.allocateDirect(triangleCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(triangleCoords);
        vertexBuffer.position(0);

        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        generateBuffersIds();
        generateVboData();
        generateIboData();
        generateVaoData();
    }

    public void draw()
    {
        GLES30.glBindVertexArray(vao);
        GLES30.glDrawElements(
                GLES30.GL_TRIANGLES,
                drawOrder.length,
                GLES30.GL_UNSIGNED_SHORT,
                0);
        GLES30.glBindVertexArray(0);
    }



    private void generateBuffersIds() {
        buffersIds = new int[2];
        GLES30.glGenBuffers(2, buffersIds, 0);
        vbo = buffersIds[0];
        ibo = buffersIds[1];

        vaoIds = new int[1];
        GLES30.glGenVertexArrays(1, vaoIds, 0);
        vao = vaoIds[0];
    }

    private void getAttributesLocations()
    {
        positionAttrib = GLES30.glGetAttribLocation(shaderClass.getShaderProgram(), "vPosition");
    }


    private void generateVboData() {
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo);
        GLES30.glBufferData(
                GLES30.GL_ARRAY_BUFFER,
                vertexBuffer.capacity() * 4,
                vertexBuffer,
                GLES30.GL_STATIC_DRAW
        );
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
    }

    private void generateIboData()
    {
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, ibo);
        GLES30.glBufferData(
                GLES30.GL_ELEMENT_ARRAY_BUFFER,
                drawListBuffer.capacity() * 2,
                drawListBuffer,
                GLES30.GL_STATIC_DRAW);
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    private void generateVaoData()
    {
        GLES30.glBindVertexArray(vao);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo);
        GLES30.glVertexAttribPointer(
                positionAttrib,
                3,
                GLES30.GL_FLOAT,
                false,
                0, 0
        );
        GLES30.glEnableVertexAttribArray(positionAttrib);

        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, ibo);

        GLES30.glBindVertexArray(0);
    }

}
