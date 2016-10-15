package com.gmail.jonsege.androiddatacollection;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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

        //Set up the toolbar.
        Toolbar myToolbar = (Toolbar) findViewById(R.id.add_toolbar);
        getLayoutInflater().inflate(R.layout.action_bar_add_new, myToolbar);
        myToolbar.setTitle("");
        setSupportActionBar(myToolbar);

        // Set the logged in text
        TextView loggedInText = (TextView) findViewById(R.id.action_bar_title);
        loggedInText.setText(String.format(getString(R.string.logged_in_text_string),UserVars.UName));

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

        // Link and wire up cancel button.
        Button mCancelButton = (Button) findViewById(R.id.action_bar_cancel);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveToNotebook();
            }
        });
    }

    //endregion

    //region Navigation

    public void goToNew(String type) {
        Intent intent = new Intent();

        if (type.equals("meas")) {
            intent = new Intent(AddNew.this, NewMeas.class);
            intent.putExtra("MODE", "new");
        } else if (type.equals("photo")) {
            intent = new Intent(AddNew.this, NewPhoto.class);
        } else if (type.equals("note")) {
            intent = new Intent(AddNew.this, NewNote.class);
        }

        startActivity(intent);
    }

    public void moveToNotebook() {
        this.finish();
    }

    //endregion

}