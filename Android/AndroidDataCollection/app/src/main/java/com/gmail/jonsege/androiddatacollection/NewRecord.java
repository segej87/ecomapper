package com.gmail.jonsege.androiddatacollection;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

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

    /**
     * The application context
     */
    private MyApplication app;

    /**
     * Tag for this activity. Will be replaced by child class.
     */
    protected String TAG = "new_record";

    /**
     * The current mode of record entry (new or old)
     */
    protected String mode;

    /**
     * Data to construct the record
     */
    protected String type;
    protected final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
    protected Date dateTime;
    protected List<String> tagArray = new ArrayList<>();
    protected List<String> accessArray = new ArrayList<>();
    protected Double[] userLoc = new Double[3];
    protected double gpsAcc = 0.0;
    protected Map<String, Object> itemsOut = new HashMap<>();
    protected String mPhoto = null;

    /**
     * The location object
     */
    private UserLocation userLocation;

    /**
     * The record created or loaded by the user
     */
    protected Record record;

    /**
     * The index of the record in the app's record list (for old records only
     */
    int recordIndex = -1;

    //endregion

    //region Initialization

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the current application.
        app = (MyApplication) this.getApplicationContext();

        // Create an object to handle location requests
        userLocation = new UserLocation(this);

        // Get the current record mode.
        Intent intent = getIntent();
        mode = intent.getStringExtra("MODE");

        // If the mode is new, get the current datetime and start tracking location.
        switch(mode) {
            case "new":
                // Get the date and time this view was created.
                // TODO: allow user to update datetime
                dateTime = new Date();

                // Tell the User Location class to get the user's location.
                userLocation.setRequestingLocation(true);

                // Build a Google API Client to connect to Play Services.
                userLocation.buildGoogleApiClient();
                break;
            case "old":
                // Get the index of the record to load
                recordIndex = intent.getIntExtra("INDEX",-1);

                // If the index is -1, return an error. Otherwise, load the record.
                if (recordIndex == -1) {
                    Log.i(TAG,getString(R.string.load_record_failure));
                } else {
                    record = app.getRecord(recordIndex);
                }

                // Tell the User Location class not to get the user's location.
                userLocation.setRequestingLocation(false);

                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Connect to Play Services and track location.
        userLocation.connectToGoogleApi();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Stop tracking location and disconnect from Play Services.
        userLocation.stopLocationUpdates();
        userLocation.disconnectFromGoogleApi();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Stop tracking the user's location;
        userLocation.stopLocationUpdates();
    }

    //endregion

    //region Abstract Methods

    /**
     * Sets up the UI fields in the activity
     */
    abstract void setUpFields();

    /**
     * Fills the properties map to add to the Record object
     */
    abstract void setItemsOut();

    /**
     * Finishes the activity
     */
    abstract void moveToNotebook();

    /**
     * Changes the accuracy text field to show the current location accuracy
     */
    abstract void updateGPSField();

    /**
     * Checks that required data has been provided
     */
    abstract boolean checkRequiredData();

    //endregion

    //region Navigation

    /**
     * Navigates to a list picker activity, sending the request mode and any existing
     * selected items
     * @param mode type
     * @param previous existing
     */
    protected void goToListPicker(View v, String mode, List<String> previous) {

        if (v instanceof TextView && ((TextView) v).getError() != null) {
            ((TextView) v).setError(null);
        }

        if (v instanceof EditText && ((EditText) v).getError() != null) {
            ((EditText) v).setError(null);
        }

        Intent intent = new Intent(NewRecord.this, ListPickerActivity.class);
        intent.putExtra("MODE", mode);
        intent.putStringArrayListExtra("PREVIOUS", (ArrayList<String>) previous);
        startActivityForResult(intent, 100);
    }

    /**
     * Should be overridden to handle the results of a list picker activity
     * @param mode type
     * @param values selected
     */
    abstract protected void returnFromListPicker(String mode, List<String> values);

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (100) : {
                if (resultCode == Activity.RESULT_OK) {
                    String mode = data.getStringExtra("MODE");
                    List<String> result = data.getStringArrayListExtra("RESULT");
                    returnFromListPicker(mode, result);
                }
                break;
            }
        }
    }

    //endregion

    //region UI Methods

    /**
     * Creates the default name from the datatype and datetime
     * @param type datatype
     * @return name
     */
    protected String setDefaultName (String type) {
        // Format the datetime object as as string.
        String dateString = df.format(dateTime);

        // Return the formatted default name.
        return getString(R.string.default_name,type,dateString);
    }

    //endregion

    //region Data I/O

    /**
     * Creates and executes an asynchronous task to save the new record.
     */
    protected void saveRecord () {
        if (getCurrentFocus() != null) {
            getCurrentFocus().clearFocus();
        }

        if (checkRequiredData()) {
            SaveRecord saveRecord = new SaveRecord(this);
            saveRecord.execute();
        } else {
            Log.i(TAG,getString(R.string.record_missing_data));
        }
    }

    /**
     * An asynchronous task to save the new record.
     */
    private class SaveRecord extends AsyncTask<NewMeas, Void, Boolean> {
        final NewRecord context;

        SaveRecord(NewRecord context) {
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(NewMeas...Params) {

            // Create the properties map using the helper method.
            setItemsOut();

            // If the user's location was not found, set to default (null island).
            if (userLoc[0] == null) {
                Log.i(TAG,getString(R.string.no_user_location_found));
                userLoc[0] = 0.0;
                userLoc[1] = 0.0;
                userLoc[2] = 0.0;
            }

            // Set the activity's record object to the newly created record.
            context.record = new Record(this.context, type, userLoc, mPhoto, itemsOut);

            // Return whether the record's name equals the name selected by the user.
            return context.record.props.get("name").equals(itemsOut.get("name").toString());
        }

        @Override
        protected void onPostExecute(Boolean result) {

            // If success, add the record to the list, save the list, and finish the activity.
            if (result) {

                if (recordIndex == -1) {
                    // Add the new record to the application's list.
                    app.addRecord(record);
                } else {
                    // Replace the existing record with the edited one.
                    app.replaceRecord(recordIndex, record);
                }

                // Save the application's list to device storage and log.
                String saveResult = DataIO.saveRecords(this.context, app.getRecords());
                Log.i(TAG,saveResult);

                UpdateUserVars mUpdateUserVars = new UpdateUserVars();
                mUpdateUserVars.execute();
            } else {

                // Log an error.
                Log.i(TAG,getString(R.string.save_record_failure));
            }
        }

        @Override
        protected void onCancelled() {
            //TODO: warn user about losing data.
        }
    }

    private class UpdateUserVars extends AsyncTask<Void, Void, Boolean> {

        UpdateUserVars() {

        }

        @Override
        protected Boolean doInBackground(Void...params) {
            return DataIO.addUserVars(NewRecord.this, record);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                // Finish the activity
                moveToNotebook();
            }
        }

    }

    //endregion

    //region Helper Methods

    /**
     * Adds a single string to an ArrayList for use by the list picker
     * @param inView TextView
     * @return ArrayList
     */
    protected List<String> stringFromViewToArray(TextView inView) {

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

    /**
     * Creates a single string from an ArrayList
     * @param values ArrayList
     * @return String
     */
    protected String arrayToStringForView(List<String> values) {

        // Construct a string from the ArrayList to put in the text view.
        StringBuilder sb = new StringBuilder();

        String delimiter = "";
        for (String v : values) {
            sb.append(delimiter).append(v);
            delimiter = ", ";
        }
        String displayString = sb.toString();

        return displayString;
    }

    //endregion

}
