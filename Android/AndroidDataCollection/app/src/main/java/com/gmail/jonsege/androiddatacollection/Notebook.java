package com.gmail.jonsege.androiddatacollection;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    /**
     * The adapter for the list view.
     */
    NotebookArrayAdapter adapter;

    /**
     * For using a context menu to delete a record.
     */
    Record choppingBlock;

    //endregion

    //region Initialization

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notebook);

        // Get the current application context.
        app = (MyApplication) this.getApplicationContext();

        //Set up the toolbar.
        setUpToolbar();

        // Set the list view from the layout.
        listView = (ListView) findViewById(R.id.recordsList);

        // Load saved records for this user. If any exist, load them into the app context.
        LoadRecordsTask loadRecordsTask = new LoadRecordsTask(this);
        loadRecordsTask.execute((Void) null);

        setUpListViewListeners();

        // Register the list view for context menu.
        registerForContextMenu(listView);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Set the list view's adapter to the custom adapter for displaying records.
        setUpListViewAdapter();
    }

    //endregion

    //region Menu Methods

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_notebook, menu);
        return true;
    }

    /**
     * Handles calls from the options menu
     * @param item option
     * @return result
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.new_meas:
                goToNew("meas");
                return true;
            case R.id.new_note:
                goToNew("note");
                return true;
            case R.id.new_photo:
                goToNew("photo");
                return true;
            case R.id.logout:
                logoutButtonHandler();
                return true;
            case R.id.sync:
                AttemptSync syncTask = new AttemptSync();
                syncTask.execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.recordsList) {
            ListView lv = (ListView) v;
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
            choppingBlock = (Record) lv.getItemAtPosition(acmi.position);

            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.context_menu_notebook,menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if (choppingBlock != null) {
            boolean deleted = DataIO.deleteRecord(this, choppingBlock);
            if (deleted) {
                adapter.notifyDataSetChanged();
                choppingBlock = null;
            }
        }

        return false;
    }

    //endregion

    //region ListView Methods

    /**
     * Sets the adapter for the list view using the application's records list.
     */
    private void setUpListViewAdapter() {
        adapter = new NotebookArrayAdapter(this, app.getRecords());
        listView.setAdapter(adapter);
    }

    private void setUpListViewListeners() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Class outClass;

                Record selected = (Record) parent.getAdapter().getItem(position);

                Map<String, Object> props = selected.props;

                String dt = (String) props.get("datatype");

                Log.i(TAG,getString(R.string.start_old_record,dt));

                switch (dt) {
                    case "meas":
                        outClass = NewMeas.class;
                        break;
                    case "note":
                        outClass = NewNote.class;
                        break;
                    case "photo":
                        outClass = NewPhoto.class;
                        break;
                    default:
                        outClass = null;
                        break;
                }

                goToOld(position, outClass);
            }
        });
    }

    //endregion

    //region Navigation

    /**
     * Starts an activity to add a new record
     * @param type datatype
     */
    private void goToNew(String type) {
        Intent intent = new Intent();

        switch(type) {
            case "meas":
                intent = new Intent(Notebook.this, NewMeas.class);
                break;
            case "photo":
                intent = new Intent(Notebook.this, NewPhoto.class);
                break;
            case "note":
                intent = new Intent(Notebook.this, NewNote.class);
                break;
        }

        intent.putExtra("MODE", "new");

        Log.i(TAG,getString(R.string.start_new_record,type));
        startActivity(intent);
    }

    private void goToOld(int position, Class dest) {
        if (dest != null) {
            Intent intent = new Intent(Notebook.this, dest);
            intent.putExtra("MODE", "old");
            intent.putExtra("INDEX", position);
            startActivity(intent);
        }
    }

    /**
     * A method to log out of the current user state and return to the login page.
     */
    private void logoutButtonHandler() {
        LogOutTask saveLogin = new LogOutTask(this);
        saveLogin.execute((Void) null);
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
                app.replaceRecords(records);
            }

            // Set the list view's adapter to the custom adapter for displaying records.
            setUpListViewAdapter();
        }
    }

    //endregion

    //region Helper Methods

    /**
     * Sets up the activity's toolbar
     */
    private void setUpToolbar() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.login_toolbar);
        getLayoutInflater().inflate(R.layout.action_bar_notebook, myToolbar);
        myToolbar.setTitle("");
        setSupportActionBar(myToolbar);

        // Set the logged in text.
        TextView loggedInText = (TextView) findViewById(R.id.action_bar_title);
        loggedInText.setText(getString(R.string.logged_in_text_string,UserVars.UName));
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
            app.replaceRecords(new ArrayList<Record>());

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
            return app.replaceRecords(new ArrayList<Record>());
        }

        @Override
        protected void onPostExecute(String result) {
            finish();
        }
    }

    public class AttemptSync extends AsyncTask<Void, Void, Boolean> {
        AttemptSync() {

        }

        @Override
        protected Boolean doInBackground(Void...params) {
            return DataIO.attemptSync(Notebook.this);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                Log.i(TAG, "Records successfully synced");
            }
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

        File deleteFile2 = new File(this.getFilesDir(), UserVars.UserVarsSaveFileName);
        boolean deleted2 = deleteFile2.delete();
        if (deleted2) {
            Log.i(TAG, getString(R.string.file_deleted_success,deleteFile2.getAbsolutePath()));
        } else {
            Log.i(TAG, getString(R.string.file_deleted_failure,deleteFile2.getAbsolutePath()));
        }
    }

    //endregion
}
