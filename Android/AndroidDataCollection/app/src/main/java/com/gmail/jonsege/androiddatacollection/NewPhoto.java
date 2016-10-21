package com.gmail.jonsege.androiddatacollection;

import android.os.Bundle;

import java.util.List;

public class NewPhoto extends NewRecord {

    //region Class Variables

    //endregion

    //region Initialization

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.TAG = "new_photo";
        super.type = "Point";
        setContentView(R.layout.activity_new_photo);
    }

    //endregion

    //region Navigation

    /**
     * Finishes the NewPhoto activity
     */
    @Override
    void moveToNotebook() {
        super.finish();
        this.finish();
    }

    @Override
    protected void returnFromListPicker(String mode, List<String> result) {

    }

    //endregion

    //region UI Methods

    /**
     * Sets up the UI fields in the activity
     */
    @Override
    @SuppressWarnings("unchecked") void setUpFields () {

    }

    /**
     * Changes the accuracy text field to show the current location accuracy
     */
    @Override
    void updateGPSField() {

    }

    //endregion

    //region Data I/O

    /**
     * Fills the properties map to add to the Record object
     */
    @Override
    void setItemsOut() {

    }

    //endregion

    //region Helper Methods

    @Override
    boolean checkRequiredData() {
        return false;
    }

    //endregion
}
