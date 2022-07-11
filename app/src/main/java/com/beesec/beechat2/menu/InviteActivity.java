package com.beesec.beechat2.menu;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.beesec.beechat2.NightMode;
import com.beesec.beechat2.R;

import java.util.HashMap;
import java.util.Objects;

public class InviteActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    NightMode sharedPref;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new NightMode(this);
        if (sharedPref.loadNightModeState().equals("night")){
            setTheme(R.style.DarkTheme);
        }else if (sharedPref.loadNightModeState().equals("dim")){
            setTheme(R.style.DimTheme);
        }else setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite2);

        EditText meetingCreate = findViewById(R.id.meetingCreate);

        findViewById(R.id.imageView).setOnClickListener(v -> {

            meetingCreate.setText("");
            onBackPressed();

        });

        findViewById(R.id.createMeet).setOnClickListener(v -> {
            meetingCreate.setText(""+System.currentTimeMillis());
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("code", meetingCreate.getText().toString());
            hashMap.put("user", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
            FirebaseDatabase.getInstance().getReference("Code").child(meetingCreate.getText().toString()).setValue(hashMap);
            Toast.makeText(this, "Generated", Toast.LENGTH_SHORT).show();

        });

        findViewById(R.id.shareId).setOnClickListener(v -> {
            String shareBody = "Please sign up with this Referral code :- " + meetingCreate.getText().toString() + "\n " + getResources().getString(R.string.app_name)+ " - Friends Social Network" + " Download now on play store \nhttps://play.google.com/store/apps/details?id="+getApplicationContext().getPackageName();
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/*");
            intent.putExtra(Intent.EXTRA_SUBJECT,"Referral code");
            intent.putExtra(Intent.EXTRA_TEXT,shareBody);
            startActivity(Intent.createChooser(intent, "Share Via"));
        });

    }
}