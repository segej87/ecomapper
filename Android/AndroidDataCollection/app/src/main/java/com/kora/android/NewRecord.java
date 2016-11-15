package com.kora.android;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created for the Kora project by jonse on 10/15/2016.
 */

public abstract class NewRecord extends AppCompatActivity
        implements LocationOverrideFragment.LocationOverrideListener, ConfirmActionDialogFragment.ConfirmActionListener {

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
     * A flag indicating whether the class is being created from a previous state
     */
    boolean isFromSavedState = false;

    /**
     * The current mode of record entry (new or old)
     */
    private static final String MODE = "mode";
    String mode;

    /**
     * Data to construct the record
     */
    String type;
    //TODO: Figure out how to specify time zone
    final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    /**
     * The date and time when the record was opened
     */
    private static final String DATE_TIME = "dateTime";
    Date dateTime;

    /**
     * A list of tags for the record
     */
    private static final String TAG_ARRAY = "tagArray";
    List<String> tagArray = UserVars.TagsDefaults;

    /**
     * A list of access levels for the record
     */
    private static final String ACCESS_ARRAY = "accessArray";
    List<String> accessArray = UserVars.AccessDefaults;

    /**
     * A string with the species name (for measurements only)
     */
    private static final String SPECIES_STRING = "species";
    String species = null;

    /**
     * A string with the units (for measurements only)
     */
    private static final String UNITS_STRING = "units";
    String units = null;

    /**
     * The user's most recently detected location (from UserLocation class)
     */
    private static final String LOCATION = "latestLoc";
    Location latestLoc;

    /**
     * The most recent location's accuracy (reported by Play Services)
     */
    private static final String ACCURACY = "gpsAcc";
    double gpsAcc = -1;

    /**
     * The current location stability (calculated by the UserLocation class)
     */
    private static final String STABILITY = "gpsStab";
    double gpsStab = -1;

    Double[] userLoc = new Double[3];
    final Map<String, Object> itemsOut = new HashMap<>();

    private static final String PHOTO = "mPhoto";
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
     * The index of the record in the app's record list (for old records only)
     */
    private static final String RECORD_INDEX = "recordIndex";
    private int recordIndex = -1;

    /**
     * A request code for list picker activities
     */
    final static int LIST_PICKER_REQUEST_CODE = 100;

    /**
     * A request code for cancelling the record activity
     */
    final static int CANCEL_REQUEST = 501;
    final static int PHOTO_CANCEL_REQUEST = 502;

    /**
     * A request code for checking location permissions
     */
    final static int LOCATION_PERMISSIONS_CHECK = 11051;

    //endregion

    //region Initialization

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (UserVars.UUID == null)
            moveToNotebook();

        if (savedInstanceState != null) {
            isFromSavedState = true;

            // Restore state members from saved instance
            mode = savedInstanceState.getString(MODE);
            recordIndex = savedInstanceState.getInt(RECORD_INDEX);
            tagArray = savedInstanceState.getStringArrayList(TAG_ARRAY);
            accessArray = savedInstanceState.getStringArrayList(ACCESS_ARRAY);
            species = savedInstanceState.getString(SPECIES_STRING);
            units = savedInstanceState.getString(UNITS_STRING);
            mPhoto = savedInstanceState.getString(PHOTO);
            gpsAcc = savedInstanceState.getDouble(ACCURACY);
            gpsStab = savedInstanceState.getDouble(STABILITY);

            String dateString = savedInstanceState.getString(DATE_TIME);
            if (dateString != null) {
                dateTime = df.parse(dateString, new ParsePosition(0));
            }
            latestLoc = savedInstanceState.getParcelable(LOCATION);
        } else {
            // Get the current record mode.
            Intent intent = getIntent();
            mode = intent.getStringExtra("MODE");
            recordIndex = intent.getIntExtra("INDEX",-1);
        }

        // Get the current application.
        app = (KoraApplication) this.getApplicationContext();

        // Create an object to handle location requests
        userLocation = new UserLocation(this);

        // If the mode is new, get the current datetime and start tracking location.
        switch(mode) {
            case "new":
                // Get the date and time this view was created.
                // TODO: allow user to update datetime
                if (dateTime == null)
                    dateTime = new Date();

                // Build a Google API Client to connect to Play Services.
                userLocation.buildGoogleApiClient();
                break;
            case "old":
                // If the index is -1, return an error. Otherwise, load the record.
                if (recordIndex == -1) {
                    Log.i(TAG,getString(R.string.load_record_failure));
                } else {
                    record = app.getRecord(recordIndex);
                }

                // Tell the User Location class not to get the user's location.
                userLocation.requestStopUpdatingLocation();

                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Connect to Play Services and track location.
        checkLocationPermissions();
    }

    @Override
    protected void onStop() {
        // Stop tracking location and disconnect from Play Services.
        userLocation.stopLocationUpdates();
        userLocation.disconnectFromGoogleApi();

        super.onStop();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current state
        savedInstanceState.putString(MODE, mode);
        savedInstanceState.putInt(RECORD_INDEX, recordIndex);
        savedInstanceState.putStringArrayList(TAG_ARRAY, (ArrayList) tagArray);
        savedInstanceState.putStringArrayList(ACCESS_ARRAY, (ArrayList) accessArray);
        savedInstanceState.putString(SPECIES_STRING, species);
        savedInstanceState.putString(UNITS_STRING, units);
        savedInstanceState.putString(PHOTO, mPhoto);
        savedInstanceState.putString(DATE_TIME, df.format(dateTime));
        savedInstanceState.putParcelable(LOCATION, latestLoc);
        savedInstanceState.putDouble(ACCURACY, gpsAcc);
        savedInstanceState.putDouble(STABILITY, gpsStab);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
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

    private void showNoticeDialog(boolean nullLoc, String numMin, String acc, String stab) {
        Bundle b = new Bundle();
        b.putBoolean("NULLLOC", nullLoc);
        b.putString("NUMMIN", numMin);
        b.putString("ACC", acc);
        b.putString("STAB", stab);

        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new LocationOverrideFragment();
        dialog.setArguments(b);
        dialog.show(getSupportFragmentManager(), "LocationOverrideFragment");
    }

    @Override
    public void onDialogPositiveClick() {
        // User touched the dialog's positive button
        this.userOverrideStale = true;
        saveRecord();
    }

    @Override
    public void onDialogNegativeClick() {
        // User touched the dialog's negative button
        this.userOverrideStale = false;
    }

    void showConfirmDialog(int requestCode, String message, String positiveString, String negativeString) {
        Bundle b = new Bundle();
        b.putInt("REQUEST_CODE", requestCode);
        b.putString("MESSAGE", message);
        b.putString("POSSTRING", positiveString);
        b.putString("NEGSTRING", negativeString);

        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new ConfirmActionDialogFragment();
        dialog.setArguments(b);
        dialog.show(getSupportFragmentManager(), "ConfirmActionDialogFragment");
    }

    @Override
    public void onConfirmPositiveClick(DialogFragment d) {
        int requestCode = d.getArguments().getInt("REQUEST_CODE");
        switch (requestCode) {
            case CANCEL_REQUEST:
                moveToNotebook();
                break;
            case PHOTO_CANCEL_REQUEST:
                DataIO.deleteFile(mPhoto);
                moveToNotebook();
                break;
        }
    }

    @Override
    public void onConfirmNegativeClick() {
        Log.i(TAG, getString(R.string.user_cancel));
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

            if (mode.equals("new")) {
                // If the user's location was not found, set to default (null island).
                if (latestLoc == null) {
                    Log.i(TAG, getString(R.string.no_user_location_found));
                    userLoc[0] = 0.0;
                    userLoc[1] = 0.0;
                    userLoc[2] = 0.0;
                } else {
                    userLoc[0] = latestLoc.getLongitude();
                    userLoc[1] = latestLoc.getLatitude();
                    userLoc[2] = latestLoc.getAltitude();
                }
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

                if (latestLoc == null) {
                    Toast.makeText(NewRecord.this,
                            getString(R.string.no_user_location_found),
                            Toast.LENGTH_SHORT).show();
                }

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
            showConfirmDialog(CANCEL_REQUEST,
                    getString(R.string.cancel_confirmation_message),
                    getString(R.string.cancel_positive_string),
                    getString(R.string.cancel_negative_string));
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

    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(NewRecord.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(NewRecord.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                ActivityCompat.requestPermissions(NewRecord.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSIONS_CHECK);

            } else {
                ActivityCompat.requestPermissions(NewRecord.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSIONS_CHECK);
            }
        } else {
            userLocation.connectToGoogleApi();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSIONS_CHECK: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    userLocation.connectToGoogleApi();
                break;
            }
        }
    }

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

    boolean checkLocationOK() {
        boolean staleLoc;

        if (latestLoc == null) {
            showNoticeDialog(true, null, null, null);
            return false;
        }

        Long elapsedTime = ((new Date()).getTime() - latestLoc.getTime())/60000;
        String numMin = String.valueOf((elapsedTime.intValue()));

        staleLoc = elapsedTime > UserVars.maxUpdateTime ||
                (gpsAcc == -1 || gpsAcc > UserVars.minGPSAccuracy) ||
                (gpsStab == -1 || gpsStab > UserVars.minGPSStability);

        if (staleLoc) {
            String minOutString;
            if (elapsedTime > UserVars.maxUpdateTime)
                minOutString = numMin;
            else
                minOutString = "none";

            String accOutString;
            if (gpsAcc == -1)
                accOutString = getString(R.string.gps_locking);
            else if (gpsAcc > UserVars.minGPSAccuracy)
                accOutString = getString(R.string.gps_w_unit,
                        String.format(Locale.getDefault(), "%.2f", gpsAcc));
            else
                accOutString = "none";

            String stabOutString;
            if (gpsStab == -1)
                stabOutString = getString(R.string.gps_locking);
            else if (gpsStab > UserVars.minGPSStability)
                stabOutString = getString(R.string.gps_w_unit,
                        String.format(Locale.getDefault(), "%.2f", gpsStab));
            else
                stabOutString = "none";

            showNoticeDialog(false,
                    minOutString,
                    accOutString,
                    stabOutString);
        }

        return !staleLoc;
    }

    //endregion

}