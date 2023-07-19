package org.pw.masterthesis.peaksrecognition.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

import org.pw.masterthesis.peaksrecognition.R;

import java.text.DecimalFormatSymbols;

public class DisplayRenderConfiguration extends AppCompatActivity {

    private EditText latitudeEditText;
    private EditText longitudeEditText;
    private EditText altitudeEditText;

    private EditText yawEditText;
    private EditText pitchEditText;
    private EditText rollEditText;

    private EditText minDistanceEditText;
    private EditText maxDistanceEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_render_configuration);

        latitudeEditText = findViewById(R.id.displayRender_latitude);
        longitudeEditText = findViewById(R.id.displayRender_longitude);
        altitudeEditText = findViewById(R.id.displayRender_altitude);

        yawEditText = findViewById(R.id.displayRender_yaw);
        pitchEditText = findViewById(R.id.displayRender_pitch);
        rollEditText = findViewById(R.id.displayRender_roll);

        minDistanceEditText = findViewById(R.id.displayRender_minDistance);
        maxDistanceEditText = findViewById(R.id.displayRender_maxDistance);
    }

    public void renderButton_onClick(View view) {
        double latitude = getDecimalFromInput(latitudeEditText);
        double longitude = getDecimalFromInput(longitudeEditText);
        double altitude = getDecimalFromInput(altitudeEditText);

        float yaw = (float) getDecimalFromInput(yawEditText);
        float pitch = (float) getDecimalFromInput(pitchEditText);
        float roll = (float) getDecimalFromInput(rollEditText);

        double minDistance = getDecimalFromInput(minDistanceEditText);
        double maxDistance = getDecimalFromInput(maxDistanceEditText);

        boolean edges = ((Switch) findViewById(R.id.displayRender_edgeSwitch)).isChecked();
        boolean peaks = ((Switch) findViewById(R.id.displayRender_peaksSwitch)).isChecked();

        Intent intent = new Intent(this, DisplayRender.class);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        intent.putExtra("altitude", altitude);

        intent.putExtra("yaw", yaw);
        intent.putExtra("pitch", pitch);
        intent.putExtra("roll", roll);

        intent.putExtra("minDistance", minDistance);
        intent.putExtra("maxDistance", maxDistance);

        intent.putExtra("edges", edges);
        intent.putExtra("peaks", peaks);

        startActivity(intent);
    }

    private double getDecimalFromInput(EditText editText) {
        String input = editText.getText().toString();
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        char decimalSeparator = symbols.getDecimalSeparator();
        input = input.replace(',', decimalSeparator);

        return Double.parseDouble(input);
    }

    public void displayRender_fillButton_onClick(View view) {
        final double[] observerLocation = new double[]{49.339045, 20.081936, 991.1};
        final float[] observerRotation = new float[]{144.31152f, 2.3836904f, -2.0597333f};
        final double[] distance = new double[]{0.00001, 30.0};

        latitudeEditText.setText(String.valueOf(observerLocation[0]));
        longitudeEditText.setText(String.valueOf(observerLocation[1]));
        altitudeEditText.setText(String.valueOf(observerLocation[2]));

        yawEditText.setText(String.valueOf(observerRotation[0]));
        pitchEditText.setText(String.valueOf(observerRotation[1]));
        rollEditText.setText(String.valueOf(observerRotation[2]));

        minDistanceEditText.setText(String.valueOf(distance[0]));
        maxDistanceEditText.setText(String.valueOf(distance[1]));
    }
}