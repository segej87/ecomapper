package com.gmail.jonsege.androiddatacollection;

import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewMeas extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    //region Class Variables

    // The application
    private MyApplication app;

    // A tag for logging from this activity
    private static final String TAG = "new_meas";

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
    private LocationRequest mLocationRequest;
    private static final int LOCATION_REQUEST_INTERVAL = 10000;
    private static final int LOCATION_REQUEST_FASTEST_INTERVAL = 5000;
    private static final int LOCATION_REQUEST_PRIORITY = LocationRequest.PRIORITY_HIGH_ACCURACY;
    private boolean mRequestingLocationUpdates = true;
    private String mLastUpdateTime;

    // The user's last known location
    Location mLastLocation;
    Location mCurrentLocation;

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

        // Decide to use Play Services Location based on the API level

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
                Log.i(TAG,getString(R.string.load_record_failure));
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
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }

        if (mRequestingLocationUpdates) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mRequestingLocationUpdates) {
            stopLocationUpdates();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        getLastLocation();
        initializeLocationUpdates();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG,getString(R.string.play_services_connection_failure) + connectionResult.getErrorMessage());
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

    private void getLastLocation() {
        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
        } catch (SecurityException e) {
            Log.i(TAG,getString(R.string.last_location_retrieve_failure, e.getLocalizedMessage()));
            checkLocationSettings();
        }
        if (mLastLocation != null) {
            userLoc[0] = mLastLocation.getLongitude();
            userLoc[1] = mLastLocation.getLatitude();
            userLoc[2] = mLastLocation.getAltitude();
        } else {
            Log.i(TAG,getString(R.string.last_location_retrieve_failure));
        }
    }

    private void checkLocationSettings() {
        createLocationRequest();

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        mRequestingLocationUpdates = true;
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            Log.i(TAG,getString(R.string.requesting_settings_change, "Location"));
                            status.startResolutionForResult(
                                    NewMeas.this,
                                    0);
                            mRequestingLocationUpdates = true;
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                            mRequestingLocationUpdates =false;
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        Log.i(TAG,getString(R.string.settings_change_error, "Location"));
                        mRequestingLocationUpdates =false;
                        break;
                }
            }
        });
    }

    private void initializeLocationUpdates() {
        checkLocationSettings();

        Log.i(TAG,getString(R.string.start_location_updates_report, mRequestingLocationUpdates));

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(LOCATION_REQUEST_INTERVAL);
        mLocationRequest.setFastestInterval(LOCATION_REQUEST_FASTEST_INTERVAL);
        mLocationRequest.setPriority(LOCATION_REQUEST_PRIORITY);
    }

    private void startLocationUpdates() {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
            Log.i(TAG,getString(R.string.location_updates_start));
        } catch (SecurityException e) {
            Log.i(TAG,getString(R.string.location_updates_start_error, e.getLocalizedMessage()));
            checkLocationSettings();
        }
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        userLoc[0] = mCurrentLocation.getLongitude();
        userLoc[1] = mCurrentLocation.getLatitude();
        userLoc[2] = mCurrentLocation.getAltitude();
        gpsAcc = mCurrentLocation.getAccuracy();
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
            Log.i(TAG,getString(R.string.general_error_prefix, e.getLocalizedMessage()));
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
                Log.i(TAG,getString(R.string.no_user_location_found));
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
                Log.i(TAG,saveResult);
                moveToAddNew();
            } else {
                Log.i(TAG,getString(R.string.save_record_failure));
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
                Log.i(TAG,getString(R.string.parse_failure, e.getLocalizedMessage()));
            }
            tagArray = (ArrayList<String>) record.props.get("tags");
            accessArray = (ArrayList<String>) record.props.get("access");
            userLoc = record.coords;
            try {
                gpsAcc = Double.valueOf(record.props.get("accuracy").toString());
            } catch (Exception e) {
                Log.i(TAG,getString(R.string.parse_failure, e.getLocalizedMessage()));
            }
        }
    }

    //endregion
}
