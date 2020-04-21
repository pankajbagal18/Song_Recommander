package com.example.songrecommander;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class PhotoAnalysis extends AppCompatActivity {

    private ImageView parentImage;
    private ListView faceList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_analysis);
        parentImage = (ImageView)findViewById(R.id.currentPhoto);
        //faceList = (ListView)findViewById(R.id.imageDesListView);
        Bitmap currentPhotoBitmap = BitmapFactory.decodeFile(getIntent().getStringExtra("PHOTO_PATH"));
        parentImage.setImageBitmap(currentPhotoBitmap);
        SparseArray<Face> detectedFaces = detectFaces(currentPhotoBitmap);
    }



    /*
    private void uploadFacesToFirebase(Bitmap bitmap, SparseArray<Face> detectedFaces) {

        FirebaseStorage storage = FirebaseStorage.getInstance("gs://song-recommander.appspot.com");
        StorageReference storageReference = storage.getReference();
        faceRef = mAuth.getUid() + "/"+timeStamp+"/faces/face";
        for(int i=0;i<detectedFaces.size();i++)
        {

            Face detectedFace = detectedFaces.valueAt(i);
            Bitmap faceBitmap = Bitmap.createBitmap(bitmap,
                    (int)detectedFace.getPosition().x,
                    (int)detectedFace.getPosition().y,
                    (int)detectedFace.getWidth(),
                    (int)detectedFace.getHeight()
            );
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            faceBitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
            byte[] data = byteArrayOutputStream.toByteArray();
            StorageReference imagesRef = storageReference.child(mAuth.getUid() + "/"+timeStamp+"/faces/face"+(i+1)+".JPEG" );

            UploadTask uploadTask = imagesRef.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_LONG).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //Toast.makeText(getApplicationContext(),"Uploaded",Toast.LENGTH_LONG).show();

                }
            });
        }
    }
*/
    private SparseArray<Face> detectFaces(Bitmap bitmap)
    {
        SparseArray<Face> faces=null;
        com.google.android.gms.vision.face.FaceDetector faceDetector = new com.google.android.gms.vision.face.FaceDetector.Builder(this)
                .setTrackingEnabled(true)
                .setLandmarkType(com.google.android.gms.vision.face.FaceDetector.ALL_LANDMARKS)
                .setMode(FaceDetector.ACCURATE_MODE)
                .build();
        if(!faceDetector.isOperational())
        {
            Toast.makeText(this,"FaceDetector is not working",Toast.LENGTH_SHORT).show();
        }
        else
        {
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            faces = faceDetector.detect(frame);
            faceDetector.release();
        }
        return faces;
    }

}
