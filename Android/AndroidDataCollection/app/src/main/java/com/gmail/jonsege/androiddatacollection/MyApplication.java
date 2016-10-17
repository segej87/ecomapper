package com.gmail.jonsege.androiddatacollection;

import android.app.Application;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jonse on 10/6/2016.
 */

public class MyApplication extends Application {

    //region Class Variables

    /**
     * Tag for this class
     */
    private final static String TAG = "application";

    /**
     * The records list for the application
     */
    private List<Record> records = new ArrayList<>();

    //endregion

    //region Getters and Setters

    /**
     * Returns the list of records being used by the application.
     * @return records
     */
    public List<Record> getRecords() {
        return records;
    }

    /**
     * Returns a record from an index in the list
     * @param i index
     * @return record
     */
    public Record getRecord(int i) {
        return records.get(i);
    }

    /**
     * Adds a single record to the records list
     * @param record record
     * @return report
     */
    public void addRecord(Record record) {
        this.records.add(record);

        Log.i(TAG,getString(R.string.adding_record));
    }

    /**
     * Replaces the application's record list with a provided list
     * @param records records
     * @return report
     */
    public String addRecords(List<Record> records) {
        this.records = records;
        return getString(R.string.io_success);
    }

    //endregion
}
