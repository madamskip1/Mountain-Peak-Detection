package com.example.peaksrecognition.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.example.peaksrecognition.R;

import java.text.DecimalFormatSymbols;

public class DisplayRenderConfiguration extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_render_configuration);
    }

    public void renderButton_onClick(View view) {
        double latitude = getDecimalFromInput(R.id.displayRender_latitude);
        double longitude = getDecimalFromInput(R.id.displayRender_longitude);
        double altitude = getDecimalFromInput(R.id.displayRender_altitude);

        double yaw = getDecimalFromInput(R.id.displayRender_yaw);
        double pitch = getDecimalFromInput(R.id.displayRender_pitch);
        double roll = getDecimalFromInput(R.id.displayRender_roll);

        double minDistance = getDecimalFromInput(R.id.displayRender_minDistance);
        double maxDistance = getDecimalFromInput(R.id.displayRender_maxDistance);

        Intent intent = new Intent(this, DisplayRender.class);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        intent.putExtra("altitude", altitude);

        intent.putExtra("yaw", yaw);
        intent.putExtra("pitch", pitch);
        intent.putExtra("roll", roll);

        intent.putExtra("minDistance", minDistance);
        intent.putExtra("maxDistance", maxDistance);

        startActivity(intent);
    }

    private double getDecimalFromInput(int editTextID)
    {
        EditText signedDoubleEditText = findViewById(editTextID);
        String input = signedDoubleEditText.getText().toString();
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        char decimalSeparator = symbols.getDecimalSeparator();
        input = input.replace(',', decimalSeparator); // Replace comma with decimal separator if necessary

        return Double.parseDouble(input);
    }
}