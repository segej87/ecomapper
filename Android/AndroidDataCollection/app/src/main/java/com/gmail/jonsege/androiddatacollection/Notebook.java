package com.gmail.jonsege.androiddatacollection;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Notebook extends AppCompatActivity {

    //region Class Variables

    private MyApplication app;
    private ListView listView;
    private NotebookArrayAdapter adapter;

    //endregion

    //region Initialization

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notebook);

        // Get the current application.
        app = (MyApplication) this.getApplicationContext();

        //Set up the toolbar.
        Toolbar myToolbar = (Toolbar) findViewById(R.id.login_toolbar);
        getLayoutInflater().inflate(R.layout.action_bar_notebook, myToolbar);
        myToolbar.setTitle("");
        setSupportActionBar(myToolbar);

        // Set the logged in text
        TextView loggedInText = (TextView) findViewById(R.id.action_bar_title);
        loggedInText.setText(String.format(getString(R.string.logged_in_text_string),UserVars.UName));

        Button mLogoutButton = (Button) findViewById(R.id.action_bar_logout);
        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        Button mAddButton = (Button) findViewById(R.id.action_bar_add);
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Notebook.this, AddNew.class);
                startActivity(intent);
            }
        });

        listView = (ListView) findViewById(R.id.recordsList);

        List<Record> records = DataIO.loadRecords(this);
        if (records.size() > 0) {
            app.addRecords(records);
        }

        adapter = new NotebookArrayAdapter(this, app.getRecords());

        listView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        //TODO: figure out how to refresh adapter without reinitializing
        super.onResume();
        adapter = new NotebookArrayAdapter(this, app.getRecords());
        listView.setAdapter(adapter);
//        listView.refreshDrawableState();
    }

    //endregion

    //region Helper Methods

    private String saveLogin(String uuid) {
        return DataIO.saveLogin(this, uuid);
    }

    /**
     * A method to log out of the current user state and return to the login page.
     */
    private void logout() {
        //TODO: Move to asynchronous thread
        String saveLoginResult = saveLogin(getString(R.string.logout_flag));
        System.out.println(getString(R.string.saved_logout_log) +
                saveLoginResult.replace(getString(R.string.io_success) + ": ",""));

        DataIO.clearUserVars();
        app.addRecords(new ArrayList<Record>());
        finish();
    }

    /**
    *A method to delete the saved records file. Should be deleted after testing.
     */
    private void deleteRecordsFile() {
        File deleteFile = new File(this.getFilesDir(), UserVars.RecordsSaveFileName);
        boolean deleted = deleteFile.delete();
        System.out.println("File deleted: " + deleted + ": " + deleteFile.getAbsolutePath());
    }

    //endregion
}
