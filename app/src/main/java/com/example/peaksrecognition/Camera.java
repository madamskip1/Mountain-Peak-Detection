package com.example.peaksrecognition;

import android.util.Log;

public class Camera {
    private final double fovHorizontal;
    private final double aspectRatio;
    private final double near;
    private final double far;

    private final double[] position;
    private final double[] targetVector;
    private double[] angles;
    private final double[] upVector;
    private double[] directionVector;

    Camera(double fovHorizontal, double aspectRatio, double near, double far) {
        this.fovHorizontal = fovHorizontal;
        this.aspectRatio = aspectRatio;
        this.near = near;
        this.far = far;

        position = new double[]{0.0, 0.0, 0.0};
        angles = new double[]{0.0, 0.0, 0.0};
        upVector = new double[]{0.0, 1.0, 0.0};
        targetVector = new double[]{0.0, 0.0, 0.0};
        directionVector = new double[]{0.0, 0.0, 0.0};
    }

    public void setPosition(double x, double y, double z) {
        position[0] = x;
        position[1] = y;
        position[2] = z;
    }

    public void setAngles(double yawDegree, double pitchDegree, double rollDegree) {
        angles = fixAngles(yawDegree, pitchDegree, rollDegree);
        updateVectors();
    }

    public double[] getProjectionMatrix() {
        double f = 1.0 / Math.tan(Math.toRadians(fovHorizontal / 2.0));

        return new double[]{
                f / aspectRatio, 0.0, 0.0, 0.0,  // col 1
                0.0, f, 0.0, 0.0,  // col 2
                0.0, 0.0, (far + near) / (near - far), -1.0,  // col 3
                0.0, 0.0, (2.0 * far * near) / (near - far), 0.0  // col 4
        };
    }

    public double[] getViewMatrix() {
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

        double xTranslation = Utility.dotProduct(xMatrix, 3, negatedEye, 3);
        double yTranslation = Utility.dotProduct(yMatrix, 3, negatedEye, 3);
        double zTranslation = Utility.dotProduct(zMatrix, 3, negatedEye, 3);

        return new double[]{
                xMatrix[0], yMatrix[0], zMatrix[0], 0.0,  // col 1
                xMatrix[1], yMatrix[1], zMatrix[1], 0.0,  // col 2
                xMatrix[2], yMatrix[2], zMatrix[2], 0.0,  // col 3
                xTranslation, yTranslation, zTranslation, 1.0,  // col 1
        };
    }

    private double[] fixAngles(double yawDegree, double pitchDegree, double rollDegree) {
        yawDegree = 180.0 - yawDegree;
        if (yawDegree < 0.0) {
            yawDegree = 360.0 + yawDegree;
        } else if (yawDegree >= 360.0) {
            yawDegree = yawDegree - 360.0;
        }

        pitchDegree = (-1.0) * pitchDegree;
        rollDegree = (-1.0) * rollDegree;

        return new double[]{yawDegree, pitchDegree, rollDegree};
    }

    private void updateVectors() {
        updateDirectionVector();
        updateUpVector();
        updateTargetVector();
        Log.d("moje", "position: " + position[0] + " " + position[1] + " " + position[2] + " ");
        Log.d("moje", "angles: " + angles[0] + " " + angles[1] + " " + angles[2] + " ");
        Log.d("moje", "upVector: " + upVector[0] + " " + upVector[1] + " " + upVector[2] + " ");
        Log.d("moje", "targetVector: " + targetVector[0] + " " + targetVector[1] + " " + targetVector[2] + " ");
        Log.d("moje", "directionVector: " + directionVector[0] + " " + directionVector[1] + " " + directionVector[2] + " ");
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

        for (int i = 0; i < 3; ++i) {
            upVector[i] = rotationMatrix[i][0] * upVector[0] + rotationMatrix[i][1] * upVector[1] + rotationMatrix[i][2] * upVector[2];
        }


    }

    private void updateTargetVector() {
        for (int i = 0; i < 3; ++i) {
            targetVector[i] = position[i] + directionVector[i];
        }
    }

}
