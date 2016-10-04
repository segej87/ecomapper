package com.gmail.jonsege.androiddatacollection;

import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Notebook extends AppCompatActivity {

    //region Class Variables

    List<Record> records = new ArrayList<Record>();
    ListView listView;

    //endregion

    //region Initialization

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notebook);

        // Set the logged in text
        TextView loggedInText = (TextView) findViewById(R.id.logged_in_text);
        loggedInText.setText(getText(R.string.logged_in_text_string) + UserVars.UName);

        Button mAddButton = (Button) findViewById(R.id.add_button);
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
}
