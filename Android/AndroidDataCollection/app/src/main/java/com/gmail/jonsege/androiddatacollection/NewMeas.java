package com.gmail.jonsege.androiddatacollection;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
                mNameTextField.setText(setDefaultName("Meas"));
            }
        });

        // Set up the save button
        Button mSaveButton = (Button) findViewById(R.id.action_bar_save);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveRecord();
            }
        });

        // Set up the cancel button
        Button mCancelButton = (Button) findViewById(R.id.action_bar_cancel);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveToAddNew();
            }
        });
    }

    //endregion

    //region Navigation

    /**
     * Finishes the NewMeas activity
     */
    @Override
    void moveToAddNew() {
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
        StringBuilder sb = new StringBuilder();
        String delimiter = "";
        for (String v : values) {
            sb.append(delimiter).append(v);
            delimiter = ", ";
        }
        String displayString = sb.toString();

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
     * Changes the accuracy text field to show the current location accuracy
     */
    @Override
    void updateGPSField() {
        mGPSAccField.setText(getString(R.string.gps_acc_starter,String.valueOf(gpsAcc)));
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
            //TODO: present error to user and prevent leaving without fixing double issue
            Log.i(TAG,getString(R.string.general_error_prefix, e.getLocalizedMessage()));
        }

        itemsOut.put("species", mMeasTextField.getText().toString());
        itemsOut.put("units", mUnitsTextField.getText().toString());
    }

    //endregion

    //region Helper Methods

    /**
     * Sets up the activity's toolbar
     */
    private void setUpToolbar () {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.new_toolbar);
        getLayoutInflater().inflate(R.layout.action_bar_new_record, myToolbar);
        myToolbar.setTitle("");
        setSupportActionBar(myToolbar);

        // Set the logged in text
        TextView loggedInText = (TextView) findViewById(R.id.action_bar_title);
        loggedInText.setText(String.format(getString(R.string.logged_in_text_string),UserVars.UName));
    }

    /**
     * Sets up the UI fields in the activity
     */
    @Override
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
            mGPSAccField.setText(getString(R.string.gps_acc_starter, String.valueOf(gpsAcc)));
        } else if (mode.equals("old")) {
            mNameTextField.setText(record.props.get("name").toString());
            mAccessTextField.setText(record.props.get("access").toString());
            mMeasTextField.setText(record.props.get("species").toString());
            mValTextField.setText(record.props.get("value").toString());
            mUnitsTextField.setText(record.props.get("units").toString());
            mNoteTextField.setText(record.props.get("text").toString());
            mTagTextField.setText(record.props.get("tags").toString());
            mGPSAccField.setText(getString(R.string.gps_acc_starter, record.props.get("accuracy").toString()));

            try {
                dateTime = df.parse(record.props.get("datetime").toString());
            } catch (Exception e) {
                Log.i(TAG,getString(R.string.parse_failure, e.getLocalizedMessage()));
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
                Log.i(TAG,getString(R.string.parse_failure, e.getLocalizedMessage()));
            }
        }
    }

    private void setUpPickerButtons() {
        // Set up the tag picker button
        Button mAccessPickerButton = (Button) findViewById(R.id.accessPickerButton);
        mAccessPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToListPicker("access", accessArray);
            }
        });

        Button mSpeciesPickerButton = (Button) findViewById(R.id.measPickerButton);
        mSpeciesPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToListPicker("species", stringFromViewToArray(mMeasTextField));
            }
        });

        Button mUnitsPickerButton = (Button) findViewById(R.id.unitsPickerButton);
        mUnitsPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToListPicker("units", stringFromViewToArray(mUnitsTextField));
            }
        });

        Button mTagsPickerButton = (Button) findViewById(R.id.tagPickerButton);
        mTagsPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToListPicker("tags", tagArray);
            }
        });
    }

    /**
     * Adds a single string to an ArrayList for use by the list picker
     * @param inView TextView
     * @return ArrayList
     */
    private List<String> stringFromViewToArray(TextView inView) {

        // The array to return.
        List<String> outArray = new ArrayList<>();

        // Text in the text view.
        String viewText = inView.getText().toString();

        if (viewText.equals("")) {
            return outArray;
        }

        outArray.add(viewText);
        return outArray;
    }

    //endregion
}
