package com.kora.android;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import org.jetbrains.annotations.Contract;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NewPhoto extends NewRecord {

    //region Class Variables

    /**
     * Tag for this activity
     */
    private final String TAG = "new_photo";

    /**
     * UI elements
     */
    private EditText mNameTextField;
    private TextView mAccessTextField;
    private ImageButton mPhotoButton;
    private EditText mNoteTextField;
    private TextView mTagTextField;
    private TextView mGPSAccField;
    private TextView mGPSStabField;

    /**
     * Request code for taking a new photo
     */
    private final int CAMERA_IDENTIFIER = 199;

    /**
     * Make sure that only one camera is open
     */
    private boolean alreadyInCamera = false;

    //endregion

    //region Initialization

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.TAG = this.TAG;
        super.type = "Point";
        setContentView(R.layout.activity_new_photo);

        //Set up the toolbar.
        setUpToolbar();

        // Initialize the text fields and set hints if necessary.
        setUpFields();

        // Set up the picker buttons
        setUpPickerButtons();

        // Set up the default name button
        Button mDefaultNameButton = (Button) findViewById(R.id.defaultNameButton);
        mDefaultNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mNameTextField.getError() != null) {
                    mNameTextField.setError(null);
                }

                mNameTextField.setText(setDefaultName("Photo"));
                mNameTextField.clearFocus();
            }
        });

        // Set up the photo button
        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!alreadyInCamera) {
                    Intent camera = new Intent(NewPhoto.this, CameraActivity.class);
                    camera.putExtra("DATE", df.format(dateTime));

                    alreadyInCamera = true;
                    startActivityForResult(camera, CAMERA_IDENTIFIER);
                }
            }
        });
    }

    protected void onStart() {
        super.onStart();
        alreadyInCamera = false;
    }

    //endregion

    //region Menu Methods

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu_record, menu);
        return true;
    }

    /**
     * Handles calls from the options menu
     * @param item option
     * @return result
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_record:
                saveRecord();
                return true;
            case R.id.cancel_record:
                if (mode.equals("new"))
                    DataIO.deleteFile(mPhoto);
                moveToNotebook();
                return true;
            default:
                return false;
        }
    }

    //endregion

    //region Navigation

    /**
     * Finishes the NewPhoto activity
     */
    @Override
    void moveToNotebook() {
        super.finish();
        this.finish();
    }

    @Override
    protected void returnFromListPicker(String mode, List<String> values) {
        String displayString = arrayToStringForView(values);

        switch(mode) {
            case "access":
                accessArray = values;
                mAccessTextField.setText(displayString);
                break;
            case "tags":
                tagArray = values;
                mTagTextField.setText(displayString);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case LIST_PICKER_REQUEST_CODE:
                    String mode = data.getStringExtra("MODE");
                    List<String> result = data.getStringArrayListExtra("RESULT");
                    returnFromListPicker(mode, result);
                    break;
                case CAMERA_IDENTIFIER:
                    mPhoto = data.getStringExtra("PATH");
                    setImageButton();
                    break;
            }
        }
    }

    //endregion

    //region UI Methods

    /**
     * Sets up the activity's toolbar
     */
    private void setUpToolbar () {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.new_toolbar);
        getLayoutInflater().inflate(R.layout.action_bar_new_record, myToolbar);
        myToolbar.setTitle("");
        setSupportActionBar(myToolbar);

        // Set the title
        String titleType;
        switch (mode) {
            case "new":
                titleType = getString(R.string.title_constructor_new);
                break;
            case "old":
                titleType = getString(R.string.title_constructor_editing);
                break;
            default:
                titleType = "";
        }
        TextView title = (TextView) findViewById(R.id.action_bar_title);
        title.setText(getString(R.string.title_constructor,
                titleType,
                getString(R.string.photo_name_tag)));

        // Set the logged in text
        TextView loggedInText = (TextView) findViewById(R.id.action_bar_logged_in);
        loggedInText.setText(String.format(getString(R.string.logged_in_text_string),UserVars.UName));
    }

    /**
     * Sets up the UI fields in the activity
     */
    @SuppressWarnings("unchecked") private void setUpFields () {

        // Pull the views from the layout.
        mNameTextField = (EditText) findViewById(R.id.nameTextField);
        mAccessTextField = (TextView) findViewById(R.id.accessTextField);
        mPhotoButton = (ImageButton) findViewById(R.id.photoButton);
        mNoteTextField = (EditText) findViewById(R.id.notesTextField);
        mTagTextField = (TextView) findViewById(R.id.tagTextField);
        GridLayout mGPSReportLayout = (GridLayout) findViewById(R.id.gpsReportGrid);
        mGPSAccField = (TextView) findViewById(R.id.gpsAccReport);
        mGPSStabField = (TextView) findViewById(R.id.gpsStabReport);
        mNameTextField.setHint(getString(R.string.enter_name_hint,getString(R.string.photo_name_tag)));

        if (mode.equals("new")) {
            if (!isFromSavedState) {
                // Set default values for items
                accessArray = UserVars.AccessDefaults;
                tagArray = UserVars.TagsDefaults;
            }

            // Set up the GPS section
            updateGPSField();
        } else if (mode.equals("old")) {
            if (!isFromSavedState) {
                try {
                    mNameTextField.setText(record.props.get("name").toString());
                    accessArray = (ArrayList<String>) record.props.get("access");
                    mNoteTextField.setText(record.props.get("text").toString());
                    tagArray = (ArrayList<String>) record.props.get("tags");
                    mPhoto = record.photoPath;
                } catch (Exception e) {
                    Log.i(TAG, e.getLocalizedMessage());
                }

                try {
                    dateTime = df.parse(record.props.get("datetime").toString());
                } catch (Exception e) {
                    Log.i(TAG, getString(R.string.parse_failure,
                            "date",
                            e.getLocalizedMessage()));
                }

                userLoc = record.coords;

                try {
                    gpsAcc = Double.valueOf(record.props.get("accuracy").toString());
                } catch (Exception e) {
                    Log.i(TAG, getString(R.string.parse_failure,
                            "accuracy",
                            e.getLocalizedMessage()));
                }
            }

            // Disable the take photo option
            mPhotoButton.setEnabled(false);

            // Hide the gps layout
            mGPSReportLayout.setVisibility(View.GONE);
        }

        setImageButton();

        mAccessTextField.setText(arrayToStringForView(accessArray));
        mTagTextField.setText(arrayToStringForView(tagArray));
    }

    private void setUpPickerButtons() {
        // Set up the tag picker button
        Button mAccessPickerButton = (Button) findViewById(R.id.accessPickerButton);
        mAccessPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToListPicker(mAccessTextField, "access", accessArray);
            }
        });

        Button mTagsPickerButton = (Button) findViewById(R.id.tagPickerButton);
        mTagsPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToListPicker(mTagTextField, "tags", tagArray);
            }
        });
    }

    /**
     * Changes the accuracy text field to show the current location accuracy
     */
    @Override
    void updateGPSField() {
        String accOutString;
        if (gpsAcc == -1)
            accOutString = getString(R.string.gps_locking);
        else
            accOutString = getString(R.string.gps_w_unit,String.format(Locale.getDefault(),"%.2f",gpsAcc));

        mGPSAccField.setText(accOutString);
        if (gpsAcc == -1 || gpsAcc > UserVars.minGPSAccuracy) {
            mGPSAccField.setTextColor(ContextCompat.getColor(this, R.color.dark_red));
        } else {
            mGPSAccField.setTextColor(ContextCompat.getColor(this, R.color.dark_green));
        }

        String stabOutString;
        if (gpsAcc == -1)
            stabOutString = getString(R.string.gps_locking);
        else
            stabOutString = getString(R.string.gps_w_unit,String.format(Locale.getDefault(),"%.2f",gpsStab));

        mGPSStabField.setText(stabOutString);
        if (gpsStab == -1 || gpsStab > UserVars.minGPSStability) {
            mGPSStabField.setTextColor(ContextCompat.getColor(this, R.color.dark_red));
        } else {
            mGPSStabField.setTextColor(ContextCompat.getColor(this, R.color.dark_green));
        }
    }

    //endregion

    //region Data I/O

    /**
     * Fills the properties map to add to the Record object
     */
    @Override
    void setItemsOut() {
        itemsOut.put("datatype", "photo");

        String dateOut = df.format(dateTime);
        itemsOut.put("datetime", dateOut);

        itemsOut.put("name", mNameTextField.getText().toString());

        itemsOut.put("access", accessArray);

        itemsOut.put("text", mNoteTextField.getText().toString());

        itemsOut.put("tags", tagArray);

        itemsOut.put("accuracy", gpsAcc);

        // Link the photo's local and blob urls
        String blobPath = UserVars.blobRootString +
                UserVars.UUID + "/" +
                mPhoto.substring(mPhoto.lastIndexOf('/') + 1);

        itemsOut.put("filepath", blobPath);

        UserVars.Medias.put(blobPath, mPhoto);
    }

    //endregion

    //region Helper Methods

    /**
     * Sets the image of the photo button
     */
    private void setImageButton() {
        if (mPhoto != null) {
            if ((new File(mPhoto)).exists()) {

                // First decode to check image dimensions
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(mPhoto, options);

                // Calculate inSampleSize
                //TODO: Figure out how to calculate reqWidth and reqHeight
                options.inSampleSize = calculateInSampleSize(options);

                // Decode bitmap with inSampleSize set
                options.inJustDecodeBounds = false;
                mPhotoButton.setBackground(null);

                Bitmap imageIn = BitmapFactory.decodeFile(mPhoto, options);

                String cacheKey = mPhoto.substring(mPhoto.lastIndexOf('/') + 1).
                        replaceAll(".jpg","").
                        replaceAll("_","");

                ((KoraApplication) this.getApplicationContext()).addBitmapToMemoryCache(cacheKey, imageIn);
                mPhotoButton.setImageBitmap(BitmapFactory.decodeFile(mPhoto, options));
            }
        }
    }

    @Contract(pure = true)
    private static int calculateInSampleSize(BitmapFactory.Options options) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;

        final int reqWidth = 300;
        final int reqHeight = 300;

        int inSampleSize = 1;

        final int halfHeight = height / 2;
        final int halfWidth = width / 2;

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while ((halfHeight / inSampleSize) >= reqHeight
                && (halfWidth / inSampleSize) >= reqWidth) {
            inSampleSize *= 2;
        }

        return inSampleSize;
    }

    @Override
    boolean checkRequiredData() {
        String firstError = "none";
        View errorView = new View(this);

        boolean dateCheck = dateTime != null;


        boolean locCheck = mode.equals("old") || userOverrideStale || checkLocationOK();

        if (!(mNameTextField.getText() != null &&
                !mNameTextField.getText().toString().equals(""))) {
            firstError = "name";
            errorView = mNameTextField;
        } else if (!(accessArray.size() > 0)) {
            firstError = "access";
            errorView = mAccessTextField;
        } else if (mPhoto == null || !(new File(mPhoto)).exists()) {
            firstError = "photo";
            errorView = mPhotoButton;
        } else if (!(tagArray.size() > 0)) {
            firstError = "tags";
            errorView = mTagTextField;
        }

        String errorString;
        switch(firstError) {
            case "name":
                errorString = getString(R.string.name_field_string);
                break;
            case "access":
                errorString = getString(R.string.access_field_string);
                break;
            case "photo":
                errorString = getString(R.string.photo_field_string);
                break;
            case "tags":
                errorString = getString(R.string.tag_field_string);
                break;
            default:
                errorString = getString(R.string.field_required,"");
        }

        errorString = getString(R.string.field_required,errorString);

        if (errorView instanceof EditText) {
            ((EditText) errorView).setError(errorString);
        } else if (errorView instanceof TextView) {
            ((TextView) errorView).setError(errorString);
        } else if (errorView instanceof ImageButton) {
            errorView.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_red));
        }

        errorView.requestFocus();

        return (firstError.equals("none") && dateCheck && locCheck);
    }

    //endregion
}