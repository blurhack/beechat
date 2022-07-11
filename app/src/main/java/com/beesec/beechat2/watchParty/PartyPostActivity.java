package com.beesec.beechat2.watchParty;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.beesec.beechat2.NightMode;
import com.beesec.beechat2.R;

import java.util.HashMap;
import java.util.Objects;

public class PartyPostActivity extends AppCompatActivity {

    String id;

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
        setContentView(R.layout.activity_party_post);

        id = getIntent().getStringExtra("room");

        findViewById(R.id.imageView).setOnClickListener(v -> FirebaseDatabase.getInstance().getReference().child("Party").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
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
        }));

        EditText bg_text = findViewById(R.id.bg_text);

        findViewById(R.id.next).setOnClickListener(v -> {
            String timeStamp = String.valueOf(System.currentTimeMillis());
            HashMap<Object, String> hashMap = new HashMap<>();
            hashMap.put("id", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
            hashMap.put("pId", timeStamp);
            hashMap.put("text", bg_text.getText().toString());
            hashMap.put("type", "party");
            hashMap.put("meme", "https://firebasestorage.googleapis.com/v0/b/storage-4e152.appspot.com/o/watch.jpeg?alt=media&token=7f9d0bad-7fa0-4c65-8b41-130d7ce0d8f5");
            hashMap.put("vine", id);
            hashMap.put("pTime", timeStamp);
            FirebaseDatabase.getInstance().getReference("Posts").child(timeStamp).setValue(hashMap);
            Snackbar.make(v,"Post Uploaded", Snackbar.LENGTH_LONG).show();

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
            //extra
            postExtra(timeStamp);
        });

    }

    private void postExtra(String timeStamp) {
        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("privacy", "public");
        hashMap.put("feeling", "");
        hashMap.put("location", "");
        FirebaseDatabase.getInstance().getReference("postExtra").child(timeStamp).setValue(hashMap);
    }


}