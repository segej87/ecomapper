package com.gmail.jonsege.androiddatacollection;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class NewMeas extends NewRecord {

    //region Class Variables

    // UI elements.
    private EditText mNameTextField;
    private TextView mAccessTextField;
    private TextView mMeasTextField;
    private EditText mValTextField;
    private TextView mUnitsTextField;
    private EditText mNoteTextField;
    private TextView mTagTextField;
    private TextView mGPSAccField;

    // Tag for this class.
    private final String TAG = "new_meas";

    //endregion

    //region Initialization

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.TAG = this.TAG;
        super.type = "Point";
        setContentView(R.layout.activity_new_meas);

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

                mNameTextField.setText(setDefaultName("Meas"));
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
                moveToNotebook();
                return true;
            default:
                return false;
        }
    }

    //endregion

    //region Navigation

    /**
     * Finishes the NewMeas activity
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

        // Construct a string from the ArrayList to put in the text view.
        String displayString = arrayToStringForView(values);

        // Set the appropriate array to the list picker result and add text to the
        // appropriate text view.
        switch(mode) {
            case "access":
                accessArray = values;
                mAccessTextField.setText(displayString);
                break;
            case "species":
                mMeasTextField.setText(displayString);
                break;
            case "units":
                mUnitsTextField.setText(displayString);
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
                getString(R.string.measurement_name_tag)));

        // Set the logged in text
        TextView loggedInText = (TextView) findViewById(R.id.action_bar_logged_in);
        loggedInText.setText(String.format(getString(R.string.logged_in_text_string),UserVars.UName));
    }

    /**
     * Sets up the UI fields in the activity
     */
    @SuppressWarnings("unchecked") void setUpFields () {
        mNameTextField = (EditText) findViewById(R.id.nameTextField);
        mAccessTextField = (TextView) findViewById(R.id.accessTextField);
        mMeasTextField = (TextView) findViewById(R.id.measTextField);
        mValTextField = (EditText) findViewById(R.id.valTextField);
        mUnitsTextField = (TextView) findViewById(R.id.unitsTextField);
        mNoteTextField = (EditText) findViewById(R.id.notesTextField);
        mTagTextField = (TextView) findViewById(R.id.tagTextField);
        mGPSAccField = (TextView) findViewById(R.id.gpsAccView);
        mNameTextField.setHint(getString(R.string.enter_name_hint,getString(R.string.measurement_name_tag)));

        if (mode.equals("new")) {
            mAccessTextField.setText(arrayToStringForView(UserVars.AccessDefaults));
            mTagTextField.setText(arrayToStringForView(UserVars.TagsDefaults));
            mMeasTextField.setText(UserVars.SpecDefault);
            mUnitsTextField.setText(UserVars.UnitsDefault);
            mGPSAccField.setText(getString(R.string.gps_acc_starter, String.valueOf(gpsAcc)));
        } else if (mode.equals("old")) {
            mNameTextField.setText(record.props.get("name").toString());
            mAccessTextField.setText(arrayToStringForView((List<String>) record.props.get("access")));
            mMeasTextField.setText(record.props.get("species").toString());
            mValTextField.setText(record.props.get("value").toString());
            mUnitsTextField.setText(record.props.get("units").toString());
            mNoteTextField.setText(record.props.get("text").toString());
            mTagTextField.setText(arrayToStringForView((List<String>) record.props.get("tags")));
            mGPSAccField.setVisibility(View.GONE);

            try {
                dateTime = df.parse(record.props.get("datetime").toString());
            } catch (Exception e) {
                Log.i(TAG,getString(R.string.parse_failure,
                        "date",
                        e.getLocalizedMessage()));
            }

            try {
                tagArray = (ArrayList<String>) record.props.get("tags");
                accessArray = (ArrayList<String>) record.props.get("access");
            } catch (ClassCastException e) {
                Log.i(TAG,e.getLocalizedMessage());
            }
            userLoc = record.coords;
            try {
                gpsAcc = Double.valueOf(record.props.get("accuracy").toString());
            } catch (Exception e) {
                Log.i(TAG,getString(R.string.parse_failure,
                        "accuracy",
                        e.getLocalizedMessage()));
            }
        }
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

        Button mSpeciesPickerButton = (Button) findViewById(R.id.measPickerButton);
        mSpeciesPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToListPicker(mMeasTextField, "species", stringFromViewToArray(mMeasTextField));
            }
        });

        Button mUnitsPickerButton = (Button) findViewById(R.id.unitsPickerButton);
        mUnitsPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToListPicker(mUnitsTextField, "units", stringFromViewToArray(mUnitsTextField));
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
        mGPSAccField.setText(getString(R.string.gps_acc_starter,String.format("%.2f",gpsAcc)));
    }

    //endregion

    //region Data I/O

    /**
     * Fills the properties map to add to the Record object
     */
    @Override
    void setItemsOut() {
        itemsOut.put("datatype", "meas");
        itemsOut.put("name", mNameTextField.getText().toString());
        itemsOut.put("tags", tagArray);

        String dateOut = df.format(dateTime);
        itemsOut.put("datetime", dateOut);

        itemsOut.put("access", accessArray);
        itemsOut.put("accuracy", gpsAcc);

        itemsOut.put("text", mNoteTextField.getText().toString());

        String value = mValTextField.getText().toString();
        double valueOut;
        try {
            valueOut = Double.parseDouble(value);
            itemsOut.put("value", valueOut);
        } catch (Exception e) {
            Log.i(TAG,getString(R.string.general_error_prefix, e.getLocalizedMessage()));
        }

        itemsOut.put("species", mMeasTextField.getText().toString());
        itemsOut.put("units", mUnitsTextField.getText().toString());
    }

    //endregion

    //region Helper Methods

    @Override
    boolean checkRequiredData() {
        String firstError = "none";
        View errorView = new View(this);

        boolean dateCheck = dateTime != null;

        boolean locCheck = mode.equals("old") || (userOverrideStale || !checkLocationStale());

        boolean valCheck;
        try {
            Double.parseDouble(mValTextField.getText().toString());
            valCheck = true;
        } catch (NumberFormatException e) {
            Log.i(TAG,getString(R.string.parse_failure,
                    getString(R.string.value_header).toLowerCase(),
                    e.getLocalizedMessage()));
            valCheck = false;
        }

        if (!(mNameTextField.getText() != null &&
                !mNameTextField.getText().toString().equals(""))) {
            firstError = "name";
            errorView = mNameTextField;
        } else if (!(accessArray.size() > 0)) {
            firstError = "access";
            errorView = mAccessTextField;
        } else if (!(mMeasTextField.getText() != null &&
                !mMeasTextField.getText().toString().equals(""))) {
            firstError = "species";
            errorView = mMeasTextField;
        } else if (!valCheck) {
            firstError = "value";
            errorView = mValTextField;
        } else if (!(mUnitsTextField.getText() != null &&
                !mUnitsTextField.getText().toString().equals(""))) {
            firstError = "units";
            errorView = mUnitsTextField;
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
            case "species":
                errorString = getString(R.string.spec_field_string);
                break;
            case "value":
                errorString = getString(R.string.wrong_number_format);
                break;
            case "units":
                errorString = getString(R.string.unit_field_string);
                break;
            case "tags":
                errorString = getString(R.string.tag_field_string);
                break;
            default:
                errorString = getString(R.string.field_required,"");
        }

        if (!errorString.equals(getString(R.string.wrong_number_format))) {
            errorString = getString(R.string.field_required,errorString);
        }

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
