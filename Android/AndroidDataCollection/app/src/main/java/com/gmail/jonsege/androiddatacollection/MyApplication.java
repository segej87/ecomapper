package com.gmail.jonsege.androiddatacollection;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
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
    private final List<Record> records = new ArrayList<>();

    //endregion

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    //region Getters and Setters

    /**
     * Returns the list of records being used by the application.
     * @return records
     */
    public synchronized List<Record> getRecords() {
        return records;
    }

    /**
     * Returns a record from an index in the list
     * @param i index
     * @return record
     */
    public synchronized Record getRecord(int i) {
        return records.get(i);
    }

    /**
     * Adds a single record to the records list
     * @param record
     *      the record to add to the list
     */
    public synchronized void addRecord(Record record) {
        this.records.add(record);

        Log.i(TAG,getString(R.string.adding_record));
    }

    /**
     * Appends records to the applications records list
     * @param records
     *      An array list of records to add to the list
     */
    public synchronized void addRecords(List<Record> records) {
        this.records.addAll(records);
    }

    /**
     * Replaces the application's record list with a provided list
     * @param records
     *      The records to replace the existing list
     * @return
     *      A report of success or failure
     */
    public synchronized String replaceRecords(List<Record> records) {
        deleteRecords();
        this.records.addAll(records);
        return getString(R.string.io_success);
    }

    /**
     * Replaces a specific record in the records list
     * @param index
     *      The index of the record to replace
     * @param record
     *      The new record to put at the specified index
     */
    public synchronized void replaceRecord(int index, Record record) {
        this.records.set(index, record);
    }

    public synchronized boolean deleteRecord(Record record) {
        try {
            this.records.remove(record);
            return true;
        } catch (Exception e) {
            Log.e(TAG,getString(R.string.general_error_prefix,e.getLocalizedMessage()));
        }

        return false;
    }

    public synchronized void deleteRecords() {
        for (Iterator<Record> r = this.records.iterator(); r.hasNext();) {
            Record record= r.next();
            if (record != null) {
                r.remove();
            }
        }
    }

    //endregion
}
