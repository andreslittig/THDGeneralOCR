package com.example.abl1428.thdgeneralocr;
import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;

import org.opencv.android.JavaCameraView;

public class CameraView extends JavaCameraView {

    private static final String TAG = "Tutorial3View";

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public Camera getCamera() {
        return mCamera;
    }

}