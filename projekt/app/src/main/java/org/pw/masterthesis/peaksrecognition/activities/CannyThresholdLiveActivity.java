package org.pw.masterthesis.peaksrecognition.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Window;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.opencv.android.OpenCVLoader;
import org.pw.masterthesis.peaksrecognition.R;
import org.pw.masterthesis.peaksrecognition.activities_frame_analysers.CannyThresholdLive;

public class CannyThresholdLiveActivity extends AppCompatActivity {
    private static final int MY_CAMERA_REQUEST_CODE = 100;

    static {
        OpenCVLoader.initDebug();
    }

    TextView lowTextView;
    TextView highTextView;
    SeekBar lowBar;
    SeekBar highBar;
    int lowThreshold = 0;
    int highThreshold = 0;
    CannyThresholdLive cannyThresholdLive;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        } else {
            // Camera permission is already granted
        }
        setContentView(R.layout.activity_canny_threshold_live);
        lowTextView = findViewById(R.id.liveThresholdLowText);
        highTextView = findViewById(R.id.liveThresholdHighText);
        lowTextView.setText(String.valueOf(lowThreshold));
        highTextView.setText(String.valueOf(highThreshold));

        lowBar = findViewById(R.id.liveThresholdLowBar);
        highBar = findViewById(R.id.liveThresholdHighBar);

        lowBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                lowTextView.setText(String.valueOf(i));
                lowThreshold = i;
                cannyThresholdLive.setThreshold(lowThreshold, highThreshold);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        highBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                highTextView.setText(String.valueOf(i));
                highThreshold = i;
                cannyThresholdLive.setThreshold(lowThreshold, highThreshold);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        // checking if the permission has already been granted
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        }

        cannyThresholdLive = new CannyThresholdLive(this, findViewById(R.id.cannyLiveTestImageView), 640, 480);
        cannyThresholdLive.setThreshold(lowThreshold, highThreshold);
        cannyThresholdLive.start();
    }

}