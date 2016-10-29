package com.kora.android;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A login screen that offers login via username/password.
 */
public class LoginActivity extends AppCompatActivity {

    //region Class Variables

    /**
     * Tag for this activity.
     */
    private static final String TAG = "login";

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    /**
     * UI references.
     */
    private EditText mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    //endregion

    //region Initialization

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.notebook_toolbar);
        myToolbar.setTitleTextColor(Color.LTGRAY);
        setSupportActionBar(myToolbar);

        // Set up the login form.
        mUsernameView = (EditText) findViewById(R.id.username);
        mUsernameView.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    //endregion

    //region Login Methods

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid username.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        } else if (!isUNameValid(username)) {
            mUsernameView.setError(getString(R.string.error_invalid_email));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            //TODO: remove after testing
            if (username.equals("letmepass") && password.equals("letsdothis")) {
                UserVars.UUID = "testing";
                UserVars.UName = "Testing";
                UserVars.UserVarsSaveFileName = getString(R.string.user_vars_file_prefix) + UserVars.UUID;
                UserVars.RecordsSaveFileName = getString(R.string.record_file_prefix) + UserVars.UUID;
                String loginResult = DataIO.saveLogin(this.getApplicationContext(), UserVars.UUID);
                if (loginResult.contains(getString(R.string.save_login_failure))) {
                    showError(getString(R.string.save_login_failure));
                } else {
                    String userVarsResult = DataIO.saveUserVars(this.getApplicationContext());
                    Log.i(TAG,"Save login: " + loginResult + ", Save user vars: " + userVarsResult);
                    moveToNotebook();
                }
            } else {
                // Show a progress spinner, and kick off a background task to
                // perform the user login attempt.
                showProgress(true);
                mAuthTask = new UserLoginTask(username, password);
                mAuthTask.execute((Void) null);
            }
        }
    }

    /**
     * An asynchronous login task to get the user id for the provided username and password
     */
    public class UserLoginTask extends AsyncTask<Void, Void, String> {

        private final String mUsername;
        private final String mPassword;
        private final String loginURL = getString(R.string.php_server_root) + getString(R.string.php_get_login);

        UserLoginTask(String username, String password) {
            mUsername = username;
            mPassword = password;
        }

        @Override
        protected String doInBackground(Void... params) {
            String response;
            Map<String,Object> pars = new LinkedHashMap<>();
            pars.put("username", mUsername);
            pars.put("password", mPassword);

            try {
                StringBuilder postData = new StringBuilder();
                for (Map.Entry<String,Object> par : pars.entrySet()) {
                    if (postData.length() != 0) postData.append('&');
                    postData.append(URLEncoder.encode(par.getKey(), "UTF-8"));
                    postData.append('=');
                    postData.append(URLEncoder.encode(String.valueOf(par.getValue()), "UTF-8"));
                }
                byte[] postDataBytes = postData.toString().getBytes("UTF-8");

                //TODO: Check for internet connection. If not return error.

                URL url = new URL(loginURL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty( "Content-Length", String.valueOf(postDataBytes.length));
                conn.setDoOutput(true);
                conn.getOutputStream().write(postDataBytes);

                Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

                StringBuilder sb = new StringBuilder();
                for (int c; (c = in.read()) >= 0;)
                    sb.append((char)c);
                response = sb.toString();

                conn.disconnect();

                return response;
            } catch (Exception e) {
                return getString(R.string.server_connection_error) + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Log.i(TAG,getString(R.string.login_server_response,result));

            if (!(result.contains(getString(R.string.server_error_string)))) {
                String uid = result.toLowerCase();
                UserVars.UUID = uid;
                UserVars.UName = mUsername;
                UserVars.UserVarsSaveFileName = getString(R.string.user_vars_file_prefix) + uid;
                UserVars.RecordsSaveFileName = getString(R.string.record_file_prefix) + uid;
                UserVars.MediasSaveFileName = getString(R.string.media_file_prefix) + uid;

                UserListsTask mListTask = new UserListsTask(UserVars.UUID);
                mListTask.execute((Void) null);
            } else {
                mAuthTask = null;
                showProgress(false);
                if (result.contains(getString(R.string.server_connection_error))) {
                    showError(getString(R.string.internet_failure_title));
                }
                if (result.contains(getString(R.string.server_password_error))) {
                    mPasswordView.setError(getString(R.string.error_incorrect_password));
                    mPasswordView.requestFocus();
                } else if (result.contains(getString(R.string.server_username_error))) {
                    mUsernameView.setError(getString(R.string.error_username_not_found));
                    mUsernameView.requestFocus();
                }

            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

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
            return DataIO.retrieveLists(LoginActivity.this, uuid);
        }

        @Override
        protected void onPostExecute(String result) {
            mAuthTask = null;
            showProgress(false);

            boolean meshListResult = DataIO.meshUserVars(LoginActivity.this, result);

            if (meshListResult && !(result.startsWith(getString(R.string.server_error_string)))) {
                //TODO: Move to background thread
                String loginResult = saveLogin(uuid);

                if (loginResult.contains(getString(R.string.io_success))) {
                    Log.i(TAG,getString(R.string.new_login_log,uuid));
                    DataIO.saveUserVars(LoginActivity.this);
                    moveToNotebook();
                } else {
                    showError(loginResult);
                }
            } else if (!meshListResult) {
                showError(getString(R.string.save_user_vars_failure));
            }else {
                if (result.contains(getString(R.string.server_connection_error))) {
                    showError(getString(R.string.internet_failure_title));
                }
                //TODO: Add possible returned errors.
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    //endregion

    //region Helper Methods

    /**
     * Checks whether a supplied username is in a valid format.
     * @param uName username
     * @return boolean
     */
    private boolean isUNameValid(String uName) {
        return uName.length() > 3;
    }

    /**
     * Checks whether a supplied password is in a valid format.
     * @param password password
     * @return boolean
     */
    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Presents an error to the user.
     * @param title description
     */
    private void showError(String title) {
        String showTitle = getString(R.string.general_error_report);
        String message = getString(R.string.general_error_report);

        if (title.equals(getString(R.string.internet_failure_title))) {
            showTitle = title;
            message = getString(R.string.internet_failure);
        } else if (title.contains(getString(R.string.save_login_failure))) {
            showTitle = getString(R.string.save_login_failure);
            message = title;
        } else if (title.contains(getString(R.string.load_login_failure))) {
            showTitle = getString(R.string.load_login_failure);
            message = title;
        } else if (title.contains(getString(R.string.save_user_vars_failure))) {
            showTitle = getString(R.string.general_error_title);
            message = getString(R.string.save_user_vars_failure);
        }

        AlertDialog.Builder intAlert = new AlertDialog.Builder(this);
        intAlert.setMessage(message);
        intAlert.setTitle(showTitle);
        intAlert.setPositiveButton("OK",null);
        intAlert.setCancelable(false);
        intAlert.create().show();
    }

    /**
     * Saves the new user ID to a shared preferences file.
     * @param uuid UserID
     * @return result
     */
    private String saveLogin(String uuid) {
        String result;

        result = DataIO.saveLogin(this, uuid);

        return result;
    }

    //endregion

    //region Navigation

    /**
     * Transitions view to the Notebook activity.
     */
    private void moveToNotebook() {
        // Clear this login activity's views.
        this.mUsernameView.getText().clear();
        this.mPasswordView.getText().clear();

        Intent intent = new Intent(LoginActivity.this, Notebook.class);
        startActivity(intent);
        this.finish();
    }

    //endregion

    //region UIMethods

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    //endregion
}

