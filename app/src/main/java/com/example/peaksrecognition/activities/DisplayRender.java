package com.example.peaksrecognition.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.example.peaksrecognition.Config;
import com.example.peaksrecognition.R;
import com.example.peaksrecognition.mainopengl.OffScreenRenderer;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

public class DisplayRender extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_render);
        renderToLayout();
    }

    private void renderToLayout()
    {
        Config config = prepareConfig();
        OffScreenRenderer offScreenRenderer = new OffScreenRenderer(this, config);
        offScreenRenderer.render();
        Mat renderedImage = offScreenRenderer.getRenderedMat();
        Bitmap bitmap = Bitmap.createBitmap(renderedImage.cols(), renderedImage.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(renderedImage, bitmap);

        ImageView imageView = findViewById(R.id.displayRenderImageView);
        imageView.setImageBitmap(bitmap);
    }

    private Config prepareConfig()
    {
        Intent intent = getIntent();

        double latitude = intent.getDoubleExtra("latitude", 0.0);
        double longitude = intent.getDoubleExtra("longitude", 0.0);
        double altitude = intent.getDoubleExtra("altitude", 0.0);
        double yaw = intent.getDoubleExtra("yaw", 0.0);
        double pitch = intent.getDoubleExtra("pitch", 0.0);
        double roll = intent.getDoubleExtra("roll", 0.0);
        double minDistance = intent.getDoubleExtra("minDistance", 0.0);
        double maxDistance = intent.getDoubleExtra("maxDistance", 0.0);

        Config config = new Config();
        config.initObserverLocation = new double[] { latitude, longitude, altitude };
        config.initObserverRotation = new double[] { yaw, pitch, roll };
        config.maxDistance = maxDistance;
        config.minDistance = minDistance;
        config.FOVHorizontal = 66.0f;
        config.simplifyFactor = 3;
        config.initHgtSize = 3601;

        return config;
    }

}