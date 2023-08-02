package org.pw.masterthesis.peaksrecognition.renderer;

import org.opencv.core.Mat;
import org.pw.masterthesis.peaksrecognition.Peaks;
import org.pw.masterthesis.peaksrecognition.mainopengl.Camera;
import org.pw.masterthesis.peaksrecognition.managers.CoordsManager;

public interface Renderer {
    void render();

    Mat getRenderedMat();

    Peaks getPeaks();

    CoordsManager getCoordsManager();

    Camera getCamera();
}
