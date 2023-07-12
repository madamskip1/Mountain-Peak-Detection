package com.example.peaksrecognition.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.peaksrecognition.PeaksRecognizer;
import com.example.peaksrecognition.R;

public class PeaksRecognitionActivity extends AppCompatActivity {
    PeaksRecognizer peaksRecognizer;

    @Override
    protected void onDestroy() {
        super.onDestroy();

        peaksRecognizer.destroy();
    }

    @Override
    protected void onPause() {
        super.onPause();

        peaksRecognizer.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        peaksRecognizer.resume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 123);
        } else {
            // Camera permission is already granted
        }

        setContentView(R.layout.activity_peaks_recognition);
        peaksRecognizer = new PeaksRecognizer(this, this, findViewById(R.id.imageViewRecognize));
        peaksRecognizer.prepareAndStart();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission granted
            } else {
                // Camera permission denied
            }
        }
    }
}