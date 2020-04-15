package com.example.songrecommander;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextView welcomeMSG;
    private Button logout;
    private Button photoButton;
    private Button fbAnalysisButton;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;
    String currentPhotoPath;
    Uri photoURI;
    String imageFileName;
    String timeStamp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logout = (Button)findViewById(R.id.logout);
        mAuth = FirebaseAuth.getInstance();
        welcomeMSG = (TextView)findViewById(R.id.welcomeMSG);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser==null)
        {
            startActivity(new Intent(getApplicationContext(),SignInActivity.class));
            this.finish();
        }
        else
        {
            welcomeMSG.setText("Welcome "+currentUser.getEmail()+" to our app.");
        }

        photoButton = (Button)findViewById(R.id.photoButton);
        fbAnalysisButton = (Button)findViewById(R.id.fbAnalysisButton);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                startActivity(new Intent(MainActivity.this, SignInActivity.class));
                MainActivity.this.finish();
            }
        });
        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });
    }

    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                return;
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
            else
            {
                Toast.makeText(getApplicationContext(),"Exception occurred",Toast.LENGTH_LONG).show();
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            Toast.makeText(getApplicationContext(), "Picture Taken", Toast.LENGTH_LONG).show();
            String photoPath = photoURI.getPath();
            Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
            SparseArray<Face> detectedFaces = detectFaces(bitmap);
            int status = uploadImageToFirebase();
            uploadFacesToFirebase(bitmap, detectedFaces);
            Intent photoAnalysisIntent = new Intent(getApplicationContext(), PhotoAnalysis.class);
            photoAnalysisIntent.putExtra("PHOTO_PATH", currentPhotoPath);
            startActivity(photoAnalysisIntent);
            finish();

    }

    private void uploadFacesToFirebase(Bitmap bitmap, SparseArray<Face> detectedFaces) {

        FirebaseStorage storage = FirebaseStorage.getInstance("gs://song-recommander.appspot.com");
        StorageReference storageReference = storage.getReference();
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


    private SparseArray<Face> detectFaces(Bitmap bitmap)
    {
        SparseArray<Face> faces=null;
        FaceDetector faceDetector = new FaceDetector.Builder(this)
                .setTrackingEnabled(true)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
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

    private int uploadImageToFirebase()
    {
        final int[] successCode = {0};
        FirebaseStorage storage = FirebaseStorage.getInstance("gs://song-recommander.appspot.com");
        StorageReference storageReference = storage.getReference();
        StorageReference imagesRef = storageReference.child(mAuth.getUid() + "/"+timeStamp+"/image/" + photoURI.getLastPathSegment());
        UploadTask uploadTask = imagesRef.putFile(photoURI);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //Toast.makeText(getApplicationContext(),"Uploaded",Toast.LENGTH_LONG).show();
                successCode[0] =1;
            }
        });
        return successCode[0];
    }
    private File createImageFile() throws IOException {
        // Create an image file name
        timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

}
