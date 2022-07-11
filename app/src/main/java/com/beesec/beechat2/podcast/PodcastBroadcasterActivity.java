package com.beesec.beechat2.podcast;

import android.content.Intent;
import android.os.Bundle;

import com.beesec.beechat2.live.activities.BaseActivity;

import io.agora.rtc.Constants;

@SuppressWarnings("SameParameterValue")
public class PodcastBroadcasterActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gotoLiveActivity(Constants.CLIENT_ROLE_BROADCASTER);
    }
    private void gotoLiveActivity(int role) {
        Intent intent = new Intent(getIntent());
        intent.putExtra(com.beesec.beechat2.live.Constants.KEY_CLIENT_ROLE, role);
        intent.setClass(getApplicationContext(), PodcastActivity.class);
        intent.putExtra("type", getIntent().getStringExtra("type"));
        startActivity(intent);
        finish();
    }
}