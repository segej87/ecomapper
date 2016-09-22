package com.gmail.jonsege.androiddatacollection;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.content.Intent;

public class MainActivity extends AppCompatActivity {

    TextView mUname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        mUname = (TextView) findViewById(R.id.userText);
        mUname.setText("Logged in as: " + intent.getStringExtra(LoginActivity.UNAME));
    }
}
