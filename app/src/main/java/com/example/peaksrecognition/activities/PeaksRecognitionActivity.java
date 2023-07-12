package com.example.peaksrecognition.activities;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

        setContentView(R.layout.activity_peaks_recogniation);
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