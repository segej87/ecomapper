package com.gmail.jonsege.androiddatacollection;

import android.app.Application;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jonse on 10/6/2016.
 */

public class MyApplication extends Application {
    private List<Record> records = new ArrayList<>();

    public List<Record> getRecords() {
        return records;
    }

    public Record getRecord(int i) {
        return records.get(i);
    }

    public void addRecord(Record record) {
        this.records.add(record);
        System.out.println("Adding record " + record.recordJsonEncode().toString());
    }

    public void addRecords(List<Record> records) {
        this.records = records;
    }
}
