package com.kora.android;

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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NewNote extends NewRecord {

    //region Class Variables

    /**
     * Tag for this activity
     */
    private final String TAG = "new_note";

    /**
     * UI elements
     */
    private EditText mNameTextField;
    private TextView mAccessTextField;
    private EditText mNoteTextField;
    private TextView mTagTextField;
    private TextView mGPSAccField;
    private TextView mGPSStabField;

    //endregion

    //region Initialization

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.TAG = this.TAG;
        super.type = "Point";
        setContentView(R.layout.activity_new_note);

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

                mNameTextField.setText(setDefaultName("Note"));
                mNameTextField.clearFocus();
            }
        });
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
                    showConfirmDialog(CANCEL_REQUEST,
                            getString(R.string.cancel_confirmation_message),
                            getString(R.string.cancel_positive_string),
                            getString(R.string.cancel_negative_string));
                else
                    moveToNotebook();
                return true;
            default:
                return false;
        }
    }

    //endregion

    //region Navigation

    /**
     * Finishes the NewNote activity
     */
    @Override
    void moveToNotebook() {
        super.finish();
        this.finish();
    }

    /**
     * Handles values supplied to the NewMeas activity by a ListPickerActivity
     * @param mode mode
     * @param values values
     */
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
                getString(R.string.note_name_tag)));

        // Set the logged in text
        TextView loggedInText = (TextView) findViewById(R.id.action_bar_logged_in);
        loggedInText.setText(String.format(getString(R.string.logged_in_text_string),UserVars.UName));
    }

    /**
     * Sets up the UI fields in the activity
     */
    @SuppressWarnings("unchecked") private void setUpFields () {

        // Pull the views from the layout
        mNameTextField = (EditText) findViewById(R.id.nameTextField);
        mAccessTextField = (TextView) findViewById(R.id.accessTextField);
        mNoteTextField = (EditText) findViewById(R.id.notesTextField);
        mTagTextField = (TextView) findViewById(R.id.tagTextField);
        GridLayout mGPSReportLayout = (GridLayout) findViewById(R.id.gpsReportGrid);
        mGPSAccField = (TextView) findViewById(R.id.gpsAccReport);
        mGPSStabField = (TextView) findViewById(R.id.gpsStabReport);
        mNameTextField.setHint(getString(R.string.enter_name_hint,getString(R.string.note_name_tag)));

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

            // Hide the gps layout
            mGPSReportLayout.setVisibility(View.GONE);
        }

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
        itemsOut.put("datatype", "note");
        itemsOut.put("name", mNameTextField.getText().toString());
        itemsOut.put("tags", tagArray);

        String dateOut = df.format(dateTime);
        itemsOut.put("datetime", dateOut);

        itemsOut.put("access", accessArray);
        itemsOut.put("accuracy", gpsAcc);

        itemsOut.put("text", mNoteTextField.getText().toString());
    }

    //endregion

    //region Helper Methods

    @Override
    boolean checkRequiredData() {

        String firstError = "none";
        View errorView = new View(this);

        boolean dateCheck = dateTime != null;

        boolean locCheck = mode.equals("old") || (userOverrideStale || checkLocationOK());

        if (!(mNameTextField.getText() != null &&
                !mNameTextField.getText().toString().equals(""))) {
            firstError = "name";
            errorView = mNameTextField;
        } else if (!(accessArray.size() > 0)) {
            firstError = "access";
            errorView = mAccessTextField;
        } else if (!(mNoteTextField.getText() != null &&
                !mNoteTextField.getText().toString().equals(""))) {
            firstError = "note";
            errorView = mNoteTextField;
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
            case "note":
                errorString = getString(R.string.note_field_string);
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
        }

        errorView.requestFocus();

        return (firstError.equals("none") && dateCheck && locCheck);
    }

    //endregion
}