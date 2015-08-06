package com.example.abl1428.thdgeneralocr;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.hardware.Camera;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.opencv.core.Core.BORDER_DEFAULT;
import static org.opencv.core.Core.bitwise_not;
import static org.opencv.imgproc.Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.COLOR_RGB2GRAY;
import static org.opencv.imgproc.Imgproc.MORPH_RECT;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.THRESH_OTSU;
import static org.opencv.imgproc.Imgproc.getStructuringElement;
import static org.opencv.imgproc.Imgproc.rectangle;
import static org.opencv.imgproc.Imgproc.resize;
import static org.opencv.photo.Photo.fastNlMeansDenoising;


public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2, View.OnTouchListener {

    private CameraView mOpenCvCameraView;
    private Mat datMat;
    private Mat original;
    private ImageButton take;
    private ImageButton cancel;
    private TextView portrait;
    private Button barcode;
    private SeekBar seekBar;
    private TextView lScape;
    private ToggleButton toggle;
    private Button readText;


    private final String TAG = "MainActivity";

    private boolean gpsEnabled;
    private boolean threshold  = true;
    private boolean landscape = false;
    private boolean capture = false;
    private int TOLERANCE = 155;
    private Location mLocation;

    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @Override
    public void onStop() {
        super.onStop();
        mOpenCvCameraView.onFinishTemporaryDetach();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (getActionBar() != null) {
            getActionBar().hide();
        }
        setContentView(R.layout.activity_cv);

        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            int change = 100;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                change = progressValue;
                TOLERANCE = change;
                //Toast.makeText(getApplicationContext(), "Changing seekbar's progress", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(getApplicationContext(), "Started tracking seekbar", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                //Toast.makeText(getApplicationContext(), "Stopped tracking seekbar", Toast.LENGTH_SHORT).show();
            }
        });



        cancel = (ImageButton) findViewById(R.id.cancel);
        toggle = (ToggleButton) findViewById(R.id.toggle);
        take = (ImageButton) findViewById(R.id.capture);
        readText = (Button) findViewById(R.id.readText);

        cancel.setVisibility(View.GONE);
        readText.setVisibility(View.GONE);

        mOpenCvCameraView = (CameraView) findViewById(R.id.tutorial1_activity_java_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }


    static{ System.loadLibrary("opencv_java"); }

    BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.setOnTouchListener(MainActivity.this);
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public void onCameraViewStarted(int width, int height) {
        datMat = new Mat();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mOpenCvCameraView.getCamera().stopPreview();
    }



    @Override
    public void onCameraViewStopped() {
    }

    public void switchBarcode(View view) {
//        Intent barcodeActivity = new Intent(this, MainActivity.class);
//        mOpenCvCameraView.onFinishTemporaryDetach();
//        startActivity(barcodeActivity);
    }
    public boolean onTouch(View view, MotionEvent me) {

        if(capture){
        } else {
            List<Camera.Area> one = new ArrayList<Camera.Area>();
            one.add(new Camera.Area(new Rect(800, 0, 0, 1200), 1));
            mOpenCvCameraView.getCamera().getParameters().setFocusAreas(one);
            mOpenCvCameraView.getCamera().getParameters().setFocusMode("FOCUS_MODE_CONTINUOUS_PICTURE");
            Toast.makeText(getApplicationContext(), "Focusing", Toast.LENGTH_SHORT).show();
            Log.d("Camera", "Focusing");
        }
        return false;
    }

    public void enableCapture(View view) {
        capture = true;
        mOpenCvCameraView.getCamera().stopPreview();
        cancel.setVisibility(View.VISIBLE);
        readText.setVisibility(View.VISIBLE);
        toggle.setVisibility(View.GONE);
        take.setVisibility(View.GONE);
        if( mOpenCvCameraView != null ){
            Camera.Parameters params = mOpenCvCameraView.getCamera().getParameters();
            params.setFlashMode( Camera.Parameters.FLASH_MODE_TORCH );
            mOpenCvCameraView.getCamera().setParameters( params );
        }
    }

    public void restartCamera(View view) {
        Log.d(TAG, "no pressed");
        //Restarts the camera to retake picture
        capture = false;
        mOpenCvCameraView.getCamera().startPreview();
        cancel.setVisibility(View.GONE);
        readText.setVisibility(View.GONE);
        toggle.setVisibility(View.VISIBLE);
        seekBar.setVisibility(View.VISIBLE);
        take.setVisibility(View.VISIBLE);
        landscape = false;

    }

    public void toggle(View view) {
        if(threshold) {
            seekBar.setVisibility(View.GONE);
            threshold = false;
        } else {
            seekBar.setVisibility(View.VISIBLE);
            threshold = true;
        }
    }



    public void captureImage(View view) {



        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss_MM-dd-yyyy");
        String currentDateandTime = sdf.format(new Date());
        Mat rotMat = new Mat();
        datMat = datMat.submat(0,800,0,1200);
//        if (!landscape) {
//            Core.flip(datMat.t(), rotMat, 1);
//        } else {
            rotMat = datMat;
  //      }
        //fastNlMeansDenoising(rotMat, rotMat, 6, 7, 21);
        //resize(rotMat, rotMat, new Size(rotMat.width() * 2, rotMat.height() * 2)); // Resizes the image

        String filepath = Environment.getExternalStorageDirectory().getPath() +
                "/" + currentDateandTime + ".jpg";
       // Imgproc.GaussianBlur(rotMat, rotMat, new Size(5, 5), 0);
        Rect boundRect;
        Mat img_Gray = new Mat();
        //Imgproc.blur(rotMat,rotMat,new Size(35,35));
        //grayscale cvtcolor

        Imgproc.Sobel(datMat, datMat, CvType.CV_8U, 1, 0, 3, 1, 0, BORDER_DEFAULT);
        Imgproc.threshold(datMat, datMat, 0, 255, THRESH_OTSU + THRESH_BINARY);
        Mat element = getStructuringElement(MORPH_RECT,new Size(40,40));
        Imgproc.morphologyEx(datMat, datMat, 3, element);
        List<MatOfPoint> list = new ArrayList<MatOfPoint>();
        List<org.opencv.core.Rect> rects = new ArrayList<org.opencv.core.Rect>(list.size());
        Imgproc.findContours(datMat, list, new Mat(), 0, 1);
        int maxWidth = 0;
        int height = 0;
        for(int i = 0; i < list.size(); i++) {
            //Imgproc.approxPolyDP(new MatOfPoint2f(list.get(i)),new MatOfPoint2f(list.get(i)),3,true);
            org.opencv.core.Rect bound = Imgproc.boundingRect(list.get(i));
            rects.add(bound);
            if(bound.width > maxWidth) maxWidth = bound.width;
            height += bound.height;
        }


        Mat largeMat = new Mat(new Size(maxWidth, height),original.type());
        largeMat.setTo(new Scalar(255,255,255));
        Log.d("DIMS", "Maxwidth: "+maxWidth + ", Maxheight"+height);
        height = 0;
        for(org.opencv.core.Rect rekt : rects) {
            //rectangle(datMat, new Point(rekt.x, rekt.y), new Point(rekt.x + rekt.width, rekt.y + rekt.height), new Scalar(60, 60, 60), 10);
            Mat submat = original.submat( rekt.y, rekt.y+rekt.height, rekt.x, rekt.x+rekt.width);

            Mat dstMat = largeMat.submat(height,height+rekt.height,0,rekt.width);
            height += rekt.height;
            submat.copyTo(dstMat);
        }



        Imgcodecs.imwrite(filepath, largeMat);
        capture = false;


        Intent tessIntent = new Intent(this,TesseractActivity.class);
        tessIntent.putExtra(TesseractActivity.FILEPATH, filepath);
        Log.d("CURRENT TOLERANCE", "" + TOLERANCE);
        startActivity(tessIntent);


    }




    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        Mat rgba = inputFrame.rgba();



        if(threshold) {

            //bitwise_not(rgba, datMat);
            Imgproc.cvtColor(rgba, datMat, COLOR_RGB2GRAY);
            Imgproc.adaptiveThreshold(datMat, datMat, 255, ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY, 11, 2);
            if(capture) {
                original = datMat.clone();
            }


            // Imgproc.threshold(datMat, datMat, TOLERANCE, 255, 0);
            // Imgproc.adaptiveThreshold(datMat,datMat,255,ADAPTIVE_THRESH_GAUSSIAN_C,THRESH_BINARY,11,2);


            rectangle(datMat, new Point(0, 0), new Point(1200, 800), new Scalar(TOLERANCE, TOLERANCE, TOLERANCE), 7);
            return datMat;

        }


        return rgba;
    }
    @Override
    public void onResume() {

        cancel.setVisibility(View.GONE);
        readText.setVisibility(View.GONE);
        toggle.setVisibility(View.VISIBLE);
        take.setVisibility(View.VISIBLE);
        seekBar.setVisibility(View.VISIBLE);
        landscape = false;
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

}