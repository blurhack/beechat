package com.beesec.beechat2.watchParty;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class StartPartyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String id = getIntent().getStringExtra("room");

        Intent intent = new Intent(getApplicationContext(), WatchPartyActivity.class);
        intent.putExtra("room", id);
        startActivity(intent);
        finish();

    }
}