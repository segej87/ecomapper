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
    private static final String ACTIVITY_RESULT = "isFromActivityResult";
    private boolean isFromActivityResult = false;

    /**
     * The file path to write the photo and return to the New Photo activity
     */
    private static final String OUTPUT_FILE = "outputFilePath";
    private String outputFilePath;

    /**
     * A datetime string for creating the unique file name
     */
    private static final String DATE_TIME = "dateTime";
    private String dateTime;

    /**
     * The Uri for the new photo
     */
    private static final String PHOTO_URI = "photoUri";
    private Uri photoUri;

    //endregion

    //region Initialization

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera);

        if (savedInstanceState != null) {
            // Restore state members from saved instance
            isFromActivityResult = savedInstanceState.getBoolean(ACTIVITY_RESULT);
            outputFilePath = savedInstanceState.getString(OUTPUT_FILE);
            dateTime = savedInstanceState.getString(DATE_TIME);
            photoUri = savedInstanceState.getParcelable(PHOTO_URI);
        } else {
            // Read the datetime string from the calling intent
            dateTime = getIntent().getStringExtra("DATE");
        }

        // Get the package manager to monitor the intent
        pm = getPackageManager();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(!isFromActivityResult){
            dispatchTakePictureIntent();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current state
        savedInstanceState.putBoolean(ACTIVITY_RESULT, isFromActivityResult);
        savedInstanceState.putString(OUTPUT_FILE, outputFilePath);
        savedInstanceState.putString(DATE_TIME, dateTime);
        savedInstanceState.putParcelable(PHOTO_URI, photoUri);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
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
//                photoUri = getUriForFile(CameraActivity.this, "org.koramap.fileprovider", photoFile);

                Log.i(TAG,getString(R.string.camera_start));
                isFromActivityResult = true;

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

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
//        final File root = new File(getFilesDir() + File.separator + "Medias" + File.separator + UserVars.MediasSaveFileName + File.separator);
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
