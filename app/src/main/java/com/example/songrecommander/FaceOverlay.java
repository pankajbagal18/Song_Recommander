package com.example.songrecommander;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.FrameMetrics;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class FaceOverlay extends View {
    private Bitmap mBitmap;
    private SparseArray<Face> mFaces;
    public FaceOverlay(Context context) {
        this(context,null);
    }

    public FaceOverlay(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public FaceOverlay(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public void setBitmap(Bitmap bitmap)
    {
        mBitmap = bitmap;
        FaceDetector faceDetector = new FaceDetector.Builder(getContext())
                .setTrackingEnabled(true)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setMode(FaceDetector.ACCURATE_MODE)
                .build();
        if(!faceDetector.isOperational())
        {

        }
        else
        {
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            mFaces = faceDetector.detect(frame);
            faceDetector.release();
        }
        invalidate();
    }
    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        if((mBitmap!=null)&&(mFaces!=null))
        {
            drawBitmap(canvas);
        }
    }

    private void drawBitmap(Canvas canvas) {
        double viewWidth = canvas.getWidth();
        double viewHeight = canvas.getHeight();
        double imageWidth = mBitmap.getWidth();
        double imageHeight = mBitmap.getHeight();
        int size = 200;
        float left=0,top=0,right=0,bottom=0;
        float offsetTop = 0;
        float offsetBottom = 0;
        Toast.makeText(getContext(),"Face count : "+mFaces.size(),Toast.LENGTH_LONG)
.show();
        canvas.drawBitmap(mBitmap,0,0,null);
        for(int i=0;i<mFaces.size();i++)
        {
            Face face = mFaces.valueAt(i);

            left = (float)(face.getPosition().x);
            top = (float)(face.getPosition().y);
            right = (float)(face.getPosition().x+face.getWidth());
            bottom = (float)(face.getPosition().y+face.getHeight());
            Rect src = new Rect((int)left,(int)top,(int)right,(int)bottom);
            Rect dst = new Rect((int)offsetTop,(int)offsetBottom,size,size);
            offsetTop += size;
            offsetBottom += size;
//          canvas.drawBitmap(mBitmap,src,dst,null);
            canvas.drawRect(left,top,right,bottom,new Paint());
        }
    }
}
