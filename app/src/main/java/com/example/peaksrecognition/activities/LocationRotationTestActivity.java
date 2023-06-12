package com.example.peaksrecognition.activities;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.peaksrecognition.LocationManager;
import com.example.peaksrecognition.R;
import com.example.peaksrecognition.RotationManager;

public class LocationRotationTestActivity extends AppCompatActivity {
    TextView latitudeTextView;
    TextView longitudeTextView;
    TextView altitudeTextView;
    TextView yawTextView;
    TextView rollTextView;
    TextView pitchTextView;

    LocationManager locationManager;
    RotationManager rotationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_rotation_test);
        latitudeTextView = findViewById(R.id.location_rotation_test_latitude);
        longitudeTextView = findViewById(R.id.location_rotation_test_longitude);
        altitudeTextView = findViewById(R.id.location_rotation_test_altitude);
        yawTextView = findViewById(R.id.location_rotation_test_yaw);
        rollTextView = findViewById(R.id.location_rotation_test_pitch);
        pitchTextView = findViewById(R.id.location_rotation_test_roll);

        locationManager = new LocationManager(this);
        locationManager.askForLocationPermissions(this, this);
        locationManager.setLocationUpdateCallback(new LocationManager.LocationListener() {
            @Override
            public void onLocationUpdate(Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                double altitude = location.getAltitude();

                latitudeTextView.setText(String.valueOf(latitude));
                longitudeTextView.setText(String.valueOf(longitude));
                altitudeTextView.setText(String.valueOf(altitude));
            }

            @Override
            public void onLocationAvailabilityChange(boolean isAvailable) {

            }
        });

        rotationManager = new RotationManager(this);
        rotationManager.addRotationListener(rotationVector -> {
            Log.d("moje", "dupa");
            yawTextView.setText(String.valueOf(rotationVector[0]));
            rollTextView.setText(String.valueOf(rotationVector[1]));
            pitchTextView.setText(String.valueOf(rotationVector[2]));
        });

        locationManager.start();
        rotationManager.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null)
            locationManager.stop();
        if (rotationManager != null)
            rotationManager.stop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (locationManager != null)
            locationManager.stop();
        if (rotationManager != null)
            rotationManager.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (locationManager != null)
            locationManager.start();
        if (rotationManager != null)
            rotationManager.start();
    }
}