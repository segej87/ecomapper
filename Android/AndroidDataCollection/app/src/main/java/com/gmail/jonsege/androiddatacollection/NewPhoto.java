package com.gmail.jonsege.androiddatacollection;

import android.os.Bundle;

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
    void moveToAddNew() {
        super.finish();
        this.finish();
    }

    //endregion

    //region UI Methods

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

    /**
     * Sets up the UI fields in the activity
     */
    @Override
    @SuppressWarnings("unchecked") void setUpFields () {

    }

    //endregion
}
