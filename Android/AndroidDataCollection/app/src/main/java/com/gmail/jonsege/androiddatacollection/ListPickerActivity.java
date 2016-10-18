package com.gmail.jonsege.androiddatacollection;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ListPickerActivity extends AppCompatActivity {

    //region Class Variables

    /**
     * Tag for this activity.
     */
    private final static String TAG = "list_picker";

    /**
     * The list picker mode.
      */
    private String mode;

    /**
     * The list views.
     */
    private ListView mSelectedView;
    private ListView mAvailableView;

    /**
     * The adapters for the list views.
     */
    ArrayAdapter<String> mFullAdapter;
    ArrayAdapter<String> mSelectedAdapter;

    /**
     * The data sources for the list views.
     */
    private List<String> fullList;
    private List<String> filteredList;
    private List<String> selectedList = new ArrayList<>();

    //endregion

    //region Initialization

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_picker);

        // Set up the toolbar.
        setUpToolbar();

        // Get the list picker mode.
        mode = getIntent().getStringExtra("MODE");

        // Get any existing selected items.
        List<String> previous = getIntent().getStringArrayListExtra("PREVIOUS");

        // Set up the save and cancel buttons.
        Button mSaveButton = (Button) findViewById(R.id.action_bar_save);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToNewRecord();
            }
        });

        Button mCancelButton = (Button) findViewById(R.id.action_bar_cancel);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListPickerActivity.this.finish();
            }
        });

        // Set up the fields.
        setUpFields();

        // Get the full data list from User Variables depending on the mode.
        getInitialData(previous);

        // Set the adapters for the list views.
        mFullAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,fullList);
        mAvailableView.setAdapter(mFullAdapter);
        mSelectedAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,selectedList);
        mSelectedView.setAdapter(mSelectedAdapter);

        // Set listeners for list view taps.
        mAvailableView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selected = (String) parent.getAdapter().getItem(position);
                int posInFullArray = fullList.indexOf(selected);

                fullList.remove(posInFullArray);

                selectedList.add(selected);

                // Notify the adapters of changes.
                mFullAdapter.notifyDataSetChanged();
                mSelectedAdapter.notifyDataSetChanged();

            }
        });

        mSelectedView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selected = (String) parent.getAdapter().getItem(position);
                int posInSelectedArray = selectedList.indexOf(selected);

                selectedList.remove(posInSelectedArray);

                fullList.add(selected);

                // Notify the adapters of changes.
                mFullAdapter.notifyDataSetChanged();
                mSelectedAdapter.notifyDataSetChanged();
            }
        });
    }

    //endregion

    //region List View Methods



    //endregion

    //region Navigation

    private void moveToNewRecord() {
        Intent resultIntent = new Intent();

        resultIntent.putExtra("MODE", mode);
        resultIntent.putStringArrayListExtra("RESULT", (ArrayList<String>) selectedList);

        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    //endregion

    //region UI Methods

    /**
     * Sets up the UI elements.
     */
    private void setUpFields() {
        mAvailableView = (ListView) findViewById(R.id.availableList);
        mSelectedView = (ListView) findViewById(R.id.selectedList);
    }

    //endregion

    //region Helper Methods

    /**
     * Set up the toolbar
     */
    private void setUpToolbar() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.new_toolbar);
        getLayoutInflater().inflate(R.layout.action_bar_list_picker, myToolbar);
        myToolbar.setTitle("");
        setSupportActionBar(myToolbar);
    }

    /**
     * Loads data into the full list and filtered list depending on the list picker mode.
     */
    private void getInitialData(List<String> previous) {
        if (mode != null) {

            // Initialize a list to hold the results from User Vars.
            List<String> dataList = new ArrayList<>();

            switch(mode) {
                case "access":
                    dataList = UserVars.AccessLevels;
                    break;
                case "species":
                    Set<String> specSet = UserVars.Species.keySet();
                    for (String s : specSet) {
                        dataList.add(s);
                    }
                    break;
                case "units":
                    Set<String> unitSet = UserVars.Units.keySet();
                    for (String u : unitSet) {
                        dataList.add(u);
                    }
                    break;
                case "tags":
                    Set<String> tagSet = UserVars.Tags.keySet();
                    for (String t : tagSet) {
                        dataList.add(t);
                    }
                    break;
            }

            // Set the full list equal to the appropriate data list.
            fullList = dataList;

            // Set the filtered list equal to the full list initially.
            filteredList = fullList;

            // Deal with previous items.
            if (previous != null) {
                for (String p : previous) {
                    selectedList.add(p);
                    if (fullList.contains(p)) {
                        fullList.remove(p);
                    } else {
                        Log.e(TAG, getString(R.string.missing_entry_in_list,mode,p));
                    }
                }
            }

        } else {
            Log.e(TAG, getString(R.string.no_mode_error));
        }
    }

    //endregion
}
