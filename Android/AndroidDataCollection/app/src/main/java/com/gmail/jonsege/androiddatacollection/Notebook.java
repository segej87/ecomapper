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

        Button mAddButton = (Button) findViewById(R.id.add_button);
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Notebook.this, AddNew.class);
                startActivity(intent);
            }
        });

        listView = (ListView) findViewById(R.id.recordsList);

        Map<String, Object> testprops = new HashMap<>();
        testprops.put("name", "test1");
        Record record1 = new Record(null, null, testprops);
        records.add(record1);

        NotebookArrayAdapter adapter = new NotebookArrayAdapter(this,records);

        listView.setAdapter(adapter);
    }

    //endregion
}
