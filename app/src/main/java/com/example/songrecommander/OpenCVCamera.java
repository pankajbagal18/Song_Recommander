package com.example.songrecommander;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class OpenCVCamera extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {


    private static final String TAG = "OCVSample::Activity";
    private CameraBridgeViewBase mOpenCvCameraView;
    private boolean mIsJavaCamera = true;
    Mat mRgba;
    Mat mRgbaF;
    Mat mRgbaT;
    private BaseLoaderCallback mLoaderCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status)
            {
                case BaseLoaderCallback.SUCCESS:
                {
                    Log.i(TAG,"OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                }
                break;
                default:
                {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_c_v_camera2);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ActivityCompat.requestPermissions(this,new String[]{
            Manifest.permission.CAMERA
        },1);

        mOpenCvCameraView = (JavaCameraView)findViewById(R.id.openCvCameraView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        //mOpenCvCameraView.enableView();
        //mOpenCvCameraView.enableFpsMeter();
    }

    @Override
    public void onPause()
    {

        super.onPause();
        if(mOpenCvCameraView!=null)
            mOpenCvCameraView.disableView();
    }


    @Override
    public void onResume()
    {

        super.onResume();
        if(!OpenCVLoader.initDebug())
        {
            Log.d(TAG,"OpenCV Library not found");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0,this,mLoaderCallBack);
        }
        else
        {
            mLoaderCallBack.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.d("Pankaj","Pankaj Camera");
        //Toast.makeText(this,"Camera Started",Toast.LENGTH_LONG).show();
        mRgba = new Mat(height,width, CvType.CV_8UC4);
        mRgbaT = new Mat(height,width, CvType.CV_8UC4);
        mRgbaF = new Mat(height,width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Log.d("Bagal","Bagal Frame");
        //Toast.makeText(this,"Frame Captured",Toast.LENGTH_LONG).show();
        mRgba = inputFrame.rgba();
        Core.transpose(mRgba,mRgbaT);
        Imgproc.resize(mRgbaT,mRgbaF,mRgbaF.size(),0,0,0);
        Core.flip(mRgbaF,mRgba,1);
        return  inputFrame.rgba();
    }
}
