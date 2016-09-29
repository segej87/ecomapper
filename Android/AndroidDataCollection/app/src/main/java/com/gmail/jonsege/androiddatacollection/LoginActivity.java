package com.gmail.jonsege.androiddatacollection;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.json.*;

/**
 * A login screen that offers login via username/password.
 */
public class LoginActivity extends AppCompatActivity {

    //region Class Variables

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    //endregion

    //region Initialization

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mUsernameView = (AutoCompleteTextView) findViewById(R.id.username);

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

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        } else if (!isEmailValid(username)) {
            mUsernameView.setError(getString(R.string.error_invalid_email));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(username, password);
            mAuthTask.execute((Void) null);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, String> {

        private final String mUsername;
        private final String mPassword;
        private final String loginURL = getString(R.string.php_server_root) + getString(R.string.php_get_login);

        UserLoginTask(String email, String password) {
            mUsername = email;
            mPassword = password;
        }

        @Override
        protected String doInBackground(Void... params) {
            String response = new String();
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
                return "Server Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            System.out.println("Login server response: " + result);

            if (!(result.contains("Error:"))) {
                String uid = result;
                String uName = mUsername.toString();
                UserVars.uuid = uid;
                UserVars.uName = uName;

                UserListsTask mListTask = new UserListsTask(UserVars.uuid);
                mListTask.execute((Void) null);
            } else {
                mAuthTask = null;
                showProgress(false);
                if (result.contains("Server Error: ")) {
                    showError("Connection problem");
                }
                if (result.contains("do not match")) {
                    mPasswordView.setError(getString(R.string.error_incorrect_password));
                    mPasswordView.requestFocus();
                } else if (result.contains("not found")) {
                    mPasswordView.setError(getString(R.string.error_username_not_found));
                    mPasswordView.requestFocus();
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
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserListsTask extends AsyncTask<Void, Void, String> {

        private final String uuid;
        private final String listsURL = getString(R.string.php_server_root) + getString(R.string.php_get_lists);

        UserListsTask(String uuidIn) {
            uuid = uuidIn;
        }

        @Override
        protected String doInBackground(Void... params) {
            String response = new String();
            Map<String,Object> pars = new LinkedHashMap<>();
            pars.put("GUID", uuid);

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

                URL url = new URL(listsURL);
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
                return "Server Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            mAuthTask = null;
            showProgress(false);

            System.out.println("List server response: " + result);

            if (!(result.startsWith("Error:"))) {
                String jsonString = result;

                try {
                    JSONObject jObject = new JSONObject(result);

                    JSONArray accessArray = jObject.getJSONArray("institutions");
                    for (int i = 0; i < accessArray.length(); i++) {
                        UserVars.AccessLevels.add(accessArray.getString(i));
                    }

                    Object[] addArray = new Object[2];
                    addArray[0] = "Server";
                    addArray[1] = 0;

                    JSONArray tagsArray = jObject.getJSONArray("tags");
                    for (int i = 0; i < tagsArray.length(); i++) {
                        UserVars.Tags.put(tagsArray.getString(i),addArray);
                    }

                    JSONArray specArray = jObject.getJSONArray("species");
                    for (int i = 0; i < specArray.length(); i++) {
                        UserVars.Species.put(specArray.getString(i),addArray);
                    }

                    JSONArray unitArray = jObject.getJSONArray("units");
                    for (int i = 0; i < unitArray.length(); i++) {
                        UserVars.Units.put(unitArray.getString(i),addArray);
                    }
                } catch (JSONException e) {
                    System.out.println("Error parsing JSON response: " + e.getMessage());
                }

                Intent intent = new Intent(LoginActivity.this, Notebook.class);
                startActivity(intent);
            } else {
                if (result.contains("Server Error: ")) {
                    showError("Connection problem");
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

    private void showError(String title) {
        String message = new String();

        if (title == "Connection problem") {
            message = getString(R.string.internet_failure);
        }
        AlertDialog.Builder intAlert = new AlertDialog.Builder(this);
        intAlert.setMessage(message);
        intAlert.setTitle(title);
        intAlert.setPositiveButton("OK",null);
        intAlert.setCancelable(false);
        intAlert.create().show();
    }

    //endregion

    //region Helper Methods

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.length() > 4;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
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
