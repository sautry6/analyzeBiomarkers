package com.example.testphoto;

//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
import java.io.IOException;
//import java.io.OutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
import android.graphics.Color;
//import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
//import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

    import android.provider.MediaStore;
    import android.view.View;
    import android.view.View.OnClickListener;
    import android.widget.ImageView;
    import android.widget.ImageButton;
    import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

/** testPhoto
 *      Goals: 1) Capture Image of uPAD
 *             2) Process Image of uPAD
 *             3) Risk Score Calculator
 *      selectImage()
 *          - Function focuses on pressing the Image Capture Button in order to take a photo of the
 *          - uPAD. You can either choose to take a picture of the uPAD through the Camera App on
 *          - the Android or you can choose an already captured image from Gallery.
 *      processImage()
 *          - Function focuses on pressing the Analyze Button in order to take the previously
 *          - captured image and processing it to determine the concentration of each biomarker.
 *          - Concentration is determined based on the color intensity in the captured wells.
 *      riskScore()
 *          - Function focuses on using the biomarker concentrations alongside recorded patient data
 *          - in order to determine a risk score for each patient. This will help to determine if a
 *          - patient is at a higher likelihood of developing preeclampsia later on.*/

    public class MainActivity extends Activity {
        final int TAKE_PICTURE = 1;
        final int ACTIVITY_SELECT_IMAGE = 2;
        private ImageButton capture_btn;
        private ImageView snowflake;
        private ImageButton analyze_btn;

        public void onCreate(Bundle savedBundleInstance) {
            super.onCreate(savedBundleInstance);
            setContentView(R.layout.activity_main);

            // Establish Active Variables in App
            capture_btn = findViewById(R.id.capture_btn);
            snowflake = findViewById(R.id.snowflake);
            analyze_btn = findViewById(R.id.analyze_btn);

            // Function to Capture Image (via Camera or Gallery)
            capture_btn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectImage();
                }
            });

            // Function to Process Image
            analyze_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Must Convert ImageView to Bitmap in Order to Process
                    BitmapDrawable drawable = (BitmapDrawable) snowflake.getDrawable();
                    Bitmap photo = drawable.getBitmap();
                    // After Processing, Processed Image wil Open in New Activity
                    startActivity(new Intent(MainActivity.this, Analyze_Activity.class));
                }
            });

        }
/** selectImage()
 *      Goal: Capture an image of the uPAD through the Android's Camera App or through Gallery
 *                  1. Create Pop-Up with Three Options:
 *                      - Take Photo
 *                      - Choose from Gallery
 *                      - Cancel
 *                  2. Open Corresponding Helper App (Camera or Gallery)
 *                  3. Crop Image to Fit into Established ImageView
 *                  4. Output Image (next function) */
        private void selectImage()
        {   // Set Up Pop-Up with Photo-Taking Options
            final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Photo Options");
                builder.setItems(options, new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        if(options[which].equals("Take Photo"))
                        {   // Initiates Camera App Option
                            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(cameraIntent, TAKE_PICTURE);
                        }
                        else if(options[which].equals("Choose from Gallery"))
                        {   // Initiates Gallery Option
                            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(intent, ACTIVITY_SELECT_IMAGE);
                        }
                        else if (options[which].equals("Cancel"))
                        {   // Closes Pop-Up
                            dialog.dismiss();
                        }

                    }
                });
                builder.show();
        }

        public void onActivityResult(int requestCode, int resultCode, Intent data)
        {
            super.onActivityResult(requestCode, resultCode, data);
            if(resultCode == RESULT_OK)
            {
                if(requestCode == TAKE_PICTURE)
                {   // Takes a Picture with Camera and Crops It to Fit in ImageView Snowflake
                    Bitmap camera = (Bitmap)data.getExtras().get("data");
                    MediaStore.Images.Media.insertImage(getContentResolver(), camera, null, null);
                    Toast.makeText(getApplicationContext(), "Image Saved!", Toast.LENGTH_SHORT).show();
                    Bitmap photo = cropToSquare(camera);
                    snowflake.setImageBitmap(photo);
                }
                else if(requestCode == ACTIVITY_SELECT_IMAGE)
                {
                    Uri selectedImage = data.getData();
                    try {
                        // Allows You to Choose a Photo from the Gallery and Crop It to Fit into Imageview
                        Bitmap gallery = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                        String path = MediaStore.Images.Media.insertImage(getContentResolver(), gallery, null, null);
                        Toast.makeText(getApplicationContext(), "Image Saved!", Toast.LENGTH_SHORT).show();
                        Bitmap photo = cropToSquare(gallery);
                        snowflake.setImageBitmap(photo);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Failed!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

        // Crop Image to Fit in the ImageView Box (copied from StackOverflow)
        private static Bitmap cropToSquare(Bitmap bitmap){
            int width  = bitmap.getWidth();
            int height = bitmap.getHeight();
            int newWidth = (height > width) ? width : height;
            int newHeight = (height > width)? height - (height - width) : height;
            int cropW = (width - height) / 2;
            cropW = (cropW < 0)? 0: cropW;
            int cropH = (height - width) / 2;
            cropH = (cropH < 0)? 0: cropH;
            Bitmap cropImg = Bitmap.createBitmap(bitmap, cropW, cropH, newWidth, newHeight);

            return cropImg;
        }

    /** processImage()
     *      Goal: Analyze the Color Intensity of Each Outer Well in order to Determine Biomarker
     *            Concentrations based on Image Processing and Color Thresholding */
        private void processImage(Bitmap photo) {
            // Establish Variables
            int A, R, G, B;
            int pixel;

            // Create Output Matrices: Used After Feature Detection
            int[][] alpha = new int[photo.getHeight()][photo.getWidth()];
            int[][] red = new int[photo.getHeight()][photo.getWidth()];
            int[][] green = new int[photo.getHeight()][photo.getWidth()];
            int[][] blue = new int[photo.getHeight()][photo.getWidth()];

            int width = photo.getWidth();
            int height = photo.getHeight();

            // Loop for Finding each RGB Value
            for (int x = 0; x < width; ++x) {
                for (int y = 0; y < height; ++y) {
                    // Get Single Pixel
                    pixel = photo.getPixel(x,y);

                    // Get ARGB Values for that Pixel
                    A = Color.alpha(pixel);
                    R = Color.red(pixel);
                    G = Color.green(pixel);
                    B = Color.blue(pixel);

                    // Put Each Value in their Respective Matrix
                    alpha[y][x] = A;
                    red[y][x] = R;
                    green[y][x] = G;
                    blue[y][x] = B;
                }
            }

            // Perform Circle Detection

            // Draw Outline Around Circles

            // Move to Second Activity to Confirm Correct Circle Detection

            // Once Outer Wells are Determined, Put a Box Around Them to Validate
                // If Box is Wrong, Manually Adjust Boxes to Surrount Outer Well (may require another activity)







        }
    }

