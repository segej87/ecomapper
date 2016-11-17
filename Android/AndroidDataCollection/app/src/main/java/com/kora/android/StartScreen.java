package com.kora.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created for the Kora project by jonse on 10/26/2016.
 */
public class StartScreen extends AppCompatActivity {

    //region Class Variables

    /**
     * Tag for this activity.
     */
    private static final String TAG = "start";

    //endregion

    //region Initialization

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((KoraApplication) this.getApplicationContext()).setUpMemoryCache();

        // Check for saved uuid and go to notebook if there is one
        String savedLogin = DataIO.loadLogin(this);
        if (savedLogin.contains(getString(R.string.io_success))){
            String savedUUID = savedLogin.replace(getString(R.string.io_success) + ": ","");
            if (!savedUUID.equals("")) {
                Log.i(TAG,getString(R.string.saved_login_log, savedUUID));
                UserVars.UUID = savedUUID;

                UserVars.UserVarsSaveFileName = getString(R.string.user_vars_file_prefix) + savedUUID;
                String userVarsResult = DataIO.loadUserVars(this);

                Log.i(TAG,getString(R.string.load_user_vars_report,userVarsResult));

                int syncPref = Integer.valueOf(PreferenceManager
                        .getDefaultSharedPreferences(this)
                        .getString(SettingsActivity.SYNC_STARTUP_KEY, "1"));

                boolean shouldSyncLists = (syncPref == 1 &&
                        DataIO.isNetworkConnected(this)) ||
                        (syncPref == 0 &&
                        DataIO.isWiFiConnected(this));

                if (shouldSyncLists) {
                    UserListsTask ult = new UserListsTask(savedUUID);
                    ult.execute((Void) null);
                } else {
                    moveToNotebook();
                }
            } else {
                moveToLogin();
            }
        } else {
            moveToLogin();
        }
    }

    //endregion

    //region Navigation

    private void moveToNotebook() {
        Intent intent = new Intent(StartScreen.this, Notebook.class);
        startActivity(intent);
        this.finish();
    }

    private void moveToLogin() {
        Intent intent = new Intent(StartScreen.this, LoginActivity.class);
        startActivity(intent);
        this.finish();
    }

    //endregion

    //region User Variables

    /**
     * An asynchronous task to retrieve user-specific lists and variables from the server.
     */
    public class UserListsTask extends AsyncTask<Void, Void, String> {

        private final String uuid;

        UserListsTask(String uuidIn) {
            uuid = uuidIn;
        }

        @Override
        protected String doInBackground(Void... params) {
            return DataIO.retrieveLists(StartScreen.this, uuid);
        }

        @Override
        protected void onPostExecute(String result) {
            boolean meshListResult = DataIO.meshUserVars(StartScreen.this, result);

            if (meshListResult && !(result.startsWith(getString(R.string.server_error_string)))) {
                DataIO.saveUserVars(StartScreen.this);
            } else if (!meshListResult) {
                Log.e(TAG, getString(R.string.save_user_vars_failure));
            } else if (result.contains(getString(R.string.server_connection_error))) {
                Log.e(TAG, getString(R.string.internet_failure_title));
            }

            moveToNotebook();
        }
    }

    //endregion

}