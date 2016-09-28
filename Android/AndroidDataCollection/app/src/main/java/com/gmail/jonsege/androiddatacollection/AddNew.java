package com.gmail.jonsege.androiddatacollection;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.Intent;

public class AddNew extends AppCompatActivity {

    //region Class Variables

    TextView mUname;

    //endregion

    //region Initialization

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        Intent intent = getIntent();
        mUname = (TextView) findViewById(R.id.userText);
        mUname.setText("Logged in as: " + UserVars.uName);

        // Link and wire up buttons.
        Button mNewMeasButton = (Button) findViewById(R.id.meas_button);
        mNewMeasButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToNew("meas");
            }
        });

        Button mNewPhotoButton = (Button) findViewById(R.id.photo_button);
        mNewPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToNew("photo");
            }
        });

        Button mNewNoteButton = (Button) findViewById(R.id.note_button);
        mNewNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToNew("note");
            }
        });
    }

    //endregion

    //region Navigation

    public void goToNew(String type) {
        Intent intent = new Intent();

        if (type == "meas") {
            intent = new Intent(AddNew.this, NewMeas.class);
        } else if (type == "photo") {
            intent = new Intent(AddNew.this, NewPhoto.class);
        } else if (type == "note") {
            intent = new Intent(AddNew.this, NewNote.class);
        }

        if (intent != null)
            startActivity(intent);
    }

    //endregion

}
