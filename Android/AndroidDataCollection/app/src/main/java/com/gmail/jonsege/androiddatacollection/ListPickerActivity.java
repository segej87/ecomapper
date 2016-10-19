package com.gmail.jonsege.androiddatacollection;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

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
    private List<String> selectedList = new ArrayList<>();

    /**
     * The search and add new text fields.
     */
    private SearchView mSearchView;
    private EditText mAddNew;

    private Button mAddButton;

    /**
     * Keep track of search activity
     */
    private boolean searchActive = false;

    /**
     * Should the search view be cleared or refreshed on click?
     */
    private final static boolean CLEAR_SEARCH = false;

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

        // Set up the fields.
        setUpFields();

        // Get the full data list from User Variables depending on the mode.
        getInitialData(previous);

        // Set the adapters for the list views.
        setUpListViewAdapters();

        // Set listeners for list view taps.
        setUpListViewListeners();

        // Set up the search view.
        setUpSearchView();
    }

    //endregion

    //region ListView Methods

    private void setUpListViewAdapters() {
        mFullAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,fullList);
        mAvailableView.setAdapter(mFullAdapter);
        mSelectedAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,selectedList);
        mSelectedView.setAdapter(mSelectedAdapter);
    }

    private void setUpListViewListeners() {
        mAvailableView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mAddNew.hasFocus() || mSearchView.hasFocus()) clearTextFocus();

                String selected = (String) parent.getAdapter().getItem(position);

                fullList.remove(selected);

                selectedList.add(selected);

                // If there is an active search, clear the search field
                if (searchActive) {
                    if (CLEAR_SEARCH) {
                        clearSearchView();
                    } else {
                        resetSearchView();
                    }
                }

                // Notify the adapters of changes.
                mFullAdapter.notifyDataSetChanged();
                mSelectedAdapter.notifyDataSetChanged();
            }
        });

        mSelectedView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mAddNew.hasFocus() || mSearchView.hasFocus()) clearTextFocus();

                String selected = (String) parent.getAdapter().getItem(position);
                int posInSelectedArray = selectedList.indexOf(selected);

                selectedList.remove(posInSelectedArray);

                fullList.add(selected);

                if (searchActive) {
                    if (!CLEAR_SEARCH) {
                        resetSearchView();
                    }
                }

                // Notify the adapters of changes.
                mFullAdapter.notifyDataSetChanged();
                mSelectedAdapter.notifyDataSetChanged();
            }
        });
    }

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

    private void clearTextFocus() {
        if (mSearchView.hasFocus()) {
            mSearchView.clearFocus();
        }

        if (mAddNew.hasFocus()) {
            mAddNew.clearFocus();
        }
    }

    //endregion

    //region Search Methods

    /**
     * Sets up the search view functionality
     */
    private void setUpSearchView() {
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                if (!query.equals("")) {
                    ListPickerActivity.this.mFullAdapter.getFilter().filter(query);
                    searchActive = true;
                    return true;
                }

                setUpListViewAdapters();
                searchActive = false;
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if (!newText.equals("")) {
                    mFullAdapter.getFilter().filter(newText);
                    searchActive = true;
                    return true;
                }

                setUpListViewAdapters();
                searchActive = false;
                return false;
            }
        });
    }

    private void clearSearchView() {
        mSearchView.setQuery("", true);
        mSearchView.clearFocus();
        mSearchView.setIconified(true);
    }

    private void resetSearchView() {
        String oldQuery = mSearchView.getQuery().toString();
        mSearchView.setQuery("", true);
        mSearchView.setQuery(oldQuery,true);

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
    }

    /**
     * Sets up the UI elements.
     */
    private void setUpFields() {
        mAvailableView = (ListView) findViewById(R.id.availableList);
        mSelectedView = (ListView) findViewById(R.id.selectedList);

        mSearchView = (SearchView) findViewById(R.id.searchView);
        mAddNew = (EditText) findViewById(R.id.addNewText);

        String modeTypeString;
        switch(mode) {
            case "access":
                modeTypeString = getString(R.string.access_string);
                break;
            case "species":
                modeTypeString = getString(R.string.species_string);
                break;
            case "units":
                modeTypeString = getString(R.string.units_string);
                break;
            case "tags":
                modeTypeString = getString(R.string.tags_string);
                break;
            default:
                modeTypeString = getString(R.string.generic_string);
        }

        mSearchView.setQueryHint(getString(R.string.search_hint,modeTypeString));
        mAddNew.setHint(getString(R.string.add_new,modeTypeString));

        mAddButton = (Button) findViewById(R.id.addNewButton);

        mAddNew.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (!mSearchView.getQuery().equals("")) {
                        clearSearchView();
                    }
                    mAddButton.setVisibility(View.VISIBLE);
                } else {
                    mAddButton.setVisibility(View.INVISIBLE);
                }
            }
        });

        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newItem = mAddNew.getText().toString();

                if (newItem.equals("") || selectedList.contains(newItem)) {

                } else if (fullList.contains(newItem)) {
                    mAvailableView.performItemClick(v, fullList.indexOf(newItem), 0);
                } else if (!fullList.contains(newItem) && !selectedList.contains(newItem)) {
                    selectedList.add(newItem);
                }

                mAddNew.setText(null);
                clearTextFocus();
            }
        });
    }

    /**
     * Loads data into the full list and filtered list depending on the list picker mode.
     */
    private void getInitialData(List<String> previous) {

        // Initialize the list view data sources
        fullList = new ArrayList<>();
        selectedList = new ArrayList<>();

        if (mode != null) {

            switch(mode) {
                case "access":
                    fullList.addAll(UserVars.AccessLevels);
                    break;
                case "species":
                    Set<String> specSet = UserVars.Species.keySet();
                    if (specSet.size() > 0) {
                        for (String s : specSet) {
                            fullList.add(s);
                        }
                    }
                    break;
                case "units":
                    Set<String> unitSet = UserVars.Units.keySet();
                    for (String u : unitSet) {
                        fullList.add(u);
                    }
                    break;
                case "tags":
                    Set<String> tagSet = UserVars.Tags.keySet();
                    for (String t : tagSet) {
                        fullList.add(t);
                    }
                    break;
            }

            // If the record already had selected items, select them again.
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
