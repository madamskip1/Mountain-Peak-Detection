package org.pw.masterthesis.peaksrecognition.mainopengl;

import org.pw.masterthesis.peaksrecognition.Config;
import org.pw.masterthesis.peaksrecognition.Utility;

public class Camera {
    private final double fovVertical;
    private final float aspectRatio;
    private final float near;
    private final float far;
    private final double[] position;
    private final double[] targetVector;
    private final Config.DeviceOrientation deviceOrientation;
    private double[] upVector;
    private float[] angles;
    private float lastRollAngle;
    private double[] directionVector;

    public Camera(double fovVertical, float aspectRatio, float near, float far, Config.DeviceOrientation deviceOrientation) {
        this.fovVertical = fovVertical;
        this.aspectRatio = aspectRatio;
        this.near = near;
        this.far = far;
        this.deviceOrientation = deviceOrientation;

        position = new double[]{0.0, 0.0, 0.0};
        angles = new float[]{0.0f, 0.0f, 0.0f};
        lastRollAngle = 0.0f;
        upVector = new double[]{0.0, 1.0, 0.0};
        targetVector = new double[]{0.0, 0.0, 0.0};
        directionVector = new double[]{0.0, 0.0, 0.0};
    }

    public void setPosition(double x, double y, double z) {
        position[0] = x;
        position[1] = y;
        position[2] = z;
    }

    public void setAngles(float yawDegree, float pitchDegree, float rollDegree) {
        if (deviceOrientation == Config.DeviceOrientation.LANDSCAPE) {
            rollDegree += 90.0f;
        }
        angles = fixAngles(yawDegree, pitchDegree, rollDegree);
        float rollDegreeDiff = angles[2] - lastRollAngle;
        lastRollAngle = angles[2];
        angles[2] = rollDegreeDiff;

        updateVectors();
    }

    public float[] getProjectionMatrix() {
        float f = 1.0f / (float) Math.tan(Math.toRadians(fovVertical / 2.0));

        return new float[]{
                (f / aspectRatio), 0.0f, 0.0f, 0.0f,  // col 1
                0.0f, f, 0.0f, 0.0f,  // col 2
                0.0f, 0.0f, (far + near) / (near - far), -1.0f,  // col 3
                0.0f, 0.0f, (2.0f * far * near) / (near - far), 0.0f  // col 4
        };
    }

    public float[] getViewMatrix() {
        double[] eyeTarget = new double[]{
                position[0] - targetVector[0],
                position[1] - targetVector[1],
                position[2] - targetVector[2]
        };
        double[] negatedEye = new double[]{
                (-1.0) * position[0],
                (-1.0) * position[1],
                (-1.0) * position[2]
        };
        double[] zMatrix = Utility.normalizeVectorDouble(eyeTarget, 3);
        double[] xMatrix = Utility.normalizeVectorDouble(Utility.crossProduct3ElementsVector(upVector, zMatrix), 3);
        double[] yMatrix = Utility.crossProduct3ElementsVector(zMatrix, xMatrix);

        float xTranslation = (float) Utility.dotProduct(xMatrix, 3, negatedEye, 3);
        float yTranslation = (float) Utility.dotProduct(yMatrix, 3, negatedEye, 3);
        float zTranslation = (float) Utility.dotProduct(zMatrix, 3, negatedEye, 3);

        return new float[]{
                -(float) xMatrix[0], -(float) yMatrix[0], (float) zMatrix[0], 0.0f,  // col 1
                -(float) xMatrix[1], -(float) yMatrix[1], (float) zMatrix[1], 0.0f,  // col 2
                -(float) xMatrix[2], -(float) yMatrix[2], (float) zMatrix[2], 0.0f,  // col 3
                -xTranslation, -yTranslation, zTranslation, 1.0f,  // col 1
        };
    }

    private float[] fixAngles(float yawDegree, float pitchDegree, float rollDegree) {
        yawDegree = 180.0f - yawDegree;
        if (yawDegree < 0.0f) {
            yawDegree = 360.0f + yawDegree;
        } else if (yawDegree >= 360.0f) {
            yawDegree = yawDegree - 360.0f;
        }

        pitchDegree = (-1.0f) * pitchDegree;
        rollDegree = (-1.0f) * rollDegree;

        return new float[]{yawDegree, pitchDegree, rollDegree};
    }

    private void updateVectors() {
        updateDirectionVector();
        updateUpVector();
        updateTargetVector();
    }

    private void updateDirectionVector() {
        double yawRadians = Math.toRadians(angles[0]);
        double pitchRadians = Math.toRadians(angles[1]);

        double[] direction = new double[3];
        direction[0] = Math.cos(yawRadians) * Math.cos(pitchRadians);
        direction[1] = Math.sin(pitchRadians);
        direction[2] = Math.sin(yawRadians) * Math.cos(pitchRadians);

        double magnitude = Math.sqrt(direction[0] * direction[0] + direction[1] * direction[1] + direction[2] * direction[2]);

        direction[0] /= magnitude;
        direction[1] /= magnitude;
        direction[2] /= magnitude;

        directionVector = direction;
    }

    private void updateUpVector() {
        double rollRadians = Math.toRadians(angles[2]);
        double cosAngle = Math.cos(rollRadians);
        double sinAngle = Math.sin(rollRadians);

        double x = directionVector[0];
        double y = directionVector[1];
        double z = directionVector[2];

        double[][] rotationMatrix = new double[][]{
                {(cosAngle + x * x * (1 - cosAngle)), (x * y * (1 - cosAngle) - z * sinAngle), (x * z * (1 - cosAngle) + y * sinAngle)},
                {(y * x * (1 - cosAngle) + z * sinAngle), (cosAngle + y * y * (1 - cosAngle)), (y * z * (1 - cosAngle) - x * sinAngle)},
                {(z * x * (1 - cosAngle) - y * sinAngle), (z * y * (1 - cosAngle) + x * sinAngle), (cosAngle + z * z * (1 - cosAngle))}
        };

        double[] newUpVector = new double[3];
        for (int i = 0; i < 3; ++i) {
            newUpVector[i] = rotationMatrix[i][0] * upVector[0] + rotationMatrix[i][1] * upVector[1] + rotationMatrix[i][2] * upVector[2];
        }
        upVector = newUpVector;
    }

    private void updateTargetVector() {
        for (int i = 0; i < 3; ++i) {
            targetVector[i] = position[i] + directionVector[i];
        }
    }
}
