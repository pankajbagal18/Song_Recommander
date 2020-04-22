package com.example.songrecommander;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
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

import com.example.songrecommander.predictivemodels.Classification;
import com.example.songrecommander.predictivemodels.TensorFlowClassifier;
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

import org.w3c.dom.Text;

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
    private Button detectButton;
    private Button clearButton;
    private TextView emotionTextView;
    private Button fbAnalysisButton;
    private SquareImageView faceImageView;
    TensorFlowClassifier classifier;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;

    String currentPhotoPath;
    Uri photoURI;
    String imageFileName;
    String timeStamp;
    Bitmap imageBitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logout = (Button)findViewById(R.id.logout);
        mAuth = FirebaseAuth.getInstance();
        welcomeMSG = (TextView)findViewById(R.id.welcomeMSG);
        faceImageView = (SquareImageView)findViewById(R.id.faceImageView);
        detectButton = (Button)findViewById(R.id.detectButton);
        clearButton = (Button)findViewById(R.id.clearButton);
        emotionTextView = (TextView)findViewById(R.id.emotionTextView);
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
        detectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detectFaces();
                detectEmotion();
            }
        });
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearStatus();
            }
        });
        loadModel();
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
        super.onActivityResult(requestCode, resultCode, data);
        Toast.makeText(getApplicationContext(), "Picture Taken", Toast.LENGTH_LONG).show();
        imageBitmap = BitmapFactory.decodeFile(currentPhotoPath);
        faceImageView.setImageBitmap(imageBitmap);
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

    private void loadModel() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier=TensorFlowClassifier.create(getAssets(), "CNN",
                            "opt_em_convnet_5000.pb", "labels.txt", 48,
                            "input", "output_50", true, 7);

                } catch (final Exception e) {
                    //if they aren't found, throw an error!
                    throw new RuntimeException("Error initializing classifiers!", e);
                }
            }
        }).start();
    }
    private SparseArray<Face> detectFaces()
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
            Frame frame = new Frame.Builder().setBitmap(imageBitmap).build();
            faces = faceDetector.detect(frame);
            faceDetector.release();
        }
        if(faces == null)
        {
            Toast.makeText(getApplicationContext(),"No face found",Toast.LENGTH_SHORT).show();
            return null;
        }
        Face detectedFace = faces.valueAt(0);
        Bitmap faceBitmap = Bitmap.createBitmap(imageBitmap,
                (int)detectedFace.getPosition().x,
                (int)detectedFace.getPosition().y,
                (int)detectedFace.getWidth(),
                (int)detectedFace.getHeight()
        );
        faceImageView.setImageBitmap(faceBitmap);
        return faces;
    }
    /**
     * The main function for emotion detection
     */
    private void detectEmotion(){

        Bitmap image=((BitmapDrawable)faceImageView.getDrawable()).getBitmap();
        Bitmap grayImage = toGrayscale(image);
        Bitmap resizedImage=getResizedBitmap(grayImage,48,48);
        int pixelarray[];

        //Initialize the intArray with the same size as the number of pixels on the image
        pixelarray = new int[resizedImage.getWidth()*resizedImage.getHeight()];

        //copy pixel data from the Bitmap into the 'intArray' array
        resizedImage.getPixels(pixelarray, 0, resizedImage.getWidth(), 0, 0, resizedImage.getWidth(), resizedImage.getHeight());


        float normalized_pixels [] = new float[pixelarray.length];
        for (int i=0; i < pixelarray.length; i++) {
            // 0 for white and 255 for black
            int pix = pixelarray[i];
            int b = pix & 0xff;
            //  normalized_pixels[i] = (float)((0xff - b)/255.0);
            // normalized_pixels[i] = (float)(b/255.0);
            normalized_pixels[i] = (float)(b);

        }
        System.out.println(normalized_pixels);
        Log.d("pixel_values",String.valueOf(normalized_pixels));
        String text=null;

        try{
            final Classification res = classifier.recognize(normalized_pixels);
            //if it can't classify, output a question mark
            if (res.getLabel() == null) {
                text = "Status: "+ ": ?\n";
            } else {
                //else output its name
                text = String.format("%s: %s, %f\n", "Status: ", res.getLabel(),
                        res.getConf());
            }}
        catch (Exception  e){
            System.out.print("Exception:"+e.toString());

        }

        //this.faceImageView.setImageBitmap(grayImage);
        emotionTextView.setText(text);
        detectButton.setEnabled(false);
    }
    /**
     *
     * @param bmpOriginal
     * @return
     */
    // https://stackoverflow.com/questions/3373860/convert-a-bitmap-to-grayscale-in-android?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
    public Bitmap toGrayscale(Bitmap bmpOriginal)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    //https://stackoverflow.com/questions/15759195/reduce-size-of-bitmap-to-some-specified-pixel-in-android?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
    public Bitmap getResizedBitmap(Bitmap image, int bitmapWidth, int bitmapHeight) {
        return Bitmap.createScaledBitmap(image, bitmapWidth, bitmapHeight, true);
    }
    private void clearStatus(){
        detectButton.setEnabled(false);
        this.faceImageView.setImageResource(R.drawable.ic_launcher_background);
        this.emotionTextView.setText("Mood ?");

    }
}
