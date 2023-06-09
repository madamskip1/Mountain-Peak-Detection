package com.example.peaksrecognition.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

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
        OffScreenRenderer offScreenRenderer = new OffScreenRenderer(this);
        offScreenRenderer.render();
        Mat renderedImage = offScreenRenderer.getRenderedMat();
        Bitmap bitmap = Bitmap.createBitmap(renderedImage.cols(), renderedImage.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(renderedImage, bitmap);

        ImageView imageView = findViewById(R.id.displayRenderImageView);
        imageView.setImageBitmap(bitmap);
    }
}