package com.gmail.jonsege.androiddatacollection;

import android.content.Context;
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

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by jonse on 10/15/2016.
 */

class UserLocation implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    //region Class Variables

    // Calling context
    private NewRecord context;

    // Google Api Client
    private GoogleApiClient mGoogleApiClient;

    // Location Request Objects
    private LocationRequest mLocationRequest;
    private static final int LOCATION_REQUEST_INTERVAL = 10000;
    private static final int LOCATION_REQUEST_FASTEST_INTERVAL = 5000;
    private static final int LOCATION_REQUEST_PRIORITY = LocationRequest.PRIORITY_HIGH_ACCURACY;
    private boolean mRequestingLocationUpdates = true;
    private String mLastUpdateTime;

    // Location Objects
    private Location mLastLocation;
    private Location mCurrentLocation;

    //endregion

    //region Initialization

    UserLocation(NewRecord context) {
        this.context = context;
    }

    //endregion

    //region Google API Methods

    @Override
    public void onConnected(Bundle bundle) {
        getLastLocation();
        initializeLocationUpdates();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(context.TAG,context.getString(R.string.play_services_connection_failure) + connectionResult.getErrorMessage());
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.disconnect();
    }

    public void connectToGoogleApi() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    public void disconnectFromGoogleApi() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    //endregion

    //region Location Methods

    synchronized void buildGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(context)
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
            Log.i(context.TAG,context.getString(R.string.last_location_retrieve_failure, e.getLocalizedMessage()));
            checkLocationSettings();
        }
        if (mLastLocation != null) {
            context.userLoc[0] = mLastLocation.getLongitude();
            context.userLoc[1] = mLastLocation.getLatitude();
            context.userLoc[2] = mLastLocation.getAltitude();
        } else {
            Log.i(context.TAG,context.getString(R.string.last_location_retrieve_failure));
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
                            Log.i(context.TAG,context.getString(R.string.requesting_settings_change, "Location"));
                            status.startResolutionForResult(
                                    context,
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
                        Log.i(context.TAG,context.getString(R.string.settings_change_error, "Location"));
                        mRequestingLocationUpdates =false;
                        break;
                }
            }
        });
    }

    private void initializeLocationUpdates() {
        checkLocationSettings();

        Log.i(context.TAG,context.getString(R.string.start_location_updates_report, mRequestingLocationUpdates));

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
            Log.i(context.TAG,context.getString(R.string.location_updates_start));
        } catch (SecurityException e) {
            Log.i(context.TAG,context.getString(R.string.location_updates_start_error, e.getLocalizedMessage()));
            checkLocationSettings();
        }
    }

    void stopLocationUpdates() {
        if (mRequestingLocationUpdates) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        context.userLoc[0] = mCurrentLocation.getLongitude();
        context.userLoc[1] = mCurrentLocation.getLatitude();
        context.userLoc[2] = mCurrentLocation.getAltitude();
        context.gpsAcc = mCurrentLocation.getAccuracy();
        context.updateGPSField();
    }

    //endregion

}
