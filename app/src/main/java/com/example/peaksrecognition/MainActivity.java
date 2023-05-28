package com.example.peaksrecognition;

import androidx.appcompat.app.AppCompatActivity;

import android.opengl.GLSurfaceView;
import android.os.Bundle;

import com.example.peaksrecognition.mainopengl.MyGLSurfaceView;
import com.example.peaksrecognition.terrain.TerrainLoader;

public class MainActivity extends AppCompatActivity {

    private GLSurfaceView glView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TerrainLoader terrainLoader = new TerrainLoader(this);

        glView = new MyGLSurfaceView(this);
        setContentView(glView);
    }
}