package com.beesec.beechat2.watchParty;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.beesec.beechat2.NightMode;
import com.beesec.beechat2.R;

import java.util.HashMap;
import java.util.Objects;

public class PartyPrivacyActivity extends AppCompatActivity {

    String privacy = "public";
    NightMode sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new NightMode(this);
        if (sharedPref.loadNightModeState().equals("night")){
            setTheme(R.style.DarkTheme);
        }else if (sharedPref.loadNightModeState().equals("dim")){
            setTheme(R.style.DimTheme);
        }else setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party_privacy);

        String id = getIntent().getStringExtra("room");

        RadioButton privateP = findViewById(R.id.privateP);
        RadioButton publicP = findViewById(R.id.publicP);

        privateP.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                privacy = "private";
                publicP.setChecked(false);
            }
        });

        publicP.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                privacy = "public";
                privateP.setChecked(false);
            }
        });

        findViewById(R.id.createWeb).setOnClickListener(v -> {
            if (privacy.equals("private")){
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("privacy", "private");
                FirebaseDatabase.getInstance().getReference().child("Party").child(id).updateChildren(hashMap);
                FirebaseDatabase.getInstance().getReference().child("Party").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (Objects.requireNonNull(snapshot.child("type").getValue()).toString().equals("upload_youtube")){
                            Intent intent = new Intent(getApplicationContext(), StartYouTubeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.putExtra("room", id);
                            startActivity(intent);
                            finish();
                        }else {
                            Intent intent = new Intent(getApplicationContext(), StartPartyActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.putExtra("room", id);
                            startActivity(intent);
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }

                });
            }else if (privacy.equals("public")){
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("privacy", "public");
                FirebaseDatabase.getInstance().getReference().child("Party").child(id).updateChildren(hashMap);
                Intent intent = new Intent(getApplicationContext(), PartyPostActivity.class);
                intent.putExtra("room", id);
                startActivity(intent);
                finish();
            }
        });

    }
}