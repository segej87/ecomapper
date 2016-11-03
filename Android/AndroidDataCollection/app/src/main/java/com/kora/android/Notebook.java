package com.kora.android;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static android.view.View.VISIBLE;

public class Notebook extends AppCompatActivity
        implements ConfirmActionDialogFragment.ConfirmActionListener {

    //region Class Variables

    /**
     * Tag for this activity.
     */
    private static final String TAG = "notebook";

    /**
     * The activity's application context.
     */
    private KoraApplication app;

    /**
     * The context menu
     */
    private Menu optionsMenu;

    /**
     * The list view to display records.
     */
    private ListView listView;

    /**
     * The adapter for the list view.
     */
    private NotebookArrayAdapter adapter;

    /**
     * For using a context menu to delete a record.
     */
    private Record choppingBlock;

    /**
     * A progress spinner to indicate ongoing sync
     */
    private ProgressBar mRecordsProgress;

    /**
     * New record buttons
     */
    private ImageButton mMeasButton;
    private ImageButton mNoteButton;
    private ImageButton mPhotoButton;

    /**
     * Media monitor views
     */
    private LinearLayout mMediaMonitor;
    private TextView mMediaCounter;
    private ProgressBar mMediaProgress;
    private ImageView mMediaPlaceholder;

    /**
     * Request codes for action confirmation dialog
     */
    private static final int SYNC_REQUEST = 500;
    private static final int LOGOUT_REQUEST = 501;

    /**
     * Request codes for progress spinners
     */
    private static final int RECORDS_REQUEST = 1000;
    private static final int MEDIAS_REQUEST = 1001;

    //endregion

    //region Initialization

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notebook);

        // Get the current application context.
        app = (KoraApplication) this.getApplicationContext();

        if (UserVars.UUID == null) {
            boolean recovered = DataIO.recoverFromMemoryLoss(this);
            if (!recovered)
                moveToLogin();
        }

        // Set up the toolbar.
        setUpToolbar();

        // Set up the button bar.
        setUpButtons();

        // Set up views in the layout.
        setUpFields();

        // Load saved records for this user. If any exist, load them into the app context.
        LoadRecordsTask loadRecordsTask = new LoadRecordsTask(this);
        loadRecordsTask.execute((Void) null);

        setUpListViewListeners();

        // Register the list view for context menu.
        registerForContextMenu(listView);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Set the list view's adapter to the custom adapter for displaying records.
        setUpListViewAdapter();

        // Start uploading any old medias if on WiFi.
        uploadMedias(false);
    }

    //endregion

    //region Menu Methods

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu_notebook, menu);
        optionsMenu = menu;
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
            case R.id.logout:
                showConfirmDialog(LOGOUT_REQUEST,
                        getString(R.string.logout_confirmation_message),
                        getString(R.string.logout_positive_string),
                        getString(R.string.logout_negative_string));
                return true;
            case R.id.sync:
                if (DataIO.isNetworkConnected(Notebook.this)) {
                    showConfirmDialog(SYNC_REQUEST,
                            getString(R.string.sync_confirmation_message),
                            getString(R.string.sync_positive_string),
                            getString(R.string.sync_negative_string));
                } else
                    showError(R.string.internet_failure_title);
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

    //region UI Methods

    /**
     * Sets up the activity's toolbar
     */
    private void setUpToolbar() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.notebook_toolbar);
        getLayoutInflater().inflate(R.layout.action_bar_notebook, myToolbar);
        myToolbar.setTitle("");
        setSupportActionBar(myToolbar);

        // Set the logged in text.
        TextView loggedInText = (TextView) findViewById(R.id.action_bar_logged_in);
        loggedInText.setText(getString(R.string.logged_in_text_string,UserVars.UName));
    }

    private void setUpButtons() {
        mMeasButton = (ImageButton) findViewById(R.id.meas_button);
        mMeasButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToNew("meas");
            }
        });

        mNoteButton = (ImageButton) findViewById(R.id.note_button);
        mNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToNew("note");
            }
        });

        mPhotoButton = (ImageButton) findViewById(R.id.photo_button);
        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToNew("photo");
            }
        });
    }

    private void setUpFields() {

        // Set the list view from the layout.
        listView = (ListView) findViewById(R.id.recordsList);

        // Set the progress view from the layout.
        mRecordsProgress = (ProgressBar) findViewById(R.id.sync_progress);

        // Set up the media monitor layout.
        mMediaMonitor = (LinearLayout) findViewById(R.id.media_monitor);
        mMediaCounter = (TextView) findViewById(R.id.media_counter);
        mMediaProgress = (ProgressBar) findViewById(R.id.media_progress);
        mMediaPlaceholder = (ImageView) findViewById(R.id.media_placeholder);

        mMediaMonitor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadMedias(true);
            }
        });

        mediaMonitorManager();
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final int requestCode, final boolean show) {

        final ProgressBar activeProgress;
        switch (requestCode) {
            case RECORDS_REQUEST:
                activeProgress = mRecordsProgress;
                break;
            case MEDIAS_REQUEST:
                activeProgress = mMediaProgress;
                if (show)
                    mMediaPlaceholder.setVisibility(View.GONE);
                else
                    mMediaPlaceholder.setVisibility(VISIBLE);
                break;
            default:
                activeProgress = null;
                break;
        }

        if (activeProgress != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

                activeProgress.setVisibility(show ? VISIBLE : View.GONE);
                activeProgress.animate().setDuration(shortAnimTime).alpha(
                        show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        activeProgress.setVisibility(show ? VISIBLE : View.GONE);
                    }
                });
            } else {
                // The ViewPropertyAnimator APIs are not available, so simply show
                // and hide the relevant UI components.
                activeProgress.setVisibility(show ? VISIBLE : View.GONE);
            }
        }
    }

    /**
     * Presents an error to the user.
     * @param title description
     */
    private void showError(int title) {
        String showTitle;
        String message;

        switch (title) {
            case R.string.internet_failure_title:
                showTitle = getString(R.string.internet_failure_title);
                message = getString(R.string.internet_failure);
                break;
            default:
                showTitle = getString(R.string.general_error_report);
                message = getString(R.string.general_error_report);
        }

        AlertDialog.Builder intAlert = new AlertDialog.Builder(this);
        intAlert.setMessage(message);
        intAlert.setTitle(showTitle);
        intAlert.setPositiveButton("OK",null);
        intAlert.setCancelable(false);
        intAlert.create().show();
    }

    private void showConfirmDialog(int requestCode, String message, String positiveString, String negativeString) {
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
        // User touched the dialog's positive button
        int requestCode = d.getArguments().getInt("REQUEST_CODE");

        switch(requestCode) {
            case SYNC_REQUEST:
                attemptSync();
                break;
            case LOGOUT_REQUEST:
                logoutButtonHandler();
                break;
        }
    }

    @Override
    public void onConfirmNegativeClick() {
        // User touched the dialog's negative button
        Log.i(TAG, getString(R.string.user_cancel));
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
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        Log.i(TAG,getString(R.string.start_new_record,type));
        startActivity(intent);
    }

    /**
     * Sends an intent to edit a record
     * @param position
     *      The list position of the record to edit in the app's records list
     * @param dest
     *      The destination activity (depending on record type)
     */
    private void goToOld(int position, Class dest) {
        if (dest != null) {
            Intent intent = new Intent(Notebook.this, dest);
            intent.putExtra("MODE", "old");
            intent.putExtra("INDEX", position);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
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
     * Sends an intent to the login activity
     */
    private void moveToLogin() {
        Intent intent = new Intent(Notebook.this, LoginActivity.class);
        startActivity(intent);
        this.finish();
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

            ClearRecords clearRecords = new ClearRecords();
            clearRecords.execute((Void) null);
        }
    }

    /**
     * An asynchronous task to clear the records list and finish the activity.
     */
    private class ClearRecords extends AsyncTask<Void, Void, String> {

        ClearRecords() {

        }

        @Override
        protected String doInBackground(Void...params) {
            return app.replaceRecords(new ArrayList<Record>());
        }

        @Override
        protected void onPostExecute(String result) {
            moveToLogin();
        }
    }

    /**
     * An asynchronous task to sync user variable lists
     */
    private class SetListsTask extends AsyncTask<Void, Void, String>{

        SetListsTask() {

        }

        protected String doInBackground(Void...Params) {
            return DataIO.retrieveLists(Notebook.this, UserVars.UUID);
        }

        protected void onPostExecute(String result) {
            boolean success = false;
            if (!result.contains(getString(R.string.server_error_string))) {
                success = DataIO.setLists(Notebook.this, result);
            }

            boolean mediaMark = markMedias();
            DataIO.saveUserVars(Notebook.this);
            mediaMonitorManager();

            showProgress(RECORDS_REQUEST, false);
            toggleViewsEnabled(true);

            if (success && mediaMark) {
                app.deleteRecordsOnly();
                DataIO.saveRecords(Notebook.this, app.getRecords());
                adapter.notifyDataSetChanged();

                uploadMedias(false);
            } else {
                Log.i(TAG,result);
            }
        }
    }

    //endregion

    //region Server Ops

    /**
     * A helper method to kick off the asynchronous task
     */
    private void attemptSync() {
        // Kick off an asynchronous task to upload records
        Log.i(TAG, "Syncing records");
        showProgress(RECORDS_REQUEST, true);
        toggleViewsEnabled(false);
        AttemptSyncTask syncTask = new AttemptSyncTask();
        syncTask.execute((Void) null);
    }

    /**
     * An asynchronous task to upload records to the server
     */
    private class AttemptSyncTask extends AsyncTask<Void, Void, Boolean> {

        AttemptSyncTask() {

        }

        @Override
        protected Boolean doInBackground(Void...params) {
            return app.getRecords().size() == 0 || DataIO.uploadRecords(Notebook.this);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                SetListsTask slt = new SetListsTask();
                slt.execute((Void) null);
            }
        }
    }

    /**
     * A helper method to kick off the asynchronous task
     */
    private void uploadMedias(final boolean userTouch) {
        mMediaMonitor.setEnabled(false);
        if (UserVars.MarkedMedia.size() > 0 && DataIO.isWiFiConnected(Notebook.this)) {
            UploadMediasTask umt = new UploadMediasTask();
            Log.i(TAG, "Syncing media");
            showProgress(MEDIAS_REQUEST, true);
            umt.execute((Void) null);
        } else {
            if (userTouch)
                Toast.makeText(Notebook.this,
                        getString(R.string.no_upload_no_wifi),
                        Toast.LENGTH_SHORT).show();
            mMediaMonitor.setEnabled(true);
        }
    }

    /**
     * An asynchronous task to upload media (photos) to the server
     */
    private class UploadMediasTask extends AsyncTask<Void, Void, Boolean> {

        UploadMediasTask() {

        }

        @Override
        protected Boolean doInBackground(Void...Params) {
            for (Iterator<String> iter = UserVars.MarkedMedia.iterator(); iter.hasNext(); ) {
                String m = iter.next();
                String filePath = UserVars.Medias.get(m);
                boolean res = DataIO.isWiFiConnected(Notebook.this) && DataIO.uploadBlob(filePath);
                if (res) {
                    UserVars.Medias.remove(m);
                    String cacheKey = m.substring(m.lastIndexOf('/') + 1).
                            replaceAll(".jpg","").
                            replaceAll("_","");
                    app.removeBitmapFromMemCache(cacheKey);
                    iter.remove();
                } else {
                    this.cancel(true);
                }
                DataIO.saveUserVars(Notebook.this);
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                Log.i(TAG, getString(R.string.media_upload_complete));
            }

            showProgress(MEDIAS_REQUEST, false);
            mMediaMonitor.setEnabled(true);
            mediaMonitorManager();
        }

        @Override
        protected void onCancelled() {
            Log.i(TAG, getString(R.string.no_upload_no_wifi));

            showProgress(MEDIAS_REQUEST, false);
            mMediaMonitor.setEnabled(true);
            mediaMonitorManager();
        }
    }

    //endregion

    //region Helper Methods

    private void mediaMonitorManager() {
        int mmSize = UserVars.MarkedMedia.size();
        if (mmSize == 0)
            mMediaMonitor.setVisibility(View.GONE);
        else {
            if (mMediaMonitor.getVisibility() == View.GONE)
                mMediaMonitor.setVisibility(View.VISIBLE);

            mMediaCounter.setText(String.valueOf(mmSize));
        }
    }

    private void toggleViewsEnabled(final boolean enabled) {
        mMeasButton.setEnabled(enabled);
        mNoteButton.setEnabled(enabled);
        mPhotoButton.setEnabled(enabled);
        optionsMenu.setGroupEnabled(R.id.opMenuGroup, enabled);
        listView.setEnabled(enabled);
        mMediaMonitor.setEnabled(enabled);
    }

    /**
     * Saves the current user ID to a shared preferences file. Used for logging out.
     * @param uuid UserID
     * @return result
     */
    private String saveLogin(String uuid) {
        return DataIO.saveLogin(this, uuid);
    }

    private boolean markMedias() {
        List<Record> records = app.getRecords();
        int numPhotos = 0;

        try {
            if (records.size() > 0) {
                for (Record record : records) {
                    if (record != null) {
                        if (record.props.get("datatype").equals("photo")) {
                            numPhotos++;
                            String fp = record.props.get("filepath").toString();
                            if (!UserVars.MarkedMedia.contains(fp)) {
                                UserVars.MarkedMedia.add(fp);
                            }
                        }
                    }
                }

                if (UserVars.MarkedMedia != null && UserVars.MarkedMedia.size() == numPhotos)
                    return true;
            } else if (records.size() == 0) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    //endregion
}