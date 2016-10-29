package com.gmail.jonsege.androiddatacollection;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
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
 * Created for the Kora project by jonse on 10/15/2016.
 */

public abstract class NewRecord extends AppCompatActivity
        implements LocationOverrideFragment.LocationOverrideListener {

    //region Class Variables

    /**
     * The application context
     */
    private KoraApplication app;

    /**
     * Tag for this activity. Will be replaced by child class.
     */
    String TAG = "new_record";

    /**
     * The current mode of record entry (new or old)
     */
    String mode;

    /**
     * Data to construct the record
     */
    String type;
    //TODO: Figure out how to specify time zone
    final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Date dateTime;
    List<String> tagArray = UserVars.TagsDefaults;
    List<String> accessArray = UserVars.AccessDefaults;
    Location latestLoc;
    Double[] userLoc = new Double[3];
    double gpsAcc = 0.0;
    final Map<String, Object> itemsOut = new HashMap<>();
    String mPhoto = null;

    /**
     * The location object
     */
    private UserLocation userLocation;

    /**
     * Record whether the user has overridden the stale location warning
     */
    boolean userOverrideStale = false;

    /**
     * The record created or loaded by the user
     */
    Record record;

    /**
     * The index of the record in the app's record list (for old records only
     */
    private int recordIndex = -1;

    /**
     * A request code for list picker activities
     */
    final int LIST_PICKER_REQUEST_CODE = 100;

    //endregion

    //region Initialization

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the current application.
        app = (KoraApplication) this.getApplicationContext();

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

    //endregion

    //region Abstract Methods

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
    void goToListPicker(View v, String mode, List<String> previous) {

        if (v instanceof TextView && ((TextView) v).getError() != null) {
            ((TextView) v).setError(null);
        }

        if (v instanceof EditText && ((EditText) v).getError() != null) {
            ((EditText) v).setError(null);
        }

        Intent intent = new Intent(NewRecord.this, ListPickerActivity.class);
        intent.putExtra("MODE", mode);
        intent.putStringArrayListExtra("PREVIOUS", (ArrayList<String>) previous);
        startActivityForResult(intent, LIST_PICKER_REQUEST_CODE);
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
            case (LIST_PICKER_REQUEST_CODE) : {
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
    String setDefaultName (String type) {
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
    void saveRecord () {
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
            if (latestLoc == null) {
                Log.i(TAG,getString(R.string.no_user_location_found));
                userLoc[0] = 0.0;
                userLoc[1] = 0.0;
                userLoc[2] = 0.0;
            } else {
                userLoc[0] = latestLoc.getLongitude();
                userLoc[1] = latestLoc.getLatitude();
                userLoc[2] = latestLoc.getAltitude();
            }

            // Set the activity's record object to the newly created record.
            context.record = new Record(type, userLoc, mPhoto, itemsOut);

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
    List<String> stringFromViewToArray(TextView inView) {

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
    @SuppressWarnings("replaceable")
    String arrayToStringForView(List<String> values) {

        // Construct a string from the ArrayList to put in the text view.
        StringBuilder sb = new StringBuilder();

        String delimiter = "";

        for (String appString : values) {
            if (!sb.toString().contains(appString)) {
                sb.append(delimiter).append(appString);
                delimiter = ", ";
            }
        }

        return sb.toString();
    }

    boolean checkLocationStale() {
        boolean staleLoc;

        //TODO: Warn user of location failure!
        if (latestLoc == null) {
            return false;
        }

        Long elapsedTime = ((new Date()).getTime() - latestLoc.getTime()) / 60000;
        String numMin = String.valueOf((elapsedTime.intValue()));

        if (elapsedTime > 1) {
            staleLoc = true;
            showNoticeDialog(numMin);
        } else {
            staleLoc = false;
        }

        return staleLoc;
    }

    private void showNoticeDialog(String numMin) {
        Bundle b = new Bundle();
        b.putString("NUMMIN", numMin);

        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new LocationOverrideFragment();
        dialog.setArguments(b);
        dialog.show(getSupportFragmentManager(), "LocationOverrideFragment");
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // User touched the dialog's positive button
        this.userOverrideStale = true;
        saveRecord();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button
        this.userOverrideStale = false;
    }

    //endregion

}
