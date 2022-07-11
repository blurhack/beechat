package com.beesec.beechat2.calling;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.beesec.beechat2.R;
import com.beesec.beechat2.chat.ChatActivity;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class RingingActivity extends AppCompatActivity {

    String his;
    String room;
    String call;
    MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ringing);

        mp = MediaPlayer.create(this, R.raw.ringtone);
        mp.start();

        //Room
        room = getIntent().getStringExtra("room");
        his = getIntent().getStringExtra("from");

        //GetRoom
        FirebaseDatabase.getInstance().getReference().child("calling").child(room).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //Type
                String type = Objects.requireNonNull(snapshot.child("type").getValue()).toString();
                call = Objects.requireNonNull(snapshot.child("call").getValue()).toString();
                if (type.equals("cancel")){
                    mp.stop();
                    Toast.makeText(RingingActivity.this, "Canceled", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RingingActivity.this, ChatActivity.class);
                    intent.putExtra("hisUID", his);
                    intent.putExtra("type", "create");
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //User
        FirebaseDatabase.getInstance().getReference().child("Users").child(his).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //Name
                TextView name = findViewById(R.id.name);
                name.setText(Objects.requireNonNull(snapshot.child("name").getValue()).toString());

                //DP
                CircleImageView dp = findViewById(R.id.dp);
                if (!Objects.requireNonNull(snapshot.child("photo").getValue()).toString().isEmpty())  Picasso.get().load(Objects.requireNonNull(snapshot.child("photo").getValue()).toString()).into(dp);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        findViewById(R.id.end).setOnClickListener(v -> {
            mp.stop();
            Toast.makeText(RingingActivity.this, "Declined", Toast.LENGTH_SHORT).show();
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("type", "dec");
            FirebaseDatabase.getInstance().getReference().child("calling").child(room).updateChildren(hashMap);
            Intent intent = new Intent(RingingActivity.this, ChatActivity.class);
            intent.putExtra("hisUID", his);
            intent.putExtra("type", "create");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.pick).setOnClickListener(v -> {
            mp.stop();
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("type", "ans");
            FirebaseDatabase.getInstance().getReference().child("calling").child(room).updateChildren(hashMap);
            //go to call

            if (call.equals("video")){
                //go to call
                Intent intent = new Intent(RingingActivity.this, VideoChatViewActivity.class);
                intent.putExtra("room", room);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }else {
                //go to call
                Intent intent = new Intent(RingingActivity.this, VoiceChatViewActivity.class);
                intent.putExtra("room", room);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();

            }

        });


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mp.stop();
        Toast.makeText(RingingActivity.this, "Declined", Toast.LENGTH_SHORT).show();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("type", "dec");
        FirebaseDatabase.getInstance().getReference().child("calling").child(room).updateChildren(hashMap);
        Intent intent = new Intent(RingingActivity.this, ChatActivity.class);
        intent.putExtra("hisUID", his);
        intent.putExtra("type", "create");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}