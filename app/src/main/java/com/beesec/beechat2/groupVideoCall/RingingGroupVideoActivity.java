package com.beesec.beechat2.groupVideoCall;

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
import com.beesec.beechat2.groupVideoCall.openvcall.model.ConstantApp;
import com.beesec.beechat2.groupVideoCall.openvcall.ui.VideoGroupCallActivity;
import com.beesec.beechat2.groupVoiceCall.openacall.ui.BaseActivity;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class RingingGroupVideoActivity extends BaseActivity {

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
        FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Video").child(room).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (Objects.requireNonNull(snapshot.child("type").getValue()).toString().equals("cancel") && !snapshot.child("end").hasChild(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())){
                    mp.stop();
                    Toast.makeText(RingingGroupVideoActivity.this, "Canceled", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RingingGroupVideoActivity.this, GroupChatActivity.class);
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
            Toast.makeText(RingingGroupVideoActivity.this, "Declined", Toast.LENGTH_SHORT).show();
            FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Video").child(room).child("end").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).setValue(true);
            Intent intent = new Intent(RingingGroupVideoActivity.this, GroupChatActivity.class);
            intent.putExtra("group", groupId);
            intent.putExtra("type", "create");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();

        });

        findViewById(R.id.pick).setOnClickListener(v -> {
            mp.stop();
            FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Video").child(room).child("ans").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).setValue(true);
            vSettings().mChannelName = room;
            String encryption = "AES-128-XTS";
            vSettings().mEncryptionKey = encryption;
            Intent i = new Intent(RingingGroupVideoActivity.this, VideoGroupCallActivity.class);
            i.putExtra(ConstantApp.ACTION_KEY_CHANNEL_NAME, room);
            i.putExtra(ConstantApp.ACTION_KEY_ENCRYPTION_KEY, encryption);
            i.putExtra(ConstantApp.ACTION_KEY_ENCRYPTION_MODE, getResources().getStringArray(R.array.encryption_mode_values)[vSettings().mEncryptionModeIndex]);
            i.putExtra("group", groupId);
            startActivity(i);
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
        Toast.makeText(RingingGroupVideoActivity.this, "Declined", Toast.LENGTH_SHORT).show();
        FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Video").child(room).child("end").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).setValue(true);
        Intent intent = new Intent(RingingGroupVideoActivity.this, GroupChatActivity.class);
        intent.putExtra("group", groupId);
        intent.putExtra("type", "create");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

}