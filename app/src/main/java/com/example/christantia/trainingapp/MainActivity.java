package com.example.christantia.trainingapp;

import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.hardware.Camera;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.EditText;
import android.widget.FrameLayout;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

public class MainActivity extends AppCompatActivity implements Camera.PictureCallback {
    private static final String DEBUG_TAG = "TrainingApp";
    private String user = "";
    private String trial = "";
    private float x = 0;
    private float y = 0;
    File file;
    File directory;
    File dir;
    File dir2;
    private Camera cam = null;
    private CameraPreview mPreview;
    private String timeTouched;
    int dummy = 0;
    View shape;
    //Animation animation1;
    ScaleAnimation animation1;
    File root = android.os.Environment.getExternalStorageDirectory();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //setup folder
        // Find the root of the external storage.

        Log.d(DEBUG_TAG,"External file system root: " +root);

        directory = new File (root.getAbsolutePath() + "/Experiment");
        directory.mkdirs();

        //take in user input
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("User Name");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                user = input.getText().toString();
                dir = new File (directory + "/" + user);
                dir.mkdirs();
            }
        });

        AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        builder2.setTitle("Trial Number");

        // Set up the input
        final EditText input2 = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input2.setInputType(InputType.TYPE_CLASS_TEXT);
        builder2.setView(input2);

        // Set up the buttons
        builder2.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                trial = input2.getText().toString();
                dir2 = new File (dir + "/" + trial);
                dir2.mkdirs();
                file = new File(dir2, "myData.txt");
                View decorView = getWindow().getDecorView();
                // Hide the status bar.
                int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
                decorView.setSystemUiVisibility(uiOptions);
                // Remember that you should never show the action bar if the
                // status bar is hidden, so hide that too if necessary.
                getSupportActionBar().hide();
            }
        });
        builder2.show();
        builder.show();


        //setup camera
        initializeCamera();

        // Create our Preview view and set it as the content of our activity.
        shape = (View)findViewById(R.id.shape);
        //animation1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale);
        animation1 = new ScaleAnimation(1.0f, 0.5f, 1.0f, 0.5f, Animation.ABSOLUTE, (float) (shape.getX() + 37.50), Animation.ABSOLUTE, (float) (shape.getY() + 37.50));
        animation1.setDuration(750);
        animation1.setRepeatCount(-1);
        animation1.setRepeatMode(Animation.REVERSE);
        //animation1.setInterpolator(new LinearInterpolator());

        shape.startAnimation(animation1);

    }

    /*@Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

        // Get the Camera instance as the activity achieves full user focus
        try {
            cam.reconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    /*@Override
    public void onPause(){
        super.onPause();
        cam.release();
    }*/

    private void initializeCamera(){
        Log.d(DEBUG_TAG,"Initializing camera");
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    cam = Camera.open(camIdx);
                } catch (RuntimeException e) {
                    Log.e(DEBUG_TAG, "Camera failed to open: " + e.getLocalizedMessage());
                }
            }
        }
        mPreview = new CameraPreview(this, cam);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        Log.d(DEBUG_TAG,"TOUCH");
        /*View shape2 = (View) findViewById(R.id.shape2);
        View shape3 = (View) findViewById(R.id.shape3);*/
        if (event.getAction() == MotionEvent.ACTION_UP){
            Log.d(DEBUG_TAG,"Action was UP");

            //capture image
            timeTouched= System.currentTimeMillis() + "";
            x = shape.getX() + 50;
            y = shape.getY() + 50;
            String position = (int) x + " " + (int) y + " " + timeTouched;

            if (cam == null)
                initializeCamera();
            cam.takePicture(null, null, this);

            if (dummy % 2 == 0)
                writeTextFile(position);
            dummy++;

            //set new position
            shape.clearAnimation();
            animation1.cancel();
            shape.setX(ThreadLocalRandom.current().nextInt(0,
                    Resources.getSystem().getDisplayMetrics().widthPixels) + 1);
            shape.setY(ThreadLocalRandom.current().nextInt(0,
                    Resources.getSystem().getDisplayMetrics().heightPixels) + 1);
            Log.d(DEBUG_TAG, "PIVOT =" + shape.getPivotX() + " " + shape.getPivotY());
            animation1 = new ScaleAnimation(1.0f, 0.5f, 1.0f, 0.5f, Animation.ABSOLUTE, (float) (shape.getX() + 25), Animation.ABSOLUTE, (float) (shape.getY()) + 25);
            animation1.setDuration(750);
            animation1.setRepeatCount(-1);
            animation1.setRepeatMode(Animation.REVERSE);
            Log.d(DEBUG_TAG, "PIVOT =" + shape.getPivotX() + " " + shape.getPivotY());
            shape.startAnimation(animation1);

            /*shape2.setX(event.getX() - 50);
            shape2.setY(event.getY() - 50);
            shape3.setX(x);
            shape3.setY(y);*/
            return true;
        }
        return super.dispatchTouchEvent(event);
    }

    private void writeTextFile(String position){

        try {
            FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("\n" + position);
            bw.flush();
            bw.close();
            fw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i(DEBUG_TAG, "******* File not found.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(DEBUG_TAG,"File written to "+file);
    }

    //private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

    public void onPictureTaken(byte[] data, Camera camera) {
        Log.d(DEBUG_TAG, "Picture taken");
        File pictureFile = new File(dir2, timeTouched + ".png");
        //dummy = dummy + 1;
        if (pictureFile == null){
            Log.d(DEBUG_TAG, "Error creating media file, check storage permissions");
            return;
        }

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(DEBUG_TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(DEBUG_TAG, "Error accessing file: " + e.getMessage());
        }
        cam.stopPreview();
        cam.release();

        cam = null;
    }


}
