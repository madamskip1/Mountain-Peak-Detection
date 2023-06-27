package com.example.peaksrecognition.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.peaksrecognition.BlendRenderAndLive;
import com.example.peaksrecognition.R;

public class BlendRenderAndLiveActivity extends AppCompatActivity {
    BlendRenderAndLive blendRenderAndLive;

    @Override
    protected void onDestroy() {
        super.onDestroy();

        blendRenderAndLive.destroy();
    }

    @Override
    protected void onPause() {
        super.onPause();

        blendRenderAndLive.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        blendRenderAndLive.resume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blend_render_and_live);
        blendRenderAndLive = new BlendRenderAndLive(this, this, findViewById(R.id.imageViewBlendRenderLive));
        blendRenderAndLive.prepareAndStart();
    }
}