package org.pw.masterthesis.peaksrecognition;

public class Utility {
    public static double[] normalizeVectorDouble(double[] x) {
        int size = x.length;
        return normalizeVectorDouble(x, size);
    }

    public static double[] normalizeVectorDouble(double[] x, int size) {
        double magnitude = 0.0;
        for (int i = 0; i < size; ++i) {
            magnitude += x[i] * x[i];
        }
        magnitude = Math.sqrt(magnitude);

        double[] normalizedX = new double[size];
        for (int i = 0; i < size; ++i) {
            normalizedX[i] = x[i] / magnitude;
        }

        return normalizedX;
    }

    public static double dotProduct(double[] vector1, double[] vector2) {
        return dotProduct(vector1, vector1.length, vector2, vector2.length);
    }

    public static double dotProduct(double[] vector1, int vector1Length, double[] vector2, int vector2Length) {
        double result = 0.0;
        int length = Math.min(vector1Length, vector2Length);

        for (int i = 0; i < length; i++) {
            result += vector1[i] * vector2[i];
        }

        return result;
    }

    public static double[] crossProduct3ElementsVector(double[] vector1, double[] vector2) {
        double x = vector1[1] * vector2[2] - vector1[2] * vector2[1];
        double y = vector1[2] * vector2[0] - vector1[0] * vector2[2];
        double z = vector1[0] * vector2[1] - vector1[1] * vector2[0];

        return new double[]{x, y, z};
    }
}
