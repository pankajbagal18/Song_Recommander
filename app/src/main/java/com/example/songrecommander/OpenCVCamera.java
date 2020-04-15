package com.example.songrecommander;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.provider.ContactsContract;
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
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class OpenCVCamera extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {


    private static final String TAG = "OCVSample::Activity";
    private CameraBridgeViewBase mOpenCvCameraView;
    private CascadeClassifier cascadeClassifier;
    private boolean mIsJavaCamera = true;
    private int absoluteFaceSize;
    Mat mRgba;
    Mat mGrey;
    private BaseLoaderCallback mLoaderCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == BaseLoaderCallback.SUCCESS) {
                Log.i(TAG, "OpenCV loaded successfully");
                mOpenCvCameraView.enableView();
                initializeOpenCVDependencies();
            } else {
                super.onManagerConnected(status);
            }
        }
    };

    private void initializeOpenCVDependencies() {
        try
        {
            InputStream is  = getResources().openRawResource(R.raw.lbpcascade_frontalface);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir,"lbpcascade_frontalface.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead=is.read(buffer))!=-1)
            {
                os.write(buffer,0,bytesRead);
            }
            is.close();
            os.close();
            cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());

        }
        catch (Exception e)
        {
            Log.e("OpenCVCamera","Error loading cascade",e);
        }
    }

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
        mRgba = new Mat(height,width,CvType.CV_8UC4);
        mGrey = new Mat(height,width,CvType.CV_8UC4);
        absoluteFaceSize = (int)(height*0.2);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
        mGrey.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Log.d("Bagal","Bagal Frame");
        //Toast.makeText(this,"Frame Captured",Toast.LENGTH_LONG).show();
        mRgba = inputFrame.rgba();
        mGrey = inputFrame.gray();
        Core.transpose(mRgba,mRgba);
        Core.flip(mRgba,mRgba,1);
        //detect face
        MatOfRect faceDetections = new MatOfRect();
        cascadeClassifier.detectMultiScale(mRgba,faceDetections);
        for(Rect rect:faceDetections.toArray())
        {
            Imgproc.rectangle(mRgba,
                    new Point(rect.x,rect.y),
                    new Point(rect.x+rect.width,rect.y+rect.height),
                    new Scalar(255,0,0));
        }
        return mRgba;
    }
}
