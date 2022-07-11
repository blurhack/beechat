package com.beesec.beechat2.watchParty;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.beesec.beechat2.MainActivity;
import com.beesec.beechat2.R;
import com.beesec.beechat2.adapter.AdapterPartyChat;
import com.beesec.beechat2.model.ModelPartyChat;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

@SuppressWarnings("ALL")
public class YouTubePartyActivity extends YouTubeBaseActivity {

    String ky;
    String id;

    EditText sendMessage;
    ImageView send;
    RecyclerView chat_rv;

    //Post
    AdapterPartyChat partyChat;
    List<ModelPartyChat> modelPartyChats;

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(YouTubePartyActivity.this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_you_tube_party);

        id  = getIntent().getStringExtra("room");

        FirebaseDatabase.getInstance().getReference().child("key").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    ky = snapshot.child("key").getValue().toString();
                }else {
                    Toast.makeText(YouTubePartyActivity.this, "Please paste key in firebase", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        YouTubePlayerView youTubePlayerView = findViewById(R.id.YouTubePlayer);

        FirebaseDatabase.getInstance().getReference().child("Party").child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //UserInfo
                FirebaseDatabase.getInstance().getReference().child("Users").child(snapshot.child("from").getValue().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        TextView username = findViewById(R.id.username);
                        username.setText(snapshot.child("username").getValue().toString());
                        String mDp = snapshot.child("photo").getValue().toString();
                        CircleImageView photo = findViewById(R.id.mDp);
                        if (!mDp.isEmpty()){
                            Picasso.get().load(mDp).placeholder(R.drawable.avatar).into(photo);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                TextView live = findViewById(R.id.number);
                live.setText(String.valueOf( snapshot.child("users").getChildrenCount()));

                youTubePlayerView.initialize(ky, new YouTubePlayer.OnInitializedListener() {
                    @Override
                    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                        //Video
                        youTubePlayer.loadVideo(snapshot.child("link").getValue().toString());
                        youTubePlayer.play();
                    }
                    @Override
                    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                    }
                });



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        findViewById(R.id.chat).setOnClickListener(v -> {
            findViewById(R.id.chatView).setVisibility(View.VISIBLE);
            youTubePlayerView.setVisibility(View.GONE);
        });

        findViewById(R.id.imageView).setOnClickListener(v -> {
            findViewById(R.id.chatView).setVisibility(View.GONE);
            youTubePlayerView.setVisibility(View.VISIBLE);
        });

        //Id
        sendMessage = findViewById(R.id.editText);
        send = findViewById(R.id.message_send);
        chat_rv = findViewById(R.id.chat_rv);

        modelPartyChats = new ArrayList<>();

        chat_rv.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        chat_rv.setLayoutManager(linearLayoutManager);

        send.setOnClickListener(v1 -> {
            String msg = sendMessage.getText().toString();
            if (msg.isEmpty()){
                Snackbar.make(v1, "Type a message to send", Snackbar.LENGTH_LONG).show();
            }else {

                String timeStamp = ""+System.currentTimeMillis();
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("ChatId", timeStamp);
                hashMap.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                hashMap.put("msg", msg);
                FirebaseDatabase.getInstance().getReference().child("Party").child(id).child("Chats").child(timeStamp).setValue(hashMap);

                sendMessage.setText("");

            }
        });

        readMessage();

        hideTimer();
        findViewById(R.id.main).setOnClickListener(v -> {
            findViewById(R.id.live_room_top_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.number).setVisibility(View.VISIBLE);
            findViewById(R.id.menu).setVisibility(View.VISIBLE);
            hideTimer();
        });

        findViewById(R.id.invite).setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), InviteMoreActivity.class);
            intent.putExtra("room", id);
            startActivity(intent);
        });

        findViewById(R.id.change).setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ChangeWatchPartyActivity.class);
            intent.putExtra("room", id);
            startActivity(intent);
        });

        findViewById(R.id.close).setOnClickListener(v -> {
            FirebaseDatabase.getInstance().getReference().child("Party").child(id).child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
            String timeStamp = ""+System.currentTimeMillis();
            HashMap<String, Object> hashMap1 = new HashMap<>();
            hashMap1.put("ChatId", timeStamp);
            hashMap1.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
            hashMap1.put("msg", "has left");
            FirebaseDatabase.getInstance().getReference().child("Party").child(id).child("Chats").child(timeStamp).setValue(hashMap1);
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        });

    }

    private void hideTimer() {
        new Handler().postDelayed(() -> {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            findViewById(R.id.live_room_top_layout).setVisibility(View.GONE);
            findViewById(R.id.number).setVisibility(View.GONE);
            findViewById(R.id.menu).setVisibility(View.GONE);
        },2000);
    }

    private void readMessage() {
        DatabaseReference ref =  FirebaseDatabase.getInstance().getReference().child("Party").child(id).child("Chats");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                modelPartyChats.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelPartyChat modelLiveChat = ds.getValue(ModelPartyChat.class);
                    modelPartyChats.add(modelLiveChat);
                }
                partyChat = new AdapterPartyChat(getApplicationContext(), modelPartyChats);
                chat_rv.setAdapter(partyChat);
                partyChat.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

}