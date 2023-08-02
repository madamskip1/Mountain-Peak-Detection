package org.pw.masterthesis.peaksrecognition;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class RotationManager implements SensorEventListener {
    private final SensorManager sensorManager;
    private final Sensor rotationSensor;
    private RotationListener rotationListener;

    public RotationManager(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    public void addRotationListener(RotationListener rotationListener) {
        this.rotationListener = rotationListener;
    }

    public void start() {
        sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float[] rotationVector = calcRotationVector(sensorEvent);
        rotationListener.onRotationChanged(rotationVector);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    private float[] calcRotationVector(SensorEvent sensorEvent) {
        float[] rotationMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, sensorEvent.values);

        float[] adjustedRotationMatrix = new float[9];
        SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z,
                adjustedRotationMatrix);

        float[] orientation = new float[3];
        SensorManager.getOrientation(adjustedRotationMatrix, orientation);

        for (int i = 0; i < 3; i++) {
            orientation[i] = (float) (Math.toDegrees(orientation[i]));
        }

        return orientation;
    }

    public interface RotationListener {
        void onRotationChanged(float[] rotationVector);
    }
}
