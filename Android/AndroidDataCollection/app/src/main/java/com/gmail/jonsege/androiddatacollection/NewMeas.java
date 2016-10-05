package com.gmail.jonsege.androiddatacollection;

import android.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NewMeas extends AppCompatActivity {

    //region Class Variables

    // UI elements.
    EditText mNameTextField;
    TextView mAccessTextField;
    TextView mMeasTextField;
    EditText mValTextField;
    TextView mUnitsTextField;
    EditText mNoteTextField;
    TextView mTagTextField;

    // Data to construct the record
    Date dateTime;
    List<String> tagArray = new ArrayList<>();
    List<String> accessArray = new ArrayList<>();
    Double[] userLoc = new Double[3];
    double gpsAcc = 0.0;

    // The record (either passed in or constructed)
    Record record;

    //endregion

    //region Initialization

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_meas);

        // Get the date and time this view was created.
        // TODO: allow user to update
        dateTime = new Date();

        // Initialize the text fields and set hints if necessary.
        mNameTextField = (EditText) findViewById(R.id.nameTextField);
        mNameTextField.setHint(getString(R.string.enter_name_hint,"measurement"));

        mAccessTextField = (TextView) findViewById(R.id.accessTextField);
        mMeasTextField = (TextView) findViewById(R.id.measTextField);
        mValTextField = (EditText) findViewById(R.id.valTextField);
        mUnitsTextField = (TextView) findViewById(R.id.unitsTextField);
        mNoteTextField = (EditText) findViewById(R.id.notesTextField);
        mTagTextField = (TextView) findViewById(R.id.tagTextField);

        // Set up the default name button
        Button mDefaultNameButton = (Button) findViewById(R.id.defaultNameButton);
        mDefaultNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDefaultName();
            }
        });

        constructRecord();
        System.out.println(record.props.toString());
    }

    //endregion

    //region Helper Methods

    private void setDefaultName () {
        String dateFormat = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat df = new SimpleDateFormat(dateFormat);
        String dateString = df.format(dateTime);
        this.mNameTextField.setText(getString(R.string.default_name,"Meas",dateString));
    }

    private void constructRecord() {
        Map<String, Object> propsOut = new HashMap<>();
        propsOut.put("datatype", "meas");
        propsOut.put("name", mNameTextField.getText().toString());
        propsOut.put("tags", tagArray);

        String dateFormat = "yyyy-MM-dd HH:mm:ss z";
        SimpleDateFormat df = new SimpleDateFormat(dateFormat);
        String dateOut = df.format(dateTime);
        propsOut.put("datetime", dateOut);

        propsOut.put("access", accessArray);
        propsOut.put("accuracy", gpsAcc);
        propsOut.put("text", mNoteTextField.getText().toString());

        String value = mValTextField.getText().toString();
        double valueOut;
        try {
            valueOut = Double.parseDouble(value);
            propsOut.put("value", valueOut);
        } catch (Exception e) {
            //TODO: present error to user and prevent leaving without fixing double issue
            System.out.println(getString(R.string.general_error_prefix) + e.getLocalizedMessage());
        }

        propsOut.put("species", mMeasTextField.getText().toString());
        propsOut.put("units", mUnitsTextField.getText().toString());

        this.record = new Record(userLoc, null, propsOut);
    }

    //endregion
}
