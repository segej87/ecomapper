package com.gmail.jonsege.androiddatacollection;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class Notebook extends AppCompatActivity {

    //region Class Variables

    private List<Record> records = new ArrayList<Record>();
    private ListView listView;

    //endregion

    //region Initialization

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notebook);

        //Set up the toolbar.
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setTitle("");

        // Inflate the custom toolbar layout and add it to the view.
        getLayoutInflater().inflate(R.layout.action_bar, myToolbar);
        setSupportActionBar(myToolbar);

        // Set the logged in text
        TextView loggedInText = (TextView) findViewById(R.id.action_bar_title);
        loggedInText.setText(String.format(getString(R.string.logged_in_text_string),UserVars.UName));

        Button mLogoutButton = (Button) findViewById(R.id.action_bar_logout);
        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Move to asynchronous thread
                String saveLoginResult = saveLogin("");
                System.out.println(getString(R.string.saved_logout_log) +
                        saveLoginResult.replace(getString(R.string.io_success) + ": ",""));
                DataIO.clearUserVars();
                finish();
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

        if (DataIO.loadRecords(this).size() > 0) {
            records = DataIO.loadRecords(this);
        }

        NotebookArrayAdapter adapter = new NotebookArrayAdapter(this,records);

        listView.setAdapter(adapter);
    }

    //endregion

    //region Helper Methods

    private String saveLogin(String uuid) {
        String saveLoginResult = DataIO.saveLogin(this, uuid);
        return saveLoginResult;
    }

    //endregion
}
