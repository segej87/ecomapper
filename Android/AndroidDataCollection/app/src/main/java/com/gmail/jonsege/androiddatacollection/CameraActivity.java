package com.gmail.jonsege.androiddatacollection;

import android.app.Activity;
import android.content.ComponentCallbacks2;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * Created by jonse on 10/27/2016.
 */
public class CameraActivity extends AppCompatActivity implements ComponentCallbacks2 {

    /**
     * A tag for this activity
     */
    private final String TAG = "camera";

    private PackageManager pm;
    private String outputFilePath;
    private boolean isFromActivityResult = false;
    Intent resultIntent;
    private String dateTime;

    /**
     * Request code for taking a new photo
     */
    private final int REQUEST_IMAGE_CAPTURE = 200;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        pm = getPackageManager();
        dateTime = getIntent().getStringExtra("DATE");
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!isFromActivityResult){
            dispatchTakePictureIntent();
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

                Log.e(TAG, "What the fuck??");
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

    public void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (photoFile != null) {
//                Uri photoURI = FileProvider.getUriForFile(this,
//                        "org.koramap.fileprovider",
//                        photoFile);

                Uri photoURI = Uri.fromFile(photoFile);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                resultIntent = new Intent();
                resultIntent.putExtra("PATH", outputFilePath);
                setResult(Activity.RESULT_OK, resultIntent);
                isFromActivityResult = true;
                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String imageFileName = createUniquePhotoName();

        final File root = new File(Environment.getExternalStorageDirectory() + File.separator + UserVars.MediasSaveFileName + File.separator);
        root.mkdirs();
        final File sdImageMainDirectory = new File(root, imageFileName);
        outputFilePath = sdImageMainDirectory.getAbsolutePath();
        return sdImageMainDirectory;
    }

    private String createUniquePhotoName() {
        return "Photo_" + dateTime.
                replaceAll("-", "").
                replaceAll(":", "").
                replaceAll(" ", "_") + ".jpg";
    }
}
