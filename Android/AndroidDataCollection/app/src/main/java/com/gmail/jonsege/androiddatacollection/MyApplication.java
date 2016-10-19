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
     * @param record
     *      the record to add to the list
     */
    public void addRecord(Record record) {
        this.records.add(record);

        Log.i(TAG,getString(R.string.adding_record));
    }

    /**
     * Appends records to the applications records list
     * @param records
     *      An array list of records to add to the list
     */
    public void addRecords(List<Record> records) {
        this.records.addAll(records);
    }

    /**
     * Replaces the application's record list with a provided list
     * @param records
     *      The records to replace the existing list
     * @return
     *      A report of success or failure
     */
    public String replaceRecords(List<Record> records) {
        this.records = records;
        return getString(R.string.io_success);
    }

    /**
     * Replaces a specific record in the records list
     * @param index
     *      The index of the record to replace
     * @param record
     *      The new record to put at the specified index
     */
    public void replaceRecord(int index, Record record) {
        this.records.set(index, record);
    }

    //endregion
}
