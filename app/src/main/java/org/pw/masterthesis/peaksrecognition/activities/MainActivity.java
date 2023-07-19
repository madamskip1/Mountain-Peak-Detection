package org.pw.masterthesis.peaksrecognition.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.OpenCVLoader;
import org.pw.masterthesis.peaksrecognition.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OpenCVLoader.initDebug();
        setContentView(R.layout.activity_main);
    }

    public void LiveTest_button_onClick(View view) {
        Intent peaksRecognitionIntent = new Intent(this, PeaksRecognitionActivity.class);
        startActivity(peaksRecognitionIntent);
    }

    public void DisplayRender_button_onClick(View view) {
        Intent displayRenderIntent = new Intent(this, DisplayRenderConfiguration.class);
        startActivity(displayRenderIntent);
    }

    public void LocationRotationTest_button_onClick(View view) {
        Intent locationRotationTestIntent = new Intent(this, LocationRotationTestActivity.class);
        startActivity(locationRotationTestIntent);
    }

    public void DisplayRenderLive_button_onClick(View view) {
        Intent displayRenderLiveIntent = new Intent(this, DisplayRenderLiveActivity.class);
        startActivity(displayRenderLiveIntent);
    }

    public void LiveCamera_button_onClick(View view) {
        Intent liveCameraIntent = new Intent(this, CannyThresholdLiveActivity.class);
        startActivity(liveCameraIntent);
    }

    public void BlendRenderAndLive_button_onClick(View view) {
        Intent blendRenderIntent = new Intent(this, BlendRenderAndLiveActivity.class);
        startActivity(blendRenderIntent);
    }

    public void CannyThresholdsLive_onClick(View view) {
        Intent cannyThresholdsLiveIntent = new Intent(this, CannyThresholdLiveActivity.class);
        startActivity(cannyThresholdsLiveIntent);
    }
}