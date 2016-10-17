package com.gmail.jonsege.androiddatacollection;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Notebook extends AppCompatActivity {

    //region Class Variables

    /**
     * Tag for this activity.
     */
    private static final String TAG = "notebook";

    /**
     * The activity's application context.
     */
    private MyApplication app;

    /**
     * The list view to display records.
     */
    private ListView listView;

    //endregion

    //region Initialization

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notebook);

        // Get the current application context.
        app = (MyApplication) this.getApplicationContext();

        //Set up the toolbar.
        Toolbar myToolbar = (Toolbar) findViewById(R.id.login_toolbar);
        getLayoutInflater().inflate(R.layout.action_bar_notebook, myToolbar);
        myToolbar.setTitle("");
        setSupportActionBar(myToolbar);

        // Set the logged in text.
        TextView loggedInText = (TextView) findViewById(R.id.action_bar_title);
        loggedInText.setText(getString(R.string.logged_in_text_string,UserVars.UName));

        // Set up the log out button.
        Button mLogoutButton = (Button) findViewById(R.id.action_bar_logout);
        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutButtonHandler();
            }
        });

        // Set up the add new button.
        Button mAddButton = (Button) findViewById(R.id.action_bar_add);
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addButtonHandler();
            }
        });

        listView = (ListView) findViewById(R.id.recordsList);

        // Load saved records for this user. If any exist, load them into the app context.
        LoadRecordsTask loadRecordsTask = new LoadRecordsTask(this);
        loadRecordsTask.execute((Void) null);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Set the list view's adapter to the custom adapter for displaying records.
        setListAdapter();
    }

    //endregion

    //region Data I/O

    /**
     * An asynchronous task to load saved records and present them in the list view.
     */
    public class LoadRecordsTask extends AsyncTask<Void, Void, List<Record>> {

        private final Context context;

        LoadRecordsTask(Context context) {
            this.context = context;
        }

        @Override
        protected List<Record> doInBackground(Void...params) {
            return DataIO.loadRecords(context);
        }

        @Override
        protected void onPostExecute(List<Record> records) {
            if (records.size() > 0) {
                app.addRecords(records);
            }

            // Set the list view's adapter to the custom adapter for displaying records.
            setListAdapter();
        }
    }

    //endregion

    //region Helper Methods

    /**
     * Sets the adapter for the list view using the application's records list.
     */
    private void setListAdapter() {
        NotebookArrayAdapter adapter = new NotebookArrayAdapter(this, app.getRecords());
        listView.setAdapter(adapter);
    }

    /**
     * Handles calls to the add new button.
     */
    private void addButtonHandler(){
        Intent intent = new Intent(Notebook.this, AddNew.class);
        startActivity(intent);
    }

    /**
     * A method to log out of the current user state and return to the login page.
     */
    private void logoutButtonHandler() {
        LogOutTask saveLogin = new LogOutTask(this);
        saveLogin.execute((Void) null);
    }

    /**
     * Saves the current user ID to a shared preferences file. Used for logging out.
     * @param uuid UserID
     * @return result
     */
    private String saveLogin(String uuid) {
        return DataIO.saveLogin(this, uuid);
    }

    /**
     * An asynchronous task to log out of the current user.
     */
    public class LogOutTask extends AsyncTask<Void, Void, String> {
        final Context context;

        LogOutTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(Void...params) {
            return saveLogin(getString(R.string.logout_flag));
        }

        @Override
        protected void onPostExecute(String result) {
            String oldUUID = result.replace(context.getString(R.string.io_success) + ": ","");
            Log.i(TAG,context.getString(R.string.saved_logout_log,oldUUID));

            ClearUserVars clearUserVars = new ClearUserVars(context);
            clearUserVars.execute((Void) null);
        }
    }

    /**
     * An asynchronous task to clear user variables.
     */
    public class ClearUserVars extends AsyncTask<Void, Void, String> {
        final Context context;

        ClearUserVars(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(Void...params) {
            return DataIO.resetUserVars(context);
        }

        @Override
        protected void onPostExecute(String result) {
            app.addRecords(new ArrayList<Record>());

            ClearRecords clearRecords = new ClearRecords(context);
            clearRecords.execute((Void) null);
        }
    }

    /**
     * An asynchronous task to clear the records list and finish the activity.
     */
    public class ClearRecords extends AsyncTask<Void, Void, String> {
        final Context context;

        ClearRecords(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(Void...params) {
            return app.addRecords(new ArrayList<Record>());
        }

        @Override
        protected void onPostExecute(String result) {
            finish();
        }
    }

    /**
    *A method to delete the saved records file. Should be deleted after testing.
     */
    private void deleteRecordsFile() {
        File deleteFile = new File(this.getFilesDir(), UserVars.RecordsSaveFileName);
        boolean deleted = deleteFile.delete();
        if (deleted) {
            Log.i(TAG, getString(R.string.file_deleted_success,deleteFile.getAbsolutePath()));
        } else {
            Log.i(TAG, getString(R.string.file_deleted_failure,deleteFile.getAbsolutePath()));
        }
    }

    //endregion
}
