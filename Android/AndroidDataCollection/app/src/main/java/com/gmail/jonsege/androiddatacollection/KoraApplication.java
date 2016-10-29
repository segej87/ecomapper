package com.gmail.jonsege.androiddatacollection;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.multidex.MultiDex;
import android.support.v4.util.LruCache;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created for the Kora project by jonse on 10/6/2016.
 */

public class KoraApplication extends Application {

    //region Class Variables

    /**
     * Tag for this class
     */
    private final static String TAG = "application";

    /**
     * The records list for the application
     */
    private final List<Record> records = new ArrayList<>();

    /**
     * A LruCache for images in the list view
     */
    private LruCache<String, Bitmap> mMemoryCache;

    //endregion

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    //region Record Getters and Setters

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
        deleteRecordsOnly();
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
        if (!this.records.get(index).photoPath.equals(record.photoPath)) {
            DataIO.deleteFile(this.records.get(index).photoPath);
        }
        this.records.set(index, record);
    }

    public synchronized boolean deleteRecordAndMedia(Record record) {
        try {
            this.records.remove(record);
            if (record.photoPath != null) {
                DataIO.deleteFile(record.photoPath);
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG,getString(R.string.general_error_prefix,e.getLocalizedMessage()));
        }

        return false;
    }

    /**
     * If all records are deleted at once, and the media should be deleted
     */
    public synchronized void deleteRecordsAndMedia() {
        for (Iterator<Record> r = this.records.iterator(); r.hasNext();) {
            Record record= r.next();
            if (record != null) {
                r.remove();
                if (record.photoPath != null) {
                    DataIO.deleteFile(record.photoPath);
                }
            }
        }
    }

    /**
     * If only the records should be deleted, but not the media
     */
    public synchronized void deleteRecordsOnly() {
        for (Iterator<Record> r = this.records.iterator(); r.hasNext();) {
            Record record= r.next();
            if (record != null) {
                r.remove();
            }
        }
    }

    //endregion

    //region Memory Cache

    public void setUpMemoryCache() {
        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public synchronized void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (mMemoryCache == null) {
            setUpMemoryCache();
        }

        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public synchronized Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    //endregion
}
