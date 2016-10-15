package com.gmail.jonsege.androiddatacollection;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jonse on 10/15/2016.
 */

public abstract class NewRecord extends AppCompatActivity {

    //region Class Variables

    // The application
    private MyApplication app;

    // A tag for logging from this activity
    protected String TAG;

    // The current mode of record entry
    protected String mode;

    // Data to construct the record
    protected SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
    protected Date dateTime;
    protected List<String> tagArray = new ArrayList<>();
    protected List<String> accessArray = new ArrayList<>();
    protected Double[] userLoc = new Double[3];
    protected double gpsAcc = 0.0;
    protected Map<String, Object> itemsOut = new HashMap<>();

    // The location object
    private UserLocation userLocation;

    // The record created or loaded by the activity
    protected Record record;

    //endregion

    //region Initialization

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the current application.
        app = (MyApplication) this.getApplicationContext();

        userLocation = new UserLocation(this);

        // Get the current record mode.
        Intent intent = getIntent();
        mode = intent.getStringExtra("MODE");
        if (mode.equals("new")) {
            // Get the date and time this view was created.
            // TODO: allow user to update
            dateTime = new Date();

            userLocation.buildGoogleApiClient();

        } else if (mode.equals("old")) {
            int recordIndex = intent.getIntExtra("INDEX",-1);
            if (recordIndex == -1) {
                Log.i(TAG,getString(R.string.load_record_failure));
            } else {
                record = app.getRecord(recordIndex);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        userLocation.connectToGoogleApi();
    }

    @Override
    protected void onStop() {
        super.onStop();
        userLocation.stopLocationUpdates();
        userLocation.disconnectFromGoogleApi();
    }

    @Override
    protected void onPause() {
        super.onPause();

        userLocation.stopLocationUpdates();
    }

    //endregion

    //region Abstract Methods

    abstract void setUpFields();

    abstract void setItemsOut();

    abstract void moveToAddNew();

    abstract void updateGPSField();

    //endregion

    //region UI Methods

    protected String setDefaultName (String type) {
        String result;
        String dateString = df.format(dateTime);
        result = getString(R.string.default_name,type,dateString);
        return result;
    }

    //endregion

    //region Data I/O

    protected void saveRecord () {
        SaveRecord saveRecord = new SaveRecord(this);
        saveRecord.execute();
    }

    public class SaveRecord extends AsyncTask<NewMeas, Void, Boolean> {
        final NewRecord context;

        SaveRecord(NewRecord context) {
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(NewMeas...Params) {
            setItemsOut();

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

}
