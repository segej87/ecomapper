package com.kora.android;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentCallbacks2;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

/**
 * Created for the Kora project by jonse on 10/27/2016.
 */
public class CameraActivity extends AppCompatActivity implements ComponentCallbacks2 {

    //region Class Variables

    /**
     * A tag for this activity
     */
    private final String TAG = "camera";

    /**
     * Request code for taking a new photo
     */
    private final int REQUEST_IMAGE_CAPTURE = 250;

    /**
     * Request code for cropping a photo
     */
    private final int REQUEST_IMAGE_CROP = 251;

    /**
     * A package manager to check if the camera is already open
     */
    private PackageManager pm;

    /**
     * A flag indicating whether the activity is being called from the camera
     */
    private boolean isFromActivityResult = false;

    /**
     * The file path to write the photo and return to the New Photo activity
     */
    private String outputFilePath;

    /**
     * A datetime string for creating the unique file name
     */
    private String dateTime;

    /**
     * The Uri for the new photo
     */
    private Uri photoUri;

    //endregion

    //region Initialization

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Get the package manager to monitor the intent
        pm = getPackageManager();

        // Read the datetime string from the calling intent
        dateTime = getIntent().getStringExtra("DATE");
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!isFromActivityResult){
            dispatchTakePictureIntent();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFromActivityResult) {
            //TODO: Implement ContentProvider!!
        }
    }

    /**
     * Release memory when the UI becomes hidden or when system resources become low.
     * @param level the memory-related event that was raised.
     */
    public void onTrimMemory(int level) {

        // Determine which lifecycle or system event was raised.
        switch (level) {

            case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:

                /*
                   Release any UI objects that currently hold memory.

                   The user interface has moved to the background.
                */

                break;

            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE:
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW:
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL:
                System.gc();

                /*
                   Release any memory that your app doesn't need to run.

                   The device is running low on memory while the app is running.
                   The event raised indicates the severity of the memory-related event.
                   If the event is TRIM_MEMORY_RUNNING_CRITICAL, then the system will
                   begin killing background processes.
                */

                break;

            case ComponentCallbacks2.TRIM_MEMORY_BACKGROUND:
            case ComponentCallbacks2.TRIM_MEMORY_MODERATE:
                System.gc();
            case ComponentCallbacks2.TRIM_MEMORY_COMPLETE:

                /*
                   Release as much memory as the process can.

                   The app is on the LRU list and the system is running low on memory.
                   The event raised indicates where the app sits within the LRU list.
                   If the event is TRIM_MEMORY_COMPLETE, the process will be one of
                   the first to be terminated.
                */
                System.gc();

                break;

            default:
                /*
                  Release any non-critical data structures.

                  The app received an unrecognized memory level value
                  from the system. Treat this as a generic low-memory message.
                */
                break;
        }
    }

    //endregion

    //region Navigation

    /**
     * Opens the camera app to take a picture
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(pm) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (photoFile != null) {
                photoUri = Uri.fromFile(photoFile);

                Log.i(TAG,getString(R.string.camera_start));
                isFromActivityResult=true;

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println("Result code " +
                resultCode +
                " after request code " +
                requestCode +
                ". Success code is " + RESULT_OK);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE:
                    isFromActivityResult = true;
                    try {
                        performCrop();
                    } catch (Exception e) {
                        Log.e(TAG, e.getLocalizedMessage());
                    }
                    break;
                case REQUEST_IMAGE_CROP:
                    isFromActivityResult = true;
                    try {
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("PATH", outputFilePath);
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    } catch (Exception e) {
                        Log.e(TAG, e.getLocalizedMessage());
                    }
                    break;
            }
        } else {
            Log.i(TAG, getString(R.string.camera_cancel));
            isFromActivityResult = true;
            switch (requestCode) {
                case REQUEST_IMAGE_CROP:
                    DataIO.deleteFile(outputFilePath);
                    break;
            }
            finish();
        }
    }

    private void performCrop() {
        try {
            Log.i(TAG, "Starting crop");
            Intent cropIntent = new Intent("com.android.camera.action.CROP");

            // Set options for the crop intent.
            cropIntent.setDataAndType(photoUri, "image/*");
            cropIntent.putExtra("crop", "true");
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            cropIntent.putExtra("outputX", 256);
            cropIntent.putExtra("outputY", 256);
            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

            // Start the crop intent for result
            startActivityForResult(cropIntent, REQUEST_IMAGE_CROP);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "This device doesn't support the crop feature");
            Toast toast = Toast.makeText(this, "This device doesn't support the crop feature", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    //endregion

    //region Helper Methods

    private String createUniquePhotoName() {
        return "Photo_" + dateTime.
                replaceAll("-", "").
                replaceAll(":", "").
                replaceAll(" ", "_") + ".jpg";
    }

    private File createImageFile() {
        // Create an image file name
        String imageFileName = createUniquePhotoName();

        final File root = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator + UserVars.MediasSaveFileName + File.separator);
        boolean fileCheck = root.exists();
        if (!fileCheck) {
            fileCheck = root.mkdirs();
        }

        File noMedFile = new File(root + File.separator + ".nomedia");
        boolean noMedCheck = noMedFile.exists();
        if (!noMedCheck) {
            try {
                noMedCheck = noMedFile.createNewFile();
                if (noMedCheck) {
                    Log.i(TAG,getString(R.string.create_file_success,noMedFile.getAbsolutePath()));
                }
            } catch (java.io.IOException e) {
                Log.e(TAG,getString(R.string.create_file_fail,noMedFile.getAbsolutePath()));
            }
        }

        final File sdImageMainDirectory = new File(root, imageFileName);
        if (fileCheck) {
            outputFilePath = sdImageMainDirectory.getAbsolutePath();
        }

        return sdImageMainDirectory;
    }

    //endregion
}
