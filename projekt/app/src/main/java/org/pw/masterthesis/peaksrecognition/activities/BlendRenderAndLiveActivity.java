package org.pw.masterthesis.peaksrecognition.activities;

import android.os.Bundle;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;

import org.pw.masterthesis.peaksrecognition.R;
import org.pw.masterthesis.peaksrecognition.activities_frame_analysers.BlendRenderAndLive;

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
        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_blend_render_and_live);
        blendRenderAndLive = new BlendRenderAndLive(this, this, findViewById(R.id.imageViewBlendRenderLive));
        blendRenderAndLive.prepareAndStart();
    }
}