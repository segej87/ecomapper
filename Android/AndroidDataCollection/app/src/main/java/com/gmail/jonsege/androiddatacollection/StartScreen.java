package com.gmail.jonsege.androiddatacollection;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by jonse on 10/26/2016.
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
                moveToNotebook();
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
}
