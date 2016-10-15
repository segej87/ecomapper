package com.gmail.jonsege.androiddatacollection;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewMeas extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    //region Class Variables

    // The application
    private MyApplication app;

    // UI elements.
    private EditText mNameTextField;
    private TextView mAccessTextField;
    private TextView mMeasTextField;
    private EditText mValTextField;
    private TextView mUnitsTextField;
    private EditText mNoteTextField;
    private TextView mTagTextField;
    private TextView mGPSAccField;

    // Data to construct the record
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
    private Date dateTime;
    private List<String> tagArray = new ArrayList<>();
    private List<String> accessArray = new ArrayList<>();
    private Double[] userLoc = new Double[3];
    private double gpsAcc = 0.0;
    private Map<String, Object> itemsOut = new HashMap<>();

    // Variables for the user location request
    private GoogleApiClient mGoogleApiClient;

    // The user's last known location
    Location mLastLocation;
    Double mLatitude;
    Double mLongitude;

    // The record (either passed in or constructed)
    private Record record;

    //endregion

    //region Initialization

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_meas);

        // Get the current application.
        app = (MyApplication) this.getApplicationContext();

        //Set up the toolbar.
        Toolbar myToolbar = (Toolbar) findViewById(R.id.new_toolbar);
        getLayoutInflater().inflate(R.layout.action_bar_new_record, myToolbar);
        myToolbar.setTitle("");
        setSupportActionBar(myToolbar);

        // Set the logged in text
        TextView loggedInText = (TextView) findViewById(R.id.action_bar_title);
        loggedInText.setText(String.format(getString(R.string.logged_in_text_string),UserVars.UName));

        // Get the current record mode.
        Intent intent = getIntent();
        String mode = intent.getStringExtra("MODE");
        if (mode.equals("new")) {
            // Get the date and time this view was created.
            // TODO: allow user to update
            dateTime = new Date();

            buildGoogleApiClient();

        } else if (mode.equals("old")) {
            int recordIndex = intent.getIntExtra("INDEX",-1);
            if (recordIndex == -1) {
                System.out.println(getString(R.string.load_record_failure));
            } else {
                record = app.getRecord(recordIndex);
            }
        }

        // Initialize the text fields and set hints if necessary.
        setUpFields(mode);

        // Set up the default name button
        Button mDefaultNameButton = (Button) findViewById(R.id.defaultNameButton);
        mDefaultNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDefaultName();
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

    //region Google API Methods

    @Override
    protected void onStart() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        getUserLocation();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        System.out.println("Connection to Play Services failed: " + connectionResult.getErrorMessage());
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.disconnect();
    }

    //endregion

    //region Navigation

    private void moveToAddNew() {
        this.finish();
    }

    private void returnFromListPicker(String mode, List<String> values) {
        StringBuilder sb = new StringBuilder();
        String delim = "";
        for (String v : values) {
            sb.append(delim).append(v);
            delim = ", ";
        }

        String dispString = sb.toString();

        if (mode == "access") {
            mAccessTextField.setText(dispString);
        } else if (mode == "species") {
            mMeasTextField.setText(dispString);
        }else if (mode == "units") {
            mUnitsTextField.setText(dispString);
        } else if (mode == "tags") {
            mTagTextField.setText(dispString);
        }
    }

    //endregion

    //region Location Methods

    protected synchronized void buildGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    private void getUserLocation() {
        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 0);
        }

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            mLatitude = mLastLocation.getLatitude();
            mLongitude = mLastLocation.getLongitude();
        } else {
            System.out.println("Could not retrieve user's location");
        }
    }

    //endregion

    //region Data I/O

    private void setItemsOut() {
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
            System.out.println(getString(R.string.general_error_prefix) + e.getLocalizedMessage());
        }

        itemsOut.put("species", mMeasTextField.getText().toString());
        itemsOut.put("units", mUnitsTextField.getText().toString());
    }

    private void saveRecord () {
        SaveRecord saveRecord = new SaveRecord(this);
        saveRecord.execute();
    }

    public class SaveRecord extends AsyncTask<NewMeas, Void, Boolean> {
        final NewMeas context;

        SaveRecord(NewMeas context) {
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(NewMeas...Params) {
            context.setItemsOut();

            if (userLoc[0] == null) {
                System.out.println("No user location found. Sending to null island");
                userLoc[0] = 0.0;
                userLoc[1] = 0.0;
                userLoc[2] = 0.0;
            }

            context.record = new Record(userLoc, null, itemsOut);
            return context.record.props.get("name").equals(itemsOut.get("name").toString());
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                app.addRecord(record);
                String saveResult = DataIO.saveRecords(this.context, app.getRecords());
                System.out.println(saveResult);
                moveToAddNew();
            } else {
                System.out.println(getString(R.string.save_record_failure));
            }
        }

        @Override
        protected void onCancelled() {

        }
    }

    //endregion

    //region Helper Methods

    private void setDefaultName () {
        String dateString = df.format(dateTime);
        this.mNameTextField.setText(getString(R.string.default_name,"Meas",dateString));
    }

    private void setUpFields (String mode) {
        mNameTextField = (EditText) findViewById(R.id.nameTextField);
        mAccessTextField = (TextView) findViewById(R.id.accessTextField);
        mMeasTextField = (TextView) findViewById(R.id.measTextField);
        mValTextField = (EditText) findViewById(R.id.valTextField);
        mUnitsTextField = (TextView) findViewById(R.id.unitsTextField);
        mNoteTextField = (EditText) findViewById(R.id.notesTextField);
        mTagTextField = (TextView) findViewById(R.id.tagTextField);
        mGPSAccField = (TextView) findViewById(R.id.gpsAccView);
        mNameTextField.setHint(getString(R.string.enter_name_hint,"measurement"));

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
                System.out.println(getString(R.string.parse_failure, e.getLocalizedMessage()));
            }
            tagArray = (ArrayList<String>) record.props.get("tags");
            accessArray = (ArrayList<String>) record.props.get("access");
            userLoc = record.coords;
            try {
                gpsAcc = Double.valueOf(record.props.get("accuracy").toString());
            } catch (Exception e) {
                System.out.println(getString(R.string.parse_failure, e.getLocalizedMessage()));
            }
        }
    }

    //endregion
}
