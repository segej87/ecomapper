package com.gmail.jonsege.androiddatacollection;

import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

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

/**
 * Created by jonse on 10/15/2016.
 */

class UserLocation implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    //region Class Variables

    /**
     * Calling context
     */
    private final NewRecord context;

    /**
     * Google API Client for connecting to Play Services
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * Location request objects
     */
    private LocationRequest mLocationRequest;
    private static final int LOCATION_REQUEST_INTERVAL = 10000;
    private static final int LOCATION_REQUEST_FASTEST_INTERVAL = 5000;
    private static final int LOCATION_REQUEST_PRIORITY = LocationRequest.PRIORITY_HIGH_ACCURACY;
    private boolean mRequestingLocationUpdates = true;

    /**
     * Location objects
     */
    private Location mLastLocation;

    //endregion

    //region Initialization

    UserLocation(NewRecord context) {
        this.context = context;
    }

    //endregion

    //region Google API Methods

    @Override
    public void onConnected(Bundle bundle) {

        // Get the user's last known location.
        getLastLocation();

        // Start tracking the user's location.
        initializeLocationUpdates();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        // Log the Play Services connection error.
        Log.i(context.TAG,context.getString(R.string.play_services_connection_failure,
                connectionResult.getErrorMessage()));
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Helper method to connect to the Google API client
     */
    void connectToGoogleApi() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    /**
     * Helper method to disconnect from the Google API client
     */
    void disconnectFromGoogleApi() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    //endregion

    //region Location Methods

    /**
     * Helper method to build the Google API client
     */
    synchronized void buildGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    /**
     * Checks the user's location settings and requests an update if necessary
     */
    private void checkLocationSettings() {

        // Create a location request.
        createLocationRequest();

        // Try to build the location request.
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());

        // A callback for the location build request
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
                            // Log that a settings change is being requested.
                            Log.i(context.TAG,context.getString(R.string.requesting_settings_change, "Location"));
                            status.startResolutionForResult(
                                    context,
                                    0);

                            if (status.getStatusCode() == LocationSettingsStatusCodes.SUCCESS){
                                mRequestingLocationUpdates = true;
                            }
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                            mRequestingLocationUpdates =false;
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        Log.i(context.TAG,context.getString(R.string.settings_change_error, "Location"));
                        mRequestingLocationUpdates =false;
                        break;
                }
            }
        });
    }

    /**
     * Gets the user's last known location from Play Services
     */
    private void getLastLocation() {
        try {

            // Request the last known location from Play Services.
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
        } catch (SecurityException e) {

            // Log the error preventing last location reading.
            Log.i(context.TAG,context.getString(R.string.last_location_retrieve_failure, e.getLocalizedMessage()));

            // Check the user's location settings.
            checkLocationSettings();
        }

        // If the last location is not null, update the calling context's location array.
        if (mLastLocation != null) {
            context.userLoc[0] = mLastLocation.getLongitude();
            context.userLoc[1] = mLastLocation.getLatitude();
            context.userLoc[2] = mLastLocation.getAltitude();
        } else {

            // Log an error if the last location is null.
            Log.i(context.TAG,context.getString(R.string.last_location_retrieve_failure,
                    context.getString(R.string.last_location_null)));
        }
    }

    /**
     * Constructs the location request from class variables
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(LOCATION_REQUEST_INTERVAL);
        mLocationRequest.setFastestInterval(LOCATION_REQUEST_FASTEST_INTERVAL);
        mLocationRequest.setPriority(LOCATION_REQUEST_PRIORITY);
    }

    /**
     * Checks the location settings, then starts location updates if permissions are adequate
     */
    private void initializeLocationUpdates() {
        checkLocationSettings();

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        } else {
            Log.i(context.TAG, context.getString(R.string.start_location_updates_report, false));
        }
    }

    /**
     * Helper method to start tracking the user's location
     */
    private void startLocationUpdates() {
        try {

            // Log that location updates are starting, then request location updates from API
            Log.i(context.TAG,context.getString(R.string.location_updates_start));
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        } catch (SecurityException e) {

            // If there is a security exception, log and check permissions
            Log.i(context.TAG,context.getString(R.string.location_updates_start_error, e.getLocalizedMessage()));
            checkLocationSettings();
        }
    }

    /**
     * Helper method to stop tracking the user's location
     */
    void stopLocationUpdates() {
        if (mRequestingLocationUpdates) {

            // Log that location updates are stopping, then stop tracking the user's location.
            Log.i(context.TAG,context.getString(R.string.location_updates_stop));
            mRequestingLocationUpdates = false;
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        //TODO: warn user if location changes by more than accuracy

        // Set the calling context's location array using the new location
        context.userLoc[0] = location.getLongitude();
        context.userLoc[1] = location.getLatitude();
        context.userLoc[2] = location.getAltitude();

        // Set the calling context's accuracy variable using the new location
        context.gpsAcc = location.getAccuracy();

        // Update the calling context's text field showing the current accuracy
        context.updateGPSField();
    }

    //endregion

}
