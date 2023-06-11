package com.example.peaksrecognition.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.peaksrecognition.R;

import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OpenCVLoader.initDebug();
        setContentView(R.layout.activity_main);
    }

    public void LiveTest_button_onClick(View view) {

    }

    public void DisplayRender_button_onClick(View view) {
        Intent displayRender = new Intent(this, DisplayRenderConfiguration.class);
        startActivity(displayRender);
    }

    public void LocationRotationTest_button_onClick(View view) {
        Intent displayRender = new Intent(this, LocationRotationTestActivity.class);
        startActivity(displayRender);
    }
}