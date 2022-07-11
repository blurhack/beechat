package com.beesec.beechat2.groupVoiceCall;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.beesec.beechat2.R;
import com.beesec.beechat2.group.GroupChatActivity;
import com.beesec.beechat2.groupVoiceCall.openacall.model.ConstantApp;
import com.beesec.beechat2.groupVoiceCall.openacall.ui.BaseActivity;
import com.beesec.beechat2.groupVoiceCall.openacall.ui.VoiceCallGroupActivity;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class RingingGroupVoiceActivity extends BaseActivity {
    String groupId,room;
    MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling_group_video);

        mp = MediaPlayer.create(this, R.raw.ringtone);
        mp.start();

        groupId = getIntent().getStringExtra("group");
        room = getIntent().getStringExtra("room");

        //Query
        FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Voice").child(room).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (Objects.requireNonNull(snapshot.child("type").getValue()).toString().equals("cancel") && !snapshot.child("end").hasChild(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())){
                    mp.stop();
                    Toast.makeText(RingingGroupVoiceActivity.this, "Canceled", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RingingGroupVoiceActivity.this, GroupChatActivity.class);
                    intent.putExtra("group", groupId);
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

        //GroupInfo
        FirebaseDatabase.getInstance().getReference().child("Groups").child(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                TextView name = findViewById(R.id.name);
                name.setText(Objects.requireNonNull(snapshot.child("gName").getValue()).toString());

                //DP
                CircleImageView dp = findViewById(R.id.dp);
                if (!Objects.requireNonNull(snapshot.child("gIcon").getValue()).toString().isEmpty())  Picasso.get().load(Objects.requireNonNull(snapshot.child("gIcon").getValue()).toString()).into(dp);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        findViewById(R.id.end).setOnClickListener(v -> {
            mp.stop();
            Toast.makeText(RingingGroupVoiceActivity.this, "Declined", Toast.LENGTH_SHORT).show();
            FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Voice").child(room).child("end").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).setValue(true);
            Intent intent = new Intent(RingingGroupVoiceActivity.this, GroupChatActivity.class);
            intent.putExtra("group", groupId);
            intent.putExtra("type", "create");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();

        });

        findViewById(R.id.pick).setOnClickListener(v -> {
            mp.stop();
            FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Voice").child(room).child("ans").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).setValue(true);
            vSettings().mChannelName = room;
            Intent i = new Intent(RingingGroupVoiceActivity.this, VoiceCallGroupActivity.class);
            i.putExtra(ConstantApp.ACTION_KEY_CHANNEL_NAME, room);
            i.putExtra("group", groupId);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();

        });


    }

    @Override
    protected void initUIandEvent() {

    }

    @Override
    protected void deInitUIandEvent() {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mp.stop();
        Toast.makeText(RingingGroupVoiceActivity.this, "Declined", Toast.LENGTH_SHORT).show();
        FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Voice").child(room).child("end").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).setValue(true);
        Intent intent = new Intent(RingingGroupVoiceActivity.this, GroupChatActivity.class);
        intent.putExtra("group", groupId);
        intent.putExtra("type", "create");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

}