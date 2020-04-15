package com.example.songrecommander;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.FaceDetector;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class PhotoAnalysis extends AppCompatActivity {

    private FaceOverlay mFaceOverlayView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_analysis);
        mFaceOverlayView = (FaceOverlay)findViewById(R.id.face_overlay);
        String photoPath = getIntent().getStringExtra("PHOTO_PATH");
        mFaceOverlayView.setBitmap(BitmapFactory.decodeFile(photoPath));

        /*image = (ImageView) findViewById(R.id.currentPhoto);
        String photoPath = getIntent().getStringExtra("PHOTO_PATH");
        if(photoPath==null)
            Toast.makeText(getApplicationContext(),"null image",Toast.LENGTH_LONG).show();
        else {
            Toast.makeText(getApplicationContext(),"path : "+photoPath,Toast.LENGTH_LONG).show();
            int targetW = image.getWidth();
            int targetH = image.getHeight();

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inPurgeable = true;
            Bitmap photoBitmap = BitmapFactory.decodeFile(photoPath,bmOptions);
            image.setImageBitmap(photoBitmap);
        }
        backButton = (Button)findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
            }
        });*/
    }
}
